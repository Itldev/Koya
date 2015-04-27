package fr.itldev.koya.model.impl;

import org.alfresco.service.cmr.repository.NodeRef;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import fr.itldev.koya.services.impl.util.NodeRefDeserializer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationDetails {

    private NodeRef parentNodeRef;
    private String firstName;
    private String lastName;
    private String name;
    private String title;
    private String typeQName;
    private NodeRef nodeRef;
    private String displayPath;

    public NodeRef getParentNodeRef() {
        return parentNodeRef;
    }

    public void setParentNodeRef(NodeRef parentNodeRef) {
        this.parentNodeRef = parentNodeRef;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    public String getTypeQName() {
        return typeQName;
    }

    public void setTypeQName(String typeQName) {
        this.typeQName = typeQName;
    }

//    @JsonDeserialize(using = NodeRefDeserializer.class)
    public NodeRef getNodeRef() {
        return nodeRef;
    }

    public void setNodeRef(NodeRef nodeRef) {
        this.nodeRef = nodeRef;
    }

    public String getDisplayPath() {
        return displayPath;
    }

    public void setDisplayPath(String displayPath) {
        this.displayPath = displayPath;
    }
    
}
