package pwe.planner.ui;

import static java.time.Duration.ofMillis;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static pwe.planner.testutil.TypicalIndexes.INDEX_SECOND_MODULE;
import static pwe.planner.testutil.TypicalModules.getTypicalModules;
import static pwe.planner.ui.testutil.GuiTestAssert.assertCardDisplaysModule;
import static pwe.planner.ui.testutil.GuiTestAssert.assertCardEquals;

import java.util.Collections;
import java.util.Set;

import org.junit.Test;

import guitests.guihandles.ModuleCardHandle;
import guitests.guihandles.ModuleListPanelHandle;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import pwe.planner.model.module.Code;
import pwe.planner.model.module.Credits;
import pwe.planner.model.module.Module;
import pwe.planner.model.module.Name;
import pwe.planner.model.planner.Semester;
import pwe.planner.model.tag.Tag;

public class ModuleListPanelTest extends GuiUnitTest {
    private static final ObservableList<Module> TYPICAL_MODULES = FXCollections.observableList(getTypicalModules());

    private static final long CARD_CREATION_AND_DELETION_TIMEOUT = 2500;

    private final SimpleObjectProperty<Module> selectedModule = new SimpleObjectProperty<>();
    private ModuleListPanelHandle moduleListPanelHandle;

    @Test
    public void display() {
        initUi(TYPICAL_MODULES);

        for (int i = 0; i < TYPICAL_MODULES.size(); i++) {
            moduleListPanelHandle.navigateToCard(TYPICAL_MODULES.get(i));
            Module expectedModule = TYPICAL_MODULES.get(i);
            ModuleCardHandle actualCard = moduleListPanelHandle.getModuleCardHandle(i);

            assertCardDisplaysModule(expectedModule, actualCard);
            assertEquals(Integer.toString(i + 1) + ". ", actualCard.getId());
        }
    }

    @Test
    public void selection_modelSelectedModuleChanged_selectionChanges() {
        initUi(TYPICAL_MODULES);
        Module secondModule = TYPICAL_MODULES.get(INDEX_SECOND_MODULE.getZeroBased());
        guiRobot.interact(() -> selectedModule.set(secondModule));
        guiRobot.pauseForHuman();

        ModuleCardHandle expectedModule = moduleListPanelHandle.getModuleCardHandle(INDEX_SECOND_MODULE.getZeroBased());
        ModuleCardHandle selectedModule = moduleListPanelHandle.getHandleToSelectedCard();
        assertCardEquals(expectedModule, selectedModule);
    }

    /**
     * Verifies that creating and deleting large number of modules in {@code ModuleListPanel} requires lesser than
     * {@code CARD_CREATION_AND_DELETION_TIMEOUT} milliseconds to execute.
     */
    @Test
    public void performanceTest() {
        ObservableList<Module> backingList = createBackingList(10000);

        assertTimeoutPreemptively(ofMillis(CARD_CREATION_AND_DELETION_TIMEOUT), () -> {
            initUi(backingList);
            guiRobot.interact(backingList::clear);
        }, "Creation and deletion of module cards exceeded time limit");
    }

    /**
     * Returns a list of modules containing {@code moduleCount} modules that is used to populate the
     * {@code ModuleListPanel}.
     */
    private ObservableList<Module> createBackingList(int moduleCount) {
        ObservableList<Module> backingList = FXCollections.observableArrayList();
        for (int i = 0; i < moduleCount; i++) {
            Name name = new Name(i + "a");
            Credits credits = new Credits("999");
            Code code = new Code("CS1010");
            Set<Code> corequisites = Collections.emptySet();
            Set<Semester> semesters = Collections.emptySet();
            Set<Tag> tags = Collections.emptySet();
            Module module = new Module(code, name, credits, semesters, corequisites, tags);
            backingList.add(module);
        }
        return backingList;
    }

    /**
     * Initializes {@code moduleListPanelHandle} with a {@code ModuleListPanel} backed by {@code backingList}.
     * Also shows the {@code Stage} that displays only {@code ModuleListPanel}.
     */
    private void initUi(ObservableList<Module> backingList) {
        ModuleListPanel moduleListPanel =
                new ModuleListPanel(backingList, selectedModule, selectedModule::set);
        uiPartRule.setUiPart(moduleListPanel);

        moduleListPanelHandle = new ModuleListPanelHandle(getChildNode(moduleListPanel.getRoot(),
                ModuleListPanelHandle.MODULE_LIST_VIEW_ID));
    }
}
