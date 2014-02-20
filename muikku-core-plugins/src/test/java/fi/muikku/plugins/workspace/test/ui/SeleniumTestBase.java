package fi.muikku.plugins.workspace.test.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openqa.selenium.remote.RemoteWebDriver;

import fi.muikku.model.workspace.WorkspaceEntityCompact;
import fi.muikku.plugins.material.model.HtmlMaterialCompact;
import fi.muikku.plugins.workspace.model.WorkspaceMaterialCompact;
import fi.muikku.plugins.workspace.model.WorkspaceRootFolderCompact;
import fi.muikku.schooldata.entity.WorkspaceCompact;
import fi.muikku.test.TestSqlFiles;

public abstract class SeleniumTestBase {
  
  @Rule
  public TestName testName = new TestName();
  
  // Source: http://en.wikipedia.org/wiki/Pangram
  public static final String PANGRAM_ENGLISH = "The quick brown fox jumps over the lazy dog";
  public static final String PANGRAM_POLISH = "Mężny bądź, chroń pułk twój i sześć flag";
  public static final String PANGRAM_DUTCH = "Lynx c.q. vos prikt bh: dag zwemjuf!";
  public static final String PANGRAM_GERMAN = "Victor jagt zwölf Boxkämpfer quer über den großen Sylter Deich";
  public static final String PANGRAM_FRENCH = "Portez ce vieux whisky au juge blond qui fume";
  public static final String PANGRAM_TURKISH = "Pijamalı hasta yağız şoföre çabucak güvendi";
  public static final String PANGRAM_SPANISH = "El veloz murciélago hindú comía feliz cardillo y kiwi. La cigüeña tocaba el saxofón detrás del palenque de paja.";
  public static final String PANGRAM_SWEDISH = "Flygande bäckasiner söka strax hwila på mjuka tuvor.";
  public static final String PANGRAM_RUSSIAN = "Любя, съешь щипцы, — вздохнёт мэр, — кайф жгуч";
  public static final String PANGRAM_CZECH = "Nechť již hříšné saxofony ďáblů rozzvučí síň úděsnými tóny waltzu, tanga a quickstepu.";
  public static final String PANGRAM_GREEK = "Ξεσκεπάζω την ψυχοφθόρα βδελυγμία.";
  public static final String PANGRAM_LITHUANIAN = "Įlinkusi fechtuotojo špaga blykčiodama gręžė apvalų arbūzą.";
  public static final String PANGRAM_HEBREW = "עטלף אבק נס דרך מזגן שהתפוצץ כי חם.‎";
  public static final String PANGRAM_CATALAN = "Jove xef, porti whisky amb quinze glaçons d'hidrogen, coi!";
  public static final String PANGRAM_IGBO = "Nne, nna, wepụ he'l'ụjọ dum n'ime ọzụzụ ụmụ, vufesi obi nye Chukwu, ṅụrịanụ, gbakọọnụ kpaa, kwee ya ka o guzoshie ike; ọ ghaghị ito, nwapụta ezi agwa.";
  public static final String PANGRAM_YORUBA = "Ìwò̩fà ń yò̩ séji tó gbojúmó̩, ó hàn pákànpò̩ gan-an nis̩é̩ rè̩ bó dò̩la.";
  public static final String PANGRAM_AFRIKAANS = "Jong Xhosas of Zoeloes wil hou by die status quo vir chemie in die Kaap";
  public static final String PANGRAM_JAPANESE = "いろはにほへと ちりぬるを わかよたれそ つねならむ うゐのおくやま けふこえて あさきゆめみし ゑひもせすん";
  public static final String PANGRAM_INDONESIAN = "Hafiz mengendarai bajaj payah-payah ke warnet-x untuk mencetak lembar verifikasi dalam kertas quarto";
  // Source: http://fi.wikipedia.org/wiki/Pangrammi#Suomi
  public static final String PANGRAM_FINNISH = "Charles Darwin jammaili Åken hevixylofonilla Qatarin yöpub Zeligissä.";

  private static final String PROTOCOL = "http";
  private static final String STUDENT1_USERNAME = "st1@oo.fi";
  private static final String STUDENT1_PASSWORD = "qwe";
  
  protected String getHostname() {
    return System.getProperty("integrationtest.serverhost");
  }
  
  protected String getContextPath() {
    return "/" + System.getProperty("integrationtest.contextpath");
  }
  
  protected int getPort() {
    return Integer.parseInt(System.getProperty("integrationtest.serverport"), 10);
  }
  
  protected URL getAppUrl(String path) throws MalformedURLException {
    return new URL(PROTOCOL, getHostname(), getPort(), getContextPath() + path);
  }

  protected URI getAppUri(String path) throws MalformedURLException, URISyntaxException {
    return getAppUrl(path).toURI();
  }
  
  protected String getStudent1Username() {
    return STUDENT1_USERNAME;
  }
  
  protected String getStudent1Password() {
    return STUDENT1_PASSWORD;
  }

