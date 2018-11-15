package com.xulei.toyproxy.main;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author lei.X
 * @date 2018/11/15
 */

@Component
@Slf4j
public class MainRunner implements CommandLineRunner,InitializingBean {

    @Autowired
    private final SocksServer socksServer;


    public MainRunner(SocksServer socksServer){
        this.socksServer = socksServer;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("服务器配置完毕");
    }

    @Override
    public void run(String... args) throws Exception {

        log.info("[启动器]启动器启动中...");
        socksServer.start();

        log.info("[启动器]启动器启动完成");

    }
}
