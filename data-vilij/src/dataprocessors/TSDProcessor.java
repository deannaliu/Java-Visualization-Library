/*
    Mei Qi Deanna Liu
    111041152
    CSE 219
    Homework #4
*/
package dataprocessors;

import data.DataSet;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import vilij.components.ErrorDialog;

import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * The data files used by this data visualization applications follow a tab-separated format, where each data point is
 * named, labeled, and has a specific location in the 2-dimensional X-Y plane. This class handles the parsing and
 * processing of such data. It also handles exporting the data to a 2-D plot.
 * <p>
 * A sample file in this format has been provided in the application's <code>resources/data</code> folder.
 *
 * @author Ritwik Banerjee
 * @author Deanna Liu
 * @see XYChart
 */

public final class TSDProcessor {

    public static class InvalidDataNameException extends Exception {

        private static final String NAME_ERROR_MSG = "All data instance names must start with the @ character.";

        public InvalidDataNameException(String name) {
            super(String.format("Invalid name '%s'." + NAME_ERROR_MSG, name));
        }
    }

    public Map<String, String> dataLabels;
    public Map<String, Point2D> dataPoints;
    public Map<String, String> alteredDataLabels;
    public Map<String, Point2D> alteredDataPoints;
    private Map<String, String> dataNames;
    private Map<Integer, String> onlyLabels;
    private Map<Integer, Point2D> onlyPoints;
    private Map<Integer, String> onlyNames;
    ObservableList<XYChart.Series<Number, Number>> chartData;
    public Map<Point2D, String> alteredData;

    public XYChart.Series<Number, Number> avgSeries;
    private List<Double> xValues = new ArrayList<>();
    private List<Double> yValues = new ArrayList<>();

    private static final String INVALID_DATA = "Invalid Data";
    private static final String AVG_Y = "Average Y-Values";

    public TSDProcessor() {
        dataLabels = new HashMap<>();
        dataPoints = new HashMap<>();
        dataNames = new HashMap<>();
        onlyLabels = new HashMap<>();
        onlyPoints = new HashMap<>();
        onlyNames = new HashMap<>();
    }

    Double totalAddY = new Double(0.0);
    Double totalY = new Double(0.0);
    Integer count = new Integer(0);


