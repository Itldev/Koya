package fr.itldev.koya.model.json;

import org.alfresco.service.cmr.repository.NodeRef;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 *	
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PdfRendition {

	NodeRef nodeRef;
	String name;
	String title;
	Boolean success;

	public NodeRef getNodeRef() {
		return nodeRef;
	}

	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Boolean isSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

}
