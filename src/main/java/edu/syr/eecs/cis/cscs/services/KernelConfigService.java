package edu.syr.eecs.cis.cscs.services;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class KernelConfigService {

    private static Logger logger = LogManager.getLogger();

    @Autowired
    Properties encryptedProperties;

    public boolean processKernelConfig() {
        return true;

    }
}
