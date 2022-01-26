package app.components;

import app.GraphStage;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;


import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;

public class ThemeChooser {
    ComboBox<String> themeChooser;

    public ThemeChooser(GraphStage graphStage) {
        themeChooser = new ComboBox<>(FXCollections.observableList(Arrays.asList("light", "dark", "blue")));
        themeChooser.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
        {
            graphStage.getScene().getStylesheets().remove(getCSSUrl(oldValue));
            graphStage.graphView.getStylesheets().remove(getCSSUrl(oldValue));
            graphStage.graphView.getStylesheets().add(getCSSUrl(newValue));
            graphStage.getScene().getStylesheets().add(getCSSUrl(newValue));
        });
    }

    public String getCSSUrl(String theme) {
        File f = new File("./DesktopUI/src/app/styles/" + theme + ".css");
        try {
            return f.toURI().toURL().toExternalForm();
        } catch (MalformedURLException e) {
            e.printStackTrace();
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
