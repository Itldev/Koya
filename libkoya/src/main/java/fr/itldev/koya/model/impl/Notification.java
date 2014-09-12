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
package fr.itldev.koya.model.impl;

import fr.itldev.koya.model.json.NotificationDetailsDeserializer;
import java.util.Date;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

public class Notification {

    private Integer id;
    @JsonProperty("siteNetwork")
    private String companyName;
    private String feedUserId;
    private String postUserId;
    @JsonProperty("postDate")
    private Date date;
    @JsonProperty("activityType")
    private String notificationType;
    @JsonProperty("activitySummary")
    //The activitySummary json properties send back to us by the alfresco webservice is a string containing json, we also deserialize it.
    @JsonDeserialize(using = NotificationDetailsDeserializer.class)
    private NotificationDetails notificationDetails;
    
    private String message;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getFeedUserId() {
        return feedUserId;
    }

    public void setFeedUserId(String feedUserId) {
        this.feedUserId = feedUserId;
    }

    public String getPostUserId() {
        return postUserId;
    }

    public void setPostUserId(String postUserId) {
        this.postUserId = postUserId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public NotificationDetails getNotificationDetails() {
        return notificationDetails;
    }

    public void setNotificationDetails(NotificationDetails notificationDetails) {
        this.notificationDetails = notificationDetails;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    

}
