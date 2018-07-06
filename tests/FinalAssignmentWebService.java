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

import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.es2.finalassignment.Application;
import com.es2.finalassignment.Controller;

class FinalAssignmentWebService {
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

	@Test
	void testLoginSuccess() throws IOException {
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

		System.out.println(con.getResponseCode());
		if(200 == con.getResponseCode()) {
			BufferedReader in = new BufferedReader(
					  new InputStreamReader(con.getInputStream()));
			String inputLine;
			//con.setDoOutput(true);
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}
			//System.out.println(content);
			in.close();
			if(con.getHeaderField("Content-Type").equals("application/json")) {
				String f = new String(content);
				JSONObject a = new JSONObject(f);
				String g = Controller.validateToken(a.getString("token"));
				assertNotNull(g);
				System.out.println(g);
			}
		}
		con.disconnect();
		assertEquals(200, con.getResponseCode());
	}
	
	@Test
	void testLoginFailUser() throws IOException {
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
		con.disconnect();
		assertEquals(401, con.getResponseCode());
	}
	
	@Test
	void testLoginFailPassw() throws IOException {
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
			//System.out.println(content);
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
			//System.out.println(content);
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
	
	@Test
	void testADD_UTENTE_SUCCESSFUL() throws IOException {
		
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
			System.out.println("aa"+status);
			
			//			con.getErrorStream() EM CASO DE ERRO USA ISTO!
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
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
				System.out.println(a.toString());
			}
			con.disconnect();
		}
		assertEquals(200, status);
	}
	
	@Test
	void testADD_UTENTE_Fail_notJson() throws IOException {
		
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
				System.out.println(a.toString());
			}
			con.disconnect();
		}
		assertEquals(401, status);
	}
	
	@Test
	void testADD_UTENTE_Fail_InvalidJson() throws IOException {
		
		String token = doLogin();
		int status = 0;
		if(token != null) {
	 		URL url  = new URL("http://127.0.0.1:8080/app/addutente");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			
			con.setRequestProperty("Accept", "application/json"); 

			JSONObject jsobj = new JSONObject(); 
			jsobj.put("ID_UTENTE", "1");
			OutputStream os = con.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
			osw.write(jsobj.toString());//JSON A ENVIAR
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
				System.out.println(a.toString());
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
