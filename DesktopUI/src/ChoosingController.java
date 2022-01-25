import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.util.HashMap;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ChoosingController {
    private boolean choosing = false;
    private final HashMap<Target, Boolean> chosen;
    private final HBox bottomChoosingButtons;
    private final GraphStage graphStage;
    private int limit;
    private Consumer<Target> onChoose;

    public ChoosingController(GraphStage graphStage) {
        this.graphStage = graphStage;
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
        graphStage.engine.getAllTargets().values().forEach(target -> {
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
        graphStage.root.setBottom(choosingState ? bottomChoosingButtons : null);
        graphStage.graphView.setPressable(choosingState);
        if (choosingState)
            chosen.clear();
    }


    private void whatIfRequired(ActionEvent actionEvent) {
    }

    private void cancel(ActionEvent actionEvent) {
        clear(actionEvent);
        graphStage.reset();

        System.out.println("chosen after cancel " + chosen);
    }

    private void whatIfDepends(ActionEvent actionEvent) {
    }

    private synchronized void clear(ActionEvent actionEvent) {
        chosen.forEach((target, pressed) -> {
            if (pressed) {
                graphStage.graphView.getGraphVertex(target).setVertexStyleToDefault();
                chosen.put(target, false);
            }
        });
        graphStage.graphView.setPressable(true);
        this.onChoose = (t) -> {
        };
        System.out.println("chosen after clear " + chosen);
    }

    public void manualClick(Target target) {
        System.out.println("target = " + target);

        if (target == null || getChosenTargets().size() == limit && !getChosenTargets().contains(target))
            return;
        graphStage.graphView.pressOnVertex(target);
    }

    public void onClicked(Target target) {
        if (choosing) {
            onChoose.accept(target);
            chosen.put(target, !chosen.getOrDefault(target, false));
            System.out.println("clicked " + chosen);
            Set<Target> pressedTargets = chosen.keySet().stream().filter(chosen::get).collect(Collectors.toSet());
            if (pressedTargets.size() == limit) {
                graphStage.engine.getAllTargets().values().forEach(t -> {
                    if (!pressedTargets.contains(t))
                        graphStage.graphView.setPressable(t, false);
                });
            } else {
                graphStage.graphView.setPressable(true);
            }
        }
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public boolean isChoosing() {
        return choosing;
    }

    public Set<Target> getChosenTargets() {
        System.out.println(chosen);
        return chosen.keySet().stream().filter(chosen::get).collect(Collectors.toSet());
    }

    public void setOnChoose(Consumer<Target> onChoose) {
        this.onChoose = onChoose;
    }

    public boolean isChosen(String name) {
        return getChosenTargets().stream().anyMatch((target) -> target.name.equals(name));
    }
}
