package fr.itldev.koya.model;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Directory;
import fr.itldev.koya.model.impl.Document;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.SalesOffer;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.Template;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.impl.util.NodeRefDeserializer;

/**
 * Koya Node is a subset of Alfresco Nodes Handled by Koya AMP
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "ktype")
@JsonSubTypes({ @Type(value = Company.class, name = "Company"),
		@Type(value = Space.class, name = "Space"),
		@Type(value = Dossier.class, name = "Dossier"),
		@Type(value = Directory.class, name = "Directory"),
		@Type(value = Document.class, name = "Document"),
		@Type(value = SalesOffer.class, name = "Salesoffer"),
		@Type(value = Template.class, name = "Template"),
		@Type(value = User.class, name = "User") })
public abstract class KoyaNode {

	protected NodeRef nodeRef;

	protected String name;
	protected String title;
	private Map<String, String> workflows = new HashMap<>();

	/*
	 * ======== Constructors
	 */

	public KoyaNode(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}

	public KoyaNode(NodeRef nodeRef, String name) {
		this.nodeRef = nodeRef;
		this.name = name;
	}

	protected KoyaNode() {
	}

	/*
	 * ======== Attributes Getters/Setters
	 */

	@JsonDeserialize(using = NodeRefDeserializer.class)
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
		if (title == null || title.isEmpty()) {
			return name;
		}
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Map<String, String> getWorkflows() {
		return workflows;
	}

	public void setWorkflows(Map<String, String> workflows) {
		this.workflows = workflows;
	}

	/**
	 * Useful method to deserialize content.
	 * 
	 * @return
	 */
	public final String getKtype() {
		return this.getClass().getSimpleName();
	}

	/**
	 * Implemented for deserialization compatibility
	 * 
	 * @param contentType
	 */
	public final void setKtype(String contentType) {
	}

	private static final Integer HASHCONST1 = 3;
	private static final Integer HASHCONST2 = 47;

	@Override
	public int hashCode() {
		int hash = HASHCONST1;
		hash = HASHCONST2 * hash
				+ (getNodeRef() != null ? getNodeRef().hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final KoyaNode other = (KoyaNode) obj;
		if ((getNodeRef() == null) ? (other.getNodeRef() != null) : !this
				.getNodeRef().equals(other.getNodeRef())) {
			return false;
		}
		return true;
	}

	public String toString() {
		return "{'ktype':'" + getKtype() + "','name':'" + getName() + "'}";
		// "','nodeRef':'" + getNodeRef().toString() +
	}

}
