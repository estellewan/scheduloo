package com.example.models;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class Course {

    private int id;
    private String subject_code;
    private String subject_catalog;
    private String section;
    private String weekdays;
    private String start_time;
    private String end_time;
    private ArrayList<Long> userList;

    public Course() {}
    
    public Course(int id, String subject_code, String subject_catalog, String section) {
    	this.id = id;
        this.subject_code = subject_code;
        this.subject_catalog = subject_catalog;
        this.section = section;
    }
    
    public int getId() {
        return id;
    }
    
    public String getSubjectCode() {
        return subject_code;
    }

    public String getSubjectCatalog() {
        return subject_catalog;
    }
    
    public String getSection() {
        return section;
    }
    
    public String getWeekdays() {
        return weekdays;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setSubjectCode(String subjectCode) {
        this.subject_code = subjectCode;
    }

    public void setSubjectCatalog(String subjectCatalog) {
        this.subject_catalog = subjectCatalog;
    }
    
    public void setSection(String section) {
        this.section = section;
    }
    
    public void setWeekdays(String weekdays) {
        this.weekdays = weekdays;
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
    public ArrayList<Long> getUserList() {
        return userList;
    }
    public void setUserList(ArrayList<Long>userList) {
        this.userList = userList;
    }
}
