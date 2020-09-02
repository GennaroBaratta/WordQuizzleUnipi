package org.baratta.logic;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.StringTokenizer;

public class ChallengeListener extends Task<ReadOnlyListProperty<Notification>> {
    private final ReadOnlyListWrapper<Notification> notificationsWrapper;
    private final ReadOnlyListProperty<Notification> notifications;
    private final DatagramSocket socket;
    private final byte[] buf = new byte[256];

    public ChallengeListener(DatagramSocket socket) {
        this.socket = socket;
        notificationsWrapper = new ReadOnlyListWrapper<>(FXCollections.observableArrayList());
        notifications = notificationsWrapper.getReadOnlyProperty();
    }

    @Override
    protected ReadOnlyListProperty<Notification> call() {
        while (!this.isCancelled()) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                packet = new DatagramPacket(buf, buf.length, address, port);
                String received = new String(packet.getData(), 0, packet.getLength());
                System.out.println(received);

                StringTokenizer tokenizer = new StringTokenizer(received);
                if (tokenizer.nextToken().startsWith("CHALLENGE")) {
                    DatagramPacket finalPacket = packet;
                    Platform.runLater(() -> {
                        notificationsWrapper.add(new Notification(tokenizer.nextToken().trim(), "CHALLENGE", finalPacket.getAddress(), finalPacket.getPort()));
                    });
                }
            } catch (IOException e) {
                if (this.isCancelled() || socket.isClosed()) {
                    break;
                }
                e.printStackTrace();
            }
        }
        return notifications;
    }


    public ReadOnlyListProperty<Notification> getNotifications() {
        return notifications;
    }

    public DatagramSocket getSocket() {
        return this.socket;
    }
}
