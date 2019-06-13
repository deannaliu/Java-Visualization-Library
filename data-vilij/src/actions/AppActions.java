/*
    Mei Qi Deanna Liu
    111041152
    CSE 219
    Homework #4
*/
package actions;

import classification.RandomClassifier;
import clustering.KMeansClusterer;
import clustering.RandomClusterer;
import com.sun.org.apache.xpath.internal.operations.Bool;
import data.DataSet;
import dataprocessors.AppData;
import dataprocessors.TSDProcessor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import settings.AppPropertyTypes;
import vilij.components.ActionComponent;
import vilij.components.ConfirmationDialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import ui.*;

import javax.imageio.ImageIO;

import static vilij.settings.PropertyTypes.GUI_RESOURCE_PATH;
import static vilij.settings.PropertyTypes.ICONS_RESOURCE_PATH;

/**
 * This is the concrete implementation of the action handlers required by the application.
 *
 * @author Ritwik Banerjee
 * @author Deanna Liu
 */

public final class AppActions implements ActionComponent {

    public ApplicationTemplate applicationTemplate;



    Path dataFilePath;
    File savedToFile;
    AppUI appui;
    AppData appData;
    boolean clear;
    public boolean isSaved = false;
    String[] clustInfo;
    StringBuilder errorMessageLoad = new StringBuilder(0);
    public boolean validData = true;

    public StringBuilder wholeInfo = new StringBuilder(0);
    String prevPath;
    Map<String, Integer> dataLabels = new HashMap<>();
    public List<String> classConfigs = new ArrayList<>();
    public List<String> clustConfigs = new ArrayList<>();
    public List<String> randClustConfigs = new ArrayList<>();
  public boolean isRunning;
   boolean isBack;

    public String configuration = "";
    private DataSet dataSet;

    boolean classContRun = false;
    boolean clustContRun = false;
    public boolean manual = true;
    private static final String SEPARATOR = "/";


    String algorithmNamesPath;

    boolean selection = false;
    private RandomClassifier randomClassifier;

    List<String> hiddenArr = new ArrayList<>();
    public boolean validClassification;
    int nonNull = 0;

    public AppActions(ApplicationTemplate applicationTemplate, AppUI appui, AppData appData) {
        this.applicationTemplate = applicationTemplate;
        this.appui = appui;
        this.appData = appData;
        classConfigs.add("" + "\t" + "" + "\t" + "" + "\t" + "false" + "\t" + "false");
        classConfigs.add("" + "\t" + "" + "\t" + "" + "\t" + "false" + "\t" + "false");
        clustConfigs.add("" + "\t" + "" + "\t" + "" + "\t" + "false" + "\t" + "" + "\t" + "false");
        clustConfigs.add("" + "\t" + "" + "\t" + "" + "\t" + "false" + "\t" + "" + "\t" + "false");
        randClustConfigs.add("" + "\t" + "" + "\t" + "" + "\t" + "false" + "\t" + "" + "\t" + "false");
        randClustConfigs.add("" + "\t" + "" + "\t" + "" + "\t" + "false" + "\t" + "" + "\t" + "false");
    }

    @Override
    public void handleNewRequest() {
        manual = true;
        appui.getChart().getData().clear();
        PropertyManager manager = applicationTemplate.manager;
        classConfigs = new ArrayList<>();
        clustConfigs = new ArrayList<>();
        configuration = "";
        TextArea textArea = appui.getTextArea();
        Text instanceLine = appui.getInstancesNode();
        VBox labelNameList = appui.getListLabel();
        VBox algorithm = appui.getAlgorithmType();
        appui.clear();
        appui.getChart().setVisible(false);
        textArea.setDisable(false);
        appui.loadedDataOrNot = false;
        textArea.setVisible(true);
        labelNameList.setVisible(false);
        labelNameList.getChildren().clear();
        instanceLine.setVisible(false);
        instanceLine.setText("");
        appui.doneRunning.setVisible(false);
        algorithm.getChildren().clear();
        algorithm.setVisible(false);
        appui.toggleDoneEdit.setVisible(true);
        appui.toggleDoneEdit.setSelected(false);
        appui.whatMode.setText(manager.getPropertyValue(AppPropertyTypes.EDIT_MODE.name()));
        appui.whatMode.setVisible(true);
        textArea.setMinHeight(textArea.getWidth()-150);
    }



