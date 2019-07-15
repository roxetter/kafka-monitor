package cn.alfred.openfalcon.kafkamonitor;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Alfred Zhang
 * @version 1.0
 * @class ConfigProperties
 * @description
 * @date 2019-07-11 16:07
 **/
@Data
@Component
@ConfigurationProperties(prefix = "km")
public class ConfigProperties {

    private Target[] targets;

    private Metric[] metrics;

    private String reportUrl;

    @Data
    public static class Target {

        private String jmxHost;

        private int jmxPort;

        private String username;

        private String password;

        private String endpoint;

        private int kafkaPort;
    }

    @Data
    public static class Metric {

        private String name;

        private String jmxName;

        private String jmxAttribute;

    }

}

