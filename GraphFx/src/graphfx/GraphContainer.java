package graphfx;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.lang.invoke.VolatileCallSite;

public class GraphContainer extends BorderPane {
    private static final double MIN_SCALE = 1;
    private static final double MAX_SCALE = 5;
    private static final double SCROLL_DELTA = 0.25;
    private GraphPanel graphView;

    public GraphContainer(GraphPanel graphView) {
        this.graphView = graphView;
        ContentResizerPane resizer = new ContentResizerPane(graphView);
        setCenter(resizer);
        HBox bottom = new HBox(10);

        CheckBox automatic = new CheckBox("Automatic layout");
        automatic.selectedProperty().bindBidirectional(graphView.automaticLayoutProperty());
        Button reverse = new Button("reverse");
        reverse.setOnAction(this::reverse);
        reverse.setStyle(
                "-fx-background-radius: 5em; " +
                        "-fx-min-width: 60px; " +
                        "-fx-min-height: 60px; " +
                        "-fx-max-width: 60px; " +
                        "-fx-max-height: 60px;"
        );


        bottom.getChildren().add(automatic);
        setBottom(bottom);
        VBox settings = new VBox(50, createSlider(resizer), reverse);
        setRight(settings);

    }

    private void reverse(ActionEvent actionEvent) {
        graphView.placeUpsideDown();
    }

    private Node createSlider(ContentResizerPane resizer) {

        Slider slider = new Slider(MIN_SCALE, MAX_SCALE, MIN_SCALE);
        slider.setOrientation(Orientation.VERTICAL);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setMajorTickUnit(SCROLL_DELTA);
        slider.setMinorTickCount(1);
        slider.setBlockIncrement(0.125f);
        slider.setSnapToTicks(true);

        Text label = new Text("Zoom");

        VBox paneSlider = new VBox(slider, label);

        paneSlider.setPadding(new Insets(10, 10, 10, 10));
        paneSlider.setSpacing(10);

        slider.valueProperty().addListener((obs, oldValue, newValue) -> {
            resizer.setResizeFactor(newValue.doubleValue());
        });

        return paneSlider;
    }

}