    @Override
    public void handleSaveRequest() {
        PropertyManager manager = applicationTemplate.manager;
        String info = appui.getTextArea().getText();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(applicationTemplate.getUIComponent().getTitle());
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name()), manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name())));
        StringBuilder dataEntry = new StringBuilder();
        dataEntry.append(info);
        verifyLoadedData(dataEntry);
        if (validData) {
            if (isSaved) {
                String[] content = info.split("\n");
                saveTheFile(content, savedToFile);
                appui.getSaveButton().setDisable(true);
            }
            if (!isSaved) {
                File savedFile = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
                savedToFile = savedFile;
                if (savedFile != null) {
                    String[] content = info.split("\n");
                    isSaved = saveTheFile(content, savedFile);
                    appui.getSaveButton().setDisable(true);
                }
            }
        }
    }

    public boolean saveTheFile(String[] content, File file) {
        try {
            FileWriter fileWriter = new FileWriter(file);
            for (int i = 0; i < content.length; i++) {
                String line = content[i];
                fileWriter.write(line);
                fileWriter.write("\n");
            }
            fileWriter.close();
            return true;
        } catch (IOException e) {
            createExceptionDialog(e);
            return false;
        }
    }

    @Override
    public void handleLoadRequest() {
        isSaved = true;
        TextArea textArea = appui.getTextArea();
        VBox algorithm = appui.getAlgorithmType();
        TextArea hiddenTextArea = appui.getHiddenTextArea();
        PropertyManager manager = applicationTemplate.manager;
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(null);
        int countLines = 0;
        if (selectedFile != null) {
            String fileName = selectedFile.getName();
            String filePath = selectedFile.getPath();
            dataFilePath = selectedFile.toPath();
            if (!fileName.substring(fileName.length() - 4, fileName.length()).equals(manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name()))) {
                ErrorDialog errorDialog = ErrorDialog.getDialog();                          //Create an error dialog
                errorDialog.show(manager.getPropertyValue(AppPropertyTypes.LOAD_ERROR_TITLE.name()), manager.getPropertyValue(AppPropertyTypes.LOAD_ERROR_MSG.name()) + fileName);    //Display the error dialog
            } else {
                appui.isLoadedData(false);
                appui.loadedDataOrNot = true;
                try (BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)))) {
                    String line;
                    errorMessageLoad = new StringBuilder(0);
                    wholeInfo = new StringBuilder(0);
                    while ((line = reader.readLine()) != null)
                        wholeInfo.append(line + "\n");
                    verifyLoadedData(wholeInfo);
                    if (validData) {
                        textArea.clear();
                        appui.clearText();
                        textArea.setVisible(true);
                        textArea.setDisable(true);
                        appui.whatMode.setVisible(false);
                        appui.toggleDoneEdit.setVisible(false);
                        dataLabels.clear();
                        nonNull = 0;
                        algorithm.getChildren().clear();
                        algorithm.setVisible(false);
                        textArea.setMinSize(textArea.getWidth(),180);
                        hiddenTextArea.clear();
                        BufferedReader reader1 = new BufferedReader(new FileReader(new File(filePath)));
                        while ((line = reader1.readLine()) != null) {
                            countLines++;
                            textArea.appendText(line + "\n");
                        }
                        if (countLines > 10) {
                            ErrorDialog errorDialog = ErrorDialog.getDialog();                          //Create an error dialog
                            errorMessageLoad.append(manager.getPropertyValue(AppPropertyTypes.LOADED_DATA.name()) + countLines + manager.getPropertyValue(AppPropertyTypes.FIRST_TEN.name()) + "\n");
                            errorDialog.show(manager.getPropertyValue(AppPropertyTypes.ERROR.name()), errorMessageLoad.toString());
                        }
                        showInformation(wholeInfo, countLines, fileName);
                        appui.getSaveButton().setDisable(true);
                        prevPath = filePath;
                        if(nonNull>=2)
                            validClassification = true;
                        else
                            validClassification = false;
                        showAlgorithmType(false);
                    }
                } catch (IOException e) {
                    createExceptionDialog(e);
                }
            }
        }
    }

    public StringBuilder alterLabels(String newLabel, StringBuilder wholeInfo) {
        StringBuilder temp = new StringBuilder(0);
        String[] tsdString = wholeInfo.toString().split("\n");
        clustInfo = new String[tsdString.length];
        for(int i = 0; i < tsdString.length; i++) {
            String name = tsdString[i].split("\t")[0];
            String point = tsdString[i].split("\t")[2];
            clustInfo[i] = name + "\t" + newLabel + "\t" + point + "\n";
            temp.append(clustInfo[i]);
        }
        return temp;
    }

    public Map<String, Integer> dupeLabels(StringBuilder tsdString) {
        PropertyManager manager = applicationTemplate.manager;
        AtomicBoolean hadAnError = new AtomicBoolean(false);
        StringBuilder errorMessage = new StringBuilder();
        Stream.of(tsdString.toString().split("\n"))
                .map(line -> Arrays.asList(line.split("\t")))
                .forEach(list -> {
                    try {
                        String label = list.get(1);
                        dataLabels.put(label, Integer.parseInt("0"));

                    } catch (Exception e) {
                        errorMessage.setLength(0);
                        errorMessage.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage());
                        hadAnError.set(true);
                        ErrorDialog errorDialog = ErrorDialog.getDialog();
                        errorDialog.show(manager.getPropertyValue(AppPropertyTypes.INVALID_DATA.name()), errorMessage.toString());
                    }
                });
        alterCount(dataLabels,tsdString);
        return dataLabels;
    }

    public void showInformation(StringBuilder wholeInfo, int countLines, String fileName) {
        PropertyManager manager = applicationTemplate.manager;
        TextArea textArea = appui.getTextArea();
        Text instanceLine = appui.getInstancesNode();
        VBox labelNameList = appui.getListLabel();

        Set<String> dataLabels = dupeLabels(wholeInfo).keySet();
        instanceLine.setWrappingWidth(textArea.getWidth());
        if(fileName.equals(""))
            instanceLine.setText(countLines + manager.getPropertyValue(AppPropertyTypes.INSTANCE.name())+ dataLabels.size() + manager.getPropertyValue(AppPropertyTypes.LABELS_ARE.name()));
        else
            instanceLine.setText(countLines + manager.getPropertyValue(AppPropertyTypes.INSTANCE.name()) + dataLabels.size() +
                    manager.getPropertyValue(AppPropertyTypes.LOADED_LABELS.name()) + fileName + manager.getPropertyValue(AppPropertyTypes.LABELS_ARE.name()));
        instanceLine.setVisible(true);
        labelNameList.setVisible(true);
        for (Iterator i = dataLabels.iterator(); i.hasNext();) {
            Text labelName = new Text();
            String line1 = i.next().toString();
            if(line1.equals(""))
                labelName.setText("- null");
            else {
                labelName.setText("- " + line1);
                nonNull++;
            }
            labelNameList.getChildren().add(labelName);
        }
    }

    public void showAlgorithmType(boolean back) {
        PropertyManager manager = applicationTemplate.manager;
        VBox algorithm = appui.getAlgorithmType();
        appui.runButton.setVisible(false);
        algorithm.getChildren().clear();
        ObservableList items = FXCollections.observableArrayList();
        ListView listView = new ListView();
        listView.setPrefSize(200, 75);
        Text title = new Text();
        title.setText(manager.getPropertyValue(AppPropertyTypes.ALGORITHM_TYPE.name()));
        ToggleGroup group = new ToggleGroup();
        if(back)
            items.clear();
        items.add(title);
        for(int i = 0; i < appui.algorithms.size(); i++){
            RadioButton rb = new RadioButton(appui.algorithms.get(i));
            rb.setUserData(appui.algorithms.get(i));
            rb.setToggleGroup(group);
            items.add(rb);
            if(i == 0 && !validClassification)
                rb.setDisable(true);
            else if(i == 0 && validClassification)
                rb.setDisable(false);
            int finalI = i;
            rb.setOnAction(e->handleAlgorithm(appui.algorithms.get(finalI), algorithm, back));
        }
       /* RadioButton rb1 = new RadioButton(appui.algorithms.get(0));
        rb1.setUserData(appui.algorithms.get(0));
        rb1.setToggleGroup(group);
        RadioButton rb2 = new RadioButton(appui.algorithms.get(0));
        rb2.setUserData(appui.algorithms.get(0));
        rb2.setToggleGroup(group);
        items.addAll(title, rb1, rb2);
        */
        listView.setItems(items);
        algorithm.getChildren().addAll(listView);
        algorithm.setMinHeight(1000);
        algorithm.setVisible(true);
    }

    public void handleAlgorithm(String type, VBox algorithm, boolean back) {
        this.isBack = back;
        algorithm.getChildren().clear();
        ObservableList items = FXCollections.observableArrayList();
        ListView listView = new ListView();
        listView.setPrefSize(200, 75);
        Text title = new Text();
        title.setText(type);
        ToggleGroup group = new ToggleGroup();
        RadioButton rb1 = new RadioButton("Algorithm A      ");
        rb1.setUserData("A");
        rb1.setToggleGroup(group);
        RadioButton rb2 = new RadioButton("Algorithm B      ");
        rb2.setUserData("B");
        if(type.equals("Classification") && !isBack) {
            isBack = false;
            classConfigs.clear();
            classConfigs.add(rb1.getUserData().toString() + "\t" + "" + "\t" + "" + "\t" + "false" + "\t" + "false");
            classConfigs.add(rb2.getUserData().toString() + "\t" + "" + "\t" + "" + "\t" + "false" + "\t" + "false");
            configuration = type;
        }
        else if(type.equals("Clustering") && !isBack){
            isBack = false;
            clustConfigs.clear();
            configuration = type;
            clustConfigs.add(rb1.getUserData().toString() + "\t" + "" + "\t" + "" + "\t" + "false" + "\t" + "" + "\t" + "false");
            clustConfigs.add(rb2.getUserData().toString() + "\t" + "" + "\t" + "" + "\t" + "false" + "\t" + "" + "\t" + "false");
        }
        else if(type.equals("Random Clustering") && !isBack) {
            isBack = false;
            randClustConfigs.clear();
            configuration = type;
            randClustConfigs.add(rb1.getUserData().toString() + "\t" + "" + "\t" + "" + "\t" + "false" + "\t" + "" + "\t" + "false");
            randClustConfigs.add(rb2.getUserData().toString() + "\t" + "" + "\t" + "" + "\t" + "false" + "\t" + "" + "\t" + "false");
        }
        rb2.setToggleGroup(group);
        HBox row1 = new HBox();
        HBox row2 = new HBox();
        Button settingsButton1 = new Button(null, new ImageView(new Image(getClass().getResourceAsStream(appui.gearIconPath))));
        Button settingsButton2 = new Button(null, new ImageView(new Image(getClass().getResourceAsStream(appui.gearIconPath))));
        row1.getChildren().addAll(rb1, settingsButton1);
        row2.getChildren().addAll(rb2, settingsButton2);

        items.addAll(title, row1, row2);
        listView.setItems(items);
        Button goBack = new Button("Go Back");
        algorithm.getChildren().addAll(listView, goBack);
        goBack.setOnAction(e->showAlgorithmType(true));
        algorithm.setMinHeight(1000);
        algorithm.setVisible(true);
        if(type.equals("Classification")) {
            rb1.setOnAction(e->handleRunClass(rb1, classConfigs, 0, type));
            rb2.setOnAction(e->handleRunClass(rb2, classConfigs, 1, type));
            settingsButton1.setOnAction(e->alterConfigs(settingsButton1, type, classConfigs, rb1, 0));
            settingsButton2.setOnAction(e->alterConfigs(settingsButton2, type, classConfigs, rb2, 1));
        }
        else if(type.equals("Clustering")){
            rb1.setOnAction(e->handleRunClust(rb1, clustConfigs, 0, type));
            rb2.setOnAction(e->handleRunClust(rb2, clustConfigs, 1, type));
            settingsButton1.setOnAction(e->alterConfigs(settingsButton1, type, clustConfigs, rb1, 0));
            settingsButton2.setOnAction(e->alterConfigs(settingsButton2, type, clustConfigs, rb2, 1));
        }
        else {
            rb1.setOnAction(e->handleRunClust(rb1, randClustConfigs, 0, type));
            rb2.setOnAction(e->handleRunClust(rb2, randClustConfigs, 1, type));
            settingsButton1.setOnAction(e->alterConfigs(settingsButton1, type, randClustConfigs, rb1, 0));
            settingsButton2.setOnAction(e->alterConfigs(settingsButton2, type, randClustConfigs, rb2, 1));
        }
    }

    public void alterConfigs(Button settingsButton, String type, List<String> configs, RadioButton b, int i) {
        b.setSelected(true);
        PropertyManager manager = applicationTemplate.manager;
        String[] configuration = configs.get(i).split("\t");
        TextInputDialog dialog = new TextInputDialog("walter");
        dialog.setTitle(manager.getPropertyValue(AppPropertyTypes.ALGORITHM_RUN_TYPE.name()));
        dialog.setResizable(false);
        Label label1 = new Label("Max Iterations: ");
        Label label2 = new Label("Update Interval: ");
        Label label3 = new Label("Continuous Run? ");
        TextField text1 = new TextField();
        text1.setText(configuration[1]);
        TextField text2 = new TextField();
        text2.setText(configuration[2]);
        CheckBox contRun = new CheckBox();
        contRun.setSelected(Boolean.valueOf(configuration[3]));
        Label label4 = new Label("Cluster Number: ");
        TextField text3 = new TextField();
        GridPane grid = new GridPane();
        grid.add(label1, 1, 1);
        grid.add(text1, 2, 1);
        grid.add(label2, 1, 2);
        grid.add(text2, 2, 2);
        grid.add(label3,1,3);
        grid.add(contRun,2,3);
        if(!type.equals("Classification")) {
            text3.setText(configuration[4]);
            grid.add(label4,1,4);
            grid.add(text3, 2,4);
            if(contRun.isSelected())
                clustContRun = true;
        }
        else if(type.equals("Classification"))
            classContRun = true;
        dialog.setHeaderText("");
        dialog.getDialogPane().setContent(grid);
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            checkData(text1, text2, text3, i, b, contRun, configuration, type, configs);
            if(!b.isSelected() && !selection)
                appui.runButton.setVisible(false);
            else{
                appui.runButton.setVisible(true);
                selection = true;
                appui.runButton.setOnAction(e->handleRunRequest(configs, i, type));
            }
        }
        else
            b.setSelected(false);
    }

    public void handleRunClust(RadioButton rb, List<String> configs, int i, String type) {
        if(configs.size() != 0){
            checkConfigClust(rb, configs, i, type);
            if (rb.isSelected())
                appui.runButton.setVisible(true);
            appui.runButton.setOnAction(e -> handleRunRequest(configs, i, type));
        }
    }

    public void handleRunClass(RadioButton rb, List<String> configs, int i, String type) {
        if(configs.size() != 0) {
            checkConfigClass(rb, configs, i, type);
            if (rb.isSelected())
                appui.runButton.setVisible(true);
            appui.runButton.setOnAction(e -> handleRunRequest(configs, i, type));
        }
    }

    public void handleRunRequest(List<String> configs, int i,  String type) {
        appui.getChart().getData().clear();
        String[] configurations = configs.get(i).split("\t");
        appui.getChart().setVisible(true);
        createDataSet(appui.loadedDataOrNot);
        if(type.equals("Classification")) {
            try {
                appData.processor.dataLabels.clear();
                appui.getChart().getData().clear();
                appData.processor.processString(appui.getTextArea().getText());
                appData.displayData();
                //appData.processor.drawLine(output.get(0), output.get(1), output.get(2), appui.getChart());
              //  randomClassifier = new RandomClassifier( appData, appui, dataSet, Integer.parseInt(configurations[1]), Integer.parseInt(configurations[2]), Boolean.parseBoolean(configurations[3]));
                Class<?> klass = RandomClassifier.class;
                Constructor konstructor = klass.getConstructors()[0];
                RandomClassifier mc1 = (RandomClassifier) konstructor.newInstance( appData, appui, dataSet, Integer.parseInt(configurations[1]), Integer.parseInt(configurations[2]), Boolean.parseBoolean(configurations[3]));
                //Thread object = new Thread(randomClassifier);
                Thread object = new Thread(mc1);
                object.start();
            }
            catch (Exception e) {
                createExceptionDialog(e);
            }
        }
        else if(type.equals("Clustering")) {
            appData.processor.dataLabels.clear();
            appui.getChart().getData().clear();

            Class<?> klass = KMeansClusterer.class;
            Constructor konstructor = klass.getConstructors()[0];
            try {
                appData.processor.processDataSet(dataSet.getLocations(), dataSet.getLabels()); //labels have 2 parts, need 1 of them only fix!
             //   appData.processor.alterInformation(dataSet, appui.getChart());
                //appData.displayData2();
                appui.getChart().getData().clear();
                KMeansClusterer mc1 = (KMeansClusterer)konstructor.newInstance(dataSet, Integer.parseInt(configurations[1]), Integer.parseInt(configurations[2]), new AtomicBoolean(Boolean.parseBoolean(configurations[3])), Integer.parseInt(configurations[4]), appData, appui);
                Thread object = new Thread(mc1);
                object.start();
            } catch (Exception e)
            {
            }
            //Thread object = new Thread(randomClassifier);

        }
        else {
            try {
                appData.processor.dataLabels.clear();
                appui.getChart().getData().clear();
                int randomNumber =  (int)(Math.random() * appui.getTextArea().getText().split("\n").length + 1);
                StringBuilder newInfo = appui.appActions.alterLabels(randomNumber + "", wholeInfo);
                appData.processor.processString(newInfo.toString());
                //appData.processor.drawLine(output.get(0), output.get(1), output.get(2), appui.getChart());
                //  randomClassifier = new RandomClassifier( appData, appui, dataSet, Integer.parseInt(configurations[1]), Integer.parseInt(configurations[2]), Boolean.parseBoolean(configurations[3]));
                Class<?> klass = RandomClusterer.class;
                Constructor konstructor = klass.getConstructors()[0];
                RandomClusterer mc1 = (RandomClusterer) konstructor.newInstance(5, appData, appui, dataSet, Integer.parseInt(configurations[1]), Integer.parseInt(configurations[2]), Boolean.parseBoolean(configurations[3]));
                //Thread object = new Thread(randomClassifier);
                Thread object = new Thread(mc1);
                object.start();
            }
            catch (Exception e) {
                createExceptionDialog(e);
            }
        }
    }


    public void createDataSet(boolean loaded) {
        PropertyManager manager = applicationTemplate.manager;
        AtomicBoolean hadAnError = new AtomicBoolean(false);
        StringBuilder errorMessage = new StringBuilder();
        if(loaded) {
            try{
               dataSet = DataSet.fromTSDFile(dataFilePath);
            }
            catch (Exception e) {
                errorMessage.setLength(0);
                errorMessage.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage());
                hadAnError.set(true);
                ErrorDialog errorDialog = ErrorDialog.getDialog();
                errorDialog.show(manager.getPropertyValue(AppPropertyTypes.ERROR.name()) , errorMessage.toString());
            }
        }
        else {
            try {
                dataSet = DataSet.fromTextArea(appui.getTextArea());
            }
            catch (Exception e) {
                errorMessage.setLength(0);
                errorMessage.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage());
                hadAnError.set(true);
                ErrorDialog errorDialog = ErrorDialog.getDialog();
                errorDialog.show(manager.getPropertyValue(AppPropertyTypes.ERROR.name()) , errorMessage.toString());
            }
        }
    }


    public void checkConfigClust(RadioButton rb1, List<String> configs, int i, String type) {
        String[] configuration = configs.get(i).split("\t");
        if(configuration[1].equals("") || configuration[2].equals("") || configuration[4].equals(""))
            promptToRun(configuration, configs, i, type, rb1);
    }

    public void checkConfigClass(RadioButton rb1, List<String> configs, int i, String type) {
        String[] configuration = configs.get(i).split("\t");
        if(configuration[1].equals("") || configuration[2].equals(""))
            promptToRun(configuration, configs, i, type, rb1);
    }

    public void promptToRun(String[] configs, List<String> listconfigs, int i, String type, RadioButton rb1) {
        PropertyManager manager = applicationTemplate.manager;
        TextInputDialog dialog = new TextInputDialog("walter");
        dialog.setTitle(manager.getPropertyValue(AppPropertyTypes.ALGORITHM_RUN_TYPE.name()));
        dialog.setResizable(false);
        Label label1 = new Label("Max Iterations: ");
        Label label2 = new Label("Update Interval: ");
        Label label3 = new Label("Continuous Run? ");
        TextField text1 = new TextField();
        text1.setText(configs[1]);
        TextField text2 = new TextField();
        text2.setText(configs[2]);
        CheckBox contRun = new CheckBox();
        contRun.setSelected(Boolean.valueOf(configs[3]));
        Label label4 = new Label("Cluster Number: ");
        TextField text3 = new TextField();
        GridPane grid = new GridPane();
        grid.add(label1, 1, 1);
        grid.add(text1, 2, 1);
        grid.add(label2, 1, 2);
        grid.add(text2, 2, 2);
        grid.add(label3,1,3);
        grid.add(contRun,2,3);
        if(!type.equals("Classification")) {
            text3.setText(configs[4]);
            grid.add(label4,1,4);
            grid.add(text3, 2,4);
        }
        dialog.setHeaderText("");
        dialog.getDialogPane().setContent(grid);
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent())
            checkData(text1,text2, text3, i, rb1, contRun,configs,type, listconfigs);
        else{
            rb1.setSelected(false);
            appui.runButton.setVisible(false);
        }
    }

    public void checkData(TextField text1, TextField text2, TextField text3, int i, RadioButton rb1, CheckBox contRun, String[] configs, String type, List<String> listconfigs) {
        PropertyManager manager = applicationTemplate.manager;
        ErrorDialog dialog = ErrorDialog.getDialog();
        try {
            if (type.equals("Classification")) {
                if (text1.getText().equals("") || text2.getText().equals("") || !validNumber(text1.getText()) || !validNumber(text2.getText()) || Integer.parseInt(text1.getText()) < 0 || Integer.parseInt(text2.getText()) < 0) {
                    String errorMsg = "All entries must be valid numbers and greater than 0. Try Again";
                    dialog.show("Invalid Configurations", errorMsg);
                    promptToRun(configs, listconfigs, i, type, rb1);
                } else {
                    listconfigs.set(i, rb1.getUserData() + "\t" + text1.getText() + "\t" + text2.getText() + "\t" + String.valueOf(contRun.isSelected()) + "\t" + "true");
                    appui.runButton.setVisible(true);
                }
            } else{
                if (text1.getText().equals("") || text3.getText().equals("") || !validNumber(text3.getText()) || Integer.parseInt(text1.getText()) < 0 || Integer.parseInt(text2.getText()) < 0 || Integer.parseInt(text3.getText()) < 0) {
                    String errorMsg = "All entries must be valid numbers and greater than 0. Try Again";
                    dialog.show("Invalid Configurations", errorMsg);
                    promptToRun(configs, listconfigs, i, type, rb1);
                } else {
                    listconfigs.set(i, rb1.getUserData() + "\t" + text1.getText() + "\t" + text2.getText() + "\t" + String.valueOf(contRun.isSelected()) + "\t" + text3.getText() + "\t" + "true");
                }
            }
        }
        catch (Exception e) {
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.setLength(0);
            errorMessage.append(e.getClass().getSimpleName());
            ErrorDialog errorDialog = ErrorDialog.getDialog();
            errorDialog.show(manager.getPropertyValue(AppPropertyTypes.ERROR.name()), errorMessage.toString());
        }
    }


    public boolean validNumber(String num) {
        try
        {
            int i = Integer.parseInt(num);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    public void verifyLoadedData(StringBuilder info) {
        PropertyManager manager = applicationTemplate.manager;
        StringBuilder errorMessage = new StringBuilder();
        String[] arr = info.toString().split("\n");
        List<String> names = appui.arrToList(arr);
        errorMessage.setLength(0);
        List<String> errorNames = new ArrayList<>();
        List<String> dupe = new ArrayList<>();

        AtomicInteger count = new AtomicInteger(0);
        Stream.of(info.toString().split("\n")).map(line -> Arrays.asList(line.split("\t"))).forEach(list -> {
            count.incrementAndGet();
            String name = list.get(0);
            if (!name.startsWith("@"))
                errorMessage.append(manager.getPropertyValue(AppPropertyTypes.LINE.name()) + (count.intValue()) + manager.getPropertyValue(AppPropertyTypes.INVALID_NAME.name()) + "\n");
            try {
                String[] pair = list.get(2).split(",");
                Double.parseDouble(pair[0]);
                Double.parseDouble(pair[1]);
                if (pair.length < 2 || pair.length > 2 || pair[0].equals("") || pair[1].equals(""))
                    errorMessage.append(manager.getPropertyValue(AppPropertyTypes.LINE.name()) + (count.intValue()) + manager.getPropertyValue(AppPropertyTypes.INVALID_POINT.name()) + "\n");
            } catch (Exception e) {
                errorMessage.append(manager.getPropertyValue(AppPropertyTypes.LINE.name()) + (count.intValue()) + manager.getPropertyValue(AppPropertyTypes.INVALID_POINT.name()) + "\n");
            }
        });

        for (int i = 0; i < names.size(); i++) {
            for (int j = i + 1; j < names.size(); j++) {
                String validName = names.get(j);
                if (i != j && names.get(i).equals(names.get(j)) && !errorNames.contains(names.get(j))) {
                    errorNames.add(validName);
                    dupe.add(names.get(j) + "\t" + (i + 1) + " ");
                }
            }
        }
        for (int j = 0; j < errorNames.size(); j++) {
            for (int i = 0; i < names.size(); i++) {
                if (i != j && names.get(i).equals(errorNames.get(j))) {
                    dupe.set(j, dupe.get(j).concat((i + 1) + " "));
                }
            }
        }

        for (int i = 0; i < dupe.size(); i++) {
            String[] infoLine = dupe.get(i).split("\t");
            String[] temp = infoLine[1].split(" ");
            Set<String> set = new TreeSet<>(Arrays.asList(temp));
            errorMessage.append(infoLine[0] + manager.getPropertyValue(AppPropertyTypes.DUPE_MESSAGE.name()) + set.toString());
            errorMessage.append("\n");
        }
        if (errorMessage.length() > 0) {
            validData = false;                           //means there's duplicate @
            ErrorDialog errorDialog = ErrorDialog.getDialog();
            errorDialog.show(manager.getPropertyValue(AppPropertyTypes.ERROR.name()), errorMessage.toString());
        } else
            validData = true;
    }



    private void alterCount(Map<String, Integer> dataLabels, StringBuilder tsdString) {
        Stream.of(tsdString.toString().split("\n")).map(line -> Arrays.asList(line.split("\t"))).forEach(list -> {
            String label = list.get(1);
            for (Iterator i = dataLabels.keySet().iterator(); i.hasNext(); ) {
                String labelName = i.next().toString();
                if (labelName.equals(label))
                    dataLabels.put(label, dataLabels.get(labelName) + 1);
            }
        });
    }

    @Override
    public void handleExitRequest() {
        ConfirmationDialog newAlert = ConfirmationDialog.getDialog();
        // TODO for homework 1
        if(isSaved || appui.getTextArea().getText().isEmpty()) {
            Stage window = applicationTemplate.getUIComponent().getPrimaryWindow();
            window.close();
            System.exit(0);
        }
        else if(!isSaved && !appui.getTextArea().getText().isEmpty()) {
            newAlert.show("Unsaved Data", "You have unsaved data, would you like to save?");
            if (ConfirmationDialog.getDialog().getSelectedOption().equals(ConfirmationDialog.Option.YES)) {
                handleSaveRequest();
                Stage window = appui.applicationTemplate.getUIComponent().getPrimaryWindow();
                window.close();
                System.exit(0);

            } else if (ConfirmationDialog.getDialog().getSelectedOption().equals(ConfirmationDialog.Option.NO)) {
                Stage window = appui.applicationTemplate.getUIComponent().getPrimaryWindow();
                window.close();
                System.exit(0);
            }

        }
        else {
            Stage window = applicationTemplate.getUIComponent().getPrimaryWindow();
            window.close();
            System.exit(0);

        }}

    @Override
    public void handlePrintRequest() {
    }

    public void handleScreenshotRequest() throws IOException {
        PropertyManager manager = applicationTemplate.manager;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(manager.getPropertyValue(AppPropertyTypes.CHART.name()));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(manager.getPropertyValue(AppPropertyTypes.IMAGE_FILE_EXT.name()), manager.getPropertyValue(AppPropertyTypes.IMAGE_FILE_EXT_DESC.name())));
        File savedFile = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
        XYChart<Number,Number> chart = ((AppUI) applicationTemplate.getUIComponent()).getChart();
        WritableImage image = chart.snapshot(new SnapshotParameters(), null);
        if(savedFile != null)
            savePicture(image, savedFile);
    }

    public void savePicture(Image image, File output) {
        BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
        try {
            ImageIO.write(bImage, "png", output);
        } catch (IOException e) {
            createExceptionDialog(e);
        }

    }

    public void createExceptionDialog(Exception e){
        PropertyManager manager = applicationTemplate.manager;
        StringBuilder errorMessage = new StringBuilder();                           //Create the instance of the error message
        errorMessage.setLength(0);                                                  //create empty error message
        errorMessage.append(e.getClass().getSimpleName());                          //Add on the name of the error
        ErrorDialog errorDialog = ErrorDialog.getDialog();                          //Create an error dialog
        errorDialog.show(manager.getPropertyValue(AppPropertyTypes.ERROR.name()), errorMessage.toString());             //Display the error dialog
    }

    /**
     * This helper method verifies that the user really wants to save their unsaved work, which they might not want to
     * do. The user will be presented with three options:
     * <ol>
     * <li><code>yes</code>, indicating that the user wants to save the work and continue with the action,</li>
     * <li><code>no</code>, indicating that the user wants to continue with the action without saving the work, and</li>
     * <li><code>cancel</code>, to indicate that the user does not want to continue with the action, but also does not
     * want to save the work at this point.</li>
     * </ol>
     *
     * @return <code>false</code> if the user presses the <i>cancel</i>, and <code>true</code> otherwise.
     */
    private boolean promptToSave() throws IOException {
        ConfirmationDialog newAlert = ConfirmationDialog.getDialog();
        PropertyManager manager = applicationTemplate.manager;
        newAlert.show(manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK_TITLE.name()), manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK.name()));             //Create an alert asking if user would like to save the data
        if (ConfirmationDialog.getDialog().getSelectedOption().equals(ConfirmationDialog.Option.YES)) {
            clear = true;                                                                                       //If user chose YES, then return true and set clear to be true
            return true;
        }
        else if(ConfirmationDialog.getDialog().getSelectedOption().equals(ConfirmationDialog.Option.CANCEL))
            clear = false;                                                                                      //If user chose CANCEL, then set clear to be false
        else if(ConfirmationDialog.getDialog().getSelectedOption().equals(ConfirmationDialog.Option.NO))
            clear = true;                                                                                       //If user chose NO, then clear is set to false
        return false;                                                                                           //Otherwise, return false
    }
}
