package edu.syr.eecs.cis.cscs.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class YumConfigService {

    private static Logger logger = LogManager.getLogger();

    @Autowired
    Properties encryptedProperties;

    @Autowired
    StateMachineClientOperationsService clientOps;

    public boolean processYumConfig() {
        return true;

    }
}
