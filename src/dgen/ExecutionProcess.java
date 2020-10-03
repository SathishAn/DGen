package dgen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;
import java.util.Map.Entry;
import javax.net.ssl.*;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.codoid.products.exception.FilloException;
import com.codoid.products.fillo.Fillo;
import com.codoid.products.fillo.Recordset;

import javafx.collections.ObservableList;

public class ExecutionProcess implements Runnable {
	static Fillo fillo = new Fillo();
	private static String User_Agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36";
	public static HashMap<String, String> testData;
	public static HashMap<String, String> result;
	public static HashMap<String, String> cookies;
	public static HashMap<String, String> environData;
	public static HashMap<String, Integer> iteration;
	private final String propertyFilePath = "./configs/config.properties";
	private static String iRegion;
	private static String appUrl;
	private static ObservableList<Modules> moduleList;
	private static String reportPath,reportfile, fileName , logFile;
	private static boolean status;
	private static boolean columnFlag;
	private static Logger logs = Logger.getLogger(ExecutionProcess.class);
	
	public ExecutionProcess(ObservableList<Modules> moduleList) {
		
		try {
			//this.moduleList = moduleList;
			
			environData = new HashMap<>();
			BufferedReader reader = new BufferedReader(new FileReader(propertyFilePath));
			Properties properties = new Properties();
			properties.load(reader);
			for (Entry<Object, Object> prop : properties.entrySet()) {
				if(prop.getValue().toString().startsWith("Encrypted :~")) {
					String encryptedString = EncryptionFile.decrypt(prop.getValue().toString().replace("Encrypted :~", "").toString());
					environData.put(prop.getKey().toString(), encryptedString);
					System.out.println(encryptedString);
					
				}else {
					environData.put(prop.getKey().toString(), prop.getValue().toString());
				}
				
				appUrl = appUrl;
			}

		} catch (FileNotFoundException e) {

			logs.error(e.getMessage());
		} catch (IOException e) {

			logs.error(e.getMessage());
		}
	}


