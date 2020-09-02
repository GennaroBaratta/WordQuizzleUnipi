package org.baratta.logic;

import java.net.InetAddress;

//richiesta amicizia o richiesta sfida
public class Notification implements Comparable<Notification> {
    private final String fromUser;
    private final String req;
    private  final InetAddress address;
    private final int port;

    public Notification(String fromUser, String req, InetAddress address, int port) {
        this.fromUser = fromUser;
        this.req = req;
        this.address=address;
        this.port=port;
    }

    public final String getFromUser() {
        return fromUser;
    }

    public final String getReq() {
        return req;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "Una nuova "+req+" da "+fromUser;
    }

    @Override
    public int compareTo(Notification n) {
        int diff = this.getFromUser().compareTo(n.getFromUser());
        if (diff == 0) {
            diff = this.getReq().compareTo(n.getReq());
        }
        return diff;
    }
}
