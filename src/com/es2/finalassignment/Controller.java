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
import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime;    
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
 
@Path("/app")
public class Controller {
   
 
    static AesKey key = new AesKey("73AD5182EDA7FAA8".getBytes());
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
        Boolean isOk=false; 
        String error_msg = ""; 
        try { 
            
            BufferedReader kk = new BufferedReader(new InputStreamReader(req.getInputStream())); 
            String jsonEmString = kk.lines().collect(Collectors.joining()); 
            if(isJSONValid(jsonEmString)) { 
               
              jsobj = new JSONObject(jsonEmString); 
              if(jsobj.has("username") && jsobj.has("password")) { 
                isOk=true;   
              }else { 
                error_msg="Invalid JSON"; 
              } 
            }else { 
              error_msg="Missing JSON"; 
            } 
             
          } catch (IOException e) { 
            // TODO Auto-generated catch block 
            e.printStackTrace(); 
          } 
       
       
        //System.out.println(ADD_UTENTE(2l,"NOME","1996/12/19",1l,1,1,"email@tu.com",2,3,"profissao",1,0,0,0,0,"alergias","info","terapeutica"));
        //System.out.println(ADD_MEDICO("NOME","NOME_CLINICO","email@tu.com",999999999,"PASSWORD","1996/12/19",19121996,"IPV_MEDECINA","ES2",3));
        
        
        if(isOk) { 
        	   DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
        	   LocalDateTime now = LocalDateTime.now();  
        	   
	        long result = VALIDAR_LOGIN(jsobj.getString("username"), jsobj.getString("password"),dtf.format(now),"DeviceXPTO");
	        //o resultaod da funçao valida é 0 sempre que da erro caso contrario devolve o id do utilizador que logou
	        if (result!=0){
	            JSONObject jj = new JSONObject();
	            jj.put("success", true);
	            jj.put("token", createToken(new JSONObject().put("username", req.getHeader("username")).toString() ) );
	            return Response.status(200).entity(jj.toString()).build(); 
	        }else { 
	            error_msg="Invalid Login"; 
	          } 
        }
         
