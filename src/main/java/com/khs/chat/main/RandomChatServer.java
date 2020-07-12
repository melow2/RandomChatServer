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
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.Executors;

public class RandomChatServer extends BaseServer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RandomChatServer.class);

    // 메시지는 개행으로 구분한다.
    private static char CR = (char) 0x0D;
    private static char LF = (char) 0x0A;

    public RandomChatServer() {
        listenAddress = new InetSocketAddress(ADDRESS, PORT);
    }

    @Override
    public void run() {
        serverLog("RandomChatServer Running Start..[ "+listenAddress.getAddress()+":"+listenAddress.getPort()+"]");
        try (Selector selector = Selector.open()) {
            try (ServerSocketChannel serverChannel = ServerSocketChannel.open()) {

                serverChannel.configureBlocking(false);                  // non-Blocking 설정
                serverChannel.socket().bind(listenAddress);              // 서버 ip, port 설정
                serverChannel.register(selector, SelectionKey.OP_ACCEPT);// 채널에 accept 대기 설정

                // 셀렉터가 있을 경우.
                while (selector.select() > 0) {
                    Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                    while (keys.hasNext()) {
                        SelectionKey key = keys.next();
                        keys.remove();
                        if (!key.isValid()) {
                            continue;
                        }
                        // 접속일 경우..
                        if (key.isAcceptable()) {
                            this.accept(selector, key);
                            // 수신일 경우..
                        } else if (key.isReadable()) {
                            this.receive(selector, key);
                            // 발신일 경우..
                        } else if (key.isWritable()) {
                            this.send(selector, key);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 접속시 호출 함수..
    private void accept(Selector selector, SelectionKey key) {
        try {
            ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
            SocketChannel channel = serverChannel.accept(); // accept을 해서 Socket 채널을 가져온다.
            channel.configureBlocking(false);
            Socket socket = channel.socket(); // 소켓 취득
            SocketAddress remoteAddr = socket.getRemoteSocketAddress();
            logger.info("Connected to: " + remoteAddr);
            StringBuffer sb = new StringBuffer();   // 접속 Socket 단위로 사용되는 Buffer;
            sb.append("Welcome server!\r\n>");
            channel.register(selector, SelectionKey.OP_WRITE, sb);      // Socket 채널을 channel에 송신 등록한다
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 수신시 호출 함수..
    private void receive(Selector selector, SelectionKey key) {
        try {
            SocketChannel channel = (SocketChannel) key.channel(); // 키 채널을 가져온다.
            channel.configureBlocking(false); // 채널 Non-blocking 설정
            Socket socket = channel.socket(); // 소켓 취득
            ByteBuffer buffer = ByteBuffer.allocate(1024); // Byte 버퍼 생성
            // ***데이터 수신***
            int size = channel.read(buffer);
            // 수신 크기가 없으면 소켓 접속 종료. 클라이언트 종료 시.
            if (size == -1) {
                SocketAddress remoteAddr = socket.getRemoteSocketAddress();
                System.out.println("Connection closed by client: " + remoteAddr);
                channel.close();
                socket.close();
                key.cancel();
                return;
            }

            byte[] data = new byte[size];
            System.arraycopy(buffer.array(), 0, data, 0, size);
            StringBuffer sb = (StringBuffer) key.attachment(); // StringBuffer 취득
            sb.append(new String(data)); // 버퍼에 수신된 데이터 추가
            // 데이터 끝이 개행 일 경우.
            if (sb.length() > 2 && sb.charAt(sb.length() - 2) == CR && sb.charAt(sb.length() - 1) == LF) {
                sb.setLength(sb.length() - 2); // 개행 삭제
                String msg = sb.toString(); // 메시지를 콘솔에 표시한다.
                System.out.println(msg);
                // exit 경우 접속을 종료한다.
                if ("exit".equals(msg)) {
                    channel.close();
                    socket.close();
                    key.cancel();
                    return;
                }
                sb.insert(0, "Echo - "); // Echo - 메시지> 의 형태로 재 전송.
                sb.append("\r\n>"); // Socket 채널을 channel에 송신 등록한다
                channel.register(selector, SelectionKey.OP_WRITE, sb);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 발신시 호출 함수
    private void send(Selector selector, SelectionKey key) {
        try {
            SocketChannel channel = (SocketChannel) key.channel(); // 키 채널을 가져온다.
            channel.configureBlocking(false); // 채널 Non-blocking 설정
            StringBuffer sb = (StringBuffer) key.attachment(); // StringBuffer 취득
            String data = sb.toString();
            sb.setLength(0); // StringBuffer 초기화
            ByteBuffer buffer = ByteBuffer.wrap(data.getBytes()); // byte 형식으로 변환
            channel.write(buffer);// ***데이터 송신***
            channel.register(selector, SelectionKey.OP_READ, sb); // Socket 채널을 channel에 수신 등록한다
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void execute() {
        Executors.newSingleThreadExecutor().execute(new RandomChatServer());
    }
}
