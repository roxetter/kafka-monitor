package cn.alfred.openfalcon.kafkamonitor.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Alfred Zhang
 * @version 1.0
 * @class Metric
 * @description
 * @date 2019-07-12 16:07
 **/
@Data
@AllArgsConstructor
public class Metric {

    private String endpoint;
    private String metric;
    private String tags;
    private Object value;
    private int step;
    private long timestamp;
    private String counterType;
}
