package com.khs.chat.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class BaseServer {
    protected static final String ADDRESS = "localhost";
    protected static final int PORT = 8686;
    protected InetSocketAddress listenAddress;

    private static final Logger logger = LoggerFactory.getLogger(BaseServer.class);

    public static void serverLog(String msg){
        logger.info((new SimpleDateFormat("[yy.MM.dd.kk:mm:ss]").format(new Date())+" "+msg));
    }
}
