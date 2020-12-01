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

package org.phoebus.applications.saveandrestore.data.providers.jmasar;

import org.phoebus.applications.saveandrestore.data.DataProvider;
import org.phoebus.applications.saveandrestore.data.DataProviderException;
import org.phoebus.applications.saveandrestore.model.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.phoebus.applications.saveandrestore.model.ConfigPv;
import org.phoebus.applications.saveandrestore.model.Node;
import org.phoebus.applications.saveandrestore.model.SnapshotItem;

import java.util.List;

public class JMasarDataProvider implements DataProvider {

	@Autowired
	private JMasarJerseyClient jmasarClient;

	@Override
	public Node getRootNode() {
		return jmasarClient.getRoot();
	}

	@Override
	public Node getNode(String uniqueNodeId){
		return jmasarClient.getNode(uniqueNodeId);
	}

	@Override
	public List<Node> getChildNodes(Node node) {
		return jmasarClient.getChildNodes(node);
	}

	@Override
	public Node updateNode(Node nodeToUpdate) {
		return updateNode(nodeToUpdate, false);
	}

	@Override
	public Node updateNode(Node nodeToUpdate, boolean customTimeForMigration) {
		try {
			return jmasarClient.updateNode(nodeToUpdate, customTimeForMigration);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public Node createNode(String parentsUniqueId, Node node) {
		return jmasarClient.createNewNode(parentsUniqueId, node);
	}
	
	@Override
	public String getServiceUrl() {
		return jmasarClient.getServiceUrl();
	}

	@Override
	public boolean deleteNode(String uniqueNodeId) {

		try {
			jmasarClient.deleteNode(uniqueNodeId);
		} catch (DataProviderException e) {
			return false;
		}
		return true;
	}
	
	@Override
	public List<ConfigPv> getConfigPvs(String uniqueNodeId) {
		
		return jmasarClient.getConfigPvs(uniqueNodeId);
	}

    @Override
    public Node getSaveSetForSnapshot(String uniqueNodeId) {

        return jmasarClient.getParentNode(uniqueNodeId);
    }
	
	@Override
	public Node updateSaveSet(Node configToUpdate, List<ConfigPv> confgPvList) {
		return jmasarClient.updateConfiguration(configToUpdate, confgPvList);
	}

	@Override
	public List<SnapshotItem> getSnapshotItems(String snapshotUniqueId){
		return jmasarClient.getSnapshotItems(snapshotUniqueId);
	}


	@Override
	public Node saveSnapshot(String configUniqueId, List<SnapshotItem> snapshotItems, String snapshotName, String comment){
		return jmasarClient.saveSnapshot(configUniqueId, snapshotItems, snapshotName, comment);
	}

	@Override
	public List<Tag> getAllTags() {
		return jmasarClient.getAllTags();
	}

	@Override
	public List<Node> getFromPath(String path) {
		return jmasarClient.getFromPath(path);
	}

	@Override
	public String getFullPath(String uniqueNodeId) {
		return jmasarClient.getFullPath(uniqueNodeId);
	}
}
