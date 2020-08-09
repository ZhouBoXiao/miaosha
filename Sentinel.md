# Sentinel

## 介绍

Sentinel 是面向分布式服务架构的高可用流量防护组件，主要以流量为切入点，从限流、流量整形、熔断降级、系统负载保护、热点防护等多个维度来帮助开发者保障微服务的稳定性。

Sentinel 具有以下特性:

- **丰富的应用场景**：Sentinel 承接了阿里巴巴近 10 年的双十一大促流量的核心场景，例如秒杀（即突发流量控制在系统容量可以承受的范围）、消息削峰填谷、集群流量控制、实时熔断下游不可用应用等。
- **完备的实时监控**：Sentinel 同时提供实时的监控功能。您可以在控制台中看到接入应用的单台机器秒级数据，甚至 500 台以下规模的集群的汇总运行情况。
- **广泛的开源生态**：Sentinel 提供开箱即用的与其它开源框架/库的整合模块，例如与 Spring Cloud、Dubbo、gRPC 的整合。您只需要引入相应的依赖并进行简单的配置即可快速地接入 Sentinel。
- **完善的 SPI 扩展点**：Sentinel 提供简单易用、完善的 SPI 扩展接口。您可以通过实现扩展接口来快速地定制逻辑。例如定制规则管理、适配动态数据源等。

## 流量控制

对定义的资源名进行流控。

资源是Sentinel 中最重要的一个概念，Sentinel 通过资源来保护具体的业务代码或其他后方服务。Sentinel 把复杂的逻辑给屏蔽掉了，用户只需要为受保护的代码或服务定义一个资源，然后定义规则就可以了，剩下的通通交给sentinel来处理了。并且资源和规则是解耦的，规则甚至可以在运行时动态修改。定义完资源后，就可以通过在程序中埋点来保护你自己的服务了，埋点的方式有两种：

- try-catch 方式（通过 SphU.entry(...)），当 catch 到BlockException时执行异常处理(或fallback)
- if-else 方式（通过 SphO.entry(...)），当返回 false 时执行异常处理(或fallback)



### 定义规则

定义完资源后，就可以来定义限流的规则了，但是我们需要对流控规则做个详细的了解，以便更好的进行限流的操作，流控的规则对应的是 FlowRule。

一条FlowRule有以下几个重要的属性组成：

- resource: 规则的资源名
- grade: 限流阈值类型，qps 或线程数
- count: 限流的阈值
- limitApp: 被限制的应用，授权时候为逗号分隔的应用集合，限流时为单个应用
  - default : 不区分调用者，来自任何调用者的请求都将进行限流统计。
  - {some_origin_name} ： 针对特定的调用者，只有一个来自这个调用者的请求才会进行流量控制。
  - other ： 除了{some_origin_name}以外的其余调用方的流量进行流量控制
- strategy: 基于调用关系的流量控制
  - STRATEGY_DIRECT：直接的调用关系
  - STRATEGY_RELATE：调用相关联
  - STRATEGY_CHAIN：根据调用链来判断
- controlBehavior：流控策略
  - CONTROL_BEHAVIOR_DEFAULT
    - 直接拒绝，默认的流量控制方式，当QPS超过任意规则的阈值后，新的请求就会被立即拒绝，拒绝方式为抛出FlowException。
  - CONTROL_BEHAVIOR_WARM_UP
    - 排队等待，又称为冷启动。通过”冷启动”，让通过的流量缓慢增加，在一定时间内逐渐增加到阈值上限，给冷系统一个预热的时间，避免冷系统被压垮的情况。
  - CONTROL_BEHAVIOR_RATE_LIMITER
    - 慢启动，又称为匀速器模式。这种方式严格控制了请求通过的间隔时间，也即是让请以均匀的速度通过，对应的是漏桶算法。主要用于处理间隔性突发的流量。

#### 默认规则

默认规则的资源名定义为`default_rule`，**系统启动前必须在sentinel-dashboard上添加`default_rule`。**




## 集群流控

集群流控能够精确地控制整个集群的 qps，结合单机限流兜底，可以更好地发挥流量控制的效果。


**集群限流**

代码逻辑在outfox.ead.auth.init.ClusterInitFunc。集群限流的初始化类，采用的是SPI来扩展接口。使用时需要在resources/META-INF/services文件夹下的com.alibaba.csp.sentinel.init.InitFunc中添加ClusterInitFunc的全限定名。

集群流控包含：token server和token client。token server 有两种部署方式：

1. 独立部署，就是单独启动一个 token server 服务来处理 token client 的请求，[sentinel-demo-cluster-server-alone](https://github.com/alibaba/Sentinel/tree/master/sentinel-demo/sentinel-demo-cluster/sentinel-demo-cluster-server-alone)。如果独立部署的 token server 服务挂掉的话，那其他的 token client 就会退化成本地流控的模式，也就是单机版的流控，所以这种方式的集群限流需要保证 token server 的高可用性。

2. 嵌入部署，就是在多个 sentinel-core 中选择一个实例设置为 token server，随着应用一起启动，其他的 sentinel-core 都是集群中 token client。嵌入式部署的模式中，如果 token server 服务挂掉的话，我们可以将另外一个 token client 升级为token server来。Sentinel 提供了一个 api 来进行 token server 与 token client 的切换：

   ```
   http://<ip>:<port>/setClusterMode?mode=<xxx>
   ```

   其中 mode 为 `0` 代表 client，`1` 代表 server，`-1` 代表关闭。



## Sentinel-dashboard

Sentinel 控制台是流量控制、熔断降级规则统一配置和管理的入口，它为用户提供了机器自发现、簇点链路自发现、监控、规则配置等功能。使用 Sentinel 控制台的流程如下：

```
客户端接入 -> 机器自发现 -> 查看簇点链路 -> 配置流控规则 -> 查看流控效果
```


### 规则持久化

- 规则的持久化配置中心可以是redis、nacos、zk、file等等任何可以持久化的数据源，只要能保证更新规则时，客户端能得到通知即可
- 规则的更新可以通过 Sentinel Dashboard 也可以通过各个配置中心自己的更新接口来操作
- AbstractDataSource 中的 SentinelProperty 持有了一个 PropertyListener 接口，最终更新 RuleManager 中的规则是 PropertyListener 去做的



**参考**：

1. [Sentinel 实战-控制台篇](http://www.iocoder.cn/Sentinel/houyi/The-console/)
2. [Sentinel 实战-限流篇](http://www.iocoder.cn/Sentinel/houyi/Sentinel-practical-current-limiting-journal/)
3. [Sentinel实战-规则持久化](http://www.iocoder.cn/Sentinel/houyi/Rule-persistence/)
4. [Sentinel 实战-集群流控](http://www.iocoder.cn/Sentinel/houyi/Cluster-flow-control/)
5. [Sentinel 实战-集群流控环境搭建](http://www.iocoder.cn/Sentinel/houyi/Cluster-flow-control-environment-setup/)
