package com.calculator.web.tests;

import com.calculator.web.tests.archive.WebCalculatorArchiveFactory;
import com.calculator.web.tests.pageObjects.db.DatabasePage;
import com.calculator.web.tests.pageObjects.resources.CalculateResourcePage;

import static com.calculator.web.tests.DatasetPaths.*;

import java.io.*;
import java.net.URL;
import java.sql.*;

import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import org.junit.*;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(Arquillian.class)
public class CalculateResourceIT {
	
	public static DatabasePage dbPage;
	
	@ClassRule
	public static final EnvironmentVariables environmentVariables = new EnvironmentVariables();
	
	@ArquillianResource
	private URL baseUrl;
	private CalculateResourcePage resourcePage;
	
    @Deployment(testable = false)
    public static WebArchive createDeployment() {
    	WebCalculatorArchiveFactory webCalculatorArchive = new WebCalculatorArchiveFactory();
        return webCalculatorArchive.makeArchive();
    }
    
    @BeforeClass
    public static void setUpClass() throws Exception {
    	EnvironmentVariablesMocker environmentVariablesMocker = new EnvironmentVariablesMocker(environmentVariables);
    	environmentVariablesMocker.mockEnvironmentVariables();
    	
    	dbPage = new DatabasePage();
    	dbPage.startDatabaseServer();
    	dbPage.createSchema();
    }
    
    @AfterClass
    public static void tearDownClass() throws Exception {
    	dbPage.dropTable();
    	dbPage.shutDownDatabaseServer();
    }
    
    @Before
    public void setUp() throws Exception {
    	resourcePage = new CalculateResourcePage(baseUrl);
    	dbPage.useDataSet(EMPTY_DATA_SET);
    }
    
    @After
    public void tearDown() throws SQLException {
    	dbPage.restartAutoIncrementation();
    }
 
    @Test
    public void calculateExpressionAcceptance() throws IOException, InterruptedException, SQLException {
    	resourcePage.setExpressionParameter("(1+2)*3 + 2^2 + 4/2");
    	Response calculationResponse = resourcePage.getResourceContent();
    	
    	assertThat(calculationResponse.getStatus(), equalTo(Response.Status.ACCEPTED.getStatusCode()));
    }
    
    @Test
    public void verifyCalculationSaving() throws Exception {
    	resourcePage.setExpressionParameter("1+1");
    	resourcePage.getResourceContent();
    	dbPage.compareActualToExpectedTable(PENDING_SINGLE_ITEM_DATA_SET);
    }
}