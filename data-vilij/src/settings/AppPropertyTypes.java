/*
    Mei Qi Deanna Liu
    111041152
    CSE 219
    Homework #2
*/
package settings;

/**
 * This enumerable type lists the various application-specific property types listed in the initial set of properties to
 * be loaded from the workspace properties <code>xml</code> file specified by the initialization parameters.
 *
 * @author Ritwik Banerjee
 * @see vilij.settings.InitializationParams
 */
public enum AppPropertyTypes {

    /* resource files and folders */
    DATA_RESOURCE_PATH,
    GUIICON_RESOURCE_PATH,

    /* user interface icon file names */
    SCREENSHOT_ICON,

    /* tooltips for user interface buttons */
    SCREENSHOT_TOOLTIP,
    INVALID_CONFIG,
    INVALID_ITERATIONS,
    INVALID_INTERVALS,
    INVALID_CLUSTER,
    ALGORITHM_RUN_TYPE,
    LABEL,
    ALGORITHM_TYPE,
    CLUSTERING_TYPE,
    CLASSIFICATION_TYPE,
    ALGORITHMS,

    /* error messages */
    RESOURCE_SUBDIR_NOT_FOUND,

    /* application-specific message titles */
    SAVE_UNSAVED_WORK_TITLE,

    /* application-specific messages */
    SAVE_UNSAVED_WORK,

    /* application-specific parameters */
    DATA_FILE_EXT,
    DATA_FILE_EXT_DESC,
    IMAGE_FILE_EXT,
    IMAGE_FILE_EXT_DESC,

    CHART_LABEL,
    ERROR,
    DUPE_MESSAGE,
    DUPE_TITLE,
    LOAD_ERROR_TITLE,
    LOAD_ERROR_MSG,
    LOADED_DATA,
    FIRST_TEN,
    LINE,
    INVALID_NAME,
    INVALID_POINT,
    CHART,
    INSTANCE,
    LOADED_LABELS,
    LABELS_ARE,
    INVALID_DATA,
    DONE_MODE,
    EDIT_MODE,
    SETTINGS_ICON,
    RUN_ICON,
    RESUME_ICON,
    RUNNING,
    UNSAVED_DATA,
    EXIT_APP,
    NO_INFO,
}
