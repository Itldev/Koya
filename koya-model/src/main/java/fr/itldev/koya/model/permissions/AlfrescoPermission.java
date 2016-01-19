package fr.itldev.koya.model.permissions;

import java.util.Objects;

/**
 *
 *
 */
public abstract class AlfrescoPermission {

    protected String permissionName;

    protected AlfrescoPermission(String permissionName) {
        this.permissionName = permissionName;
    }

    @Override
    public String toString() {
        return permissionName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return this.permissionName.equals(obj.toString());
        }
        final AlfrescoPermission other = (AlfrescoPermission) obj;
        return Objects.equals(this.permissionName, other.permissionName);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.permissionName);
        return hash;
    }

}
