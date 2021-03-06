package edu.syr.eecs.cis.cscs.configuration;
/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

import edu.syr.eecs.cis.cscs.entities.statemachine.*;

import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.atomix.copycat.server.CopycatServer;
import io.atomix.copycat.server.storage.Storage;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 * @author <a href="http://github.com/kjfallon>Kelly Fallon</a>
 */

@Configuration
public class StateMachineConfig {


    private static Logger logger = LogManager.getLogger();

    @Autowired
    Properties encryptedProperties;

    @Bean
    CopycatServer stateMachineServer() throws Exception {

        logger.info("Starting init of state machine server...");
        List<String> seedHostList =
                Arrays.asList(encryptedProperties.getProperty("stateMachineSeedHostList").split("\\s*,\\s*"));

        // address to bind the server.
        InetAddress ip;
        String bindIP = "";
        Integer bindPort = 5000;

        try {
            ip = InetAddress.getLocalHost();
            bindIP = ip.getHostAddress();
        } catch (UnknownHostException e) {
            logger.error("Error parsing InetAddress");
            e.printStackTrace();
        }

        // if host and port are specified in property file then use them
        if (StringUtils.isNotEmpty(encryptedProperties.getProperty("stateMachineBindHost"))) {
            bindIP = encryptedProperties.getProperty("stateMachineBindHost");
        }
        if (StringUtils.isNotEmpty(encryptedProperties.getProperty("stateMachineBindPort"))) {
            bindPort = Integer.parseInt(encryptedProperties.getProperty("stateMachineBindPort"));
        }
        Address bindAddress = new Address(bindIP, bindPort);

        // Build a list of all seed member addresses to create initial connections to
        List<Address> members = new ArrayList<>();
        for (String hostPortTuple : seedHostList) {
            String[] parts = hostPortTuple.split(":");
            members.add(new Address(parts[0], Integer.valueOf(parts[1])));
        }

        logger.info("Binding CopycatServer to: " + bindIP + ":" + bindPort);
        CopycatServer server = CopycatServer.builder(bindAddress)
                .withStateMachine(MapStateMachine::new)
                .withTransport(NettyTransport.builder()
                        .withThreads(4)
                        .withAcceptBacklog(1024)
                        .build())
                .withStorage(Storage.builder()
                        .withDirectory(encryptedProperties.getProperty("stateMachineFileStoragePath"))
                        .withMaxSegmentSize(1024 * 1024 * 32)
                        .withMinorCompactionInterval(Duration.ofMinutes(1))
                        .withMajorCompactionInterval(Duration.ofMinutes(15))
                        .build())
                .build();

        server.serializer().register(MapPutCommand.class);
        server.serializer().register(MapGetQuery.class);

        server.bootstrap(members).join();

        logger.info("Completed init of state machine server.");
        return server;
    }

}