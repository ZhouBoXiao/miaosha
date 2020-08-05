#!/bin/bash

# JVM 参数配置
# -Xms:初始堆大小  -Xmx:最大堆大小
JVM_MEMORY="-Xms2048m -Xmx2048m  -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m"
# 推荐使用G1 (兼顾吞吐量和响应时间的收集器)
JVM_GC="-XX:+UseG1GC -XX:MaxGCPauseMillis=500"
#  PrintHeapAtGC:打印GC前后的详细堆栈信息
JVM_GC_LOG="-XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintHeapAtGC -XX:+PrintGCApplicationStoppedTime -Xloggc:logs/gc.log"
JVM_ARGS=${JVM_MEMORY}" "${JVM_GC}" "${JVM_GC_LOG}" -XX:+HeapDumpOnOutOfMemoryError"

project_path=$(dirname $0)/
pid_file=${project_path}/pid
NAME="miaosha"
VERSION=0.0.1

# 环境不同需要而外配置
export JAVA_HOME=/usr/java/jdk1.8.0_66

function USAGE() {
    cat <<EOF
Usage:
    http.sh <command> [-P key=value] [-JAVA_HOME <java_home>] [-JVM_ARGS <"jvm_args">] [args...]
The commands are:
    start   start service
    stop    stop service
    build   build service
Examples
    http.sh start [-P key=value] [args...]
    http.sh stop
    http.sh build [-P key=value] [args...]
EOF
}

function check_process_is_running() {
    # -f 指文件
    if [[ -f ${pid_file} ]]; then
        pid=$(cat ${pid_file})
        ps ${pid} >/dev/null 2>&1
        return $?
    fi
    return 1
}

command=$1
case ${command} in
start)
    # $# : 参数个数
    if [[ $# -lt 2 ]]; then
        USAGE
        exit 1
    fi
    check_process_is_running
    # $? : 获取上述返回值
    if [[ $? -eq 0 ]]; then
        echo "Service[pid="$(cat pid)"] Already Started, Stop It First"
        exit 11
    fi
    shift 1 # 参数左移一位
    while [[ $# -gt 0 ]]; do
        case $1 in
        -JAVA_HOME)
            export JAVA_HOME=$2
            shift 2
            ;;
        *)
            break
            ;;
        esac
    done
    if [[ -z "${JAVA_HOME}" ]]; then
        echo "Error: JAVA_HOME is not set."
        exit 111
    fi
    export CLASSPATH=${JAVA_HOME}/lib
    export PATH=${JAVA_HOME}/bin:$PATH
    RUN_COMMAND="nohup java ${JVM_ARGS} -jar ./target/${NAME}-${VERISON}-SNAPSHOT.jar >/dev/null 2>&1 &"
    echo "START SERVICE..."
    echo "[Command]" ${RUN_COMMAND}
    nohup java ${JVM_ARGS} -jar ./target/${NAME}-${VERISON}-SNAPSHOT.jar >/dev/null 2>&1 &
    # refresh heart-beat file if it is not empty
    FILE_HEART_BEAT=logs/stat.log
    [[ -s ${FILE_HEART_BEAT} ]] && touch ${FILE_HEART_BEAT}
    echo $! >${pid_file}
    ;;
stop)
    check_process_is_running
    if [[ $? -eq 0 ]]; then
        kill ${pid}
        echo "Stop Service[pid=$pid]..."
        sleep 1
    else
        echo "No Service Is Running"
    fi
    (*)
    echo Unsupported operation: ${command}
    USAGE
    exit 9
    ;;
esac
