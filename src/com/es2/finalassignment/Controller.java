package com.es2.finalassignment;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

import java.awt.RenderingHints.Key;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.keys.AesKey;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.JoseException;
import org.json.JSONObject;

@Path("/app")
public class Controller {
	

	AesKey key = new AesKey("73AD5182EDA7FAA8".getBytes());
	/** The name of the MySQL account to use (or empty for anonymous) */
	private final String userName = "ei2_201718";

	/** The password for the MySQL account (or empty for anonymous) */
	private final String password = "password";

	/** The name of the computer running MySQL */
	private final String serverName = "193.137.7.39";

	/** The port of the MySQL server (default is 3306) */
	private final int portNumber = 3306;

	/** The name of the database we are testing with (this default is installed with MySQL) */
	private final String dbName = "ei2_201718";

	@Context HttpServletRequest req; 
	
	@POST
	@Path("/auth")
	@Produces("application/json")
	public Response clientLogin() {
		return Response.status(200).entity("Ola").build();
	}
	
	@GET
	@Path("/{idclient}")
	@Produces("application/json")
    public Response clientDetails(@PathParam("idclient") Integer idClient) {
		//return Response.status(200).entity("ok 🍬").build();
		JSONObject j = new JSONObject().append("name", "aaa");
		validateToken(createToken( j.toString()));
		String key = req.getHeader("Authorization");
		if(key==null || !key.split(" ")[1].equals("123456789")) return Response.status(401).entity("Bad Token").build();
			
		return getClientDetails(req, idClient);
    }

	private String createToken(String payload) {
		 JsonWebEncryption jwe = new JsonWebEncryption();
		 jwe.setPayload(payload);
		 jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.A128KW);
		 jwe.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
		 jwe.setKey(key);
		 String serializedJwe = null;
		try {
			serializedJwe = jwe.getCompactSerialization();
		} catch (JoseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 System.out.println("Serialized Encrypted JWE: " + serializedJwe);
		 return serializedJwe;
	}
	
	private String validateToken(String token) {
		 JsonWebEncryption jwe = new JsonWebEncryption();
		 jwe = new JsonWebEncryption();
		 jwe.setAlgorithmConstraints(new AlgorithmConstraints(ConstraintType.WHITELIST, 
		        KeyManagementAlgorithmIdentifiers.A128KW));
		 jwe.setContentEncryptionAlgorithmConstraints(new AlgorithmConstraints(ConstraintType.WHITELIST, 
		        ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256));
		 jwe.setKey(key);
		 try {
			jwe.setCompactSerialization(token);
			System.out.println("Payload: " + jwe.getPayload());
			return jwe.getPayload();
		} catch (JoseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 return null;
		 
	}
	//Connection to DB
	public Connection getConnection() throws SQLException {
		Connection conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", this.userName);
		connectionProps.put("password", this.password);

		
		conn = DriverManager.getConnection("jdbc:mysql://"
				+ this.serverName + ":" + this.portNumber + "/" + this.dbName,
				connectionProps);

		return conn;
	}	

	private void execSQL(Connection conn,String query) {
		Statement s = null;
		ResultSet rs = null;
		try {
			s = conn.createStatement();

		s.executeQuery(query);
		rs = s.getResultSet();
		
		if (rs.next())
		{
			//outputToClient.writeObject();
			
			double overall_CA =  rs.getInt("CA_Mark")*0.30;
			double overall_Exam = rs.getInt("Exam_Mark")*0.70;
			double grade = overall_CA + overall_Exam;
			
			String info = "StudentID: " + rs.getString("STUD_ID") + "\n First Name: " + rs.getString("FNAME") + "\n Last Name: " + rs.getString("SNAME") + "\n"
					+ "Module: " + rs.getString("ModuleName") +"\n CA MARK: " + rs.getInt("CA_Mark") + "(" + overall_CA +")\n "
							+ "Exam Mark: " + rs.getInt("Exam_Mark") + "(" +  overall_Exam + ") \n Overall Grade: " + grade ;
			
			System.out.println("Welcome " + rs.getString("FNAME") + " " + rs.getString("SNAME") + " you are now connected to the Server \n"+ info);
		}
		else
		{
			System.out.println("Sorry. You are not a registered student. Bye \\n");
		}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private Response getClientDetails(HttpServletRequest req, Integer idClient) {

		JSONObject clients = new JSONObject();
		
				
		switch(idClient) {
			case 1 : 
				clients.put("id", idClient);
				clients.put("name", "José das Couves");
				clients.put("address", "Viseu");
				clients.put("ssn", "211888999");
				clients.put("job", "ajudante");
				break;
			case 2 : 
				clients.put("id", idClient);
				clients.put("name", "Maria da Sé");
				clients.put("address", "Porto");
				break;
			case 3 : 
				String s = "<data><id>3</id><name>João dos Coices</name><address>Alentejo</address></data>";
				return Response.status(200).entity(s).build();
			default:
				return Response.status(404).entity("Not found").build();
		}
				
		return Response.status(200).entity(clients.toString()).build();
	}
}
