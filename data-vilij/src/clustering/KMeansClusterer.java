package clustering;
import algorithms.Clusterer;
import data.DataSet;
import dataprocessors.AppData;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.stage.Stage;
import settings.AppPropertyTypes;
import ui.AppUI;
import vilij.components.ConfirmationDialog;
import vilij.propertymanager.PropertyManager;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Ritwik Banerjee
 * @author Deanna Liu
 */
public class KMeansClusterer extends Clusterer {

    private DataSet dataset;
    private List<Point2D> centroids;

    private AppData appData;
    private AppUI appui;
    private final int           maxIterations;
    private final int           updateInterval;
    private final AtomicBoolean tocontinue;
    private final AtomicBoolean cont;
    private final int numClust;



    public KMeansClusterer(DataSet dataset, int maxIterations, int updateInterval, AtomicBoolean cont, int numberOfClusters, AppData appData, AppUI appui) {
        super(numberOfClusters);
        this.appData = appData;
        this.cont = cont;
        this.dataset = dataset;
        this.appui = appui;
        this.numClust = numberOfClusters;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(false);
    }

    synchronized void stop() {
        stopped = true;
        suspended = false;
        notify();
    }

    Thread t;
    public boolean suspended = false;
    private boolean stopped = false;
    public int count;


    synchronized void suspend() {
        suspended = true;
    }

    synchronized void resume() {
        suspended = false;
        notify();
    }
    @Override
    public int getMaxIterations() { return maxIterations; }

    @Override
    public int getUpdateInterval() { return updateInterval; }

    @Override
    public boolean tocontinue() { return tocontinue.get(); }
    public boolean toCont() {
        return cont.get();
    }
    boolean exitRequest = false;

