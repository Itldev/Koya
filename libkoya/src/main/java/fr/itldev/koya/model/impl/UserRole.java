package fr.itldev.koya.model.impl;

import fr.itldev.koya.model.ContentTyped;

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
