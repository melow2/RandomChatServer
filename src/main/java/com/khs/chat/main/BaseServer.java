package com.khs.chat.main;

import java.net.InetSocketAddress;

public abstract class BaseServer {
    protected static final String ADDRESS = "localhost";
    protected static final int PORT = 8686;
    protected InetSocketAddress listenAddress;
}
