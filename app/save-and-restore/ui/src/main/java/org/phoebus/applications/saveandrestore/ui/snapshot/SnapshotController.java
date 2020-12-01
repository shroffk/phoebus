/**
 * Copyright (C) 2019 European Spallation Source ERIC.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * <a target="_blank" href="https://icons8.com/icons/set/percentage">Percentage icon</a> icon by <a target="_blank" href="https://icons8.com">Icons8</a>
 * <a target="_blank" href="https://icons8.com/icons/set/price-tag">Price Tag icon</a> icon by <a target="_blank" href="https://icons8.com">Icons8</a>
 * <a target="_blank" href="https://icons8.com/icons/set/add-tag">Add Tag icon</a> icon by <a target="_blank" href="https://icons8.com">Icons8</a>
 * <a target="_blank" href="https://icons8.com/icons/set/remove-tag">Remove Tag icon</a> icon by <a target="_blank" href="https://icons8.com">Icons8</a>
 */
package org.phoebus.applications.saveandrestore.ui.snapshot;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.util.converter.DoubleStringConverter;
import org.awaitility.core.ConditionTimeoutException;
import org.epics.gpclient.GPClient;
import org.epics.gpclient.PVConfiguration;
import org.epics.gpclient.PVEvent;
import org.epics.gpclient.PVReader;
import org.epics.vtype.Alarm;
import org.epics.vtype.Display;
import org.epics.vtype.Time;
import org.epics.vtype.VEnum;
import org.epics.vtype.VNumber;
import org.epics.vtype.VNumberArray;
import org.epics.vtype.VString;
import org.epics.vtype.VStringArray;
import org.epics.vtype.VType;
import org.phoebus.applications.saveandrestore.ApplicationContextProvider;
import org.phoebus.applications.saveandrestore.Messages;
import org.phoebus.applications.saveandrestore.SafeMultiply;
import org.phoebus.applications.saveandrestore.Utilities;
import org.phoebus.applications.saveandrestore.data.NodeChangedListener;
import org.phoebus.applications.saveandrestore.model.ConfigPv;
import org.phoebus.applications.saveandrestore.model.Node;
import org.phoebus.applications.saveandrestore.model.NodeType;
import org.phoebus.applications.saveandrestore.model.SnapshotItem;
import org.phoebus.applications.saveandrestore.service.SaveAndRestoreException;
import org.phoebus.applications.saveandrestore.service.SaveAndRestoreService;
import org.phoebus.applications.saveandrestore.ui.model.*;
import org.phoebus.framework.preferences.PreferencesReader;
import org.phoebus.ui.docking.DockPane;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.awaitility.Awaitility.await;

public class SnapshotController implements NodeChangedListener {

    @FXML
    private TextArea snapshotComment;

    @FXML
    private TextField createdBy;

    @FXML
    private TextField createdDate;

    @FXML
    private VBox vBox;

    @FXML
    private TextField snapshotName;

    @FXML
    private Button restoreButton;

    @FXML
    private Button saveSnapshotButton;

    @FXML
    private ToggleButton showLiveReadbackButton;

    @FXML
    private ToggleButton showStoredReadbackButton;

    @FXML
    private ToggleButton showTreeTableButton;

    @FXML
    private Label thresholdLabel;

    @FXML
    private Spinner<Double> thresholdSpinner;

    @FXML
    private Label multiplierLabel;

    @FXML
    private Spinner<Double> multiplierSpinner;

    @FXML
    private ToggleButton showHideDeltaPercentageButton;

    @FXML
    private ToggleButton hideShowEqualItemsButton;

    @FXML
    private TextField filterTextField;

    @FXML
    private CheckBox preserveSelectionCheckBox;

    private SnapshotTable snapshotTable;

    private SnapshotTreeTable snapshotTreeTable;

    /**
     * The {@link SnapshotTab} controlled by this controller.
     */
    private SnapshotTab snapshotTab;

    @Autowired
    private SaveAndRestoreService saveAndRestoreService;

    @Autowired
    private String defaultEpicsProtocol;

    private SimpleStringProperty createdByTextProperty = new SimpleStringProperty();
    private SimpleStringProperty createdDateTextProperty = new SimpleStringProperty();
    private SimpleStringProperty snapshotNameProperty = new SimpleStringProperty();
    private SimpleStringProperty snapshotCommentProperty = new SimpleStringProperty();
    private SimpleStringProperty snapshotUniqueIdProperty = new SimpleStringProperty();

    private List<VSnapshot> snapshots = new ArrayList<>(10);
    private final Map<String, PV> pvs = new HashMap<>();
    private final Map<String, String> readbacks = new HashMap<>();
    private final Map<String, TableEntry> tableEntryItems = new LinkedHashMap<>();
    private final BooleanProperty snapshotRestorableProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty snapshotSaveableProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty showLiveReadbackProperty = new SimpleBooleanProperty(false);

    private final ObservableSet<Integer> dirtySnapshotEntries = FXCollections.observableSet();
    private String persistentSnapshotName = null;
    private boolean persistentGoldenState = false;

    private boolean showStoredReadbacks = false;

    private boolean showDeltaPercentage = false;
    private boolean hideEqualItems;

    private PreferencesReader preferencesReader = (PreferencesReader) ApplicationContextProvider.getApplicationContext().getBean("preferencesReader");
    private final SimpleBooleanProperty showTreeTable = new SimpleBooleanProperty(false);
    private boolean isTreeTableViewEnabled = preferencesReader.getBoolean("treeTableView.enable");
    private final int cagetTimeoutMs = preferencesReader.getInt("ca.cagetTimeout");
    private final int caputTimeoutMs = preferencesReader.getInt("ca.caputTimeout");
    private final int pvConnectTimeoutMs = preferencesReader.getInt("ca.pvConnectTimeout");

    private Node config;

    private static Executor UI_EXECUTOR = Platform::runLater;

    //private SimpleBooleanProperty snapshotNodePropertiesDirty = new SimpleBooleanProperty(false);

    public static final Logger LOGGER = Logger.getLogger(SnapshotController.class.getName());

    private static final String DESCRIPTION_PROPERTY = "description";
    /**
     * The time between updates of dynamic data in the table, in ms
     */
    public static final long TABLE_UPDATE_INTERVAL = 500;

    private List<List<Pattern>> regexPatterns = new ArrayList<>();

