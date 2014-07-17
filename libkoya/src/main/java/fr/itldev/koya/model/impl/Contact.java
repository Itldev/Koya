package fr.itldev.koya.model.impl;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 */
public class Contact {

    private String userNodeRef;
    private String firstName;
    private String lastName;
    private String title;
    private List<ContactItem> contactItems = new ArrayList<>();

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public String getUserNodeRef() {
        return userNodeRef;
    }

    public void setUserNodeRef(String userNodeRef) {
        this.userNodeRef = userNodeRef;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ContactItem> getContactItems() {
        return contactItems;
    }

    public void setContactItems(List<ContactItem> contactItems) {
        this.contactItems = contactItems;
    }

    // </editor-fold >
}
