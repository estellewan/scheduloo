package com.example.services;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/course")
@Produces(MediaType.APPLICATION_JSON)
public class CourseService {

	private static Connection getConnection() throws URISyntaxException, SQLException {
	    URI dbUri = new URI(System.getenv("DATABASE_URL"));

	    String username = dbUri.getUserInfo().split(":")[0];
	    String password = dbUri.getUserInfo().split(":")[1];
	    String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();
	   
	    return DriverManager.getConnection("jdbc:postgresql://ec2-54-225-182-133.compute-1.amazonaws.com:5432/d62p1gkdb00cq?user=xczctfsrvdfdev&password=4E1-Aqk-r8X_sw46AB3eMXzxNx&ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory");
	}
	
    @GET
    public ResultSet get() {
    	Connection connection = null;
		try {
			connection = getConnection();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        Statement stmt = null;
        System.out.println("Read from DB: ");
		try {
			stmt = connection.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			 
		}
        ResultSet rs = null;
		try {
			rs = stmt.executeQuery("SELECT * FROM courses");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        //while (rs.next()) {
            //System.out.println("Read from DB: " + rs.getTimestamp("tick"));
        //}
        return rs;
    }
}

