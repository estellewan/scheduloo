package com.example.services;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.example.models.Course;
import com.example.models.CourseDate;
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
    
    private String[] daysofEntireWeek = {"S", "M", "T", "W", "Th", "F","Sa"};
    DateFormat dfpost = new SimpleDateFormat("yyyy-MM-dd");
    
    /**
     * @param user_id
     * @return list of class ids enrolled in by user_id
     * @throws ParseException 
     */
    @GET
    @Path("/{user_id}/{search_date}/{n}")
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<CourseDate> getCourseByUser(@PathParam("user_id") String user_id, @PathParam("search_date") String search_date, @PathParam("n") String n) throws ClassNotFoundException, SQLException, URISyntaxException, ParseException {
        
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        
        Connection connection = getConnection();
        
        Statement stmt = connection.createStatement();

        ResultSet rs = stmt.executeQuery("SELECT * FROM course_user AS cu JOIN courses AS c ON c.id = cu.course_id WHERE user_id="+user_id);
        
        ArrayList<Course> courseListWithoutFilter = new ArrayList<Course>();
        ArrayList<CourseDate> courseDatesList = new ArrayList<CourseDate>();
        
        while (rs.next()) {
            Course course = new Course();
            course.setId(rs.getInt("course_id"));
            course.setSubjectCode(rs.getString("subject_code"));
            course.setSubjectCatalog(rs.getString("subject_catalog"));
            course.setSection(rs.getString("section"));
            course.setWeekdays(rs.getString("weekday"));
            course.setStartTime(rs.getString("start_time"));
            course.setEndTime(rs.getString("end_time"));
            courseListWithoutFilter.add(course);
        }
        
        // Get Calendar object for date
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(df.parse(search_date));
        
        for (int i = 0; i < Integer.parseInt(n); i++) {
            // Get date we want
            Calendar curCal = (Calendar) startCal.clone();
            curCal.add(Calendar.DATE, i);
            
            // Get date we want in String format
            String dateWeWant = df.format(curCal.getTime());
            
            // Get list of all courses for day we want
            ArrayList<Course> courseList = getCourseListForDay(dateWeWant, courseListWithoutFilter, connection);
            
            // Init CourseDate
            CourseDate courseDate = new CourseDate();
            courseDate.setDate(dateWeWant);
            courseDate.setCourses(courseList);
            
            courseDatesList.add(courseDate);
        }      
        

        rs.close();
        stmt.close();
        connection.close();
        
        return courseDatesList;
    }
    
    private ArrayList<Course> getCourseListForDay(String search_date, ArrayList<Course> courseListWithoutFilter, Connection connection) throws ParseException, SQLException {
        ArrayList<Course> courseList = new ArrayList<Course>();
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dfpost.parse(search_date));
        
        for (int i = 0; i < courseListWithoutFilter.size(); i++) {
            
            ArrayList<String> weekdays = splitDays(courseListWithoutFilter.get(i).getWeekdays());
            
            if (weekdays.size() == 0) {
                // TODO Dates given instead. Labs held on biweekly basis for instance
            } else {
                if (weekdays.contains(daysofEntireWeek[calendar.get(Calendar.DAY_OF_WEEK)-1])) {
                    Course course = new Course();
                    course.setId(courseListWithoutFilter.get(i).getId());
                    course.setSubjectCode(courseListWithoutFilter.get(i).getSubjectCode());
                    course.setSubjectCatalog(courseListWithoutFilter.get(i).getSubjectCatalog());
                    course.setSection(courseListWithoutFilter.get(i).getSection());
                    course.setWeekdays(courseListWithoutFilter.get(i).getWeekdays());
                    course.setStartTime(courseListWithoutFilter.get(i).getStartTime());
                    course.setEndTime(courseListWithoutFilter.get(i).getEndTime());
                    course.setUserList(getUserListFromCourseId(courseListWithoutFilter.get(i).getId(), connection));
                    courseList.add(course);
                }
            }
            
        }
        return courseList;
    }

    private ArrayList<Long> getUserListFromCourseId(int course_id, Connection connection) throws SQLException {
        
        ArrayList<Long>userIds = new ArrayList<Long>();
        Statement stmt = connection.createStatement();

        ResultSet rs = stmt.executeQuery("SELECT * FROM course_user WHERE course_id="+course_id);
        
        while (rs.next()) {
            userIds.add(rs.getLong("user_id"));
        }
        
        rs.close();
        stmt.close();
        
        return userIds;
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
        long user_id = courseUser.getUserId();
                
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
            classDetail = getJSONFromAPIUrlAsJSONObject("http://api.uwaterloo.ca/v2/courses/" +
                    course_id + "/schedule.json?key="+API_KEY+"&output=json").getJSONArray("data").getJSONObject(0);
            
            classes = classDetail.getJSONArray("classes");
            
            // Go through list of class times
            for (int k = 0; k < classes.length(); k++) {
                
                date = classes.getJSONObject(k).getJSONObject("date");

                weekdays = splitDays(date.getString("weekdays"));
                
                start_time = sdf.parse(date.getString("start_time"));
                end_time = sdf.parse(date.getString("end_time"));
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } catch (org.apache.http.ParseException e) {
            e.printStackTrace();
            throw e;
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
                e.printStackTrace();
                throw e;
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

    private JSONObject getJSONFromAPIUrlAsJSONObject(String url) throws ClientProtocolException, IOException, org.apache.http.ParseException, JSONException {
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

