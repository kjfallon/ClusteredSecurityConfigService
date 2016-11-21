package edu.syr.eecs.cis.cscs.services;

import edu.syr.eecs.cis.cscs.entities.YumDomain;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

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
            logger.debug("Reading key: " + nameKey);
            domain.setName(clientOps.readKey(nameKey));
            logger.debug("Reading key: " + baseUrlKey);
            domain.setBaseUrl(clientOps.readKey(baseUrlKey));
            logger.debug("Reading key: " + enabledKey);
            domain.setEnabled(clientOps.readKey(enabledKey));
            logger.debug("Domain " + domainName + " name: " + domain.getName());
            logger.debug("Domain " + domainName + " base url: " + domain.getBaseUrl());
            logger.debug("Domain " + domainName + " enabled: " + domain.getEnabled());
            if (StringUtils.isNotEmpty(domain.getName()) && StringUtils.isNotEmpty(domain.getBaseUrl()) &&
                    StringUtils.isNotEmpty(domain.getEnabled())) {
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
