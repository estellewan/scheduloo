package com.example.models;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourseUser {

    private int id;
    private int course_id;
    private long user_id;

    public CourseUser() {}
    public CourseUser(int course_id, long user_id) {
        this.course_id = course_id;
        this.user_id = user_id;
    }
    
    public int getId() {
        return id;
    }
    
    public int getCourseId() {
        return course_id;
    }

    public long getUserId() {
        return user_id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setCourseId(int course_id) {
        this.course_id = course_id;
    }

    public void setUserId(long user_id) {
        this.user_id = user_id;
    }
}
