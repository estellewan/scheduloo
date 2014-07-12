package com.example.models;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventUser {

    private int id;
    private int event_id;
    private long user_id;
    private String event_site;

    public EventUser() {}
    public EventUser(int event_id, long user_id) {
        this.setEventId(event_id);
        this.user_id = user_id;
    }
    
    public int getId() {
        return id;
    }

    public long getUserId() {
        return user_id;
    }
    
    public void setId(int id) {
        this.id = id;
    }

    public void setUserId(long user_id) {
        this.user_id = user_id;
    }
    public int getEventId() {
        return event_id;
    }
    public void setEventId(int event_id) {
        this.event_id = event_id;
    }
    public String getEventSite() {
        return event_site;
    }
    public void setEventSite(String event_site) {
        this.event_site = event_site;
    }
}
