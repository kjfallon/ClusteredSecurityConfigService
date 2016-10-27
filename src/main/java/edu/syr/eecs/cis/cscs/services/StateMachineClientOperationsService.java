package edu.syr.eecs.cis.cscs.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class StateMachineClientOperationsService {

    private static Logger logger = LogManager.getLogger();

    @Autowired
    Properties encryptedProperties;

    public String readKey() {
        String value = "";

        return value;
    }

    public boolean writeKey(String value) {
        boolean result = false;

        return result;
    }

    public boolean deleteKey(String value) {
        boolean result = false;

        return result;
    }

}
