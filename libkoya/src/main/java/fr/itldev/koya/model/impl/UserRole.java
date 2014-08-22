package fr.itldev.koya.model.impl;

import fr.itldev.koya.model.ContentTyped;
import java.util.Objects;

public class UserRole implements ContentTyped {

    String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserRole(String name) {
        this.name = name;
    }

    public UserRole() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UserRole other = (UserRole) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

    /**
     * Useful method to deserialize content.
     *
     * @return
     */
    @Override
    public String getContentType() {
        return this.getClass().getCanonicalName();
    }

    /**
     * Implemented for deserialization compatibility
     *
     * @param contentType
     */
    @Override
    public void setContentType(String contentType) {
    }
}
