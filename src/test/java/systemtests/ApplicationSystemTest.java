package systemtests;

import static guitests.guihandles.WebViewUtil.waitUntilBrowserLoaded;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static pwe.planner.testutil.TypicalDegreePlanners.getTypicalDegreePlannerList;
import static pwe.planner.testutil.TypicalModules.getTypicalModuleList;
import static pwe.planner.testutil.TypicalRequirementCategories.getTypicalRequirementCategoriesList;
import static pwe.planner.ui.StatusBarFooter.SYNC_STATUS_INITIAL;
import static pwe.planner.ui.StatusBarFooter.SYNC_STATUS_UPDATED;
import static pwe.planner.ui.testutil.GuiTestAssert.assertListMatching;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;

import guitests.guihandles.BrowserPanelHandle;
import guitests.guihandles.CommandBoxHandle;
import guitests.guihandles.MainMenuHandle;
import guitests.guihandles.MainWindowHandle;
import guitests.guihandles.ModuleListPanelHandle;
import guitests.guihandles.ResultDisplayHandle;
import guitests.guihandles.StatusBarFooterHandle;
import pwe.planner.TestApp;
import pwe.planner.commons.core.index.Index;
import pwe.planner.commons.exceptions.IllegalValueException;
import pwe.planner.logic.commands.ClearCommand;
import pwe.planner.logic.commands.FindCommand;
import pwe.planner.logic.commands.ListCommand;
import pwe.planner.logic.commands.PlannerListCommand;
import pwe.planner.logic.commands.SelectCommand;
import pwe.planner.model.Application;
import pwe.planner.model.Model;
import pwe.planner.storage.JsonSerializableApplication;
import pwe.planner.ui.BrowserPanel;
import pwe.planner.ui.CommandBox;

/**
 * A system test class for Application, which provides access to handles of GUI components and helper methods
 * for test verification.
 */
public abstract class ApplicationSystemTest {
    @ClassRule
    public static ClockRule clockRule = new ClockRule();

    private static final List<String> COMMAND_BOX_DEFAULT_STYLE = Arrays.asList("text-input", "text-field");
    private static final List<String> COMMAND_BOX_ERROR_STYLE =
            Arrays.asList("text-input", "text-field", CommandBox.ERROR_STYLE_CLASS);

    private MainWindowHandle mainWindowHandle;
    private TestApp testApp;
    private SystemTestSetupHelper setupHelper;

    @BeforeClass
    public static void setupBeforeClass() {
        SystemTestSetupHelper.initialize();
    }

    @Before
    public void setUp() {
        setupHelper = new SystemTestSetupHelper();
        testApp = setupHelper
                .setupApplication(this::getInitialData, getModuleListFileLocation(), getDegreePlannerListFileLocation(),
                        getRequirementCategoryListFileLocation());
        mainWindowHandle = setupHelper.setupMainWindowHandle();

        waitUntilBrowserLoaded(getBrowserPanel());
        assertApplicationStartingStateIsCorrect();
    }

    @After
    public void tearDown() {
        setupHelper.tearDownStage();
    }

    /**
     * Returns the data to be loaded into the file in {@link #getModuleListFileLocation()}.
     */
    protected Application getInitialData() {

        Application application = new Application();
        try {
            application = new JsonSerializableApplication(getTypicalModuleList(), getTypicalDegreePlannerList(),
                    getTypicalRequirementCategoriesList()).toModelType();
        } catch (IllegalValueException ive) {
            assertCommandBoxShowsErrorStyle();
        }
        return application;
    }

    /**
     * Returns the directory of the data file.
     */
    protected Path getModuleListFileLocation() {
        return TestApp.SAVE_LOCATION_FOR_MODULE_LIST_TESTING;
    }

    protected Path getDegreePlannerListFileLocation() {
        return TestApp.SAVE_LOCATION_FOR_DEGREE_PLANNER_LIST_TESTING;
    }

    protected Path getRequirementCategoryListFileLocation() {
        return TestApp.SAVE_LOCATION_FOR_REQUIREMENT_CATEGORY_LIST_TESTING;
    }