    /**
     * Processes the data and populated two {@link Map} objects with the data.
     *
     * @param tsdString the input data provided as a single {@link String}
     * @throws Exception if the input string does not follow the <code>.tsd</code> data format
     */
    public boolean processString(String tsdString) throws Exception {
        AtomicBoolean hadAnError = new AtomicBoolean(false);
        StringBuilder errorMessage = new StringBuilder();
        AtomicInteger count = new AtomicInteger(-1);
        Stream.of(tsdString.split("\n"))
                .map(line -> Arrays.asList(line.split("\t")))
                .forEach(list -> {
                    try {
                        String name = checkedname(list.get(0));
                        String label = list.get(1);
                        String[] pair = list.get(2).split(",");
                        Point2D point = new Point2D(Double.parseDouble(pair[0]), Double.parseDouble(pair[1]));
                        dataLabels.put(name, label);
                        onlyLabels.put(count.incrementAndGet(), label);
                        dataPoints.put(name, point);
                        dataNames.put(label, name);
                        onlyPoints.put(count.intValue(), point);
                        onlyNames.put(count.intValue(), name);
                    } catch (Exception e) {
                        errorMessage.setLength(0);
                        errorMessage.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage());
                        hadAnError.set(true);
                        ErrorDialog errorDialog = ErrorDialog.getDialog();
                        errorDialog.show(INVALID_DATA, errorMessage.toString());
                    }
                });
        //if (errorMessage.length() > 0)
        //  throw new Exception(errorMessage.toString());
        if (!hadAnError.get())
            return false;
        else
            return true;
    }

    public void processDataSet(Map<String, Point2D> locations, Map<String, String> labels) {
        Set<String> labelNames = new HashSet<>(labels.values());
        dataLabels = labels; //Key = @, Value = label
        dataPoints = locations; //Key = @, value = point;
        Iterator iterator = dataLabels.values().iterator();
        int count = 0;
        while (iterator.hasNext()) {
            onlyLabels.put(count, iterator.next().toString());
            count++;
        }
        iterator = dataPoints.values().iterator();
        count = 0;
        while(iterator.hasNext()) {
            onlyPoints.put(count, (Point2D)iterator.next());
            count++;
        }
        alteredData = new HashMap<>();
    }

    public void alterInformation(DataSet dataSet, XYChart<Number, Number> chart) {
      //  dataLabels = dataSet.getLabels();
       // dataPoints = dataSet.getLocations();
        alteredDataLabels = dataSet.getLabels();
        alteredDataPoints = dataSet.getLocations();
        chartData = chart.getData();
        Iterator dataIterator = alteredDataPoints.values().iterator();
        Iterator valueIterator = alteredDataLabels.values().iterator();
        while(valueIterator.hasNext()) {
                    alteredData.put((Point2D)dataIterator.next(), valueIterator.next().toString());
        }

    }

    public void drawAPoint(XYChart<Number, Number> chart, int whichPoint, String name) {
        List<String> newdata= new ArrayList<>(alteredData.values());
        List<Point2D> newpoints= new ArrayList<>(alteredData.keySet());
            for(int k = 0; k < chart.getData().size(); k++)
                if(name.equals(chart.getData().get(k).getName())) {
                   chart.getData().get(k).getData().add(new XYChart.Data<>(newpoints.get(whichPoint).getX(), newpoints.get(whichPoint).getY()));
                   return;
                }
    }

    public List<XYChart.Series<Number, Number>> listOfDataPoints = new ArrayList<>();

   /* public void alterPoint(DataSet dataset, XYChart<Number, Number> chart) {
      /*  Iterator keyIterator = alteredData.keySet().iterator();

       for(int i = start-1; i < finish; i++) {
           while(keyIterator.hasNext()){
                XYChart.Data<Number, Number> next = (XYChart.Data<Number, Number>)keyIterator.next();
                if(chart.getData().get(0).getData().get(0).equals(next))
                    chart.getData().get(0).getData().get(0).setXValue(next.getXValue());
            }
        }

        List<String> newLabels = new ArrayList<>(dataLabels.values());
        List<String> originalKey = new ArrayList<>(dataPoints.keySet());
        List<String> alteredKey = new ArrayList<>(alteredDataPoints.keySet());
        List<Point2D> alteredPoint = new ArrayList<>(alteredDataPoints.values());
        Set<String> labels = new HashSet<>(alteredDataLabels.values());
        ObservableList<XYChart.Series<Number, Number>> chartData = chart.getData();

        for (String label : labels) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(label);
            dataset.getLabels().entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = dataset.getLocations().get(entry.getKey());
                XYChart.Data<Number, Number> dataPoint = new XYChart.Data<>(point.getX(), point.getY());
                series.getData().add(dataPoint);

            });
          //  listOfDataPoints.add(series);
            //appui.getChart().getData().add(series);
        }
                       /* for (int i = 0; i < originalKey.size(); i++) {
                            for (int j = 0; j < alteredKey.size(); j++) {
                                if (originalKey.get(i).equals(alteredKey.get(j))) {
                                    appData.processor.alterPoint(appui.getChart(), i, j, alteredPoint.get(j).getX(), alteredPoint.get(j).getY(), newLabels.get(j));
                                    j = alteredKey.size();
                                }
                                }
                        }
        List<String> val = new ArrayList<>(alteredData.values());
        List<XYChart.Data<Number,Number>> key = new ArrayList<>(alteredData.keySet());
        for(int i = 0; i < val.size(); i++) {
            String[] nameAndLab = val.get(i).split("\t");
            String name = nameAndLab[1];
            String lab = nameAndLab[0];


            for(int j = 0; j <names.size(); j++) {
                for(int l = 0; l < chart.getData().size(); l++) {
                    for(int k = 0; k < chart.getData().get(l).getData().size(); k++) {
                        for (int m = 0; m < names.size(); m++) {
                            if (names.get(m).equals(name)) {
                                chart.getData().get(l).getData().get(k).setXValue(key.get(j).getXValue());
                                chart.getData().get(l).getData().get(k).setYValue(key.get(j).getYValue());
                                if (!chart.getData().get(l).getName().equals(name)) {
                                    chart.getData().get(l).setName(lab);

                                }
                                m = names.size();
                            }
                        }
                        k = chart.getData().get(l).getData().size();

                    }
                    l = chart.getData().size();

                }
                j = names.size();
            }
        }



    }*/

        public boolean hasLabel(XYChart<Number, Number> chart, String name) {
            for(int y = 0; y < chart.getData().size(); y++) {
                if (chart.getData().get(y).getName().equals(name))
                    return true;
            }
            return false;
     }
    /**
     * Exports the data to the specified 2-D chart.
     *
     * @param chart the specified chart
     */


