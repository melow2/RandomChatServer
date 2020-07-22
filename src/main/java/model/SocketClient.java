package model;

import java.io.Serializable;
import java.nio.channels.SocketChannel;

public class SocketClient implements Serializable {
    private static final long serialVersionUID = 1L;
    private String protocol;    // 프로토콜.
    private String deviceId;    // 기기ID.
    private String gender;      // 성별.
    private Long roomNumber;    // 방번호.
    private String message;     // 메세지 내용
    private String selected;    // 매칭 요구 성별.

    // 서버에 첫 접속시.
    public SocketClient(String protocol, String deviceId, String gender) {
        this.protocol = protocol;
        this.deviceId = deviceId;
        this.gender = gender;
    }

    // 재 연결 시.
    public SocketClient(String protocol, String deviceId, String gender, Long roomNumber, String message, String selected) {
        this.protocol = protocol;
        this.deviceId = deviceId;
        this.gender = gender;
        this.roomNumber =roomNumber;
        this.message = message;
        this.selected =selected;
    }

    // 메세지 주고 받을 시.
    public SocketClient(String protocol, String deviceId, String gender, String message) {
        this.protocol = protocol;
        this.deviceId = deviceId;
        this.gender = gender;
        this.message = message;
    }

    @Override
    public String toString() {
        return "SocketClient{" +
                "protocol='" + protocol + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", gender='" + gender + '\'' +
                ", roomNumber=" + roomNumber +
                ", message='" + message + '\'' +
                ", selected='" + selected + '\'' +
                '}';
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Long getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(Long roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSelected() {
        return selected;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }
}