    public MainWindowHandle getMainWindowHandle() {
        return mainWindowHandle;
    }

    public CommandBoxHandle getCommandBox() {
        return mainWindowHandle.getCommandBox();
    }

    public ModuleListPanelHandle getModuleListPanel() {
        return mainWindowHandle.getModuleListPanel();
    }

    public MainMenuHandle getMainMenu() {
        return mainWindowHandle.getMainMenu();
    }

    public BrowserPanelHandle getBrowserPanel() {
        return mainWindowHandle.getBrowserPanel();
    }

    public StatusBarFooterHandle getStatusBarFooter() {
        return mainWindowHandle.getStatusBarFooter();
    }

    public ResultDisplayHandle getResultDisplay() {
        return mainWindowHandle.getResultDisplay();
    }

    /**
     * Executes {@code command} in the application's {@code CommandBox}.
     * Method returns after UI components have been updated.
     */
    protected void executeCommand(String command) {
        rememberStates();
        // Injects a fixed clock before executing a command so that the time stamp shown in the status bar
        // after each command is predictable and also different from the previous command.
        clockRule.setInjectedClockToCurrentTime();

        mainWindowHandle.getCommandBox().run(command);

        waitUntilBrowserLoaded(getBrowserPanel());
    }

    /**
     * Displays all modules in the application.
     */
    protected void showAllModules() {
        executeCommand(ListCommand.COMMAND_WORD);
        assertEquals(getModel().getApplication().getModuleList().size(), getModel().getFilteredModuleList().size());
    }

    /**
     * Displays all modules with any parts of their names matching {@code keyword} (case-insensitive).
     */
    protected void showModulesWithName(String keyword) {
        executeCommand(FindCommand.COMMAND_WORD + " " + keyword);
        assertTrue(getModel().getFilteredModuleList().size() < getModel().getApplication().getModuleList().size());
    }

    /**
     * Selects the module at {@code index} of the displayed list.
     */
    protected void selectModule(Index index) {
        executeCommand(SelectCommand.COMMAND_WORD + " " + index.getOneBased());
        assertEquals(index.getZeroBased(), getModuleListPanel().getSelectedCardIndex());
    }

    /**
     * Deletes all modules in the application.
     */
    protected void deleteAllModules() {
        executeCommand(ClearCommand.COMMAND_WORD);
        assertEquals(0, getModel().getApplication().getModuleList().size());
    }

    /**
     * Displays all degree planners in the application.
     */
    protected void showAllDegreePlanners() {
        executeCommand(PlannerListCommand.COMMAND_WORD);
        assertEquals(getModel().getApplication().getDegreePlannerList().size(),
                getModel().getFilteredDegreePlannerList().size());
    }

    /**
     * Asserts that the {@code CommandBox} displays {@code expectedCommandInput}, the {@code ResultDisplay} displays
     * {@code expectedResultMessage}, the storage contains the same module objects as {@code expectedModel}
     * and the module list panel displays the modules in the model correctly.
     */
    protected void assertApplicationDisplaysExpected(String expectedCommandInput, String expectedResultMessage,
            Model expectedModel) {
        assertEquals(expectedCommandInput, getCommandBox().getInput());
        assertEquals(expectedResultMessage, getResultDisplay().getText());
        assertEquals(new Application(expectedModel.getApplication()), testApp.readStorageapplication());
        assertListMatching(getModuleListPanel(), expectedModel.getFilteredModuleList());
    }

    /**
     * Calls {@code BrowserPanelHandle}, {@code ModuleListPanelHandle} and {@code StatusBarFooterHandle} to remember
     * their current state.
     */
    private void rememberStates() {
        StatusBarFooterHandle statusBarFooterHandle = getStatusBarFooter();
        getBrowserPanel().rememberUrl();
        statusBarFooterHandle.rememberSaveLocation();
        statusBarFooterHandle.rememberSyncStatus();
        getModuleListPanel().rememberSelectedModuleCard();
    }

