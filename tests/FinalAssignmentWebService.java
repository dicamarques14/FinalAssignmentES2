import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.es2.finalassignment.Application;
import com.es2.finalassignment.Controller;

class FinalAssignmentWebService {
	
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
	
	private static Thread t;
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		t = new Thread()
		{
			public void run(){
			    try {
				Application.main(null);
			    } catch (Exception e) {
				e.printStackTrace();
			    }
			}
		};
		t.start();
		Thread.sleep(3000);
	}
	@AfterAll
	static void tearDownAfterClass() throws Exception {
		t.interrupt();
	}
	
	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	String doLogin() throws IOException {
		URL url  = new URL("http://127.0.0.1:8080/app/auth");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setDoOutput(true);
		con.setRequestMethod("POST");
		
		con.setRequestProperty("Accept", "application/json");
		JSONObject jsobj = new JSONObject();
		jsobj.put("username", "admin");
		jsobj.put("password", "5392005052f29208f29bf7b721a72515");
		
		OutputStream os = con.getOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
		osw.write(jsobj.toString());
		osw.flush();
		osw.close();
		os.close();  //don't forget to close the OutputStream
		con.connect();
		if(200 == con.getResponseCode()) {
			BufferedReader in = new BufferedReader(
					  new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			} 
			
			in.close();
			if(con.getHeaderField("Content-Type").equals("application/json")) {
				String f = new String(content);
				JSONObject a = new JSONObject(f);

				con.disconnect();
				return a.getString("token");
			}
		}
		
		con.disconnect();
		return null;
	}
	
	private void delete_ID_UTENTE(Long ID_UTENTE) {
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
    	String SQL_DELETE = "DELETE FROM PACIENTES_BEEP WHERE ID_UTENTE = ?";
         try {
                 PreparedStatement statement = conn.prepareStatement(SQL_DELETE,Statement.RETURN_GENERATED_KEYS);
                 statement.setLong(1, ID_UTENTE); 
                
                 int affectedRows = statement.executeUpdate();
  
                 if (affectedRows == 0) {
                     throw new SQLException("DELETING user failed, no rows affected.");
                 }
  
             }catch (Exception e) {
            	 System.out.println("Exception : " +e.getMessage());
             } 
    }
	
	@Test
	void testLoginSuccessANDTestToken() throws IOException {
		System.out.println("testLoginSuccessANDTestToken");
		URL url  = new URL("http://127.0.0.1:8080/app/auth");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setDoOutput(true);
		con.setRequestMethod("POST");
		con.setRequestProperty("Accept", "application/json");
		con.setRequestProperty("Content-Type", "application/json");
		
		JSONObject jsobj = new JSONObject();
		jsobj.put("username", "admin");
		jsobj.put("password", "5392005052f29208f29bf7b721a72515");
		
		BufferedWriter httpBodyWriter = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
		httpBodyWriter.write(jsobj.toString());
		httpBodyWriter.close(); 
		
		if(200 == con.getResponseCode()) {
			BufferedReader in = new BufferedReader(
					  new InputStreamReader(con.getInputStream()));
			String inputLine;
			//con.setDoOutput(true);
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			} 
			in.close();
			if(con.getHeaderField("Content-Type").equals("application/json")) {
				String f = new String(content);
				JSONObject a = new JSONObject(f);
				assertTrue(a.getBoolean("success"));
				String g = Controller.validateToken(a.getString("token"));
				assertNotNull(g); 
			}
		}
		con.disconnect();
		assertEquals(200, con.getResponseCode());
	}
	
	@Test
	void testLoginFailUser() throws IOException {
		System.out.println("testLoginFailUser");
		
		URL url  = new URL("http://127.0.0.1:8080/app/auth");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setDoOutput(true);
		con.setRequestMethod("POST");
		con.setRequestProperty("Accept", "application/json");
		
		JSONObject jsobj = new JSONObject();
		jsobj.put("username", "asdasdasdas");
		jsobj.put("password", "asdasdasdas");
		
		OutputStream os = con.getOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
		osw.write(jsobj.toString());
		osw.flush();
		osw.close();
		os.close();  //don't forget to close the OutputStream
		con.connect(); 
		int status = con.getResponseCode();
		
		//			con.getErrorStream() EM CASO DE ERRO USA ISTO!
		BufferedReader in;
		if(status == 200) {
			in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		}else {
			in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
		}
		String inputLine;
		StringBuffer content = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
		    content.append(inputLine);
		}
		//System.out.println(content);
		in.close();
		if(con.getHeaderField("Content-Type").equals("application/json")) {
			String f = new String(content);
			JSONObject a = new JSONObject(f); 
			  assertFalse(a.getBoolean("success"));
		}
		con.disconnect();
		assertEquals(401, con.getResponseCode());
	}
	
	@Test
	void testLoginFailPassw() throws IOException {
		System.out.println("testLoginFailPassw");
		URL url  = new URL("http://127.0.0.1:8080/app/auth");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setDoOutput(true);
		con.setRequestMethod("POST");
		
		con.setRequestProperty("Accept", "application/json");
		JSONObject jsobj = new JSONObject();
		jsobj.put("username", "admin");
		jsobj.put("password", "asdasdasdas");
		
		OutputStream os = con.getOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
		osw.write(jsobj.toString());
		osw.flush();
		osw.close();
		os.close();  //don't forget to close the OutputStream
		con.connect();
		if(401 == con.getResponseCode()) {
			BufferedReader in = new BufferedReader(
					  new InputStreamReader(con.getErrorStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			} 
			in.close();
			if(con.getHeaderField("Content-Type").equals("application/json")) {
				String f = new String(content);
				JSONObject a = new JSONObject(f);
				assertFalse(a.getBoolean("success"));

			}
		}
		con.disconnect();
		assertEquals(401, con.getResponseCode());
	}

	@Test
	void testLoginMissingPW() throws IOException {
		System.out.println("testLoginMissingPW");
		URL url  = new URL("http://127.0.0.1:8080/app/auth");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setDoOutput(true);
		con.setRequestMethod("POST");
		
		con.setRequestProperty("Accept", "application/json");
		JSONObject jsobj = new JSONObject();
		jsobj.put("username", "admin"); 
		
		OutputStream os = con.getOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
		osw.write(jsobj.toString());
		osw.flush();
		osw.close();
		os.close();  //don't forget to close the OutputStream
		int status = con.getResponseCode();
		
		//			con.getErrorStream() EM CASO DE ERRO USA ISTO!
		BufferedReader in;
		if(status == 200) {
			in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		}else {
			in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
		}
		String inputLine;
		StringBuffer content = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
		    content.append(inputLine);
		}
		//System.out.println(content);
		in.close();
		if(con.getHeaderField("Content-Type").equals("application/json")) {
			String f = new String(content);
			JSONObject a = new JSONObject(f); 
			  assertFalse(a.getBoolean("success"));
		}
		con.disconnect();

		assertEquals(401, con.getResponseCode());
	}
	
	@Test
	void testLoginMissingUsernane() throws IOException {
		System.out.println("testLoginMissingUsernane");
		URL url  = new URL("http://127.0.0.1:8080/app/auth");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setDoOutput(true);
		con.setRequestMethod("POST");
		
		con.setRequestProperty("Accept", "application/json");
		JSONObject jsobj = new JSONObject();
		jsobj.put("password", "admin"); 
		
		OutputStream os = con.getOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
		osw.write(jsobj.toString());
		osw.flush();
		osw.close();
		os.close();  //don't forget to close the OutputStream
		con.connect();
		if(401 == con.getResponseCode()) {
			BufferedReader in = new BufferedReader(
					  new InputStreamReader(con.getErrorStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			} 
			in.close();
			if(con.getHeaderField("Content-Type").equals("application/json")) {
				String f = new String(content);
				JSONObject a = new JSONObject(f);
				assertFalse(a.getBoolean("success"));
				System.out.println(a.getString("error_msg"));

			}
		}
		con.disconnect();
		assertEquals(401, con.getResponseCode());
	}
	
	@Test
	void testLoginMissingJson() throws IOException {
		System.out.println("testLoginMissingJson");
		URL url  = new URL("http://127.0.0.1:8080/app/auth");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setDoOutput(true);
		con.setRequestMethod("POST");
		
		con.setRequestProperty("Accept", "application/json"); 
		
		OutputStream os = con.getOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
		osw.write("trash");
		osw.flush();
		osw.close();
		os.close();  //don't forget to close the OutputStream
		con.connect();
		if(401 == con.getResponseCode()) {
			BufferedReader in = new BufferedReader(
					  new InputStreamReader(con.getErrorStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			} 
			in.close();
			if(con.getHeaderField("Content-Type").equals("application/json")) {
				String f = new String(content);
				JSONObject a = new JSONObject(f);
				assertFalse(a.getBoolean("success"));
				System.out.println(a.getString("error_msg"));

			}
		}
		con.disconnect();
		assertEquals(401, con.getResponseCode());
	}

	@Test
	void testADD_UTENTE_SUCCESSFUL() throws IOException {
		System.out.println("testADD_UTENTE_SUCCESSFUL");
		delete_ID_UTENTE(6000L);
		String token = doLogin();
		int status = 0;
		if(token != null) {
	 		URL url  = new URL("http://127.0.0.1:8080/app/addutente");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			
			con.setRequestProperty("Accept", "application/json");
			
			JSONObject jsobj = new JSONObject();
			jsobj.put("token", token);
			jsobj.put("ID_UTENTE", "6000"); //6000
			jsobj.put("NOME", "Joaquina Das Couves");
			jsobj.put("DATA_NASC", "1994-06-11");
			jsobj.put("ID_MORADA", "1");
			jsobj.put("TELEFONE", "123456789");
			jsobj.put("NCONTRIBUINTE", "510407404");
			jsobj.put("EMAIL", "mail@mail.com");
			jsobj.put("PESO", "100");
			jsobj.put("ALTURA", "12");
			jsobj.put("PROFISSAO", "coises");
			jsobj.put("DIABETES", "1");
			jsobj.put("HIPERTENSAO", "1");
			jsobj.put("INSUFICIENCIA", "1");
			jsobj.put("CORONARIA", "1");
			jsobj.put("VALVULA", "1");
			jsobj.put("ALERGIAS", "a ES2");
			jsobj.put("INFO", "Info");
			jsobj.put("TERAPEUTICA", "Terapia"); 
			OutputStream os = con.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
			osw.write(jsobj.toString());//JSON A ENVIAR
			osw.flush();
			osw.close();
			os.close();  //don't forget to close the OutputStream
			con.connect(); 
			status = con.getResponseCode(); 
			
			//			con.getErrorStream() EM CASO DE ERRO USA ISTO!
			BufferedReader in;
			if(status == 200) {
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			}else {
				in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}  
			in.close();
			if(con.getHeaderField("Content-Type").equals("application/json")) {
				String f = new String(content);
				JSONObject a = new JSONObject(f); 
				assertTrue(a.getBoolean("success"));
			}
			con.disconnect();
		}
		assertEquals(200, status);
	}
	
	@Test
	void testADD_UTENTE_miss_idutente() throws IOException {
		System.out.println("testADD_UTENTE_miss_idutente");
		
		String token = doLogin();
		int status = 0;
		if(token != null) {
	 		URL url  = new URL("http://127.0.0.1:8080/app/addutente");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			
			con.setRequestProperty("Accept", "application/json");
			
			JSONObject jsobj = new JSONObject();
			jsobj.put("token", token);
			//jsobj.put("ID_UTENTE", "1");
			jsobj.put("NOME", "Joaquina Das Couves");
			jsobj.put("DATA_NASC", "1994-06-11");
			jsobj.put("ID_MORADA", "1");
			jsobj.put("TELEFONE", "123456789");
			jsobj.put("NCONTRIBUINTE", "510407404");
			jsobj.put("EMAIL", "mail@mail.com");
			jsobj.put("PESO", "100");
			jsobj.put("ALTURA", "12");
			jsobj.put("PROFISSAO", "coises");
			jsobj.put("DIABETES", "1");
			jsobj.put("HIPERTENSAO", "1");
			jsobj.put("INSUFICIENCIA", "1");
			jsobj.put("CORONARIA", "1");
			jsobj.put("VALVULA", "1");
			jsobj.put("ALERGIAS", "a ES2");
			jsobj.put("INFO", "Info");
			jsobj.put("TERAPEUTICA", "Terapia");

			OutputStream os = con.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
			osw.write(jsobj.toString());//JSON A ENVIAR
			osw.flush();
			osw.close();
			os.close();  //don't forget to close the OutputStream
			con.connect(); 
			status = con.getResponseCode();

			
			//			con.getErrorStream() EM CASO DE ERRO USA ISTO!
			BufferedReader in;
			if(status == 200) {
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			}else {
				in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}

			in.close();
			if(con.getHeaderField("Content-Type").equals("application/json")) {
				String f = new String(content);
				JSONObject a = new JSONObject(f); 
				
				assertFalse(a.getBoolean("success"));
			}
			con.disconnect();
		}
		assertEquals(401, status);
	}
	
	@Test
	void testADD_UTENTE_miss_NOME() throws IOException {
		System.out.println("testADD_UTENTE_miss_NOME");
		
		String token = doLogin();
		int status = 0;
		if(token != null) {
	 		URL url  = new URL("http://127.0.0.1:8080/app/addutente");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			
			con.setRequestProperty("Accept", "application/json");
			
			JSONObject jsobj = new JSONObject();
			jsobj.put("token", token);
			jsobj.put("ID_UTENTE", "1");
			//jsobj.put("NOME", "Joaquina Das Couves");
			jsobj.put("DATA_NASC", "1994-06-11");
			jsobj.put("ID_MORADA", "1");
			jsobj.put("TELEFONE", "123456789");
			jsobj.put("NCONTRIBUINTE", "510407404");
			jsobj.put("EMAIL", "mail@mail.com");
			jsobj.put("PESO", "100");
			jsobj.put("ALTURA", "12");
			jsobj.put("PROFISSAO", "coises");
			jsobj.put("DIABETES", "1");
			jsobj.put("HIPERTENSAO", "1");
			jsobj.put("INSUFICIENCIA", "1");
			jsobj.put("CORONARIA", "1");
			jsobj.put("VALVULA", "1");
			jsobj.put("ALERGIAS", "a ES2");
			jsobj.put("INFO", "Info");
			jsobj.put("TERAPEUTICA", "Terapia");
			  
			OutputStream os = con.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
			osw.write(jsobj.toString());//JSON A ENVIAR
			osw.flush();
			osw.close();
			os.close();  //don't forget to close the OutputStream
			con.connect(); 
			status = con.getResponseCode(); 
			
			//			con.getErrorStream() EM CASO DE ERRO USA ISTO!
			BufferedReader in;
			if(status == 200) {
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			}else {
				in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}
			//System.out.println(content);
			in.close();
			if(con.getHeaderField("Content-Type").equals("application/json")) {
				String f = new String(content);
				JSONObject a = new JSONObject(f); 
				  assertFalse(a.getBoolean("success"));
			}
			con.disconnect();
		}
		assertEquals(401, status);
	}
	
	@Test
	void testADD_UTENTE_miss_DATA_NASC() throws IOException {
		System.out.println("testADD_UTENTE_miss_DATA_NASC");
		
		String token = doLogin();
		int status = 0;
		if(token != null) {
	 		URL url  = new URL("http://127.0.0.1:8080/app/addutente");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			
			con.setRequestProperty("Accept", "application/json");
			
			JSONObject jsobj = new JSONObject();
			jsobj.put("token", token);
			jsobj.put("ID_UTENTE", "1");
			jsobj.put("NOME", "Joaquina Das Couves");
			//jsobj.put("DATA_NASC", "1994-06-11");
			jsobj.put("ID_MORADA", "1");
			jsobj.put("TELEFONE", "123456789");
			jsobj.put("NCONTRIBUINTE", "510407404");
			jsobj.put("EMAIL", "mail@mail.com");
			jsobj.put("PESO", "100");
			jsobj.put("ALTURA", "12");
			jsobj.put("PROFISSAO", "coises");
			jsobj.put("DIABETES", "1");
			jsobj.put("HIPERTENSAO", "1");
			jsobj.put("INSUFICIENCIA", "1");
			jsobj.put("CORONARIA", "1");
			jsobj.put("VALVULA", "1");
			jsobj.put("ALERGIAS", "a ES2");
			jsobj.put("INFO", "Info");
			jsobj.put("TERAPEUTICA", "Terapia");
			  
			OutputStream os = con.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
			osw.write(jsobj.toString());//JSON A ENVIAR
			osw.flush();
			osw.close();
			os.close();  //don't forget to close the OutputStream
			con.connect(); 
			status = con.getResponseCode();
			
			//			con.getErrorStream() EM CASO DE ERRO USA ISTO!
			BufferedReader in;
			if(status == 200) {
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			}else {
				in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}
			//System.out.println(content);
			in.close();
			if(con.getHeaderField("Content-Type").equals("application/json")) {
				String f = new String(content);
				JSONObject a = new JSONObject(f); 
				  assertFalse(a.getBoolean("success"));
			}
			con.disconnect();
		}
		assertEquals(401, status);
	}
	
	@Test
	void testADD_UTENTE_miss_ID_MORADA() throws IOException {
		System.out.println("testADD_UTENTE_miss_ID_MORADA");
		
		String token = doLogin();
		int status = 0;
		if(token != null) {
	 		URL url  = new URL("http://127.0.0.1:8080/app/addutente");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			
			con.setRequestProperty("Accept", "application/json");
			
			JSONObject jsobj = new JSONObject();
			jsobj.put("token", token);
			jsobj.put("ID_UTENTE", "1");
			jsobj.put("NOME", "Joaquina Das Couves");
			jsobj.put("DATA_NASC", "1994-06-11");
			//jsobj.put("ID_MORADA", "1");
			jsobj.put("TELEFONE", "123456789");
			jsobj.put("NCONTRIBUINTE", "510407404");
			jsobj.put("EMAIL", "mail@mail.com");
			jsobj.put("PESO", "100");
			jsobj.put("ALTURA", "12");
			jsobj.put("PROFISSAO", "coises");
			jsobj.put("DIABETES", "1");
			jsobj.put("HIPERTENSAO", "1");
			jsobj.put("INSUFICIENCIA", "1");
			jsobj.put("CORONARIA", "1");
			jsobj.put("VALVULA", "1");
			jsobj.put("ALERGIAS", "a ES2");
			jsobj.put("INFO", "Info");
			jsobj.put("TERAPEUTICA", "Terapia");
			  
			OutputStream os = con.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
			osw.write(jsobj.toString());//JSON A ENVIAR
			osw.flush();
			osw.close();
			os.close();  //don't forget to close the OutputStream
			con.connect(); 
			status = con.getResponseCode();
			
			//			con.getErrorStream() EM CASO DE ERRO USA ISTO!
			BufferedReader in;
			if(status == 200) {
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			}else {
				in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}
			//System.out.println(content);
			in.close();
			if(con.getHeaderField("Content-Type").equals("application/json")) {
				String f = new String(content);
				JSONObject a = new JSONObject(f); 
				  assertFalse(a.getBoolean("success"));
					System.out.println(a.getString("error_msg"));
			}
			con.disconnect();
		}
		assertEquals(401, status);
	}
	
	@Test
	void testADD_UTENTE_miss_TELEFONE() throws IOException {
		System.out.println("testADD_UTENTE_miss_TELEFONE");
		
		String token = doLogin();
		int status = 0;
		if(token != null) {
	 		URL url  = new URL("http://127.0.0.1:8080/app/addutente");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			
			con.setRequestProperty("Accept", "application/json");
			
			JSONObject jsobj = new JSONObject();
			jsobj.put("token", token);
			jsobj.put("ID_UTENTE", "1");
			jsobj.put("NOME", "Joaquina Das Couves");
			jsobj.put("DATA_NASC", "1994-06-11");
			jsobj.put("ID_MORADA", "1");
			//jsobj.put("TELEFONE", "123456789");
			jsobj.put("NCONTRIBUINTE", "510407404");
			jsobj.put("EMAIL", "mail@mail.com");
			jsobj.put("PESO", "100");
			jsobj.put("ALTURA", "12");
			jsobj.put("PROFISSAO", "coises");
			jsobj.put("DIABETES", "1");
			jsobj.put("HIPERTENSAO", "1");
			jsobj.put("INSUFICIENCIA", "1");
			jsobj.put("CORONARIA", "1");
			jsobj.put("VALVULA", "1");
			jsobj.put("ALERGIAS", "a ES2");
			jsobj.put("INFO", "Info");
			jsobj.put("TERAPEUTICA", "Terapia");
			  
			OutputStream os = con.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
			osw.write(jsobj.toString());//JSON A ENVIAR
			osw.flush();
			osw.close();
			os.close();  //don't forget to close the OutputStream
			con.connect(); 
			status = con.getResponseCode();
			
			//			con.getErrorStream() EM CASO DE ERRO USA ISTO!
			BufferedReader in;
			if(status == 200) {
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			}else {
				in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}
			//System.out.println(content);
			in.close();
			if(con.getHeaderField("Content-Type").equals("application/json")) {
				String f = new String(content);
				JSONObject a = new JSONObject(f); 
				  assertFalse(a.getBoolean("success"));
			}
			con.disconnect();
		}
		assertEquals(401, status);
	}
	
	@Test
	void testADD_UTENTE_miss_NCONTRIBUINTE() throws IOException {
		System.out.println("testADD_UTENTE_miss_NCONTRIBUINTE");
		
		String token = doLogin();
		int status = 0;
		if(token != null) {
	 		URL url  = new URL("http://127.0.0.1:8080/app/addutente");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			
			con.setRequestProperty("Accept", "application/json");
			
			JSONObject jsobj = new JSONObject();
			jsobj.put("token", token);
			jsobj.put("ID_UTENTE", "1");
			jsobj.put("NOME", "Joaquina Das Couves");
			jsobj.put("DATA_NASC", "1994-06-11");
			jsobj.put("ID_MORADA", "1");
			jsobj.put("TELEFONE", "123456789");
			//jsobj.put("NCONTRIBUINTE", "510407404");
			jsobj.put("EMAIL", "mail@mail.com");
			jsobj.put("PESO", "100");
			jsobj.put("ALTURA", "12");
			jsobj.put("PROFISSAO", "coises");
			jsobj.put("DIABETES", "1");
			jsobj.put("HIPERTENSAO", "1");
			jsobj.put("INSUFICIENCIA", "1");
			jsobj.put("CORONARIA", "1");
			jsobj.put("VALVULA", "1");
			jsobj.put("ALERGIAS", "a ES2");
			jsobj.put("INFO", "Info");
			jsobj.put("TERAPEUTICA", "Terapia");
			  
			OutputStream os = con.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
			osw.write(jsobj.toString());//JSON A ENVIAR
			osw.flush();
			osw.close();
			os.close();  //don't forget to close the OutputStream
			con.connect(); 
			status = con.getResponseCode();
			
			//			con.getErrorStream() EM CASO DE ERRO USA ISTO!
			BufferedReader in;
			if(status == 200) {
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			}else {
				in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}
			//System.out.println(content);
			in.close();
			if(con.getHeaderField("Content-Type").equals("application/json")) {
				String f = new String(content);
				JSONObject a = new JSONObject(f); 
				  assertFalse(a.getBoolean("success"));
			}
			con.disconnect();
		}
		assertEquals(401, status);
	}
	
	@Test
	void testADD_UTENTE_miss_EMAIL() throws IOException {
		System.out.println("testADD_UTENTE_miss_EMAIL");
		
		String token = doLogin();
		int status = 0;
		if(token != null) {
	 		URL url  = new URL("http://127.0.0.1:8080/app/addutente");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			
			con.setRequestProperty("Accept", "application/json");
			
			JSONObject jsobj = new JSONObject();
			jsobj.put("token", token);
			jsobj.put("ID_UTENTE", "1");
			jsobj.put("NOME", "Joaquina Das Couves");
			jsobj.put("DATA_NASC", "1994-06-11");
			jsobj.put("ID_MORADA", "1");
			jsobj.put("TELEFONE", "123456789");
			jsobj.put("NCONTRIBUINTE", "510407404");
			//jsobj.put("EMAIL", "mail@mail.com");
			jsobj.put("PESO", "100");
			jsobj.put("ALTURA", "12");
			jsobj.put("PROFISSAO", "coises");
			jsobj.put("DIABETES", "1");
			jsobj.put("HIPERTENSAO", "1");
			jsobj.put("INSUFICIENCIA", "1");
			jsobj.put("CORONARIA", "1");
			jsobj.put("VALVULA", "1");
			jsobj.put("ALERGIAS", "a ES2");
			jsobj.put("INFO", "Info");
			jsobj.put("TERAPEUTICA", "Terapia");
			  
			OutputStream os = con.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
			osw.write(jsobj.toString());//JSON A ENVIAR
			osw.flush();
			osw.close();
			os.close();  //don't forget to close the OutputStream
			con.connect(); 
			status = con.getResponseCode();
			
			//			con.getErrorStream() EM CASO DE ERRO USA ISTO!
			BufferedReader in;
			if(status == 200) {
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			}else {
				in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}
			//System.out.println(content);
			in.close();
			if(con.getHeaderField("Content-Type").equals("application/json")) {
				String f = new String(content);
				JSONObject a = new JSONObject(f); 
				  assertFalse(a.getBoolean("success"));
			}
			con.disconnect();
		}
		assertEquals(401, status);
	}

	@Test
	void testADD_UTENTE_miss_PESO() throws IOException {
		System.out.println("testADD_UTENTE_miss_PESO");
		
		String token = doLogin();
		int status = 0;
		if(token != null) {
	 		URL url  = new URL("http://127.0.0.1:8080/app/addutente");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			
			con.setRequestProperty("Accept", "application/json");
			
			JSONObject jsobj = new JSONObject();
			jsobj.put("token", token);
			jsobj.put("ID_UTENTE", "1");
			jsobj.put("NOME", "Joaquina Das Couves");
			jsobj.put("DATA_NASC", "1994-06-11");
			jsobj.put("ID_MORADA", "1");
			jsobj.put("TELEFONE", "123456789");
			jsobj.put("NCONTRIBUINTE", "510407404");
			jsobj.put("EMAIL", "mail@mail.com");
			//jsobj.put("PESO", "100");
			jsobj.put("ALTURA", "12");
			jsobj.put("PROFISSAO", "coises");
			jsobj.put("DIABETES", "1");
			jsobj.put("HIPERTENSAO", "1");
			jsobj.put("INSUFICIENCIA", "1");
			jsobj.put("CORONARIA", "1");
			jsobj.put("VALVULA", "1");
			jsobj.put("ALERGIAS", "a ES2");
			jsobj.put("INFO", "Info");
			jsobj.put("TERAPEUTICA", "Terapia");
			  
			OutputStream os = con.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
			osw.write(jsobj.toString());//JSON A ENVIAR
			osw.flush();
			osw.close();
			os.close();  //don't forget to close the OutputStream
			con.connect(); 
			status = con.getResponseCode();
			
			//			con.getErrorStream() EM CASO DE ERRO USA ISTO!
			BufferedReader in;
			if(status == 200) {
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			}else {
				in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}
			//System.out.println(content);
			in.close();
			if(con.getHeaderField("Content-Type").equals("application/json")) {
				String f = new String(content);
				JSONObject a = new JSONObject(f); 
				  assertFalse(a.getBoolean("success"));
			}
			con.disconnect();
		}
		assertEquals(401, status);
	}

	@Test
	void testADD_UTENTE_miss_ALTURA() throws IOException {
		System.out.println("testADD_UTENTE_miss_ALTURA");
		
		String token = doLogin();
		int status = 0;
		if(token != null) {
	 		URL url  = new URL("http://127.0.0.1:8080/app/addutente");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			
			con.setRequestProperty("Accept", "application/json");
			
			JSONObject jsobj = new JSONObject();
			jsobj.put("token", token);
			jsobj.put("ID_UTENTE", "1");
			jsobj.put("NOME", "Joaquina Das Couves");
			jsobj.put("DATA_NASC", "1994-06-11");
			jsobj.put("ID_MORADA", "1");
			jsobj.put("TELEFONE", "123456789");
			jsobj.put("NCONTRIBUINTE", "510407404");
			jsobj.put("EMAIL", "mail@mail.com");
			jsobj.put("PESO", "100");
			//jsobj.put("ALTURA", "12");
			jsobj.put("PROFISSAO", "coises");
			jsobj.put("DIABETES", "1");
			jsobj.put("HIPERTENSAO", "1");
			jsobj.put("INSUFICIENCIA", "1");
			jsobj.put("CORONARIA", "1");
			jsobj.put("VALVULA", "1");
			jsobj.put("ALERGIAS", "a ES2");
			jsobj.put("INFO", "Info");
			jsobj.put("TERAPEUTICA", "Terapia");
			  
			OutputStream os = con.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
			osw.write(jsobj.toString());//JSON A ENVIAR
			osw.flush();
			osw.close();
			os.close();  //don't forget to close the OutputStream
			con.connect(); 
			status = con.getResponseCode();
			
			//			con.getErrorStream() EM CASO DE ERRO USA ISTO!
			BufferedReader in;
			if(status == 200) {
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			}else {
				in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}
			//System.out.println(content);
			in.close();
			if(con.getHeaderField("Content-Type").equals("application/json")) {
				String f = new String(content);
				JSONObject a = new JSONObject(f); 
				  assertFalse(a.getBoolean("success"));
			}
			con.disconnect();
		}
		assertEquals(401, status);
	}
	
	@Test
	void testADD_UTENTE_miss_PROFISSAO() throws IOException {
		System.out.println("testADD_UTENTE_miss_PROFISSAO");
		
		String token = doLogin();
		int status = 0;
		if(token != null) {
	 		URL url  = new URL("http://127.0.0.1:8080/app/addutente");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			
			con.setRequestProperty("Accept", "application/json");
			
			JSONObject jsobj = new JSONObject();
			jsobj.put("token", token);
			jsobj.put("ID_UTENTE", "1");
			jsobj.put("NOME", "Joaquina Das Couves");
			jsobj.put("DATA_NASC", "1994-06-11");
			jsobj.put("ID_MORADA", "1");
			jsobj.put("TELEFONE", "123456789");
			jsobj.put("NCONTRIBUINTE", "510407404");
			jsobj.put("EMAIL", "mail@mail.com");
			jsobj.put("PESO", "100");
			jsobj.put("ALTURA", "12");
			//jsobj.put("PROFISSAO", "coises");
			jsobj.put("DIABETES", "1");
			jsobj.put("HIPERTENSAO", "1");
			jsobj.put("INSUFICIENCIA", "1");
			jsobj.put("CORONARIA", "1");
			jsobj.put("VALVULA", "1");
			jsobj.put("ALERGIAS", "a ES2");
			jsobj.put("INFO", "Info");
			jsobj.put("TERAPEUTICA", "Terapia");
			  
			OutputStream os = con.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
			osw.write(jsobj.toString());//JSON A ENVIAR
			osw.flush();
			osw.close();
			os.close();  //don't forget to close the OutputStream
			con.connect(); 
			status = con.getResponseCode();
			
			//			con.getErrorStream() EM CASO DE ERRO USA ISTO!
			BufferedReader in;
			if(status == 200) {
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			}else {
				in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}
			//System.out.println(content);
			in.close();
			if(con.getHeaderField("Content-Type").equals("application/json")) {
				String f = new String(content);
				JSONObject a = new JSONObject(f); 
				  assertFalse(a.getBoolean("success"));
			}
			con.disconnect();
		}
		assertEquals(401, status);
	}
	
	@Test
	void testADD_UTENTE_miss_DIABETES() throws IOException {
		System.out.println("testADD_UTENTE_miss_DIABETES");
		
		String token = doLogin();
		int status = 0;
		if(token != null) {
	 		URL url  = new URL("http://127.0.0.1:8080/app/addutente");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			
			con.setRequestProperty("Accept", "application/json");
			
			JSONObject jsobj = new JSONObject();
			jsobj.put("token", token);
			jsobj.put("ID_UTENTE", "1");
			jsobj.put("NOME", "Joaquina Das Couves");
			jsobj.put("DATA_NASC", "1994-06-11");
			jsobj.put("ID_MORADA", "1");
			jsobj.put("TELEFONE", "123456789");
			jsobj.put("NCONTRIBUINTE", "510407404");
			jsobj.put("EMAIL", "mail@mail.com");
			jsobj.put("PESO", "100");
			jsobj.put("ALTURA", "12");
			jsobj.put("PROFISSAO", "coises");
			//jsobj.put("DIABETES", "1");
			jsobj.put("HIPERTENSAO", "1");
			jsobj.put("INSUFICIENCIA", "1");
			jsobj.put("CORONARIA", "1");
			jsobj.put("VALVULA", "1");
			jsobj.put("ALERGIAS", "a ES2");
			jsobj.put("INFO", "Info");
			jsobj.put("TERAPEUTICA", "Terapia");
			  
			OutputStream os = con.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
			osw.write(jsobj.toString());//JSON A ENVIAR
			osw.flush();
			osw.close();
			os.close();  //don't forget to close the OutputStream
			con.connect(); 
			status = con.getResponseCode();
			
			//			con.getErrorStream() EM CASO DE ERRO USA ISTO!
			BufferedReader in;
			if(status == 200) {
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			}else {
				in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}
			//System.out.println(content);
			in.close();
			if(con.getHeaderField("Content-Type").equals("application/json")) {
				String f = new String(content);
				JSONObject a = new JSONObject(f); 
				  assertFalse(a.getBoolean("success"));
			}
			con.disconnect();
		}
		assertEquals(401, status);
	}
	
	@Test
	void testADD_UTENTE_miss_HIPERTENSAO() throws IOException {
		System.out.println("testADD_UTENTE_miss_HIPERTENSAO");
		
		String token = doLogin();
		int status = 0;
		if(token != null) {
	 		URL url  = new URL("http://127.0.0.1:8080/app/addutente");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			
			con.setRequestProperty("Accept", "application/json");
			
			JSONObject jsobj = new JSONObject();
			jsobj.put("token", token);
			jsobj.put("ID_UTENTE", "1");
			jsobj.put("NOME", "Joaquina Das Couves");
			jsobj.put("DATA_NASC", "1994-06-11");
			jsobj.put("ID_MORADA", "1");
			jsobj.put("TELEFONE", "123456789");
			jsobj.put("NCONTRIBUINTE", "510407404");
			jsobj.put("EMAIL", "mail@mail.com");
			jsobj.put("PESO", "100");
			jsobj.put("ALTURA", "12");
			jsobj.put("PROFISSAO", "coises");
			jsobj.put("DIABETES", "1");
			//jsobj.put("HIPERTENSAO", "1");
			jsobj.put("INSUFICIENCIA", "1");
			jsobj.put("CORONARIA", "1");
			jsobj.put("VALVULA", "1");
			jsobj.put("ALERGIAS", "a ES2");
			jsobj.put("INFO", "Info");
			jsobj.put("TERAPEUTICA", "Terapia");
			  
			OutputStream os = con.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
			osw.write(jsobj.toString());//JSON A ENVIAR
			osw.flush();
			osw.close();
			os.close();  //don't forget to close the OutputStream
			con.connect(); 
			status = con.getResponseCode();
			
			//			con.getErrorStream() EM CASO DE ERRO USA ISTO!
			BufferedReader in;
			if(status == 200) {
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			}else {
				in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}
			//System.out.println(content);
			in.close();
			if(con.getHeaderField("Content-Type").equals("application/json")) {
				String f = new String(content);
				JSONObject a = new JSONObject(f); 
				  assertFalse(a.getBoolean("success"));
			}
			con.disconnect();
		}
		assertEquals(401, status);
	}

	@Test
	void testADD_UTENTE_miss_INSUFICIENCIA() throws IOException {
		System.out.println("testADD_UTENTE_miss_INSUFICIENCIA");
		
		String token = doLogin();
		int status = 0;
		if(token != null) {
	 		URL url  = new URL("http://127.0.0.1:8080/app/addutente");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			
			con.setRequestProperty("Accept", "application/json");
			
			JSONObject jsobj = new JSONObject();
			jsobj.put("token", token);
			jsobj.put("ID_UTENTE", "1");
			jsobj.put("NOME", "Joaquina Das Couves");
			jsobj.put("DATA_NASC", "1994-06-11");
			jsobj.put("ID_MORADA", "1");
			jsobj.put("TELEFONE", "123456789");
			jsobj.put("NCONTRIBUINTE", "510407404");
			jsobj.put("EMAIL", "mail@mail.com");
			jsobj.put("PESO", "100");
			jsobj.put("ALTURA", "12");
			jsobj.put("PROFISSAO", "coises");
			jsobj.put("DIABETES", "1");
			jsobj.put("HIPERTENSAO", "1");
			//jsobj.put("INSUFICIENCIA", "1");
			jsobj.put("CORONARIA", "1");
			jsobj.put("VALVULA", "1");
			jsobj.put("ALERGIAS", "a ES2");
			jsobj.put("INFO", "Info");
			jsobj.put("TERAPEUTICA", "Terapia");
			  
			OutputStream os = con.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
			osw.write(jsobj.toString());//JSON A ENVIAR
			osw.flush();
			osw.close();
			os.close();  //don't forget to close the OutputStream
			con.connect(); 
			status = con.getResponseCode();
			
			//			con.getErrorStream() EM CASO DE ERRO USA ISTO!
			BufferedReader in;
			if(status == 200) {
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			}else {
				in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}
			//System.out.println(content);
			in.close();
			if(con.getHeaderField("Content-Type").equals("application/json")) {
				String f = new String(content);
				JSONObject a = new JSONObject(f); 
				  assertFalse(a.getBoolean("success"));
			}
			con.disconnect();
		}
		assertEquals(401, status);
	}
	
	@Test
	void testADD_UTENTE_miss_CORONARIA() throws IOException {
		System.out.println("testADD_UTENTE_miss_CORONARIA");
		
		String token = doLogin();
		int status = 0;
		if(token != null) {
	 		URL url  = new URL("http://127.0.0.1:8080/app/addutente");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			
			con.setRequestProperty("Accept", "application/json");
			
			JSONObject jsobj = new JSONObject();
			jsobj.put("token", token);
			jsobj.put("ID_UTENTE", "1");
			jsobj.put("NOME", "Joaquina Das Couves");
			jsobj.put("DATA_NASC", "1994-06-11");
			jsobj.put("ID_MORADA", "1");
			jsobj.put("TELEFONE", "123456789");
			jsobj.put("NCONTRIBUINTE", "510407404");
			jsobj.put("EMAIL", "mail@mail.com");
			jsobj.put("PESO", "100");
			jsobj.put("ALTURA", "12");
			jsobj.put("PROFISSAO", "coises");
			jsobj.put("DIABETES", "1");
			jsobj.put("HIPERTENSAO", "1");
			jsobj.put("INSUFICIENCIA", "1");
			//jsobj.put("CORONARIA", "1");
			jsobj.put("VALVULA", "1");
			jsobj.put("ALERGIAS", "a ES2");
			jsobj.put("INFO", "Info");
			jsobj.put("TERAPEUTICA", "Terapia");
			  
			OutputStream os = con.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
			osw.write(jsobj.toString());//JSON A ENVIAR
			osw.flush();
			osw.close();
			os.close();  //don't forget to close the OutputStream
			con.connect(); 
			status = con.getResponseCode();
			
			//			con.getErrorStream() EM CASO DE ERRO USA ISTO!
			BufferedReader in;
			if(status == 200) {
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			}else {
				in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}
			//System.out.println(content);
			in.close();
			if(con.getHeaderField("Content-Type").equals("application/json")) {
				String f = new String(content);
				JSONObject a = new JSONObject(f); 
				  assertFalse(a.getBoolean("success"));
			}
			con.disconnect();
		}
		assertEquals(401, status);
	}
	
	@Test
	void testADD_UTENTE_miss_VALVULA() throws IOException {
		System.out.println("testADD_UTENTE_miss_VALVULA");
		
		String token = doLogin();
		int status = 0;
		if(token != null) {
	 		URL url  = new URL("http://127.0.0.1:8080/app/addutente");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			
			con.setRequestProperty("Accept", "application/json");
			
			JSONObject jsobj = new JSONObject();
			jsobj.put("token", token);
			jsobj.put("ID_UTENTE", "1");
			jsobj.put("NOME", "Joaquina Das Couves");
			jsobj.put("DATA_NASC", "1994-06-11");
			jsobj.put("ID_MORADA", "1");
			jsobj.put("TELEFONE", "123456789");
			jsobj.put("NCONTRIBUINTE", "510407404");
			jsobj.put("EMAIL", "mail@mail.com");
			jsobj.put("PESO", "100");
			jsobj.put("ALTURA", "12");
			jsobj.put("PROFISSAO", "coises");
			jsobj.put("DIABETES", "1");
			jsobj.put("HIPERTENSAO", "1");
			jsobj.put("INSUFICIENCIA", "1");
			jsobj.put("CORONARIA", "1");
			//jsobj.put("VALVULA", "1");
			jsobj.put("ALERGIAS", "a ES2");
			jsobj.put("INFO", "Info");
			jsobj.put("TERAPEUTICA", "Terapia");
			  
			OutputStream os = con.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
			osw.write(jsobj.toString());//JSON A ENVIAR
			osw.flush();
			osw.close();
			os.close();  //don't forget to close the OutputStream
			con.connect(); 
			status = con.getResponseCode();
			
			//			con.getErrorStream() EM CASO DE ERRO USA ISTO!
			BufferedReader in;
			if(status == 200) {
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			}else {
				in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}
			//System.out.println(content);
			in.close();
			if(con.getHeaderField("Content-Type").equals("application/json")) {
				String f = new String(content);
				JSONObject a = new JSONObject(f); 
				  assertFalse(a.getBoolean("success"));
			}
			con.disconnect();
		}
		assertEquals(401, status);
	}
	
	@Test
	void testADD_UTENTE_miss_ALERGIAS() throws IOException {
		System.out.println("testADD_UTENTE_miss_ALERGIAS");
		
		String token = doLogin();
		int status = 0;
		if(token != null) {
	 		URL url  = new URL("http://127.0.0.1:8080/app/addutente");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			
			con.setRequestProperty("Accept", "application/json");
			
			JSONObject jsobj = new JSONObject();
			jsobj.put("token", token);
			jsobj.put("ID_UTENTE", "1");
			jsobj.put("NOME", "Joaquina Das Couves");
			jsobj.put("DATA_NASC", "1994-06-11");
			jsobj.put("ID_MORADA", "1");
			jsobj.put("TELEFONE", "123456789");
			jsobj.put("NCONTRIBUINTE", "510407404");
			jsobj.put("EMAIL", "mail@mail.com");
			jsobj.put("PESO", "100");
			jsobj.put("ALTURA", "12");
			jsobj.put("PROFISSAO", "coises");
			jsobj.put("DIABETES", "1");
			jsobj.put("HIPERTENSAO", "1");
			jsobj.put("INSUFICIENCIA", "1");
			jsobj.put("CORONARIA", "1");
			jsobj.put("VALVULA", "1");
			//jsobj.put("ALERGIAS", "a ES2");
			jsobj.put("INFO", "Info");
			jsobj.put("TERAPEUTICA", "Terapia");
			  
			OutputStream os = con.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
			osw.write(jsobj.toString());//JSON A ENVIAR
			osw.flush();
			osw.close();
			os.close();  //don't forget to close the OutputStream
			con.connect(); 
			status = con.getResponseCode();
			
			//			con.getErrorStream() EM CASO DE ERRO USA ISTO!
			BufferedReader in;
			if(status == 200) {
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			}else {
				in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}
			//System.out.println(content);
			in.close();
			if(con.getHeaderField("Content-Type").equals("application/json")) {
				String f = new String(content);
				JSONObject a = new JSONObject(f); 
				  assertFalse(a.getBoolean("success"));
			}
			con.disconnect();
		}
		assertEquals(401, status);
	}
	
	@Test
	void testADD_UTENTE_miss_INFO() throws IOException {
		System.out.println("testADD_UTENTE_miss_INFO");
		
		String token = doLogin();
		int status = 0;
		if(token != null) {
	 		URL url  = new URL("http://127.0.0.1:8080/app/addutente");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			
			con.setRequestProperty("Accept", "application/json");
			
			JSONObject jsobj = new JSONObject();
			jsobj.put("token", token);
			jsobj.put("ID_UTENTE", "1");
			jsobj.put("NOME", "Joaquina Das Couves");
			jsobj.put("DATA_NASC", "1994-06-11");
			jsobj.put("ID_MORADA", "1");
			jsobj.put("TELEFONE", "123456789");
			jsobj.put("NCONTRIBUINTE", "510407404");
			jsobj.put("EMAIL", "mail@mail.com");
			jsobj.put("PESO", "100");
			jsobj.put("ALTURA", "12");
			jsobj.put("PROFISSAO", "coises");
			jsobj.put("DIABETES", "1");
			jsobj.put("HIPERTENSAO", "1");
			jsobj.put("INSUFICIENCIA", "1");
			jsobj.put("CORONARIA", "1");
			jsobj.put("VALVULA", "1");
			jsobj.put("ALERGIAS", "a ES2");
			//jsobj.put("INFO", "Info");
			jsobj.put("TERAPEUTICA", "Terapia");
			  
			OutputStream os = con.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
			osw.write(jsobj.toString());//JSON A ENVIAR
			osw.flush();
			osw.close();
			os.close();  //don't forget to close the OutputStream
			con.connect(); 
			status = con.getResponseCode();
			
			//			con.getErrorStream() EM CASO DE ERRO USA ISTO!
			BufferedReader in;
			if(status == 200) {
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			}else {
				in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}
			//System.out.println(content);
			in.close();
			if(con.getHeaderField("Content-Type").equals("application/json")) {
				String f = new String(content);
				JSONObject a = new JSONObject(f); 
				  assertFalse(a.getBoolean("success"));
			}
			con.disconnect();
		}
		assertEquals(401, status);
	}
		
	@Test
	void testADD_UTENTE_miss_TERAPEUTICA() throws IOException {
		System.out.println("testADD_UTENTE_miss_TERAPEUTICA");
		
		String token = doLogin();
		int status = 0;
		if(token != null) {
	 		URL url  = new URL("http://127.0.0.1:8080/app/addutente");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			
			con.setRequestProperty("Accept", "application/json");
			
			JSONObject jsobj = new JSONObject();
			jsobj.put("token", token);
			jsobj.put("ID_UTENTE", "1");
			jsobj.put("NOME", "Joaquina Das Couves");
			jsobj.put("DATA_NASC", "1994-06-11");
			jsobj.put("ID_MORADA", "1");
			jsobj.put("TELEFONE", "123456789");
			jsobj.put("NCONTRIBUINTE", "510407404");
			jsobj.put("EMAIL", "mail@mail.com");
			jsobj.put("PESO", "100");
			jsobj.put("ALTURA", "12");
			jsobj.put("PROFISSAO", "coises");
			jsobj.put("DIABETES", "1");
			jsobj.put("HIPERTENSAO", "1");
			jsobj.put("INSUFICIENCIA", "1");
			jsobj.put("CORONARIA", "1");
			jsobj.put("VALVULA", "1");
			jsobj.put("ALERGIAS", "a ES2");
			jsobj.put("INFO", "Info");
			//jsobj.put("TERAPEUTICA", "Terapia");
			  
			OutputStream os = con.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
			osw.write(jsobj.toString());//JSON A ENVIAR
			osw.flush();
			osw.close();
			os.close();  //don't forget to close the OutputStream
			con.connect(); 
			status = con.getResponseCode(); 
			
			//			con.getErrorStream() EM CASO DE ERRO USA ISTO!
			BufferedReader in;
			if(status == 200) {
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			}else {
				in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}

			in.close();
			if(con.getHeaderField("Content-Type").equals("application/json")) {
				String f = new String(content);
				JSONObject a = new JSONObject(f); 
				  assertFalse(a.getBoolean("success"));
			}
			con.disconnect();
		}
		assertEquals(401, status);
	}
	
	@Test
	void testADD_UTENTE_idutente_Exist() throws IOException {
		System.out.println("testADD_UTENTE_idutenteExistente");
		
		String token = doLogin();
		int status = 0;
		if(token != null) {
	 		URL url  = new URL("http://127.0.0.1:8080/app/addutente");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			
			con.setRequestProperty("Accept", "application/json");
			
			JSONObject jsobj = new JSONObject();
			jsobj.put("token", token);
			jsobj.put("ID_UTENTE", "1");
			jsobj.put("NOME", "Joaquina Das Couves");
			jsobj.put("DATA_NASC", "1994-06-11");
			jsobj.put("ID_MORADA", "1");
			jsobj.put("TELEFONE", "123456789");
			jsobj.put("NCONTRIBUINTE", "510407404");
			jsobj.put("EMAIL", "mail@mail.com");
			jsobj.put("PESO", "100");
			jsobj.put("ALTURA", "12");
			jsobj.put("PROFISSAO", "coises");
			jsobj.put("DIABETES", "1");
			jsobj.put("HIPERTENSAO", "1");
			jsobj.put("INSUFICIENCIA", "1");
			jsobj.put("CORONARIA", "1");
			jsobj.put("VALVULA", "1");
			jsobj.put("ALERGIAS", "a ES2");
			jsobj.put("INFO", "Info");
			jsobj.put("TERAPEUTICA", "Terapia");
			  
			OutputStream os = con.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
			osw.write(jsobj.toString());//JSON A ENVIAR
			osw.flush();
			osw.close();
			os.close();  //don't forget to close the OutputStream
			con.connect(); 
			status = con.getResponseCode();
			//			con.getErrorStream() EM CASO DE ERRO USA ISTO!
			BufferedReader in;
			if(status == 200) {
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			}else {
				in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}
			//System.out.println(content);
			in.close();
			if(con.getHeaderField("Content-Type").equals("application/json")) {
				String f = new String(content);
				JSONObject a = new JSONObject(f); 
				  assertFalse(a.getBoolean("success"));
			}
			con.disconnect();
		}
		assertEquals(401, status);
	}
	
	@Test
	void testADD_UTENTE_idMorada_NotExist() throws IOException {
		System.out.println("testADD_UTENTE_idMorada_NotExist");
		
		String token = doLogin();
		int status = 0;
		if(token != null) {
	 		URL url  = new URL("http://127.0.0.1:8080/app/addutente");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			
			con.setRequestProperty("Accept", "application/json");
			
			JSONObject jsobj = new JSONObject();
			jsobj.put("token", token);
			jsobj.put("ID_UTENTE", "1");
			jsobj.put("NOME", "Joaquina Das Couves");
			jsobj.put("DATA_NASC", "1994-06-11");
			jsobj.put("ID_MORADA", "-1");
			jsobj.put("TELEFONE", "123456789");
			jsobj.put("NCONTRIBUINTE", "510407404");
			jsobj.put("EMAIL", "mail@mail.com");
			jsobj.put("PESO", "100");
			jsobj.put("ALTURA", "12");
			jsobj.put("PROFISSAO", "coises");
			jsobj.put("DIABETES", "1");
			jsobj.put("HIPERTENSAO", "1");
			jsobj.put("INSUFICIENCIA", "1");
			jsobj.put("CORONARIA", "1");
			jsobj.put("VALVULA", "1");
			jsobj.put("ALERGIAS", "a ES2");
			jsobj.put("INFO", "Info");
			jsobj.put("TERAPEUTICA", "Terapia");
			  
			OutputStream os = con.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
			osw.write(jsobj.toString());//JSON A ENVIAR
			osw.flush();
			osw.close();
			os.close();  //don't forget to close the OutputStream
			con.connect(); 
			status = con.getResponseCode();
			 
			
			//			con.getErrorStream() EM CASO DE ERRO USA ISTO!
			BufferedReader in;
			if(status == 200) {
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			}else {
				in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}
			//System.out.println(content);
			in.close();
			if(con.getHeaderField("Content-Type").equals("application/json")) {
				String f = new String(content);
				JSONObject a = new JSONObject(f); 
				  assertFalse(a.getBoolean("success"));
			}
			con.disconnect();
		}
		assertEquals(401, status);
	}
	
	
	@Test
	void testADD_UTENTE_noToken() throws IOException {
		System.out.println("testADD_UTENTE_noToken");
		
		String token = doLogin();
		int status = 0;
		if(token != null) {
	 		URL url  = new URL("http://127.0.0.1:8080/app/addutente");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			
			con.setRequestProperty("Accept", "application/json");
			
			JSONObject jsobj = new JSONObject();
			jsobj.put("token", "ss2");
			jsobj.put("ID_UTENTE", "1");
			jsobj.put("NOME", "Joaquina Das Couves");
			jsobj.put("DATA_NASC", "1994-06-11");
			jsobj.put("ID_MORADA", "1");
			jsobj.put("TELEFONE", "123456789");
			jsobj.put("NCONTRIBUINTE", "510407404");
			jsobj.put("EMAIL", "mail@mail.com");
			jsobj.put("PESO", "100");
			jsobj.put("ALTURA", "12");
			jsobj.put("PROFISSAO", "coises");
			jsobj.put("DIABETES", "1");
			jsobj.put("HIPERTENSAO", "1");
			jsobj.put("INSUFICIENCIA", "1");
			jsobj.put("CORONARIA", "1");
			jsobj.put("VALVULA", "1");
			jsobj.put("ALERGIAS", "a ES2");
			jsobj.put("INFO", "Info");
			jsobj.put("TERAPEUTICA", "Terapia");
			  
			OutputStream os = con.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
			osw.write(jsobj.toString());//JSON A ENVIAR
			osw.flush();
			osw.close();
			os.close();  //don't forget to close the OutputStream
			con.connect(); 
			status = con.getResponseCode(); 
			
			//			con.getErrorStream() EM CASO DE ERRO USA ISTO!
			BufferedReader in;
			if(status == 200) {
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			}else {
				in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}
			//System.out.println(content);
			in.close();
			if(con.getHeaderField("Content-Type").equals("application/json")) {
				String f = new String(content);
				JSONObject a = new JSONObject(f); 
				  assertFalse(a.getBoolean("success"));
					System.out.println(a.getString("error_msg"));
			}
			con.disconnect();
		}
		assertEquals(401, status);
	}
	

	@Test
	void testADD_UTENTE_fail_Diabetes() throws IOException {
		System.out.println("testADD_UTENTE_fail_Diabetes");
		delete_ID_UTENTE(6000L);
		String token = doLogin();
		int status = 0;
		if(token != null) {
	 		URL url  = new URL("http://127.0.0.1:8080/app/addutente");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			
			con.setRequestProperty("Accept", "application/json");
			
			JSONObject jsobj = new JSONObject();
			jsobj.put("token", token);
			jsobj.put("ID_UTENTE", "6000"); //6000
			jsobj.put("NOME", "Joaquina Das Couves");
			jsobj.put("DATA_NASC", "1994-06-11");
			jsobj.put("ID_MORADA", "1");
			jsobj.put("TELEFONE", "123456789");
			jsobj.put("NCONTRIBUINTE", "510407404");
			jsobj.put("EMAIL", "mail@mail.com");
			jsobj.put("PESO", "100");
			jsobj.put("ALTURA", "12");
			jsobj.put("PROFISSAO", "coises");
			jsobj.put("DIABETES", "48964684684864864");
			jsobj.put("HIPERTENSAO", "1");
			jsobj.put("INSUFICIENCIA", "1");
			jsobj.put("CORONARIA", "1");
			jsobj.put("VALVULA", "1");
			jsobj.put("ALERGIAS", "a ES2");
			jsobj.put("INFO", "Info");
			jsobj.put("TERAPEUTICA", "Terapia"); 
			OutputStream os = con.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
			osw.write(jsobj.toString());//JSON A ENVIAR
			osw.flush();
			osw.close();
			os.close();  //don't forget to close the OutputStream
			con.connect(); 
			status = con.getResponseCode(); 
			
			//			con.getErrorStream() EM CASO DE ERRO USA ISTO!
			BufferedReader in;
			if(status == 200) {
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			}else {
				in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}  
			in.close();
			if(con.getHeaderField("Content-Type").equals("application/json")) {
				String f = new String(content);
				JSONObject a = new JSONObject(f); 
				assertFalse(a.getBoolean("success"));
				System.out.println(a.getString("error_msg"));
			}
			con.disconnect();
		}
		assertEquals(401, status);
	}

	
	@Test
	void testADD_UTENTE_Fail_notJson() throws IOException {
		System.out.println("testADD_UTENTE_Fail_notJson");
		
		String token = doLogin();
		int status = 0;
		if(token != null) {
	 		URL url  = new URL("http://127.0.0.1:8080/app/addutente");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			
			con.setRequestProperty("Accept", "application/json"); 

			OutputStream os = con.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
			osw.write("trash");//JSON A ENVIAR
			osw.flush();
			osw.close();
			os.close();  //don't forget to close the OutputStream
			con.connect(); 
			status = con.getResponseCode(); 
			
			//			con.getErrorStream() EM CASO DE ERRO USA ISTO!
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}

			in.close();
			if(con.getHeaderField("Content-Type").equals("application/json")) {
				String f = new String(content);
				JSONObject a = new JSONObject(f); 
				  assertFalse(a.getBoolean("success"));
					System.out.println(a.getString("error_msg"));
			}
			con.disconnect();
		}
		assertEquals(401, status);
	}
	 
	
	/*
	@Test
	void testContentType() throws IOException {
		URL url  = new URL("http://127.0.0.1:8080/app/client/1");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();

		con.setRequestMethod("GET");
		con.setRequestProperty("Authorization", "Bearer as.k.k.k.l");
		con.setRequestProperty("Accept", "application/json"); 
		con.disconnect();
		assertEquals("application/json", con.getHeaderField("Content-Type"));
			
	}*/

}
