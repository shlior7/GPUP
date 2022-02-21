package graphApp.controllers;

import graphApp.GraphPane;
import graphApp.actions.FindCircuit;
import graphApp.actions.FindPath;
import graphApp.actions.SideAction;
import graphApp.actions.WhatIf;
import graphApp.actions.task.TaskController;
import graphApp.components.AnchoredButton;
import graphApp.styles.ThemeChooser;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Popup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class SideController extends VBox {
    private final GraphPane graphPane;
    private final List<SideAction> actionList;
    private final StackPane settings;
    private ThemeChooser themeChooser;

    public SideController(GraphPane graphPane) {
        this.graphPane = graphPane;
        settings = new StackPane();
//        this.actionList = Stream.of(new FindPath(graphPane), new FindCircuit(graphPane), new WhatIf(graphPane)).collect(Collectors.toList());
        this.actionList = new ArrayList<>();
        this.setSpacing(20);
        this.setPrefWidth(200);
        CreateButtonsVBox();
        createSettingsStack();
        createBottomSettings();
    }

    public void addSideAction(SideAction sideAction) {
        actionList.add(sideAction);
        this.getChildren().add(1, sideAction.getActionButton());
        settings.getChildren().add(sideAction.getSettings());
        sideAction.setOnOpenSettings(this::onOpenSettings);
    }

    public void addTaskControllerAction(TaskController taskController) {
        actionList.add(taskController);
        this.getChildren().add(0, taskController.getActionButton());
        settings.getChildren().add(taskController.getSettings());
        taskController.setOnOpenSettings(this::onOpenSettings);
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
        themeChooser = new ThemeChooser(graphPane);
        VBox wrapper = new VBox(themeChooser.getThemeChooser());
        wrapper.setAlignment(Pos.BOTTOM_CENTER);
        return wrapper;
//        this.getChildren().add(wrapper);
    }

    private void setHover(ImageView info) {
        Text text = new Text(this.graphPane.graph.getInfo());
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

    ///TODO
    ////1.create Servlet For Task Info
    ////2.create Servlet For Task Reset
    public void reset(ActionEvent actionEvent) {
//        if (graphStage.engine.isTaskRunning())
//            return;
        graphPane.graphView.reset();
//        graphStage.engine.reset();
        graphPane.choosingController.setChoosingState(false);
        graphPane.choosingController.clear(null);
        actionList.forEach((SideAction::reset));
    }

    public void initOnOpenSettings() {
        actionList.forEach(action -> {
            action.setOnOpenSettings(this::onOpenSettings);
        });
    }

    public void onOpenSettings() {
        reset(null);
    }

    public ThemeChooser getThemeChooser() {
        return themeChooser;
    }

}
