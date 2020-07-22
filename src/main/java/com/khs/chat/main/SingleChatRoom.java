package com.khs.chat.main;

import model.SocketClient;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

public class SingleChatRoom extends SocketManager {
    public Long roomNumber = 0L;
    public Map<SocketChannel, SocketClient> socketClients = new HashMap<>();
    public SingleChatRoom(SocketChannel socketChannel, SocketClient socketClient,Long roomNumber) {
        this.roomNumber = roomNumber;
        addClient(socketChannel, socketClient);
    }
    public void addClient(SocketChannel socketChannel, SocketClient socketClient) {
        socketClients.put(socketChannel, socketClient);
    }
}
