package com.khs.chat.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;

public class RandomChatServer extends SocketManager implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RandomChatServer.class);

    public RandomChatServer() throws Exception {
        listenAddress = new InetSocketAddress(ADDRESS, PORT);
        selector = Selector.open();
        ServerSocketChannel channel = ServerSocketChannel.open();
        ServerSocket socket = channel.socket();
        socket.bind(listenAddress);
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
        logger.info("RandomChatServer is ready..[ " + listenAddress.getAddress() + ":" + listenAddress.getPort() + "]");
    }

    @Override
    public void run() {
        logger.info("RandomChatServer Running Start..[ " + listenAddress.getAddress() + ":" + listenAddress.getPort() + "]");
        /*
         * 앞서 생성된 서버소켓채널에 대해 accept 상태 일때 알려달라고 selector에 등록 후 이벤트 대기.
         * 클라이언트 접속 시 selector는 미리 등록했던 ServerSocketChannel에 이벤트가 발생했으므로. select는 1.
         * */
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // 감지된 이벤트가 있다면.
                if (selector.select() > 0) {
                    /*
                     * 현재 selector에 등록된 채널 중 동작이 하나라도 실행 되는 경우.
                     * 그 채널들을 SelectionKey의 Set에 추가 후 채널들의 키를 얻음.
                     * */
                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> iter = keys.iterator();
                    while (iter.hasNext()) {
                        SelectionKey selectionKey = iter.next();
                        iter.remove();
                        SelectableChannel channel = selectionKey.channel(); // 현재 채널이 하고 있는 동작에 대한 파악을 위한 것.
                        if (channel instanceof ServerSocketChannel) {
                            /*
                             * ServerSocketChannel이라면, accept()를 호출해서
                             * 접속 요청을 해온 상대방 소켓과 연결 될 수 있는 SocketChannel을 얻는다.
                             * */
                            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) channel;
                            SocketChannel socketChannel = serverSocketChannel.accept();
                            if (socketChannel == null) {
                                /*
                                 * 현 시점의 ServerSocketChannel은 Non-Blocking 이기에
                                 * 당장 접속이 없어도 블로킹 되지 않고, null을 던지므로 체크를 해줘야한다.
                                 * */
                                continue;
                            }
                            accept(socketChannel);
                        } else {
                            // 일반 소켓 채널인 경우 해당 채널을 얻어낸다.
                            SocketChannel socketChannel = (SocketChannel) channel;
                            executeChannelAction(socketChannel, selectionKey);
                            // serverLog("[클라이언트 이벤트]: ");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void executeChannelAction(SocketChannel socketChannel, SelectionKey selectionKey) throws IOException {
        if (selectionKey.isConnectable()){
            logger.info("[클라이언트와 연결 설정 성공]");
            if(socketChannel.isConnectionPending()){
                logger.info("[클라이언트와 연결 설정 마무리 중]");
                socketChannel.finishConnect();
            }
        }else if(selectionKey.isReadable()){
            receive(selectionKey);
        }else if(selectionKey.isWritable()){
            send(selectionKey);
        }
    }

    public static void execute() throws Exception {
        Executors.newSingleThreadExecutor().execute(new RandomChatServer());
    }
}
