package edu.syr.eecs.cis.cscs.services;

import edu.syr.eecs.cis.cscs.entities.YumDomain;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

        return true;
    }
}
