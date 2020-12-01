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
 */

package org.phoebus.applications.saveandrestore.service;

import org.phoebus.applications.saveandrestore.data.DataProvider;
import org.phoebus.applications.saveandrestore.data.NodeAddedListener;
import org.phoebus.applications.saveandrestore.data.NodeChangedListener;
import org.phoebus.applications.saveandrestore.model.ConfigPv;
import org.phoebus.applications.saveandrestore.model.Node;
import org.phoebus.applications.saveandrestore.model.NodeType;
import org.phoebus.applications.saveandrestore.model.SnapshotItem;
import org.phoebus.applications.saveandrestore.ui.model.VDisconnectedData;
import org.phoebus.applications.saveandrestore.ui.model.VNoData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.phoebus.applications.saveandrestore.model.ConfigPv;
import org.phoebus.applications.saveandrestore.model.Node;
import org.phoebus.applications.saveandrestore.model.SnapshotItem;
import org.phoebus.applications.saveandrestore.model.Tag;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Component
public class SaveAndRestoreService {

    @Autowired
    private ExecutorService executor;

    @Autowired
    private DataProvider dataProvider;

    private List<NodeChangedListener> nodeChangeListeners = Collections.synchronizedList(new ArrayList());
    private List<NodeAddedListener> nodeAddedListeners = Collections.synchronizedList(new ArrayList());

    private static final Logger LOG = LoggerFactory.getLogger(SaveAndRestoreService.class.getName());

    public Node getRootNode() {

        Future<Node> future = executor.submit(() -> dataProvider.getRootNode());

        try {
            return future.get();
        } catch (Exception ie) {
            LOG.error("Unable to retrieve root node, cause: " + ie.getMessage());
        }

        return null;
    }

    /**
     * Method that is searching for corresponding node.
     * 
     * This method has two searching modes, depending on how checkOnlyIfDuplicatedSnapshotName is set:
     *  - When true, this method throws an exception if two or more snapshots are found with the provided name.
     *  - When false, this method throws an exception if any node already has the provided name.
     *
     * @param name node name
     * @param checkOnlyIfDuplicatedSnapshotName  true if we are searching for snapshot, false if we are searching for any type of node
     * @return found snapshot node
     * @throws SaveAndRestoreException if we are searching for snapshot node and we find more than one with matching name
     *                                 if we are searching for any type of node and we find one node with matching name
     */
    public Optional<Node> findNode(String name, boolean checkOnlyIfDuplicatedSnapshotName) throws SaveAndRestoreException {
        Queue<Node> unvisitedNodes = new ArrayDeque<Node>();
        Node nodeFound = null;
        Node root = getRootNode();
        unvisitedNodes.add(root);
        while(!unvisitedNodes.isEmpty()){
            Node node = unvisitedNodes.remove();
            if(node.getName().equals(name)){
                if(!checkOnlyIfDuplicatedSnapshotName ){
                    throw new SaveAndRestoreException("Node with " +name + " already exists!");
                }
                if(node.getNodeType() == NodeType.SNAPSHOT){
                    if(nodeFound != null){
                        throw new SaveAndRestoreException("More than 1 snapshot has name " + name + "!");
                    }
                    nodeFound = node;
                }
            }
            unvisitedNodes.addAll(getChildNodes(node));
        }

        return Optional.ofNullable(nodeFound);
    }

    public Node getNode(String uniqueNodeId) {

        Future<Node> future = executor.submit(() -> dataProvider.getNode(uniqueNodeId));

        try {
            return future.get();
        } catch (Exception ie) {
            LOG.error("Unable to retrieve node " + uniqueNodeId + ", cause: " + ie.getMessage());
        }

        return null;
    }

    public List<Node> getChildNodes(Node node) {
        Future<List<Node>> future = executor.submit(() -> dataProvider.getChildNodes(node));

        try {
            return future.get();
        } catch (Exception ie) {
            LOG.error("Unable to retrieve child nodes of node " + node.getId() + ", cause: " + ie.getMessage());
        }

        return null;

    }

    public Node updateNode(Node nodeToUpdate) throws Exception {
        return updateNode(nodeToUpdate, false);
    }

    public Node updateNode(Node nodeToUpdate, boolean customTimeForMigration) throws Exception {
        Future<Node> future = executor.submit(() -> dataProvider.updateNode(nodeToUpdate, customTimeForMigration));

        Node node = future.get();

        notifyNodeChangeListeners(node);

        return node;
    }

    public Node createNode(String parentsUniqueId, Node newTreeNode) throws Exception {
        Future<Node> future = executor.submit(() -> dataProvider.createNode(parentsUniqueId, newTreeNode));

        notifyNodeAddedListeners(getNode(parentsUniqueId), newTreeNode);
        return future.get();
    }

