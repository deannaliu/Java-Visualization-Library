package classification;

import algorithms.Classifier;
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

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Ritwik Banerjee
 * @author Deanna Liu
 */
public class RandomClassifier extends Classifier{

    private static final Random RAND = new Random();

    @SuppressWarnings("FieldCanBeLocal")
    // this mock classifier doesn't actually use the data, but a real classifier will
    private DataSet dataset;
    private AppData appdata;
    AppUI appui;
    private ReentrantLock runLock = new ReentrantLock();

    private final int maxIterations;
    private final int updateInterval;

    Thread t;
    private boolean suspended = false;
    private boolean stopped = false;
    public int count;

    // currently, this value does not change after instantiation
    private final AtomicBoolean tocontinue;

    @Override
    public int getMaxIterations() {
        return maxIterations;
    }

    public boolean isSuspended() {
        return suspended;
    }

    @Override
    public int getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public boolean tocontinue() {
        return tocontinue.get();
    }

    public RandomClassifier(AppData appdata,
                            AppUI appui,
                            DataSet dataset,
                            int maxIterations,
                            int updateInterval,
                            boolean tocontinue) {
        this.appdata = appdata;
        this.appui = appui;
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue);
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



    public void handleExitRunEvent() {
       ConfirmationDialog newAlert = ConfirmationDialog.getDialog();
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

    public void handleNewRunEvent() throws InterruptedException {
        appui.clear();
        appui.appActions.handleNewRequest();
        appui.runButton.setDisable(false);
        suspended = true;
        synchronized (this) {
            while (suspended)
                stop();
        }
    }
    @Override
    public void run() {
        runLock.lock();
        try{
        appui.getExitButton().setOnAction(e->handleExitRunEvent());
            appui.getNewButton().setOnAction(e-> {
                try {
                    handleNewRunEvent();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            });
        for(int i = 1; i <= maxIterations && !tocontinue(); i++) {
            int xCoefficient =  new Long(-1 * Math.round((2 * RAND.nextDouble() - 1) * 10)).intValue();
            int yCoefficient = 10;
            int constant     = RAND.nextInt(11);
            suspended = false;
            int finalIteration = maxIterations * updateInterval;
            int fI = maxIterations - updateInterval;
            if(finalIteration > maxIterations) {
                finalIteration = finalIteration - updateInterval;
            }
            int howManyRuns = maxIterations/updateInterval;
            // this is the real output of the classifier
            output = Arrays.asList(xCoefficient, yCoefficient, constant);
            if (i % updateInterval == 0) {
                try{
                    count++;
                 //   System.out.printf("Iteration number %d: ", i); //
                  //  flush();
                    //System.out.println(finalIteration1);
                    int finalCount = count;
                 //   System.out.println(finalCount);
                    Platform.runLater(() -> {
                        try {
                            appdata.processor.resetLine(appui.getChart());
                            appdata.processor.drawLine(xCoefficient, yCoefficient, constant, appui.getChart(), appui.getScrnshotButton());
                            appui.runButton.setDisable(true);

                            appui.resumeButton.setVisible(true);
                            appui.resumeButton.setOnAction(e->resume());
                          //  System.out.println(howManyRuns + " " + finalCount);
                            if(howManyRuns == finalCount) {
                                appui.runButton.setDisable(false);
                                appui.resumeButton.setVisible(false);

                            //    appui.appActions.isRunning = false;
                               // appui.doneRunning.setVisible(true);
                            }
                            appui.getScrnshotButton().setDisable(false);
                        }
                        catch (Exception e){}});
                    suspended = true;
                    synchronized (this) {
                        while (suspended)
                            wait();
                        if (stopped)
                            break;
                    }
                }
                catch (Exception e) {
                }

            }

          /*  if(i == maxIterations) {
                appui.runButton.setDisable(false);
                appui.resumeButton.setVisible(false);
                break;
            }*/
            if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                System.out.printf("Iteration number %d: ", i);
                flush();
                break;
            }
        }

        for (int i = 1; i <= maxIterations && tocontinue(); i++) {
            int xCoefficient =  new Long(-1 * Math.round((2 * RAND.nextDouble() - 1) * 10)).intValue();
            int yCoefficient = 10;
            int howManyRuns = maxIterations/updateInterval;
            int constant     = RAND.nextInt(11);
            int finalIteration = maxIterations * updateInterval;
          //  appui.appActions.isRunning = true;
            // this is the real output of the classifier
            output = Arrays.asList(xCoefficient, yCoefficient, constant);
            // everything below is just for internal viewing of how the output is changing
            // in the final project, such changes will be dynamically visible in the UI

            if (i % updateInterval == 0) {
                try{
                   // System.out.printf("Iteration number %d: ", i); //
                    //flush();
                    Platform.runLater(() -> {
                        try {
                            appui.getScrnshotButton().setDisable(true);
                            appdata.processor.resetLine(appui.getChart());
                            appdata.processor.drawLine(xCoefficient, yCoefficient, constant, appui.getChart(), appui.getScrnshotButton());
                            appui.runButton.setDisable(true);
                        }
                    catch (Exception e){}});

                    Thread.sleep(2000);
                    resume();
                }
                catch (Exception e) {
                }

            }
            if(i * updateInterval >= maxIterations){
                appui.runButton.setDisable(false);
                appui.getScrnshotButton().setDisable(false);
                appui.getExitButton().setOnAction(e->appui.appActions.handleExitRequest());
               // appui.doneRunning.setVisible(true);
               // appui.appActions.isRunning = false;
            }
            if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                System.out.printf("Iteration number %d: ", i);
                flush();
                break;
            }
        }}
        finally{
            runLock.unlock();
        }
    }


    // for internal viewing only
    protected void flush() {
        System.out.printf("%d\t%d\t%d%n", output.get(0), output.get(1), output.get(2));
    }

    /** A placeholder main method to just make sure this code runs smoothly */
    public static void main(String... args) throws IOException {
        DataSet          dataset    = DataSet.fromTSDFile(Paths.get("C:\\Users\\cbex9\\Desktop\\Correct2.tsd"));
       // RandomClassifier classifier = new RandomClassifier( dataset, 100, 5, true);
      // classifier.run(); // no multithreading yet
    }
}