/**
 * Koya is an alfresco module that provides a corporate orientated dataroom.
 *
 * Copyright (C) Itl Developpement 2014
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see `<http://www.gnu.org/licenses/>`.
 */
package fr.itldev.koya.model;

import java.util.HashMap;
import java.util.Map;
import org.alfresco.service.cmr.repository.NodeRef;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * This class handle permissions applied to a node in relation for user.
 *
 */
public class Permissions {

    //Properties
    private static final Integer ReadProperties = 0;
    private static final Integer WriteProperties = 1;
    //Children
    private static final Integer CreateChildren = 2;
    private static final Integer DeleteChildren = 3;
    private static final Integer ReadChildren = 4;
    private static final Integer LinkChildren = 5;
    //Content
    private static final Integer ReadContent = 6;
    private static final Integer WriteContent = 7;
    private static final Integer ExecuteContent = 8;
    //Delete node
    private static final Integer DeleteNode = 9;
    //Associations
    private static final Integer DeleteAssociations = 10;
    private static final Integer ReadAssociations = 11;
    private static final Integer CreateAssociations = 12;
    //Permissions
    private static final Integer ReadPermissions = 13;
    private static final Integer ChangePermissions = 14;
    //Extra
//    private static final Integer Flatten = 0;
//    private static final Integer SetOwner = 0;
//    private static final Integer Lock = 0;
//    private static final Integer Unlock = 0;

    /**
     * Koya specific permissions
     */
    private static final Integer shareWithCustomers = 15;
    private static final Integer uploadAsConsumer = 16;
    //
    private String username;
    private String nodeRef;
    private Map<Integer, Boolean> perms = new HashMap<>();

    //<editor-fold>
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNodeRef() {
        return nodeRef;
    }

    public void setNodeRef(String nodeRef) {
        this.nodeRef = nodeRef;
    }

    public Map<Integer, Boolean> getPerms() {
        return perms;
    }

    public void setPerms(Map<Integer, Boolean> perms) {
        this.perms = perms;
    }

    //</editor-fold>
    public Permissions() {
    }

    public Permissions(String username, NodeRef nodeRef) {
        this.username = username;
        this.nodeRef = nodeRef.toString();
    }

    // ==============Setters===========================
    public void canReadProperties(Boolean perm) {
        perms.put(ReadProperties, perm);
    }

    public void canWriteProperties(Boolean perm) {
        perms.put(WriteProperties, perm);
    }

    public void canCreateChildren(Boolean perm) {
        perms.put(CreateChildren, perm);
    }

    public void canDeleteChildren(Boolean perm) {
        perms.put(DeleteChildren, perm);
    }

    public void canReadChildren(Boolean perm) {
        perms.put(ReadChildren, perm);
    }

    public void canLinkChildren(Boolean perm) {
        perms.put(LinkChildren, perm);
    }

    public void canReadContent(Boolean perm) {
        perms.put(ReadContent, perm);
    }

    public void canWriteContent(Boolean perm) {
        perms.put(WriteContent, perm);
    }

    public void canExecuteContent(Boolean perm) {
        perms.put(ExecuteContent, perm);
    }

    public void canDeleteNode(Boolean perm) {
        perms.put(DeleteNode, perm);
    }

    public void canDeleteAssociations(Boolean perm) {
        perms.put(DeleteAssociations, perm);
    }

    public void canReadAssociations(Boolean perm) {
        perms.put(ReadAssociations, perm);
    }

    public void canCreateAssociations(Boolean perm) {
        perms.put(CreateAssociations, perm);
    }

    public void canReadPermissions(Boolean perm) {
        perms.put(ReadPermissions, perm);
    }

    public void canChangePermissions(Boolean perm) {
        perms.put(ChangePermissions, perm);
    }

    /*
     *
     * === koya specific permissions ===
     */
    public void canShareWithCustomers(Boolean perm) {
        perms.put(shareWithCustomers, perm);
    }
    
    public void canUploadAsConsumer(Boolean perm) {
        perms.put(uploadAsConsumer, perm);
    }
    
    

    /*
     *
     * ==== Public permissions getters =====
     *
     */
    @JsonIgnore
    public Boolean getCanReadProperties() {
        return perms.get(ReadProperties);
    }

    @JsonIgnore
    public Boolean getCanWriteProperties() {
        return perms.get(WriteProperties);
    }

    @JsonIgnore
    public Boolean getCanCreateChildren() {
        return perms.get(CreateChildren);

    }

    @JsonIgnore
    public Boolean getCanDeleteChildren() {
        return perms.get(DeleteChildren);

    }

    @JsonIgnore
    public Boolean getCanReadChildren() {
        return perms.get(ReadChildren);
    }

    @JsonIgnore
    public Boolean getCanLinkChildren() {
        return perms.get(LinkChildren);
    }

    @JsonIgnore
    public Boolean getCanReadContent() {
        return perms.get(ReadContent);
    }

    @JsonIgnore
    public Boolean getCanWriteContent() {
        return perms.get(WriteContent);
    }

    @JsonIgnore
    public Boolean getCanExecuteContent() {
        return perms.get(ExecuteContent);
    }

    @JsonIgnore
    public Boolean getCanDeleteNode() {
        return perms.get(DeleteNode);
    }

    @JsonIgnore
    public Boolean getCanDeleteAssociations() {
        return perms.get(DeleteAssociations);
    }

    @JsonIgnore
    public Boolean getCanReadAssociations() {
        return perms.get(ReadAssociations);
    }

    @JsonIgnore

    public Boolean getCanCreateAssociations() {
        return perms.get(CreateAssociations);
    }

    @JsonIgnore

    public Boolean getCanReadPermissions() {
        return perms.get(ReadPermissions);
    }

    @JsonIgnore
    public Boolean getCanChangePermissions() {
        return perms.get(ChangePermissions);
    }

    /*
     *
     * === koya specific permissions ===
     */
    @JsonIgnore
    public Boolean getCanRename() {
        return getCanWriteProperties();
    }

    @JsonIgnore
    public Boolean getCanDownload() {
        return getCanReadContent();
    }

    @JsonIgnore
    public Boolean getCanShare() {
        return getCanChangePermissions();
    }

    @JsonIgnore
    public Boolean getCanShareWithCustomers() {
        return perms.get(shareWithCustomers);
    }
    
    @JsonIgnore
    public Boolean getCanUploadAsConsumer() {
        return perms.get(uploadAsConsumer);
    }

    @JsonIgnore
    public Boolean getCanGrantPermissionsOnChildren() {
        return getCanCreateChildren();
    }

}
