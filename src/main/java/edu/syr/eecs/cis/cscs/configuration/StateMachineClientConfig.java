package edu.syr.eecs.cis.cscs.configuration;


import edu.syr.eecs.cis.cscs.entities.statemachine.*;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.atomix.copycat.client.ConnectionStrategies;
import io.atomix.copycat.client.CopycatClient;
import io.atomix.copycat.client.RecoveryStrategies;
import io.atomix.copycat.client.ServerSelectionStrategies;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Configuration
@DependsOn("stateMachineServer")
public class StateMachineClientConfig {

    private static Logger logger = LogManager.getLogger();

    @Autowired
    Properties encryptedProperties;

    @Bean
    CopycatClient stateMachineClient() {

    logger.info("Starting init of state machine client...");

    // Specify member servers the client will connect to.
    List<Address> members = new ArrayList<>();
    List<String> clientMemberList = new ArrayList<>();
    // if a list of servers is specified in the config then use them, otherwise use the local server instance on port 5000
    if (StringUtils.isNotEmpty(encryptedProperties.getProperty("stateMachineClientMemberList"))) {
        clientMemberList =
                Arrays.asList(encryptedProperties.getProperty("stateMachineClientMemberList").split("\\s*,\\s*"));
        for (String hostPortTuple : clientMemberList) {
            String[] parts = hostPortTuple.split(":");
            members.add(new Address(parts[0], Integer.valueOf(parts[1])));
        }
    }
    else {
        InetAddress ip = null;
        try {
            ip = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            logger.error("Error parsing InetAddress");
            e.printStackTrace();
        }
        members.add(new Address(ip.getHostAddress(), 5000));
    }

    CopycatClient client = CopycatClient.builder()
            .withTransport(new NettyTransport())
            .withConnectionStrategy(ConnectionStrategies.FIBONACCI_BACKOFF)
            .withRecoveryStrategy(RecoveryStrategies.RECOVER)
            .withServerSelectionStrategy(ServerSelectionStrategies.LEADER)
            .build();

    client.serializer().register(MapPutCommand.class);
    client.serializer().register(MapGetQuery.class);

    client.connect(members).join();

    logger.info("Completed init of state machine client.");
    return client;
}
}
