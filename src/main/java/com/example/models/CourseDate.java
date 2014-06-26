package com.example.models;

import java.util.ArrayList;



public class CourseDate {
    private String date;
    private ArrayList<Course> courses;
    
    
    public CourseDate() {
        courses = new ArrayList<Course>();
    }


    public String getDate() {
        return date;
    }


    public void setDate(String date) {
        this.date = date;
    }


    public ArrayList<Course> getCourses() {
        return courses;
    }


    public void setCourses(ArrayList<Course> courses) {
        this.courses = courses;
    }
}