package app.components;

import TargetGraph.TargetGraph;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import okhttp3.Response;
import types.Compilation;
import types.Simulation;
import types.Task;
import types.TaskType;
import utils.Constants;
import utils.Utils;
import utils.http.HttpClientUtil;

import java.io.File;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;

public class TaskSettings implements Initializable {
    public int runningNumber;
    public boolean runFromScratch;
    public boolean submitted;
    public boolean chooseAll;
    public File compilationOutputPath = null;
    public File workingDirectory = null;
    public Stage settingStage;
    public Set<TaskType> TasksWithPrice;
    public TargetGraph TasksGraph;
    public Task Task;

    @FXML
    public TextField taskName;
    public Button RunFromScratchButton;
    public Button RunIncremental;
    public Button pathToWorkingDir;
    public Button chooseTargets;

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

    public void showAndReturn(Pane root) {
        this.submitted = false;
        if (settingStage == null) {
            settingStage = new Stage();
            settingStage.setTitle("Task Settings");
            Scene scene = new Scene(root);
            settingStage.setScene(scene);
            settingStage.initModality(Modality.WINDOW_MODAL);
        }
        settingStage.showAndWait();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void init(TargetGraph tasksGraph, Set<TaskType> tasksWithPrice) {
        TasksGraph = tasksGraph;
        TasksWithPrice = tasksWithPrice;
        SimulationTask.setDisable(true);
        compilerTask.setDisable(true);

        TasksWithPrice.forEach(t -> {
            switch (t) {
                case Simulation:
                    SimulationTask.setDisable(false);
                    simulationTaskPressed(null);
                    break;
                case Compilation:
                    compilerTaskPressed(null);
                    compilerTask.setDisable(false);
                    break;
            }
        });

        runFromScratch = true;
        if (Task != null) {
            runningNumber++;
            int index = Task.getTaskName().indexOf('-');
            Task.setTaskName(Task.getTaskName().substring(0, index == -1 ? Task.getTaskName().length() : index) + "-" + runningNumber);
            taskName.setText(Task.getTaskName());
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
    void simulationTaskPressed(ActionEvent event) {
        if (SimulationTask.isSelected()) {
            compilerTask.setSelected(false);
            toggleSimulationProperties(false);
            toggleCompilerProperties(true);
        } else {
            compilerTask.setSelected(true);
            toggleSimulationProperties(true);
            toggleCompilerProperties(false);
        }
    }

    @FXML
    void cancelClicked(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
        if (runningNumber != 0)
            runningNumber--;
    }

    @FXML
    void onClickedPathToCompilation(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        compilationOutputPath = directoryChooser.showDialog(settingStage);
    }

    public void toggleSimulationProperties(boolean disable) {
        checkBoxRandom.setDisable(disable);
        processTimeText.setDisable(disable);
        successProbabilityText.setDisable(disable);
        warningProbabilityText.setDisable(disable);
    }

    public void toggleCompilerProperties(boolean disable) {
        pathToCompilation.setDisable(disable);
        pathToWorkingDir.setDisable(disable);
    }

    public void toggleDisableAll(boolean disable) {
        taskName.setDisable(disable);
        compilerTask.setDisable(disable);
        SimulationTask.setDisable(disable);
        checkBoxRandom.setDisable(disable);
        ComboTargetsToRun.setDisable(disable);
        pathToCompilation.setDisable(disable);
        pathToWorkingDir.setDisable(disable);
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
            toggleCompilerProperties(false);
        } else {
            SimulationTask.setSelected(true);
            toggleSimulationProperties(false);
            toggleCompilerProperties(true);
        }
    }

    private boolean validateTextField(TextField text) {
        return (Objects.equals(text.getStyle(), "") && text.getText() != null && !text.getText().isEmpty());
    }

    private boolean validateTaskName(TextField taskName) {
        if (!validateTextField(taskName))
            return false;

        String url = HttpClientUtil.createUrl(Constants.VALIDATE_TASK, Utils.tuple(Constants.TASKNAME, taskName.getText()));
        Response res = HttpClientUtil.runSync(url);
        boolean ok = res.code() == 200;
        if (!ok) {
            Utils.alertWarning("Task Name " + taskName + " is already in use");
        }
        return ok;
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
        if (ComboTargetsToRun.getValue() != null && validateTaskName(taskName)) {
            chooseAll = ComboTargetsToRun.getSelectionModel().getSelectedIndex() == 1;

            if (SimulationTask.isSelected()) {
                if (validateTextField(processTimeText) && validateTextField(warningProbabilityText) && validateTextField(successProbabilityText)) {
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
        if (!submitted && runningNumber != 0)
            runningNumber--;
    }

    public String getTaskName() {
        return Task.getTaskName();
    }
}