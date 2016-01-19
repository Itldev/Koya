package fr.itldev.koya.model.impl;

import java.util.Date;
import java.util.HashMap;

import org.alfresco.service.cmr.repository.NodeRef;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import fr.itldev.koya.model.json.util.ActivitySummaryDeserializer;

public class Activity {
	private Integer id;
	@JsonProperty("siteNetwork")
	private String companyName;
	private String feedUserId;
	private String postUserId;
	@JsonProperty("postDate")
	private Date date;
	@JsonProperty("activityType")
	private String activityType;

	/*
	 * The activitySummary json properties send back to us by the alfresco
	 * webservice is a string containing json, we also deserialize it.
	 */
	@JsonProperty("activitySummary")
	@JsonDeserialize(using = ActivitySummaryDeserializer.class)
	private HashMap<String, String> activitySummary;

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

	public HashMap<String, String> getActivitySummary() {
		return activitySummary;
	}

	public void setActivitySummary(HashMap<String, String> activitySummary) {
		this.activitySummary = activitySummary;
	}

	public String getActivityType() {
		return activityType;
	}

	public void setActivityType(String activityType) {
		this.activityType = activityType;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getProperty(String propertyKey) {
		return this.activitySummary.get(propertyKey);
	}

	public NodeRef getNodeRefProperty(String propertyKey) {
		return new NodeRef(getProperty(propertyKey));
	}

}
