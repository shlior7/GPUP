
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class GraphContainer extends BorderPane {
    private static final double MIN_SCALE = 1;
    private static final double MAX_SCALE = 5;
    private static final double SCROLL_DELTA = 0.25;

    public GraphContainer(GraphPanel graphView) {
        ContentResizerPane resizer = new ContentResizerPane(graphView);
        setCenter(resizer);
        HBox bottom = new HBox(10);

        CheckBox automatic = new CheckBox("Automatic layout");
        automatic.selectedProperty().bindBidirectional(graphView.automaticLayoutProperty());

        bottom.getChildren().add(automatic);

        setBottom(bottom);
        setRight(createSlider(resizer));

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
            System.out.println(newValue.doubleValue());
        });

        return paneSlider;
    }

}
