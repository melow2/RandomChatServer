package com.khs.chat.main;

import model.SocketClient;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public abstract class ManagementChatRoom {
    public abstract void enterSingleRoom(SocketChannel socketChannel, SocketClient seed) throws IOException, InterruptedException;
    public abstract void enterSingleFemaleRoom(SocketChannel socketChannel, SocketClient seed) throws IOException, InterruptedException;
    public abstract void broadcastSingleRoom(SocketChannel channel,Long roomNumber, SocketClient clientMessage) throws IOException, InterruptedException;
}
