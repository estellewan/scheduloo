package com.example.services;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.example.models.Course;

@Path("/course")
public class CourseService {

    private static Connection getConnection() throws URISyntaxException, SQLException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");

        return DriverManager.getConnection("jdbc:mysql://cs446.cpr3v5unvsxc.us-east-1.rds.amazonaws.com:3306/cs446?"+ 
            "user=cs446&password=estellay");
    }
	
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<Course> get() throws ClassNotFoundException, SQLException, URISyntaxException {
        Connection connection = getConnection();
        
        Statement stmt = connection.createStatement();

        ResultSet rs = stmt.executeQuery("SELECT * FROM courses");
        ArrayList<Course> courseList = new ArrayList<Course>();
        Course course = null;
        while (rs.next()) {
            course = new Course(rs.getInt("id"), rs.getString("subject_code"), rs.getString("subject_catalog"), rs.getString("section"));
            courseList.add(course);
        }
        return courseList;
    }
    
    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    public void add(Course course) throws SQLException, ClassNotFoundException, URISyntaxException {
    	Connection connection = getConnection();
        
        Statement stmt = connection.createStatement();

        String msql = "INSERT INTO courses "+
        		"(id,subject_code, subject_catalog, section) "+
        		"VALUES ("+course.getId()+",'"+course.getSubjectCode()+"','"+course.getSubjectCatalog()+"','"+course.getSection()+"')";
        stmt.executeUpdate(msql);
    }
}