public List<String> names = new ArrayList<>();
    void toChartDataAlter(XYChart<Number, Number> chart) {
        Set<String> labels = new HashSet<>(dataLabels.values());
        for (String label : labels) {
            if(!hasLabel(chart, label)) {
                XYChart.Series<Number, Number> series = new XYChart.Series<>();
                series.setName(label);
                   /* dataLabels.entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                        Point2D point = dataPoints.get(entry.getKey());
                        XYChart.Data<Number, Number> dataPoint = new Data<>(point.getX(), point.getY());
                        name.set(entry.getKey());
                        names.add(entry.getKey());
                        dataPoint.setNode(new HoveredValue(name.toString()));
                        series.getData().add(dataPoint);
                   });*/
                listOfDataPoints.add(series);
                chart.getData().add(series);
            }
        }
    }

    public void putEachPoints(XYChart<Number, Number> chart, String name, Point2D point) {
        Set<String> labels = new HashSet<>(dataLabels.values());
        for(int i = 0; i < listOfDataPoints.size(); i++) {
            if(listOfDataPoints.get(i).equals(name)){
                chart.getData().get(i).getData().add(new XYChart.Data<> (point.getX(), point.getY()));
            }
        }
    }


    public void toChartData(XYChart<Number, Number> chart) {
        Set<String> labels = new HashSet<>(dataLabels.values());
        AtomicReference<String> name = new AtomicReference<>("");
        totalY = 0.0;
        totalAddY = 0.0;
        count = 0;
        for (String label : labels) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(label);
            dataLabels.entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                    Point2D point = dataPoints.get(entry.getKey());
                    XYChart.Data<Number, Number> dataPoint = new Data<>(point.getX(), point.getY());
                    name.set(entry.getKey());
                    names.add(entry.getKey());
                    dataPoint.setNode(new HoveredValue(name.toString()));
                    series.getData().add(dataPoint);
                    totalAddY += point.getY();
                    totalY++;
                    xValues.add(point.getX());
                    yValues.add(point.getY());
                    count++;
            });

            chart.getData().add(series);
        }
        Collections.sort(xValues);
    }
    public void toChartData2(XYChart<Number, Number> chart, int where) {
        Set<String> labels = new HashSet<>();
        List<String> u = new ArrayList<>(dataLabels.values());
        for(int i = where; i < u.size(); i++) {
            labels.add(u.get(i));
        }
        //Set<String> labels = new HashSet<>(dataLabels.values());
        AtomicReference<String> name = new AtomicReference<>("");
        totalY = 0.0;
        totalAddY = 0.0;
        count = 0;
        AtomicInteger ya = new AtomicInteger();
        for (String label : labels) {
            if(!hasLabel(chart, label)) {
                XYChart.Series<Number, Number> series = new XYChart.Series<>();
                series.setName(label);
                dataLabels.entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                            Point2D point = dataPoints.get(entry.getKey());
                            XYChart.Data<Number, Number> dataPoint = new Data<>(point.getX(), point.getY());
                            name.set(entry.getKey());
                            names.add(entry.getKey());
                            dataPoint.setNode(new HoveredValue(name.toString()));
                            series.getData().add(dataPoint);
                            totalAddY += point.getY();
                            totalY++;
                            xValues.add(point.getX());
                            yValues.add(point.getY());
                            count++;
                });
                chart.getData().add(series);

            }
        }
        Collections.sort(xValues);
    }
    public class HoveredValue extends StackPane {
        HoveredValue(String name) {
            setPrefSize(10, 10);
            Label label = createLabel(name);
            setOnMouseEntered(mouseEvent -> {
                getChildren().setAll(label);
                setCursor(Cursor.HAND);
                toFront();
            });
            setOnMouseExited(mouseEvent -> {
                getChildren().clear();
                setCursor(Cursor.HAND);
            });
        }

        public Label createLabel(String name) {
            final Label label = new Label(name);
            label.getStyleClass().addAll("default-color0", "chart-line-symbol", "chart-series-line");
            label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
            return label;
        }
    }

    public void createAvgLine(XYChart<Number, Number> chart) {
        avgSeries = new XYChart.Series<>();
        double min = xValues.get(0);
        double max = xValues.get(xValues.size() - 1);
        double avg = findAverageYValues();
        XYChart.Data<Number, Number> leftX = new XYChart.Data<>(min, avg);
        XYChart.Data<Number, Number> rightX = new XYChart.Data<>(max, avg);
        avgSeries.setName(AVG_Y);
        //populating the series with data
        avgSeries.getData().add(leftX);
        avgSeries.getData().add(rightX);
        chart.getData().add(avgSeries);
        removeDots(chart);
    }


    public void eraseLine(XYChart.Series<Number, Number> series) {
        avgSeries.getData().clear();
    }


    public void resetLine(XYChart<Number, Number> chart) {
        for (XYChart.Series<Number, Number> series : chart.getData()) {
            if (series.getName().equals("Random Classification")) {
                series.getData().clear();
            }
        }
    }

    public void drawLine(int A, int B, int C, XYChart<Number, Number> chart, Button screenshotButton) {
        screenshotButton.setDisable(true);
        avgSeries = new XYChart.Series<>();
        avgSeries.getData().clear();
        double minX = 0;
        if(xValues.get(0) < 0)
            minX = xValues.get(0);
        double maxX = xValues.get(xValues.size()-1);

        double minY = ((C*-1) - (A*minX))/B;
        double maxY = ((C*-1) - (A*maxX))/B;
        XYChart.Data<Number, Number> leftX = new XYChart.Data<>(minX, minY);
        XYChart.Data<Number, Number> rightX = new XYChart.Data<>(maxX, maxY);
        avgSeries.setName("Random Classification");
        //populating the series with data
        avgSeries.getData().add(leftX);
        avgSeries.getData().add(rightX);

        chart.getData().add(avgSeries);
        avgSeries.getNode().lookup(".chart-series-line").setStyle("-fx-stroke: red ");
        removeDots(chart);
    }

    public void removeDots(XYChart<Number, Number> chart) {
        for (XYChart.Series<Number, Number> series : chart.getData()) {
            if (series.getName().equals("Random Classification")) {
                for (XYChart.Data<Number, Number> data : series.getData()) {
                    // this node is StackPane
                    StackPane stackPane = (StackPane) data.getNode();
                    stackPane.setVisible(false);
                }
            }
        }
    }

    Double findAverageYValues() {
        Double avg = totalAddY/totalY;
        return avg;
    }

    void clear() {
        dataPoints.clear();
        dataLabels.clear();
    }

    public String checkedname(String name) throws InvalidDataNameException {
        if (!name.startsWith("@")) {
            throw new InvalidDataNameException(name);
        }
        return name;
    }
}