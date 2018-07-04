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
import java.sql.PreparedStatement;
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
	private final String serverName = "193.137.7.39";
	//private final String serverName = "10.0.3.201";

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
			System.out.println(req.getInputStream());
			BufferedReader kk = new BufferedReader(new InputStreamReader(req.getInputStream()));
			
			jsobj = new JSONObject(kk.lines().collect(Collectors.joining()));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		System.out.println(insertmebitch(getConnection(),1,1,1,2,3,6,"s","a","a"));
		
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
		
		System.out.println(req.getRemoteAddr()); 
		System.out.println(req.getLocalAddr()); 
		
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
			String aaa = new String("SELECT ID_UTILIZADOR, EMAIL, PASSWORD FROM ei2_201718.UTILIZADORES_BEEP where EMAIL = '"+username+"'");
			//System.out.println(aaa);
			s.executeQuery(aaa);
			rs = s.getResultSet();
			
			if (rs.next())
			{
				if(rs.getString("PASSWORD").equals(password)) {
					System.out.println("Welcome " + rs.getString("EMAIL") + " " + rs.getString("PASSWORD"));	
					//LogSuccessfulLogin(rs.getInt("ID_UTILIZADOR") ,rs.getString("EMAIL"), )
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
	
	//retorna o id do info inserido, else 0
	private long insert_new_INFO(Connection conn,
			Long ID_UTENTE,
			Integer DIABETES,
			Integer HIPERTENSAO,
			Integer INSUFICIENCIA,
			Integer CORONARIA,
			Integer VALVULA,
			String ALERGIAS,
			String INFO,
			String TERAPEUTICA
			) {
		String SQL_INSERT = "Insert into INFO_BEEP(ID_UTENTE,DIABETES,HIPERTENSAO,INSUFICIENCIA,CORONARIA,VALVULA,ALERGIAS,INFO,TERAPEUTICA) VALUES (?,?,?,?,?,?,?,?,?)";
		Long ID_insert_INFO = (long) 0;
		try { 
		        PreparedStatement statement = conn.prepareStatement(SQL_INSERT,Statement.RETURN_GENERATED_KEYS);
		        statement.setLong(1, ID_UTENTE);
		        statement.setInt(2, DIABETES);
		        statement.setInt(3, HIPERTENSAO);
		        statement.setInt(4, INSUFICIENCIA);
		        statement.setInt(5, CORONARIA);
		        statement.setInt(6, VALVULA);
		        statement.setString(7, ALERGIAS);
		        statement.setString(8, INFO);
		        statement.setString(9, TERAPEUTICA);
		        
		        int affectedRows = statement.executeUpdate();

		        if (affectedRows == 0) {
		            throw new SQLException("Creating user failed, no rows affected.");
		        }

		        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
		            if (generatedKeys.next()) {
		            	ID_insert_INFO = generatedKeys.getLong(1); 
		            }
		            else {
		                throw new SQLException("Creating user failed, no ID obtained.");
		            }
		        }
		    }catch (Exception e) {
				// TODO: handle exception
			}
		 
		return ID_insert_INFO;
	}
	
	private long insert_new_PACIENTES(
			Connection conn,
			Long ID_UTENTE,
			Long ID_MORADA,
			Long ID_INFO,
			String NOME,
			Integer NCONTRIBUINTE,
			String DATA_NASC,
			Integer TELEFONE,
			String EMAIL,
			Integer PESO,
			Integer ALTURA,
			String PROFISSAO) {
		
		
		return 0;
	}
	
	private int ADD_UTENTE(Connection conn,
			Long ID_UTENTE,
			String NOME,
			String DATA_NASC,
			Long ID_MORADA,
			Integer TELEFONE,
			Integer NCONTRIBUINTE,
			String EMAIL,
			Integer PESO,
			Integer ALTURA,
			String PROFISSAO,
			Integer DIABETES,
			Integer HIPERTENSAO,
			Integer INSUFICIENCIA,
			Integer CORONARIA,
			Integer VALVULA,
			String ALERGIAS,
			String INFO,
			String TERAPEUTICA) {
		long info_id = 0;
		long pacientes_id = 0;
		
		info_id = insert_new_INFO(getConnection(),ID_UTENTE,DIABETES,
				  HIPERTENSAO,
				  INSUFICIENCIA,
				  CORONARIA,
				  VALVULA,
				  ALERGIAS,
				  INFO,
				  TERAPEUTICA);
		
		if(info_id > 0) {
			pacientes_id = insert_new_PACIENTES(getConnection(), ID_UTENTE, ID_MORADA, info_id, NOME, NCONTRIBUINTE, DATA_NASC, TELEFONE, EMAIL, PESO, ALTURA, PROFISSAO);
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
