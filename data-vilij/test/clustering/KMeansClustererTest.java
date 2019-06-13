package clustering;

import algorithms.Clusterer;
import classification.RandomClassifier;
import data.DataSet;
import dataprocessors.AppData;
import org.junit.Test;
import ui.AppUI;

import java.io.*;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class KMeansClustererTest {

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
    public void parse1Line() throws IOException {
        setBoundaries();
        String filePath = "C:\\Users\\cbex9\\Desktop\\School\\Sophmore - Semester 2\\CSE 219\\medliu\\homework6\\data-vilij\\resources\\data\\sample-data.tsd";
        DataSet dataSet = DataSet.fromTSDFile(Paths.get(filePath));
        BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)));
        String line = reader.readLine();
        KMeansClusterer k = new KMeansClusterer(dataSet, boundaryIteration, boundaryInterval, boundaryCont, boundaryCluster, appData, appui);
    }

    @Test (expected = NoSuchFileException.class)
    public void parse1LineNoFile() throws IOException{
        String filePath = "data/sample.tsd";
        DataSet dataSet = DataSet.fromTSDFile(Paths.get(filePath));
        BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)));
    }

    @Test (expected = AccessDeniedException.class)
    public void testRunFault() throws IOException{
        setBoundaries();
        String filePath = "";
        DataSet dataSet = DataSet.fromTSDFile(Paths.get(filePath));
        KMeansClusterer k = new KMeansClusterer(dataSet, boundaryIteration, boundaryInterval, boundaryCont, boundaryCluster, appData, appui);
        k.run();
    }

}