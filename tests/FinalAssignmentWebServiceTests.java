import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.es2.finalassignment.Application;

class FinalAssignmentWebServiceTests {

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
	void testContentType() throws IOException {
		URL url  = new URL("http://127.0.0.1:8080/client/1");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();

		con.setRequestMethod("GET");
		con.setRequestProperty("Authorization", "Bearer 123456789");
		con.setRequestProperty("Accept", "application/json");
		
		if(200 == con.getResponseCode()) {
			BufferedReader in = new BufferedReader(
					  new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}
			System.out.println(content);
			in.close();
		}
		con.disconnect();
		assertEquals("application/json", con.getHeaderField("Content-Type"));
			
	}

}
