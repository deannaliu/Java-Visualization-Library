package actions;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import dataprocessors.AppData;
import dataprocessors.TSDProcessor;
import javafx.stage.Stage;
import org.junit.Test;
import ui.AppUI;
import ui.DataVisualizer;
import vilij.components.ConfirmationDialog;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class AppActionsTest {

    AppActions appActions;
    TextArea textArea = new TextArea();
    TextArea noLines = new TextArea();
    String whereToSave;
    ApplicationTemplate applicationTemplate;
    TSDProcessor tsdProcessor;
    AppData appData;
    AppUI appUI;
    PropertyManager  manager;
    Stage stage;

    @Test
    public void fillTextArea() {
        textArea.setText("@Instance1\tlabel11\t1.5,2.2\n@Instance2\tlabel1\t1.8,3");
        whereToSave = ("C:\\Users\\cbex9\\Desktop");
        tsdProcessor = new TSDProcessor();
        appActions.createExceptionDialog(new IOException());
    }

    @Test (expected = NullPointerException.class)
    public void testSaveNoLines() {
        fillTextArea();
        appActions.saveTheFile(this.noLines.getText().split("\n"), new File(whereToSave));
    }


}