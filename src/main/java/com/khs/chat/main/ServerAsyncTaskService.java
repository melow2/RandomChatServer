package com.khs.chat.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service("serverAsyncTaskService")
public class ServerAsyncTaskService{

    private static final Logger logger = LoggerFactory.getLogger(ServerAsyncTaskService.class);

    @PostConstruct
    public void initServerThread() throws Exception {
        RandomChatServer.execute();
    }

}