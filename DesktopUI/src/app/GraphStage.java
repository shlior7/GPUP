package app;

import TargetGraph.Target;
import app.components.Theme;
import app.controllers.ChoosingController;
import app.controllers.SideController;
import engine.Engine;
import graphfx.GraphContainer;
import graphfx.GraphPanel;
import graphfx.GraphProperties;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.net.MalformedURLException;

public class GraphStage extends Stage {
    public final Engine engine;
    public final BorderPane root;
    public final ChoosingController choosingController;
    public final SideController sideController;
    public GraphPanel<Target> graphView;

    public GraphStage(Engine engine) {
        this.engine = engine;
        this.choosingController = new ChoosingController(this);
        this.sideController = new SideController(this);
        AnchorPane.setBottomAnchor(sideController, 0.0);
        AnchorPane.setTopAnchor(sideController, 0.0);
        this.root = new BorderPane();
        this.initStyle(StageStyle.DECORATED);
        this.setTitle("Target graph - " + engine.getTargetGraph().getGraphsName());
        this.setMinHeight(500);
        this.setMinWidth(800);
        this.setScene(new Scene(root, 2048, 1800));
        root.setRight(new AnchorPane(sideController));
        CreateGraph();
    }

    public void CreateGraph() {
        GraphProperties properties = new GraphProperties("edge.arrow = true\n" + "edge.label = false\n" + "edge.arrowsize = 7\n");
        graphView = new GraphPanel<>(Engine.TargetGraph(), properties, this::onVertexClicked);
        GraphContainer graphContainer = new GraphContainer(graphView);
        root.setCenter(graphContainer);
    }

    public void showGraph() {
        sideController.getThemeChooser().setTheme(Theme.light);
        this.show();
        graphView.init();
    }

    public void reset() {
        sideController.reset(null);
    }

    public void onVertexClicked(Target target) {
        choosingController.onClicked(target);
    }

}
