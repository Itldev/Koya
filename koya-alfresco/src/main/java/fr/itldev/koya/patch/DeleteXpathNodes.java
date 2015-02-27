package fr.itldev.koya.patch;

import java.util.List;

import org.alfresco.repo.admin.patch.AbstractPatch;

import fr.itldev.koya.alfservice.KoyaNodeService;

/**
 * 
 * Silently Delete node with xpath reference. 
 * 
 * 
 * 
 */
public class DeleteXpathNodes extends AbstractPatch {

	private List<String> xPathNodes;
	private KoyaNodeService koyaNodeService;

	// private NodeService nodeService;

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	public void setXPathNodes(List<String> xPathNodes) {
		this.xPathNodes = xPathNodes;
	}

	@Override
	protected String applyInternal() throws Exception {
		for (String xPath : xPathNodes) {
			try {
				nodeService.deleteNode(koyaNodeService.xPath2NodeRef(xPath));
			} catch (Exception e) {

			}
		}
		return "";

	}

}
