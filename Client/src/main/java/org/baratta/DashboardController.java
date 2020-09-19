package org.baratta;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.baratta.logic.ChallengeListener;
import org.baratta.logic.ChallengeSender;
import org.baratta.logic.WordQuizzleClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

public class DashboardController {
    @FXML
    private Button addFriend;
    @FXML
    private Button logoutButton;
    @FXML
    private Label usernameLabel;
    @FXML
    private VBox friendListContainer;
    @FXML
    private Label responseLabel;
    @FXML
    private Label wordLabel;
    @FXML
    private Pane notificationList;
    @FXML
    private TextField word;
    @FXML
    private Button wordButton;

    private String username;

    //private  ChallengeListener listener;
    private WordQuizzleClient client;

    private ChallengeListener challengeListener;

    private Notifications notificationsBox;
    public void initDashboard(final Router router, String username) {
        client = WordQuizzleClient.getInstance();
        challengeListener= new ChallengeListener(client.challengeSocket);
        this.username = username;
        notificationsBox = new Notifications(challengeListener);
        notificationList.getChildren().add(notificationsBox);

        updateFriendList();

        notificationsBox.isChallengingProperty().addListener((observableValue, aBoolean, t1) -> {
            if(t1 != null && t1){
                showWord(null);
            }
        });


        startTask(challengeListener);
        wordButton.setOnAction(new EventHandler<>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                handleSendWord();
            }
        });
        usernameLabel.setText(username);
        logoutButton.setOnAction(new EventHandler<>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    challengeListener.cancel();
                    client.logout(username);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                router.logout();
            }
        });
    }

    private void updateFriendList() {
        String replay = client.mostra_classifica(username);
        ObjectMapper mapper = new ObjectMapper();
        LinkedHashMap<String, Integer> friendList;
        List<HBox> friendsLabels = new ArrayList<>();
        try {
            friendList = mapper.readValue(replay, new TypeReference<>() {
            });
            for (String friend : friendList.keySet()) {
                HBox rankEntry = new HBox(8);
                rankEntry.setAlignment(Pos.CENTER_LEFT);
                rankEntry.setPadding(new Insets(5, 5, 5, 80));
                Label label = new Label(friend);
                Label score = new Label(friendList.get(friend).toString());
                if (!friend.equals(username)) {
                    Button challenge = new Button("Sfida");
                    challenge.setOnMouseClicked(new EventHandler<>() {
                        @Override
                        public void handle(MouseEvent mouseEvent) {
                            ChallengeSender challengeSender = new ChallengeSender(username, friend);
                            challengeSender.messageProperty().addListener((observableValue, s, t1) -> {
                                if (t1 != null) {
                                    if (t1.startsWith("OK")) {
                                        showWord(null);
                                        responseLabel.setText("Sfida accettata");
                                    } else if (t1.startsWith("KO")) {
                                        responseLabel.setText("Sfida non accetta");
                                    } else {
                                        responseLabel.setText(t1);
                                    }
                                }
                            });
                            startTask(challengeSender);
                        }
                    });
                    rankEntry.getChildren().addAll(label, score, challenge);
                } else {
                    rankEntry.getChildren().addAll(label, score);
                }
                friendsLabels.add(rankEntry);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        friendListContainer.getChildren().setAll(friendsLabels);
    }

    private void showWord(String response) {
        if (response == null) {
            wordButton.setVisible(true);
            word.setVisible(true);
        }
        String res = client.word(response);
        if (res.startsWith("Sfida terminata") || res.startsWith("Tempo scaduto") || res.startsWith("Sfida non esistente")) {

            notificationsBox.isChallengingProperty().setValue(false);
            updateFriendList();
            hideWord();
            responseLabel.setText(res);
        } else {
            wordLabel.setText(res);
            wordLabel.setVisible(true);
        }
    }

    private void hideWord() {
        wordButton.setVisible(false);
        wordLabel.setVisible(false);
        word.setVisible(false);
    }

    public void handleSendWord() {
        if (!word.getText().isEmpty()) {
            wordButton.setDisable(true);
            showWord(word.getText());
            word.setText("");
            word.requestFocus();
            wordButton.setDisable(false);
        }
    }

    private <V> void startTask(Task<V> task) {
        Thread backgroundThread = new Thread(task);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    public void handleAddFriend() {
        TextInputDialog dialog = new TextInputDialog("Friend's name");
        dialog.setTitle("Add a friend");
        dialog.setHeaderText(null);
        dialog.setContentText(null);
        Optional<String> friend = dialog.showAndWait();
        if (friend.isPresent()) {
            String result = client.aggiungi_amico(username, friend.get());
            updateFriendList();
            responseLabel.setText(result);
        }
    }
}
