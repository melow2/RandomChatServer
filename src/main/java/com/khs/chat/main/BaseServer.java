package com.khs.chat.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class BaseServer {
    protected static final String ADDRESS = "localhost";
    protected static final int PORT = 8686;
    protected InetSocketAddress listenAddress;
}
