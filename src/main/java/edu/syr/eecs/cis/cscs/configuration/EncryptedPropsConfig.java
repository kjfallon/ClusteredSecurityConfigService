package edu.syr.eecs.cis.cscs.configuration;

import org.apache.logging.log4j.LogManager;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.properties.EncryptableProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.Security;
import java.util.Properties;

// This class allows loading additional properties from a file named encrypted.properties.
// The value of these properties may be encrypted.  Decryption at runtime depenends on the
// passphrase being independently distributed to the target environment and available as the
// environment variable APPSRV_KEY.

@Configuration
public class EncryptedPropsConfig {

    private static org.apache.logging.log4j.Logger logger = LogManager.getLogger();

    @Bean
    Properties encryptedProperties() {

        Security.addProvider(new BouncyCastleProvider());
        StandardPBEStringEncryptor BCencryptor = new StandardPBEStringEncryptor();
        BCencryptor.setProviderName("BC");
        BCencryptor.setAlgorithm("PBEWITHSHA256AND256BITAES-CBC-BC");
        BCencryptor.setPassword(System.getenv("APPSRV_KEY"));

        Properties props = new EncryptableProperties(BCencryptor);
        try {
            props.load(this.getClass().getClassLoader().getResourceAsStream("encrypted.properties"));
            logger.info("Successfully loaded encrypted props");
        }
        catch (Exception e) {
            logger.error("Failed to load encrypted.properties file. Make sure the classpath includes it.");
        }

        return props;
    }

}
