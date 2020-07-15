package com.khs.chat.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;

import static com.khs.chat.main.MessageConstants.*;

public class RandomChatRoom extends ManagementChatRoom {

    private static RandomChatRoom instance = null;
    protected List<SingleChatRoom> singleChatRooms = new ArrayList<>();
    protected HashMap<SocketChannel, Long> currentSingleChatRoomUsers = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(RandomChatRoom.class);

    public static RandomChatRoom getInstance() throws IOException {
        if (instance == null) {
            instance = new RandomChatRoom();
        }
        return instance;
    }

    @Override
    public Long enterSingleRoom(SocketChannel socketChannel) throws IOException, InterruptedException {
        long seed = System.currentTimeMillis(); // 방번호를 추가.
        if (singleChatRooms.size() == 0) {
            // 생성된 방이 없다면.
            singleChatRooms.add(createNewRoom(socketChannel, seed));
            logger.info("[CREATE_NEW_ROOM] >> *CURRENT_ROOM_SIZE: " + singleChatRooms.size());
            broadcastSingleRoom(socketChannel,seed,CONNECTION,"낯선사람을 기다리고 있습니다..");
            return seed;
        } else {
            int roomPosition = singleChatRooms.size() - 1;
            SingleChatRoom lastRoom = singleChatRooms.get(roomPosition); // 마지막 방.
            Long roomNumber = lastRoom.roomNumber;
            ArrayList<SocketChannel> socketChannels = lastRoom.socketChannels;
            // 마지막 방의 접속자 수가 1명이라면.
            if (socketChannels.size() == 1) {
                lastRoom.addClient(socketChannel);
                currentSingleChatRoomUsers.put(socketChannel, roomNumber); // 사용자 리스트에 추가.
                singleChatRooms.set(roomPosition, lastRoom);
                logger.info("[ENTER] >> ENTER IN ROOM " + socketChannel.getLocalAddress());
                logger.info("[ENTER] >> CURRENT_ROOM_INFO: " + roomNumber + "/ " + socketChannels.size());
                broadcastSingleRoom(socketChannel,roomNumber,NEW_CLIENT,"낯선사람을 만났습니다.");
                return roomNumber;
            } else {
                singleChatRooms.add(createNewRoom(socketChannel, seed));
                logger.info("[CREATE_NEW_ROOM] >> CURRENT_ROOM_SIZE: " + singleChatRooms.size());
                broadcastSingleRoom(socketChannel,seed,CONNECTION,"낯선사람을 기다리고 있습니다..");
                return seed;
            }
        }
    }

    @Override
    public void broadcastSingleRoom(SocketChannel channel,Long roomNumber, String protocol, String message){
//        logger.info("[BROADCAST]: "+roomNumber+"/"+protocol+"/"+message);
        switch (protocol) {
            case CONNECTION:    // 새로운 방 생성.
            case NEW_CLIENT:    // 새로운 사용자 연결.
            case QUIT_CLIENT:   // 사용자가 접속을 종료했을 경우.
                try{
                    for (Map.Entry<SocketChannel, Long> entry : currentSingleChatRoomUsers.entrySet()) {
                        SocketChannel curChannel = entry.getKey();
                        Long curRoomNumber = entry.getValue();
                        if (roomNumber.equals(curRoomNumber)) {
                            ByteBuffer buffer = parseMessage(protocol+MSG_DELIM +roomNumber+MSG_DELIM +message);
                            while (buffer.hasRemaining()) {
                                curChannel.write(buffer);
                            }
                            buffer.compact();
                        }
                    }
                }catch (IOException e){
                    // broken pipe 에러 무시.
                }
                break;
            case MESSAGING:     // 메세지 주고 받음.
                try {
                    for (Map.Entry<SocketChannel, Long> entry : currentSingleChatRoomUsers.entrySet()) {
                        SocketChannel curChannel = entry.getKey();
                        Long curRoomNumber = entry.getValue();
                        if (roomNumber.equals(curRoomNumber) && curChannel != channel) {
                            ByteBuffer buffer = parseMessage(protocol + MSG_DELIM + curRoomNumber + MSG_DELIM + message);
                            while (buffer.hasRemaining())
                                curChannel.write(buffer);
                            buffer.compact();
                        }
                    }
                }catch (IOException e){
                    // broken pipe 에러 무시.
                }
                break;
        }
    }

    private SingleChatRoom createNewRoom(SocketChannel socketChannel, long seed) throws IOException {
        currentSingleChatRoomUsers.put(socketChannel, seed); // 사용자 리스트에 추가.
        SingleChatRoom singleChatRoom = new SingleChatRoom(socketChannel, seed);
        logger.info("[CREATE_NEW_ROOM] >> CREATE ROOM BY " + socketChannel.getLocalAddress());
        return singleChatRoom;
    }
}
