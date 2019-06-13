package vilij.components;

/**
 * Defines the behavior of the core actions to be handled by an application.
 *
 * @author Ritwik Banerjee
 * @author Deanna Liu
 *
 */
public interface ActionComponent {

    void handleNewRequest() throws InterruptedException;

    void handleSaveRequest();

    void handleLoadRequest();

    void handleExitRequest();

    void handlePrintRequest();

}
