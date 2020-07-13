package com.khs.chat.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class SocketManager extends BaseServer {
    protected static Selector selector; // 어떤 채널이 어떤 IO를 할 수 있는지 알려주는 클래스.
    private static final Logger logger = LoggerFactory.getLogger(SocketManager.class);

    // 접속시 호출 함수..
    protected static void accept(SocketChannel socketChannel) throws IOException {
        socketChannel.configureBlocking(false);
        Socket socket = socketChannel.socket(); // 소켓 취득
        SocketAddress remoteAddr = socket.getRemoteSocketAddress();
        logger.info("Connected to: " + remoteAddr);
        StringBuffer sb = new StringBuffer();   // 접속 Socket 단위로 사용되는 Buffer;
        sb.append("Welcome server!\r\n>");
        socketChannel.register(selector, SelectionKey.OP_WRITE,sb);
    }

    // 수신시 호출 함수..
    protected static void receive(SelectionKey key) {
        SocketChannel channel = (SocketChannel)key.channel();
        Socket socket = channel.socket();
        SocketAddress remoteAddr = socket.getRemoteSocketAddress();
        try {
            channel.configureBlocking(false); // 채널은 블록킹 상태이기때문에 논블럭킹 설정.
            ByteBuffer buffer = ByteBuffer.allocate(2048*2048);
            int size = channel.read(buffer);
            if(size==-1){
                disconnect(channel,key,remoteAddr);
                return;
            }
            byte[] data = new byte[size];
            System.arraycopy(buffer.array(), 0, data, 0, size);
            StringBuffer sb = new StringBuffer();
            sb.append(new String(data)); // 버퍼에 수신된 데이터 추가
            logger.info(sb.toString());
        } catch (IOException e) {
            disconnect(channel,key,remoteAddr);
        }
    }

    // 발신시 호출 함수
    protected static void send(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel(); // 키 채널을 가져온다.
        channel.configureBlocking(false); // 채널 Non-blocking 설정
        StringBuffer sb = (StringBuffer) key.attachment(); // StringBuffer 취득
        String data = sb.toString();
        sb.setLength(0); // StringBuffer 초기화
        ByteBuffer buffer = ByteBuffer.wrap(data.getBytes()); // byte 형식으로 변환
        channel.write(buffer);// ***데이터 송신***
        channel.register(selector, SelectionKey.OP_READ); // Socket 채널을 channel에 수신 등록한다
    }

    protected static void disconnect(SocketChannel channel, SelectionKey key, SocketAddress addr) {
        logger.info(MessageConstants.CLIENT_CONNECTION_CLOSE+": "+addr);
        try {
            channel.socket().close();
            channel.close();
            key.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
