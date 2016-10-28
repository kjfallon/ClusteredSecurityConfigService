package edu.syr.eecs.cis.cscs.services;

import edu.syr.eecs.cis.cscs.entities.statemachine.*;
import io.atomix.copycat.client.CopycatClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@Service
public class StateMachineClientOperationsService {

    private static Logger logger = LogManager.getLogger();

    @Autowired
    Properties encryptedProperties;

    @Autowired
    CopycatClient stateMachineClient;

    // returns the value as a string of the specified key
    public String readKey(String key) {
        Object getResult = stateMachineClient.submit(new MapGetQuery(key)).join();
        logger.debug("MapGetQuery result: " + getResult);

        String value = (getResult != null) ? getResult.toString() : "";
        return value;
    }

    // returns as string the value that was set
    public String writeKey(String key, String value) {
        Object putResult = stateMachineClient.submit(new MapPutCommand(key, value)).join();
        logger.debug("MapPutCommand result: " + putResult);

        String valueResult = (putResult != null) ? putResult.toString() : "";
        return valueResult;

    }

}
