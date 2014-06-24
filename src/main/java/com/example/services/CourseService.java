package com.example.services;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.example.models.Course;
import com.example.models.CourseUser;

@Path("/course")
public class CourseService {
    
    // Registered API uwaterloo key
    protected String API_KEY = "801e2a06ff9686b4599da267caf86c7b";
   
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
     * @throws Exception 
     */
    @POST
    @Path("/add-course-user")
    @Consumes(MediaType.APPLICATION_JSON)
    public void addCourseWithUser(CourseUser courseUser) throws Exception {
        Connection connection = getConnection();
        
        Statement stmt = connection.createStatement();

        int count = 0;
        int course_id = courseUser.getCourseId();
        int user_id = courseUser.getUserId();
                
        // DUPLICATE - Check if we are adding a class which has already been added
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM course_user "+
                "WHERE course_id="+course_id+" AND user_id="+user_id);
        
        rs.next();
        count = rs.getInt("rowcount");
        rs.close();
        
        // Count the number of records with same course id and user id
        if (count > 0) {
            stmt.close();
            connection.close();
            throw new SQLException("Course has already been added!");
        }
        
        // TIME CONFLICT - Check if no time clashes with class
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
        JSONObject classDetail = null;
        JSONArray classes = null;
        ArrayList<String> weekdays = null;
        JSONObject date = null;
        Date start_time = null;
        Date end_time = null;
        
        // Get class details from UWaterloo Open API
        try {
            classDetail = getJSONFromAPIUrlAsJSONObject("https://api.uwaterloo.ca/v2/courses/" +
                    course_id + "/schedule.json?key="+API_KEY+"&output=json").getJSONArray("data").getJSONObject(0);
            
            classes = classDetail.getJSONArray("classes");
            
            // Go through list of class times
            for (int k = 0; k < classes.length(); k++) {
                
                date = classes.getJSONObject(k).getJSONObject("date");

                weekdays = splitDays(date.getString("weekdays"));
                
                start_time = sdf.parse(date.getString("start_time"));
                end_time = sdf.parse(date.getString("end_time"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("There's was an error in getting the class details, please try again later.");
        }
        
        rs = stmt.executeQuery("SELECT * FROM course_user AS cu JOIN courses AS c ON c.id = cu.course_id WHERE user_id="+user_id);

        while (rs.next()) {
            
            ArrayList<String> r_weekdays = splitDays(rs.getString("weekday"));
            
            Date r_start = null; 
            Date r_end = null;
            
            try {
                r_start = sdf.parse(rs.getString("start_time"));
                r_end = sdf.parse(rs.getString("end_time"));
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            for (int j = 0; j < weekdays.size(); j++) {
                if (r_weekdays.contains(weekdays.get(j))) {
                    
                    String class_conflict = rs.getString("subject_code")+" "+rs.getString("subject_catalog");
                    
                    if ((r_start.before(start_time) || r_start.equals(start_time)) &&
                            r_end.after(start_time)) {
                        throw new Exception("Cannot add class. There is a time conflict with another class: "+class_conflict);
                    } else if ((start_time.before(r_start) || start_time.equals(r_start)) &&
                            end_time.after(r_start)) {
                        throw new Exception("Cannot add class. There is a time conflict with another class: "+class_conflict);
                    }
                            
                }
            }
            
        }
        
        rs.close();

        String msql = "INSERT INTO course_user (course_id, user_id) "+
                "VALUES ("+course_id+",'"+user_id+"')";
        stmt.executeUpdate(msql);
        
        // Insert course into course table if not there already
        try {
            
            msql = "INSERT INTO courses (id, subject_code, subject_catalog, section, weekday, start_time, end_time) "+
                    "VALUES ("+course_id+",'"+classDetail.getString("subject")+"','"+classDetail.getString("catalog_number")+"','"+
                    classDetail.getString("section")+"','"+date.getString("weekdays")+"','"+date.getString("start_time")+"','"+date.getString("end_time")+"')";
            
            stmt.executeUpdate(msql);
            
        } catch (SQLException e) {
            // Do not care if trying to overwrite course
            // This scenario will happen for multiple users
        }
        
        stmt.close();
        
        connection.close();
    }
    
    private String[] daysofWeek = {"M", "Th", "W", "T", "F"};
    
    private ArrayList<String> splitDays(String str) {
        
        ArrayList<String> arr = new ArrayList<String>();
        
        if (str.length() == 0) return null;
        
        for (int i = 0; i < daysofWeek.length; i++) {
            if (str.contains(daysofWeek[i])) {
                str = str.replace(daysofWeek[i], "");
                arr.add(daysofWeek[i]);
            }
        }
        
        return arr;
    }

    private JSONObject getJSONFromAPIUrlAsJSONObject(String url) throws Exception {
        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse responseGet = client.execute(new HttpGet(url));
        HttpEntity resEntityGet = responseGet.getEntity();
        
        if (resEntityGet != null) { 
            return new JSONObject(EntityUtils.toString(resEntityGet));            
        }
        
        return null;
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

