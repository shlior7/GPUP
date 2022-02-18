package app.actions.task;

import app.GraphStage;
import engine.Engine;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import task.Compilation;
import task.Simulation;
import task.Task;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class TaskSettings {
    public Task task;
    public boolean runFromScratch;
    public int numThreads;
    public int maxThreads;
    public boolean submitted;
    public boolean chooseAll;
    public File directoryPath = null;
    public Stage settingStage;
    public GraphStage parent;
    public static boolean TaskAlreadyRan;
    @FXML
    private Parent root;

    @FXML
    private CheckBox runFromScratchBox;

    @FXML
    private ChoiceBox<String> threadsToRunCombo;

    @FXML
    private ToggleButton SimulationTask;

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

    @FXML
    private Button pathToCompilation;

    public TaskSettings() {
    }

    public static TaskSettings createTaskSettings(boolean taskAlreadyRan) {
        TaskAlreadyRan = taskAlreadyRan;
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL taskUrl = TaskSettings.class.getResource("taskSettings.fxml");
        fxmlLoader.setLocation(taskUrl);
        try {
            fxmlLoader.load(taskUrl.openStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fxmlLoader.getController();
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
        SimulationTask = (ToggleButton) scene.lookup("#SimulationTask");
        compilerTask = (ToggleButton) scene.lookup("#compilerTask");

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
        if (SimulationTask.isSelected()) {
            compilerTask.setSelected(false);
            toggleSimulationProperties(false);
        } else {
            compilerTask.setSelected(true);
            toggleSimulationProperties(true);
        }
    }

    @FXML
    void handleRunFromScratchChange(ActionEvent event) {
        toggleDisableAll(!runFromScratchBox.isSelected());
    }

    @FXML
    void cancelClicked(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    void onClickedPathToCompilation(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryPath = directoryChooser.showDialog(settingStage);
    }

    public void toggleSimulationProperties(boolean disable) {
        pathToCompilation.setDisable(!disable);
        checkBoxRandom.setDisable(disable);
        processTimeText.setDisable(disable);
        successProbabilityText.setDisable(disable);
        warningProbabilityText.setDisable(disable);
    }

    public void toggleDisableAll(boolean disable) {
        compilerTask.setDisable(disable);
        SimulationTask.setDisable(disable);
        checkBoxRandom.setDisable(disable);
        ComboTargetsToRun.setDisable(disable);
        threadsToRunCombo.setDisable(disable);
        pathToCompilation.setDisable(disable);
        processTimeText.setDisable(disable);
        successProbabilityText.setDisable(disable);
        warningProbabilityText.setDisable(disable);
        if (!disable) {
            toggleSimulationProperties(compilerTask.isSelected());
        }
    }

    @FXML
    void compilerTaskPressed(ActionEvent event) {
        if (compilerTask.isSelected()) {
            SimulationTask.setSelected(false);
            toggleSimulationProperties(true);
        } else {
            SimulationTask.setSelected(true);
            toggleSimulationProperties(false);
        }
    }

    @FXML
    void submitOnClicked(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        runFromScratch = runFromScratchBox.isSelected();
        if (!runFromScratch) {
            if (!TaskAlreadyRan) {
                Utils.alertWarning("There were no tasks that ran yet...");
            } else {
                submitted = true;
                stage.close();
                return;
            }
        }

        if (ComboTargetsToRun.getValue() != null) {
            chooseAll = ComboTargetsToRun.getSelectionModel().getSelectedIndex() == 1;
            if (threadsToRunCombo.getValue() != null) {
                numThreads = threadsToRunCombo.getSelectionModel().getSelectedIndex() + 1;

                if (SimulationTask.isSelected()) {
                    if (checkValidTextField(processTimeText) && checkValidTextField(warningProbabilityText) && checkValidTextField(successProbabilityText)) {
                        submitted = true;
                        task = new Simulation(Integer.parseInt(processTimeText.getText()), checkBoxRandom.isSelected(), Float.parseFloat(successProbabilityText.getText()), Float.parseFloat(warningProbabilityText.getText()));
                        stage.close();
                    }
                }
                if (compilerTask.isSelected()) {
                    if (directoryPath == null) {
                        Alert information = new Alert(Alert.AlertType.WARNING);
                        information.setTitle("Warning");
                        information.setContentText("You need to choose path to compilation products!");
                        information.showAndWait();
                    } else {
                        submitted = true;
                        task = new Compilation(directoryPath.getPath(), parent.engine.TargetGraph("").getWorkingDir());
                        stage.close();
                    }
                }
            }
        }
    }


    private boolean checkValidTextField(TextField text) {
        return (text.getStyle() == "" && text.getText() != null && !text.getText().isEmpty());
    }
}