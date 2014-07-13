package com.example.models;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class Event {

    private int id;
    private int event_id;
    private String site;
    private String name;
    private String start_time;
    private String end_time;
    private String start_date;
    private String end_date;
    private ArrayList<Long> userList;

    public Event() {}
    public Event(int id, String site) {
    	this.id = id;
        this.setSite(site);
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

    public int getEventId() {
        return event_id;
    }
    public void setEventId(int event_id) {
        this.event_id = event_id;
    }
    public String getSite() {
        return site;
    }
    public void setSite(String site) {
        this.site = site;
    }
    public String getStartDate() {
        return start_date;
    }
    public void setStartDate(String start_date) {
        this.start_date = start_date;
    }
    public String getEndDate() {
        return end_date;
    }
    public void setEndDate(String end_date) {
        this.end_date = end_date;
    }
    public ArrayList<Long> getUserList() {
        return userList;
    }
    public void setUserList(ArrayList<Long>userList) {
        this.userList = userList;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
