package com.khs.chat.main;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class SingleChatRoom extends SocketManager {
    public Long roomNumber;
    public ArrayList<SocketChannel> socketChannels = new ArrayList<>();

    public SingleChatRoom(SocketChannel socketChannel, long seed) {
        roomNumber = seed;
        addClient(socketChannel);
    }

    public void addClient(SocketChannel socketChannel){
        socketChannels.add(socketChannel);
    }
}