    public boolean deleteNode(String uniqueNodeId) throws Exception {
        Future<Boolean> future = executor.submit(() -> {

            return dataProvider.deleteNode(uniqueNodeId);

        });

        return future.get();
    }

    public List<ConfigPv> getConfigPvs(String uniqueNodeId) throws Exception {

        Future<List<ConfigPv>> future = executor.submit(() -> dataProvider.getConfigPvs(uniqueNodeId));

        return future.get();
    }

    public Node updateSaveSet(Node configToUpdate, List<ConfigPv> configPvList) throws Exception {
        Future<Node> future = executor.submit(() -> dataProvider.updateSaveSet(configToUpdate, configPvList));

        Node updatedNode = future.get();
        notifyNodeChangeListeners(updatedNode);
        return updatedNode;
    }


    public String getServiceIdentifier() {
        return dataProvider.getServiceUrl();
    }

    public List<SnapshotItem> getSnapshotItems(String uniqueNodeId) throws Exception {

        Future<List<SnapshotItem>> future = executor.submit(() -> dataProvider.getSnapshotItems(uniqueNodeId));
        return future.get();
    }

    public Node getParentNode(String uniqueNodeId) throws Exception {
        Future<Node> future = executor.submit(() -> dataProvider.getSaveSetForSnapshot(uniqueNodeId));
        return future.get();
    }

    public List<Node> getFromPath(String path) throws Exception {
        Future<List<Node>> future = executor.submit(() -> dataProvider.getFromPath(path));
        return future.get();
    }

    public String getFullPath(String uniqueNodeId) throws Exception {
        Future<String> future = executor.submit(() -> dataProvider.getFullPath(uniqueNodeId));
        return future.get();
    }

    public Node tagSnapshotAsGolden(final Node node, boolean golden) throws Exception {
        Future<Node> future = executor.submit(() -> {
            node.getProperties().put("golden", golden ? "true" : "false");
            return dataProvider.updateNode(node);
        });

        Node updatedNode = future.get();
        notifyNodeChangeListeners(updatedNode);
        return updatedNode;
    }

    public Node addTagToSnapshot(final Node node, final Tag tag) throws Exception {
        Future<Node> future = executor.submit(() -> {
            node.addTag(tag);
            return dataProvider.updateNode(node);
        });

        Node updatedNode = future.get();
        notifyNodeChangeListeners(updatedNode);
        return updatedNode;
    }

    public Node removeTagFromSnapshot(final Node node, final Tag tag) throws Exception {
        Future<Node> future = executor.submit(() -> {
            node.removeTag(tag);

            return dataProvider.updateNode(node);
        });

        Node updatedNode = future.get();
        notifyNodeChangeListeners(updatedNode);
        return updatedNode;
    }

    public Node saveSnapshot(Node saveSetNode, List<SnapshotItem> snapshotItems, String snapshotName, String comment) throws Exception {
        // Some beautifying is needed to ensure successful serialization.
        List<SnapshotItem> beautifiedItems = snapshotItems.stream().map(snapshotItem -> {
            if (snapshotItem.getValue() instanceof VNoData || snapshotItem.getValue() instanceof VDisconnectedData) {
                snapshotItem.setValue(null);
            }
            if (snapshotItem.getReadbackValue() instanceof VNoData || snapshotItem.getReadbackValue() instanceof VDisconnectedData) {
                snapshotItem.setReadbackValue(null);
            }
            return snapshotItem;
        }).collect(Collectors.toList());
        Future<Node> future = executor.submit(() -> dataProvider.saveSnapshot(saveSetNode.getUniqueId(), beautifiedItems, snapshotName, comment));

        Node savedSnapshot = future.get();
        notifyNodeAddedListeners(saveSetNode, savedSnapshot);
        return savedSnapshot;
    }

    public List<Tag> getAllTags() throws Exception {
        Future<List<Tag>> future = executor.submit(() -> dataProvider.getAllTags());

        return future.get();
    }

    public void addNodeChangeListener(NodeChangedListener nodeChangeListener){
        nodeChangeListeners.add(nodeChangeListener);
    }

    public void removeNodeChangeListener(NodeChangedListener nodeChangeListener){
        nodeChangeListeners.remove(nodeChangeListener);
    }

    private void notifyNodeChangeListeners(Node changedNode){
        nodeChangeListeners.stream().forEach(listener -> listener.nodeChanged(changedNode));
    }

    public void addNodeAddedListener(NodeAddedListener nodeAddedListener){
        nodeAddedListeners.add(nodeAddedListener);
    }

    public void removeNodeAddedListener(NodeAddedListener nodeAddedListener){
        nodeAddedListeners.remove(nodeAddedListener);
    }

    private void notifyNodeAddedListeners(Node parentNode, Node newNode){
        nodeAddedListeners.stream().forEach(listener -> listener.nodeAdded(parentNode, newNode));
    }
}
