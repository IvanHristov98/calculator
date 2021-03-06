package com.calculator.web.wrappers.db.dao;

import com.calculator.web.wrappers.db.dao.dbMappers.CalculationResult;
import com.calculator.web.wrappers.db.dao.dbMappers.CalculationStatus;

import static com.calculator.web.wrappers.db.dao.dbMappers.tables.CalculationResultsTable.*;
import static com.calculator.web.wrappers.db.MockedPersistenceProperties.*;
import static com.calculator.web.wrappers.db.MockedPersistencePropertyNames.*;

import org.junit.*;
import org.mockito.Mock;

import java.sql.*;
import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.dbunit.*;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.database.*;
import org.dbunit.dataset.*;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

public class CalculationResultsDaoTest {	
	@Mock private Timestamp mockedTimestamp;
	private EntityManager entityManager;
	private CalculationResultsDao calculationResultsDao;
	
	@BeforeClass
	public static void setUpClass() throws SQLException {
		Connection connection = getJdbcConnection();
		
		CalculationResultsTable dbSetter = new CalculationResultsTable(connection);
		dbSetter.create();
		
		connection.close();
	}
	
	@AfterClass
	public static void tearDownClass() throws SQLException {
		Connection connection = getJdbcConnection();
		
		CalculationResultsTable dbSetter = new CalculationResultsTable(connection);
		dbSetter.drop();
		
		connection.close();
	}
	
    @Before
    public void setUp() throws Exception {
    	initMocks(this);
    	
    	applyDataSet(Datasets.EMPTY_DATA_SET.getPath());
    	setUpEntityManger();
    	setUpCalculationResultsDao(entityManager);
    }
    
    @After
    public void tearDown() throws SQLException {
    	tearDownEntityManager();
    	restartCalculationResultsAutoIncrementation();
    }
    
	@Test
	public void verifyItemFinding() throws Exception {
		applyDataSet(Datasets.PENDING_SINGLE_ITEM_DATA_SET.getPath());
		
		final int requestId = 1;
		final String email = "test@mail.com";
		
		CalculationResult calculationResult = new CalculationResult();
		calculationResult.setRequestId(requestId);
		calculationResult.setEmail(email);
		
		CalculationResult result = calculationResultsDao.getItem(calculationResult);
		
		assertThat(result, notNullValue());
		assertThat(result.getRequestId(), equalTo(calculationResult.getRequestId()));
	}
	
	@Test
	public void verifyInvalidItemFindingRequest() throws Exception {
		applyDataSet(Datasets.PENDING_SINGLE_ITEM_DATA_SET.getPath());
		
		CalculationResult calculationResult = new CalculationResult();
		calculationResult.setRequestId(1);
		calculationResult.setEmail("wrong@mail.com");
		
		assertThat(calculationResultsDao.getItem(calculationResult), is(nullValue()));
	}
	
	@Test
	public void verifyNumberWhenGettingItems() throws Exception {
		applyDataSet(Datasets.PENDING_TWO_ITEMS_DATA_SET.getPath());
		final int totalRecordsInTable = 2;
		
		CalculationResult calculationResult = new CalculationResult();
		calculationResult.setEmail("test@mail.com");
		
		List<CalculationResult> result = calculationResultsDao.getItems(calculationResult);
		
		assertThat(result.size(), equalTo(totalRecordsInTable));
	}
	
	@Test
	public void verifyItemSaving() throws SQLException, DatabaseUnitException {
		CalculationResult item = new CalculationResult();
		item.setExpression("1+1");
		item.setMoment(mockedTimestamp);
		item.setEvaluation(2.0d);
		item.setStatus(CalculationStatus.PENDING);
		item.setEmail("test@mail.com");
		
		calculationResultsDao.save(item);
		
		compareActualToCurrentTable(Datasets.PENDING_SINGLE_ITEM_DATA_SET.getPath());
	}
	
	@Test
	public void verifyItemUpdating() throws Exception {
		applyDataSet(Datasets.PENDING_WRONG_SINGLE_ITEM_DATA_SET.getPath());
		
		CalculationResult item = new CalculationResult();
		item.setRequestId(1);
		item.setExpression("1+1");
		item.setMoment(mockedTimestamp);
		item.setEvaluation(2.0d);
		item.setStatus(CalculationStatus.PENDING);
		item.setEmail("test@mail.com");
		
		calculationResultsDao.update(item);
		
		compareActualToCurrentTable(Datasets.PENDING_SINGLE_ITEM_DATA_SET.getPath());
	}
	
