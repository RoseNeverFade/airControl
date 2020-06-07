package com.demo.aircontrol;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class SocketClient extends WebSocketClient {

    public SocketClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println( "opened connection" );
    }

    @Override
    public void onMessage(String message) {
        System.out.println( message);
        // send(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        this.send("close");
        System.out.println( "Connection closed by " + ( remote ? "remote peer" : "us" ) );
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

}

