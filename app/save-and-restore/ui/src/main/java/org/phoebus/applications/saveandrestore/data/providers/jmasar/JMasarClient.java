package org.phoebus.applications.saveandrestore.data.providers.jmasar;

import org.phoebus.applications.saveandrestore.data.DataProviderException;
import org.phoebus.applications.saveandrestore.model.ConfigPv;
import org.phoebus.applications.saveandrestore.model.Node;
import org.phoebus.applications.saveandrestore.model.SnapshotItem;
import org.phoebus.applications.saveandrestore.model.Tag;

import java.util.List;

/**
 * Implementations of this interface shall handle the communication to the JMasar service.
 */
public interface JMasarClient {

    public String getServiceUrl();

    public Node getRoot();

    public Node getNode(String uniqueNodeId);

    public Node getParentNode(String unqiueNodeId);

    public List<Node> getChildNodes(Node node) throws DataProviderException ;

    public List<SnapshotItem> getSnapshotItems(String snapshotUniqueId);

    public Node saveSnapshot(String configUniqueId, List<SnapshotItem> snapshotItems, String snapshotName, String comment);

    public List<ConfigPv> getConfigPvs(String configUniqueId);

    public Node createNewNode(String parentsUniqueId, Node node);

    public Node updateNode(Node nodeToUpdate);

    public Node updateNode(Node nodeToUpdate, boolean customTimeForMigration);

    public void deleteNode(String uniqueNodeId);

    public Node updateConfiguration(Node configToUpdate, List<ConfigPv> configPvList);

    public List<Tag> getAllTags();

    public List<Node> getFromPath(String path);

    public String getFullPath(String uniqueNodeId);
}
