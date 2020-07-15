package com.khs.chat.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service("serverAsyncTaskService")
public class ServerAsyncTaskService {

    private static final Logger logger = LoggerFactory.getLogger(ServerAsyncTaskService.class);

    @PostConstruct
    public void initServerThread() throws Exception {
        RandomChatServer.execute();
    }

}