    /**
     * Asserts that the previously selected card is now deselected and the browser's url is now displaying the
     * default page.
     *
     * @see BrowserPanelHandle#isUrlChanged()
     */
    protected void assertSelectedCardDeselected() {
        assertEquals(BrowserPanel.DEFAULT_PAGE, getBrowserPanel().getLoadedUrl());
        assertFalse(getModuleListPanel().isAnyCardSelected());
    }

    /**
     * Asserts that the browser's url is changed to display the details of the module in the module list panel at
     * {@code expectedSelectedCardIndex}, and only the card at {@code expectedSelectedCardIndex} is selected.
     *
     * @see BrowserPanelHandle#isUrlChanged()
     * @see ModuleListPanelHandle#isSelectedModuleCardChanged()
     */
    protected void assertSelectedCardChanged(Index expectedSelectedCardIndex) {
        getModuleListPanel().navigateToCard(getModuleListPanel().getSelectedCardIndex());
        String selectedCardName = getModuleListPanel().getHandleToSelectedCard().getName();
        URL expectedUrl;
        try {
            expectedUrl = new URL(BrowserPanel.SEARCH_PAGE_URL + selectedCardName.replaceAll(" ", "%20"));
        } catch (MalformedURLException mue) {
            throw new AssertionError("URL expected to be valid.", mue);
        }
        assertEquals(expectedUrl, getBrowserPanel().getLoadedUrl());

        assertEquals(expectedSelectedCardIndex.getZeroBased(), getModuleListPanel().getSelectedCardIndex());
    }

    /**
     * Asserts that the browser's url and the selected card in the module list panel remain unchanged.
     *
     * @see BrowserPanelHandle#isUrlChanged()
     * @see ModuleListPanelHandle#isSelectedModuleCardChanged()
     */
    protected void assertSelectedCardUnchanged() {
        assertFalse(getBrowserPanel().isUrlChanged());
        assertFalse(getModuleListPanel().isSelectedModuleCardChanged());
    }

    /**
     * Asserts that the command box's shows the default style.
     */
    protected void assertCommandBoxShowsDefaultStyle() {
        assertEquals(COMMAND_BOX_DEFAULT_STYLE, getCommandBox().getStyleClass());
    }

    /**
     * Asserts that the command box's shows the error style.
     */
    protected void assertCommandBoxShowsErrorStyle() {
        assertEquals(COMMAND_BOX_ERROR_STYLE, getCommandBox().getStyleClass());
    }

    /**
     * Asserts that the entire status bar remains the same.
     */
    protected void assertStatusBarUnchanged() {
        StatusBarFooterHandle handle = getStatusBarFooter();
        assertFalse(handle.isSaveLocationChanged());
        assertFalse(handle.isSyncStatusChanged());
    }

    /**
     * Asserts that only the sync status in the status bar was changed to the timing of
     * {@code ClockRule#getInjectedClock()}, while the save location remains the same.
     */
    protected void assertStatusBarUnchangedExceptSyncStatus() {
        StatusBarFooterHandle handle = getStatusBarFooter();
        String timestamp = new Date(clockRule.getInjectedClock().millis()).toString();
        String expectedSyncStatus = String.format(SYNC_STATUS_UPDATED, timestamp);
        assertEquals(expectedSyncStatus, handle.getSyncStatus());
        assertFalse(handle.isSaveLocationChanged());
    }

    /**
     * Asserts that the starting state of the application is correct.
     */
    private void assertApplicationStartingStateIsCorrect() {
        assertEquals("", getCommandBox().getInput());
        assertEquals("", getResultDisplay().getText());
        assertListMatching(getModuleListPanel(), getModel().getFilteredModuleList());
        assertEquals(BrowserPanel.DEFAULT_PAGE, getBrowserPanel().getLoadedUrl());
        assertEquals(Paths.get("").toAbsolutePath().relativize(testApp.getStorageSaveLocation().toAbsolutePath())
                .toString(), getStatusBarFooter().getSaveLocation());
        assertEquals(SYNC_STATUS_INITIAL, getStatusBarFooter().getSyncStatus());
    }

    /**
     * Returns a defensive copy of the current model.
     */
    protected Model getModel() {
        return testApp.getModel();
    }
}
