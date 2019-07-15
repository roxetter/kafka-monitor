package cn.alfred.openfalcon.kafkamonitor;

import cn.alfred.openfalcon.kafkamonitor.domain.Metric;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Alfred Zhang
 * @version 1.0
 * @class Scheduler
 * @description
 * @date 2019-07-11 10:19
 **/
@Slf4j
@Component
public class Scheduler {

    private final static int step = 60;

    private final static String counterType = "GAUGE";

    private final static int maxTryNum = 3;

    private final static RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(3000)
            .setConnectionRequestTimeout(3000)
            .setSocketTimeout(3000).build();

    private final ConfigProperties configProperties;

    public Scheduler(ConfigProperties configProperties) {

        this.configProperties = configProperties;
    }

    @Scheduled(fixedRate = 60000)
    public void run() {

        log.info("配置参数={}", configProperties);

        // 检查配置
        ConfigProperties.Metric[] kpis = configProperties.getMetrics();
        ConfigProperties.Target[] targets = configProperties.getTargets();
        if (kpis == null || kpis.length == 0) {
            log.error("配置错误(未定义指标)");
            System.exit(1);
        }
        if (targets == null || targets.length == 0) {
            log.error("配置错误(未定义监控目标)");
            System.exit(1);
        }

        ConfigProperties.Metric kpi = null;
        List<Metric> metrics = new LinkedList<>();
        for (ConfigProperties.Target target : targets) {
            String url = "service:jmx:rmi:///jndi/rmi://" + target.getJmxHost() + ":" + target.getJmxPort() + "/jmxrmi";
            Map<String, Object> env = new HashMap<>();
            env.put(JMXConnector.CREDENTIALS, new String[]{target.getUsername(), target.getPassword()});
            try (JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(url), env)) {
                MBeanServerConnection connection = connector.getMBeanServerConnection();

                for (int i = 0; i < kpis.length; i++) {
                    kpi = kpis[i];
                    ObjectName name = new ObjectName(kpi.getJmxName());
                    Object value = connection.getAttribute(name, kpi.getJmxAttribute());
                    metrics.add(new Metric(target.getEndpoint(), kpi.getName(), "port=" + target.getKafkaPort(), value, step, 0, counterType));
                }

            } catch (Exception e) {
                log.error("ex,kpi={}", kpi, e);
            }
        }

        // 统一设置时间戳
        long ts = System.currentTimeMillis() / 1000;
        for (Metric metric : metrics) {
            metric.setTimestamp(ts);
        }
        String json = JSON.toJSONString(metrics);
        log.info("metrics={}", json);

        // 调用Open-Falcon接口，上传指标
        reportResult(json);
    }

    private void reportResult(String content) {

        int tryNum = 0;
        boolean success = false;
        while (tryNum++ < maxTryNum) {
            if (doReportResult(content)) {
                success = true;
                break;
            }
        }
        if (success) {
            log.info("上报成功");
        } else {
            log.error("多次重试后上报失败");
        }
    }

    private boolean doReportResult(String content) {

        boolean success = false;

        try (CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build()) {
            HttpPost httpPost = new HttpPost(configProperties.getReportUrl());
            httpPost.setEntity(new StringEntity(content));
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            CloseableHttpResponse response = client.execute(httpPost);
            if (200 == response.getStatusLine().getStatusCode() && "success".equals(EntityUtils.toString(response.getEntity()))) {
                success = true;
            } else {
                log.error("上报失败,response={}", response);
            }
        } catch (IOException ioe) {
            log.error("ex", ioe);
        }
        return success;
    }

}