        JSONObject jj = new JSONObject(); 
        jj.put("success", false); 
        jj.put("error_msg", error_msg); 
        return Response.status(401).entity(jj.toString()).build(); 
       
    }
 
    @POST 
    @Path("/addutente") 
    @Produces("application/json") 
    public Response addUtenteResponse() { 
      JSONObject jsobj = null; 
      Boolean isOk=false; 
      String error_msg = ""; 
      try { 
         
        BufferedReader kk = new BufferedReader(new InputStreamReader(req.getInputStream())); 
        String jsonEmString = kk.lines().collect(Collectors.joining()); 
         
        //verifica se é json! 
        if(isJSONValid(jsonEmString)) { 
           
          jsobj = new JSONObject(jsonEmString); 
          //verifica se tem os campos! 
          if(jsobj.has("token")) { 
            if( validateToken(jsobj.getString("token"))!= null ) { 
              //check the rest of the fields and exec addutente! 
            	if(jsobj.has("ID_UTENTE") && 
                jsobj.has("NOME") &&
                jsobj.has("DATA_NASC") &&
                jsobj.has("ID_MORADA") && 
                jsobj.has("TELEFONE") &&
                jsobj.has("NCONTRIBUINTE") && 
                jsobj.has("EMAIL") &&
                jsobj.has("PESO") &&
                jsobj.has("ALTURA") &&
                jsobj.has("PROFISSAO") &&
                jsobj.has("DIABETES") && 
                jsobj.has("HIPERTENSAO") && 
                jsobj.has("INSUFICIENCIA") && 
                jsobj.has("CORONARIA") && 
                jsobj.has("VALVULA") && 
                jsobj.has("ALERGIAS") &&
                jsobj.has("INFO") &&
                jsobj.has("TERAPEUTICA")) {
            		isOk=true;   
            	}else{
            		error_msg="Invalid JSON";
            	}
            } 
          }else { 
            error_msg="Invalid JSON"; 
          } 
           
        }else { 
          error_msg="Missing JSON"; 
        } 
         
      } catch (IOException e) { 
        // TODO Auto-generated catch block 
        e.printStackTrace(); 
      } 
       
      if(isOk) {  
        int result = ADD_UTENTE( 
        jsobj.getLong("ID_UTENTE"), 
        jsobj.getString("NOME"), 
        jsobj.getString("DATA_NASC"), 
        jsobj.getLong("ID_MORADA"), 
        jsobj.getInt("TELEFONE"), 
        jsobj.getInt("NCONTRIBUINTE"), 
        jsobj.getString("EMAIL"), 
        jsobj.getInt("PESO"), 
        jsobj.getInt("ALTURA"), 
        jsobj.getString("PROFISSAO"), 
        jsobj.getInt("DIABETES"), 
        jsobj.getInt("HIPERTENSAO"), 
        jsobj.getInt("INSUFICIENCIA"), 
        jsobj.getInt("CORONARIA"), 
        jsobj.getInt("VALVULA"), 
        jsobj.getString("ALERGIAS"), 
        jsobj.getString("INFO"),
        jsobj.getString("TERAPEUTICA")); 
         
        if (result==1){ 
          JSONObject jj = new JSONObject(); 
          jj.put("success", true); 
          return Response.status(200).entity(jj.toString()).build();   
        }else { 
          error_msg="Invalid Insert"; 
        } 
      } 
       
      JSONObject jj = new JSONObject(); 
      jj.put("success", false); 
      jj.put("error_msg", error_msg); 
      return Response.status(401).entity(jj.toString()).build(); 
    } 
    
    
    private boolean isJSONValid(String test) { 
        try { 
            new JSONObject(test); 
        } catch (JSONException ex) { 
            try { 
                new JSONArray(test); 
            } catch (JSONException ex1) { 
                System.out.println("[E]Invalid json!"); 
              return false; 
            } 
        } 
        return true; 
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
   
    public static String validateToken(String token) {
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
 

   
    //Funções para adicionar utente
    private long insert_new_INFO(
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
    	Connection conn = getConnection();
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
        System.out.println("Correu tudo bem até aqui!");
        System.out.println("Devolvi "+ID_insert_INFO);
        return ID_insert_INFO;
    }
   
    private long insert_new_PACIENTES(
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
    	 	Connection conn = getConnection();
	        String SQL_INSERT = "Insert into PACIENTES_BEEP(ID_UTENTE,ID_MORADA,ID_INFO,NOME,NCONTRIBUINTE,DATA_NASC,TELEFONE,EMAIL,PESO,ALTURA,PROFISSAO) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
	        Long ID_insert_INFO = (long) 0;
	        try {
	                PreparedStatement statement = conn.prepareStatement(SQL_INSERT,Statement.RETURN_GENERATED_KEYS);
	                statement.setLong(1, ID_UTENTE);
	                statement.setLong(2, ID_MORADA);
	                statement.setLong(3, ID_INFO);
	                statement.setString(4, NOME);
	                statement.setInt(5, NCONTRIBUINTE);
	                statement.setString(6, DATA_NASC);
	                statement.setInt(7, TELEFONE);
	                statement.setString(8, EMAIL);
	                statement.setInt(9, PESO);
	                statement.setInt(10, ALTURA);
	                statement.setString(11, PROFISSAO);
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
	            	System.out.println("Exception : " +e.getMessage());
	            	return 1;
	            	
	            }
	    
    	    System.out.println("Correu tudo bem até aqui! #2parte");
	        System.out.println("Devolvi "+ID_insert_INFO);
	       
    	   
       return ID_insert_INFO;
    }
    
    private int Verificarnutente(Long ID_UTENTE) {
    	Connection conn = getConnection();
        String SQL_SELECT = "Select ID_UTENTE FROM PACIENTES_BEEP WHERE ID_UTENTE= ?";
        try {
                PreparedStatement statement = conn.prepareStatement(SQL_SELECT);
                statement.setLong(1, ID_UTENTE);
                	
                statement.executeQuery();
                ResultSet rs = statement.getResultSet();

                if (rs.next())
                {
                    Long ID_utente = rs.getLong("ID_UTENTE");
                    if (ID_utente == ID_UTENTE) {
                        return 0;
                    }
                    else {
                        return 1;
                    }
                }
                else
                {
                        return 1;
                }
                
        }catch (Exception e) {
        	System.out.println("Exception : " +e.getMessage());
        	return 1;
        	
        }
    }
   
    private int Verificarnmorada(Long ID_MORADA) {
    	Connection conn = getConnection();
        String SQL_SELECT = "Select ID_MORADA FROM MORADA_BEEP WHERE ID_MORADA= ?";
        try {
                PreparedStatement statement = conn.prepareStatement(SQL_SELECT);
                statement.setLong(1, ID_MORADA);
                	
                statement.executeQuery();
                ResultSet rs = statement.getResultSet();

                if (rs.next())
                {
                    Long ID_morada = rs.getLong("ID_MORADA");
                    if (ID_morada == ID_MORADA) {
                        return 0;
                    }
                    else {
                        return 1;
                    }
                }
                else
                {
                        return 1;
                }
                
        }catch (Exception e) {
        	System.out.println("Exception : " +e.getMessage());
        	return 1;
        	
        }
    }
        
    private int ADD_UTENTE(
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
        
        Integer test1 = Verificarnutente(ID_UTENTE);
        
        if (test1 == 0) {
        	Integer test2 = Verificarnmorada(ID_MORADA);
        	if (test2 == 0) {
        		 info_id = insert_new_INFO(
        		  ID_UTENTE,
        		  DIABETES,
                  HIPERTENSAO,
                  INSUFICIENCIA,
                  CORONARIA,
                  VALVULA,
                  ALERGIAS,
                  INFO,
                  TERAPEUTICA);
        		 if(info_id > 0) {
        			 pacientes_id = insert_new_PACIENTES(ID_UTENTE, ID_MORADA, info_id, NOME, NCONTRIBUINTE, DATA_NASC, TELEFONE, EMAIL, PESO, ALTURA, PROFISSAO);
    			 	 if(pacientes_id == ID_UTENTE ) {
    			 		 return 1;
    			 	 }else {
    			 		 return 0;
    			 	 }
        		 }else {
        			 return 0;
        		 }
        	}else {
        		return 0;
        	}
        }else {
        	return 0;
        }
       
    }

    
    //Funções para adicionar Medico
    private long insert_new_UTILIZADORES(
    		String NOME,
    		String DATA_NASC,
    		Integer TELEFONE,
    		String EMAIL,
    		Integer PREVI,
    		String PASSWORD) {
    	 Connection conn = getConnection();
    	 String SQL_INSERT = "Insert into UTILIZADORES_BEEP(NOME,DATA_NASC,TELEFONE,EMAIL,PREVI,PASSWORD) VALUES (?,?,?,?,?,?)";
         Long ID_insert_of_insert= (long) 0;
         try {
                 PreparedStatement statement = conn.prepareStatement(SQL_INSERT,Statement.RETURN_GENERATED_KEYS);
                 statement.setString(1, NOME);
                 statement.setString(2, DATA_NASC);
                 statement.setInt(3, TELEFONE);
                 statement.setString(4, EMAIL);
                 statement.setInt(5, PREVI);
                 statement.setString(6, PASSWORD);
                
                 int affectedRows = statement.executeUpdate();
  
                 if (affectedRows == 0) {
                     throw new SQLException("Creating user failed, no rows affected.");
                 }
  
                 try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                     if (generatedKeys.next()) {
                    	 ID_insert_of_insert = generatedKeys.getLong(1);
                     }
                     else {
                         throw new SQLException("Creating user failed, no ID obtained.");
                     }
                 }
             }catch (Exception e) {
            	 System.out.println("Exception : " +e.getMessage());
	             return 1;
             }
         System.out.println("Correu tudo bem até aqui!");
         System.out.println("Devolvi "+ID_insert_of_insert);
         return ID_insert_of_insert;
    }
    
    private long insert_new_MEDICOS(
    		Long ID_UTILIZADOR,
    		String NOME_CLINICO,
    		Integer NORDEM,
    		String LOCAL_TRABALHO,
    		String ESPECIALIDADE) {
    	Connection conn = getConnection(); 
    	String SQL_INSERT = "Insert into MEDICOS_BEEP(ID_UTILIZADOR,NOME_CLINICO,NORDEM,LOCAL_TRABALHO,ESPECIALIDADE) VALUES (?,?,?,?,?)";
         try {
                 PreparedStatement statement = conn.prepareStatement(SQL_INSERT,Statement.RETURN_GENERATED_KEYS);
                 statement.setLong(1, ID_UTILIZADOR);
                 statement.setString(2, NOME_CLINICO);
                 statement.setInt(3, NORDEM);
                 statement.setString(4, LOCAL_TRABALHO);
                 statement.setString(5, ESPECIALIDADE);
                
                 int affectedRows = statement.executeUpdate();
  
                 if (affectedRows == 0) {
                     throw new SQLException("Creating user failed, no rows affected.");
                 }
  
             }catch (Exception e) {
            	 System.out.println("Exception : " +e.getMessage());
 	             return 0;
             }
         System.out.println("Correu tudo bem até aqui! #2parte");
         System.out.println("Devolvi "+ID_UTILIZADOR);
         return ID_UTILIZADOR;
    }
    
    private int ADD_MEDICO(String NOME,
            String NOME_CLINICO,
            String EMAIL,
            Integer TELEFONE,
            String PASSWORD,
            String DATA_NASC,
            Integer NORDEM,
            String LOCAL_TRABALHO,
            String ESPECIALIDADE,
            Integer PREVI)   {
        long ID_insert_of_insert = 0;
        long ID_UTILIZADOR_MEDICOS = 0;
       
        ID_insert_of_insert = insert_new_UTILIZADORES(
           		 NOME,
        		 DATA_NASC,
        		 TELEFONE,
        		 EMAIL,
        		 PREVI,
        		 PASSWORD);
       
        if(ID_insert_of_insert > 0) {
        	ID_UTILIZADOR_MEDICOS = insert_new_MEDICOS(
        			ID_insert_of_insert,
            		NOME_CLINICO,
            		NORDEM,
            		LOCAL_TRABALHO,
            		ESPECIALIDADE);
        	
        }else {
        	return 1;
        }
        System.out.println("Se chegou aqui nao devolveu erros de querry's");
        return 0;
    }
    
    
    //Funçoes para adicionar Secretaria
    //usa funçao inset new utilizador do grupo  funçoes para adicionar medico
    
    private long insert_new_SECRETARIA(Long ID_UTILIZADOR) {
    	Connection conn = getConnection(); 
    	String SQL_INSERT = "Insert into SECRETARIA_BEEP(ID_UTILIZADOR) VALUES (?)";
         try {
                 PreparedStatement statement = conn.prepareStatement(SQL_INSERT,Statement.RETURN_GENERATED_KEYS);
                 statement.setLong(1, ID_UTILIZADOR);

                
                 int affectedRows = statement.executeUpdate();
  
                 if (affectedRows == 0) {
                     throw new SQLException("Creating user failed, no rows affected.");
                 }
  
             }catch (Exception e) {
            	 System.out.println("Exception : " +e.getMessage());
 	             return 0;
             }
         System.out.println("Correu tudo bem até aqui! #2parte");
         System.out.println("Devolvi "+ID_UTILIZADOR);
         return ID_UTILIZADOR;
    }
    
    private int ADD_SECRETARIA(String NOME,
            String EMAIL,
            Integer TELEFONE,
            String PASSWORD,
            String DATA_NASC,
            Integer PREVI)   {
        long ID_insert_of_insert = 0;
        long ID_UTILIZADOR_MEDICOS = 0;
       
        ID_insert_of_insert = insert_new_UTILIZADORES(
           		 NOME,
        		 DATA_NASC,
        		 TELEFONE,
        		 EMAIL,
        		 PREVI,
        		 PASSWORD);
       
        if(ID_insert_of_insert > 0) {
        	ID_UTILIZADOR_MEDICOS = insert_new_SECRETARIA(
        			ID_insert_of_insert);
        	
        }else {
        	return 1;
        }
        System.out.println("Se chegou aqui nao devolveu erros de querry's");
        return 0;
    }
    
    
    //Função de validar login
    private long validateLogin(String username,String password) {

        Connection conn = getConnection();
        String SQL_SELECT = new String("SELECT  ID_UTILIZADOR,EMAIL, PASSWORD FROM UTILIZADORES_BEEP where EMAIL = ?");

        
        try {
        	 PreparedStatement statement = conn.prepareStatement(SQL_SELECT);
             statement.setString(1, username);
             	
             statement.executeQuery();
             ResultSet rs = statement.getResultSet();

           
            if (rs.next())
            {
                if(rs.getString("PASSWORD").equals(password)) {
                    System.out.println("Welcome " + rs.getString("EMAIL") );   
                    long id_user = rs.getLong("ID_UTILIZADOR");
                    return id_user;
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
    
    private int insert_new_LOG(Long ID_UTILIZADOR,
    		String DATA_HORA,
    		String ACTION,
    		String DEVICE_ID) {
    	Connection conn = getConnection(); 
    	String SQL_INSERT = "Insert into LOGS_BEEP(ID_UTILIZADOR,DATA_HORA,ACTION,DEVICE_ID,IP_CLIENTE) VALUES (?,?,?,?,?)";
         try {
                 PreparedStatement statement = conn.prepareStatement(SQL_INSERT,Statement.RETURN_GENERATED_KEYS);
                 statement.setLong(1, ID_UTILIZADOR);
                 statement.setString(2, DATA_HORA);
                 statement.setString(3, ACTION);
                 statement.setString(4, DEVICE_ID);
                 statement.setString(5, "localhost");
                 
                 int affectedRows = statement.executeUpdate();
  
                 if (affectedRows == 0) {
                     throw new SQLException("Creating user failed, no rows affected.");
                 }
  
             }catch (Exception e) {
            	 System.out.println("Exception : " +e.getMessage());
 	             return 0;
             }
    	
			return 1;
    	
    }
    
    private int VALIDAR_LOGIN(String LOGIN,
    		String PASSWORD,
    		String DATA_HORA,
    		String DEVICE_ID) {
    	long id =validateLogin(LOGIN,PASSWORD);
    	if (id !=0) {
    		 int info =insert_new_LOG(id,DATA_HORA,"Login", DEVICE_ID);
    		 if (info == 1) {
    			return 1; 
    		 }else {
    			 return 0;
    		 }
    		
    	}else {
    		return 0;
    	}
    	
    }
    
   //Criar Conta
    private int CRIAR_CONTA(String NOME,
    		String DATA_NASC,
    		Integer TELEFONE,
    		String EMAIL,
    		Integer PREVI,
    		String PASSWORD) {
    	long id = insert_new_UTILIZADORES(NOME,DATA_NASC,TELEFONE,EMAIL,PREVI,PASSWORD);
    	if (id > 0){
    		return 1;
    	}else {
    		return 0;
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
        clients.put("success", true);
        return Response.status(200).entity(clients.toString()).build();
    }
}