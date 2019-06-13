/*
    Mei Qi Deanna Liu
    111041152
    CSE 219
    Homework #4
*/

package ui;

import actions.AppActions;
import clustering.KMeansClusterer;
import dataprocessors.AppData;
import dataprocessors.TSDProcessor;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import jdk.nashorn.internal.runtime.regexp.joni.exception.ErrorMessages;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import settings.AppPropertyTypes;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;
import static settings.AppPropertyTypes.*;
import static vilij.settings.PropertyTypes.*;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * This is the application's user interface implementation.
 *
 * @author Ritwik Banerjee
 * @author Deanna Liu
 */

public final class AppUI extends UITemplate {

    /**
     * The application to which this class of actions belongs.
     */
    public ApplicationTemplate applicationTemplate;
    AppData appData;
    public AppActions appActions;

    @SuppressWarnings("FieldCanBeLocal")
    private Button scrnshotButton;                      // toolbar button to take a screenshot of the data
    public String algorithmFilePath;
    private LineChart<Number, Number> chart;         // the chart where data will be displayed
    private TextArea textArea;                          // text area for new data input
    private TextArea hiddenTextArea;
    private String scrnshotIconPath;                    // path to the screenshot icon
    public String runIconPath;
    public String gearIconPath;
    NumberAxis xAxis = new NumberAxis();
    NumberAxis yAxis = new NumberAxis();
    public String[] hiddenIn;
    private Text instances;
    private HBox processLabelsBox;
    private VBox listLabel;
    private VBox algorithmType;
    public CheckBox toggleDoneEdit = new CheckBox("Done Editing");
    public Text whatMode;
    private int nonNull = 0;
    private HBox hiddenGears;
    VBox leftPanel = new VBox(8);
    public Button resumeButton;
    public Button runButton;
    public Text doneRunning = new Text();
    File alg;
    String resumeIconPath;
    boolean validData = true;
    public boolean loadedDataOrNot = false;


    public LineChart<Number, Number> getChart() {
        return chart;
    }

    public Button getScrnshotButton() {
        return scrnshotButton;
    }

    public Button getSaveButton() {
        return saveButton;
    }

    public void isLoadedData(boolean val){
        loadedDataOrNot = val;
    }

