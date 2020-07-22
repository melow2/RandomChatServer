package com.khs.chat.main;

import model.SocketClient;
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
            ByteBuffer readBuffer = ByteBuffer.allocate(1200 * 2); // GC 발생 시 어떻게 대처?
            readBuffer.clear();
            channel.configureBlocking(false); // 채널은 블록킹 상태이기 때문에 논블럭킹 설정.
            int size = channel.read(readBuffer);
            readBuffer.flip();
            if (size == -1) {
                disconnect(channel, key, remoteAddr);
                return;
            }
            SocketClient client = (SocketClient) byteBufferToObject(readBuffer);
            readBuffer.compact();
            messageProcessing(channel, key, client);
        } catch (Exception e) {
            disconnect(channel, key, remoteAddr);
        }
    }

    private static void messageProcessing(SocketChannel channel, SelectionKey key, SocketClient client) throws IOException {
        try {
            RandomChatRoom randomChatRoom = RandomChatRoom.getInstance();
            // logger.info("[RECEIVED]: " + received);
            switch (client.getProtocol()) {
                // 서버 접속 시
                case REQUIRE_ACCESS:
                    randomChatRoom.enterSingleRoom(channel, client);
                    break;
                case MESSAGING:
                    randomChatRoom.broadcastSingleRoom(channel, client.getRoomNumber(), client);
                    break;
                case RE_CONNECT:
//                logger.info("[새로운 사용자 다시 연결]");
                    removeSingleRoom(channel);
                    randomChatRoom.broadcastSingleRoom(channel, client.getRoomNumber(), client);
                    if (client.getSelected().equals(FEMALE) || client.getGender().equals(FEMALE)) {
                        randomChatRoom.enterSingleFemaleRoom(channel, client);
                    } else
                        randomChatRoom.enterSingleRoom(channel, client);
                    break;
            }
        } catch (Exception e) {
            // 메세지 파싱 실패 일 경우 연결 끊음.
            disconnect(channel, key, channel.socket().getLocalSocketAddress());
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

    private static Long removeSingleRoom(SocketChannel channel) throws IOException {
        RandomChatRoom randomChatRoom = RandomChatRoom.getInstance();
        Map<SocketChannel, SocketClient> currentSingleUsers = Collections.synchronizedMap(randomChatRoom.currentSingleChatRoomUsers);
        List<SingleChatRoom> singleChatRooms = Collections.synchronizedList(randomChatRoom.singleChatRooms);
        List<SingleChatRoom> singleChatFemaleRooms = Collections.synchronizedList(randomChatRoom.singleChatFemaleRooms);
        Long roomNumber = currentSingleUsers.get(channel).getRoomNumber();

        for (SingleChatRoom currentRoom : singleChatRooms) {
            if (currentRoom.roomNumber.equals(roomNumber)) {
                singleChatRooms.remove(currentRoom);
                break;
            }
        }

        for (SingleChatRoom currentRoom : singleChatFemaleRooms) {
            if (currentRoom.roomNumber.equals(roomNumber)) {
                singleChatFemaleRooms.remove(currentRoom);
                break;
            }
        }

        return roomNumber;
    }

    protected static void disconnect(SocketChannel channel, SelectionKey key, SocketAddress addr) throws IOException {
        Long roomNumber = removeSingleRoom(channel);
        RandomChatRoom randomChatRoom = RandomChatRoom.getInstance();
        Map<SocketChannel, SocketClient> currentSingleUsers = Collections.synchronizedMap(randomChatRoom.currentSingleChatRoomUsers);
        Iterator it = currentSingleUsers.entrySet().iterator();
        SocketClient target = currentSingleUsers.get(channel);
        currentSingleUsers.remove(channel);
        try {
            channel.socket().close();
            channel.close();
            key.cancel();
            logger.info("***********************************************");
            logger.info(CLIENT_CONNECTION_CLOSE + ": " + addr);
            logger.info("[종료한 사용자 정보] - 방번호: " + String.valueOf(roomNumber));
            logger.info("[현재 사용자]: " + RandomChatRoom.getInstance().currentSingleChatRoomUsers.size());
            logger.info("[현재 랜덤방 ]: " + RandomChatRoom.getInstance().singleChatRooms.size());
            logger.info("[현재 여자방 ]: " + RandomChatRoom.getInstance().singleChatFemaleRooms.size());
            logger.info("***********************************************\n");
            if (target != null) {
                target.setProtocol(QUIT_CLIENT);
                target.setMessage(MSG_QUIT_CLIENT);
                RandomChatRoom.getInstance().broadcastSingleRoom(channel, roomNumber, target);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
