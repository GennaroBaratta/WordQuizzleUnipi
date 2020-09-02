package org.baratta;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;
import org.baratta.logic.WordQuizzleClient;

import java.io.IOException;

public class RegistrationController {
    @FXML
    private Button registerButton;
    @FXML
    private Label backLabel;

    @FXML
    private Label retypeLabel;

    @FXML
    private Label feedback;

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private PasswordField retype;


    @FXML
    private boolean validate_password() {
        if (password.getText().isBlank())
            return false;
        if (!password.getText().equals(retype.getText())) {
            retypeLabel.setText("Doesn't match!");
            retypeLabel.setTextFill(Paint.valueOf("Red"));
            return false;
        } else {
            retypeLabel.setText("Ok");
            retypeLabel.setTextFill(Paint.valueOf("Green"));
            return true;
        }
    }

    private void register(Router router) throws IOException {
        WordQuizzleClient client = WordQuizzleClient.getInstance();
        if (validate_password()) {
            String replay = client.register(username.getText(), password.getText());
            //feedback
            if (replay.startsWith("OK"))
                router.showLogin();
            else {
                feedback.setText(replay);
                feedback.setVisible(true);
            }
        }
    }

    public void initRegistration(Router router) {
        registerButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    register(router);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        backLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                router.showLogin();
            }
        });
    }
}
