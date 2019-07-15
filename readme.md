# kafka-monitor

Open-Falcon的kafka插件，基于JMX接口（要求被监控的kafka开启JMX服务，开启方法可参看下文）。

## 用法

1. 执行sh build.sh
1. 将deploy目录下的kafka-monitor复制到目标服务器
1. 修改kafka-monitor/config/application-product.properties中的配置参数  
1. 执行sh restart.sh

## 配置参数

### km.reportUrl

上报地址，目前为Open-Falcon Agent对外开放的推送地址

> 例：http://127.0.0.1:1988/v1/push

### km.targets[n]

监控目标，n从0开始递增，后跟以下配置参数

#### jmxHost

目标所在地址，IP或主机名

> 例：192.168.1.120

#### jmxPort

目标开放的JMX端口

> 例：13001

#### username

目标JMX服务的用户名，未设置鉴权时留空

> 例：user

#### password

目标JMX服务的密码，未设置鉴权时留空

> 例：secret

#### endpoint

目标上报时的endpoint参数值，填主机名

> 例：host120

#### kafkaPort

目标的kafka服务端口，该参数用于为指标设置tag，区分同一服务器上的多个kafka实例

> 例：9202

### km.metrics[n]

指标定义，n从0开始递增，后跟以下配置参数

#### name

指标名，最终上报的名称，该名称可按需定义

> 例：kafka.broker.BytesInPerSec

#### jmxName

指标的原始JMX名称

> 例：kafka.server:type=BrokerTopicMetrics,name=BytesInPerSec

#### jmxAttribute

指标的原始JMX属性名

> 例：Count

## KAFKA开放JMX服务

### 不开鉴权

修改bin/kafka-run-class.sh，搜索KAFKA_JMX_OPTS，修改为：

> KAFKA_JMX_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=13001 -Dcom.sun.management.jmxremote.authenticate=false  -Dcom.sun.management.jmxremote.ssl=false"

其中，"-Dcom.sun.management.jmxremote.port"指定JMX端口。

### 开启鉴权

将以上的"-Dcom.sun.management.jmxremote.authenticate=false"替换为：

> -Dcom.sun.management.jmxremote.authenticate=true -Dcom.sun.management.jmxremote.access.file=/路径/jmx.access -Dcom.sun.management.jmxremote.password.file=/路径/jmx.passwd

将其中的"/路径"替换为某个固定的路径，并在该路径下创建两个文件jmx.access和jmx.passwd

#### jmx.access

该文件为账号权限文件，内容设置为：

> user readonly

表示用户"user"的权限为直读。

#### jmx.passwd

该文件为账号密码文件，内容设置为：

> user secret

表示用户"user"的密码是"secret"。

> 重要：jmx.passwd的mode必须设为600，即只有本用户可以读写，否则会导致KAFKA启动失败！
>> chmod 600 jmx.passwd

## JMX指标名称及释义

### 获取所有指标名称

使用jconsole连接kafka，查看MBean，找出kafka开头的bean

### 指标释义

部分重要指标释义可参考以下链接：

> https://docs.confluent.io/1.0/kafka/monitoring.html  
> https://www.datadoghq.com/blog/monitoring-kafka-performance-metrics/
