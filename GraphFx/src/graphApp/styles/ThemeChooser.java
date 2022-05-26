package graphApp.styles;


import graphApp.GraphPane;
import graphApp.components.Theme;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;

import java.util.Arrays;

public class ThemeChooser {
    ComboBox<String> themeChooser;

    public ThemeChooser(GraphPane graphPane) {
        themeChooser = new ComboBox<>(FXCollections.observableList(Arrays.asList("light", "dark", "blue")));
        themeChooser.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
        {
            graphPane.getScene().getStylesheets().remove(getCSSUrl(oldValue));
            graphPane.graphView.getStylesheets().remove(getCSSUrl(oldValue));
            graphPane.getScene().getStylesheets().add(getCSSUrl(newValue));
            graphPane.graphView.getStylesheets().add(getCSSUrl(newValue));
        });
    }

    public String getCSSUrl(String theme) {
        try {
            String url = ThemeChooser.class.getResource(theme + ".css").toExternalForm();
            return url;
        } catch (Exception ignored) {

        }
        return "";
    }

    public ComboBox<String> getThemeChooser() {
        return themeChooser;
    }

    public void setTheme(Theme theme) {
        themeChooser.setValue(theme.toString());
    }
}
