package com.khs.chat.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;

import static com.khs.chat.main.MessageConstants.*;

public abstract class SocketManager extends BaseServer {
    protected static Selector selector; // 어떤 채널이 어떤 IO를 할 수 있는지 알려주는 클래스.
    private static final Logger logger = LoggerFactory.getLogger(SocketManager.class);

    // 접속시 호출 함수.
    protected static void accept(SocketChannel socketChannel) throws IOException, InterruptedException {
        Socket socket = socketChannel.socket(); // 소켓 취득
        SocketAddress remoteAddr = socket.getRemoteSocketAddress();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ, new StringBuffer());
        logger.info("[ ** Connection Accept: " + remoteAddr + " ** ]");
    }

    // 수신시 호출 함수.
    protected static void receive(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        Socket socket = channel.socket();
        SocketAddress remoteAddr = socket.getRemoteSocketAddress();
        try {
            ByteBuffer readBuffer = ByteBuffer.allocate(1024*10);
            readBuffer.clear();
            channel.configureBlocking(false); // 채널은 블록킹 상태이기 때문에 논블럭킹 설정.
            int size = channel.read(readBuffer);
            readBuffer.flip();
            if (size == -1) {
                disconnect(channel, key, remoteAddr);
                return;
            }
            byte[] data = new byte[size];
            System.arraycopy(readBuffer.array(), 0, data, 0, size);
            String received = new String(data, "UTF-8");
            messageProcessing(channel, received);
        } catch (IOException e) {
            disconnect(channel, key, remoteAddr);
        }
    }

    private static void messageProcessing(SocketChannel channel, String received) throws IOException{
        StringTokenizer tokenizer = new StringTokenizer(received, "/");
        RandomChatRoom randomChatRoom = RandomChatRoom.getInstance();
        String protocol = tokenizer.nextToken();
        logger.info("[RECEIVED]: " + received);
        switch (protocol) {
            // 서버 접속 시
            case REQUIRE_ACCESS:
                String acceptMessage = tokenizer.nextToken();
                logger.info("[클라이언트 정보]: " + acceptMessage);
                randomChatRoom.enterSingleRoom(channel);
                break;
            case MESSAGING:
                String roomNumber = tokenizer.nextToken();
                String message = tokenizer.nextToken();
                String clientInfo = tokenizer.nextToken();
                randomChatRoom.broadcastSingleRoom(channel, Long.valueOf(roomNumber), protocol, message+MSG_DELIM+clientInfo);
                break;
            case RE_CONNECT:
//                logger.info("[새로운 사용자 다시 연결]");
                String currentRoomNumber = tokenizer.nextToken();
                String exitMessage = tokenizer.nextToken();
                removeSingleRoom(channel);
                randomChatRoom.broadcastSingleRoom(channel, Long.valueOf(currentRoomNumber), protocol, exitMessage);
                randomChatRoom.enterSingleRoom(channel);
                break;
        }
    }

    private static Long removeSingleRoom(SocketChannel channel) throws IOException {
        RandomChatRoom randomChatRoom = RandomChatRoom.getInstance();
        Map<SocketChannel, Long> currentSingleUsers = Collections.synchronizedMap(randomChatRoom.currentSingleChatRoomUsers);
        List<SingleChatRoom> singleChatRooms = Collections.synchronizedList(randomChatRoom.singleChatRooms);
        Long roomNumber = currentSingleUsers.get(channel);

        for (SingleChatRoom currentRoom : singleChatRooms) {
            if (currentRoom.roomNumber.equals(roomNumber) || currentRoom.socketChannels.contains(channel)) {
                singleChatRooms.remove(currentRoom);
                break;
            }
        }
        return roomNumber;
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

    protected static void disconnect(SocketChannel channel, SelectionKey key, SocketAddress addr) throws IOException {
        Long roomNumber = removeSingleRoom(channel);
        Iterator it = RandomChatRoom.getInstance().currentSingleChatRoomUsers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<SocketChannel, Long> item = (Map.Entry<SocketChannel, Long>) it.next();
            if (item.getKey().equals(channel)) {
                it.remove();
            }
        }
        try {
            channel.socket().close();
            channel.close();
            key.cancel();
            logger.info("***********************************************");
            logger.info(CLIENT_CONNECTION_CLOSE + ": " + addr);
            logger.info("[종료한 사용자 정보] - 방번호: " + String.valueOf(roomNumber));
            logger.info("[현재 사용자]: " + RandomChatRoom.getInstance().currentSingleChatRoomUsers.size());
            logger.info("[현재 채팅방 ]: " + RandomChatRoom.getInstance().singleChatRooms.size());
            logger.info("***********************************************\n");
            RandomChatRoom.getInstance().broadcastSingleRoom(channel, roomNumber, QUIT_CLIENT, MSG_QUIT_CLIENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