    private static final String SEPARATOR = "/";
    private static final String SEPARATOR2 = "\\";

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate, AppData appData) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
        this.appData = appData;
        appActions = new AppActions(this.applicationTemplate, this, this.appData);
    }
    public ArrayList<String> algorithms = new ArrayList<>();


    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
        PropertyManager uiManager = applicationTemplate.manager;
        String iconPath = SEPARATOR + String.join(SEPARATOR,
                uiManager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                uiManager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        String dataPath = SEPARATOR + String.join(SEPARATOR, uiManager.getPropertyValue(DATA_RESOURCE_PATH.name()));
        cssPath = SEPARATOR + String.join(SEPARATOR,
                uiManager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                uiManager.getPropertyValue(CSS_RESOURCE_PATH.name()),
                uiManager.getPropertyValue(CSS_RESOURCE_DATANAME.name()));
        scrnshotIconPath = String.join(SEPARATOR, iconPath, uiManager.getPropertyValue(SCREENSHOT_ICON.name()));
        gearIconPath = String.join(SEPARATOR, iconPath, uiManager.getPropertyValue(SETTINGS_ICON.name()));
        runIconPath = String.join(SEPARATOR, iconPath, uiManager.getPropertyValue(RUN_ICON.name()));
        resumeIconPath =  String.join(SEPARATOR, iconPath, uiManager.getPropertyValue(RESUME_ICON.name()));
        algorithmFilePath = String.join(SEPARATOR, dataPath, uiManager.getPropertyValue(ALGORITHMS.name()));
    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        PropertyManager uiManager = applicationTemplate.manager;
        super.setToolBar(applicationTemplate);
        scrnshotButton = setToolbarButton(scrnshotIconPath, uiManager.getPropertyValue(SCREENSHOT_TOOLTIP.name()), true);
        runButton = new Button(null, new ImageView(new Image(getClass().getResourceAsStream(runIconPath))));
        resumeButton = new Button(null, new ImageView(new Image(getClass().getResourceAsStream(resumeIconPath))));
        toolBar = new ToolBar(newButton, loadButton, saveButton, scrnshotButton, exitButton, runButton, resumeButton);
        runButton.setVisible(false);
        resumeButton.setVisible(false);
    }

    public Button getExitButton() {
        return exitButton;
    }
    public Button getNewButton() {
        return newButton;
    }

    @Override
    protected void setToolbarHandlers(ApplicationTemplate applicationTemplate) {
        PropertyManager manager = applicationTemplate.manager;
        applicationTemplate.setActionComponent(appActions);
        AppActions action = new AppActions(applicationTemplate, this, this.appData);
        newButton.setOnAction(e -> {
            try {
                applicationTemplate.getActionComponent().handleNewRequest();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        });
        saveButton.setOnAction(e -> applicationTemplate.getActionComponent().handleSaveRequest());
        loadButton.setOnAction(e -> applicationTemplate.getActionComponent().handleLoadRequest());
        exitButton.setOnAction(e -> applicationTemplate.getActionComponent().handleExitRequest());
        scrnshotButton.setOnAction(e -> {
            try {
                action.handleScreenshotRequest();
            } catch (IOException e1) {
                StringBuilder errorMessage = new StringBuilder();                           //Create the instance of the error message
                errorMessage.setLength(0);                                                  //create empty error message
                errorMessage.append(e1.getClass().getSimpleName());                          //Add on the name of the error
                ErrorDialog errorDialog = ErrorDialog.getDialog();                          //Create an error dialog
                errorDialog.show(manager.getPropertyValue(AppPropertyTypes.ERROR.name()), errorMessage.toString());                         //Display the error dialog
            }
        });

    }

    @Override
    public void initialize() {
        layout();
        setWorkspaceActions();
    }

    @Override
    public void clear() {
        resumeButton.setVisible(false);
        runButton.setVisible(false);
        appActions.configuration = "";
        doneRunning.setVisible(false);
        textArea.clear();
        saveButton.setDisable(true);
        scrnshotButton.setDisable(true);
        textArea.setDisable(false);
    }

    private void layout() {
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(algorithmFilePath)));
            String line;
            while ((line = reader.readLine()) != null)
                algorithms.add(line);}
        catch(Exception e) {
        }
        newButton.setDisable(false);
        PropertyManager manager = applicationTemplate.manager;
        chart = new LineChart<>(xAxis, yAxis);
        chart.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
        chart.setTitle(manager.getPropertyValue(AppPropertyTypes.CHART_LABEL.name()));
        chart.setVisible(false);             //hide the chart
        leftPanel.setAlignment(Pos.TOP_CENTER);
        leftPanel.setPadding(new Insets(10));
        VBox.setVgrow(leftPanel, Priority.ALWAYS);
        leftPanel.setMaxSize(windowWidth * 0.29, windowHeight * 0.3);
        leftPanel.setMinSize(windowWidth * 0.29, windowHeight * 0.3);
        textArea = new TextArea();
        textArea.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
        hiddenTextArea = new TextArea();
        textArea.setVisible(false);         //hide the text area
        hiddenTextArea.setVisible(false);
        instances = new Text();
        whatMode = new Text();
        whatMode.setText(manager.getPropertyValue(AppPropertyTypes.EDIT_MODE.name()));
        toggleDoneEdit.setVisible(false);
        whatMode.setVisible(false);

        processLabelsBox = new HBox();
        processLabelsBox.getChildren().addAll(instances);
        listLabel = new VBox();
        algorithmType = new VBox();
        leftPanel.getChildren().addAll(textArea, whatMode, toggleDoneEdit, processLabelsBox, listLabel,algorithmType);
        Text dataTitle = new Text(manager.getPropertyValue(AppPropertyTypes.CHART_LABEL.name()));

        StackPane rightPanel = new StackPane(dataTitle, chart);
        rightPanel.setMaxSize(windowWidth * 0.69, windowHeight * 0.69);
        rightPanel.setMinSize(windowWidth * 0.69, windowHeight * 0.69);
        StackPane.setAlignment(rightPanel, Pos.CENTER);
        workspace = new HBox(leftPanel, rightPanel);
       // HBox.setHgrow(workspace, Priority.ALWAYS);
        appPane.getChildren().add(workspace);
        VBox.setVgrow(appPane, Priority.ALWAYS);
    }

    private void setWorkspaceActions() {
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals("") || loadedDataOrNot) {
                saveButton.setDisable(true);
            } else {
                saveButton.setDisable(false);
            }
        });

        exitButton.setOnAction(e -> applicationTemplate.getActionComponent().handleExitRequest());
        newButton.setOnAction(e -> {
            try {
                applicationTemplate.getActionComponent().handleNewRequest();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        });
        toggleDoneEdit.setOnAction(e -> useCase3());
    }

    public void useCase3() {
        StringBuilder sb = new StringBuilder();
        String[] infomation = textArea.getText().split("\n");
        PropertyManager manager = applicationTemplate.manager;
        if(toggleDoneEdit.isSelected()) {
            for(int i = 0; i < infomation.length;i++)
                sb.append(infomation[i]+ "\n");
            verifyData();
            if(validData){
                showInformation(sb, infomation);
                appActions.showAlgorithmType(false);
                textArea.setDisable(true);
                instances.setVisible(true);
                toggleDoneEdit.setText("Continue Editing");
                whatMode.setText(manager.getPropertyValue(AppPropertyTypes.DONE_MODE.name()));
            }
            else {
                toggleDoneEdit.setSelected(false);
            }
        }
        else {
            toggleDoneEdit.setText("Done Editing");
            textArea.setDisable(false);
            whatMode.setText(manager.getPropertyValue(AppPropertyTypes.EDIT_MODE.name()));
            hideInformation();
        }
    }


    public void showInformation(StringBuilder in, String[] in2) {
        nonNull = 0;
        PropertyManager manager = applicationTemplate.manager;
        instances.setText("");
        instances.setWrappingWidth(textArea.getWidth());
        Map<String, Integer>dataLabels = countLabels(in, manager);
        instances.setText(in2.length + manager.getPropertyValue(AppPropertyTypes.INSTANCE.name())+ dataLabels.size() + manager.getPropertyValue(AppPropertyTypes.LABEL.name()) + manager.getPropertyValue(AppPropertyTypes.LABELS_ARE.name()));
        Set<String> labels = countLabels(in, manager).keySet();
        instances.setVisible(true);
        listLabel.setVisible(true);
        for (Iterator i = labels.iterator(); i.hasNext();) {
            Text labelName = new Text();
            String line1 = i.next().toString();
            if(line1.equals(""))
                labelName.setText("- null");
            else {
                labelName.setText("- " + line1);
                nonNull++;
            }
           listLabel.getChildren().add(labelName);
        }
        if(nonNull>=2)
            appActions.validClassification = true;
        else
            appActions.validClassification = false;
    }

    public Map<String, Integer> countLabels(StringBuilder in, PropertyManager manager) {
        StringBuilder errorMessage = new StringBuilder();
        Map<String, Integer> dataLabels = new HashMap<>();
        Stream.of(in.toString().split("\n")).map(line -> Arrays.asList(line.split("\t")))
                .forEach(list -> {
                    try {
                        String label = list.get(1);
                        dataLabels.put(label, Integer.parseInt("0"));

                    } catch (Exception e) {
                        errorMessage.setLength(0);
                        errorMessage.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage());
                        ErrorDialog errorDialog = ErrorDialog.getDialog();
                        errorDialog.show(manager.getPropertyValue(AppPropertyTypes.INVALID_DATA.name()), errorMessage.toString());
                    }
                });
        alterCount(dataLabels, in);
        return dataLabels;
    }

    private void alterCount(Map<String, Integer> dataLabels, StringBuilder tsdString) {
        try {
            Stream.of(tsdString.toString().split("\n")).map(line -> Arrays.asList(line.split("\t"))).forEach(list -> {
                String label = list.get(1);
                for (Iterator i = dataLabels.keySet().iterator(); i.hasNext(); ) {
                    String labelName = i.next().toString();
                    if (labelName.equals(label))
                        dataLabels.put(label, dataLabels.get(labelName) + 1);
                }
            });
        }
        catch (Exception e){};
    }

    public void hideInformation() {
        listLabel.getChildren().clear();
        algorithmType.getChildren().clear();
        listLabel.setVisible(false);
        algorithmType.setVisible(false);
        instances.setText("");
    }

    public TextArea getTextArea() {
        return textArea;
    }

    public VBox getAlgorithmType() {
        return algorithmType;
    }

    public VBox getListLabel() {
        return listLabel;
    }

    public Text getInstancesNode() {
        return instances;
    }

    public void clearText() {
        listLabel.getChildren().clear();
    }
    public TextArea getHiddenTextArea() {
        return hiddenTextArea;
    }


    private void verifyData() {
        PropertyManager manager = applicationTemplate.manager;
        if(textArea.getText().isEmpty()){
            validData = false;
            ErrorDialog errorDialog = ErrorDialog.getDialog();
            errorDialog.show(manager.getPropertyValue(AppPropertyTypes.INVALID_DATA.name()), manager.getPropertyValue(AppPropertyTypes.NO_INFO.name()));
            return;
        }
        StringBuilder errorMessage = new StringBuilder();

        String[] arr = textArea.getText().split("\n");
        List<String> names = arrToList(arr);
        errorMessage.setLength(0);
        List<String> errorNames = new ArrayList<>();
        List<String> dupe = new ArrayList<>();
        for(int i = 0; i < names.size(); i++){
            try {
                appData.processor.checkedname(names.get(i));
            } catch (TSDProcessor.InvalidDataNameException e) {
                validData = false;
                ErrorDialog errorDialog = ErrorDialog.getDialog();
                errorMessage.append("Invalid");
                errorDialog.show(manager.getPropertyValue(AppPropertyTypes.DUPE_TITLE.name()), errorMessage.toString());
                return;
            }
            for(int j = i+1 ; j < names.size(); j++){
                if(i != j && names.get(i).equals(names.get(j)) && !errorNames.contains(names.get(j))) {
                    String validName = names.get(j);
                    errorNames.add(validName);
                    dupe.add(names.get(j) + "\t" + (i+1) + " ");
                }
            }
        }

        for(int j = 0; j < errorNames.size(); j++){
            for(int i = 0; i < names.size(); i++) {
                if(i != j && names.get(i).equals(errorNames.get(j))){
                    dupe.set(j,dupe.get(j).concat((i+1) + " "));
                }
            }
        }

        for (String aDupe : dupe) {
            String[] info = aDupe.split("\t");
            String[] temp = info[1].split(" ");
            Set<String> set = new TreeSet<>(Arrays.asList(temp));
            errorMessage.append(info[0]).append(manager.getPropertyValue(AppPropertyTypes.DUPE_MESSAGE.name())).append(set.toString());
            errorMessage.append("\n");
        }

        if(errorMessage.length() > 0) {
            validData = false;                           //means there's duplicate @
            ErrorDialog errorDialog = ErrorDialog.getDialog();
            errorDialog.show(manager.getPropertyValue(AppPropertyTypes.DUPE_TITLE.name()), errorMessage.toString());
        }
        else
            validData = true;
    }

    public List<String> arrToList(String[] arr) {
        List<String> atValue = new ArrayList<>();
        String[] tabSep = separateLineByTab(arr);
        Collections.addAll(atValue, tabSep);
        return atValue;
    }

    public String[] separateLineByTab(String[] arr) {
        String[] tabSep = new String[arr.length];
        for (int i = 0; i < tabSep.length; i++) {
            tabSep[i] = arr[i].split("\t")[0];
        }
        return tabSep;
    }
}