  @Before
  public void baseSetUp() throws Exception {
    Logger.getLogger(getClass().getCanonicalName()).log(Level.INFO, "Starting setUp.");
    Connection connection = getConnection();
    try {
      String[] files = new String[] {};

      Method method = getClass().getMethod(testName.getMethodName(), new Class<?>[] {});
      if (method != null) {
        TestSqlFiles annotation = method.getAnnotation(TestSqlFiles.class);
        if (annotation != null) {
          files = annotation.value();
        }
        Logger.getLogger(getClass().getCanonicalName()).log(Level.INFO, "Processing files: " + files.toString());
      }
      
      for (String file : files) {
        runSql(connection, "sql/" + file + "-setup.sql");
      }
      
      connection.commit();
      Logger.getLogger(getClass().getCanonicalName()).log(Level.INFO, "Committed setUp.");
    } finally {
      connection.close();
    }
  }

  @After
  public void baseTearDown() throws Exception {
    Logger.getLogger(getClass().getCanonicalName()).log(Level.INFO, "Starting tearDown.");
    driver.quit();

    Connection connection = getConnection();
    try {
      String[] files = new String[] {};
      
      Method method = getClass().getMethod(testName.getMethodName(), new Class<?>[] {});
      if (method != null) {
        files = method.getAnnotation(TestSqlFiles.class).value();
        Logger.getLogger(getClass().getCanonicalName()).log(Level.INFO, "Processing files: " + files.toString());
      }
      
      List<String> filesList = Arrays.asList(files);
      Collections.reverse(filesList);
      
      for (String file : filesList) {
        runSql(connection, "sql/" + file + "-teardown.sql");
      }
      
      connection.commit();
      Logger.getLogger(getClass().getCanonicalName()).log(Level.INFO, "Committed tearDown.");
    } finally {
      connection.close();
    }
  }

  private Connection getConnection() throws SQLException, ClassNotFoundException {
    String driver = System.getProperty("integrationtest.datasource.jdbc.muikku.driver");
    String url = System.getProperty("integrationtest.datasource.jdbc.muikku.url");
    String username = System.getProperty("integrationtest.datasource.jdbc.muikku.username");
    String password = System.getProperty("integrationtest.datasource.jdbc.muikku.password");
    
    System.out.println(driver);
    System.out.println(url);
    System.out.println(username);
    System.out.println(password);
    
    System.out.println(new File(".").getAbsolutePath());
    
    Class.forName(driver);
    return DriverManager.getConnection(url, username, password);
  }
  
  private void runSql(Connection connection, String file) throws IOException, SQLException {
    ClassLoader classLoader = getClass().getClassLoader();
    InputStream sqlStream = classLoader.getResourceAsStream(file);
    if (sqlStream != null) {
      try {
        String sqlString = IOUtils.toString(sqlStream);
        
        Pattern commentPattern = Pattern.compile("--.*$", Pattern.MULTILINE);
        sqlString = commentPattern.matcher(sqlString).replaceAll("");
        
        String[] sqls = sqlString.split(";(?=([^\']*\'[^\']*\')*[^\']*$)"); // Quote-aware split on ';'
        for (String sql : sqls) {
          sql = sql.trim();
          if (StringUtils.isNotBlank(sql)) {
            Statement statement = connection.createStatement();
            statement.execute(sql);
            Logger.getLogger(getClass().getCanonicalName()).log(Level.INFO, "Executing test SQL: " + sql);
          }
        }
        
      } finally {
        sqlStream.close();
      }
    } else {
      throw new FileNotFoundException(file);
    }
  }

  protected WorkspaceCompact createWorkspace(String schoolDataSource, String name, String description, String workspaceTypeId, String courseIdentifierIdentifier) throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException {
    WorkspaceCompact workspace = new WorkspaceCompact();
    workspace.setCourseIdentifierIdentifier(courseIdentifierIdentifier);
    workspace.setDescription(description);
    workspace.setSchoolDataSource(schoolDataSource);
    workspace.setWorkspaceTypeId(workspaceTypeId);
    workspace.setName(name);

    ObjectMapper objectMapper = new ObjectMapper();
    String data = objectMapper.writeValueAsString(workspace);  

    String resultString = restPostRequest("/workspace/workspaces/", data).getContent();
    return objectMapper.readValue(resultString, WorkspaceCompact.class);
  }
  
  protected WorkspaceEntityCompact getWorkspaceEntity(String schoolDataSource, String identifier) throws JsonParseException, JsonMappingException, IOException, URISyntaxException {
    String resultString = restGetRequest("/workspace/workspaceEntities/?schoolDataSource=" + schoolDataSource + "&identifier=" + identifier).getContent();
    
    ObjectMapper objectMapper = new ObjectMapper();
    WorkspaceEntityCompact[] workspaceEntities = objectMapper.readValue(resultString, WorkspaceEntityCompact[].class);
    if (workspaceEntities != null && workspaceEntities.length == 1) {
      return workspaceEntities[0];
    }
    
    return null;
  }
  
