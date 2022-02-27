package chat.client.component.main;


import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;


public class ChatClient {
    private SplitPane parent = new SplitPane();
    private ChatAppMainController chatAppMainController;

    public ChatClient(Stage primaryStage, Tab tab) {
//        primaryStage.setMinHeight(600);
//        primaryStage.setMinWidth(600);
        //primaryStage.setTitle("Chat App Client");
        URL chatPage = getClass().getResource("/chat/client/component/main/chat-app-main.fxml");

        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(chatPage);
            parent = fxmlLoader.load();
            tab.setContent(parent);
            chatAppMainController = fxmlLoader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public ChatAppMainController getChatAppMainController(){
        return chatAppMainController;
    }
}
