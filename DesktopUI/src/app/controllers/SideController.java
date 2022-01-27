package app.controllers;

import app.GraphStage;
import app.actions.FindCircuit;
import app.actions.FindPath;
import app.actions.SideAction;
import app.actions.WhatIf;
import app.actions.task.RunTask;
import app.components.AnchoredButton;
import app.styles.ThemeChooser;
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
    private ThemeChooser themeChooser;

    public SideController(GraphStage graphStage) {
        this.graphStage = graphStage;
        settings = new StackPane();
        this.actionList = Stream.of(new RunTask(graphStage, this::onOpenSettings), new FindPath(graphStage, this::onOpenSettings), new FindCircuit(graphStage, this::onOpenSettings), new WhatIf(graphStage, this::onOpenSettings)).collect(Collectors.toList());
        this.setSpacing(20);
        this.setPrefWidth(200);
        CreateButtonsVBox();
        createSettingsStack();
        createBottomSettings();
    }

    public void createBottomSettings() {
        HBox bottomSettings = new HBox(20);
        createThemeChooser();
        bottomSettings.getChildren().addAll(createInfoIcon(), createThemeChooser());
        VBox wrapper = new VBox(bottomSettings);
        VBox.setVgrow(wrapper, Priority.ALWAYS);
        wrapper.setAlignment(Pos.BOTTOM_CENTER);
        this.getChildren().add(wrapper);
    }

    public ImageView createInfoIcon() {
        ImageView infoIcon = new ImageView(new Image("resources/info_button.png"));
        infoIcon.setFitHeight(70);
        infoIcon.setFitWidth(70);
        VBox wrapper = new VBox(infoIcon);
        VBox.setVgrow(wrapper, Priority.ALWAYS);
        wrapper.setAlignment(Pos.BOTTOM_CENTER);
//        this.getChildren().add(wrapper);
        setHover(infoIcon);
        return infoIcon;
    }

    public VBox createThemeChooser() {
        themeChooser = new ThemeChooser(graphStage);
        VBox wrapper = new VBox(themeChooser.getThemeChooser());
        wrapper.setAlignment(Pos.BOTTOM_CENTER);
        return wrapper;
//        this.getChildren().add(wrapper);
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

                double sX = bounds.getMinX() - stickyNotesPane.getWidth() - 5;
                double sY = bounds.getMinY() - stickyNotesPane.getHeight() - 5;

                popup.show(this, sX, sY);
            } else {
                popup.hide();
            }
        });
    }

    public void CreateButtonsVBox() {
        for (SideAction action : actionList) {
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
        graphStage.choosingController.clear(null);
        actionList.forEach((SideAction::reset));
    }

    public void onOpenSettings() {
        reset(null);
    }

    public ThemeChooser getThemeChooser() {
        return themeChooser;
    }

}
