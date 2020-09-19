package org.baratta;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import org.baratta.logic.WordQuizzleClient;

import java.rmi.RemoteException;

public class LoginController {
    public ImageView imageView;
    public Pane content_area;
    @FXML
    private Button loginButton;

    @FXML
    private Label registrationLabel;

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private Label feedback;

    public void initLogin(Router router) {
        loginButton.setOnAction(new EventHandler<>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                String username = null;
                try {
                    username = authorize();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                if (username != null) {
                    router.login(username);
                }
            }
        });
        registrationLabel.setOnMouseClicked(new EventHandler<>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                router.showRegistration();
            }
        });
    }

    private String authorize() throws RemoteException {
        WordQuizzleClient client = WordQuizzleClient.getInstance();
        feedback.setText(client.login(username.getText(), password.getText()));
        feedback.setVisible(true);
        if (feedback.getText().startsWith("OK")) {
            return username.getText();
        }
        return null;
    }
}
