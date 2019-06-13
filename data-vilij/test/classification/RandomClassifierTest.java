package classification;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import data.DataSet;
import dataprocessors.AppData;
import org.junit.Test;
import ui.AppUI;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class RandomClassifierTest {
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

    @Test (expected = AccessDeniedException.class)
    public void testRunNoFile() throws IOException {
        setBoundaries();
        String filePath = "";
        DataSet dataSet = DataSet.fromTSDFile(Paths.get(filePath));
        RandomClassifier r = new RandomClassifier(appData, appui, dataSet, boundaryIteration, boundaryInterval, boundaryCont.get());
        r.run();
    }
}