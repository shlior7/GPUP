package graphApp;

import TargetGraph.Target;
import TargetGraph.TargetGraph;
import graphApp.actions.SideAction;
import graphApp.controllers.ChoosingController;
import graphApp.controllers.SideController;
import graphfx.GraphContainer;
import graphfx.GraphPanel;
import graphfx.GraphProperties;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

public class GraphPane extends BorderPane {
    public final ChoosingController choosingController;
    public final SideController sideController;
    public GraphPanel<Target> graphView;
    public TargetGraph graph;

    public GraphPane(TargetGraph graph, SideAction TaskController) {
        super();
        this.setWidth(2040);
        this.setHeight(1800);
        this.graph = graph;
        this.choosingController = new ChoosingController(this);
        this.sideController = new SideController(this, TaskController);
        createSideController();
        createGraph();
    }

    public void createSideController() {
        AnchorPane.setBottomAnchor(sideController, 0.0);
        AnchorPane.setTopAnchor(sideController, 0.0);
        this.setMinHeight(500);
        this.setMinWidth(800);
        setRight(new AnchorPane(sideController));
        sideController.initOnOpenSettings();
    }

    public void createGraph() {
        GraphProperties properties = new GraphProperties("edge.arrow = true\n" + "edge.label = false\n" + "edge.arrowsize = 7\n");
        graphView = new GraphPanel<>(graph, properties, this::onVertexClicked);
        GraphContainer graphContainer = new GraphContainer(graphView);
        setCenter(graphContainer);
    }

    public void init() {
        try {
            System.out.println(this.getWidth() + "  " + this.getHeight());
            graphView.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reset() {
        sideController.reset(null);
    }

    public void onVertexClicked(Target target) {
        choosingController.onClicked(target);
    }
}