	@Test
	public void verifyItemDeletion() throws Exception {
		applyDataSet(Datasets.PENDING_SINGLE_ITEM_DATA_SET.getPath());
		
		CalculationResult item = new CalculationResult();
		item.setRequestId(1);
		item.setExpression("1+1");
		item.setStatus(CalculationStatus.PENDING);
		item.setMoment(mockedTimestamp);
		item.setEmail("test@mail.com");
		
		calculationResultsDao.delete(item);
		
		compareActualToCurrentTable(Datasets.EMPTY_DATA_SET.getPath());
	}
	
	@Test
	public void verifyPendingItemsGetting() throws Exception {
		applyDataSet(Datasets.PENDING_SINGLE_ITEM_DATA_SET.getPath());
		final int totalPendingRecordsInTable = 1;
		
		List<CalculationResult> pendingItems = calculationResultsDao.getPendingItems();
		
		assertThat(pendingItems.size(), equalTo(totalPendingRecordsInTable));
	}
	
	private void restartCalculationResultsAutoIncrementation() throws SQLException {
		Connection connection = getJdbcConnection();
    	CalculationResultsTable dbSetter = new CalculationResultsTable(connection);
    	dbSetter.restartAutoIncrementation();
	}
	
	private void applyDataSet(String dataSetFileName) throws Exception {
		IDatabaseTester databaseTester = new JdbcDatabaseTester(JDBC_DRIVER.getValue(), JDBC_URL.getValue(), JDBC_USER.getValue(), JDBC_PASSWORD.getValue());
        
        IDataSet dataSet = getDataSet(dataSetFileName);
        databaseTester.setDataSet(dataSet);
        databaseTester.onSetup();
	}
	
	private IDataSet getDataSet(String dataSetFileName) throws DataSetException {
		return new FlatXmlDataSetBuilder().build(getClass().getResourceAsStream(dataSetFileName));
	}
	
	private void setUpEntityManger() {
		Map<String, String> persistenceMap = new HashMap<>();
		persistenceMap.put(JDBC_URL_PROPERTY_NAME.getValue(), JDBC_URL.getValue());
		persistenceMap.put(JDBC_USER_PROPERTY_NAME.getValue(), JDBC_USER.getValue());
		persistenceMap.put(JDBC_PASSWORD_PROPERTY_NAME.getValue(), JDBC_PASSWORD.getValue());
		persistenceMap.put(JDBC_DRIVER_PROPERTY_NAME.getValue(), JDBC_DRIVER.getValue());
		
		EntityManagerFactory managerFactory = Persistence.createEntityManagerFactory("CalculationResults", persistenceMap);
		entityManager = managerFactory.createEntityManager();
	}
	
	private void setUpCalculationResultsDao(EntityManager entityManager) {
		calculationResultsDao = new CalculationResultsDao(entityManager);
	}
	
	private void tearDownEntityManager() {
		entityManager.close();
	}
	
	private void compareActualToCurrentTable(String expecteDataSetFileName) throws SQLException, DatabaseUnitException {
		IDatabaseConnection connection = getDatabaseConnection();
		
		IDataSet databaseDataSet = connection.createDataSet(new String[] {TABLE_NAME});
		IDataSet actualDataSet = new CachedDataSet(databaseDataSet);
        IDataSet expectedDataSet = getDataSet(expecteDataSetFileName);
        
        ITable expectedTable = expectedDataSet.getTable(TABLE_NAME);
        ITable actualTable = actualDataSet.getTable(TABLE_NAME);
        ITable filteredActualTable = DefaultColumnFilter.includedColumnsTable(actualTable, expectedTable.getTableMetaData().getColumns());
        
        verifyTablesEquality(expectedTable, filteredActualTable);
	}
	
	private IDatabaseConnection getDatabaseConnection() throws DatabaseUnitException, SQLException {
		Connection jdbcConnection = getJdbcConnection();
		return new DatabaseConnection(jdbcConnection);
	}
	
	private static Connection getJdbcConnection() throws SQLException {
		return DriverManager.getConnection(JDBC_URL.getValue(), JDBC_USER.getValue(), JDBC_PASSWORD.getValue());
	}
	
	private void verifyTablesEquality(ITable expectedTable, ITable actualTable) throws DatabaseUnitException {
		Assertion.assertEquals(expectedTable, actualTable);
	}
}
