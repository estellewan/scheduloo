package com.example.services;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.example.models.Course;

@Path("/course")
@Produces(MediaType.APPLICATION_JSON)
public class CourseService {

	private static Connection getConnection() throws URISyntaxException, SQLException, ClassNotFoundException {
	    URI dbUri = new URI(System.getenv("DATABASE_URL"));
	    Class.forName("org.postgresql.Driver");

	    String username = dbUri.getUserInfo().split(":")[0];
	    String password = dbUri.getUserInfo().split(":")[1];
	    String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();

	    return DriverManager.getConnection(dbUrl+"?user="+username+"&password="+password+"&ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory");
	}
	
    @GET
    public ArrayList<Course> get() throws ClassNotFoundException, SQLException, URISyntaxException {
    	Connection connection = getConnection();
        
        Statement stmt = connection.createStatement();

        ResultSet rs = stmt.executeQuery("SELECT * FROM courses");
		
		ArrayList<Course> courseList = new ArrayList<Course>();
		Course course = null;
		while (rs.next()) {
			course = new Course(rs.getString("subject_code"), rs.getString("subject_catalog"));
			courseList.add(course);
		}
        return courseList;
    }
}

