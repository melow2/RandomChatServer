package com.khs.chat.main;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public abstract class ManagementChatRoom {
    public abstract Long enterSingleRoom(SocketChannel socketChannel) throws IOException, InterruptedException;

    public abstract void broadcastSingleRoom(SocketChannel channel, Long roomNumber, String newClient, String protocol) throws IOException, InterruptedException;
}
