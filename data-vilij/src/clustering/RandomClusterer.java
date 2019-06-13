package clustering;
        import algorithms.Classifier;
        import algorithms.Clusterer;
        import data.DataSet;
        import dataprocessors.AppData;
        import dataprocessors.TSDProcessor;
        import javafx.application.Platform;
        import javafx.stage.Stage;
        import org.omg.Messaging.SYNC_WITH_TRANSPORT;
        import settings.AppPropertyTypes;
        import ui.AppUI;
        import vilij.components.ConfirmationDialog;
        import vilij.propertymanager.PropertyManager;
        import vilij.templates.ApplicationTemplate;

        import java.io.IOException;
        import java.nio.file.Paths;
        import java.util.ArrayList;
        import java.util.Arrays;
        import java.util.List;
        import java.util.Random;
        import java.util.concurrent.atomic.AtomicBoolean;
        import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Ritwik Banerjee
 * @author Deanna Liu
 */
public class RandomClusterer extends Clusterer {

    private int numberOfLabels;

    @SuppressWarnings("FieldCanBeLocal")
    // this mock classifier doesn't actually use the data, but a real classifier will
    private DataSet dataset;
    private AppData appdata;
    AppUI appui;
    private ReentrantLock runLock = new ReentrantLock();

    private boolean suspended = false;
    private boolean stopped = false;
    public int count;

    private final AtomicBoolean tocontinue;
    private static final Random RAND = new Random();

    private List<Integer> randomLabels = new ArrayList<>();

    private final int maxIterations;
    private final int updateInterval;

    public ConfirmationDialog newAlert = ConfirmationDialog.getDialog();


    public boolean isSuspended() {
        return suspended;
    }

    @Override
    public int getMaxIterations() {
        return maxIterations;
    }

    @Override
    public int getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public boolean tocontinue() {
        return tocontinue.get();
    }

    public RandomClusterer(int k, AppData appdata, AppUI appui, DataSet dataset, int maxIterations, int updateInterval, boolean tocontinue) {
        super(k);
        this.appdata = appdata;
        this.appui = appui;
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue);
        createRandomLabels();
    }

    public void createRandomLabels() {
        for(int i = 0; i < numberOfLabels; i++)
            randomLabels.set(i, i+1);
    }

    public void handleExitRunEvent() {
        PropertyManager manager = appui.applicationTemplate.manager;
        if(tocontinue() && !appui.loadedDataOrNot) {
            try {
                exitRequest = true;
                suspended = true;
                synchronized (this) {
                    while (suspended) {
                        wait();
                        newAlert.show(manager.getPropertyValue(AppPropertyTypes.RUNNING.name()), manager.getPropertyValue(AppPropertyTypes.UNSAVED_DATA.name()));
                        if (ConfirmationDialog.getDialog().getSelectedOption().equals(ConfirmationDialog.Option.YES)) {
                            appui.appActions.handleSaveRequest();
                            Stage window = appui.applicationTemplate.getUIComponent().getPrimaryWindow();
                            window.close();
                            System.exit(0);

                        }
                        else if(ConfirmationDialog.getDialog().getSelectedOption().equals(ConfirmationDialog.Option.NO)) {
                            Stage window = appui.applicationTemplate.getUIComponent().getPrimaryWindow();
                            window.close();
                            System.exit(0);
                        }
                        else if(ConfirmationDialog.getDialog().getSelectedOption().equals(ConfirmationDialog.Option.CANCEL)) {
                            resume();
                        }
                    }
                }
            }
            catch (Exception e){}
        }
        else if(tocontinue() && appui.loadedDataOrNot) {
            try {
                exitRequest = true;
                suspended = true;
                synchronized (this) {
                    while (suspended) {
                        wait();
                        newAlert.show(manager.getPropertyValue(AppPropertyTypes.RUNNING.name()), manager.getPropertyValue(AppPropertyTypes.EXIT_APP.name()));
                        if (ConfirmationDialog.getDialog().getSelectedOption().equals(ConfirmationDialog.Option.YES)) {
                            Stage window = appui.applicationTemplate.getUIComponent().getPrimaryWindow();
                            window.close();
                            System.exit(0);
                        } else resume();

                    }
                }

            } catch (Exception e) {
            }
        }
        else if(!tocontinue() && !appui.loadedDataOrNot) {
            newAlert.show(manager.getPropertyValue(AppPropertyTypes.RUNNING.name()), manager.getPropertyValue(AppPropertyTypes.UNSAVED_DATA.name()));
            if (ConfirmationDialog.getDialog().getSelectedOption().equals(ConfirmationDialog.Option.YES)) {
                appui.appActions.handleSaveRequest();
                Stage window = appui.applicationTemplate.getUIComponent().getPrimaryWindow();
                window.close();
                System.exit(0);
            }
            else if(ConfirmationDialog.getDialog().getSelectedOption().equals(ConfirmationDialog.Option.NO)) {
                Stage window = appui.applicationTemplate.getUIComponent().getPrimaryWindow();
                window.close();
                System.exit(0);
            }
        }
    }

    boolean exitRequest = false;

    synchronized void stop() {
        stopped = true;
        suspended = false;
        notify();
    }

    synchronized void suspend() {
        suspended = true;
    }

    synchronized void resume() {
        suspended = false;
        notify();
    }

    @Override
    public void run() {
        runLock.lock();
        try{
            appui.getExitButton().setOnAction(e->handleExitRunEvent());
            for (int i = 1; i <= maxIterations; i++) {
                int randomNumber =  (int)(Math.random() * 50 + 1);
                int howManyRuns = maxIterations/updateInterval;
                int finalIteration = maxIterations * updateInterval;
                //  appui.appActions.isRunning = true;
                // this is the real output of the classifier

                // everything below is just for internal viewing of how the output is changing
                // in the final project, such changes will be dynamically visible in the UI

                    try{
                        Platform.runLater(() -> {
                            try {
                                appdata.displayData();
                                appui.getScrnshotButton().setDisable(true);
                                appui.runButton.setDisable(true);
                            }
                            catch (Exception e){}});

                       Thread.sleep(1000);
                        resume();
                    }
                    catch (Exception e) {
                    }
                if(i * updateInterval >= maxIterations){
                    appui.runButton.setDisable(false);
                    appui.getScrnshotButton().setDisable(false);
                    appui.getExitButton().setOnAction(e->appui.appActions.handleExitRequest());
                    // appui.doneRunning.setVisible(true);
                    // appui.appActions.isRunning = false;
                }
            }}
        finally{
            runLock.unlock();
        }
    }


    // for internal viewing only
    protected void flush() {
    //    System.out.printf("%d\t%d\t%d%n", output.get(0), output.get(1), output.get(2));
    }

    /** A placeholder main method to just make sure this code runs smoothly */
    public static void main(String... args) throws IOException {
        DataSet          dataset    = DataSet.fromTSDFile(Paths.get("C:\\Users\\cbex9\\Desktop\\Correct2.tsd"));
        // RandomClassifier classifier = new RandomClassifier( dataset, 100, 5, true);
        // classifier.run(); // no multithreading yet
    }
}