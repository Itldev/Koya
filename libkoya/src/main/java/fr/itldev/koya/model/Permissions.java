package fr.itldev.koya.model;

import java.util.HashMap;
import java.util.Map;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *
 *
 */
public class Permissions {

    private final Integer PERM_ADDCHILD = 0;
    private final Integer PERM_RENAME = 1;
    private final Integer PERM_DELETE = 2;
    private final Integer PERM_SHARE = 3;
    private final Integer PERM_READPROPERTIES = 4;
    private final Integer PERM_DOWNLOAD = 5;
    private final Integer PERM_WRITEPROPERTIES = 6;

    private String username;
    private Map<Integer, Boolean> perms = new HashMap<>();

    public Map<Integer, Boolean> getPerms() {
        return perms;
    }

    public void setPerms(Map<Integer, Boolean> perms) {
        this.perms = perms;
    }

    public Permissions() {
    }

    public Permissions(String username) {
        this.username = username;
    }

    public void canAddChild(Boolean perm) {
        perms.put(PERM_ADDCHILD, perm);
    }

    public void canRename(Boolean perm) {
        perms.put(PERM_RENAME, perm);
    }

    public void canDelete(Boolean perm) {
        perms.put(PERM_DELETE, perm);
    }

    public void canDownload(Boolean perm) {
        perms.put(PERM_DOWNLOAD, perm);
    }

    public void canShare(Boolean perm) {
        perms.put(PERM_SHARE, perm);
    }

    public void canReadProperties(Boolean perm) {
        perms.put(PERM_READPROPERTIES, perm);
    }
    
    public void canWriteProperties(Boolean perm) {
        perms.put(PERM_WRITEPROPERTIES, perm);
    }

    /*
     *
     * ==== Public permissions getters =====
     *
     */
    @JsonIgnore
    public Boolean getCanAddChild() {
        return perms.get(PERM_ADDCHILD);
    }

    @JsonIgnore
    public Boolean getCanRename() {
        return perms.get(PERM_RENAME);
    }

    @JsonIgnore
    public Boolean getCanDelete() {
        return perms.get(PERM_DELETE);
    }

    @JsonIgnore
    public Boolean getCanDownload() {
        return perms.get(PERM_DOWNLOAD);
    }

    @JsonIgnore
    public Boolean getCanShare() {
        return perms.get(PERM_SHARE);
    }

    @JsonIgnore
    public Boolean getCanReadProperties() {
        return perms.get(PERM_READPROPERTIES);
    }
    
     @JsonIgnore
    public Boolean getCanWriteProperties() {
        return perms.get(PERM_WRITEPROPERTIES);
    }

}
