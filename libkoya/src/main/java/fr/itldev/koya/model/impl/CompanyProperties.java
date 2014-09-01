package fr.itldev.koya.model.impl;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 *
 */
public class CompanyProperties {

    private String companyName;
    private String companyAddress;
    private String logoNodeRef;
    private String description;
    private String legalInformations;
    private List<Contact> contacts = new ArrayList<>();
    private List<ContactItem> contactItems = new ArrayList<>();

    private GeoPos geoPos;

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyAddress() {
        return companyAddress;
    }

    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }

    public String getLogoNodeRef() {
        return logoNodeRef;
    }

    public void setLogoNodeRef(String logoNodeRef) {
        this.logoNodeRef = logoNodeRef;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLegalInformations() {
        return legalInformations;
    }

    public void setLegalInformations(String legalInformations) {
        this.legalInformations = legalInformations;
    }

    public GeoPos getGeoPos() {
        return geoPos;
    }

    public void setGeoPos(GeoPos geoPos) {
        this.geoPos = geoPos;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }

    public List<ContactItem> getContactItems() {
        return contactItems;
    }

    public void setContactItems(List<ContactItem> contactItems) {
        this.contactItems = contactItems;
    }

    // </editor-fold >
    public CompanyProperties() {
    }

    public CompanyProperties(String companyName) {
        this.companyName = companyName;
    }

}
