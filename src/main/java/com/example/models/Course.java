package com.example.models;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Course {

    private int id;
    private final String subject_code;
    private final String subject_catalog;

    public Course(String subject_code, String subject_catalog) {
        this.subject_code = subject_code;
        this.subject_catalog = subject_catalog;
    }

    public String getSubjectCode() {
        return subject_code;
    }

    public String getSubjectCatalog() {
        return subject_catalog;
    }
}
