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
 * <a target="_blank" href="https://icons8.com/icons/set/tags">Tags icon</a> icon by <a target="_blank" href="https://icons8.com">Icons8</a>
 * <a target="_blank" href="https://icons8.com/icons/set/export">Export icon</a> icon by <a target="_blank" href="https://icons8.com">Icons8</a>
 * <a target="_blank" href="https://icons8.com/icons/set/import">Import icon</a> icon by <a target="_blank" href="https://icons8.com">Icons8</a>
 */

package org.phoebus.applications.saveandrestore.ui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.util.Pair;
import org.phoebus.applications.saveandrestore.ApplicationContextProvider;
import org.phoebus.applications.saveandrestore.DirectoryUtilities;
import org.phoebus.applications.saveandrestore.Messages;
import org.phoebus.applications.saveandrestore.data.DataProvider;
import org.phoebus.applications.saveandrestore.filehandler.csv.CSVExporter;
import org.phoebus.applications.saveandrestore.filehandler.csv.CSVImporter;
import org.phoebus.applications.saveandrestore.model.ConfigPv;
import org.phoebus.applications.saveandrestore.model.Node;
import org.phoebus.applications.saveandrestore.model.NodeType;
import org.phoebus.applications.saveandrestore.model.Tag;
import org.phoebus.applications.saveandrestore.service.SaveAndRestoreService;
import org.phoebus.applications.saveandrestore.ui.model.SnapshotEntry;
import org.phoebus.applications.saveandrestore.ui.model.VDisconnectedData;
import org.phoebus.applications.saveandrestore.ui.model.VNoData;
import org.phoebus.applications.saveandrestore.ui.model.VSnapshot;
import org.phoebus.applications.saveandrestore.ui.saveset.SaveSetController;
import org.phoebus.applications.saveandrestore.ui.saveset.SaveSetTab;
import org.phoebus.applications.saveandrestore.ui.snapshot.SnapshotNewTagDialog;
import org.phoebus.applications.saveandrestore.ui.snapshot.SnapshotTab;
import org.phoebus.applications.saveandrestore.ui.snapshot.tag.TagUtil;
import org.phoebus.applications.saveandrestore.ui.snapshot.tag.TagWidget;
import org.phoebus.framework.persistence.Memento;
import org.phoebus.framework.preferences.PreferencesReader;
import org.phoebus.ui.javafx.ImageCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.concurrent.Executor;
import java.util.concurrent.CountDownLatch;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class SaveAndRestoreController extends BaseSaveAndRestoreController {

    private static Executor UI_EXECUTOR = Platform::runLater;

    public static final Image folderIcon = ImageCache.getImage(BaseSaveAndRestoreController.class, "/icons/save-and-restore/folder.png");
    public static final Image saveSetIcon = ImageCache.getImage(BaseSaveAndRestoreController.class, "/icons/save-and-restore/saveset.png");
    public static final Image editSaveSetIcon = ImageCache.getImage(BaseSaveAndRestoreController.class, "/icons/save-and-restore/edit_saveset.png");
    public static final Image deleteIcon = ImageCache.getImage(BaseSaveAndRestoreController.class, "/icons/delete.png");
    public static final Image renameIcon = ImageCache.getImage(BaseSaveAndRestoreController.class, "/icons/rename_col.png");
    public static final Image snapshotIcon = ImageCache.getImage(BaseSaveAndRestoreController.class, "/icons/save-and-restore/snapshot.png");
    public static final Image snapshotGoldenIcon = ImageCache.getImage(BaseSaveAndRestoreController.class, "/icons/save-and-restore/snapshot-golden.png");
    public static final Image compareSnapshotIcon = ImageCache.getImage(BaseSaveAndRestoreController.class, "/icons/save-and-restore/compare.png");
    public static final Image snapshotTagsWithCommentIcon = ImageCache.getImage(BaseSaveAndRestoreController.class, "/icons/save-and-restore/snapshot-tags.png");
    public static final Image csvImportIcon = ImageCache.getImage(BaseSaveAndRestoreController.class, "/icons/csv_import.png");
    public static final Image csvExportIcon = ImageCache.getImage(BaseSaveAndRestoreController.class, "/icons/csv_export.png");

    @FXML
    private TreeView<Node> treeView;

    @FXML
    private TabPane tabPane;

    @FXML
    private Label jmasarServiceTitle;

    @FXML
    private Button reconnectButton;

    @FXML
    private Button tagSearchButton;

    @FXML
    private Label emptyTreeInstruction;

    @FXML
    private SplitPane splitPane;

    @Autowired
    private SaveAndRestoreService saveAndRestoreService;

    @Autowired
    private Preferences preferences;

    @Autowired
    private ObjectMapper objectMapper;

    private ContextMenu folderContextMenu;
    private ContextMenu saveSetContextMenu;
    private ContextMenu snapshotContextMenu;
    private ContextMenu rootFolderContextMenu;

    private SimpleStringProperty toggleGoldenMenuItemText = new SimpleStringProperty();
    private SimpleStringProperty jmasarServiceTitleProperty = new SimpleStringProperty();
    private BooleanProperty treeViewEmpty = new SimpleBooleanProperty(false);
    private SimpleObjectProperty<ImageView> toggleGoldenImageViewProperty = new SimpleObjectProperty<>();

    private ImageView snapshotImageView = new ImageView(snapshotIcon);
    private ImageView snapshotGoldenImageView = new ImageView(snapshotGoldenIcon);

    private static final String TREE_STATE = "tree_state";

    private static final Logger LOG = LoggerFactory.getLogger(SaveAndRestoreService.class.getName());

    private PreferencesReader preferencesReader;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        preferencesReader = (PreferencesReader) ApplicationContextProvider.getApplicationContext().getAutowireCapableBeanFactory().getBean("preferencesReader");

        reconnectButton.setGraphic(ImageCache.getImageView(BaseSaveAndRestoreController.class, "/icons/refresh.png"));
        reconnectButton.setTooltip(new Tooltip(Messages.buttonRefresh));

        ImageView tagSearchButtonImageView = ImageCache.getImageView(BaseSaveAndRestoreController.class, "/icons/tagSearch.png");
        tagSearchButtonImageView.setFitWidth(16);
        tagSearchButtonImageView.setFitHeight(16);

        tagSearchButton.setGraphic(tagSearchButtonImageView);
        tagSearchButton.setTooltip(new Tooltip(Messages.buttonTagSearch));

        emptyTreeInstruction.textProperty().setValue(Messages.labelCreateFolderEmptyTree);


        folderContextMenu = new ContextMenu();
        MenuItem newFolderMenuItem = new MenuItem(Messages.contextMenuNewFolder, new ImageView(folderIcon));
        newFolderMenuItem.setOnAction(ae -> {
            createNewFolder(treeView.getSelectionModel().getSelectedItem());
        });

        MenuItem renameFolderMenuItem = new MenuItem(Messages.contextMenuRename, new ImageView(renameIcon));
        renameFolderMenuItem.setOnAction(ae -> {
            renameNode(treeView.getSelectionModel().getSelectedItem());
        });

        MenuItem deleteFolderMenuItem = new MenuItem(Messages.contextMenuDelete, new ImageView(deleteIcon));
        deleteFolderMenuItem.setOnAction(ae -> {
            deleteNodes(treeView.getSelectionModel().getSelectedItems());
        });

        MenuItem newSaveSetMenuItem = new MenuItem(Messages.contextMenuNewSaveSet, new ImageView(saveSetIcon));
        newSaveSetMenuItem.setOnAction(ae -> {
            handleNewSaveSet(treeView.getSelectionModel().getSelectedItem());
        });

        ImageView importSaveSetIconImageView = new ImageView(csvImportIcon);
        importSaveSetIconImageView.setFitWidth(18);
        importSaveSetIconImageView.setFitHeight(18);

        MenuItem importSaveSetMenuItem = new MenuItem(Messages.importSaveSetLabel, importSaveSetIconImageView);
        importSaveSetMenuItem.setOnAction(ae -> {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle(Messages.importSaveSetLabel);
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Supported file formats (CSV, SNP)", "*.csv", "*.bms"));
                File file = fileChooser.showOpenDialog(splitPane.getScene().getWindow());
                if (file != null) {
                    CSVImporter.importFile(treeView.getSelectionModel().getSelectedItem().getValue(), file);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        folderContextMenu.getItems().addAll(newFolderMenuItem, renameFolderMenuItem, deleteFolderMenuItem, newSaveSetMenuItem);
        if (preferencesReader.getBoolean("enableCSVIO")) {
            folderContextMenu.getItems().add(importSaveSetMenuItem);
        }

        rootFolderContextMenu = new ContextMenu();
        MenuItem newRootFolderMenuItem = new MenuItem(Messages.contextMenuNewFolder, new ImageView(folderIcon));
        newRootFolderMenuItem.setOnAction(ae -> {
            createNewFolder(treeView.getSelectionModel().getSelectedItem());
        });
        rootFolderContextMenu.getItems().add(newRootFolderMenuItem);

        saveSetContextMenu = new ContextMenu();

        MenuItem deleteSaveSetMenuItem = new MenuItem(Messages.contextMenuDelete, new ImageView(deleteIcon));
        deleteSaveSetMenuItem.setOnAction(ae -> {
            deleteNodes(treeView.getSelectionModel().getSelectedItems());
        });

        MenuItem renameSaveSetMenuItem = new MenuItem(Messages.contextMenuRename, new ImageView(renameIcon));
        renameSaveSetMenuItem.setOnAction(ae -> {
            renameNode(treeView.getSelectionModel().getSelectedItem());
        });

        MenuItem openSaveSetMenuItem = new MenuItem(Messages.contextMenuOpen, new ImageView(saveSetIcon));
        openSaveSetMenuItem.setOnAction(ae -> {
            openSaveSetForSnapshot(treeView.getSelectionModel().getSelectedItem());
        });

        MenuItem editSaveSetMenuItem = new MenuItem(Messages.contextMenuEdit, new ImageView(editSaveSetIcon));
        editSaveSetMenuItem.setOnAction(ae -> {
            nodeDoubleClicked(treeView.getSelectionModel().getSelectedItem());
        });

        ImageView exportSaveSetIconImageView = new ImageView(csvExportIcon);
        exportSaveSetIconImageView.setFitWidth(18);
        exportSaveSetIconImageView.setFitHeight(18);

        MenuItem exportSaveSetMenuItem = new MenuItem(Messages.exportSaveSetLabel, exportSaveSetIconImageView);
        exportSaveSetMenuItem.setOnAction(ae -> {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle(Messages.exportSaveSetLabel);
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV (BMS compatible)", "*.csv"));
                fileChooser.setInitialFileName(treeView.getSelectionModel().getSelectedItem().getValue().getName());
                File file = fileChooser.showSaveDialog(splitPane.getScene().getWindow());
                if (file != null) {
                    if (!file.getAbsolutePath().toLowerCase().endsWith("csv")) {
                        file = new File(file.getAbsolutePath() + ".csv");
                    }

                    CSVExporter.export(treeView.getSelectionModel().getSelectedItem().getValue(), file.getAbsolutePath());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        ImageView importSnapshotIconImageView = new ImageView(csvImportIcon);
        importSnapshotIconImageView.setFitWidth(18);
        importSnapshotIconImageView.setFitHeight(18);

        MenuItem importSnapshotMenuItem = new MenuItem(Messages.importSnapshotLabel, importSnapshotIconImageView);
        importSnapshotMenuItem.setOnAction(ae -> {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle(Messages.importSnapshotLabel);
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Supported file formats (CSV, SNP)", "*.csv", "*.snp"));
                File file = fileChooser.showOpenDialog(splitPane.getScene().getWindow());
                if (file != null) {
                    CSVImporter.importFile(treeView.getSelectionModel().getSelectedItem().getValue(), file);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        saveSetContextMenu.getItems().addAll(openSaveSetMenuItem, editSaveSetMenuItem, renameSaveSetMenuItem, deleteSaveSetMenuItem);
        if (preferencesReader.getBoolean("enableCSVIO")) {
            saveSetContextMenu.getItems().addAll(exportSaveSetMenuItem, importSnapshotMenuItem);
        }

        snapshotContextMenu = new ContextMenu();

        MenuItem deleteSnapshotMenuItem = new MenuItem(Messages.contextMenuDelete, new ImageView(deleteIcon));
        deleteSnapshotMenuItem.setOnAction(ae -> {
            deleteNodes(treeView.getSelectionModel().getSelectedItems());
        });

        MenuItem renameSnapshotItem = new MenuItem(Messages.contextMenuRename, new ImageView(renameIcon));
        renameSnapshotItem.setOnAction(ae -> {
            renameNode(treeView.getSelectionModel().getSelectedItem());
        });

        MenuItem compareSaveSetMenuItem = new MenuItem(Messages.contextMenuCompareSnapshots, new ImageView(compareSnapshotIcon));
        compareSaveSetMenuItem.setOnAction(ae -> {
            comapreSnapshot(treeView.getSelectionModel().getSelectedItem());
        });

        MenuItem tagAsGolden = new MenuItem(Messages.contextMenuTagAsGolden, new ImageView(snapshotGoldenIcon));
        tagAsGolden.textProperty().bind(toggleGoldenMenuItemText);
        tagAsGolden.graphicProperty().bind(toggleGoldenImageViewProperty);
        tagAsGolden.setOnAction(ae -> {
            Node node = toggleGoldenProperty(treeView.getSelectionModel().getSelectedItem());
            treeView.getSelectionModel().getSelectedItem().setValue(node);
        });

        ImageView snapshotTagsWithCommentIconImage = new ImageView(snapshotTagsWithCommentIcon);
        snapshotTagsWithCommentIconImage.setFitHeight(22);
        snapshotTagsWithCommentIconImage.setFitWidth(22);
        Menu tagWithComment = new Menu(Messages.contextMenuTagsWithComment, snapshotTagsWithCommentIconImage);
        tagWithComment.setOnShowing(event -> {
            Node node = treeView.getSelectionModel().getSelectedItem().getValue();

            ObservableList<MenuItem> tagList = tagWithComment.getItems();

            while (tagList.size() > 2) {
                tagList.remove(tagList.size() - 1);
            }

            if (node.getTags().isEmpty()) {
                CustomMenuItem noTags = TagWidget.NoTagMenuItem();
                noTags.setDisable(true);
                tagList.add(noTags);
            } else {
                node.getTags().sort(new TagComparator());
                node.getTags().stream().forEach(tag -> {
                    CustomMenuItem tagItem = TagWidget.TagWithCommentMenuItem(tag);

                    tagItem.setOnAction(actionEvent -> {
                        Alert confirmation = new Alert(AlertType.CONFIRMATION);
                        confirmation.setTitle(Messages.tagRemoveConfirmationTitle);
                        String locationString = DirectoryUtilities.CreateLocationString(node, true);
                        javafx.scene.Node headerNode = TagUtil.CreateRemoveHeader(locationString, node.getName(), tag);
                        confirmation.getDialogPane().setHeader(headerNode);
                        confirmation.setContentText(Messages.tagRemoveConfirmationContent);

                        Optional<ButtonType> result = confirmation.showAndWait();
                        result.ifPresent(buttonType -> {
                            if (buttonType == ButtonType.OK) {
                                try {
                                    saveAndRestoreService.removeTagFromSnapshot(node, tag);
                                } catch (Exception e) {

                                }
                            }
                        });
                    });
                    tagList.add(tagItem);
                });
            }
        });

        CustomMenuItem addTagWithCommentMenuItem = TagWidget.AddTagWithCommentMenuItem();
        addTagWithCommentMenuItem.setOnAction(action -> {
            Node selectedNode = treeView.getSelectionModel().getSelectedItem().getValue();
            SnapshotNewTagDialog snapshotNewTagDialog = new SnapshotNewTagDialog(selectedNode.getTags());
            snapshotNewTagDialog.initModality(Modality.APPLICATION_MODAL);

            String locationString = DirectoryUtilities.CreateLocationString(selectedNode, true);
            snapshotNewTagDialog.getDialogPane().setHeader(TagUtil.CreateAddHeader(locationString, selectedNode.getName()));

            Optional<Pair<String, String>> result = snapshotNewTagDialog.showAndWait();
            result.ifPresent(items -> {
                Tag aNewTag = Tag.builder()
                        .snapshotId(selectedNode.getUniqueId())
                        .name(items.getKey())
                        .comment(items.getValue())
                        .userName(System.getProperty("user.name"))
                        .build();

                try {
                    saveAndRestoreService.addTagToSnapshot(selectedNode, aNewTag);
                } catch (Exception e) {

                }
            });
        });

        tagWithComment.getItems().addAll(addTagWithCommentMenuItem, new SeparatorMenuItem());

        ImageView exportSnapshotIconImageView = new ImageView(csvExportIcon);
        exportSnapshotIconImageView.setFitWidth(18);
        exportSnapshotIconImageView.setFitHeight(18);

        MenuItem exportSnapshotMenuItem = new MenuItem(Messages.exportSnapshotLabel, exportSnapshotIconImageView);
        exportSnapshotMenuItem.setOnAction(ae -> {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle(Messages.exportSnapshotLabel);
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV (SNP compatible)", "*.csv"));
                fileChooser.setInitialFileName(treeView.getSelectionModel().getSelectedItem().getValue().getName());
                File file = fileChooser.showSaveDialog(splitPane.getScene().getWindow());
                if (file != null) {
                    if (!file.getAbsolutePath().toLowerCase().endsWith("csv")) {
                        file = new File(file.getAbsolutePath() + ".csv");
                    }

                    CSVExporter.export(treeView.getSelectionModel().getSelectedItem().getValue(), file.getAbsolutePath());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        snapshotContextMenu.getItems().addAll(renameSnapshotItem, deleteSnapshotMenuItem, compareSaveSetMenuItem, tagAsGolden, tagWithComment);
        if (preferencesReader.getBoolean("enableCSVIO")) {
            snapshotContextMenu.getItems().add(exportSnapshotMenuItem);
        }

        treeView.setEditable(true);

        treeView.setOnMouseClicked(me -> {
            TreeItem<Node> item = treeView.getSelectionModel().getSelectedItem();
            if(item == null){
                return;
            }
            if (item.getValue().getNodeType().equals(NodeType.SNAPSHOT)) {
                toggleGoldenMenuItemText.set(Boolean.parseBoolean(item.getValue().getProperty("golden")) ? Messages.contextMenuRemoveGoldenTag : Messages.contextMenuTagAsGolden);
                toggleGoldenImageViewProperty.set(Boolean.parseBoolean(item.getValue().getProperty("golden")) ? snapshotImageView : snapshotGoldenImageView);
            }
            if (me.getClickCount() == 2) {
                nodeDoubleClicked(treeView.getSelectionModel().getSelectedItem());
            }
        });

        treeView.setShowRoot(false);
        treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        jmasarServiceTitle.textProperty().bind(jmasarServiceTitleProperty);

        ContextMenu treeViewContextMenu = new ContextMenu();
        MenuItem newTopLevelFolderMenuItem = new MenuItem(Messages.contextMenuNewTopLevelFolder, new ImageView(folderIcon));
        newTopLevelFolderMenuItem.setOnAction(ae -> {
            createNewFolder(treeView.getRoot());
        });

        treeViewContextMenu.getItems().addAll(newTopLevelFolderMenuItem);

        treeView.setContextMenu(treeViewContextMenu);

        emptyTreeInstruction.visibleProperty().bindBidirectional(treeViewEmpty);

        saveAndRestoreService.addNodeChangeListener(this);
        saveAndRestoreService.addNodeAddedListener(this);

        treeView.setCellFactory(p -> new BrowserTreeCell(folderContextMenu,
                saveSetContextMenu, snapshotContextMenu, rootFolderContextMenu));

        loadTreeData();
    }

    /**
     * Loads the data for the tree root as provided (persisted) by the current
     * {@link DataProvider}.
     */
    @SuppressWarnings({"rawtypes","unchecked"})
    @FXML
    public void loadTreeData() {

        Task<TreeItem<Node>> loadRootNode = new Task<>() {
            @Override
            protected TreeItem<Node> call() throws Exception {
                Node rootNode = saveAndRestoreService.getRootNode();
                TreeItem<Node> rootItem = createNode(rootNode);
                List<String> savedTreeViewStructure = getSavedTreeStructure();
                // Check if there is a save tree structure. Also check that the first node id (=tree root)
                // has the same unique id as the actual root node retrieved from the remote service. This check
                // is needed to handle the case when the client connects to a different save-and-restore service.
                if(savedTreeViewStructure != null && savedTreeViewStructure.get(0).equals(rootNode.getUniqueId())){
                    HashMap<String, List<TreeItem<Node>>> childNodesMap = new HashMap<>();
                    savedTreeViewStructure.stream().forEach(s -> {
                        List<Node> childNodes = saveAndRestoreService.getChildNodes(Node.builder().uniqueId(s).build());
                        if(childNodes != null) { // This may be the case if the tree structure was modified outside of the UI
                            List<TreeItem<Node>> childItems = childNodes.stream().map(n -> createNode(n)).collect(Collectors.toList());
                            childItems.sort(new TreeNodeComparator());
                            childNodesMap.put(s, childItems);
                        }
                    });
                    setChildItems(childNodesMap, rootItem);
                }
                else{
                    List<Node> childNodes = saveAndRestoreService.getChildNodes(rootItem.getValue());
                    List<TreeItem<Node>> childItems = childNodes.stream().map(n -> createNode(n)).collect(Collectors.toList());
                    treeViewEmpty.setValue(childItems.isEmpty());
                    childItems.sort(new TreeNodeComparator());
                    rootItem.getChildren().addAll(childItems);
                }

                return rootItem;
            }

            @Override
            public void succeeded(){
                TreeItem<Node> rootItem = getValue();
                jmasarServiceTitleProperty.set(saveAndRestoreService.getServiceIdentifier());

                treeView.setRoot(rootItem);
                restoreTreeState();
            }

            @Override
            public void failed(){
                jmasarServiceTitleProperty.set(MessageFormat.format(Messages.jmasarServiceUnavailable, saveAndRestoreService.getServiceIdentifier()));
            }
        };

        new Thread(loadRootNode).start();
    }

    private List<String> getSavedTreeStructure(){
        String savedTreeState = preferences.get(TREE_STATE, null);
        if(savedTreeState == null){
            return null;
        }
        try {
            return objectMapper.readValue(savedTreeState, new TypeReference<List<String>>() {});
        } catch (IOException e) {
            LOG.error("Unable to obtain tree node data from service", e);
            return null;
        }
    }


    private void expandTreeNode(TreeItem<Node> targetItem) {

        targetItem.getChildren().clear();

        List<Node> childNodes = saveAndRestoreService.getChildNodes(targetItem.getValue());;
        Collections.sort(childNodes);
        targetItem.getChildren().addAll(childNodes.stream().map(n -> createNode(n)).collect(Collectors.toList()));
        targetItem.getChildren().sort(new TreeNodeComparator());
    }

    /**
     * Deletion of tree nodes when multiple nodes are selected is allowed only if all of the
     * selected nodes have the same parent node. This method checks the parent node(s) of
     * the selected nodes accordingly.
     * @param selectedItems The selected tree nodes.
     * @return <code>true</code> if all selected nodes have the same parent node, <code>false</code> otherwise.
     */
    private boolean isDeletionPossible(ObservableList<TreeItem<Node>> selectedItems){
        Node parentNode = selectedItems.get(0).getParent().getValue();
        for(TreeItem<Node> treeItem : selectedItems){
            if(!treeItem.getParent().getValue().getUniqueId().equals(parentNode.getUniqueId())){
                return false;
            }
        }
        return true;
    }

    private void comapreSnapshot(TreeItem<Node> treeItem) {

        try {
            SnapshotTab currentTab = (SnapshotTab)tabPane.getSelectionModel().getSelectedItem();
            if(currentTab == null){
                return;
            }
            currentTab.addSnapshot(treeItem.getValue());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private Node toggleGoldenProperty(TreeItem<Node> treeItem) {

        try {
            return saveAndRestoreService.tagSnapshotAsGolden(treeItem.getValue(),
                    !Boolean.parseBoolean(treeItem.getValue().getProperty("golden")));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void deleteNodes(ObservableList<TreeItem<Node>> selectedItems){
        if(!isDeletionPossible(selectedItems)){
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(Messages.promptDeleteSelectedTitle);
            alert.setHeaderText(Messages.deletionNotAllowedHeader);
            alert.setContentText(Messages.deletionNotAllowed);
            alert.showAndWait();
            return;
        }

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(Messages.promptDeleteSelectedTitle);
        alert.setHeaderText(Messages.promptDeleteSelectedHeader);
        alert.setContentText(Messages.promptDeleteSelectedContent);
        alert.getDialogPane().addEventFilter(KeyEvent.ANY, event -> {
            if (event.getCode().equals(KeyCode.ENTER) || event.getCode().equals(KeyCode.SPACE)) {
                event.consume();
            }
        });
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get().equals(ButtonType.OK)) {
            selectedItems.stream().forEach(treeItem -> deleteTreeItem(treeItem));
        }
    }

    /**
     * Deletes the {@link Node} associated with the {@link TreeItem}, and
     * removes the {@link TreeItem} from the tree view. If {@link Node} is associated
     * with an open tab, that tab is cleaned up and closed.
     * @param treeItem
     */
    private void deleteTreeItem(TreeItem<Node> treeItem){
        TreeItem<Node> parent = treeItem.getParent();
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                saveAndRestoreService.deleteNode(treeItem.getValue().getUniqueId());
                return null;
            }

            @Override
            public void succeeded(){
                UI_EXECUTOR.execute(() -> {
                    parent.getChildren().remove(treeItem);
                    List<Tab> tabsToRemove = new ArrayList<>();
                    for(Tab tab : tabPane.getTabs()) {
                        if(tab.getId().equals(treeItem.getValue().getUniqueId())) {
                           tabsToRemove.add(tab);
                           tab.getOnCloseRequest().handle(null);
                        }
                    }
                    tabPane.getTabs().removeAll(tabsToRemove);
                    treeView.getSelectionModel().select(null);
                });
            }
        };

        new Thread(task).start();
    }

    private void openSaveSetForSnapshot(TreeItem<Node> treeItem) {
        SnapshotTab tab = new SnapshotTab(treeItem.getValue(), saveAndRestoreService);
        tab.loadSaveSet(treeItem.getValue());

        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    private void createNewFolder(TreeItem<Node> parentTreeItem) {

        List<String> existingFolderNames =
                parentTreeItem.getChildren().stream()
                        .filter(item -> item.getValue().getNodeType().equals(NodeType.FOLDER))
                        .map(item -> item.getValue().getName())
                        .collect(Collectors.toList());

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(Messages.contextMenuNewFolder);
        dialog.setContentText(Messages.promptNewFolder);
        dialog.setHeaderText(null);
        dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);

        dialog.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            String value = newValue.trim();
            dialog.getDialogPane().lookupButton(ButtonType.OK)
                    .setDisable(existingFolderNames.contains(value) || value.isEmpty());
        });

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            Node newFolderNode = Node.builder()
                    .name(result.get())
                    .build();
            try {
                Node newTreeNode = saveAndRestoreService
                        .createNode(parentTreeItem.getValue().getUniqueId(), newFolderNode);
            } catch (Exception e) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Action failed");
                alert.setHeaderText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void nodeDoubleClicked(TreeItem<Node> node) {

        // Disallow opening a tab multiple times for the same save set.
        for(Tab tab : tabPane.getTabs()) {
            if(tab.getId() != null && tab.getId().equals(node.getValue().getUniqueId())) {
                return;
            }
        }

        Tab tab;

        switch (node.getValue().getNodeType()) {
            case CONFIGURATION:
                tab = new SaveSetTab(node.getValue(), saveAndRestoreService);
                break;
            case SNAPSHOT:
                tab = new SnapshotTab(treeView.getSelectionModel().getSelectedItem().getValue(), saveAndRestoreService);
                ((SnapshotTab) tab).loadSnapshot(treeView.getSelectionModel().getSelectedItem().getValue());
                break;
            case FOLDER:
            default:
                return;
        }

        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    private List<SnapshotEntry> saveSetToSnapshotEntries(List<ConfigPv> configPvs) {
        List<SnapshotEntry> snapshotEntries = new ArrayList<>();
        for (ConfigPv configPv : configPvs) {
            SnapshotEntry snapshotEntry =
                    new SnapshotEntry(configPv, VNoData.INSTANCE, true, configPv.getReadbackPvName(), VNoData.INSTANCE, null, configPv.isReadOnly());
            snapshotEntries.add(snapshotEntry);
        }

        return snapshotEntries;
    }

    private void handleNewSaveSet(TreeItem<Node> parentTreeItem) {

      List<String> existingFolderNames =
                parentTreeItem.getChildren().stream()
                        .filter(item -> item.getValue().getNodeType().equals(NodeType.CONFIGURATION))
                        .map(item -> item.getValue().getName())
                        .collect(Collectors.toList());

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(Messages.promptNewSaveSetTitle);
        dialog.setContentText(Messages.promptNewSaveSetContent);
        dialog.setHeaderText(null);
        dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);

        dialog.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            String value = newValue.trim();
            dialog.getDialogPane().lookupButton(ButtonType.OK)
                    .setDisable(existingFolderNames.contains(value) || value.isEmpty());
        });

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            Node newSateSetNode = Node.builder()
                    .nodeType(NodeType.CONFIGURATION)
                    .name(result.get())
                    .build();
            try {
                Node newTreeNode = saveAndRestoreService
                        .createNode(treeView.getSelectionModel().getSelectedItem().getValue().getUniqueId(), newSateSetNode);
                TreeItem<Node> newSaveSetNode = createNode(newTreeNode);
                nodeDoubleClicked(newSaveSetNode);
                treeView.getSelectionModel().clearSelection();
                treeView.getSelectionModel().select(treeView.getRow(newSaveSetNode));
            } catch (Exception e) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Action failed");
                alert.setHeaderText(e.getMessage());
                alert.showAndWait();
            }
        }
    }


    /**
     * Renames a node through the service and its underlying data provider.
     * If there is a problem in the call to the remote JMasar service,
     * the user is shown a suitable error dialog and the name of the node is restored.
     * @param node The node being renamed
     */
    private void renameNode(TreeItem<Node> node){

        List<String> existingSiblingNodes =
                node.getParent().getChildren().stream()
                        .filter(item -> item.getValue().getNodeType().equals(node.getValue().getNodeType()))
                        .map(item -> item.getValue().getName())
                        .collect(Collectors.toList());

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(Messages.promptRenameNodeTitle);
        dialog.setContentText(Messages.promptRenameNodeContent);
        dialog.setHeaderText(null);
        dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
        dialog.getEditor().textProperty().setValue(node.getValue().getName());


        dialog.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            String value = newValue.trim();
            dialog.getDialogPane().lookupButton(ButtonType.OK)
                    .setDisable(existingSiblingNodes.contains(value) || value.isEmpty());
        });

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            node.getValue().setName(result.get());
            try {
                saveAndRestoreService.updateNode(node.getValue());
            } catch (Exception e) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle(Messages.errorActionFailed);
                alert.setHeaderText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private TreeItem<Node> createNode(final Node node){
        return new TreeItem<>(node){
            @Override
            public boolean isLeaf(){
                return node.getNodeType().equals(NodeType.SNAPSHOT);
            }
        };
    }

    @Override
    public void nodeChanged(Node node){
        // Find the node that has changed
        TreeItem<Node> nodeSubjectToUpdate = recursiveSearch(node.getUniqueId(), treeView.getRoot());
        if(nodeSubjectToUpdate == null){
            // TODO: log this?
            return;
        }
        nodeSubjectToUpdate.setValue(node);
        nodeSubjectToUpdate.getParent().getChildren().sort(new TreeNodeComparator());
        treeView.getSelectionModel().clearSelection();
        treeView.getSelectionModel().select(nodeSubjectToUpdate);
    }

    @Override
    public void nodeAdded(Node parentNode, Node newNode){
        // Find the parent to which the new node is to be added
        TreeItem<Node> parentTreeItem = recursiveSearch(parentNode.getUniqueId(), treeView.getRoot());
        if(parentTreeItem == null){
            // TODO: log this?
            return;
        }
        parentTreeItem.getChildren().add(createNode(newNode));
        parentTreeItem.getChildren().sort(new TreeNodeComparator());
        parentTreeItem.expandedProperty().setValue(true);
    }

    private TreeItem<Node> recursiveSearch(String nodeIdToLocate, TreeItem<Node> node){
        if (node.getValue().getUniqueId().equals(nodeIdToLocate))
            return node;
        List<TreeItem<Node>> childNodes = node.getChildren();
        TreeItem<Node> result = null;
        for (int i = 0; result == null && i < childNodes.size(); i++) {
            result = recursiveSearch(nodeIdToLocate, childNodes.get(i));
        }
        return result;
    }

    @Override
    public void save(final Memento memento){
        saveTreeState();
        memento.setNumber("POS", splitPane.getDividers().get(0).getPosition());
    }

    @Override
    public void restore(final Memento memento){
        memento.getNumber("POS").ifPresent(pos -> splitPane.setDividerPositions(pos.doubleValue()));
    }

    @Override
    public void locateNode(Stack<Node> nodeStack) {
        TreeItem<Node> parentTreeItem = treeView.getRoot();

        while (nodeStack.size() > 0) {
            Node currentNode = nodeStack.pop();
            TreeItem<Node> currentTreeItem = recursiveSearch(currentNode.getUniqueId(), parentTreeItem);
            currentTreeItem.setExpanded(true);
            parentTreeItem = currentTreeItem;
        }

        treeView.getSelectionModel().clearSelection();
        treeView.getSelectionModel().select(parentTreeItem);
    }

    private void saveTreeState(){
        if(treeView.getRoot() == null){
            return;
        }
        List<String> expandedNodes = new ArrayList<>();
        findExpandedNodes(expandedNodes, treeView.getRoot());
        if(expandedNodes.isEmpty()){
            return;
        }
        try {
            preferences.put(TREE_STATE, objectMapper.writeValueAsString(expandedNodes));
        } catch (JsonProcessingException e){
            e.printStackTrace();
        }
    }

    private void findExpandedNodes(List<String> expandedNodes, TreeItem<Node> treeItem){
        if(treeItem.expandedProperty().get() && !treeItem.getChildren().isEmpty()){
            expandedNodes.add(treeItem.getValue().getUniqueId());
            treeItem.getChildren().stream().forEach(ti -> findExpandedNodes(expandedNodes, ti));
        }
    }

    /**
     * Loops through the the tree view model and expands all nodes that have a non-empty children
     * list. The tree view at this point has already been updated with data from the backend.
     */
    private void restoreTreeState(){
        UI_EXECUTOR.execute(() -> {
            expandNodes(treeView.getRoot());

            // Must be added here, after nodes have been expanded. Adding the event handler
            // before expansion of nodes will break the expected behavior when restoring the tree state.
            treeView.getRoot().addEventHandler(TreeItem.branchExpandedEvent(), e -> {
                expandTreeNode(((TreeItem.TreeModificationEvent)e).getTreeItem());
            });

            // This is needed in the rare event that all top level folders have been deleted.
            treeView.getRoot().addEventHandler(TreeItem.treeNotificationEvent(), e -> {
                treeViewEmpty.set(treeView.getRoot().getChildren().isEmpty());
            });
        });
    }

    private void setChildItems(HashMap<String, List<TreeItem<Node>>> allItems, TreeItem<Node> parentItem){
        if(allItems.containsKey(parentItem.getValue().getUniqueId())){
            List<TreeItem<Node>> childItems = allItems.get(parentItem.getValue().getUniqueId());
            parentItem.getChildren().setAll(childItems);
            parentItem.getChildren().sort(new TreeNodeComparator());
            childItems.stream().forEach(ci -> setChildItems(allItems, ci));
        }
    }

    private void expandNodes(TreeItem<Node> parentNode){
        if(!parentNode.getChildren().isEmpty()){
            parentNode.setExpanded(true);
            for(TreeItem<Node> childNode : parentNode.getChildren()){
                expandNodes(childNode);
            }
        }
    }

    private class TreeNodeComparator implements Comparator<TreeItem<Node>>{
        @Override
        public int compare(TreeItem<Node> t1, TreeItem<Node> t2){
            if (t1.getValue().getNodeType().equals(NodeType.SNAPSHOT) && t2.getValue().getNodeType().equals(NodeType.SNAPSHOT)) {
                return (preferencesReader.getBoolean("sortSnapshotsTimeReversed") ? -1 : 1)*t1.getValue().getCreated().compareTo(t2.getValue().getCreated());
            }

            return t1.getValue().compareTo(t2.getValue());
        }
    }

    private class TagComparator implements Comparator<Tag> {
        @Override
        public int compare(Tag tag1, Tag tag2){
            return -tag1.getCreated().compareTo(tag2.getCreated());
        }
    }
}
