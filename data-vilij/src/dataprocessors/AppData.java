/*
    Mei Qi Deanna Liu
    111041152
    CSE 219
    Homework #4
*/
package dataprocessors;

import javafx.scene.chart.XYChart;
import settings.AppPropertyTypes;
import ui.AppUI;
import vilij.components.DataComponent;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;

import java.nio.file.Path;

/**
 * This is the concrete application-specific implementation of the data component defined by the Vilij framework.
 *
 * @author Ritwik Banerjee
 * @author Deanna Liu
 * @see DataComponent
 */

public class AppData implements DataComponent {

    public TSDProcessor processor;
    private ApplicationTemplate applicationTemplate;

    public AppData(ApplicationTemplate applicationTemplate) {
        this.processor = new TSDProcessor();
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public void loadData(Path dataFilePath) {
    }


    public void loadData(String dataString) {
        PropertyManager manager = applicationTemplate.manager;
        try {
            processor.clear();
            //Clear information on the chart to get a fresh start
            boolean cont = processor.processString(dataString);                         //Process the information from the text area
            if (!cont) {
                ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().clear();
                displayData();
            }
        } catch (Exception e) {
            StringBuilder errorMessage = new StringBuilder();                           //Create the instance of the error message
            errorMessage.setLength(0);                                                  //create empty error message
            errorMessage.append(e.getClass().getSimpleName());                          //Add on the name of the error
            ErrorDialog errorDialog = ErrorDialog.getDialog();                          //Create an error dialog
            errorDialog.show(manager.getPropertyValue(AppPropertyTypes.ERROR.name()), errorMessage.toString());             //Display the error dialog
        }
    }

    @Override
    public void saveData(Path dataFilePath) {
    }


    @Override
    public void clear() {
        processor.clear();
    }

    public void displayData(){
        XYChart<Number, Number> chart = ((AppUI) applicationTemplate.getUIComponent()).getChart();
        processor.toChartData(chart);
        chart.setLegendVisible(false);
        ((AppUI)applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(false);
    }
    public void displayData2(){
        XYChart<Number, Number> chart = ((AppUI) applicationTemplate.getUIComponent()).getChart();
        processor.toChartData(chart);
        chart.setLegendVisible(true);
        ((AppUI)applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(false);
    }

    public void displayAlteredData() {
        XYChart<Number, Number> chart = ((AppUI) applicationTemplate.getUIComponent()).getChart();
        processor.toChartDataAlter(chart);
        chart.setLegendVisible(true);
        ((AppUI)applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(false);
    }

}
