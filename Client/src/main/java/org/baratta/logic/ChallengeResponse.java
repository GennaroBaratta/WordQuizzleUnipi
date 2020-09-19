package org.baratta.logic;

import javafx.concurrent.Task;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ChallengeResponse extends Task<Void> {
    private final DatagramSocket socket;
    final InetAddress address;
    final int port;
    final String response;

    public ChallengeResponse(DatagramSocket socket,InetAddress address,int port,String response){
        this.socket=socket;
        this.address=address;
        this.port=port;
        this.response=response;
    }

    @Override
    protected Void call() {
        sendOk(address,port);
        return null;
    }

    public void sendOk(InetAddress address, int port) {
        byte[] ackbuf;
        ackbuf = response.getBytes();
        DatagramPacket ack = new DatagramPacket(ackbuf, ackbuf.length, address, port);
        try {
            socket.send(ack);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
