package com.khs.chat.main;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class SingleChatRoom {
    ArrayList<SocketChannel> socketChannels;
    public SingleChatRoom(SocketChannel socketChannel,String clientInfo){
        this.socketChannels = new ArrayList<>();
        addSocketChannel();
    }

    private void addSocketChannel() {

    }
}
