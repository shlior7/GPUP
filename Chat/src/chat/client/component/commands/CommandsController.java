package chat.client.component.commands;

import chat.client.component.api.ChatCommands;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
public class CommandsController {

    private ChatCommands chatCommands;
    private final BooleanProperty autoUpdates;
    @FXML private ToggleButton autoUpdatesButton;

    public CommandsController() {
        autoUpdates = new SimpleBooleanProperty();
    }

    @FXML
    public void initialize() {
        autoUpdates.bind(autoUpdatesButton.selectedProperty());
    }

    public ReadOnlyBooleanProperty autoUpdatesProperty() {
        return autoUpdates;
    }


    public void setChatCommands(ChatCommands chatRoomMainController) {
        this.chatCommands = chatRoomMainController;
    }
}