  protected WorkspaceEntityCompact getWorkspaceEntity(WorkspaceCompact workspace) throws JsonParseException, JsonMappingException, IOException, URISyntaxException {
    return getWorkspaceEntity(workspace.getSchoolDataSource(), workspace.getIdentifier());
  }
  
  protected WorkspaceRootFolderCompact getWorkspaceRootFolder(WorkspaceCompact workspace) throws JsonParseException, JsonMappingException, IOException, URISyntaxException {
    WorkspaceEntityCompact workspaceEntity = getWorkspaceEntity(workspace);
    if (workspaceEntity == null) {
      return null;
    }
    
    String resultString = restGetRequest("/workspace/nodes/?workspaceEntityId=" + workspaceEntity.getId()).getContent();
    
    ObjectMapper objectMapper = new ObjectMapper();
    WorkspaceRootFolderCompact[] rootFolders = objectMapper.readValue(resultString, WorkspaceRootFolderCompact[].class);
    if (rootFolders != null && rootFolders.length == 1) {
      return rootFolders[0];
    }
    
    return null;
  }
  
  protected HtmlMaterialCompact createHtmlMaterial(String urlName, String title, String html) throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException {
    HtmlMaterialCompact htmlMaterial = new HtmlMaterialCompact();
    htmlMaterial.setHtml(html);
    htmlMaterial.setTitle(title);
    htmlMaterial.setUrlName(urlName);
    
    ObjectMapper objectMapper = new ObjectMapper();
    String data = objectMapper.writeValueAsString(htmlMaterial);  

    String resultString = restPostRequest("/materials/html/", data).getContent();
    return objectMapper.readValue(resultString, HtmlMaterialCompact.class);
  }
  
  protected WorkspaceMaterialCompact createWorkspaceMaterial(Long materialId, Long parentId, String urlName) throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException {
    WorkspaceMaterialCompact workspaceMaterial = new WorkspaceMaterialCompact();
    
    workspaceMaterial.setMaterial_id(materialId);
    workspaceMaterial.setParent_id(parentId);
    workspaceMaterial.setUrlName(urlName);
    
    ObjectMapper objectMapper = new ObjectMapper();
    String data = objectMapper.writeValueAsString(workspaceMaterial);  

    String resultString = restPostRequest("/workspace/materials/", data).getContent();
    return objectMapper.readValue(resultString, WorkspaceMaterialCompact.class);
  }
  
  protected WorkspaceMaterialCompact createWorkspaceMaterial(WorkspaceCompact workspace, Long materialId, String urlName) throws JsonParseException, JsonMappingException, IOException, URISyntaxException {
    WorkspaceRootFolderCompact rootFolder = getWorkspaceRootFolder(workspace);
    if (rootFolder != null) {
      return createWorkspaceMaterial(materialId, rootFolder.getId(), urlName);
    }
    
    return null;
  }

  protected void deleteWorkspaceMaterial(WorkspaceMaterialCompact workspaceMaterial) {
    // TODO Auto-generated method stub
    
  }

  protected void deleteHtmlMaterial(HtmlMaterialCompact htmlMaterial) {
    // TODO Auto-generated method stub
    
  }

  protected void deleteWorkspace(WorkspaceCompact workspace) {
    // TODO Auto-generated method stub
  }
  
  private RestResponse restGetRequest(String path) throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException {
    HttpGet httpGet = new HttpGet(getAppUri("/rest" + path));
    return executeRestRequest(httpGet);
  }
  
  private RestResponse restPostRequest(String path, String data) throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException {
    HttpPost httpPost = new HttpPost(getAppUri("/rest" + path));
    httpPost.setEntity(new StringEntity(data, "UTF-8"));
    
    return executeRestRequest(httpPost);
  } 

  private RestResponse executeRestRequest(HttpRequestBase httpRequest) throws IOException, ClientProtocolException {
    HttpClient client = HttpClientBuilder.create().build();

    httpRequest.setHeader("Content-Type", " application/json");
    httpRequest.setHeader("Accept", " application/json");

    HttpResponse response = client.execute(httpRequest);
    HttpEntity entity = response.getEntity();
    
    try {
      int status = response.getStatusLine().getStatusCode();
      if (status == 204) {
        return new RestResponse(status, null);
      }
      
      return new RestResponse(status, IOUtils.toString(entity.getContent()));
    } finally {
      EntityUtils.consume(entity);
    }
  } 
  
  protected void setDriver(RemoteWebDriver driver) {
    this.driver = driver;
  }

  protected RemoteWebDriver getDriver() {
    return driver;
  }
  
  private RemoteWebDriver driver;
  
  private class RestResponse {
    
    public RestResponse(int statusCode, String content) {
      this.statusCode = statusCode;
      this.content = content;
    }
    
    @SuppressWarnings("unused")
    public int getStatusCode() {
      return statusCode;
    }
    
    public String getContent() {
      return content;
    }
    
    private int statusCode;
    private String content;
  }
}
