package dataprocessors;

import actions.AppActions;
import javafx.stage.Stage;
import org.junit.Test;
import ui.AppUI;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class AppDataTest {

    AppActions appActions;
    TextArea textArea = new TextArea();
    TextArea noLines = new TextArea();
    String whereToSave;
    ApplicationTemplate applicationTemplate;
    TSDProcessor tsdProcessor;
    AppData appData;
    AppUI appUI;
    PropertyManager manager;
    Stage stage;

    @Test
    public void fillTextArea() {
        textArea.setText("@Instance1\tlabel11\t1.5,2.2\n@Instance2\tlabel1\t1.8,3");
        whereToSave = ("C:\\Users\\cbex9\\Desktop");
        tsdProcessor = new TSDProcessor();
    }

    @Test (expected = NullPointerException.class)
    public void testSaveNoLines() {
        fillTextArea();
        appActions.saveTheFile(this.noLines.getText().split("\n"), new File(whereToSave));
    }

    @Test
    public void testSave() {
        textArea.setText("@Instance1\tlabel11\t1.5,2.2\n@Instance2\tlabel1\t1.8,3");
        whereToSave = ("C:\\Users\\cbex9\\Desktop");
        AppData a = new AppData(applicationTemplate);
        a.saveData(Paths.get(whereToSave));
    }
}