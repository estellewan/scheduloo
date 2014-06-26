package com.example.models;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventUser {

    private int id;
    private int event_id;
    private int user_id;

    public EventUser() {}
    public EventUser(int event_id, int user_id) {
        this.setEventId(event_id);
        this.user_id = user_id;
    }
    
    public int getId() {
        return id;
    }

    public int getUserId() {
        return user_id;
    }
    
    public void setId(int id) {
        this.id = id;
    }

    public void setUserId(int user_id) {
        this.user_id = user_id;
    }
    public int getEventId() {
        return event_id;
    }
    public void setEventId(int event_id) {
        this.event_id = event_id;
    }
}
