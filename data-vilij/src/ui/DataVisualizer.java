/*
    Mei Qi Deanna Liu
    111041152
    CSE 219
    Homework #4
*/
package ui;

import actions.AppActions;
import dataprocessors.AppData;
import javafx.stage.Stage;
import vilij.templates.ApplicationTemplate;

import static vilij.settings.InitializationParams.*;

/**
 * The main class from which the application is run. The various components used here must be concrete implementations
 * of types defined in {@link vilij.components}.
 *
 * @author Ritwik Banerjee
 * @author Deanna Liu
 */

public final class DataVisualizer extends ApplicationTemplate {
    @Override
    public void start(Stage primaryStage) {
        dialogsAudit(primaryStage);
        if (propertyAudit())
            userInterfaceAudit(primaryStage);
    }

    @Override
    protected boolean propertyAudit() {
        boolean failed = manager == null || !(loadProperties(PROPERTIES_XML) && loadProperties(WORKSPACE_PROPERTIES_XML));
        if (failed)
            errorDialog.show(LOAD_ERROR_TITLE.getParameterName(), PROPERTIES_LOAD_ERROR_MESSAGE.getParameterName());
        return !failed;
    }

    @Override
    protected void userInterfaceAudit(Stage primaryStage) {
        AppData thisData = new AppData(this);
        AppUI thisUI = new AppUI(primaryStage, this, thisData);
        setUIComponent(thisUI);
        setActionComponent(new AppActions(this, thisUI, thisData));
        setDataComponent(thisData);
        uiComponent.initialize();
    }
}
