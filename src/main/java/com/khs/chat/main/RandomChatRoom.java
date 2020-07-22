package com.khs.chat.main;

import model.SocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.html.HTMLDocument;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;

import static com.khs.chat.main.MessageConstants.*;

public class RandomChatRoom extends ManagementChatRoom {

    private static RandomChatRoom instance = null;
    protected List<SingleChatRoom> singleChatRooms = Collections.synchronizedList(new ArrayList<>());
    protected List<SingleChatRoom> singleChatFemaleRooms = Collections.synchronizedList(new ArrayList<>());
    protected Map<SocketChannel, SocketClient> currentSingleChatRoomUsers = Collections.synchronizedMap(new HashMap<>());
    private static final Logger logger = LoggerFactory.getLogger(RandomChatRoom.class);

    public static RandomChatRoom getInstance() throws IOException {
        if (instance == null) {
            instance = new RandomChatRoom();
        }
        return instance;
    }

    @Override
    public void enterSingleRoom(SocketChannel socketChannel, SocketClient client) throws IOException {
        long seed = System.currentTimeMillis(); // 방번호를 추가.
        if (singleChatRooms.size() == 0) {
            // 생성된 방이 없다면.
            client.setRoomNumber(seed);
            singleChatRooms.add(createNewRoom(socketChannel, client, seed));
            client.setProtocol(CONNECTION);
            client.setMessage(MSG_WAITING_CLIENT);
            broadcastSingleRoom(socketChannel, seed, client);
        } else {
            int roomPosition = singleChatRooms.size() - 1;
            SingleChatRoom lastRoom = singleChatRooms.get(roomPosition);                                // 마지막 방.
            Map<SocketChannel, SocketClient> socketClients = lastRoom.socketClients;                    // 마지막 방의 참여자 리스트.
            Map.Entry<SocketChannel, SocketClient> entry = socketClients.entrySet().iterator().next();  // 마지막 방 첫번째 참여자 정보
            SocketChannel key = entry.getKey();     // 마지막 방 첫번째 참여자 채널 키.
            SocketClient value = entry.getValue();  // 마지막 방 첫번째 참여자 클라이언트 정보.
            // 마지막 방의 접속자 수가 1명이라면.
            if (socketClients.size() == 1) {
                client.setRoomNumber(value.getRoomNumber());
                currentSingleChatRoomUsers.put(socketChannel, client); // 사용자 리스트에 추가.
                lastRoom.addClient(socketChannel, client);
                singleChatRooms.set(roomPosition, lastRoom);
                client.setProtocol(NEW_CLIENT);
                client.setMessage(MSG_MEETING_CLIENT);
                broadcastSingleRoom(socketChannel, value.getRoomNumber(), client);
            } else {
                client.setRoomNumber(seed);
                singleChatRooms.add(createNewRoom(socketChannel, client, seed));
                client.setProtocol(CONNECTION);
                client.setMessage(MSG_WAITING_CLIENT);
                broadcastSingleRoom(socketChannel, seed, client);
            }
        }
    }

