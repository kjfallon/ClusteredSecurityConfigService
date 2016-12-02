package edu.syr.eecs.cis.cscs.services;

import edu.syr.eecs.cis.cscs.entities.YumDomain;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

@Service
public class YumConfigService {

    private static Logger logger = LogManager.getLogger();

    @Autowired
    Properties encryptedProperties;

    @Autowired
    StateMachineClientOperationsService clientOps;

    public boolean processYumConfig() {

        // Check to see if this this the Yum configuration services is enabled
        String enabled = encryptedProperties.getProperty("yumServiceEnable");
        if (StringUtils.isEmpty(enabled) || !enabled.equalsIgnoreCase("true")) {
            logger.debug("Yum configuration service is not enabled, exiting.");
            return true;
        }

        String version = encryptedProperties.getProperty("yumServiceLabel");
        if (StringUtils.isEmpty(version)) {
            logger.error("Yum service enabled but no distribution label was specified");
            return true;
        }

        // Determine which configuration domains this node is a member of
        List<String> domainNameList = new ArrayList<>();
        if (StringUtils.isNotEmpty(encryptedProperties.getProperty("yumServiceDomain"))) {
            domainNameList =
                    Arrays.asList(encryptedProperties.getProperty("yumServiceDomain").split("\\s*,\\s*"));
            logger.debug("This node is a member of " + domainNameList.size() + " config domains");
        }

        // From the distributed cluster read the Yum configuration data for each domain this node is a member of
        List<YumDomain> domainList = new ArrayList<>();
        for (String domainName : domainNameList) {
            YumDomain domain = new YumDomain();
            logger.debug("Reading values for domain " + domainName + " from cluster");
            String nameKey = domainName + ".yum." + version + ".repo.name";
            String baseUrlKey = domainName + ".yum." + version + ".repo.baseurl";
            String enabledKey = domainName + ".yum." + version + ".repo.enabled";

            // Start processing the name key
            logger.debug("Reading key: " + nameKey);
            String nameDataAndSignatureTuple = clientOps.readKey(nameKey);
            String nameValueInBase64 = "";
            String nameSignatureInBase64 = "";
            List<String> nameDataArray = Arrays.asList(nameDataAndSignatureTuple.split(","));
            if (nameDataArray.size() == 2) {
                nameValueInBase64 = nameDataArray.get(0);
                nameSignatureInBase64 = nameDataArray.get(1);
            }
            else {
                logger.error("The value of the key does not look like a comma separated tuple with two values");
            }
            byte[] nameValueInBytes = Base64.getDecoder().decode(nameValueInBase64);
            String nameValue = "";
            try {
                nameValue = new String(nameValueInBytes, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            boolean nameSignatureIsValid = false;
            try {
                nameSignatureIsValid = Crypto.verify(nameValue,
                        nameSignatureInBase64,
                        Crypto.getPublicKey(encryptedProperties.getProperty("pathToSignatureValidationPublicKey")));
            } catch (Exception e) {
                e.printStackTrace();
            }
            logger.debug("signature validation of nameKey data is: " + nameSignatureIsValid);
            domain.setName(nameValue);
            // End processing the name key

            // Start processing the baseUrl key
            logger.debug("Reading key: " + baseUrlKey);
            String baseUrlDataAndSignatureTuple = clientOps.readKey(baseUrlKey);
            String baseUrlValueInBase64 = "";
            String baseUrlSignatureInBase64 = "";
            List<String> baseUrlDataArray = Arrays.asList(baseUrlDataAndSignatureTuple.split(","));
            if (baseUrlDataArray.size() == 2) {
                baseUrlValueInBase64 = baseUrlDataArray.get(0);
                baseUrlSignatureInBase64 = baseUrlDataArray.get(1);
            }
            else {
                logger.error("The value of the key does not look like a comma separated tuple with two values");
            }
            byte[] baseUrlValueInBytes = Base64.getDecoder().decode(baseUrlValueInBase64);
            String baseUrlValue = "";
            try {
                baseUrlValue = new String(baseUrlValueInBytes, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            boolean baseUrlSignatureIsValid = false;
            try {
                baseUrlSignatureIsValid = Crypto.verify(baseUrlValue,
                        baseUrlSignatureInBase64,
                        Crypto.getPublicKey(encryptedProperties.getProperty("pathToSignatureValidationPublicKey")));
            } catch (Exception e) {
                e.printStackTrace();
            }
            logger.debug("signature validation of baseUrlKey data is: " + baseUrlSignatureIsValid);
            domain.setBaseUrl(baseUrlValue);
            // End processing the baseUrl key

            // Start processing the enabled key
            logger.debug("Reading key: " + enabledKey);
            String enabledDataAndSignatureTuple = clientOps.readKey(enabledKey);
            String enabledValueInBase64 = "";
            String enabledSignatureInBase64 = "";
            List<String> enabledDataArray = Arrays.asList(enabledDataAndSignatureTuple.split(","));
            if (enabledDataArray.size() == 2) {
                enabledValueInBase64 = enabledDataArray.get(0);
                enabledSignatureInBase64 = enabledDataArray.get(1);
            }
            else {
                logger.error("The value of the key does not look like a comma separated tuple with two values");
            }
            byte[] enabledValueInBytes = Base64.getDecoder().decode(enabledValueInBase64);
            String enabledValue = "";
            try {
                enabledValue = new String(enabledValueInBytes, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            boolean enabledSignatureIsValid = false;
            try {
                enabledSignatureIsValid = Crypto.verify(enabledValue,
                        enabledSignatureInBase64,
                        Crypto.getPublicKey(encryptedProperties.getProperty("pathToSignatureValidationPublicKey")));
            } catch (Exception e) {
                e.printStackTrace();
            }
            logger.debug("signature validation of enabledKey data is: " + enabledSignatureIsValid);
            domain.setEnabled(enabledValue);
            // End processing the enabled key

            logger.debug("Domain " + domainName + " name: " + domain.getName());
            logger.debug("Domain " + domainName + " base url: " + domain.getBaseUrl());
            logger.debug("Domain " + domainName + " enabled: " + domain.getEnabled());
            if (StringUtils.isNotEmpty(domain.getName())
                    && StringUtils.isNotEmpty(domain.getBaseUrl())
                    && StringUtils.isNotEmpty(domain.getEnabled())
                    && nameSignatureIsValid
                    && baseUrlSignatureIsValid
                    && enabledSignatureIsValid) {
                domainList.add(domain);
            }
            else {
                logger.error("Was not able to read all required values for domain " + domainName + " from cluster");
            }
        }

        writeRepoFiles(version, domainList);

        return true;
    }

    private boolean writeRepoFiles(String version, List<YumDomain> domainList) {

        String repoDirPath = "";
        // for demo one supported distribution
        if (version.equalsIgnoreCase("centos7")) {
            repoDirPath = "/etc/yum.repos.d/";
        }
        else {
            logger.error("Unsupported version for file system updates");
            return false;
        }
        // For each domain create the yum repo file if it does not exist, otherwise update it
        for (YumDomain domain : domainList) {

            String filepath = repoDirPath + "CSCS-Managed-" + domain.getName() + ".repo";
            File file = new File(filepath);
            String content = null;
            if (file.exists()) {
                // update file
                try {
                    content = IOUtils.toString(new FileInputStream(filepath), "UTF-8");
                    content = content.replaceAll("name=.*", "name=" + domain.getName());
                    content = content.replaceAll("baseurl=.*", "baseurl=" + domain.getBaseUrl());
                    content = content.replaceAll("enabled=.*", "enabled=" + domain.getEnabled());
                    IOUtils.write(content, new FileOutputStream(filepath), "UTF-8");
                    logger.debug("Updated domain " + domain.getName() + " repo file: " + filepath);
                } catch (IOException e) {
                    logger.error("error Updating domain " + domain.getName() + " repo file: " + filepath);
                    e.printStackTrace();

                }

            }
            else {
                // create file
                content = "# Managed repo for domain " + domain.getName() + "\n";
                content = content + "\n";
                content = content + "[" + domain.getName() + "]\n";
                content = content + "\n";
                content = content + "name=" + domain.getName() + "\n";
                content = content + "baseurl=" + domain.getBaseUrl() + "\n";
                content = content + "enabled=" + domain.getEnabled() + "\n";
                try {
                    IOUtils.write(content, new FileOutputStream(filepath), "UTF-8");
                    logger.debug("Created domain " + domain.getName() + " repo file: " + filepath);
                } catch (IOException e) {
                    logger.error("error Creating domain " + domain.getName() + " repo file: " + filepath);
                    e.printStackTrace();
                }

            }
        }

        return true;
    }

}
