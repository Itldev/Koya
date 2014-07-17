package fr.itldev.koya.model.impl;

/**
 * Basic contact item : can be a telephone number, fax number or mail address
 *
 */
public class ContactItem {

    public static final Integer TYPE_TEL = 1;
    public static final Integer TYPE_FAX = 2;
    public static final Integer TYPE_MAIL = 3;

    private Integer type;
    private String value;

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
