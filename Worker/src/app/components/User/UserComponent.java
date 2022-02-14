package app.components.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;

public class UserComponent extends VBox {
    private User user;
    @FXML
    private Text user_name;

    @FXML
    private Text user_position;

    @FXML
    void initialize() {
        user_name.setText(user.userName);
        user_position.setText(user.position);
    }


    public UserComponent(User user) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "user_component.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        this.user = user;

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

}