    @Override
    public void enterSingleFemaleRoom(SocketChannel socketChannel, SocketClient client) throws IOException {
        long seed = System.currentTimeMillis(); // 방번호를 추가.
        if (singleChatFemaleRooms.size() == 0 && client.getSelected().equals(FEMALE)) {
            // 생성된 방이 없고, 여자를 선택했다면. => 방만들고 기다리기.
            client.setRoomNumber(seed);
            singleChatFemaleRooms.add(createNewRoom(socketChannel, client, seed));
            client.setProtocol(CONNECTION);
            client.setMessage(MSG_WAITING_CLIENT);
            broadcastSingleRoom(socketChannel, seed, client);
        } else if(singleChatFemaleRooms.size() ==0 && !client.getSelected().equals(FEMALE)) {
            // 생성된 방도 없고, 여자를 선택한 경우가 아니라면. => 단순히 여자라서 먼저 들어온 경우.
            enterSingleRoom(socketChannel,client);
        }else{
            // 생성된 방이 있을 경우
            boolean flag = false;
            for (int i = 0; i < singleChatFemaleRooms.size(); i++) {
                SingleChatRoom singleChatRoom = singleChatFemaleRooms.get(i);
                Map<SocketChannel, SocketClient> list = singleChatRoom.socketClients;
                if (list.size() == 1) {
                    // 방에 1명이 있을 경우
                    Map.Entry<SocketChannel, SocketClient> entry = list.entrySet().iterator().next();  // 마지막 방 첫번째 참여자 정보
                    SocketClient temp = entry.getValue();
                    if (temp.getSelected().equals(client.getGender()) && temp.getGender().equals(client.getSelected())) {
                        // 상대방이 원하는 성별과, 나의 성별이 일치 할 때.
                        // 내가 원하는 성별과, 상대방의 성별이 일치 할 때.
                        client.setRoomNumber(singleChatRoom.roomNumber);
                        currentSingleChatRoomUsers.put(socketChannel, client); // 사용자 리스트에 추가.
                        singleChatRoom.addClient(socketChannel, client);
                        singleChatFemaleRooms.set(i, singleChatRoom);
                        client.setProtocol(NEW_CLIENT);
                        client.setMessage(MSG_MEETING_CLIENT);
                        broadcastSingleRoom(socketChannel, singleChatRoom.roomNumber, client);
                        flag = true;
                        break;
                    }
                }
            }

            if(!flag) {
                // 방을 찾지 못했을 경우.
                if (client.getSelected().equals(FEMALE)) {
                    // 여자를 선택했다면. => 방만들고 기다리기.
                    client.setRoomNumber(seed);
                    singleChatFemaleRooms.add(createNewRoom(socketChannel, client, seed));
                    client.setProtocol(CONNECTION);
                    client.setMessage(MSG_WAITING_CLIENT);
                    broadcastSingleRoom(socketChannel, seed, client);
                }else{
                    enterSingleRoom(socketChannel,client);
                }
            }
        }

    }

    @Override
    public void broadcastSingleRoom(SocketChannel channel, Long roomNumber, SocketClient client) {
//        logger.info("[BROADCAST]: "+roomNumber+"/"+protocol+"/"+message);
        switch (client.getProtocol()) {
            case CONNECTION:    // 새로운 방 생성.
            case NEW_CLIENT:    // 새로운 사용자 연결.
            case QUIT_CLIENT:   // 사용자가 접속을 종료했을 경우.
                try {
                    for (Map.Entry<SocketChannel, SocketClient> entry : currentSingleChatRoomUsers.entrySet()) {
                        SocketChannel curChannel = entry.getKey();
                        SocketClient curClient = entry.getValue();
                        if (roomNumber.equals(curClient.getRoomNumber())) {
                            ByteBuffer buffer = objectToByteBuffer(client);
                            curChannel.write(buffer);
                            buffer.compact();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // broken pipe 에러 무시.
                }
                break;
            case RE_CONNECT:
            case MESSAGING:     // 메세지 주고 받음.
                try {
                    for (Map.Entry<SocketChannel, SocketClient> entry : currentSingleChatRoomUsers.entrySet()) {
                        SocketChannel curChannel = entry.getKey();
                        SocketClient curClient = entry.getValue();
                        if (roomNumber.equals(curClient.getRoomNumber()) && curChannel != channel) {
                            ByteBuffer buffer = objectToByteBuffer(client);
                            curChannel.write(buffer);
                            buffer.compact();
                        }
                    }
                } catch (Exception e) {
                    // e.printStackTrace();
                    // broken pipe 에러 무시.
                }
                break;
        }
    }

    private SingleChatRoom createNewRoom(SocketChannel socketChannel, SocketClient client, long seed) throws IOException {
        currentSingleChatRoomUsers.put(socketChannel, client); // 사용자 리스트에 추가.
        SingleChatRoom singleChatRoom = new SingleChatRoom(socketChannel, client, seed);
        return singleChatRoom;
    }
}
