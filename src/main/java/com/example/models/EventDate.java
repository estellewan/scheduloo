package com.example.models;

import java.util.ArrayList;



public class EventDate {
    private String date;
    private ArrayList<Event> events;
    
    
    public EventDate() {
        events = new ArrayList<Event>();
    }


    public String getDate() {
        return date;
    }


    public void setDate(String date) {
        this.date = date;
    }


    public ArrayList<Event> getEvents() {
        return events;
    }


    public void setEvents(ArrayList<Event> events) {
        this.events = events;
    }
}