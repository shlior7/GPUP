package graphApp.controllers;

import TargetGraph.Target;
import graphApp.GraphPane;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.util.HashMap;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ChoosingController {
    private boolean choosing = false;
    private boolean required = false;
    private boolean depends = false;
    private int limit;
    private final HashMap<Target, Boolean> chosen;
    private final HBox bottomChoosingButtons;
    private final GraphPane graphPane;
    private Consumer<Target> onChoose;

    public ChoosingController(GraphPane graphPane) {
        this.graphPane = graphPane;
        this.chosen = new HashMap<>();
        this.bottomChoosingButtons = createHboxChoosingButtons();
        this.onChoose = (t) -> {
        };
    }

    public HBox createHboxChoosingButtons() {
        HBox hBox = new HBox();
        Button all = new Button("all");
        all.setOnAction(this::all);
        Button clear = new Button("clear");
        clear.setOnAction(this::clear);
        Button whatIfDepends = new Button("What If Depends On");
        whatIfDepends.setOnAction(this::whatIfDepends);
        Button whatIfRequired = new Button("What If Required For");
        whatIfRequired.setOnAction(this::whatIfRequired);
        Button cancel = new Button("cancel");
        cancel.setOnAction(this::cancel);

        hBox.getChildren().addAll(all, clear, whatIfDepends, whatIfRequired, cancel);
        return hBox;
    }

    public void all(ActionEvent actionEvent) {
        if (limit != -1)
            return;
        graphPane.graph.getVerticesMap().values().forEach(target -> {
            if (chosen.getOrDefault(target, false))
                return;
            manualClick(target);
        });
    }

    public void chooseTargets(Set<Target> targets) {
        if (limit != -1)
            return;
        targets.forEach(target -> {
            if (chosen.getOrDefault(target, false))
                return;
            manualClick(target);
        });
    }

    public void setChoosingState(boolean choosingState) {
        setChoosingState(choosingState, -1);
    }

    public void setChoosingState(boolean choosingState, int limit) {
        this.limit = limit;
        this.choosing = choosingState;
        graphPane.setBottom(choosingState ? bottomChoosingButtons : null);
        graphPane.graphView.setPressable(choosingState);
    }


    private void whatIfRequired(ActionEvent actionEvent) {
        if (limit == -1)
            required = true;
    }


    private void whatIfDepends(ActionEvent actionEvent) {
        if (limit == -1)
            depends = true;
    }


    public void cancel(ActionEvent actionEvent) {
        clear(actionEvent);
        graphPane.reset();
        this.onChoose = (t) -> {
        };
    }

    public synchronized void clear(ActionEvent actionEvent) {
        required = depends = false;
        getChosenTargets().forEach(t -> {
            graphPane.graphView.getGraphVertex(t).setVertexStyleToDefault();
            chosen.put(t, false);
            onChoose.accept(t);
        });

        graphPane.graphView.setPressable(true);
    }

    public void manualClick(Target target) {
        if (target == null || (getChosenTargets().size() == limit && !getChosenTargets().contains(target)))
            return;
        graphPane.graphView.pressOnVertex(target);
    }


    public void onClicked(Target target) {
        if (choosing) {
            requiredOrDependsWereClicked(target);
            onChoose.accept(target);
            chosen.put(target, !chosen.getOrDefault(target, false));
            checkReachedLimit();
        }
    }

    public void requiredOrDependsWereClicked(Target target) {
        if (required) {
            ClickOnAllRequired(target);
        }
        if (depends) {
            ClickOnAllDepends(target);
        }
    }

    public void ClickOnAllRequired(Target target) {
        Set<Target> requiredFor = graphPane.graph.whoAreAllYourDaddies(target.name);
        required = false;
        if (chosen.getOrDefault(target, false)) {
            manualClick(target);
        }
        requiredFor.stream().filter(t -> !chosen.getOrDefault(t, false)).forEach(this::manualClick);
    }

    public void ClickOnAllDepends(Target target) {
        Set<Target> dependsOn = graphPane.graph.whoAreAllYourBabies(target.name);
        depends = false;
        if (chosen.getOrDefault(target, false)) {
            manualClick(target);
        }
        dependsOn.stream().filter(t -> !chosen.getOrDefault(t, false)).forEach(this::manualClick);
    }

    public void checkReachedLimit() {
        Set<Target> pressedTargets = chosen.keySet().stream().filter(chosen::get).collect(Collectors.toSet());
        if (pressedTargets.size() == limit) {
            graphPane.graph.getVerticesMap().values().forEach(t -> {
                if (!pressedTargets.contains(t))
                    graphPane.graphView.setPressable(t, false);
            });
        } else {
            graphPane.graphView.setPressable(true);
        }
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public boolean isChoosing() {
        return choosing;
    }

    public Set<Target> getChosenTargets() {
        return chosen.keySet().stream().filter(chosen::get).collect(Collectors.toSet());
    }

    public void setOnChoose(Consumer<Target> onChoose) {
        this.onChoose = onChoose;
    }

    public boolean isChosen(String name) {
        return getChosenTargets().stream().anyMatch((target) -> target.name.equals(name));
    }
}
