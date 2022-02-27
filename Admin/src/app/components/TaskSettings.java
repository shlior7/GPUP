package app.components;

import TargetGraph.TargetGraph;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import types.Compilation;
import types.Simulation;
import types.Task;
import types.TaskType;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class TaskSettings implements Initializable {
    public int runningNumber;
    public boolean runFromScratch;
    public boolean submitted;
    public boolean chooseAll;
    public File compilationOutputPath = null;
    public File workingDirectory = null;
    public Stage settingStage;
    public static List<TaskType> TasksWithPrice;
    public static TargetGraph TasksGraph;
    public static Task Task;

    @FXML
    public TextField taskName;
    public Button RunFromScratchButton;
    public Button RunIncremental;
    public Button pathToWorkingDir;
    public Button chooseTargets;

    @FXML
    private Parent root;

    @FXML
    private ToggleButton SimulationTask;

    @FXML
    private TextField processTimeText;

    @FXML
    private TextField successProbabilityText;

    @FXML
    private TextField warningProbabilityText;

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

    public static TaskSettings createTaskSettings(TargetGraph tasksGraph, List<TaskType> tasksWithPrice) {
        TasksGraph = tasksGraph;
        TasksWithPrice = tasksWithPrice;
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL taskUrl = TaskSettings.class.getResource("taskSetting.fxml");
        fxmlLoader.setLocation(taskUrl);
        try {
            fxmlLoader.load(taskUrl.openStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fxmlLoader.getController();
    }

    public void showAndReturn() {
        this.submitted = false;
        if (settingStage == null) {
            settingStage = new Stage();
            settingStage.setTitle("Task Settings");
            Scene scene = new Scene(root);
            settingStage.setScene(scene);
            settingStage.initModality(Modality.WINDOW_MODAL);
        }
        initialize(settingStage.getScene());
        settingStage.showAndWait();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    private void initialize(Scene scene) {
        ComboTargetsToRun = (ChoiceBox<String>) scene.lookup("#ComboTargetsToRun");
        successProbabilityText = (TextField) scene.lookup("#successProbabilityText");
        warningProbabilityText = (TextField) scene.lookup("#warningProbabilityText");
        processTimeText = (TextField) scene.lookup("#processTimeText");
        checkBoxRandom = (CheckBox) scene.lookup("#checkBoxRandom");
        SimulationTask = (ToggleButton) scene.lookup("#SimulationTask");
        compilerTask = (ToggleButton) scene.lookup("#compilerTask");

        if (Task != null) {
            runningNumber++;
            taskName.setText(Task.getTaskName() + " - " + runningNumber);
            if ("Simulation".equals(Task.getClassName())) {
                Simulation sim = (Simulation) Task;
                processTimeText.setText(String.valueOf(sim.getTimeToProcess()));
                checkBoxRandom.setSelected(sim.isRandom());
                successProbabilityText.setText(String.valueOf(sim.getSuccessProbability()));
                warningProbabilityText.setText(String.valueOf(sim.getSuccessWithWarningProbability()));
            }
            toggleDisableAll(true);
            RunFromScratchButton.setVisible(true);
            RunIncremental.setVisible(true);
            chooseTargets.setVisible(false);
        } else {
            runningNumber = 0;
            RunFromScratchButton.setVisible(false);
            RunIncremental.setVisible(false);
            chooseTargets.setVisible(true);
            ComboTargetsToRun.setItems(FXCollections.observableArrayList("Choose targets", "All targets"));
            addListenerToProbability(successProbabilityText);
            addListenerToProbability(warningProbabilityText);
            addListenerToTime(processTimeText);
        }


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
    void cancelClicked(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    void onClickedPathToCompilation(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        compilationOutputPath = directoryChooser.showDialog(settingStage);
    }

    public void toggleSimulationProperties(boolean disable) {
        pathToCompilation.setDisable(!disable);
        checkBoxRandom.setDisable(disable);
        processTimeText.setDisable(disable);
        successProbabilityText.setDisable(disable);
        warningProbabilityText.setDisable(disable);
    }

    public void toggleDisableAll(boolean disable) {
        taskName.setDisable(disable);
        compilerTask.setDisable(disable);
        SimulationTask.setDisable(disable);
        checkBoxRandom.setDisable(disable);
        ComboTargetsToRun.setDisable(disable);
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

    private boolean checkValidTextField(TextField text) {
        return (Objects.equals(text.getStyle(), "") && text.getText() != null && !text.getText().isEmpty());
    }

    public void onClickedPathToWorkingDIr(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        workingDirectory = directoryChooser.showDialog(settingStage);
    }

    public void runFromScratchClicked(ActionEvent actionEvent) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        runFromScratch = true;
        submitted = true;
        stage.close();
    }

    public void runIncrementalClicked(ActionEvent actionEvent) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        runFromScratch = false;
        submitted = true;
        stage.close();
    }

    public void chooseTargetsClicked(ActionEvent actionEvent) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        if (ComboTargetsToRun.getValue() != null && checkValidTextField(taskName)) {
            chooseAll = ComboTargetsToRun.getSelectionModel().getSelectedIndex() == 1;

            if (SimulationTask.isSelected()) {
                if (checkValidTextField(processTimeText) && checkValidTextField(warningProbabilityText) && checkValidTextField(successProbabilityText)) {
                    submitted = true;
                    Task = new Simulation(taskName.getText(), Integer.parseInt(processTimeText.getText()), checkBoxRandom.isSelected(), Float.parseFloat(successProbabilityText.getText()), Float.parseFloat(warningProbabilityText.getText()));
                    stage.close();
                }
            }
            if (compilerTask.isSelected()) {
                if (compilationOutputPath == null) {
                    Alert information = new Alert(Alert.AlertType.WARNING);
                    information.setTitle("Warning");
                    information.setContentText("You need to choose path to compilation products!");
                    information.showAndWait();
                } else {
                    submitted = true;
                    Task = new Compilation(taskName.getText(), compilationOutputPath.getPath(), workingDirectory.getPath());
                    stage.close();
                }
            }
        }
    }

    public static String getTaskName() {
        return Task.getTaskName();
    }
}