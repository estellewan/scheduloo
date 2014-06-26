package com.example.services;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

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
    
    
    /**
     * Saves a course associated with a particular user
     * @param eventUser
     * @throws Exception 
     */
    @POST
    @Path("/add-event-user")
    @Consumes(MediaType.APPLICATION_JSON)
    public void addEventWithUser(EventUser eventUser) throws Exception {
        
        Connection connection = getConnection();
        
        Statement stmt = connection.createStatement();

        int event_id = eventUser.getEventId();
        int user_id = eventUser.getUserId();
        
        String msql = "INSERT INTO event_user (event_id, user_id) "+
                "VALUES ("+event_id+",'"+user_id+"')";
        stmt.executeUpdate(msql);
        
        stmt.close();
        
        connection.close();
    }
}