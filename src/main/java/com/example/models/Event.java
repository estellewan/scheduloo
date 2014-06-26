package com.example.models;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class Event {

    private int id;
    private String title;
    private String start_time;
    private String end_time;

    public Event() {}
    public Event(int id, String title) {
    	this.id = id;
        this.setTitle(title);
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getStartTime() {
        return start_time;
    }
    
    public void setStartTime(String startTime) {
        this.start_time = startTime;
    }
    
    public String getEndTime() {
        return end_time;
    }
    
    public void setEndTime(String endTime) {
        this.end_time = endTime;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
}