    int where = 0;

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
    public void handleExitRunEvent() {
       ConfirmationDialog newAlert = ConfirmationDialog.getDialog();
        PropertyManager manager = appui.applicationTemplate.manager;
        if(toCont() && !appui.loadedDataOrNot) {
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
        else if(toCont() && appui.loadedDataOrNot) {
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
        else if(!toCont() && !appui.loadedDataOrNot) {
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
    @Override
    public void run() {
        where = 0;
        initializeCentroids();
        int iteration = 0;

        while (iteration++ < maxIterations & tocontinue.get()) {
            try {
                assignLabels();
                recomputeCentroids();
            }
            catch (Exception e){}
        }
        try {
            appui.getExitButton().setOnAction(e->handleExitRunEvent());
            appui.getNewButton().setOnAction(e-> {
                try {
                    handleNewRunEvent();
                } catch (InterruptedException e1) {
                }
            });
            appData.processor.alterInformation(dataset, appui.getChart());
            Platform.runLater(() -> {
                        appData.displayAlteredData(); });
            List<String> newdata= new ArrayList<>(appData.processor.alteredData.values());
            List<Point2D> newpoints= new ArrayList<>(appData.processor.alteredData.keySet());
            int howmanyshouldshow = (maxIterations/updateInterval) * numClust;
            for(int i = 1; i <= howmanyshouldshow && toCont(); i++) {
                AtomicInteger counter = new AtomicInteger(0);
                if (counter.intValue() == 0) {
                    counter = new AtomicInteger(1);
                        int finalJ = where;
                            try{
                                AtomicInteger finalCounter = counter;
                                    Platform.runLater(() -> {
                                        if(finalCounter.intValue() != numClust || numClust == 1 && where < howmanyshouldshow && finalJ < howmanyshouldshow) {
                                            appui.getScrnshotButton().setDisable(true);
                                            appui.runButton.setDisable(true);
                                            appData.processor.drawAPoint(appui.getChart(), finalJ, newdata.get(finalJ));
                                            finalCounter.incrementAndGet();
                                        }
                                        else {
                                            finalCounter.set(0);
                                        }
                                    });
                                    Thread.sleep(500);
                                    resume();
                            where++;
                        }
                        catch (Exception e){}
                }
                if(i >= howmanyshouldshow){
                    Platform.runLater(() -> {
                        appui.runButton.setDisable(false);
                        appui.getScrnshotButton().setDisable(false);
                        appui.getExitButton().setOnAction(e -> appui.appActions.handleExitRequest());
                        String[] tsdString = appui.getTextArea().getText().toString().split("\n");
                        StringBuilder ya = new StringBuilder();
                        for (int n = where; n < tsdString.length; n++)
                            ya.append(tsdString[n] + "\n");
                        try {
                            if(ya.length()!=0)
                            appData.processor.processString(ya.toString());
                        } catch (Exception e) {

                        }
                        appData.processor.toChartData2(appui.getChart(), where);
                    });
                }
            }
            int howManyRuns = maxIterations/updateInterval;

            for(int i = 1; i <= howmanyshouldshow && !toCont(); i++) {
                AtomicInteger counter = new AtomicInteger(0);
                if (counter.intValue() == 0) {
                    counter = new AtomicInteger(1);

                    try {
                        AtomicInteger finalCounter = counter;
                        int finalI = i;
                        Platform.runLater(() -> {
                            if (finalCounter.intValue() != numberOfClusters || numClust == 1) {
                                appui.getScrnshotButton().setDisable(true);
                                appui.runButton.setDisable(true);
                                for(int k = 0; k < numClust && where<howmanyshouldshow; k++) {
                                    int finalJ = where;
                                    appData.processor.drawAPoint(appui.getChart(), finalJ, newdata.get(finalJ));
                                    finalCounter.getAndIncrement();
                                    where++;
                                }
                                if (where == howmanyshouldshow) {
                                    appui.runButton.setDisable(false);
                                    appui.resumeButton.setVisible(false);
                                    appui.getExitButton().setOnAction(e -> appui.appActions.handleExitRequest());
                                        String[] tsdString = appui.getTextArea().getText().toString().split("\n");
                                        StringBuilder ya = new StringBuilder();
                                        for (int n = where; n < tsdString.length; n++)
                                            ya.append(tsdString[n] + "\n");
                                        try {
                                            if(ya.length()!=0)
                                                appData.processor.processString(ya.toString());
                                        } catch (Exception e) {

                                        }
                                        appData.processor.toChartData2(appui.getChart(), where);
                                }
                                else {
                                    appui.resumeButton.setVisible(true);
                                    appui.resumeButton.setOnAction(e -> resume());
                                }

                            } else {

                                finalCounter.set(0);
                            }
                        });
                        suspended = true;
                        synchronized (this) {
                            while (suspended)
                                wait();
                            if (stopped) {
                                break;
                            }
                        }

                    } catch (Exception e) {
                    }
                }
            }

        }

        //}
        catch (Exception e){}
    }

    private void initializeCentroids() {
        Set<String>  chosen        = new HashSet<>();
        List<String> instanceNames = new ArrayList<>(dataset.getLabels().keySet());
        Random       r             = new Random();
        while (chosen.size() < numberOfClusters) {
            int i = r.nextInt(instanceNames.size());
            while (chosen.contains(instanceNames.get(i)))
                ++i;
            chosen.add(instanceNames.get(i));
        }
        centroids = chosen.stream().map(name -> dataset.getLocations().get(name)).collect(Collectors.toList());
        tocontinue.set(true);
    }

    private void assignLabels() {
        dataset.getLocations().forEach((instanceName, location) -> {
            double minDistance      = Double.MAX_VALUE;
            int    minDistanceIndex = -1;
            for (int i = 0; i < centroids.size(); i++) {
                double distance = computeDistance(centroids.get(i), location);
                if (distance < minDistance) {
                    minDistance = distance;
                    minDistanceIndex = i;
                }
            }
            dataset.getLabels().put(instanceName, Integer.toString(minDistanceIndex));
        });
    }

    private void recomputeCentroids() {
        tocontinue.set(false);
        IntStream.range(0, numberOfClusters).forEach(i -> {
            AtomicInteger clusterSize = new AtomicInteger();
            Point2D sum = dataset.getLabels()
                    .entrySet()
                    .stream()
                    .filter(entry -> i == Integer.parseInt(entry.getValue()))
                    .map(entry -> dataset.getLocations().get(entry.getKey()))
                    .reduce(new Point2D(0, 0), (p, q) -> {
                        clusterSize.incrementAndGet();
                        return new Point2D(p.getX() + q.getX(), p.getY() + q.getY());
                    });
            Point2D newCentroid = new Point2D(sum.getX() / clusterSize.get(), sum.getY() / clusterSize.get());
            if (!newCentroid.equals(centroids.get(i))) {
                centroids.set(i, newCentroid);
                tocontinue.set(true);
            }
        });
    }

    private static double computeDistance(Point2D p, Point2D q) {
        return Math.sqrt(Math.pow(p.getX() - q.getX(), 2) + Math.pow(p.getY() - q.getY(), 2));
    }

}