package org.baratta;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.baratta.logic.ChallengeListener;
import org.baratta.logic.ChallengeResponse;
import org.baratta.logic.Notification;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.Callable;

public class Notifications extends VBox {
    private final SimpleBooleanProperty isChallenging;

    public Notifications() {
        isChallenging = new SimpleBooleanProperty(false);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("notifications.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Notifications(Task worker) {
        this();
        bindToWorker((ChallengeListener) worker);
    }

    private void bindToWorker(ChallengeListener worker) {
        worker.getNotifications().addListener((ListChangeListener<? super Notification>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    List<? extends Notification> notifications = change.getAddedSubList();
                    for (Notification notification : notifications) {
                        Label message = new Label(notification.toString());
                        Button accept = new Button("Accept");
                        Button decline = new Button("Decline");
                        HBox notificationBox = new HBox(message, accept, decline);
                        decline.setOnAction(new DeclineHandler(worker,notification.getAddress(),notification.getPort(),() -> {
                            getChildren().remove(notificationBox);
                            return null;
                        }));
                        accept.setOnAction(new AcceptHandler(worker, notification.getAddress(), notification.getPort(), isChallenging, () -> {
                            getChildren().remove(notificationBox);
                            return null;
                        }));
                        getChildren().add(notificationBox);
                    }
                }
            }
        });
    }

    public SimpleBooleanProperty isChallengingProperty() {
        return isChallenging;
    }

    private static class AcceptHandler implements EventHandler<ActionEvent> {
        private final SimpleBooleanProperty isChallenging;
        private final Callable<Void> removeNotification;

        ChallengeResponse senderOk;

        public AcceptHandler(ChallengeListener worker, InetAddress address, int port, SimpleBooleanProperty isChallenging, Callable<Void> o) {
            this.isChallenging=isChallenging;
            this.removeNotification = o;
            this.senderOk = new ChallengeResponse(worker.getSocket(), address, port,"OK");
        }


        @Override
        public void handle(ActionEvent actionEvent) {
            startTask();
            isChallenging.setValue(true);
            try {
                removeNotification.call();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        private void startTask() {
            Thread backgroundThread = new Thread(senderOk);
            backgroundThread.setDaemon(true);
            backgroundThread.start();
        }
    }

    private static class DeclineHandler implements EventHandler<ActionEvent> {
        private final Callable<Void> removeNotification;
        ChallengeResponse senderKO;

        public DeclineHandler(ChallengeListener worker, InetAddress address, int port, Callable<Void> o) {

            this.removeNotification = o;
            this.senderKO = new ChallengeResponse(worker.getSocket(), address, port,"KO");
        }


        @Override
        public void handle(ActionEvent actionEvent) {
            startTask();
            try {
                removeNotification.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
          
        }
        private void startTask() {
            Thread backgroundThread = new Thread(senderKO);
            backgroundThread.setDaemon(true);
            backgroundThread.start();
        }
    }

}
