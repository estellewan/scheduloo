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
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.example.models.Course;
import com.example.models.CourseUser;

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
        
        rs.close();
        stmt.close();
        connection.close();
        
        return courseList;
    }
    
    /**
     * @param user_id
     * @return list of class ids enrolled in by user_id
     */
    @GET
    @Path("/{user_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<Integer> getCourseByUser(@PathParam("user_id") String user_id) throws ClassNotFoundException, SQLException, URISyntaxException {
        Connection connection = getConnection();
        
        Statement stmt = connection.createStatement();

        ResultSet rs = stmt.executeQuery("SELECT * FROM course_user WHERE user_id="+user_id);
        
        ArrayList<Integer> courseList = new ArrayList<Integer>();
        
        while (rs.next()) {
            courseList.add(rs.getInt("course_id"));
        }
        
        rs.close();
        stmt.close();
        connection.close();
        
        return courseList;
    }
    
    /**
     * Saves a course associated with a particular user
     * @param courseUser
     */
    @POST
    @Path("/add-course-user")
    @Consumes(MediaType.APPLICATION_JSON)
    public void addCourseWithUser(CourseUser courseUser) throws SQLException, ClassNotFoundException, URISyntaxException {
        Connection connection = getConnection();
        
        Statement stmt = connection.createStatement();

        // Check if we are adding a class which has already been added
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM course_user "+
                "WHERE course_id="+courseUser.getCourseId()+" AND user_id="+courseUser.getUserId());
        
        rs.next();
        int count = rs.getInt("rowcount");
        rs.close();
        
        // Count the number of records with same course id and user id
        if (count > 0) {
            stmt.close();
            connection.close();
            throw new SQLException("Course has already been added!");
        }

        String msql = "INSERT INTO course_user "+
                "(course_id, user_id) "+
                "VALUES ("+courseUser.getCourseId()+",'"+courseUser.getUserId()+"')";
        stmt.executeUpdate(msql);
        
        stmt.close();
        connection.close();
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
        
        stmt.close();
        connection.close();
    }
}