    @FXML
    public void initialize() {

        snapshotComment.textProperty().bindBidirectional(snapshotCommentProperty);
        createdBy.textProperty().bind(createdByTextProperty);
        createdDate.textProperty().bind(createdDateTextProperty);
        snapshotName.textProperty().bindBidirectional(snapshotNameProperty);

        snapshotTable = new SnapshotTable(this);
        vBox.getChildren().add(snapshotTable);

        if (isTreeTableViewEnabled) {
            snapshotTreeTable = new SnapshotTreeTable(this);

            showTreeTable.addListener((observableValue, aBoolean, on) -> {
                if (on) {
                    vBox.getChildren().remove(snapshotTable);
                    vBox.getChildren().add(snapshotTreeTable);
                } else {
                    vBox.getChildren().remove(snapshotTreeTable);
                    vBox.getChildren().add(snapshotTable);
                }
            });
        }

        saveSnapshotButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
           boolean canSave = snapshotSaveableProperty.get() && (!snapshotNameProperty.isEmpty().get() && !snapshotCommentProperty.isEmpty().get());
            return !canSave;
        }, snapshotSaveableProperty, snapshotNameProperty, snapshotCommentProperty));

        showLiveReadbackButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/icons/show_live_readback_column.png"))));
        showLiveReadbackButton.setTooltip(new Tooltip(Messages.toolTipShowLiveReadback));
        showLiveReadbackProperty.bind(showLiveReadbackButton.selectedProperty());
        showLiveReadbackButton.selectedProperty().addListener((a, o, n) -> {
            UI_EXECUTOR.execute(() -> {
                ArrayList arrayList = new ArrayList(tableEntryItems.values());
                snapshotTable.updateTable(arrayList, snapshots, showLiveReadbackProperty.get(), showStoredReadbacks, showDeltaPercentage);
                if (isTreeTableViewEnabled) {
                    snapshotTreeTable.updateTable(arrayList, snapshots, showLiveReadbackProperty.get(), showStoredReadbacks, showDeltaPercentage);
                }
            });
        });

        showStoredReadbackButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/icons/show_stored_readback_column.png"))));
        showStoredReadbackButton.setTooltip(new Tooltip(Messages.toolTipShowStoredReadback));
        showStoredReadbackButton.selectedProperty().addListener((a, o, n) -> {
            UI_EXECUTOR.execute(() -> {
                ArrayList arrayList = new ArrayList(tableEntryItems.values());
                snapshotTable.updateTable(arrayList, snapshots, showLiveReadbackProperty.get(), showStoredReadbacks, showDeltaPercentage);
                if (isTreeTableViewEnabled) {
                    snapshotTreeTable.updateTable(arrayList, snapshots, showLiveReadbackProperty.get(), showStoredReadbacks, showDeltaPercentage);
                }
            });
        });

        if (isTreeTableViewEnabled) {
            showTreeTableButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/icons/show_tree_table_view.png"))));
            showTreeTableButton.setTooltip(new Tooltip(Messages.toolTipShowTreeTable));
            showTreeTableButton.selectedProperty().bindBidirectional(showTreeTable);
        } else {
            showTreeTableButton.setVisible(false);
        }

        thresholdLabel.setText(Messages.labelThreshold);

        SpinnerValueFactory<Double> thresholdSpinnerValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 999.0, 0.0, 0.01);
        thresholdSpinnerValueFactory.setConverter(new DoubleStringConverter());
        thresholdSpinner.setValueFactory(thresholdSpinnerValueFactory);
        thresholdSpinner.getEditor().setAlignment(Pos.CENTER_RIGHT);
        thresholdSpinner.getEditor().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        thresholdSpinner.getEditor().textProperty().addListener((a, o, n) -> {
            thresholdSpinner.getEditor().getStyleClass().remove("input-error");
            thresholdSpinner.setTooltip(null);

            Double parsedNumber = null;
            try {
                parsedNumber = Double.parseDouble(n.trim());
                updateThreshold(parsedNumber);
            } catch (Exception e) {
                thresholdSpinner.getEditor().getStyleClass().add("input-error");
                thresholdSpinner.setTooltip(new Tooltip(Messages.toolTipMultiplierSpinner));
            }
        });

        multiplierLabel.setText(Messages.labelMultiplier);

        SpinnerValueFactory<Double> multiplierSpinnerValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 999.0, 1.0, 0.01);
        multiplierSpinnerValueFactory.setConverter(new DoubleStringConverter());
        multiplierSpinner.setValueFactory(multiplierSpinnerValueFactory);
        multiplierSpinner.getEditor().setAlignment(Pos.CENTER_RIGHT);
        multiplierSpinner.getEditor().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        multiplierSpinner.getEditor().textProperty()
                .addListener((a, o, n) -> {
                    multiplierSpinner.getEditor().getStyleClass().remove("input-error");
                    multiplierSpinner.setTooltip(null);
                    snapshotRestorableProperty.set(true);

                    Double parsedNumber = null;
                    try {
                        parsedNumber = Double.parseDouble(n.trim());
                        updateSnapshot(parsedNumber);
                    } catch (NumberFormatException e) {
                        multiplierSpinner.getEditor().getStyleClass().add("input-error");
                        multiplierSpinner.setTooltip(new Tooltip(Messages.toolTipMultiplierSpinner));
                        snapshotRestorableProperty.set(false);
                    }
                });

        ImageView showHideDeltaPercentageButtonImageView = new ImageView(new Image(getClass().getResourceAsStream("/icons/show_hide_delta_percentage.png")));
        showHideDeltaPercentageButtonImageView.setFitWidth(16);
        showHideDeltaPercentageButtonImageView.setFitHeight(16);

        showHideDeltaPercentageButton.setGraphic(showHideDeltaPercentageButtonImageView);
        showHideDeltaPercentageButton.setTooltip(new Tooltip(Messages.toolTipShowHideDeltaPercentageToggleButton));
        showHideDeltaPercentageButton.selectedProperty()
                .addListener((a, o, n) -> {
                    showDeltaPercentage = n;

                    UI_EXECUTOR.execute(() -> {
                        ArrayList arrayList = new ArrayList(tableEntryItems.values());
                        snapshotTable.updateTable(arrayList, snapshots, showLiveReadbackProperty.get(), showStoredReadbacks, showDeltaPercentage);
                        if (isTreeTableViewEnabled) {
                            snapshotTreeTable.updateTable(arrayList, snapshots, showLiveReadbackProperty.get(), showStoredReadbacks, showDeltaPercentage);
                        }
                    });
                });

        hideShowEqualItemsButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/icons/hide_show_equal_items.png"))));
        hideShowEqualItemsButton.setTooltip(new Tooltip(Messages.toolTipShowHideEqualToggleButton));
        hideShowEqualItemsButton.selectedProperty()
                .addListener((a, o, n) -> {
                    hideEqualItems = n;

                    ArrayList arrayList = new ArrayList(tableEntryItems.values());
                    UI_EXECUTOR.execute(() -> snapshotTable.updateTable(arrayList));
                    if (isTreeTableViewEnabled) {
                        UI_EXECUTOR.execute(() -> snapshotTreeTable.updateTable(arrayList));
                    }
                });

        restoreButton.disableProperty().bind(snapshotRestorableProperty.not());

        saveAndRestoreService.addNodeChangeListener(this);

        DockPane.getActiveDockPane().addEventFilter(KeyEvent.ANY, event -> {
            if (event.isShortcutDown() && event.getCode() == KeyCode.F) {
                if (!filterTextField.isFocused()) {
                    filterTextField.requestFocus();
                }
            }
        });

        preserveSelectionCheckBox.selectedProperty().addListener((observableValue, aBoolean, isSelected) -> {
            if (isSelected) {
                boolean allSelected = tableEntryItems.values().stream()
                        .filter(item -> !item.selectedProperty().get())
                        .collect(Collectors.toList()).isEmpty();

                if (allSelected) {
                    tableEntryItems.values().stream()
                            .forEach(item -> item.selectedProperty().set(false));
                }
            }
        });

        String filterShortcutName = (new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN)).getDisplayText();
        filterTextField.setPromptText("* for all matching and , as or separator, & as and separator. Start with / for regex. All if empty. (" + filterShortcutName + ")");

        filterTextField.addEventHandler(KeyEvent.ANY, event -> {
            String filterText = filterTextField.getText().trim();

            if (filterText.isEmpty()) {
                List<TableEntry> arrayList = tableEntryItems.values().stream()
                        .map(item -> {
                            if (!preserveSelectionCheckBox.isSelected()) {
                                if (!item.readOnlyProperty().get()) {
                                    item.selectedProperty().set(true);
                                }
                            }
                            return item;
                        }).collect(Collectors.toList());

                UI_EXECUTOR.execute(() -> {
                    snapshotTable.updateTable(arrayList);
                    if (isTreeTableViewEnabled) {
                        snapshotTreeTable.updateTable(arrayList);
                    }
                });

                return;
            }

            List<String> filters = Arrays.asList(filterText.split(","));
            regexPatterns = filters.stream()
                    .map(item -> {
                        if (item.startsWith("/")) {
                            return Arrays.asList(Pattern.compile(item.substring(1, item.length() - 1).trim()));
                        } else {
                            return Arrays.asList(item.split("&")).stream()
                                    .map(andItem -> andItem.replaceAll("\\*", ".*"))
                                    .map(andItem -> Pattern.compile(andItem.trim()))
                                    .collect(Collectors.toList());
                        }
                    }).collect(Collectors.toList());

            List<TableEntry> filteredEntries = tableEntryItems.values().stream()
                    .filter(item -> {
                        boolean matchEither = false;
                        for (List<Pattern> andPatternList : regexPatterns) {
                            boolean matchAnd = true;
                            for (Pattern pattern : andPatternList) {
                                if (pattern.matcher(item.pvNameProperty().get()).find()) {
                                    matchAnd &= true;
                                } else {
                                    matchAnd &= false;
                                }
                            }

                            matchEither |= matchAnd;
                        }

                        if (!preserveSelectionCheckBox.isSelected()) {
                            item.selectedProperty().setValue(matchEither);
                        } else {
                            matchEither |= item.selectedProperty().get();
                        }

                        return matchEither;
                    }).collect(Collectors.toList());

            UI_EXECUTOR.execute(() -> {
                snapshotTable.updateTable(filteredEntries);
                if (isTreeTableViewEnabled) {
                    snapshotTreeTable.updateTable(filteredEntries);
                }
            });
        });

        dirtySnapshotEntries.addListener(new SetChangeListener<Integer>() {
            @Override
            public void onChanged(Change<? extends Integer> change) {
                if (dirtySnapshotEntries.size() == 0) {
                    snapshotSaveableProperty.set(false);

                    snapshotNameProperty.set(persistentSnapshotName);
                    snapshotTab.updateTabTitile(persistentSnapshotName, persistentGoldenState);
                } else {
                    snapshotSaveableProperty.set(true);

                    snapshotNameProperty.set(persistentSnapshotName + " " + Messages.snapshotModifiedText);
                    snapshotTab.updateTabTitile(persistentSnapshotName + " " + Messages.snapshotModifiedText, false);
                }
            }
        });
    }

    public void setSnapshotTab(SnapshotTab snapshotTab){
        this.snapshotTab = snapshotTab;
    }

    public void loadSnapshot(Node snapshot) {
        try {
            this.config = saveAndRestoreService.getParentNode(snapshot.getUniqueId());
            snapshotNameProperty.set(snapshot.getName());
            snapshotUniqueIdProperty.set(snapshot.getUniqueId());

            snapshotTab.updateTabTitile(snapshot.getName(), Boolean.parseBoolean(snapshot.getProperty("golden")));
            snapshotTab.setId(snapshot.getUniqueId());

            persistentSnapshotName = snapshot.getName();
            persistentGoldenState = Boolean.parseBoolean(snapshot.getProperty("golden"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadSnapshotInternal(snapshot);
    }

    public void addSnapshot(Node treeNode) {
        if (!treeNode.getNodeType().equals(NodeType.SNAPSHOT)) {
            return;
        }

        for (VSnapshot vSnapshot : snapshots) {
            if (treeNode.getUniqueId().equals(vSnapshot.getId())) {
                return;
            }
        }

        try {
            Node snapshot = saveAndRestoreService.getNode(treeNode.getUniqueId());
            List<SnapshotItem> snapshotItems = saveAndRestoreService.getSnapshotItems(snapshot.getUniqueId());
            VSnapshot vSnapshot =
                    new VSnapshot(snapshot, snapshotItemsToSnapshotEntries(snapshotItems));
            List<TableEntry> tableEntries = addSnapshot(vSnapshot);
            snapshotTable.updateTable(tableEntries, snapshots, false, false, false);
            if (isTreeTableViewEnabled) {
                snapshotTreeTable.updateTable(tableEntries, snapshots, false, false, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadSaveSet(Node node){

        SnapshotController.this.config = saveAndRestoreService.getNode(node.getUniqueId());
        try {
            List<ConfigPv> configPvs = saveAndRestoreService.getConfigPvs(config.getUniqueId());
            Node snapshot = Node.builder().name(Messages.unnamedSnapshot).nodeType(NodeType.SNAPSHOT).build();
            VSnapshot vSnapshot =
                    new VSnapshot(snapshot, saveSetToSnapshotEntries(configPvs));
            List<TableEntry> tableEntries = setSnapshotInternal(vSnapshot);
            UI_EXECUTOR.execute(() -> {
                snapshotTable.updateTable(tableEntries, snapshots, false, false, false);
                if (isTreeTableViewEnabled) {
                    snapshotTreeTable.updateTable(tableEntries, snapshots, false, false, false);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Node retrieveNodeFromMasar(String path) {
        try {
            final List<Node> nodes = saveAndRestoreService.getFromPath(path);

            final Optional<Node> folderNode = nodes.stream()
                    .filter(node -> node.getNodeType() == NodeType.FOLDER)
                    .findAny();

            return folderNode.orElse(null);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Creates a folder node, generates saveset node in created folder and saves snapshot node. All created nodes have the same name.
     *
     * @param saveSetName name of the saveset node
     * @param comment     saveset comment
     * @param pvList      list of PVs to save
     * @throws SaveAndRestoreException if node with saveset name already exists.
     *                                 if any of the nodes can not be created.
     */
    public void generateSaveSet(String saveSetName, String comment, List<String> pvList) throws SaveAndRestoreException {
        generateSaveSet(saveSetName, comment, pvList, null);
    }

    /**
     * Creates a folder node, generates saveset node in created folder and saves snapshot node. All created nodes have the same name.
     *
     * @param saveSetName name of the saveset node
     * @param comment     saveset comment
     * @param pvList      list of PVs to save
     * @param path        directory path where the saveset will be created, e.g. /topLevelFolder/folder
     * @throws SaveAndRestoreException if node with saveset name already exists.
     *                                 if any of the nodes can not be created.
     */
    public void generateSaveSet(String saveSetName, String comment, List<String> pvList, String path)
            throws SaveAndRestoreException {

        List<ConfigPv> saveSetEntries = new ArrayList<ConfigPv>();

        Node newFolderNode;

        try {
            if (path == null) {
                // Create folder for the new saveset in the root directory.
                Node root = saveAndRestoreService.getRootNode();
                Node newFolder = Node.builder()
                        .nodeType(NodeType.FOLDER)
                        .name(saveSetName)
                        .build();
                newFolderNode = saveAndRestoreService.createNode(root.getUniqueId(), newFolder);
            } else {
                newFolderNode = retrieveNodeFromMasar(path);
            }

            if (newFolderNode == null) {
                throw new SaveAndRestoreException(String.format("Could not find a node with the specified path. (%s)", path));
            }

            // This call will throw an exception if a saveset with the same name already exists.
            saveAndRestoreService.findNode(saveSetName, false);

            //CREATE CONFIGURATION
            Node newSateSetNode = Node.builder()
                    .nodeType(NodeType.CONFIGURATION)
                    .name(saveSetName)
                    .build();
            Node newTreeNode = saveAndRestoreService.createNode(newFolderNode.getUniqueId(), newSateSetNode);

            for (String pv : pvList) {
                ConfigPv configPv = ConfigPv.builder()
                        .pvName(pv)
                        .readOnly(false)
                        .build();
                saveSetEntries.add(configPv);
            }

            newTreeNode.putProperty(DESCRIPTION_PROPERTY, comment);
            newTreeNode = saveAndRestoreService.updateSaveSet(newTreeNode, saveSetEntries);
            List<ConfigPv> configPvs = saveAndRestoreService.getConfigPvs(newTreeNode.getUniqueId());
            Node config = saveAndRestoreService.getNode(newTreeNode.getUniqueId());
            List<SnapshotItem> snapshotItems = new ArrayList<>();
            List<PVReader<VType>> readers = new ArrayList<>();

            CountDownLatch countdownLatch = new CountDownLatch(pvList.size());
            try {
                for (ConfigPv pv : configPvs) {
                    PVReader<VType> readerPv = GPClient.readAndWrite(patchPvName(pv.getPvName()))
                            .addReadListener((event, p) -> {
                                if (event.getType().contains(PVEvent.Type.VALUE)) {
                                    SnapshotItem item = SnapshotItem.builder()
                                            .value(p.getValue())
                                            .readbackValue(null)
                                            .configPv(pv)
                                            .build();
                                    snapshotItems.add(item);

                                    countdownLatch.countDown();
                                }
                            })
                            .start();
                    readers.add(readerPv);
                }

                if (!countdownLatch.await(cagetTimeoutMs, MILLISECONDS)) {
                    LOGGER.log(Level.WARNING, "Some PVs could not be saved because they are disconnected");
                }
            } finally {
                for (PVReader<VType> reader : readers) {
                    reader.close();
                }
            }
            saveAndRestoreService.saveSnapshot(config, snapshotItems, saveSetName, comment);
        } catch (Exception ex) {
            throw new SaveAndRestoreException(ex);
        }
    }

    private void loadSnapshotInternal(Node snapshot) {

        UI_EXECUTOR.execute(() -> {
            try {
                List<SnapshotItem> snapshotItems = saveAndRestoreService.getSnapshotItems(snapshot.getUniqueId());

                snapshotCommentProperty.set(snapshot.getProperty("comment"));
                createdDateTextProperty.set(snapshot.getCreated().toString());
                createdByTextProperty.set(snapshot.getUserName());
                snapshotNameProperty.set(snapshot.getName());

                VSnapshot vSnapshot =
                        new VSnapshot(snapshot, snapshotItemsToSnapshotEntries(snapshotItems));
                List<TableEntry> tableEntries = loadSnapshotInternal(vSnapshot);

                snapshotTable.updateTable(tableEntries, snapshots, false, false, false);
                if (isTreeTableViewEnabled) {
                    snapshotTreeTable.updateTable(tableEntries, snapshots, false, false, false);
                }
                snapshotRestorableProperty.set(true);

                dirtySnapshotEntries.clear();
                vSnapshot.getEntries().stream().forEach(item -> {
                    item.getValueProperty().addListener((observableValue, vType, newVType) -> {
                        if (!Utilities.areVTypesIdentical(newVType, item.getStoredValue(), false)) {
                            dirtySnapshotEntries.add(item.getConfigPv().getId());
                        } else {
                            dirtySnapshotEntries.remove(item.getConfigPv().getId());
                        }
                    });
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Restores and validates snapshot. Restore does not start if more than one snapshot with the same name exists or
     * if snapshot does not exist.
     *
     * @param name snapshot name
     * @return true if restoring was successful, false if restoring failed
     * @throws SaveAndRestoreException if snapshot does not exists or if more than one snapshot with the same name exist
     */
    public boolean restoreSnapshotAndValidate(String name) throws SaveAndRestoreException {

        Node node = saveAndRestoreService.findNode(name, true).orElseThrow(() -> new SaveAndRestoreException("Snapshot does not exist"));
        List<SnapshotItem> snapshotItems;
        try {
            snapshotItems = saveAndRestoreService.getSnapshotItems(node.getUniqueId());
        } catch (Exception ex) {
            throw new SaveAndRestoreException(ex);
        }
        VSnapshot s = new VSnapshot(node, snapshotItemsToSnapshotEntries(snapshotItems));
        List<SnapshotEntry> entries = s.getEntries();

        Map<String, PV> myPvs = new HashMap<>();
        Map<String, Boolean> writeStatuses = new LinkedHashMap<>();

        // Create TableEntries and PVs
        for (int i = 0; i < entries.size(); i++) {
            SnapshotEntry entry = entries.get(i);
            TableEntry tableEntry = new TableEntry();
            String pvName = entry.getPVName();
            tableEntry.idProperty().setValue(i + 1);
            tableEntry.pvNameProperty().setValue(pvName);
            tableEntry.setConfigPv(entry.getConfigPv());
            tableEntry.selectedProperty().setValue(entry.isSelected());
            tableEntry.setSnapshotValue(entry.getValue(), 0);
            tableEntry.setStoredReadbackValue(entry.getReadbackValue(), 0);
            tableEntry.readbackNameProperty().set(entry.getReadbackName());
            tableEntry.readOnlyProperty().set(entry.isReadOnly());
            myPvs.put(pvName, new PV(tableEntry));
            writeStatuses.put(pvName, false);
        }

        CountDownLatch countDownLatch = new CountDownLatch(s.getEntries().size());
        for (SnapshotEntry entry : entries) {
            final PV pv = myPvs.get(entry.getPVName());
            if (entry.getValue() != null) {

                // Waiting for pv to connect before writing to them.
                await().atMost(pvConnectTimeoutMs, MILLISECONDS).until(() -> pv.pv.isWriteConnected());

                pv.pv.write(entry.getValue(), (event, pvWriter) -> {
                    if (event.getType().contains(PVEvent.Type.WRITE_SUCCEEDED)) {
                        LOGGER.info(countDownLatch + " Write OK, signalling latch");
                        writeStatuses.put(entry.getPVName(), true);
                        countDownLatch.countDown();
                    } else if (event.getType().contains(PVEvent.Type.WRITE_FAILED)) {
                        LOGGER.info(countDownLatch + "Write FAILED, signalling latch");
                        writeStatuses.put(entry.getPVName(), false);
                        countDownLatch.countDown();
                    }
                });
            }
        }
        try {
            boolean success = countDownLatch.await(caputTimeoutMs, MILLISECONDS);
            if (!success) throw new SaveAndRestoreException("Restoring snapshot timed out");
        } catch (InterruptedException e) {
            throw new SaveAndRestoreException(e);
        }

        return writeStatuses.values().stream().allMatch(x -> x);
    }

    @FXML
    public void restore(ActionEvent event) {
        new Thread(() -> {
            VSnapshot s = snapshots.get(0);
            CountDownLatch countDownLatch = new CountDownLatch(s.getEntries().size());
            s.getEntries().stream().forEach(e -> {
                pvs.get(e.getPVName()).setCountDownLatch(countDownLatch);
                pvs.get(e.getPVName()).writeStatus = PVEvent.Type.WRITE_FAILED;
            });
            try {
                List<SnapshotEntry> entries = s.getEntries();
                for (SnapshotEntry entry : entries) {
                    final TableEntry e = tableEntryItems.get(entry.getPVName());
                    if (e.selectedProperty().get() && !e.readOnlyProperty().get()) {
                        final PV pv = pvs.get(e.getConfigPv().getPvName());
                        if (entry.getValue() != null) {
                            pv.pv.write(Utilities.toRawValue(entry.getValue()));
                        }
                    }
                }

                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                List<String> messages = new ArrayList<>();
                for (SnapshotEntry entry : entries) {
                    PV pv = pvs.get(entry.getPVName());
                    if (pv.getWriteStatus().equals(PVEvent.Type.WRITE_FAILED)) {
                        StringBuilder sb = new StringBuilder(200);
                        sb.append(pv.pvName).append(':').append(" error writing PV");
                        messages.add(sb.toString());
                    }
                }

                if (messages.isEmpty()) {
                    LOGGER.log(Level.FINE, "Restored snapshot {0}", s.getSnapshot().get().getName());
                } else {
                    Collections.sort(messages);
                    StringBuilder sb = new StringBuilder(messages.size() * 200);
                    messages.forEach(e -> sb.append(e).append('\n'));
                    LOGGER.log(Level.WARNING,
                            "Not all PVs could be restored for {0}: {1}. The following errors occured:\n{2}",
                            new Object[] { s.getSnapshot().get().getName(), s.getSnapshot().get(), sb.toString() });
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle(Messages.restoreErrorTitle);
                        alert.setContentText(sb.toString());
                        alert.setHeaderText(Messages.restoreErrorContent);
                        alert.showAndWait();
                    });
                }
            } finally {
            }
        }).start();

    }

    @FXML
    public void takeSnapshot(ActionEvent event) {

        UI_EXECUTOR.execute(() -> {
            snapshotNameProperty.set(null);
            snapshotCommentProperty.set(null);
            createdByTextProperty.set(null);
            createdDateTextProperty.set(null);
            snapshotSaveableProperty.setValue(false);

            snapshotTab.setId(null);
            snapshotTab.updateTabTitile(Messages.unnamedSnapshot, false);
            dirtySnapshotEntries.clear();
        });
        try {
            List<SnapshotEntry> entries = new ArrayList<>(tableEntryItems.size());
            PV pv;
            String name, delta = null;
            String readbackName = null;
            VType value = null;
            VType readbackValue = null;
            for (TableEntry t : tableEntryItems.values()) {
                name = t.pvNameProperty().get();
                pv = pvs.get(t.pvNameProperty().get());

                // there is no issues with non atomic access to snapshotTreeTableEntryPvProxy.value or snapshotTreeTableEntryPvProxy.readbackValue because the PV is
                // suspended and the value could not change while suspended
                value = pv == null || pv.pvValue == null ? VDisconnectedData.INSTANCE : pv.pvValue;
                readbackName = readbacks.get(name);
                readbackValue = pv == null || pv.readbackValue == null ? VDisconnectedData.INSTANCE : pv.readbackValue;
                for (VSnapshot s : getAllSnapshots()) {
                    delta = s.getDelta(name);
                    if (delta != null) {
                        break;
                    }
                }

                entries.add(new SnapshotEntry(t.getConfigPv(), value, t.selectedProperty().get(), readbackName, readbackValue,
                        delta, t.readOnlyProperty().get()));
            }

            Node snapshot = Node.builder().name(Messages.unnamedSnapshot).nodeType(NodeType.SNAPSHOT).build();

            multiplierSpinner.getEditor().setText("1.0");
            VSnapshot taken = new VSnapshot(snapshot, entries);
            snapshots.clear();
            snapshots.add(taken);
            List<TableEntry> tableEntries = loadSnapshotInternal(taken);
            UI_EXECUTOR.execute(() -> {
                snapshotTable.updateTable(tableEntries, snapshots, showLiveReadbackProperty.get(), false, showDeltaPercentage);
                if (isTreeTableViewEnabled) {
                    snapshotTreeTable.updateTable(tableEntries, snapshots, showLiveReadbackProperty.get(), false, showDeltaPercentage);
                }
                snapshotSaveableProperty.setValue(true);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally{

        }
    }

    @FXML
    public void saveSnapshot(ActionEvent event) {
        if(snapshotSaveableProperty.get()){ // There is a new snapshot to save
            VSnapshot snapshot = snapshots.get(0);
            List<SnapshotEntry> snapshotEntries = snapshot.getEntries();
            List<SnapshotItem> snapshotItems = snapshotEntries
                    .stream()
                    .map(snapshotEntry -> SnapshotItem.builder().value(snapshotEntry.getValue()).configPv(snapshotEntry.getConfigPv()).readbackValue(snapshotEntry.getReadbackValue()).build())
                    .collect(Collectors.toList());
            try {
                Node savedSnapshot = saveAndRestoreService.saveSnapshot(config, snapshotItems, snapshotNameProperty.get(), snapshotCommentProperty.get());
                loadSnapshot(savedSnapshot);
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(Messages.errorActionFailed);
                alert.setContentText(e.getMessage());
                alert.setHeaderText(Messages.saveSnapshotErrorContent);
                alert.showAndWait();
            }
        }
        else{ // Only snapshot name and/or comment have changed
            try {
                Node snapshotNode = snapshots.get(0).getSnapshot().get();
                Map<String, String> properties = snapshotNode.getProperties();
                properties.put("comment", snapshotCommentProperty.get());
                snapshotNode.setProperties(properties);
                snapshotNode.setName(snapshotNameProperty.get());
                snapshotNode = saveAndRestoreService.updateNode(snapshotNode);
                loadSnapshot(snapshotNode);
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(Messages.errorActionFailed);
                alert.setContentText(e.getMessage());
                alert.setHeaderText(Messages.saveSnapshotErrorContent);
                alert.showAndWait();
            }
        }

    }

    public List<VSnapshot> getAllSnapshots() {
        synchronized (snapshots) {
            return new ArrayList<>(snapshots);
        }
    }

    private List<TableEntry> loadSnapshotInternal(VSnapshot snapshotData){
        dispose();
        return setSnapshotInternal(snapshotData);
    }

    private List<TableEntry> setSnapshotInternal(VSnapshot snapshotData) {
         List<SnapshotEntry> entries = snapshotData.getEntries();
        synchronized (snapshots) {
            snapshots.add(snapshotData);
        }
        UI_EXECUTOR.execute(() -> snapshotRestorableProperty.set(snapshotData.getSnapshot().isPresent()));
        String name;
        TableEntry e;
        SnapshotEntry entry;
        for (int i = 0; i < entries.size(); i++) {
            entry = entries.get(i);
            e = new TableEntry();
            name = entry.getPVName();
            e.idProperty().setValue(i + 1);
            e.pvNameProperty().setValue(name);
            e.setConfigPv(entry.getConfigPv());
            e.selectedProperty().setValue(entry.isSelected());
            e.setSnapshotValue(entry.getValue(), 0);
            e.setStoredReadbackValue(entry.getReadbackValue(), 0);
            tableEntryItems.put(name, e);
            readbacks.put(name, entry.getReadbackName());
            e.readbackNameProperty().set(entry.getReadbackName());
            e.readOnlyProperty().set(entry.isReadOnly());
            PV pv = pvs.get(name);
            if(pv != null){
                pv.setSnapshotTableEntry(e);
            }
        }
        connectPVs();
        UI_EXECUTOR.execute(() -> snapshotSaveableProperty.set(snapshotData.isSaveable()));
        return new ArrayList<>(tableEntryItems.values());
    }

    private List<TableEntry> addSnapshot(VSnapshot data) {
        int numberOfSnapshots = getNumberOfSnapshots();
        if (numberOfSnapshots == 0) {
            return setSnapshotInternal(data); // do not dispose of anything
        } else if (numberOfSnapshots == 1 && !getSnapshot(0).isSaveable() && !getSnapshot(0).isSaved()) {
            return setSnapshotInternal(data);
        } else {
            List<SnapshotEntry> entries = data.getEntries();
            String n;
            TableEntry e;
            List<TableEntry> withoutValue = new ArrayList<>(tableEntryItems.values());
            SnapshotEntry entry;
            for (int i = 0; i < entries.size(); i++) {
                entry = entries.get(i);
                n = entry.getPVName();
                e = tableEntryItems.get(n);
                if (e == null) {
                    e = new TableEntry();
                    e.idProperty().setValue(tableEntryItems.size() + i + 1);
                    e.pvNameProperty().setValue(n);
                    e.setConfigPv(entry.getConfigPv());
                    tableEntryItems.put(n, e);
                    readbacks.put(n, entry.getReadbackName());
                    e.readbackNameProperty().set(entry.getReadbackName());
                }
                e.setSnapshotValue(entry.getValue(), numberOfSnapshots);
                e.setStoredReadbackValue(entry.getReadbackValue(), numberOfSnapshots);
                e.readOnlyProperty().set(entry.isReadOnly());
                withoutValue.remove(e);
            }
            for (TableEntry te : withoutValue) {
                te.setSnapshotValue(VDisconnectedData.INSTANCE, numberOfSnapshots);
            }
            synchronized (snapshots) {
                snapshots.add(data);
            }
            connectPVs();
            UI_EXECUTOR.execute(() -> {
                if (!snapshotSaveableProperty.get()) {
                    snapshotSaveableProperty.set(data.isSaveable());
                }
                snapshotRestorableProperty.set(true);
            });

            return new ArrayList<>(tableEntryItems.values());
        }
    }

    private List<SnapshotEntry> snapshotItemsToSnapshotEntries(List<SnapshotItem> snapshotItems) {
        List<SnapshotEntry> snapshotEntries = new ArrayList<>();
        for (SnapshotItem snapshotItem : snapshotItems) {
            SnapshotEntry snapshotEntry =
                    new SnapshotEntry(snapshotItem, true);
            snapshotEntries.add(snapshotEntry);
        }

        return snapshotEntries;
    }

    private List<SnapshotEntry> saveSetToSnapshotEntries(List<ConfigPv> configPvs){
        List<SnapshotEntry> snapshotEntries = new ArrayList<>();
        for (ConfigPv configPv : configPvs) {
            SnapshotEntry snapshotEntry =
                    new SnapshotEntry(configPv, VNoData.INSTANCE, true, configPv.getReadbackPvName(), VNoData.INSTANCE, null, configPv.isReadOnly());
            snapshotEntries.add(snapshotEntry);
        }

        return snapshotEntries;
    }

    /**
     * Returns the snapshot stored under the given index.
     *
     * @param index the index of the snapshot to return
     * @return the snapshot under the given index (0 for the base snapshot and 1 or more for the compared ones)
     */
    public VSnapshot getSnapshot(int index) {
        synchronized (snapshots) {
            return snapshots.isEmpty() ? null
                    : index >= snapshots.size() ? snapshots.get(snapshots.size() - 1)
                    : index < 0 ? snapshots.get(0) : snapshots.get(index);
        }
    }

    /**
     * Returns the number of all snapshots currently visible in the viewer (including the base snapshot).
     *
     * @return the number of all snapshots
     */
    public int getNumberOfSnapshots() {
        synchronized (snapshots) {
            return snapshots.size();
        }
    }

    private void connectPVs() {
        try {
            tableEntryItems.values().forEach(e -> {
                PV pv = pvs.get(e.getConfigPv().getPvName());
                if (pv == null) {
                    pvs.put(e.getConfigPv().getPvName(), new PV(e));
                }
            });
        } finally {
        }
    }

    private void updateThreshold(double threshold) {
        snapshots.stream().forEach(snapshot -> {
            snapshot.getEntries().forEach(item -> {
                VType vtype = item.getValue();
                VType diffVType = null;

                double ratio = threshold/100;

                TableEntry tableEntry = tableEntryItems.get(item.getPVName());
                if (vtype instanceof VNumber) {
                    diffVType = SafeMultiply.multiply((VNumber) vtype, ratio);
                    VNumber vNumber = (VNumber) diffVType;
                    boolean isNegative = vNumber.getValue().doubleValue() < 0;

                    tableEntry.setThreshold(Optional.of(new Threshold<>(isNegative ? SafeMultiply.multiply(vNumber.getValue(), -1.0) : vNumber.getValue())));
                } else if (vtype instanceof VNumberArray) {
                    // TODO: Probably ignore waveform value? or compare each component? Leave it for now.
                    diffVType = SafeMultiply.multiply((VNumberArray) vtype, ratio);
                    VNumberArray vNumberArray = (VNumberArray) diffVType;
//                  tableEntry.setThreshold(Optional.of(new Threshold<>(vNumberArray.getData())));
                }
            });
        });
    }

    private void updateSnapshot(double multiplier) {
        snapshots.stream().forEach(snapshot -> {
            snapshot.getEntries().stream().filter(item -> !item.isReadOnly()).forEach(item -> {
                VType vtype = item.getStoredValue();
                VType newVType = null;

                if (vtype instanceof VNumber) {
                    newVType = SafeMultiply.multiply((VNumber) vtype, multiplier);
                } else if (vtype instanceof VNumberArray) {
                    newVType = SafeMultiply.multiply((VNumberArray) vtype, multiplier);
                } else {
                    return;
                }

                item.set(newVType, item.isSelected());

                TableEntry tableEntry = tableEntryItems.get(item.getPVName());
                tableEntry.snapshotValProperty().set(newVType);

                ObjectProperty<VTypePair> value = tableEntry.valueProperty();
                value.setValue(new VTypePair(value.get().base, newVType, value.get().threshold));
            });
        });
    }

    public void updateSnapshot(int snapshotIndex, TableEntry rowValue, VType newValue) {
        VSnapshot snapshot = snapshots.get(snapshotIndex);
        snapshot.getEntries().stream()
                .filter(item -> item.getPVName().equals(rowValue.getConfigPv().getPvName()))
                .filter(item -> !item.isReadOnly())
                .findFirst()
                .ifPresent(item -> {
                    VType vtype = item.getValue();
                    VType newVType = null;
                    if (newValue instanceof VNumber) {
                        newVType = VNumber.of(((VNumber) newValue).getValue(), Alarm.alarmOf(vtype), Time.timeOf(vtype), Display.displayOf(vtype));
                    } else if (newValue instanceof VNumberArray) {
                        newVType = VNumberArray.of(((VNumberArray) newValue).getData(), Alarm.alarmOf(vtype), Time.timeOf(vtype), Display.displayOf(vtype));
                    } else if (newValue instanceof VString) {
                        newVType = VString.of(((VString) newValue).getValue(), Alarm.alarmOf(vtype), Time.timeOf(vtype));
                    } else if (newValue instanceof VStringArray) {
                        newVType = VStringArray.of(((VStringArray) newValue).getData(), Alarm.alarmOf(vtype), Time.timeOf(vtype));
                    } else if (newValue instanceof VEnum) {
                        newVType = newValue;
                    }
                    item.set(newVType, rowValue.selectedProperty().get());
                    rowValue.snapshotValProperty().set(newVType);
                });
    }
        
    private String patchPvName(String pvName) {
        if (pvName == null || pvName.isEmpty()) {
            return null;
        } else if (pvName.startsWith("ca://") || pvName.startsWith("pva://")) {
            return pvName;
        } else {
            return defaultEpicsProtocol + "://" + pvName;
        }
    }

    private class PV {
        final String pvName;
        final String readbackPvName;
        CountDownLatch countDownLatch;
        org.epics.gpclient.PV<VType, Object> pv;
        PVReader<VType> pvReader;
        PVReader<VType> readbackReader;
        PVEvent.Type writeStatus = PVEvent.Type.WRITE_FAILED;
        volatile VType pvValue = VDisconnectedData.INSTANCE;
        volatile VType readbackValue = VDisconnectedData.INSTANCE;
        TableEntry snapshotTableEntry;
        boolean readOnly;

        PV(TableEntry snapshotTableEntry) {
            this.snapshotTableEntry = snapshotTableEntry;
            this.pvName = patchPvName(snapshotTableEntry.pvNameProperty().get());
            this.readbackPvName = patchPvName(snapshotTableEntry.readbackNameProperty().get());
            this.readOnly = snapshotTableEntry.readOnlyProperty().get();

            if(this.readOnly){
                pvReader = GPClient.read(pvName)
                        .addReadListener((event, p) -> {
                            this.pvValue = p.isConnected() ? p.getValue() : VDisconnectedData.INSTANCE;
                            this.snapshotTableEntry.setLiveValue(this.pvValue);
                        })
                        .connectionTimeout(Duration.ofMillis(3*TABLE_UPDATE_INTERVAL))
                        .maxRate(Duration.ofMillis(TABLE_UPDATE_INTERVAL))
                        .start();
            }
            else{
                PVConfiguration pvConfiguration = GPClient.readAndWrite(GPClient.channel(pvName));
                pv = pvConfiguration.addListener((event, p) -> {
                    if(event.getType().contains(PVEvent.Type.VALUE)){
                        this.pvValue = p.isConnected() ? (VType)p.getValue() : VDisconnectedData.INSTANCE;
                        this.snapshotTableEntry.setLiveValue(this.pvValue);
                    }
                    else if(event.getType().contains(PVEvent.Type.WRITE_SUCCEEDED)){
                        if(countDownLatch != null){
                            LOGGER.info(countDownLatch + " Write OK, signalling latch");
                            countDownLatch.countDown();
                        }
                        writeStatus = PVEvent.Type.WRITE_SUCCEEDED;
                    }
                    else if(event.getType().contains(PVEvent.Type.WRITE_FAILED)){
                        if(countDownLatch != null){
                            LOGGER.info(countDownLatch + "Write FAILED, signalling latch");
                            countDownLatch.countDown();
                        }
                        writeStatus = PVEvent.Type.WRITE_FAILED;
                    }
                }).maxRate(Duration.ofMillis(TABLE_UPDATE_INTERVAL))
                        .start();
            }

            if (readbackPvName != null && !readbackPvName.isEmpty()) {
                this.readbackReader = GPClient.read(this.readbackPvName)
                        .addReadListener((event, p) -> {
                            if (showLiveReadbackProperty.get()) {
                                this.readbackValue = p.isConnected() ? p.getValue() : VDisconnectedData.INSTANCE;
                                snapshotTableEntry.setReadbackValue(this.readbackValue);
                            }
                        }).maxRate(Duration.ofMillis(TABLE_UPDATE_INTERVAL)).start();
            }
        }

        public void setCountDownLatch(CountDownLatch countDownLatch){
            LOGGER.info(countDownLatch + " New CountDownLatch set");
            this.countDownLatch = countDownLatch;
        }

        public void setSnapshotTableEntry(TableEntry snapshotTableEntry){
            this.snapshotTableEntry = snapshotTableEntry;
        }

        public PVEvent.Type getWriteStatus(){
            return writeStatus;
        }

        void dispose() {
            if (pv != null && !pv.isClosed()) {
                pv.close();
            }
            if(pvReader != null && !pvReader.isClosed()){
                pvReader.close();
            }
            if (readbackReader != null && !readbackReader.isClosed()) {
                readbackReader.close();
            }
        }
    }

    public boolean handleSnapshotTabClosed(){
        if(snapshotSaveableProperty.get()){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(Messages.promptCloseSnapshotTabTitle);
            alert.setContentText(Messages.promptCloseSnapshotTabContent);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get().equals(ButtonType.CANCEL)) {
                return false;
            }
        }
        dispose();
        saveAndRestoreService.removeNodeChangeListener(this);
        return true;
    }

    /**
     * Dispose of all allocated resources, except PVs. If <code>closePVs</code> is true the pvs are disposed of,
     * otherwise they are only marked for disposal. It is expected that the caller to this method later checks the PVs
     * and disposes of those that have not been unmarked.
     *
     */
    private void dispose() {
        synchronized (snapshots) {
            pvs.values().forEach(e -> e.dispose());
            pvs.clear();
            tableEntryItems.clear();
            snapshots.clear();
        }
    }

    /**
     * Returns true if items with equal live and snapshot values are currently hidden or false is shown.
     *
     * @return true if equal items are hidden or false otherwise.
     */
    public boolean isHideEqualItems() {
        return hideEqualItems;
    }

    @Override
    public void nodeChanged(Node node) {
        if (node.getUniqueId().equals(snapshotUniqueIdProperty.get())) {
            snapshotNameProperty.set(node.getName());
            snapshotSaveableProperty.setValue(false);
            snapshotTab.updateTabTitile(node.getName(), Boolean.parseBoolean(node.getProperty("golden")));

            persistentSnapshotName = node.getName();
            persistentGoldenState = Boolean.parseBoolean(node.getProperty("golden"));
        }
    }
}