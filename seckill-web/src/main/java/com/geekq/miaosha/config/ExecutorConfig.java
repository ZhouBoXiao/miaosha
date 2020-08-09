package com.geekq.miaosha.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

//import toolbox.lang.patch.PatchServlet;

/**
 * 线程池配置
 **/
@Configuration
public class ExecutorConfig {

    @Bean(value = "multiThreadPool")
    public ExecutorService multiThreadPool() {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setDaemon(true)
                .setNameFormat("multi-thread-%d").build();

        ExecutorService pool = MdcThreadPoolExecutor.newWithInheritedMdc("multi-thread", 60, 150, 60000L, TimeUnit.MILLISECONDS,
                                                                         new LinkedBlockingQueue<>(300), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());

        return pool;
    }

//    @Bean
//    public ServletRegistrationBean getServletRegistrationBean() {
//        ServletRegistrationBean bean = new ServletRegistrationBean(new PatchServlet());
//        bean.addUrlMappings("/detectPatch/*");
//        return bean;
//    }

}
