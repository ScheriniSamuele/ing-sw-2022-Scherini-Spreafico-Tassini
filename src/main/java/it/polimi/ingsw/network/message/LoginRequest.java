package it.polimi.ingsw.network.message;

public class LoginRequest extends Message{
    public LoginRequest(String nickname) {
        super(nickname, MessageType.LOGIN_REQUEST);
    }
}