	/*****************************************************************************
	 * 
	 * Launch the Application
	 * 
	 *****************************************************************************/
	public static void loadUrl() {
		try {
			enableSSLSocket();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			logs.error(e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			logs.error(e.getMessage());
		}
		cookies = new HashMap<>();
		getService(appUrl);
	}

	/*****************************************************************************
	 * 
	 * Login with Maker user to the Application
	 * 
	 *****************************************************************************/
	public static void makerLogin() {
		String userName = null, password = null;
		if(iRegion.equals("CH")) {
			userName = environData.get("makerCHUserName");
			password = environData.get("makerCHPassword");
		}else if(iRegion.equals("US")) {
			userName = environData.get("makerUSUserName");
			password = environData.get("makerUSPassword");
		}
		testData = new HashMap<>();
		testData.put("command", "globusCommand");
		testData.put("requestType", "CREATE.SESSION");
		testData.put("signOnName", userName);
		testData.put("password", password);
		Document response = loginPostService(appUrl, testData);
//		getDate();
	}
	
	public static void getDate() {
		testData.put("command", "globusCommand");
		testData.put("routineArgs", "BANNER");
		testData.put("routineName", "OS.NEW.USER");
		Document response = postService(appUrl, testData);
		System.out.println(response.html());
		Elements date = response.select("input[id=\"today\"]");
		String today = date.attr("value");
		System.out.println(today);
	}

	/*****************************************************************************
	 * 
	 * Login with authorize user to the Application
	 * 
	 *****************************************************************************/
	public static void authorizerLogin() {
		String userName = null, password = null;
		if(iRegion.equals("CH")) {
			userName = environData.get("authoriseCHUser");
			password = environData.get("authoriseCHPassword");
		}else if(iRegion.equals("US")) {
			userName = environData.get("authoriseUSUser");
			password = environData.get("authoriseUSPassword");
		}
		testData = new HashMap<>();
		testData.put("command", "globusCommand");
		testData.put("requestType", "CREATE.SESSION");
		testData.put("signOnName", userName);
		testData.put("password", password);
		loginPostService(appUrl, testData);
	}

	/*****************************************************************************
	 * 
	 * Create A New Customer
	 * 
	 *****************************************************************************/
	public static String initializeCutomer() {
		
		String transactionId;
		loadUrl();
		makerLogin();
		extractFormData("InitCustomer");
		Document response = postService(appUrl, testData);
		Elements TransactionID = response.select("input[id=\"transactionId\"]");
		transactionId = TransactionID.attr("value");
		System.out.println(transactionId);
		return transactionId;
	}

	public static String inputCustomer(String wbName) {
		String transactionid = initializeCutomer();
		extractExcelData("ContainerCustomer", wbName);
		testData.put("transactionId", transactionid);
		testData.put("fieldName:NAME.1:1", testData.get("fieldName:NAME.1:1") + testData.get("transactionId"));
		testData.put("fieldName:MNEMONIC", testData.get("fieldName:NAME.1:1").substring(1, 1)
				+ testData.get("fieldName:SHORT.NAME:1").substring(1, 1) + testData.get("transactionId"));
		extractFormData("InputCustomer");
		Document response = postService(appUrl, testData);
		response = checkAndHandleWarning(response);
		response = checkAndHandleOverRides(response);
		getMessage(response);
		return transactionid;

	}
	
	public static String inputCustomer_fcm(String wbName) {
		String transactionid = initializeCutomer();
		extractExcelData("ContainerCustomer", wbName);
		testData.put("transactionId", transactionid);
		
		testData.put("fieldName:MNEMONIC", testData.get("fieldName:NAME.1:1").substring(1, 1)
				+ testData.get("fieldName:SHORT.NAME:1").substring(1, 1) + testData.get("transactionId"));
		extractFormData("InputCustomer");
		Document response = postService(appUrl, testData);
		response = checkAndHandleWarning(response);
		response = checkAndHandleOverRides(response);
		getMessage(response);
		return transactionid;

	}
	/*****************************************************************************
	 * 
	 * Create MMPlacement call notice
	 * 
	 *****************************************************************************/
	public static String initializeMMPlacementsCallNotice() {
		
		String transactionId;
		loadUrl();
		makerLogin();
		extractFormData("InitMMPlacementCallNotice");
		Document response = postService(appUrl, testData);
		Elements TransactionID = response.select("input[id=\"transactionId\"]");
		transactionId = TransactionID.attr("value");
		System.out.println(transactionId);
		return transactionId;
	}
	/*****************************************************************************
	 * 
	 * Create MMTakings call notice
	 * 
	 *****************************************************************************/
	public static String initializeMMTakingsCallNotice() {
		
		String transactionId;
		loadUrl();
		makerLogin();
		extractFormData("InitMMTakingCallNotice");
		Document response = postService(appUrl, testData);
		Elements TransactionID = response.select("input[id=\"transactionId\"]");
		transactionId = TransactionID.attr("value");
		System.out.println(transactionId);
		return transactionId;
	}
	/*****************************************************************************
	 * 
	 * Create MMPlacement Fixed Maturity
	 * 
	 *****************************************************************************/
	public static String initializeMMPlacementFixedMaturity() {
		
		String transactionId;
		loadUrl();
		makerLogin();
		extractFormData("InitMMPlacementFixedMaturity");
		Document response = postService(appUrl, testData);
		Elements TransactionID = response.select("input[id=\"transactionId\"]");
		transactionId = TransactionID.attr("value");
		System.out.println(transactionId);
		return transactionId;
	}
	/*****************************************************************************
	 * 
	 * Create MMPlacement Fixed Maturity
	 * 
	 *****************************************************************************/
	public static String initializeMMTakingFixedMaturity() {
		
		String transactionId;
		loadUrl();
		makerLogin();
		extractFormData("InitMMTakingFixedMaturity");
		Document response = postService(appUrl, testData);
		Elements TransactionID = response.select("input[id=\"transactionId\"]");
		transactionId = TransactionID.attr("value");
		System.out.println(transactionId);
		return transactionId;
	}
	/*****************************************************************************
	 * 
	 * Create initialize swap irs tradedeal
	 * 
	 *****************************************************************************/
	public static String initializeIRSTradeDeal() {
		
		String transactionId;
		loadUrl();
		makerLogin();
		extractFormData("InitSwapIRSTradeDeal");
		Document response = postService(appUrl, testData);
		Elements TransactionID = response.select("input[id=\"transactionId\"]");
		transactionId = TransactionID.attr("value");
		System.out.println(transactionId);
		return transactionId;
	}
	/*****************************************************************************
	 * 
	 * Create initialize swap irs hedgedeal
	 * 
	 *****************************************************************************/
	public static String initializeIRSHedgeDeal() {
		
		String transactionId;
		loadUrl();
		makerLogin();
		extractFormData("InitSwapIRSHedgeDeal");
		Document response = postService(appUrl, testData);
		Elements TransactionID = response.select("input[id=\"transactionId\"]");
		transactionId = TransactionID.attr("value");
		System.out.println(transactionId);
		return transactionId;
	}
	/*****************************************************************************
	 * 
	 * Create initialize swap cirs tradedeal
	 * 
	 *****************************************************************************/
	public static String initializeCIRSTradeDeal() {
		
		String transactionId;
		loadUrl();
		makerLogin();
		extractFormData("InitSwapCIRSTradeDeal");
		Document response = postService(appUrl, testData);
		Elements TransactionID = response.select("input[id=\"transactionId\"]");
		transactionId = TransactionID.attr("value");
		System.out.println(transactionId);
		return transactionId;
	}
	
	/*****************************************************************************
	 * 
	 * Create initialize swap cirs hedgedeal
	 * 
	 *****************************************************************************/
	public static String initializeCIRSHedgeDeal() {
		
		String transactionId;
		loadUrl();
		makerLogin();
		extractFormData("InitSwapCIRSHedgeDeal");
		Document response = postService(appUrl, testData);
		Elements TransactionID = response.select("input[id=\"transactionId\"]");
		transactionId = TransactionID.attr("value");
		System.out.println(transactionId);
		return transactionId;
	}
	/*****************************************************************************
	 * 
	 * Create initialize fixed fiduciary
	 * 
	 *****************************************************************************/
	public static String initializeFixedFiduciary() {
		
		String transactionId;
		loadUrl();
		makerLogin();
		extractFormData("InitFixedFiduciary");
		Document response = postService(appUrl, testData);
		Elements TransactionID = response.select("input[id=\"transactionId\"]");
		transactionId = TransactionID.attr("value");
		System.out.println(transactionId);
		return transactionId;
	}
	/*****************************************************************************
	 * 
	 * Create initialize notice fiduciary
	 * 
	 *****************************************************************************/
	public static String initializeNoticeFiduciary() {
		
		String transactionId;
		loadUrl();
		makerLogin();
		extractFormData("InitNoticeFiduciary");
		Document response = postService(appUrl, testData);
		Elements TransactionID = response.select("input[id=\"transactionId\"]");
		transactionId = TransactionID.attr("value");
		System.out.println(transactionId);
		return transactionId;
	}
	//*****************************************************************************
	public static void checkErrorExist(Document response) {
			Elements error = response.select("td[class=\"errors\"]");
			if(!error.text().isEmpty()) {
				Elements errorText = response.select("td[class=\"errorText\"]");
				logs.error(error.text() + "-" + errorText.text());
				System.out.println(error.text() + " " + errorText.text());
			
			}
			
	}
	
	

	/*******************************************************************************************
	 * getMessage: To get the final transaction message
	 * 
	 * @param response
	 * 
	 ******************************************************************************************/

	public static String getMessage(Document response) {
		Elements message = response.select("td[class=\"message\"]");
		System.out.println(message.text());
		if(!message.text().isEmpty()) {
			if (!message.text().contains("Txn Complete")) {
				status = false;
			}
			logs.info(message.text());
		}else {
			checkErrorExist(response);
		}
		return message.text();
	}

	/******************************************************************************************
	 * checkAndHandleWarning: Check and handle the warning
	 * 
	 * @param response
	 * @return
	 * 
	 *****************************************************************************************/
	public static Document checkAndHandleWarning(Document response) {
		Elements warning = response.select("input[id=\"warningsPresent\"]");
		
		if (warning.attr("value").equals("YES")) {
			Elements overRide = response.select("td[class=\"overrideOn\"]");
			for(Element ov: overRide) {
				logs.warn("OverRides: " + ov.text() );
				testData.put("warningText:" + ov.text() + ":value", "NO");
			}
			response = postService(appUrl, testData);
			getMessage(response);
			}
		
		return response; 
	}

	/******************************************************************************************
	 * checkAndHandleWarning: Check and handle the OverRides
	 * 
	 * @param response
	 * @return
	 * 
	 *****************************************************************************************/
	public static Document checkAndHandleOverRides(Document response) {
		Elements warning = response.select("input[id=\"OverridesPresent\"]");
		if (warning.attr("value").equals("YES")) {
			Elements overRide = response.select("td[class=\"overrideOn\"]");
			for(Element ov: overRide) {
				logs.warn("OverRides: " + ov.text() );
				testData.put("overrideText:" + ov.text() + ":value", "YES");
			}
			response = postService(appUrl, testData);
			getMessage(response);
		}
		return response;
	}

	/**************************************************************************************
	 * 
	 * @MethodName authorizeCustomer
	 * 
	 *************************************************************************************/

	public static void authorizeCustomer(String transactionId) {
		loadUrl();
		authorizerLogin();
		extractFormData("AuthCustomer");
		testData.put("transactionId", transactionId);
		Document response = postService(appUrl, testData);
		getMessage(response);
	}

	/*****************************************************************************
	 * 
	 * Create DDA Account for the Customer
	 * 
	 * @return
	 * 
	 *****************************************************************************/
	public static String intializeAccount() {
		String transactionId;
		loadUrl();
		makerLogin();
		extractFormData("InitAccount");
		Document response = postService(appUrl, testData);
		Elements TransactionID = response.select("input[id=\"transactionId\"]");
		transactionId = TransactionID.attr("value");
		System.out.println(transactionId);
		return transactionId;
	}
	/*****************************************************************************
	 * 
	 * Create NOSTRO Account for the Customer
	 * 
	 * @return
	 * 
	 *****************************************************************************/
	public static String intializeNOSTROAccount() {
		String transactionId;
		loadUrl();
		makerLogin();
		extractFormData("InitNostroAccount");
		Document response = postService(appUrl, testData);
		Elements TransactionID = response.select("input[id=\"transactionId\"]");
		transactionId = TransactionID.attr("value");
		System.out.println(transactionId);
		return transactionId;
	}

	
	/****************************************************************************************************
	 * 
	 * Input DDA Cash Account
	 * 
	 ***************************************************************************************************/
	public static String inputAccountCreation(String customerID, String module, String wbName) {
		String transactionId = intializeAccount();
		if(module.equalsIgnoreCase("DDACashAccount")) {
			extractExcelData("DDACashAccount", wbName);
		}else if(module.equalsIgnoreCase("MoneyMarketAccount")) {
			extractExcelData("MoneyMarketAccount", wbName);
		}else if(module.equalsIgnoreCase("MarginAccount")) {
			extractExcelData("MarginAccount", wbName);
		}
		testData.put("transactionId", transactionId);
		testData.put("fieldName:CUSTOMER", customerID);
		extractFormData("InputAccount");
		Document response = postService(appUrl, testData);
		String message = getMessage(response);
		String[] id = message.split(" ");
		System.out.println(id[2]);
		transactionId= id[2];
		testData.put("transactionId", id[2]);
		return transactionId;
	}
	/****************************************************************************************************
	 * 
	 * Input AUM DDA Cash Account
	 * 
	 ***************************************************************************************************/	
	public static String inputAccountCreation(String wbName) {
		String transactionId = intializeAccount();
		extractFormData("InputAccount");
		extractExcelData("AUM_Account", wbName);
		testData.put("transactionId", transactionId);
		System.out.println(testData.get("fieldName:CUSTOMER"));
		Document response = postService(appUrl, testData);
		String message =getMessage(response);
		String[] id = message.split(" ");
		System.out.println(id[2]);
		transactionId= id[2];
		testData.put("transactionId", id[2]);
		return transactionId;
	}
	
	/****************************************************************************************************
	 * 
	 * Authorize Account
	 * 
	 ***************************************************************************************************/
	public static void authAccount(String accountID) {
		loadUrl();
		authorizerLogin();
		extractFormData("AuthAccount");
		testData.put("transactionId", accountID);
		Document response = postService(appUrl, testData);
		getMessage(response);
	}
	
	/****************************************************************************************************
	 * 
	 * Customer Charge
	 * 
	 ***************************************************************************************************/
	public static void customerCharge(String customerID) {
		loadUrl();
		makerLogin();
		extractFormData("CustomerCharge");
		testData.put("transactionId", customerID);
		testData.put("fieldName:DEPOSITORY.GROUP", "999");
		testData.put("fieldName:TR.ACT.GROUP:1:1", "999");
		Document response = postService(appUrl, testData);
		getMessage(response);
	}
	
	/****************************************************************************************************
	 * 
	 * Customer Security
	 * 
	 ***************************************************************************************************/
	public static void customerSecurity(String customerID, String module, String wbName) {
		loadUrl();
		makerLogin();
		extractFormData("CustomerSecurity");
		if(module.equalsIgnoreCase("CustomerSecurity")) {
			extractExcelData("CustomerSecurity", wbName);
		}else if(module.equalsIgnoreCase("Broker")) {
			extractExcelData("Broker", wbName);
		}else if(module.equalsIgnoreCase("Depository")) {
			extractExcelData("Depository", wbName);
		}
		testData.put("transactionId", customerID);
		Document response = postService(appUrl, testData);
		getMessage(response);
	}
	/****************************************************************************************************
	 * 
	 * Customer AUM Security
	 * 
	 ***************************************************************************************************/
	public static void customerSecurity(String wbName) {
		loadUrl();
		makerLogin();
		extractFormData("CustomerSecurity");
		extractExcelData("CustomerSecurity", wbName);
		Document response = postService(appUrl, testData);
		getMessage(response);
	}
	
	

	public static String createCustomerPortFolio(String customerID, String AccountNo, String wbName) {
		String portfolioId = customerID + "-1";
		loadUrl();
		makerLogin();
		extractFormData("CustomerPortFolio");
		extractExcelData("CustomerPortFolio", wbName);
		testData.put("transactionId", customerID);
		testData.put("fieldName:CUSTOMER.NUMBER", customerID);
		testData.put("fieldName:ACCOUNT.NOS:1", AccountNo);
		Document response = postService(appUrl, testData);
		checkAndHandleOverRides(response);
		getMessage(response);
		return portfolioId;
	}
	
	public static String createCustomerPortFolio(String wbName) {
		loadUrl();
		makerLogin();
		extractFormData("CustomerPortFolio");
		extractExcelData("AUM_CustomerPortFolio", wbName);
		String portfolioId = testData.get("transactionId") ;
		Document response = postService(appUrl, testData);
		checkAndHandleOverRides(response);
		getMessage(response);
		return portfolioId;

	}
	

	public static void createFeesTestData(String customerID, String AccountNo, String wbName) {
		
		loadUrl();
		makerLogin();
		extractFormData("FeesData");
		extractExcelData("FeesData", wbName);
		testData.put("transactionId", customerID);
		testData.put("fieldName:CUSTOMER.NUMBER", customerID);
		testData.put("fieldName:ACCOUNT.NOS:1", AccountNo);
		Document response = postService(appUrl, testData);
		checkAndHandleOverRides(response);
		getMessage(response);

	}
	
	/*****************************************************************************
	 * 
	 * Initialize A New counter party customer
	 * 
	 *****************************************************************************/
	public static String initializeCounterPartyCutomer() {
		String transactionId;
		loadUrl();
		makerLogin();
		extractFormData("InitCounterParty");
		Document response = postService(appUrl, testData);
		Elements TransactionID = response.select("input[id=\"transactionId\"]");
		transactionId = TransactionID.attr("value");
		System.out.println(transactionId);
		return transactionId;
	}
	/*****************************************************************************
	 * 
	 * Input counter party customer
	 * 
	 *****************************************************************************/
	public static String inputCounterPartyCutomer(String wbName) {
		String transactionId = initializeCounterPartyCutomer();
		extractFormData("InputCounterParty");
		extractExcelData("CounterParty",wbName);
		testData.put("transactionId", transactionId);
		testData.put("fieldName:NAME.1:1", testData.get("fieldName:NAME.1:1") + transactionId);
		testData.put("fieldName:MNEMONIC", testData.get("fieldName:NAME.1:1").substring(1, 1)
				+ testData.get("fieldName:SHORT.NAME:1").substring(1, 1) + transactionId);
		Document response = postService(appUrl, testData);
		checkAndHandleOverRides(response);
		getMessage(response);
		return transactionId;
	}
	
	/*****************************************************************************
	 * 
	 * Authorize counter party customer
	 * 
	 *****************************************************************************/
	public static void authorizeCounterPartyCustomer(String transactionId) {
		loadUrl();
		authorizerLogin();
		extractFormData("AuthCounterParty");
		testData.put("transactionId", transactionId);
		Document response = postService(appUrl, testData);
		getMessage(response);
	}
	
	
	/*****************************************************************************
	 * 
	 * Initialize Nostro Account
	 * 
	 *****************************************************************************/
	public static String intializeNostroAccount() {
		String transactionId;
		loadUrl();
		makerLogin();
		extractFormData("InitNostroAccount");
		Document response = postService(appUrl, testData);
		Elements TransactionID = response.select("input[id=\"transactionId\"]");
		transactionId = TransactionID.attr("value");
		System.out.println(transactionId);
		return transactionId;
	}
	
	/*****************************************************************************
	 * 
	 * Create Nostro Account
	 * 
	 *****************************************************************************/
	
	public static String inputNostroAccount(String customerID, String wbName) {
		String transactionId = intializeNOSTROAccount();
		extractFormData("InputNostroAccount");
		extractExcelData("NostroAccount", wbName);
		testData.put("transactionId", transactionId);
		testData.put("fieldName:CUSTOMER", customerID);
		Document response = postService(appUrl, testData);
		String message = getMessage(response);
		String[] id = message.split(" ");
		System.out.println(id[2]);
		transactionId= id[2];
		testData.put("transactionId", id[2]);
		return transactionId;
	}

	/*****************************************************************************
	 * 
	 * Authorise Nostro Account
	 * 
	 *****************************************************************************/
	public static void authoriseNostroAccount(String accountID) {
		loadUrl();
		authorizerLogin();
		extractFormData("AuthNostroAccount");
		testData.put("transactionId", accountID);
		Document response = postService(appUrl, testData);
		getMessage(response);
	}

	
	/*****************************************************************************
	 * 
	 * Initialize Fund Transfer
	 * @return 
	 * 
	 *****************************************************************************/
	public static String initializeFundTransfer() {
		String transactionId;
		loadUrl();
		makerLogin();
		extractFormData("InitFundTransfer");
		Document response = postService(appUrl, testData);
		Elements TransactionID = response.select("input[id=\"transactionId\"]");
		transactionId = TransactionID.attr("value");
		System.out.println(transactionId);
		return transactionId;
	}
	
	/*****************************************************************************
	 * 
	 * Input Fund Transfer
	 * @return 
	 * 
	 *****************************************************************************/
	public static String inputFundTransfer(String accountId, String wbName) {
		String transactionId = initializeFundTransfer();
		extractFormData("InputFundTransfer");
		extractExcelData("FundTransfer", wbName);
		testData.put("transactionId", transactionId);
		testData.put("fieldName:CREDIT.ACCT.NO", accountId);
		Document response = postService(appUrl, testData);
		response= checkAndHandleOverRides(response);
		getMessage(response);
		return transactionId;
	}
	/*****************************************************************************
	 * 
	 * Input AUM Fund Transfer
	 * @return 
	 * 
	 *****************************************************************************/
	public static String inputFundTransfer(String wbName) {
		String transactionId = initializeFundTransfer();
		extractFormData("InputFundTransfer");
		extractExcelData("AUM_FundTransfer", wbName);
		testData.put("transactionId", transactionId);
		Document response = postService(appUrl, testData);
		response= checkAndHandleOverRides(response);
		getMessage(response);
		return transactionId;
	}
	
	
	/*****************************************************************************
	 * 
	 * Authorise Fund Transfer
	 * @return 
	 * 
	 *****************************************************************************/
	public static void authoriseFundTransfer(String fundTransferId) {
		loadUrl();
		authorizerLogin();
		extractFormData("AuthFundTransfer");
		testData.put("transactionId", fundTransferId);
		Document response = postService(appUrl, testData);
		getMessage(response);
	}
	/*****************************************************************************
	 * 
	 * Initialize Securities
	 * @return 
	 * 
	 *****************************************************************************/
	public static String initializeEquity() {
		String transactionId;
		loadUrl();
		makerLogin();
		extractFormData("InitEquity");
		Document response = postService(appUrl, testData);
		Elements TransactionID = response.select("input[id=\"transactionId\"]");
		transactionId = TransactionID.attr("value");
		System.out.println(transactionId);
		return transactionId;
	}
	
	/*****************************************************************************
	 * 
	 * Input Securities
	 * @return 
	 * 
	 *****************************************************************************/
	public static String inputEquity(String wbName) {
		String transactionId = initializeEquity();
		extractFormData("InputEquity");
		extractExcelData("Equity", wbName);
		String companyName = testData.get("fieldName:COMPANY.NAME:1") + transactionId.substring(transactionId.indexOf("-")+1);
		String shortName= testData.get("fieldName:SHORT.NAME:1") + transactionId.substring(transactionId.indexOf("-")+1);
		String Mnemonic= companyName.substring(0,1)+ shortName.substring(0,1)+ transactionId.substring(transactionId.indexOf("-")+1);
		System.out.println(Mnemonic);
		testData.put("fieldName:COMPANY.NAME:1", companyName);
		testData.put("fieldName:SHORT.NAME:1", shortName);
		testData.put("fieldName:MNEMONIC", Mnemonic);
		testData.put("transactionId", transactionId);
		Document response = postService(appUrl, testData);
		response= checkAndHandleOverRides(response);
		getMessage(response);
		return transactionId;
	}
	
	/*****************************************************************************
	 * 
	 * Authorise Securities
	 * @return 
	 * 
	 *****************************************************************************/
	public static void authoriseEquity(String securityId) {
		loadUrl();
		authorizerLogin();
		extractFormData("AuthEquity");
		testData.put("transactionId", securityId);
		Document response = postService(appUrl, testData);
		getMessage(response);
	}
	
	/*****************************************************************************
	 * 
	 * Initialize BuyOrder
	 * @return 
	 * 
	 *****************************************************************************/
	public static String initializeEquityBuyOrder() {
		String transactionId;
		loadUrl();
		makerLogin();
		extractFormData("InitEquityBuyOrder");
		Document response = postService(appUrl, testData);
		getMessage(response);
		Elements TransactionID = response.select("input[id=\"transactionId\"]");
		transactionId = TransactionID.attr("value");
		System.out.println(transactionId);
		return transactionId;
	}
	
	/*****************************************************************************
	 * 
	 * Input Buy Order
	 * @return 
	 * 
	 *****************************************************************************/
	public static String inputEquityBuyOrder(String equiteeID, String customerID, String Depository, String wbName) {
		String transactionId = initializeEquityBuyOrder();
		extractFormData("InputEquityBuyOrder");
		extractExcelData("BuyOrder", wbName);
		testData.put("transactionId", transactionId);
		testData.put("fieldName:SECURITY.NO", equiteeID);
		testData.put("fieldName:CUST.NUMBER:1", customerID);
		testData.put("fieldName:SECURITY.ACCNT:1", customerID+"-1");
		testData.put("fieldName:DEPOSITORY", Depository);
		Document response = postService(appUrl, testData);
		response= checkAndHandleOverRides(response);
		getMessage(response);
		return transactionId;
	}
	
	/*****************************************************************************
	 * 
	 * Initialize Equity SellOrder
	 * @return 
	 * 
	 *****************************************************************************/
	public static String initializeEquitySellOrder() {
		String transactionId;
		loadUrl();
		makerLogin();
		extractFormData("InitEquitySellOrder");
		Document response = postService(appUrl, testData);
		Elements TransactionID = response.select("input[id=\"transactionId\"]");
		transactionId = TransactionID.attr("value");
		System.out.println(transactionId);
		return transactionId;
	}
	
	/*****************************************************************************
	 * 
	 * Input Equity Sell Order
	 * @return 
	 * 
	 *****************************************************************************/
	public static String inputEquitySellOrder(String equiteeID, String customerID, String Depository, String wbName) {
		String transactionId = initializeEquitySellOrder();
		extractFormData("InputEquitySellOrder");
		extractExcelData("SellOrder", wbName);
		testData.put("transactionId", transactionId);
		testData.put("fieldName:SECURITY.NO", equiteeID);
		testData.put("fieldName:CUST.NUMBER:1", customerID);
		testData.put("fieldName:SECURITY.ACCNT:1", customerID+"-1");
		testData.put("fieldName:DEPOSITORY", Depository);
		Document response = postService(appUrl, testData);
		response= checkAndHandleOverRides(response);
		getMessage(response);
		return transactionId;
	}
	
	
	/*****************************************************************************
	 * 
	 * Input Order Transmit
	 * @return 
	 * 
	 *****************************************************************************/
	public static String inputOrderTransmit(String orderType, String transactionId, String wbName) {
		extractFormData("InputOrderTransmit");
		extractExcelData("OrderTransmit", wbName);
		testData.put("transactionId", transactionId);
		Document response = postService(appUrl, testData);
		response= checkAndHandleOverRides(response);
		getMessage(response);
		return transactionId;
	}
	
	/*****************************************************************************
	 * 
	 * Input Order Transmit
	 * @return 
	 * 
	 *****************************************************************************/
	public static String inputDealerBlotter(String orderType, String BrokerID, String transactionId, String wbName) {
		loadUrl();
		makerLogin();
		extractFormData("InputDealerBlotter");
		extractExcelData("DealerBlotter", wbName);
		testData.put("transactionId", transactionId);
		testData.put("fieldName:BROKER.NO:1", BrokerID);
		Document response = postService(appUrl, testData);
		response= checkAndHandleOverRides(response);
		getMessage(response);
		return transactionId;
	}
	
	
	/*****************************************************************************
	 * 
	 * Authorise Equity Transaction
	 * @return 
	 * 
	 *****************************************************************************/
	public static String authSecurityTransaction(String orderType, String BrokerID, String transactionId, String wbName) {
		String secTransId = null;
		loadUrl();
		authorizerLogin();
		extractFormData("EnquireSecurityTrade");
		testData.put("transactionId", transactionId);
		Document response = postService(appUrl, testData);
		 Elements TransId =  response.select("span");
		 for(Element tran:TransId) {
			 if(tran.text().contains("SCTRSC")) {
				 secTransId = tran.text();
				 break;
			 }
		 }
		System.out.println(secTransId);
		extractFormData("AuthEquityTransaction");
		extractExcelData("AuthoriseEquityOrder", wbName);
		if(orderType.equalsIgnoreCase("BUY")) {
			testData.put("fieldName:BR.BEN.BANK.1:1", BrokerID);
		}
		testData.put("transactionId", secTransId);
		response = postService(appUrl, testData);
		response= checkAndHandleOverRides(response);
		getMessage(response);
		return transactionId;
	}
	
	/*****************************************************************************
	 * 
	 * Initialize EconomicGroup
	 * @return 
	 * 
	 *****************************************************************************/
	public static String initializeEconomicGroup() {
		String transactionId;
		loadUrl();
		makerLogin();
		extractFormData("InitEconomicGroup");
		Document response = postService(appUrl, testData);
		Elements TransactionID = response.select("input[id=\"transactionId\"]");
		transactionId = TransactionID.attr("value");
		System.out.println(transactionId);
		return transactionId;
	}
	
	/*****************************************************************************
	 * 
	 * Input Economic Group
	 * @return 
	 * 
	 *****************************************************************************/
	public static String inputEconomicGroup(String wbName) {
		String transactionId = initializeEconomicGroup();
		extractFormData("InputEconomicGroup");
		extractExcelData("EconomicGroup", wbName);
		testData.put("transactionId", transactionId);
		Document response = postService(appUrl, testData);
		response= checkAndHandleOverRides(response);
		getMessage(response);
		return transactionId;
	}
	
	
	/*****************************************************************************
	 * 
	 * Authorise EconomicGroup
	 * @return 
	 * 
	 *****************************************************************************/
	public static void authoriseEconomicGroup(String eConGroupId) {
		loadUrl();
		authorizerLogin();
		extractFormData("AuthEconomicGroup");
		testData.put("transactionId", eConGroupId);
		Document response = postService(appUrl, testData);
		getMessage(response);
	}
	
	/*****************************************************************************
	 * 
	 * Input Buy Order
	 * @return 
	 * 
	 *****************************************************************************/
	public static String inputBuyOrder(String wbName) {
		String transactionId = initializeEquityBuyOrder();
		extractFormData("InputEquityBuyOrder");
		extractExcelData("BuyOrder", wbName);
		testData.put("transactionId", transactionId);
		/*
		 * testData.put("fieldName:SECURITY.NO", equiteeID);
		 * testData.put("fieldName:CUST.NUMBER:1", customerID);
		 * testData.put("fieldName:SECURITY.ACCNT:1", customerID+"-1");
		 * testData.put("fieldName:DEPOSITORY", Depository);
		 */
		Document response = postService(appUrl, testData);
		response= checkAndHandleOverRides(response);
		getMessage(response);
		return transactionId;
	}
	/*****************************************************************************
	 * 
	 * Input Equity Sell Order
	 * @return 
	 * 
	 *****************************************************************************/
	public static String inputSellOrder(String wbName) {
		String transactionId = initializeEquitySellOrder();
		extractFormData("InputEquitySellOrder");
		extractExcelData("SellOrder", wbName);
		testData.put("transactionId", transactionId);
		/*
		 * testData.put("fieldName:SECURITY.NO", equiteeID);
		 * testData.put("fieldName:CUST.NUMBER:1", customerID);
		 * testData.put("fieldName:SECURITY.ACCNT:1", customerID+"-1");
		 * testData.put("fieldName:DEPOSITORY", Depository);
		 */
		Document response = postService(appUrl, testData);
		response= checkAndHandleOverRides(response);
		getMessage(response);
		return transactionId;
	}
	
	/*
	 * public static void inputArrangementAccountCreation(String customerID) {
	 * String transactionId = intializeAccount();
	 * extractExcelData("AccountCreation_1", "AccountCreation");
	 * extractExcelData("ValidateAccount");
	 * testData.put("transactionId", transactionId);
	 * testData.put("fieldName:CUSTOMER:1", customerID); Document response =
	 * postService(appUrl, testData); Elements user =
	 * response.select("input[id=\"user\"]"); String userId = user.attr("value");
	 * System.out.println(user.attr("value")); Elements arrangementId =
	 * response.select("input[id=\"fieldName:ARRANGEMENT\"]"); String aggrementID =
	 * arrangementId.attr("value"); System.out.println(aggrementID); Elements accID
	 * = response.select("input[id=\"fieldName:ACCOUNT.REFERENCE\"]"); String
	 * accountID = accID.attr("value");
	 * 
	 * 
	 * 
	 * 
	 * System.out.println(accountID); extractExcelData("CommitAccount",
	 * "UserInput");
	 * 
	 * extractArrangementData("Arrangement", aggrementID);
	 * testData.put("appreq:transactionId", transactionId);
	 * testData.put("appreq:fieldName:ARRANGEMENT", aggrementID);
	 * testData.put("appreq:fieldName:CUSTOMER:1", customerID);
	 * testData.put("AA.ARR.CUSTOMER,ITAU.AA"+aggrementID+
	 * "-CUSTOMER-20190122.1:fieldName:CUSTOMER:1", customerID);
	 * testData.put("AA.ARR.ACCOUNT,ITAU.AA.AR"+aggrementID+
	 * "-BALANCE-20190122.1:fieldName:ACCOUNT.REFERENCE", accountID);
	 * testData.put("AA.ARR.CUSTOMER,ITAU.AA"+aggrementID+
	 * "-CUSTOMER-20190122.1:user", userId);
	 * testData.put("AA.ARR.OFFICERS,ITAU.AA"+aggrementID+
	 * "-OFFICERS-20190122.1:user", userId);
	 * testData.put("AA.ARR.LIMIT,ITAU.AA.REF"+aggrementID+"-LIMIT-20190122.1:user",
	 * userId); testData.put("AA.ARR.BALANCE.AVAILABILITY,ITAU.AA"+aggrementID+
	 * "-BALANCE.AVAILABILITY-20190122.1:user", userId);
	 * testData.put("AA.ARR.CLOSURE,ITAU.AA"+aggrementID+"-CLOSURE-20190122.1:user",
	 * userId); testData.put("AA.ARR.ACCOUNT,ITAU.AA.AR"+aggrementID+
	 * "-BALANCE-20190122.1:user", userId);
	 * testData.put("AA.ARR.INTEREST,AA"+aggrementID+"-DRINTEREST-20190122.1:user",
	 * userId); testData.put("AA.ARR.PAYMENT.SCHEDULE,AA"+aggrementID+
	 * "-SCHEDULE-20190122.1:user", userId);
	 * testData.put("AA.ARR.SETTLEMENT,AA.NOINPUT"+aggrementID+
	 * "-SETTLEMENT-20190122.1:user", userId);
	 * testData.put("AA.ARR.BALANCE.MAINTENANCE,AA.NOINPUT"+aggrementID+
	 * "-BALANCE.MAINTENANCE-20190122.1:user", userId);
	 * testData.put("AA.ARR.PAYOFF,AA"+aggrementID+"-PAYOFF-20190122.1:user",
	 * userId); response = postService(appUrl, testData);
	 * getMessage(response); try { BufferedWriter fos = new BufferedWriter(new
	 * OutputStreamWriter(new FileOutputStream( new File("./response.html"))));
	 * fos.write(response.html()); fos.close(); } catch (IOException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); }
	 * 
	 * 
	 * }
	 */
	/*****************************************************************************
	 * 
	 * Initialize bond
	 * @return 
	 * 
	 *****************************************************************************/
	public static String initializeBond() {
		String transactionId;
		loadUrl();
		makerLogin();
		extractFormData("InitBond");
		Document response = postService(appUrl, testData);
		Elements TransactionID = response.select("input[id=\"transactionId\"]");
		transactionId = TransactionID.attr("value");
		System.out.println(transactionId);
		return transactionId;
	}
	
	/*****************************************************************************
	 * 
	 * Input Securities
	 * @return 
	 * 
	 *****************************************************************************/
	public static String inputBond(String wbName) {
		String transactionId = initializeBond();
		extractFormData("InputBond");
		extractExcelData("Bond", wbName);
		String companyName = testData.get("fieldName:COMPANY.NAME:1") + transactionId.substring(transactionId.indexOf("-")+1);
		String shortName= testData.get("fieldName:SHORT.NAME:1") + transactionId.substring(transactionId.indexOf("-")+1);
		String Mnemonic= companyName.substring(0,1)+ shortName.substring(0,1)+ transactionId.substring(transactionId.indexOf("-")+1);
		System.out.println(Mnemonic);
		testData.put("fieldName:COMPANY.NAME:1", companyName);
		testData.put("fieldName:SHORT.NAME:1", shortName);
		testData.put("fieldName:MNEMONIC", Mnemonic);
		testData.put("transactionId", transactionId);
		Document response = postService(appUrl, testData);
		response= checkAndHandleOverRides(response);
		getMessage(response);
		return transactionId;
	}
	/*****************************************************************************

	/*****************************************************************************
	 * 
	 * Input Direct sec Trade
	 * @return 
	 * 
	 *****************************************************************************/
	public static String inputDirectSecTrade(String wbName) {
		String transactionId;
		loadUrl();
		makerLogin();
		extractFormData("InputDirectSecTrade");
		extractExcelData("DirectSecTrade", wbName);
		Document response = postService(appUrl, testData);
		Elements TransactionID = response.select("input[id=\"transactionId\"]");
		transactionId = TransactionID.attr("value");
		testData.put("transactionId", transactionId);
		System.out.println(transactionId);
		response= checkAndHandleOverRides(response);
		getMessage(response);
		return transactionId;
	}
	/*****************************************************************************
	 * 
	 * Authorise direct sec trade
	 * @return 
	 * 
	 *****************************************************************************/
	public static void authoriseDirectSecTrade(String OrderID) {
		loadUrl();
		authorizerLogin();
		System.out.println(OrderID);
		extractFormData("AuthDirectSecTrade");
		testData.put("transactionId", OrderID);
		Document response = postService(appUrl, testData);
		getMessage(response);
	}
	/*****************************************************************************
	 * 
	 * Authorise MM PlacementCallNoticeContract
	 * @return 
	 * 
	 *****************************************************************************/
	public static void authoriseMMPlacementCallNoticeContract(String OrderID) {
		loadUrl();
		authorizerLogin();
		System.out.println(OrderID);
		extractFormData("AuthMMPlacementCallNotice");
		testData.put("transactionId", OrderID);
		Document response = postService(appUrl, testData);
		getMessage(response);
	}
	/*****************************************************************************
	 * 
	 * Authorise MM TakingCallNoticeContract
	 * @return 
	 * 
	 *****************************************************************************/
	public static void authoriseMMTakingCallNoticeContract(String OrderID) {
		loadUrl();
		authorizerLogin();
		System.out.println(OrderID);
		extractFormData("AuthMMTakingCallNotice");
		testData.put("transactionId", OrderID);
		Document response = postService(appUrl, testData);
		getMessage(response);
	}
	
	/*****************************************************************************
	 * 
	 * Authorise MM PlacementFixedMaturity
	 * @return 
	 * 
	 *****************************************************************************/
	public static void authoriseMMPlacementFixedMaturity(String OrderID) {
		loadUrl();
		authorizerLogin();
		System.out.println(OrderID);
		extractFormData("AuthMMPlacementFixedMaturity");
		testData.put("transactionId", OrderID);
		Document response = postService(appUrl, testData);
		getMessage(response);
	}
	/*****************************************************************************
	 * 
	 * Authorise MM TakingFixedMaturity
	 * @return 
	 * 
	 *****************************************************************************/
	public static void authoriseMMTakingFixedMaturity(String OrderID) {
		loadUrl();
		authorizerLogin();
		System.out.println(OrderID);
		extractFormData("AuthMMTakingFixedMaturity");
		testData.put("transactionId", OrderID);
		Document response = postService(appUrl, testData);
		getMessage(response);
	}
	/*****************************************************************************
	 * 
	 * Authorise SwapIRSTradeDeal
	 * @return 
	 * 
	 *****************************************************************************/
	public static void authoriseSwapIRSTradeDeal(String OrderID) {
		loadUrl();
		authorizerLogin();
		System.out.println(OrderID);
		extractFormData("AuthSwapIRSTradeDeal");
		testData.put("transactionId", OrderID);
		Document response = postService(appUrl, testData);
		getMessage(response);
	}
	/*****************************************************************************
	 * 
	 * Authorise SwapIRSHedgeDeal
	 * @return 
	 * 
	 *****************************************************************************/
	public static void authoriseSwapIRSHedgeDeal(String OrderID) {
		loadUrl();
		authorizerLogin();
		System.out.println(OrderID);
		extractFormData("AuthSwapIRSHedgeDeal");
		testData.put("transactionId", OrderID);
		Document response = postService(appUrl, testData);
		getMessage(response);
	}
	/*****************************************************************************
	 * 
	 * Authorise SwapCIRSTradeDeal
	 * @return 
	 * 
	 *****************************************************************************/
	public static void authoriseSwapCIRSTradeDeal(String OrderID) {
		loadUrl();
		authorizerLogin();
		System.out.println(OrderID);
		extractFormData("AuthSwapCIRSTradeDeal");
		testData.put("transactionId", OrderID);
		Document response = postService(appUrl, testData);
		getMessage(response);
	}
	/*****************************************************************************
	 * 
	 * Authorise SwapCIRSHedgeDeal
	 * @return 
	 * 
	 *****************************************************************************/
	public static void authoriseSwapCIRSHedgeDeal(String OrderID) {
		loadUrl();
		authorizerLogin();
		System.out.println(OrderID);
		extractFormData("AuthSwapCIRSHedgeDeal");
		testData.put("transactionId", OrderID);
		Document response = postService(appUrl, testData);
		getMessage(response);
	}
	/*****************************************************************************
	 * 
	 * Authorise FixedFiduciary
	 * @return 
	 * 
	 *****************************************************************************/
	public static String approveFixedFiduciary(String OrderID) {
		loadUrl();
		authorizerLogin();
		System.out.println(OrderID);
		extractFormData("ApproveFixedFiduciary");
		testData.put("transactionId", OrderID);
		testData.put("fieldName:POOLING.STATUS","Approved");
		Document response = postService(appUrl, testData);
		try { BufferedWriter fos = new BufferedWriter(new  OutputStreamWriter(new FileOutputStream( new File("./response.html"))));
		 fos.write(response.html()); fos.close();
		 } catch (IOException e) { // TODO * Auto-generated catch block 
			 e.printStackTrace(); }
		Elements PooledGroup = response.select("input[id=\"fieldName:POOLED.GROUP\"]");
		String pooledGroup = PooledGroup.attr("value");
		System.out.println(pooledGroup);
		
		response = checkAndHandleOverRides(response);
		
		getMessage(response);
		return pooledGroup;
	}
	/*****************************************************************************
	 * 
	 * Authorise NoticeFiduciary
	 * @return 
	 * 
	 *****************************************************************************/
	public static String approveNoticeFiduciary(String OrderID) {
		loadUrl();
		authorizerLogin();
		System.out.println(OrderID);
		extractFormData("ApproveNoticeFiduciary");
		testData.put("transactionId", OrderID);
		testData.put("fieldName:POOLING.STATUS","Approved");
		Document response = postService(appUrl, testData);
		try { BufferedWriter fos = new BufferedWriter(new  OutputStreamWriter(new FileOutputStream( new File("./response.html"))));
		 fos.write(response.html()); fos.close();
		 } catch (IOException e) { // TODO * Auto-generated catch block 
			 e.printStackTrace(); }
		Elements PooledGroup = response.select("input[id=\"fieldName:POOLED.GROUP\"]");
		String pooledGroup = PooledGroup.attr("value");
		System.out.println(pooledGroup);
		
		response = checkAndHandleOverRides(response);
		
		getMessage(response);
		return pooledGroup;
	}
	/*****************************************************************************
	 * 
	 * Input FixedFiduciaryPlacement
	 * @return 
	 * 
	 *****************************************************************************/
	public static String inputFixedFiduciaryPlacement(String pooledGrpID, String wbName) {
		
		testData.put("command", "globusCommand");
		testData.put("operation", "ENQUIRY.SELECT");
		testData.put("fieldName:1:1:1", "POOLED.GROUP");
		testData.put("operand:1:1:1", "EQ");
		testData.put("value:1:1:1", pooledGrpID);
		testData.put("command", "globusCommand");
		testData.put("requestType", "OFS.ENQUIRY");
		testData.put("enqname", "FD.PLACE");
		testData.put("enqaction", "RUN");
		
		Document response = postService(appUrl, testData);
		
		try { BufferedWriter fos = new BufferedWriter(new  OutputStreamWriter(new FileOutputStream( new File("./response.html"))));
		 fos.write(response.html()); fos.close();
		 } catch (IOException e) { // TODO * Auto-generated catch block 
			 e.printStackTrace(); }
		String fdId = null ;
		Elements idCol = response.getElementsByTag("td");
		for(Element id:idCol) {
			if(id.text().startsWith("FD1")) {
				fdId = id.text();
				break;
			}
		}
		System.out.println(fdId);
		
		loadUrl();
		makerLogin();
		
		  extractExcelData("InputFixedFiduciary", wbName);
		  extractFormData("InputFixedFiduciaryPlacement");
		  testData.put("fieldName:POOLED.GROUP", pooledGrpID);
		  testData.put("transactionId", fdId);
		 
		response = postService(appUrl, testData);
		response = checkAndHandleOverRides(response);
		
		getMessage(response);
		return fdId;
	}
	/*****************************************************************************
	 * 
	 * Input MMPlacementsCallNoticeContract
	 * @return 
	 * 
	 *****************************************************************************/
	public static String inputMMPlacementCallNotice(String wbName) {
		String transactionid = initializeMMPlacementsCallNotice();
		extractExcelData("MMPlacementsCallNoticeContract", wbName);
		testData.put("transactionId", transactionid);
		/*
		 * testData.put("fieldName:CUSTOMER.ID", customerNumber);
		 * testData.put("fieldName:PRIN.BEN.BANK.1", customerNumber);
		 */
		extractFormData("InputMMPlacementCallNotice");
		Document response = postService(appUrl, testData);
		response= checkAndHandleOverRides(response);
		getMessage(response);
		return transactionid;
	}
	/*****************************************************************************
	 * 
	 * Input MMTakingsCallNoticeContract
	 * @return 
	 * 
	 *****************************************************************************/
	public static String inputMMTakingCallNotice(String wbName) {
		String transactionid = initializeMMTakingsCallNotice();
		extractExcelData("MMTakingsCallNoticeContract", wbName);
		testData.put("transactionId", transactionid);
		/*
		 * testData.put("fieldName:CUSTOMER.ID", customerNumber);
		 * testData.put("fieldName:PRIN.BEN.BANK.1", customerNumber);
		 */
		extractFormData("InputMMTakingCallNotice");
		
		
		Document response = postService(appUrl, testData);
		
		response= checkAndHandleOverRides(response);
		getMessage(response);
		return transactionid;
	}
	/*****************************************************************************
	 * 
	 * Input MMPlacementFixedMaturity
	 * @return 
	 * 
	 *****************************************************************************/
	public static String inputMMPlacementFixedMaturity(String wbName) {
		String transactionid = initializeMMPlacementFixedMaturity();
		extractExcelData("MMPlacementsFixedMaturity", wbName);
		testData.put("transactionId", transactionid);
		/*
		 * testData.put("fieldName:CUSTOMER.ID", customerNumber);
		 * testData.put("fieldName:PRIN.BEN.BANK.1", customerNumber);
		 */
		extractFormData("InputMMPlacementFixedMaturity");
	
		
		Document response = postService(appUrl, testData);
		
		response= checkAndHandleOverRides(response);
		getMessage(response);
		return transactionid;
	}
	/*****************************************************************************
	 * 
	 * Input MMTakingFixedMaturity
	 * @return 
	 * 
	 *****************************************************************************/
	public static String inputMMTakingFixedMaturity(String wbName) {
		String transactionid = initializeMMTakingFixedMaturity();
		extractExcelData("MMTakingsFixedMaturity", wbName);
		testData.put("transactionId", transactionid);
		/*
		 * testData.put("fieldName:CUSTOMER.ID", customerNumber);
		 * testData.put("fieldName:PRIN.BEN.BANK.1", customerNumber);
		 */
		extractFormData("InputMMTakingFixedMaturity");
	
		
		Document response = postService(appUrl, testData);
		
		response= checkAndHandleOverRides(response);
		getMessage(response);
		return transactionid;
	}
	/*****************************************************************************
	 * 
	 * Input IRSTradeDeal
	 * @return 
	 * 
	 *****************************************************************************/
	public static String inputIRSTradeDeal(String wbName) {
		String transactionid = initializeIRSTradeDeal();
		extractExcelData("IRSTradeDeal", wbName);
		testData.put("transactionId", transactionid);
		/*
		 * testData.put("fieldName:CUSTOMER", customerNumber);
		 * testData.put("fieldName:ACCT.WITH.BANK:1", customerNumber);
		 */
		extractFormData("InputSwapIRSTradeDeal");
	
		
		Document response = postService(appUrl, testData);
		
		response= checkAndHandleOverRides(response);
		getMessage(response);
		return transactionid;
	}
	/*****************************************************************************
	 * 
	 * Input IRSHedgeDeal
	 * @return 
	 * 
	 *****************************************************************************/
	public static String inputIRSHedgeDeal(String wbName) {
		String transactionid = initializeIRSHedgeDeal();
		extractExcelData("IRSHedgeDeal", wbName);
		testData.put("transactionId", transactionid);
		/*
		 * testData.put("fieldName:CUSTOMER", customerNumber);
		 * testData.put("fieldName:ACCT.WITH.BANK:1", customerNumber);
		 */
		extractFormData("InputSwapIRSHedgeDeal");
	
		
		Document response = postService(appUrl, testData);
		
		response= checkAndHandleOverRides(response);
		getMessage(response);
		return transactionid;
	}
	/*****************************************************************************
	 * 
	 * Input CIRSTradeDeal
	 * @return 
	 * 
	 *****************************************************************************/
	public static String inputCIRSTradeDeal(String wbName) {
		String transactionid = initializeCIRSTradeDeal();
		extractExcelData("CIRSTradeDeal", wbName);
		testData.put("transactionId", transactionid);
		/*
		 * testData.put("fieldName:CUSTOMER", customerNumber);
		 * testData.put("fieldName:ACCT.WITH.BANK:1", customerNumber);
		 */
		extractFormData("InputSwapCIRSTradeDeal");
	
		
		Document response = postService(appUrl, testData);
		
		response= checkAndHandleOverRides(response);
		getMessage(response);
		return transactionid;
	}
	/*****************************************************************************
	 * 
	 * Input CIRSHedgeDeal
	 * @return 
	 * 
	 *****************************************************************************/
	public static String inputCIRSHedgeDeal(String wbName) {
		String transactionid = initializeCIRSHedgeDeal();
		extractExcelData("CIRSHedgeDeal", wbName);
		testData.put("transactionId", transactionid);
		/*
		 * testData.put("fieldName:CUSTOMER", customerNumber);
		 * testData.put("fieldName:ACCT.WITH.BANK:1", customerNumber);
		 */
		extractFormData("InputSwapCIRSHedgeDeal");
	
		
		Document response = postService(appUrl, testData);
		
		response= checkAndHandleOverRides(response);
		getMessage(response);
		return transactionid;
	}
	
	/*****************************************************************************
	 * 
	 * Input FixedFiduciary
	 * @return 
	 * 
	 *****************************************************************************/
	public static String inputFixedFiduciary(String wbName) {
		String transactionid = initializeFixedFiduciary();
		extractExcelData("FixedFiduciary", wbName);
		testData.put("transactionId", transactionid);
		testData.put("fieldName:FID.TYPE", "FIXED");

		extractFormData("InputFixedFiduciary");
		
		  Document response = postService(appUrl, testData);
		  response= checkAndHandleOverRides(response);
		  getMessage(response);
		 
		return transactionid;
	}
	/*****************************************************************************
	 * 
	 * Input NoticeFiduciary
	 * @return 
	 * 
	 *****************************************************************************/
	public static String inputNoticeFiduciary(String wbName) {
		String transactionid = initializeNoticeFiduciary();
		extractExcelData("NoticeFiduciary", wbName);
		testData.put("transactionId", transactionid);
		testData.put("fieldName:FID.TYPE", "NOTICE");

		extractFormData("InputNoticeFiduciary");
		
		  Document response = postService(appUrl, testData);
		  response= checkAndHandleOverRides(response);
		  getMessage(response);
		 
		return transactionid;
	}
	/**********************************************************************************************
	 * 
	 * TestCases of customer Creation
	 * 
	 **********************************************************************************************/

	public static void containerCustomerCreation(int iCount, String wbName) {
		
		String module = "ContainerCustomer", iStatus;
		logs.info("Reference Number : " + module + " " +  iCount);
		String[] colName = {"CustomerNumber",  "Status" };
		createHeaders(module, colName);
		String transactionId = inputCustomer(wbName);
		authorizeCustomer(transactionId);
		result.put("ScenarioName", module + iCount);
		result.put("CustomerNumber", transactionId);
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);

	}

