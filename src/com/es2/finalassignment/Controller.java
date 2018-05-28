package com.es2.finalassignment;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

import java.awt.RenderingHints.Key;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.stream.Collectors;

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
	//private final String serverName = "193.137.7.39";
	private final String serverName = "10.0.3.201";

	/** The port of the MySQL server (default is 3306) */
	private final int portNumber = 3306;

	/** The name of the database we are testing with (this default is installed with MySQL) */
	private final String dbName = "ei2_201718";

	@Context HttpServletRequest req; 
	
	@POST
	@Path("/auth")
	@Produces("application/json")
	public Response clientLogin() {
		JSONObject jsobj = null;
		try {
			BufferedReader kk = new BufferedReader(new InputStreamReader(req.getInputStream()));
			jsobj = new JSONObject(kk.lines().collect(Collectors.joining()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int result = execSQLLogin(getConnection(), jsobj.getString("username"), jsobj.getString("password"));
		if (result==1){
			JSONObject jj = new JSONObject();
			jj.put("success", true);
			jj.put("token", createToken(new JSONObject().put("username", req.getHeader("username")).toString() ) );
			return Response.status(200).entity(jj.toString()).build();	
		}else {
			JSONObject jj = new JSONObject();
			jj.put("success", false);
			return Response.status(401).entity(jj.toString()).build();
		}
		
	}
	
	@GET
	@Path("/client/{idclient}")
	@Produces("application/json")
    public Response clientDetails(@PathParam("idclient") Integer idClient) {
		//return Response.status(200).entity("ok 🍬").build();
		
		String keyzinho = req.getHeader("Authorization");
		if(keyzinho==null || validateToken(keyzinho.split(" ")[1])==null)
			return Response.status(401).entity( new JSONObject().append("success", false).toString()).build();
			
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
			//e.printStackTrace();
			System.out.println("Invalid Token");
			return null;
		}
		 
	}
	//Connection to DB
	public Connection getConnection()  {
		Connection conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", this.userName);
		connectionProps.put("password", this.password);

		
		try {
			conn = DriverManager.getConnection("jdbc:mysql://"
					+ this.serverName + ":" + this.portNumber + "/" + this.dbName,
					connectionProps);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return conn;
	}	

	private int execSQLLogin(Connection conn,String username,String password) {
		Statement s = null;
		ResultSet rs = null;
		try {
			s = conn.createStatement();
			String aaa = new String("SELECT EMAIL, PASSWORD FROM ei2_201718.UTILIZADORES_BEEP where EMAIL = '"+username+"'");
			//System.out.println(aaa);
			s.executeQuery(aaa);
			rs = s.getResultSet();
			
			if (rs.next())
			{
				if(rs.getString("PASSWORD").equals(password)) {
					System.out.println("Welcome " + rs.getString("EMAIL") + " " + rs.getString("PASSWORD"));	
					return 1;
				}
			}
			else
			{
				System.out.println("Sorry. You are not a registered account. Bye");
				return 0;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
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
		clients.put("success", true);
		return Response.status(200).entity(clients.toString()).build();
	}
}
