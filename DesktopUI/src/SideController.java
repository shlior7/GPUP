import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Popup;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SideController extends VBox {
    private final GraphStage graphStage;
    private final List<SideAction> actionList;
    private final StackPane settings;

    public SideController(GraphStage graphStage) {
        this.graphStage = graphStage;
        settings = new StackPane();
        this.actionList = Stream.of(new RunTask(graphStage, this::onOpenSettings), new FindPath(graphStage, this::onOpenSettings), new FindCircuit(graphStage, this::onOpenSettings), new WhatIf(graphStage, this::onOpenSettings)).collect(Collectors.toList());
        this.setSpacing(20);
        this.setPrefWidth(200);
        CreateButtonsVBox();
        createSettingsStack();
        createInfoIcon();
    }

    public void createInfoIcon() {
        ImageView infoIcon = new ImageView(new Image("resources/info_button.png"));
        infoIcon.setFitHeight(70);
        infoIcon.setFitWidth(70);
        VBox wrapper = new VBox(infoIcon);
        VBox.setVgrow(wrapper, Priority.ALWAYS);
        wrapper.setAlignment(Pos.BOTTOM_CENTER);
        this.getChildren().add(wrapper);
        setHover(infoIcon);
    }

    private void setHover(ImageView info) {
        Text text = new Text(graphStage.engine.getGraphInfo());
        text.setStyle("-fx-fill: #000000; -fx-font-size: 25;");
        StackPane stickyNotesPane = new StackPane(text);
        stickyNotesPane.setPrefSize(200, 200);
        stickyNotesPane.getStyleClass().add("info");

        Popup popup = new Popup();
        popup.getContent().add(stickyNotesPane);

        info.hoverProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                Bounds bounds = info.localToScreen(info.getBoundsInLocal());
                Bounds paneBounds = this.localToScreen(this.getBoundsInLocal());

                boolean passedRight = bounds.getMaxX() + stickyNotesPane.getWidth() >= paneBounds.getMaxX();
                boolean passedBottom = bounds.getMinY() + stickyNotesPane.getHeight() >= paneBounds.getMaxY();

                double sX = passedRight ? bounds.getMinX() - stickyNotesPane.getWidth() - 5 : bounds.getMaxX();
                double sY = passedBottom ? bounds.getMinY() - stickyNotesPane.getHeight() - 5 : bounds.getMaxY();

                popup.show(this, sX, sY);
            } else {
                popup.hide();
            }
        });
    }

    public void CreateButtonsVBox() {
        for (SideAction action : actionList) {
            System.out.println(action.getActionButton().getText());
            this.getChildren().add(action.getAnchoredButton());
        }
        this.getChildren().add(new AnchoredButton("Reset", this::reset));
        this.getChildren().add(settings);
    }

    public void createSettingsStack() {
        actionList.forEach(action -> {
            settings.getChildren().add(action.getSettings());
        });
    }

    public void reset(ActionEvent actionEvent) {
        if (graphStage.engine.isTaskRunning())
            return;
        graphStage.graphView.reset();
        graphStage.engine.reset();
        graphStage.choosingController.setChoosingState(false);
        actionList.forEach((SideAction::reset));
    }

    public void onOpenSettings() {
        reset(null);
    }
}
