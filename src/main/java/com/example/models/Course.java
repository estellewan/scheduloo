package com.example.models;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Course {

    private int id;
    private final int subject_code;
    private final int subject_catalog;

    public Course(int subject_code, int subject_catalog) {
        this.subject_code = subject_code;
        this.subject_catalog = subject_catalog;
    }

    public int getSubjectCode() {
        return subject_code;
    }

    public int getSubjectCatalog() {
        return subject_catalog;
    }
}
