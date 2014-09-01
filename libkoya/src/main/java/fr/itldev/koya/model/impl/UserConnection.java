package fr.itldev.koya.model.impl;

import java.util.Date;

/**
 * Single connection item.
 *
 */
public class UserConnection {

    private Date date;
    private String ip;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public UserConnection(Date date, String ip) {
        this.date = date;
        this.ip = ip;
    }

    public UserConnection() {
    }

}
