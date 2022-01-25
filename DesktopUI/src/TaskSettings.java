import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class TaskSettings {
    public Task task;
    public boolean runFromScratch;
    public int numThreads;
    public int maxThreads;
    public boolean submitted;
    public boolean chooseAll;
    public Stage settingStage;
    public GraphStage parent;

    @FXML
    private Parent root;

    @FXML
    private CheckBox runFromScratchBox;

    @FXML
    private ChoiceBox<String> threadsToRunCombo;

    @FXML
    private ToggleButton SimulaitionTask;

    @FXML
    private TextField processTimeText;

    @FXML
    private TextField successProbabilityText;

    @FXML
    private TextField warningProbabilityText;

    @FXML
    private Button SubmitButton;

    @FXML
    private Button cancelButton;

    @FXML
    private CheckBox checkBoxRandom;

    @FXML
    private ToggleButton compilerTask;

    @FXML
    private ChoiceBox<String> ComboTargetsToRun;

    public TaskSettings() {
    }

    public TaskSettings(int maxThreads, Stage parent) {
        this.submitted = false;
        this.maxThreads = maxThreads;
        settingStage = new Stage();
        settingStage.setTitle("Task Settings");

        FXMLLoader fxmlLoader = new FXMLLoader();
        URL taskUrl = getClass().getResource("taskSettings.fxml");
        System.out.println("taskUrl = " + taskUrl);
        fxmlLoader.setLocation(taskUrl);
        Parent load = null;
        try {
            load = fxmlLoader.load(taskUrl.openStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene scene = new Scene(load, 600, 400);
        settingStage.setScene(scene);
        settingStage.setAlwaysOnTop(true);
        settingStage.initModality(Modality.WINDOW_MODAL);
        settingStage.initOwner(parent);
        initialize(scene);
    }

    public static TaskSettings createTaskSettings() {
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL taskUrl = TaskSettings.class.getResource("taskSettings.fxml");
        System.out.println("taskUrl = " + taskUrl);
        fxmlLoader.setLocation(taskUrl);
        Parent load = null;
        try {
            load = fxmlLoader.load(taskUrl.openStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fxmlLoader.getController();
    }

    public void showAndWait() {
        settingStage.showAndWait();
        System.out.println("hey");
    }

    public void showAndReturn(int maxThreads, GraphStage parent) {
        this.submitted = false;
        this.maxThreads = maxThreads;
        this.parent = parent;
        settingStage = new Stage();
        settingStage.setTitle("Task Settings");
        Scene scene = new Scene(root);
        settingStage.setScene(scene);
        initialize(scene);
        settingStage.initModality(Modality.WINDOW_MODAL);
        settingStage.initOwner(parent);
        settingStage.showAndWait();
    }

    private void initialize(Scene scene) {
        threadsToRunCombo = (ChoiceBox<String>) scene.lookup("#threadsToRunCombo");
        ComboTargetsToRun = (ChoiceBox<String>) scene.lookup("#ComboTargetsToRun");
        successProbabilityText = (TextField) scene.lookup("#successProbabilityText");
        warningProbabilityText = (TextField) scene.lookup("#warningProbabilityText");
        processTimeText = (TextField) scene.lookup("#processTimeText");
        checkBoxRandom = (CheckBox) scene.lookup("#checkBoxRandom");

        toggleSimulationProperties(true);


        for (int i = 1; i <= maxThreads; i++) {
            threadsToRunCombo.getItems().add(String.valueOf(i));
        }
        ComboTargetsToRun.setItems(FXCollections.observableArrayList("Choose targets", "All targets"));
        addListenerToProbability(successProbabilityText);
        addListenerToProbability(warningProbabilityText);
        addListenerToTime(processTimeText);
    }

    private void addListenerToProbability(TextField text) {
        text.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (Float.parseFloat(newValue) >= 0 && Float.parseFloat(newValue) <= 1) {
                    text.setStyle("");
                } else {
                    text.setStyle("-fx-text-box-border: red;");
                }
            } catch (NumberFormatException ex) {
                text.setStyle("-fx-text-box-border: red;");
            }
        });
    }

    private void addListenerToTime(TextField text) {
        text.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (Integer.parseInt(newValue) <= 0) {
                    text.setStyle("-fx-text-box-border: red;");
                } else {
                    text.setStyle("");
                }
            } catch (NumberFormatException ex) {
                text.setStyle("-fx-text-box-border: red;");
            }
        });
    }

    @FXML
    void SimulationTaskPressed(ActionEvent event) {
        if (SimulaitionTask.isSelected()) {
            compilerTask.setSelected(false);
            toggleSimulationProperties(false);
        } else {
            compilerTask.setSelected(true);
            toggleSimulationProperties(true);
        }
    }


    @FXML
    void cancelClicked(ActionEvent event) {
        System.out.println("cancel ");
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public void toggleSimulationProperties(boolean disable) {
        checkBoxRandom.setDisable(disable);
        processTimeText.setDisable(disable);
        successProbabilityText.setDisable(disable);
        warningProbabilityText.setDisable(disable);
    }

    @FXML
    void compilerTaskPressed(ActionEvent event) {
        if (compilerTask.isSelected()) {
            SimulaitionTask.setSelected(false);
            toggleSimulationProperties(true);
        } else {
            SimulaitionTask.setSelected(true);
            toggleSimulationProperties(false);
        }
    }

    @FXML
    void submitOnClicked(ActionEvent event) {
        System.out.println("submit  ");
        Stage stage = (Stage) cancelButton.getScene().getWindow();

        runFromScratch = runFromScratchBox.isSelected();
        if (ComboTargetsToRun.getValue() != null) {
            chooseAll = ComboTargetsToRun.getSelectionModel().getSelectedIndex() == 1;
            if (threadsToRunCombo.getValue() != null) {
                numThreads = threadsToRunCombo.getSelectionModel().getSelectedIndex() + 1;

                if (SimulaitionTask.isSelected()) {
                    if (checkValidTextField(processTimeText) && checkValidTextField(warningProbabilityText) && checkValidTextField(successProbabilityText)) {
                        submitted = true;
                        task = new Simulation(Integer.parseInt(processTimeText.getText()), checkBoxRandom.isSelected(), Float.parseFloat(successProbabilityText.getText()), Float.parseFloat(warningProbabilityText.getText()));
                        stage.close();
                    }
                }
                if (compilerTask.isSelected()) {
                    submitted = true;
                    task = new Compilation("CompilationOutput", Engine.TargetGraph().getWorkingDir());
                    stage.close();
                }
            }
        }
    }

    private boolean checkValidTextField(TextField text) {
        return (text.getStyle() == "" && text.getText() != null);
    }
}