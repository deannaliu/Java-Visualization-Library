package clustering;

import classification.RandomClassifier;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import data.DataSet;
import dataprocessors.AppData;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import org.junit.Test;
import sun.applet.Main;
import ui.AppUI;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class RandomClustererTest {
    int boundaryCluster;
    int boundaryIteration;
    int boundaryInterval;
    AtomicBoolean boundaryCont;
    AppUI appui;
    AppData appData;

    @Test
    public void setBoundaries() {
        boundaryCluster = 1;
        boundaryIteration = 1;
        boundaryInterval = 1;
        boundaryCont = new AtomicBoolean(false);
    }

    @Test
    public void startApp() {
        Thread thread = new Thread(() -> {
            new JFXPanel(); //
            Platform.runLater(() -> {
                new Stage();
            });
        });
    }

    @Test (expected = AccessDeniedException.class)
    public void testRunNoFile() throws IOException {
        setBoundaries();
        String filePath = "";
        DataSet dataSet = DataSet.fromTSDFile(Paths.get(filePath));
        RandomClusterer r = new RandomClusterer(5, appData, appui, dataSet, boundaryIteration, boundaryInterval, boundaryCont.get());
        r.run();
    }

}