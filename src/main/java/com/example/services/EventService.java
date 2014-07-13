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
import com.example.models.Event;
import com.example.models.EventDate;
import com.example.models.EventUser;


@Path("/event")
public class EventService {

    // Registered API uwaterloo key
    protected String API_KEY = "801e2a06ff9686b4599da267caf86c7b";
   
    private static Connection getConnection() throws URISyntaxException, SQLException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");

        return DriverManager.getConnection("jdbc:mysql://cs446.cpr3v5unvsxc.us-east-1.rds.amazonaws.com:3306/cs446?"+ 
            "user=cs446&password=estellay");
    }
    
    @GET
    @Path("/{user_id}/{search_date}/{n}")
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<EventDate> getCourseByUser(@PathParam("user_id") String user_id, @PathParam("search_date") String search_date, @PathParam("n") String n) throws ClassNotFoundException, SQLException, URISyntaxException, ParseException {
        
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        
        Connection connection = getConnection();
        
        Statement stmt = connection.createStatement();

        ResultSet rs = stmt.executeQuery("SELECT * FROM event_user AS cu JOIN events AS e ON e.event_id = cu.event_id AND e.site = cu.event_site WHERE user_id="+user_id);
        
        ArrayList<Event> eventListWithoutFilter = new ArrayList<Event>();
        ArrayList<EventDate> eventDatesList = new ArrayList<EventDate>();
        
        while (rs.next()) {
            Event event = new Event();
            event.setId(rs.getInt("id"));
            event.setEventId(rs.getInt("event_id"));
            event.setSite(rs.getString("site"));
            event.setStartDate(rs.getString("start_date"));
            event.setEndDate(rs.getString("end_date"));
            event.setStartTime(rs.getString("start_time"));
            event.setEndTime(rs.getString("end_time"));
            event.setName(rs.getString("name"));
            eventListWithoutFilter.add(event);
        }
        
        // Get Calendar object for date
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(df.parse(search_date));
        
        for (int i = 0; i < Integer.parseInt(n); i++) {
            // Get date we want
            Calendar dateWeWant = (Calendar) startCal.clone();
            dateWeWant.add(Calendar.DATE, i);
            
            // Get date we want in String format
            //Date dateWeWant = curCal.getTime();
            
            // Get list of all courses for day we want
            ArrayList<Event> eventList = getEventListForDay(dateWeWant, eventListWithoutFilter, connection);
            
            // Init CourseDate
            EventDate eventDate = new EventDate();
            eventDate.setDate(df.format(dateWeWant.getTime()));
            eventDate.setEvents(eventList);
            
            eventDatesList.add(eventDate);
        }      
        

        rs.close();
        stmt.close();
        connection.close();
        
        return eventDatesList;
    }
    
    DateFormat dfpost = new SimpleDateFormat("yyyy-MM-dd");
    private ArrayList<Event> getEventListForDay(Calendar dateWeWant,
            ArrayList<Event> eventListWithoutFilter, Connection connection) throws ParseException, SQLException {
        ArrayList<Event> eventList = new ArrayList<Event>();
        
        for (int i = 0; i < eventListWithoutFilter.size(); i++) {
            
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dfpost.parse(eventListWithoutFilter.get(i).getStartDate()));
            

                if (calendar.get(Calendar.YEAR) == dateWeWant.get(Calendar.YEAR) &&
                        calendar.get(Calendar.DAY_OF_YEAR) == dateWeWant.get(Calendar.DAY_OF_YEAR)) {
                    Event event = eventListWithoutFilter.get(i);
                    event.setUserList(getUserListFromEventId(
                            eventListWithoutFilter.get(i).getEventId(),
                            eventListWithoutFilter.get(i).getSite(), connection));
                    eventList.add(event);
                }

            
        }
        return eventList;
    }

    private ArrayList<Long> getUserListFromEventId(int eventId, String site,
            Connection connection) throws SQLException {
        ArrayList<Long>userIds = new ArrayList<Long>();
        Statement stmt = connection.createStatement();

        ResultSet rs = stmt.executeQuery("SELECT * FROM event_user WHERE event_id="+eventId+" AND event_site='"+site+"'");
        
        while (rs.next()) {
            userIds.add(rs.getLong("user_id"));
        }
        
        rs.close();
        stmt.close();
        
        return userIds;
    }

    /**
     * Saves a course associated with a particular user
     * Expects event_id, site and user_id
     * @param eventUser
     * @throws Exception 
     */
    @POST
    @Path("/add-event-user")
    @Consumes(MediaType.APPLICATION_JSON)
    public void addEventWithUser(EventUser eventUser) throws Exception {
        
        Connection connection = getConnection();
        
        Statement stmt = connection.createStatement();

        int count = 0;
        int event_id = eventUser.getEventId();
        long user_id = eventUser.getUserId();
        String event_site = eventUser.getEventSite();
        
        // DUPLICATE - Check if we are adding a class which has already been added
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM event_user "+
                "WHERE event_id="+event_id+" AND user_id="+user_id+" AND event_site='"+event_site+"'");
        
        rs.next();
        count = rs.getInt("rowcount");
        rs.close();
        
        // Count the number of records with same course id and user id
        if (count > 0) {
            stmt.close();
            connection.close();
            throw new SQLException("Event has already been added!");
        }
        
        
        JSONObject eventDetail = null;
        JSONArray times = null;
        SimpleDateFormat sdfE = new SimpleDateFormat("H:mm:s");
        SimpleDateFormat sdf = new SimpleDateFormat("H:mm");
        String start_day = null;
        String end_day = null;
        
        Date start_time = null;
        Date end_time = null;
        
        String start_date = null;
        String end_date = null;
        
        // Get Event details from UWaterloo Open API and check if Event clashes with courses
        try {
            eventDetail = getJSONFromAPIUrlAsJSONObject("http://api.uwaterloo.ca/v2/events/" +
                    event_site + "/" + event_id + ".json?key="+API_KEY+"&output=json").getJSONObject("data");
            
            //classes = eventDetail.getJSONArray("classes");
            times = eventDetail.getJSONArray("times");
            
            // CHECK FOR TIME CONFLICT WITH COURSES
            // Go through list of class times
            for (int k = 0; k < times.length(); k++) {
                
                JSONObject time = times.getJSONObject(k);
                
                start_day = time.getString("start_day");
                end_day = time.getString("end_day");
                
                start_time = sdfE.parse(time.getString("start_time"));
                end_time = sdfE.parse(time.getString("end_time"));

                start_date = time.getString("start_date");
                end_date = time.getString("end_date");
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
        
        // Get list of courses user is taking to check for time conflict
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
            
            for (int j = 0; j < r_weekdays.size(); j++) {
                if (r_weekdays.get(j).equals(mapDayofWeek(start_day))) {
                    String class_conflict = rs.getString("subject_code")+" "+rs.getString("subject_catalog");
                    
                    // Event ends in same day check
                    if (start_date.equals(end_date)) {
                        if ((r_start.before(start_time) || r_start.equals(start_time)) &&
                                r_end.after(start_time)) {
                            throw new Exception("Cannot add event. There is a time conflict with class: "+class_conflict);
                        } else if ((start_time.before(r_start) || start_time.equals(r_start)) &&
                                end_time.after(r_start)) {
                            throw new Exception("Cannot add event. There is a time conflict with class: "+class_conflict);
                        }
                    } else {
                        throw new Exception("Cannot add event. Event spans over multiple days.");
                    }
                }
            }
        }
        
        String msql = "INSERT INTO event_user (event_id, event_site, user_id) "+
                "VALUES ("+event_id+",'"+event_site+"','"+user_id+"')";
        stmt.executeUpdate(msql);
        
        // Insert course into course table if not there already
        //try {
            
            msql = "INSERT INTO events (event_id, site, start_date, end_date, start_time, end_time) "+
                    "VALUES ("+event_id+",'"+event_site+"','"+start_date+"','"+
                    end_date+"','"+sdf.format(start_time)+"','"+sdf.format(end_time)+"')";
            
            stmt.executeUpdate(msql);
            
        //} catch (SQLException e) {
            // Do not care if trying to overwrite course
            // This scenario will happen for multiple users
        //}
        
        stmt.close();
        
        connection.close();
    }
    
    private String mapDayofWeek(String day) {
        if (day.equals("Monday")) return "M";
        if (day.equals("Tuesday")) return "T";
        if (day.equals("Wednesday")) return "W";
        if (day.equals("Thursday")) return "Th";
        if (day.equals("Friday")) return "F";
        if (day.equals("Saturday")) return "Sa";
        if (day.equals("Sunday")) return "Su";
        return null;
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
}