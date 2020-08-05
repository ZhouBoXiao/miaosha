# 秒杀系统

[![Travis](https://img.shields.io/badge/language-Java-yellow.svg)](https://github.com/qiurunze123)

## 介绍
高并发大流量下的秒杀架构

## 部署说明
### 代码下载
```shell script
git clone -b <对应的tag> https://github.com/ZhouBoXiao/miaosha.git
```
### 编译打包
修改根目录下http.sh中的jdk配置
```shell script
#如果本地环境不是jdk8，需要配置该项目
#export JAVA_HOME=/usr/java/jdk1.8.0_66/
#export CLASSPATH=${JAVA_HOME}/lib
#export PATH=${JAVA_HOME}/bin:$PATH
```

```shell script
mvn clean
mvn package  -Dmaven.test.skip=true
```

### docker环境搭建
docker compose安装
方法一：
```shell script
#下载
sudo curl -L https://github.com/docker/compose/releases/download/1.26.2/docker-compose-`uname -s`-`uname -m` -o /usr/local/bin/docker-compose
#安装
chmod +x /usr/local/bin/docker-compose
#查看版本
docker-compose version
```

方法二：
```shell script
#安装docker-compose
pip install docker-compose 
#查看版本
docker-compose version
```
docker-compose.yml 文件,内容如下：

```yaml
version: '2'
services:
  zookeeper:
    image: wurstmeister/zookeeper
    ports:
      - "2181:2181"
  kafka:
    image: wurstmeister/kafka
    links:
      - zookeeper:zk
    ports:
      - "9002:9092"
    depends_on:
      - zookeeper
    environment:
      KAFKA_ADVERTISED_HOST_NAME: 192.168.2.107
      KAFKA_ADVERTISED_PORT: "9092"
      KAFKA_ZOOKEEPER_CONNECT: zk:2181
  redis:
    image: redis
    ports:
      - "6379:6379"
  mysql:
    image: mysql
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: 'password'

使用`docker-compose up -d`在后台启动服务
使用`docker-compose ps`查看启动的服务
使用`docker-compose stop` 停止服务
```
### 启动脚本
./http.sh start service启动，

### 终止方法
执行./http.sh stop

## 技术方案

### 日志优化之MDC

### 分布式锁




