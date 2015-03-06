package fr.itldev.koya.model;

import org.alfresco.service.cmr.repository.NodeRef;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import fr.itldev.koya.services.impl.util.NodeRefDeserializer;

public abstract class KoyaNode {

    protected NodeRef nodeRef;

    protected String name;
    protected String title;

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
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    public KoyaNode(NodeRef nodeRef) {
        this.nodeRef = nodeRef;
    }
    public KoyaNode(NodeRef nodeRef,String name) {
        this.nodeRef = nodeRef;
        this.name = name;
    }
    
    protected KoyaNode(){        
    }

}
