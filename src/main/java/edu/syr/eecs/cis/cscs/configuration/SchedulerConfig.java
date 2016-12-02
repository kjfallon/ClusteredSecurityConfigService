package edu.syr.eecs.cis.cscs.configuration;

import edu.syr.eecs.cis.cscs.services.StateMachineClientOperationsService;
import edu.syr.eecs.cis.cscs.services.YumConfigService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Configuration
public class SchedulerConfig {

    private static Logger logger = LogManager.getLogger();

    @Autowired
    YumConfigService yum;

    SchedulerConfig() {
        logger.info("Loaded Scheduler config");
    }

    @Scheduled(cron="0 */5 * * * ?")
    public void processClusteredSecurityConfigMetadata() {

        logger.info("Scheduled task processClusteredSecurityConfigMetadata starting.");
        long startTime = System.nanoTime();

        logger.info("Starting to apply Yum configuration");
        logger.info("result of processYumConfig: " + yum.processYumConfig());
        logger.info("Completed applying Yum configuration");

        long duration = System.nanoTime() - startTime;
        double durationSeconds = (double)duration / 1000000000.0;
        logger.info("Scheduled task processClusteredSecurityConfigMetadata execution time: " + round(durationSeconds, 2) + " seconds ("+ round( (durationSeconds/60) , 2 ) +"min)");
        logger.info("Scheduled task processClusteredSecurityConfigMetadata complete.  Next run will be 5 minutes from this timestamp");

    }

    public static double round(double value, int places) {

        BigDecimal bd = new BigDecimal(value);
        try {
            bd = bd.setScale(places, RoundingMode.HALF_UP);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            logger.error("cannot round to less than 0 places, did not round");
        }
        return bd.doubleValue();
    }

}
