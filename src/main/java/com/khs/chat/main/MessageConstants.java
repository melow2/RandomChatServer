package com.khs.chat.main;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public abstract class MessageConstants {
    public static final String SERVER_SOCKET_OPEN_FAIL = "서버를 시작할 수 없습니다.";
    public static final String CLIENT_CONNECTION_CLOSE = "사용자가 접속을 종료했습니다.";

    protected static Charset charset = Charset.forName("UTF-8");
    protected static CharsetEncoder encoder = charset.newEncoder();

    protected static final String REQUIRE_ACCESS = "REQUIRE_ACCESS";
    protected static final String MESSAGING = "MESSAGING";
    protected static final String CONNECTION ="CONNECTION";
    protected static final String NEW_CLIENT ="NEW_CLIENT";
    protected static final String QUIT_CLIENT ="QUIT_CLIENT";
    protected static final String MSG_DELIM="/";

    protected static ByteBuffer parseMessage(String msg) throws CharacterCodingException {
        ByteBuffer buffer = ByteBuffer.allocate(2048*2048);
        buffer.clear();
        buffer = encoder.encode(CharBuffer.wrap(msg));
        return buffer;
    }

}