	/**********************************************************************************************
	 * 
	 * TestCases of DDA Cash Account Creation
	 * 
	 **********************************************************************************************/
	public static void createDDACashAccount(int iCount, String wbName) {
		String customerID, accountID;
		String module = "DDACashAccount";
		logs.info("Reference Number : " + module + " " +  iCount);
		String[] colName = { "CustomerNumber", "AccountNumber",  "Status" };
		createHeaders(module, colName);
		customerID = inputCustomer(wbName);
		authorizeCustomer(customerID);
		accountID = inputAccountCreation(customerID, module, wbName);
		authAccount(accountID);
		result.put("ScenarioName", module + iCount);
		result.put("CustomerNumber", customerID);
		result.put("AccountNumber", accountID);
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);
	}
	
	/**********************************************************************************************
	 * 
	 * TestCases of create MoneyMarket Account Account Creation
	 * 
	 **********************************************************************************************/
	public static void createMoneyMarketAccount(int iCount, String wbName) {
		String customerID, accountID;
		String module = "MoneyMarketAccount";
		logs.info("Reference Number : " + module + " " +  iCount);
		String[] colName = { "CustomerNumber", "AccountNumber",  "Status" };
		createHeaders(module, colName);
		customerID = inputCustomer(wbName);
		authorizeCustomer(customerID);
		accountID = inputAccountCreation(customerID, module, wbName);
		authAccount(accountID);
		result.put("ScenarioName", module + iCount);
		result.put("CustomerNumber", customerID);
		result.put("AccountNumber", accountID);
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);
	}

	/**********************************************************************************************
	 * 
	 * TestCases of create MoneyMarket Account Account Creation
	 * 
	 **********************************************************************************************/
	public static void createMarginAccount(int iCount, String wbName) {
		String customerID, accountID;
		String module = "MarginAccount";
		logs.info("Reference Number : " + module + " " +  iCount);
		String[] colName = { "CustomerNumber", "AccountNumber",  "Status" };
		createHeaders(module, colName);
		customerID = inputCustomer(wbName);
		authorizeCustomer(customerID);
		accountID = inputAccountCreation(customerID, module, wbName);
		authAccount(accountID);
		result.put("ScenarioName", module + iCount);
		result.put("CustomerNumber", customerID);
		result.put("AccountNumber", accountID);
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);
	}
	
	
	/**********************************************************************************************
	 * 
	 * TestCases of Customer Portfolio Creation
	 * 
	 **********************************************************************************************/
	public static void customerPortFolio( int iCount, String wbName) {
		String customerID, accountID, portfolioId;
		String module = "CustomerPortfolio";
		logs.info("Reference Number : " + module + " " +  iCount);
		String[] colName = { "CustomerNumber", "AccountNumber", "CustomerPortfolio",  "Status" };
		createHeaders(module, colName);
		customerID = inputCustomer(wbName);
		authorizeCustomer(customerID);
		accountID = inputAccountCreation(customerID, "DDACashAccount", wbName);
		authAccount(accountID);
		customerSecurity(customerID, "CustomerSecurity", wbName);
		portfolioId = createCustomerPortFolio(customerID, accountID, wbName);
		result.put("ScenarioName", module + iCount);
		result.put("CustomerNumber", customerID);
		result.put("AccountNumber", accountID);
		result.put("CustomerPortfolio", portfolioId);
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);

	}
	/**********************************************************************************************
	 * 
	 * TestCases of Broker creation
	 * 
	 **********************************************************************************************/
	public static void createBroker(int iCount, String wbName) {
		String customerID, accountID, portfolioId;
		String module = "Broker";
		logs.info("Reference Number : " + module + " " +  iCount);
		String[] colName = {"CustomerNumber",  "Status" };
		createHeaders(module, colName);
		customerID = inputCustomer(wbName);
		authorizeCustomer(customerID);
		customerSecurity(customerID, module, wbName);
		result.put("ScenarioName", module + iCount);
		result.put("CustomerNumber", customerID);
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);

	}
	
	/**********************************************************************************************
	 * 
	 * TestCases of Depository creation
	 * 
	 **********************************************************************************************/
	public static void createDepository(int iCount, String wbName) {
		String customerID, accountID, portfolioId;
		String module = "Depository";
		logs.info("Reference Number : " + module + " " +  iCount);
		String[] colName = {"CustomerNumber",  "Status" };
		createHeaders(module, colName);
		customerID = inputCustomer(wbName);
		authorizeCustomer(customerID);
		customerCharge(customerID);
		customerSecurity(customerID, module, wbName);
		result.put("ScenarioName", module + iCount);
		result.put("CustomerNumber", customerID);
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);

	}

	/**********************************************************************************************
	 * 
	 * TestCases of Customer Portfolio Creation
	 * 
	 **********************************************************************************************/
	public static void feesTestData(int iCount, String wbName) {
		String customerID, accountID, portfolioId;
		String module = "FeesData";
		logs.info("Reference Number : " + module + " " +  iCount);
		String[] colName = { "CustomerNumber", "AccountNumber", "CustomerPortfolio",  "Status" };
		createHeaders(module, colName);
		customerID = inputCustomer(wbName);
		authorizeCustomer(customerID);
		accountID = inputAccountCreation(customerID, "DDACashAccount", wbName);
		authAccount(accountID);
		customerSecurity(customerID, "CustomerSecurity", wbName);
		portfolioId = createCustomerPortFolio(customerID, accountID, wbName);
		createFeesTestData(customerID, accountID, wbName);
		result.put("ScenarioName", module + iCount);
		result.put("CustomerNumber", customerID);
		result.put("AccountNumber", accountID);
		result.put("CustomerPortfolio", portfolioId);
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);

	}
	
	/**********************************************************************************************
	 * 
	 * TestCases of FundTransfer
	 * 
	 **********************************************************************************************/
	public static void fundTransfer(int iCount, String wbName) {
		String customerID, accountID, portfolioId, fundTransferId;
		String module = "FundTransfer";
		logs.info("Reference Number : " + module + " " +  iCount);
		String[] colName = { "CustomerNumber", "AccountNumber", "CustomerPortfolio", "FundTransferID",  "Status" };
		createHeaders(module, colName);
		customerID = inputCustomer(wbName);
		authorizeCustomer(customerID);
		accountID = inputAccountCreation(customerID, "DDACashAccount", wbName);
		authAccount(accountID);
		customerSecurity(customerID, "CustomerSecurity", wbName);
		portfolioId = createCustomerPortFolio(customerID, accountID, wbName);
		fundTransferId = inputFundTransfer(accountID, wbName);
		authoriseFundTransfer(fundTransferId);
		result.put("ScenarioName", module + iCount);
		result.put("CustomerNumber", customerID);
		result.put("AccountNumber", accountID);
		result.put("CustomerPortfolio", portfolioId);
		result.put("FundTransferID", fundTransferId);
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);
	}
	
	
	

	/**************************************************************************************
	 * 
	 * TestCases of CounterParty Customer Creation
	 * 
	 *************************************************************************************/
	public static void  counterPartyCreation(int iCount, String wbName) {
		String customerId;
		String module = "CounterParty";
		logs.info("Reference Number : " + module + " " +  iCount);
		String[] colName = {"CustomerNumber",  "Status" };
		createHeaders(module, colName);
		String transactionId = inputCounterPartyCutomer(wbName);
		authorizeCounterPartyCustomer(transactionId);
		result.put("ScenarioName", module + iCount);
		result.put("CustomerNumber", transactionId);
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);
		
	}
	
	/**************************************************************************************
	 * 
	 * TestCases of Nostro Account
	 * 
	 *************************************************************************************/
	public static void createNostroAccount(int iCount, String wbName) {
		String customerID, accountID;
		String module = "NostroAccount";
		logs.info("Reference Number : " + module + " " +  iCount);
		String[] colName = { "CustomerNumber", "AccountNumber",  "Status" };
		createHeaders(module, colName);
		customerID = inputCounterPartyCutomer(wbName);
		authorizeCounterPartyCustomer(customerID);
		accountID = inputNostroAccount(customerID, wbName);
		authoriseNostroAccount(accountID);
		result.put("ScenarioName", module + iCount);
		result.put("CustomerNumber", customerID);
		result.put("AccountNumber", accountID);
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);
		
	}
	
	/**************************************************************************************
	 * 
	 * TestCases of Equity
	 * 
	 *************************************************************************************/
	public static void createEquity(int iCount, String wbName) {
		String equityId;
		String module = "Equity";
		logs.info("Reference Number : " + module + " " +  iCount);
		String[] colName = { "EquityId", "Status" };
		createHeaders(module, colName);
		equityId = inputEquity(wbName);
		authoriseEquity(equityId);
		result.put("ScenarioName", module + iCount);
		result.put("EquityId", equityId);
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);
		
	}
	
	/**************************************************************************************
	 * 
	 * TestCases of Equity Buy Order
	 * 
	 *************************************************************************************/
	public static void createEquityBuyOrder(int iCount, String wbName) {
		String secuirtyCustomer, depositoryCustomer, brokerCustomer , accountID, portfolioId, equityId, buyId;
		String module = "EquityBuyOrder";
		logs.info("Reference Number : " + module + " " +  iCount);
		String[] colName = { "CustomerNumber", "AccountNumber", "DepositoryCustomer", "BrokerCustomer", "PortFolioID", "EquityID", "BuyOrderID", "TradeID", "Status" };
		createHeaders(module, colName);
		secuirtyCustomer = inputCustomer(wbName);
		authorizeCustomer(secuirtyCustomer);
		accountID = inputAccountCreation(secuirtyCustomer, "DDACashAccount", wbName);
		authAccount(accountID);
		customerSecurity(secuirtyCustomer, "CustomerSecurity", wbName);
		portfolioId = createCustomerPortFolio(secuirtyCustomer, accountID, wbName);
		depositoryCustomer = inputCustomer(wbName);
		authorizeCustomer(depositoryCustomer);
		customerCharge(depositoryCustomer);
		customerSecurity(depositoryCustomer, "Depository", wbName);
		brokerCustomer = inputCustomer(wbName);
		authorizeCustomer(brokerCustomer);
		customerSecurity(brokerCustomer, "BROKER", wbName);
		equityId = inputEquity(wbName);
		authoriseEquity(equityId);
		buyId = inputEquityBuyOrder(equityId, secuirtyCustomer, depositoryCustomer, wbName);
		inputOrderTransmit("BUY",buyId, wbName);
		inputDealerBlotter("BUY", brokerCustomer, buyId, wbName);	
		authSecurityTransaction("BUY", brokerCustomer, buyId, wbName);
		result.put("ScenarioName", module + iCount);
		result.put("CustomerNumber", secuirtyCustomer);
		result.put("PortFolioID", portfolioId);
		result.put("DepositoryCustomer", depositoryCustomer);
		result.put("BrokerCustomer", brokerCustomer);
		result.put("EquityId", equityId);
		result.put("BuyOrderID", buyId);
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);
	}
	
	
	/**************************************************************************************
	 * 
	 * TestCases of Equity Buy Order and Sell Order
	 * 
	 *************************************************************************************/
	public static void createEquityBuySellOrder(int iCount, String wbName) {
		String secuirtyCustomer, depositoryCustomer, brokerCustomer , accountID, portfolioId, equityId, buyId, sellId;
		String module = "EquityBuyAndSellOrder";
		logs.info("Reference Number : " + module + " " +  iCount);
		String[] colName = { "CustomerNumber", "AccountNumber", "DepositoryCustomer", "BrokerCustomer", "PortFolioID", "EquityID", "BuyOrderID", "TradeID", "SellOrderID", "Status" };
		createHeaders(module, colName);
		secuirtyCustomer = inputCustomer(wbName);
		authorizeCustomer(secuirtyCustomer);
		accountID = inputAccountCreation(secuirtyCustomer, "DDACashAccount", wbName);
		authAccount(accountID);
		customerSecurity(secuirtyCustomer, "CustomerSecurity", wbName);
		portfolioId = createCustomerPortFolio(secuirtyCustomer, accountID, wbName);
		depositoryCustomer = inputCustomer(wbName);
		authorizeCustomer(depositoryCustomer);
		customerCharge(depositoryCustomer);
		customerSecurity(depositoryCustomer, "Depository", wbName);
		brokerCustomer = inputCustomer(wbName);
		authorizeCustomer(brokerCustomer);
		customerSecurity(brokerCustomer, "BROKER", wbName);
		equityId = inputEquity(wbName);
		authoriseEquity(equityId);
		buyId = inputEquityBuyOrder(equityId, secuirtyCustomer, depositoryCustomer, wbName);
		inputOrderTransmit("BUY",buyId, wbName);
		inputDealerBlotter("BUY", brokerCustomer, buyId, wbName);	
		authSecurityTransaction("BUY", brokerCustomer, buyId, wbName);
		System.out.println("BuyOrder Completed");
		sellId = inputEquitySellOrder(equityId, secuirtyCustomer, depositoryCustomer, wbName);
		inputOrderTransmit("SELL",sellId, wbName);
		inputDealerBlotter("SELL", brokerCustomer, sellId, wbName);	
		authSecurityTransaction("SELL", brokerCustomer, sellId, wbName);
		result.put("ScenarioName", module + iCount);
		result.put("CustomerNumber", secuirtyCustomer);
		result.put("PortFolioID", portfolioId);
		result.put("DepositoryCustomer", depositoryCustomer);
		result.put("BrokerCustomer", brokerCustomer);
		result.put("EquityId", equityId);
		result.put("BuyOrderID", buyId);
		result.put("SellOrderID", sellId);
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);
	}
	
	/**************************************************************************************
	 * 
	 * TestCases of AUM Accounts
	 * 
	 *************************************************************************************/
	
	public static void createAUMAccount(int iCount, String wbName) {
		String accountID;
		String module = "AUM_Account";
		logs.info("Reference Number : " + module + " " +  iCount);
		String[] colName = { "AccountNumber",  "Status" };
		createHeaders(module, colName);
		accountID = inputAccountCreation(wbName);
		authAccount(accountID);
		result.put("ScenarioName", module + iCount);
		result.put("AccountNumber", accountID);
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);
	}
	
	/**************************************************************************************
	 * 
	 * TestCases of AUM Portfolio
	 * 
	 *************************************************************************************/
	
	public static void createAUMPortfolio(int iCount, String wbName) {
		String portfolioId;
		String module = "AUM_Portfolio";
		logs.info("Reference Number : " + module + " " +  iCount);
		String[] colName = {"CustomerPortfolio",  "Status" };
		createHeaders(module, colName);
		customerSecurity(wbName);
		portfolioId = createCustomerPortFolio(wbName);
		result.put("ScenarioName", module + iCount);
		result.put("CustomerPortfolio", portfolioId);
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);
	}
	
	/**************************************************************************************
	 * 
	 * TestCases of AUM FundTransfer
	 * 
	 *************************************************************************************/
	public static void createAUMFundTransfer(int iCount, String wbName) {
		String fundTransferId;
		String module = "AUM_FundTransfer";
		logs.info("Reference Number : " + module + " " +  iCount);
		String[] colName = {"FundTransferID",  "Status" };
		createHeaders(module, colName);
		fundTransferId = inputFundTransfer(wbName);
		authoriseFundTransfer(fundTransferId);
		result.put("ScenarioName", module + iCount);
		result.put("FundTransferID", fundTransferId);
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);
	}
	
	/**************************************************************************************
	 * 
	 * TestCases of  EconomicGroup
	 * 
	 *************************************************************************************/
	public static void createEconomicGroup(int iCount, String wbName) {
		String econGroupId;
		String module = "EconomicGroup";
		logs.info("Reference Number : " + module + " " +  iCount);
		String[] colName = {"EconomicGroupID",  "Status" };
		createHeaders(module, colName);
		econGroupId = inputEconomicGroup(wbName);
		authoriseEconomicGroup(econGroupId);
		result.put("ScenarioName", module + iCount);
		result.put("FundTransferID", econGroupId);
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);
	}
	
	/**************************************************************************************
	 * 
	 * TestCases of Buy Order
	 * 
	 *************************************************************************************/
	public static void createBuyOrder(int iCount, String wbName) {
		String buyId;
		String module = "EquityBuyOrder";
		logs.info("Reference Number : " + module + " " +  iCount);
		String[] colName = {  "BuyOrderID", "Status"};
		createHeaders(module, colName);
		/*
		 * secuirtyCustomer = inputCustomer(wbName);
		 * authorizeCustomer(secuirtyCustomer); accountID =
		 * inputAccountCreation(secuirtyCustomer, "DDACashAccount", wbName);
		 * authAccount(accountID); customerSecurity(secuirtyCustomer,
		 * "CustomerSecurity", wbName); portfolioId =
		 * createCustomerPortFolio(secuirtyCustomer, accountID, wbName);
		 * depositoryCustomer = inputCustomer(wbName);
		 * authorizeCustomer(depositoryCustomer); customerCharge(depositoryCustomer);
		 * customerSecurity(depositoryCustomer, "Depository", wbName); brokerCustomer =
		 * inputCustomer(wbName); authorizeCustomer(brokerCustomer);
		 * customerSecurity(brokerCustomer, "BROKER", wbName); equityId =
		 * inputEquity(wbName); authoriseEquity(equityId);
		 */
		buyId = inputBuyOrder(wbName);
		/*
		 * inputOrderTransmit("BUY",buyId, wbName); inputDealerBlotter("BUY",
		 * brokerCustomer, buyId, wbName); authSecurityTransaction("BUY",
		 * brokerCustomer, buyId, wbName);
		 */
		result.put("ScenarioName", module + iCount);
		/*
		 * result.put("CustomerNumber", secuirtyCustomer); result.put("PortFolioID",
		 * portfolioId); result.put("DepositoryCustomer", depositoryCustomer);
		 * result.put("BrokerCustomer", brokerCustomer);
		 */
//		result.put("EquityId", equityId);
		result.put("BuyOrderID", buyId);
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);
	}
	

	/**************************************************************************************
	 * 
	 * TestCases of Sell Order
	 * 
	 *************************************************************************************/
	public static void createSellOrder(int iCount, String wbName) {
		String sellId;
		String module = "EquitySellOrder";
		logs.info("Reference Number : " + module + " " +  iCount);
		String[] colName = {  "SellOrderID", "Status"};
		createHeaders(module, colName);
		/*
		 * secuirtyCustomer = inputCustomer(wbName);
		 * authorizeCustomer(secuirtyCustomer); accountID =
		 * inputAccountCreation(secuirtyCustomer, "DDACashAccount", wbName);
		 * authAccount(accountID); customerSecurity(secuirtyCustomer,
		 * "CustomerSecurity", wbName); portfolioId =
		 * createCustomerPortFolio(secuirtyCustomer, accountID, wbName);
		 * depositoryCustomer = inputCustomer(wbName);
		 * authorizeCustomer(depositoryCustomer); customerCharge(depositoryCustomer);
		 * customerSecurity(depositoryCustomer, "Depository", wbName); brokerCustomer =
		 * inputCustomer(wbName); authorizeCustomer(brokerCustomer);
		 * customerSecurity(brokerCustomer, "BROKER", wbName); equityId =
		 * inputEquity(wbName); authoriseEquity(equityId);
		 */
		sellId = inputSellOrder(wbName);
		/*
		 * inputOrderTransmit("BUY",buyId, wbName); inputDealerBlotter("BUY",
		 * brokerCustomer, buyId, wbName); authSecurityTransaction("BUY",
		 * brokerCustomer, buyId, wbName);
		 */
		result.put("ScenarioName", module + iCount);
		/*
		 * result.put("CustomerNumber", secuirtyCustomer); result.put("PortFolioID",
		 * portfolioId); result.put("DepositoryCustomer", depositoryCustomer);
		 * result.put("BrokerCustomer", brokerCustomer);
		 */
//		result.put("EquityId", equityId);
		result.put("SellOrderID", sellId);
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);
	}
	

	/**********************************************************************************************
	 * 
	 * TestCases of DDA Cash Account Creation Fcm
	 * 
	 **********************************************************************************************/
	public static void createDDACashAccount_fcm(int iCount, String wbName) {
		String customerID, accountID;
		String module = "DDACashAccount";
		logs.info("Reference Number : " + module + " " +  iCount);
		String[] colName = { "CustomerNumber", "AccountNumber",  "Status" };
		createHeaders(module, colName);
		customerID = inputCustomer_fcm(wbName);
		authorizeCustomer(customerID);
		accountID = inputAccountCreation(customerID, module, wbName);
		authAccount(accountID);
		result.put("ScenarioName", module + iCount);
		result.put("CustomerNumber", customerID);
		result.put("AccountNumber", accountID);
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);
	}
	
	/**************************************************************************************
	 * 
	 * TestCases of create bond
	 * 
	 *************************************************************************************/
	public static void createBond(int iCount, String wbName) {
		String BondId;
		String module = "Bond";
		logs.info("Reference Number : " + module + " " +  iCount);
		String[] colName = { "BondId", "Status" };
		createHeaders(module, colName);
		BondId = inputBond(wbName);
		
		authoriseEquity(BondId);
		result.put("ScenarioName", module + iCount);
		result.put("BondId", BondId);
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);
		
	}
	/**************************************************************************************
	 * 
	 * TestCases of direct sec trade
	 * 
	 * 
	 *************************************************************************************/
	public static void directSecTrade(int iCount, String wbName) {
		String OrderID;
		String module = "DirectSecTrade";
		logs.info("Reference Number : " + module + " " +  iCount);
		String[] colName = { "OrderID", "Status" };
		createHeaders(module, colName);
		OrderID = inputDirectSecTrade(wbName);
		authoriseDirectSecTrade(OrderID);
		result.put("ScenarioName", module + iCount);
		result.put("OrderID", OrderID);
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);
		
	}
	/**************************************************************************************
	 * 
	 * TestCases of MMPlacementsCallNoticeContract
	 * 
	 * 
	 *************************************************************************************/
	public static void MMPlacementsCallNoticeContract(int iCount, String wbName) {
			
		String OrderID,customerId;
		String module = "MMPlacementsCallNoticeContract";
		logs.info("Reference Number : " + module + " " +  iCount);
//		String[] colName = {"CounterPartyID",  "OrderID", "Status" };
		String[] colName = { "OrderID", "Status" };
		createHeaders(module, colName);
		/*
		 * customerId = inputCounterPartyCutomer(wbName);
		 * authorizeCounterPartyCustomer(customerId);
		 */
	
		
//		OrderID = inputMMPlacementCallNotice(customerId,wbName);
		OrderID = inputMMPlacementCallNotice(wbName);
		authoriseMMPlacementCallNoticeContract(OrderID);
		
		
		result.put("ScenarioName", module + iCount);
//		result.put("CounterPartyID", customerId);
		result.put("OrderID", OrderID);
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);
		
	}
	/**************************************************************************************
	 * 
	 * TestCases of MMTakingCallNoticeContract
	 * 
	 * 
	 *************************************************************************************/
	public static void MMTakingsCallNoticeContract(int iCount, String wbName) {
			
		String OrderID,customerId;
		String module = "MMTakingsCallNoticeContract";
		logs.info("Reference Number : " + module + " " +  iCount);
//		String[] colName = {"CounterPartyID",  "OrderID", "Status" };
		String[] colName = {  "OrderID", "Status" };
		createHeaders(module, colName);
		/*
		 * customerId = inputCounterPartyCutomer(wbName);
		 * authorizeCounterPartyCustomer(customerId);
		 */
	
		
//		OrderID = inputMMTakingCallNotice(customerId,wbName);
		OrderID = inputMMTakingCallNotice(wbName);
		authoriseMMTakingCallNoticeContract(OrderID);
		
		
		result.put("ScenarioName", module + iCount);
//		result.put("CounterPartyID", customerId);
		result.put("OrderID", OrderID);
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);
		
	}
	/**************************************************************************************
	 * 
	 * TestCases of MMPlacementsFixedMaturity
	 * 
	 * 
	 *************************************************************************************/
	public static void MMPlacementsFixedMaturity(int iCount, String wbName) {
			
		String OrderID,customerId;
		String module = "MMPlacementsFixedMaturity";
		logs.info("Reference Number : " + module + " " +  iCount);
//		String[] colName = {"CounterPartyID",  "OrderID", "Status" };
		String[] colName = {  "OrderID", "Status" };
		createHeaders(module, colName);
		/*
		 * customerId = inputCounterPartyCutomer(wbName);
		 * authorizeCounterPartyCustomer(customerId);
		 */
	
		
		OrderID = inputMMPlacementFixedMaturity(wbName);
//		OrderID = inputMMPlacementFixedMaturity(customerId,wbName);
		authoriseMMPlacementFixedMaturity(OrderID);
		
		
		result.put("ScenarioName", module + iCount);
//		result.put("CounterPartyID", customerId);
		result.put("OrderID", OrderID);
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);
		
	}
	/**************************************************************************************
	 * 
	 * TestCases of MMTakingsFixedMaturity
	 * 
	 * 
	 *************************************************************************************/
	public static void MMTakingsFixedMaturity(int iCount, String wbName) {
			
		String OrderID,customerId;
		String module = "MMTakingsFixedMaturity";
		logs.info("Reference Number : " + module + " " +  iCount);
//		String[] colName = {"CounterPartyID",  "OrderID", "Status" };
		String[] colName = { "OrderID", "Status" };
		createHeaders(module, colName);
		/*
		 * customerId = inputCounterPartyCutomer(wbName);
		 * authorizeCounterPartyCustomer(customerId);
		 */
	
		
//		OrderID = inputMMTakingFixedMaturity(customerId,wbName);
		OrderID = inputMMTakingFixedMaturity(wbName);
		authoriseMMTakingFixedMaturity(OrderID);
		
		
		result.put("ScenarioName", module + iCount);
//		result.put("CounterPartyID", customerId);
		result.put("OrderID", OrderID);
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);
		
	}
	/**************************************************************************************
	 * 
	 * TestCases of SwapIRS_TradeDeal
	 * 
	 * 
	 *************************************************************************************/
	public static void SwapIRSTradeDeal(int iCount, String wbName) {
			
		String OrderID,customerId;
		String module = "IRSTradeDeal";
		logs.info("Reference Number : " + module + " " +  iCount);
//		String[] colName = {"CounterPartyID",  "OrderID", "Status" };
		String[] colName = { "OrderID", "Status" };
		createHeaders(module, colName);
		/*
		 * customerId = inputCounterPartyCutomer(wbName);
		 * authorizeCounterPartyCustomer(customerId);
		 */
		
//		OrderID = inputIRSTradeDeal(customerId,wbName);
		OrderID = inputIRSTradeDeal(wbName);
		authoriseSwapIRSTradeDeal(OrderID);
		
		
		result.put("ScenarioName", module + iCount);
//		result.put("CounterPartyID", customerId);
		result.put("OrderID", OrderID);
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);
		
	}
	/**************************************************************************************
	 * 
	 * TestCases of SwapIRS_HedgeDeal
	 * 
	 * 
	 *************************************************************************************/
	public static void SwapIRSHedgeDeal(int iCount, String wbName) {
			
		String OrderID,customerId;
		String module = "IRSHedgeDeal";
		logs.info("Reference Number : " + module + " " +  iCount);
//		String[] colName = {"CounterPartyID",  "OrderID", "Status" };
		String[] colName = {  "OrderID", "Status" };
		createHeaders(module, colName);
		/*
		 * customerId = inputCounterPartyCutomer(wbName);
		 * authorizeCounterPartyCustomer(customerId);
		 */
		
//		OrderID = inputIRSHedgeDeal(customerId,wbName);
		OrderID = inputIRSHedgeDeal(wbName);
		authoriseSwapIRSHedgeDeal(OrderID);
		
		
		result.put("ScenarioName", module + iCount);
//		result.put("CounterPartyID", customerId);
		result.put("OrderID", OrderID);
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);
		
	}
	/**************************************************************************************
	 * 
	 * TestCases of SwapCIRS_TradeDeal
	 * 
	 * 
	 *************************************************************************************/
	public static void SwapCIRSTradeDeal(int iCount, String wbName) {
			
		String OrderID,customerId;
		String module = "CIRSTradeDeal";
		logs.info("Reference Number : " + module + " " +  iCount);
//		String[] colName = {"CounterPartyID",  "OrderID", "Status" };
		String[] colName = { "OrderID", "Status" };
		createHeaders(module, colName);
		/*
		 * customerId = inputCounterPartyCutomer(wbName);
		 * authorizeCounterPartyCustomer(customerId);
		 */
		
//		OrderID = inputCIRSTradeDeal(customerId,wbName);
		OrderID = inputCIRSTradeDeal(wbName);
		authoriseSwapCIRSTradeDeal(OrderID);
		
		
		result.put("ScenarioName", module + iCount);
//		result.put("CounterPartyID", customerId);
		result.put("OrderID", OrderID);
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);
		
	}
	/**************************************************************************************
	 * 
	 * TestCases of SwapCIRS_HedgeDeal
	 * 
	 * 
	 *************************************************************************************/
	public static void SwapCIRSHedgeDeal(int iCount, String wbName) {
			
		String OrderID,customerId;
		String module = "CIRSHedgeDeal";
		logs.info("Reference Number : " + module + " " +  iCount);
//		String[] colName = {"CounterPartyID",  "OrderID", "Status" };
		String[] colName = { "OrderID", "Status" };
		createHeaders(module, colName);
		/*
		 * customerId = inputCounterPartyCutomer(wbName);
		 * authorizeCounterPartyCustomer(customerId);
		 * 
		 */
//		OrderID = inputCIRSHedgeDeal(customerId,wbName);
		OrderID = inputCIRSHedgeDeal(wbName);
		authoriseSwapCIRSHedgeDeal(OrderID);
		
		
		result.put("ScenarioName", module + iCount);
//		result.put("CounterPartyID", customerId);
		result.put("OrderID", OrderID);
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);
		
	}
	/**************************************************************************************
	 * 
	 * TestCases of FixedFiduciary
	 * 
	 * 
	 *************************************************************************************/
	public static void FixedFiduciary(int iCount, String wbName) {
			
		String OrderID,customerId;
		String module = "FixedFiduciary";
		logs.info("Reference Number : " + module + " " +  iCount);
		String[] colName = { "OrderID", "Status","PooledID" };
//		String[] colName = {"CounterPartyID",  "OrderID", "Status" };
		createHeaders(module, colName);
		/*
		 * customerId = inputCounterPartyCutomer(wbName);
		 * authorizeCounterPartyCustomer(customerId);
		 */
	
		
//		OrderID = inputFixedFiduciary(customerId,wbName);
		OrderID = inputFixedFiduciary(wbName);
		String pooledgrp = approveFixedFiduciary(OrderID);
			
		
		
		result.put("ScenarioName", module + iCount);
//		result.put("CounterPartyID", customerId);
		result.put("OrderID", OrderID);
		result.put("PooledID", pooledgrp);
		
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);
		
	}
	/**************************************************************************************
	 * 
	 * TestCases of FixedFiduciary
	 * 
	 * 
	 *************************************************************************************/
	public static void InputFiduciaryPlacement(int iCount, String wbName) {
			
		String OrderID,customerId;
		String module = "InputFiduciaryPlacement";
		logs.info("Reference Number : " + module + " " +  iCount);
		String[] colName = { "OrderID", "Status","PooledID","FD_ID" };
//		String[] colName = {"CounterPartyID",  "OrderID", "Status" };
		createHeaders(module, colName);
		/*
		 * customerId = inputCounterPartyCutomer(wbName);
		 * authorizeCounterPartyCustomer(customerId);
		 */
	
		
//		OrderID = inputFixedFiduciary(customerId,wbName);
		OrderID = inputFixedFiduciary(wbName);
		String pooledgrp = approveFixedFiduciary(OrderID);
		String fdId =inputFixedFiduciaryPlacement(pooledgrp, wbName);
		
		
		
		result.put("ScenarioName", module + iCount);
//		result.put("CounterPartyID", customerId);
		result.put("OrderID", OrderID);
		result.put("PooledID", pooledgrp);
		result.put("FD_ID", fdId);
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);
		
	}
	/**************************************************************************************
	 * 
	 * TestCases of NoticeFiduciary
	 * 
	 * 
	 *************************************************************************************/
	public static void NoticeFiduciary(int iCount, String wbName) {
			
		String OrderID,customerId;
		String module = "NoticeFiduciary";
		logs.info("Reference Number : " + module + " " +  iCount);
		String[] colName = { "OrderID", "Status","PooledID" };
		createHeaders(module, colName);
		OrderID = inputNoticeFiduciary(wbName);
		String pooledgrp = approveNoticeFiduciary(OrderID);
		result.put("ScenarioName", module + iCount);
		result.put("OrderID", OrderID);
		result.put("PooledID", pooledgrp);
		
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);
		
	}
	/**************************************************************************************
	 * 
	 * TestCases of Equity Buy Order
	 * 
	 *************************************************************************************/
	public static void pre_requisite_directsectrade(int iCount, String wbName) {
		String secuirtyCustomer, depositoryCustomer, brokerCustomer , accountID, portfolioId, equityId;
		String module = "pre_requisite_directsectrade";
		logs.info("Reference Number : " + module + " " +  iCount);
		String[] colName = { "CustomerNumber", "AccountNumber", "DepositoryCustomer", "BrokerCustomer", "PortFolioID", "EquityID",  "Status" };
		createHeaders(module, colName);
		secuirtyCustomer = inputCustomer(wbName);
		authorizeCustomer(secuirtyCustomer);
		accountID = inputAccountCreation(secuirtyCustomer, "DDACashAccount", wbName);
		authAccount(accountID);
		customerSecurity(secuirtyCustomer, "CustomerSecurity", wbName);
		portfolioId = createCustomerPortFolio(secuirtyCustomer, accountID, wbName);
		depositoryCustomer = inputCustomer(wbName);
		authorizeCustomer(depositoryCustomer);
		customerCharge(depositoryCustomer);
		customerSecurity(depositoryCustomer, "Depository", wbName);
		brokerCustomer = inputCustomer(wbName);
		authorizeCustomer(brokerCustomer);
		customerSecurity(brokerCustomer, "BROKER", wbName);
		equityId = inputEquity(wbName);
		authoriseEquity(equityId);
		
		result.put("ScenarioName", module + iCount);
		result.put("CustomerNumber", secuirtyCustomer);
		result.put("PortFolioID", portfolioId);
		result.put("DepositoryCustomer", depositoryCustomer);
		result.put("BrokerCustomer", brokerCustomer);
		result.put("EquityId", equityId);
		
		if (status) {
			result.put("Status", "PASS");
		} else {
			result.put("Status", "FAIL");
		}
		updateTestData(module, result);
	}
	 //*************************************************************************************/
	public static void getService(String iURl) {

		try {
			Connection.Response appUrl = Jsoup.connect(iURl).method(Connection.Method.GET).userAgent(User_Agent).timeout(20000)
					.execute();

			cookies.putAll(appUrl.cookies());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logs.error(e.getMessage());
		}

	}

	public static Document loginPostService(String iUrl, HashMap<String, String> formData) {
		Document responseText = null;
		try {
			Connection.Response loginForm = Jsoup.connect(iUrl).cookies(cookies).data(formData)
					.method(Connection.Method.POST).userAgent(User_Agent).timeout(70000).execute();
			cookies.clear();

			cookies.putAll(loginForm.cookies());
			responseText = loginForm.parse();
		} catch (IOException e) {
				logs.error(e.getMessage());
		}
		return responseText;
	}

	public static Document postService(String iUrl, HashMap<String, String> formData) {
		Document responseText = null;
		try {
			Connection.Response serviceUrl = Jsoup.connect(iUrl).cookies(cookies).data(formData)
					.method(Connection.Method.POST).userAgent(User_Agent).timeout(70000).execute();

			responseText = serviceUrl.parse();

		} catch (IOException e) {
			logs.error(e.getMessage());
		}
		return responseText;

	}

	public static HashMap<String, String> extractFormData(String ScenarioName) {
		com.codoid.products.fillo.Connection connection = null;
		String sheetName = null;
		if(iRegion.equals("CH")) {
			sheetName = "CH";
		}else {
			sheetName = "US";
		}
		Recordset recordset = null;
		try {
			connection = fillo.getConnection("./resources/DGenInput.xlsx");
			String strQuery = "Select * from " + sheetName + " where ScenarioName='" + ScenarioName + "'";
			recordset = connection.executeQuery(strQuery);
			while (recordset.next()) {
				ArrayList<String> ColCollection = recordset.getFieldNames();
				int iCount;
				int size = ColCollection.size();
				for (int Iter = 0; Iter <= (size - 1); Iter++) {

					String ColName = ColCollection.get(Iter);
					String ColValue = recordset.getField(ColName);
					if (!ColName.equalsIgnoreCase("ScenarioName")) {
						if (!ColValue.isEmpty()) {
							testData.put(ColName, ColValue);
						}

					}

				}
			}

		} catch (FilloException e) {
			// TODO Auto-generated catch block 
			logs.error(e.getMessage());
		} finally {
			recordset.close();
			connection.close();
		}

		return testData;
	}

	public static HashMap<String, String> extractExcelData(String sheetname, String wbName) {
		com.codoid.products.fillo.Connection connection = null;
		Recordset recordset = null;
		int iRecordCount, i = 1;
		try {
			connection = fillo.getConnection("./resources/InputData/" + iRegion + "/"+ wbName +".xlsx");
			String strQuery = "Select * from " + sheetname + " where ExecutionFlag='Y'";
			recordset = connection.executeQuery(strQuery);
			iRecordCount = recordset.getCount();
			if(!iteration.containsKey(sheetname)) {
				iteration.put(sheetname, 1);
			}
			
			
			if(iteration.get(sheetname) <= iRecordCount) {
				//iteration++;
			}else {
				iteration.put(sheetname, 1);
			}
			while (recordset.next()) {
				int svalue = iteration.get(sheetname);
				if(i == iteration.get(sheetname)) {
					ArrayList<String> ColCollection = recordset.getFieldNames();
					int iter;
					int size = ColCollection.size();
					for (iter = 0; iter <= (size - 1); iter++) {
	
						String ColName = ColCollection.get(iter);
						String ColValue = recordset.getField(ColName);
						if (!ColName.equalsIgnoreCase("ScenarioName")) {
							if (!ColValue.isEmpty()) {
								testData.put(ColName, ColValue);
							}
	
						}
						
					}
					svalue++;
					iteration.put(sheetname, svalue);					
					break;
					}else {
						i++;
					}
					
			}

		} catch (FilloException e) {
			// TODO Auto-generated catch block
			logs.error(e.getMessage());
		} finally {
			recordset.close();
			connection.close();
		}
 
		return testData;
	}

	public static void updateTestData(String Sheetname, HashMap<String, String> result) {
		com.codoid.products.fillo.Connection connection = null;
		try {
			connection = fillo.getConnection(reportfile);
			StringBuffer setKey = new StringBuffer();
			StringBuffer setValue = new StringBuffer();
			for (Entry<String, String> rst : result.entrySet()) {
				if (setKey.toString().isEmpty()) {
					setKey.append("(" + rst.getKey());
				} else {
					setKey.append("," + rst.getKey());
				}

				if (setValue.toString().isEmpty()) {
					setValue.append("('" + rst.getValue() + "'");
				} else {
					setValue.append(", '" + rst.getValue() + "'");
				}
			}

			String strQuery = "Insert INTO " + Sheetname + setKey.toString() + ") VALUES " + setValue.toString() + ")";
			connection.executeUpdate(strQuery);
			connection.close();
		} catch (FilloException e) {
			
			logs.error(e.getMessage());
		}

	}
	
	public void callMethodByModule(String moduleName, Integer iCount, String region) {
		
		columnFlag = true;
		iRegion = region;
		iteration = new HashMap<>();
		for (int i = 1; i <= iCount; i++) {
	        logs.info("***********************************************************");
			result = new HashMap<>();
			status = true;
			switch (moduleName.toLowerCase()) {
			case "01.containercustomer":
				containerCustomerCreation(i, moduleName);
				break;
			case "02.ddacashaccount":
				createDDACashAccount(i, moduleName);
				break;
			case "03.moneymarketaccount":
				createMoneyMarketAccount(i, moduleName);
				break;	
			case "04.marginaccount":
				createMarginAccount(i, moduleName);
				break;
				
			case "05.customerportfolio":
				customerPortFolio( i, moduleName);
				break;
			case "06.fundtransfer":
				fundTransfer( i, moduleName);
				break;
			case "07.broker":
				createBroker( i, moduleName);
				break;
			case "08.depository":
				createDepository(i, moduleName);
				break;
			case "09.counterparty":
				counterPartyCreation( i,moduleName);
				break;
			case "10.nostroaccount":
				createNostroAccount( i,moduleName);
				break;
			case "11.equity":
				createEquity(i, moduleName);
				break;
			case "12.equitybuyorder":
				createEquityBuyOrder(i, moduleName); 
				break;
			case "13.equitysellorder":
				createEquityBuySellOrder(i, moduleName);
				break;
			case "14.feesdata":
				feesTestData( i, moduleName);
				break;
			case "15.aum_account":
				createAUMAccount(i, moduleName);
				break;
			case "16.aum_portfolio":
				createAUMPortfolio(i, moduleName);
				break;
			case "17.aum_fundtransfer":
				
				createAUMFundTransfer(i, moduleName);
				break;
			case "18.economicgroup":
				createEconomicGroup(i, moduleName);
				break;
				
			case "19.buyorder":
				createBuyOrder(i, moduleName);
				break;
				
			case "20.sellorder":
				createSellOrder(i, moduleName);
				break;
				
			case "21.ddacashaccount_fcm":
				createDDACashAccount_fcm(i, moduleName);
				break;
				
			case "22.bond":
				createBond(i, moduleName);
				break;
				
			case "23.directsectrade":
				directSecTrade(i, moduleName);
				break;
				
			case "24.mmplacements_callnoticecontract":
				MMPlacementsCallNoticeContract(i, moduleName);
				break;
				
			case "25.mmtakings_callnoticecontract":
				MMTakingsCallNoticeContract(i, moduleName);
				break;	
			
			case "26.mmplacements_fixedmaturity":
				MMPlacementsFixedMaturity(i, moduleName);
				break;	
				
			case "27.mmtakings_fixedmaturity":
				MMTakingsFixedMaturity(i, moduleName);
				break;	
			
			case "28.swapirs_tradedeal":
				SwapIRSTradeDeal(i, moduleName);
				break;		
				
			case "29.swapirs_hedgedeal":
				SwapIRSHedgeDeal(i, moduleName);
				break;	
				
			case "30.swapcirs_tradedeal":
				SwapCIRSTradeDeal(i, moduleName);
				break;
				
			case "31.swapcirs_hedgedeal":
				SwapCIRSHedgeDeal(i, moduleName);
				break;
			
			case "32.fixedfiduciary":
				FixedFiduciary(i, moduleName);
				break;
				
			case "33.noticefiduciary":
				NoticeFiduciary(i, moduleName);
				break;
			
			case "34.inputfiduciaryplacement":
				InputFiduciaryPlacement(i, moduleName);
				break;
			
			case "35.pre_requisite_directsectrade":
				pre_requisite_directsectrade(i, moduleName);
				break;
				
			default:
				break;
			}
		}

	}
	
	
	
	
	private static void createHeaders(String name, String[] arg) {
		if (columnFlag) {
			XSSFWorkbook workbook;
			XSSFSheet sheet;
			FileOutputStream out;
			Row row;
			Cell cell;
			File fl = new File(reportfile);
			try {
				FileInputStream input = new FileInputStream(fl);
				workbook = new XSSFWorkbook(input);
				sheet = workbook.createSheet(name);
				row = sheet.createRow(0);
				cell = row.createCell(0);
				sheet.setColumnWidth(0, 5000);
				System.out.println("Scenarioname  created");
				cell.setCellValue("ScenarioName");
				for (int i = 0; i < arg.length; i++) {
					cell = row.createCell(i + 1);
					cell.setCellValue(arg[i]);
					sheet.setColumnWidth(i+1, 5000);
					System.out.println(arg[i] + "created");
					
				}
				
				out = new FileOutputStream(fl);
				workbook.write(out);
				out.close();
				columnFlag = false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logs.error(e.getMessage());
			}
		}

	}

	public static void enableSSLSocket() throws NoSuchAlgorithmException, KeyManagementException {
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {

			@Override
			public boolean verify(String hostname, SSLSession session) {
				// TODO Auto-generated method stub
				return true;
			}
		});

		SSLContext context = SSLContext.getInstance("TLS");
		context.init(null, new X509TrustManager[] { new X509TrustManager() {
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}

			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}

			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}
		} }, new SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
	}

	@Override
	public void run() {
		boolean chFlag = false, usFlag =false;
		RollingFileAppender appender = new RollingFileAppender();
		
		this.createOutputFolder();
		for(Modules lst: moduleList) {
			if(lst.getICHSelect().isSelected()) {
				chFlag = true;
			}
			if(lst.getIUSSelect().isSelected()) {
				usFlag = true;
			}
		}
		if(chFlag) {
			logs.removeAppender(appender);
			this.logFile =reportPath + "/"+ "logFile_CH.log";
			appender.setAppend(true);
			appender.setFile(this.logFile);
			appender.activateOptions();
			PatternLayout layOut = new PatternLayout();
	        layOut.setConversionPattern("%d{yyyy-MM-dd HH:mm:ss} %-5p [%c{1}] - %m%n");
	        appender.setLayout(layOut);
	        logs.addAppender(appender);
			this.createOutputFile("CHOutputData");
			for (Modules lst : moduleList) {
				if (lst.getICHSelect().isSelected()) {
					// this.createsheet(lst.getModuleName());
					int endNumber = Integer.parseInt(lst.getICount());
					callMethodByModule(lst.getModuleName(), endNumber, "CH");
				}
			}
		}
		
		if(usFlag) {
			this.logFile =reportPath + "/"+ "logFile_US.log";
			logs.removeAppender(appender);
			appender.setAppend(true);
			appender.setFile(this.logFile);
			appender.activateOptions();
			PatternLayout layOut = new PatternLayout();
	        layOut.setConversionPattern("%d{yyyy-MM-dd HH:mm:ss} %-5p [%c{1}] - %m%n");
	        appender.setLayout(layOut);
	        logs.addAppender(appender);
			this.createOutputFile("USOutputData");
			for (Modules lst : moduleList) {
				if (lst.getIUSSelect().isSelected()) {
					int endNumber = Integer.parseInt(lst.getICount());
					callMethodByModule(lst.getModuleName(), endNumber, "US");
				} 
			}
		}
			
		JOptionPane.showMessageDialog(null, "Completed", "Information", 1);
	}
	
	
	private void createOutputFolder() {
		SimpleDateFormat formatdate = new SimpleDateFormat("dd-MM-YYYY");
		java.util.Date date = new java.util.Date(); 
		String newDate = formatdate.format(date).toString();
		System.out.println(newDate);
		reportPath = "./reports/"+newDate;
		 File directory = new File(reportPath);
	  		if (!directory.exists()) {
	  			directory.mkdirs();
	  			System.out.println("Output folder created");
	  		}
	  		formatdate = new SimpleDateFormat("HH-mm-ss");
			newDate = formatdate.format(date).toString();
			reportPath = reportPath + "/" + newDate;
			directory = new File(reportPath);
			if (!directory.exists()) {
	  			directory.mkdirs();
	  			logs.info("Output folder created");
	  		}
	}
	
	
	
	
	private void createOutputFile(String fileName) {
		this.fileName = fileName;	
		this.reportfile = reportPath + "/" + this.fileName +".xlsx";
	  	File fl = new File(reportfile);
	  	if (!fl.exists()){	
	  		try {
	  			FileOutputStream out = new FileOutputStream(fl);
	  			XSSFWorkbook workbook = new XSSFWorkbook();
				workbook.write(out);
				logs.info("Output File created");
				out.close();
			} catch (IOException e) {
				logs.error(e.getMessage());
			}
	  	}
	}
	
	
	
	public void loadReportPath()  {
		try {
			File relPath = new File(reportPath);
			File parentFolder = new File(relPath.getParent());
			File absPath = new File(parentFolder, "../."+ reportPath );
			String absolute = absPath.getCanonicalPath() ;
			System.out.println(absolute);
			Runtime.getRuntime().exec("cmd /c start "+ absolute);
		} catch (IOException e) {
			
			logs.error("No Output files Generated");
		}
	}
	

}
