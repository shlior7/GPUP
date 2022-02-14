package app.styles;

import app.GraphStage;
import app.components.Theme;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;


import java.util.Arrays;

public class ThemeChooser {
    ComboBox<String> themeChooser;

    public ThemeChooser(GraphStage graphStage) {
        themeChooser = new ComboBox<>(FXCollections.observableList(Arrays.asList("light", "dark", "blue")));
        themeChooser.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
        {
            graphStage.getScene().getStylesheets().remove(getCSSUrl(oldValue));
            graphStage.graphView.getStylesheets().remove(getCSSUrl(oldValue));
            graphStage.getScene().getStylesheets().add(getCSSUrl(newValue));
            graphStage.graphView.getStylesheets().add(getCSSUrl(newValue));
        });
    }

    public String getCSSUrl(String theme) {
        try {
            String url = ThemeChooser.class.getResource(theme + ".css").toExternalForm();
            return url;
        } catch (Exception ignored) {

        }
        return "";
//        File f = new File("./DesktopUI/src/app/styles/" + theme + ".css");
//        try {
//            return f.toURI().toURL().toExternalForm();
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//        return "";
    }

    public ComboBox<String> getThemeChooser() {
        return themeChooser;
    }

    public void setTheme(Theme theme) {
        themeChooser.setValue(theme.toString());
    }
}
