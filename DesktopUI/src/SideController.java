import javafx.event.ActionEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SideController extends VBox {
    private GraphStage graphStage;
    private List<SideAction> actionList;
    private StackPane settings;

    public SideController(GraphStage graphStage) {
        this.graphStage = graphStage;
        settings = new StackPane();
        actionList = Stream.of(new RunTask(graphStage), new FindPath(graphStage), new FindCircuit(graphStage)).collect(Collectors.toList());
        this.setSpacing(20);
        this.setPrefWidth(200);
        CreateButtonsVBox();
        createSettingsStack();
    }

    public void CreateButtonsVBox() {
        for (SideAction action : actionList) {
            System.out.println(action.getActionButton().getText());
            this.getChildren().add(action.getAnchoredButton());
        }
//        actionSet.forEach(action -> {
//            this.getChildren().add(action.getAnchoredButton());
//        });
//        vBox.getChildren().add(createAnchoredButton("Find Circuit", this::findCircuit));
//        vBox.getChildren().add(createAnchoredButton("What If", this::whatIf));
        this.getChildren().add(new AnchoredButton("Reset", this::reset));
////        vBox.getChildren().add(createAnchoredButton(actionButton));
        this.getChildren().add(settings);
    }

    public void createSettingsStack() {
        actionList.forEach(action -> {
            this.getChildren().add(action.getSettings());
        });
    }
//    public AnchorPane createAnchoredButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> value) {
//        Button task = new Button(text);
//        AnchorPane anchorPane = new AnchorPane(task);
//        AnchorPane.setLeftAnchor(task, 0.0);
//        AnchorPane.setRightAnchor(task, 0.0);
//        task.setOnAction(value);
//        return anchorPane;
//    }

    public void reset(ActionEvent actionEvent) {
        if (graphStage.engine.isTaskRunning())
            return;
        graphStage.graphView.reset();
        graphStage.engine.reset();
        graphStage.choosingController.setChoosingState(false);
        actionList.forEach((SideAction::reset));
    }
}
