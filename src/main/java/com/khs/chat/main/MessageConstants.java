package com.khs.chat.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

public abstract class MessageConstants {
    public static final String SERVER_SOCKET_OPEN_FAIL = "서버를 시작할 수 없습니다.";
    public static final String CLIENT_CONNECTION_CLOSE = "사용자가 접속을 종료했습니다.";

    protected static Charset charset = Charset.forName("UTF-8");
    protected static CharsetEncoder encoder = charset.newEncoder();
    protected static CharsetDecoder decoder = charset.newDecoder();
    protected static final String REQUIRE_ACCESS = "REQUIRE_ACCESS";
    protected static final String RE_CONNECT = "RE_CONNECT";
    protected static final String MESSAGING = "MESSAGING";
    protected static final String CONNECTION = "CONNECTION";
    protected static final String NEW_CLIENT = "NEW_CLIENT";
    protected static final String QUIT_CLIENT = "QUIT_CLIENT";
    protected static final String MSG_DELIM = "/";
    protected static final String FEMALE="F";
    protected static final String MALE="M";

    protected static final String MSG_WAITING_CLIENT = "낯선사람을 기다리고 있습니다..";
    protected static final String MSG_MEETING_CLIENT = "낯선사람을 만났습니다.";
    protected static final String MSG_QUIT_CLIENT = "낯선사람이 떠났습니다.";
    private static final Logger logger = LoggerFactory.getLogger(MessageConstants.class);

    protected static ByteBuffer parseMessage(String msg) throws CharacterCodingException {
        ByteBuffer buffer = ByteBuffer.allocate(1200*2);
        buffer.clear();
        buffer = encoder.encode(CharBuffer.wrap(msg));
        return buffer;
    }

    public static Object byteBufferToObject(ByteBuffer byteBuffer)
            throws Exception {
        byte[] bytes = new byte[byteBuffer.limit()];
        byteBuffer.get(bytes);
        Object object = deSerializer(bytes);
        return object;
    }

    public static ByteBuffer objectToByteBuffer(Object object) throws Exception {
        ByteBuffer byteBuf = null;
        byteBuf = ByteBuffer.wrap(serializer(object));
        byteBuf.rewind();
        return byteBuf;
    }

    public static Object deSerializer(byte[] bytes) throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
        return objectInputStream.readObject();
    }

    public static byte[] serializer(Object object) throws IOException {
        ObjectOutputStream objectOutputStream = null;
        ByteArrayOutputStream byteArrayOutputStream;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw e;
        } finally {
            if (objectOutputStream != null) {
                objectOutputStream.close();
            }
        }
    }
}
