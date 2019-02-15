package snowCreateIncident;

import java.io.BufferedReader;

// Creates an incident in service-now, given an external json file with key/value pairs
//
// See readme.MD for details!

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;


public class SNOW_Create_Incident {
	public static void main(String[] args) throws Exception {
		// Sets some cmd args + etc when compiling/running in Eclipse IDE
		boolean testing_in_eclipse = true;
		
		// This java is designed to be run as runnable jar file with argument specifying an input file
		// ex cmd:"C:\Program Files\Java\jdk-11.0.1\bin\java.exe" -Djson_input_file=create_incident.json -Dheadless=on -Dchrome_driver=C:\Users\a78808\Documents\eclipse-workspace\chromedriver_win32\chromedriver.exe -jar C:\Users\a78808\ci_test.jar
		String json_input_file = System.getProperty("json_input_file");

		// For testing in eclipse IDE
		if (testing_in_eclipse) {
			json_input_file = "C:\\Users\\a78808\\create_incident.json";
		}
		
		// Open the file and parse
		JSONParser jsonParser = new JSONParser();
		JSONObject json_object = new JSONObject();
		try (FileReader reader = new FileReader(json_input_file))
			{
			//Read JSON file
            	json_object = (JSONObject) jsonParser.parse(reader);
            	//System.out.println(json_obj);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.exit(1); 
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1); 
			} catch (ClassCastException e) {
				e.printStackTrace();
				System.out.println("Please review/validate the format of the JSON file!");
				System.exit(1); 
			}
	
		// Convert the json to strings, just to keep things readable for now
		String snow_url = (String) (json_object).get("snow_url");
		String username = (String) (json_object).get("username");
		String password = (String) (json_object).get("password");
		String input_caller = (String) (json_object).get("caller");
		String input_category = (String) (json_object).get("category");
		String input_subcategory = (String) (json_object).get("subcategory");
		String input_configuration_item = (String) (json_object).get("configuration_item");
		String input_assignment_group = (String) (json_object).get("assignment_group");
		String input_application_ci = (String) (json_object).get("application_ci");
		String input_location = (String) (json_object).get("location");
		String input_impact = (String) (json_object).get("impact");
		String input_urgency = (String) (json_object).get("urgency");
		String input_priority = (String) (json_object).get("priority");
		String input_assigned_to = (String) (json_object).get("assigned_to");
		String input_short_description = (String) (json_object).get("short_description");
		String input_worknotes = (String) (json_object).get("worknotes");
		
		// Check some key strings, verify we have enough data to log in.  We'll use
		// Service-now xpath's for error indicators to verify some other values later
		if (snow_url == null || snow_url.isEmpty())  {
			System.out.println("Please validate value for \"snow_url\" in json_input_file.");
			System.exit(1);
		}
		if (username == null || username.isEmpty())  {
			System.out.println("Please validate value for \"username\" in json_input_file.");
			System.exit(1);
		}
		if (password == null || password.isEmpty())  {
			System.out.println("Please validate value for \"password\" in json_input_file.");
			System.exit(1);
		}
		
		// Check for "windowsregistry::" in the username/password fields.  If so, read keys from winreg
		if (username.contains("windowsregistry::")) {
			try {
				System.out.println("Winreg for username detected, reading username key");
			
				String username_key_path = username.toString().split("::")[1];
				String username_key = username.toString().split("::")[2];
				// Reading windows registry keys is a little tricky, we invoke the cmd line reg tool naad read output
				ProcessBuilder builder = new ProcessBuilder("reg", "query", username_key_path);
				Process reg = builder.start();
				try (BufferedReader output = new BufferedReader(
				    new InputStreamReader(reg.getInputStream()))) {
					
				    Stream<String> keys = output.lines().filter(l -> !l.isEmpty());
				    Stream<String> matches = keys.filter(l -> l.contains(username_key));
				    Optional<String> reg_username_str = matches.findFirst();
				    String reg_username = reg_username_str.toString().split("REG_SZ")[1].split("]")[0].stripLeading();
				    //System.out.println("reg_username: " + reg_username);
				    username = reg_username;
				    //System.out.println("reg_password: " + reg_password);
				    // Use key ... 
				} catch (Exception ArrayIndexOutOfBoundsException) {
					System.out.println(username_key_path + "\\" + username_key + " not found or permissions incorrect!");
					System.exit(1);
				}
				reg.waitFor();
			} catch (Exception ArrayIndexOutOfBoundsException) {
				System.out.println("username windowsregistry::registry path::registry key format was invalid, please review .json config file");
				System.exit(1);
			}
		}
		
		// Check for "windowsregistry::" in the username/password fields.  If so, read keys from winreg
		if (password.contains("windowsregistry::")) {
			try {
				System.out.println("Winreg for password detected, reading password key");
			
				String password_key_path = password.toString().split("::")[1];
				String password_key = password.toString().split("::")[2];
				// Reading windows registry keys is a little tricky, we invoke the cmd line reg tool naad read output
				ProcessBuilder builder = new ProcessBuilder("reg", "query", password_key_path);
				Process reg = builder.start();
				try (BufferedReader output = new BufferedReader(
				    new InputStreamReader(reg.getInputStream()))) {
					
				    Stream<String> keys = output.lines().filter(l -> !l.isEmpty());
				    Stream<String> matches = keys.filter(l -> l.contains(password_key));
				    Optional<String> reg_password_str = matches.findFirst();
				    String reg_password = reg_password_str.toString().split("REG_SZ")[1].split("]")[0].toString().stripLeading();
				    //System.out.println("reg_password: " + reg_password);
				    password = reg_password;
				    //System.out.println("reg_password: " + reg_password);
				    // Use key ... 
				} catch (Exception ArrayIndexOutOfBoundsException) {
					System.out.println(password_key_path + "\\" + password_key + " not found or permissions incorrect!");
					System.exit(1);
				}
				reg.waitFor();
			} catch (Exception ArrayIndexOutOfBoundsException) {
				System.out.println("password windowsregistry::registry path::registry key format was invalid, please review .json config file");
				System.exit(1);
			}
				}		
		
		
		System.out.println("Connecting to " + snow_url);
		System.out.println("(username: " + username + "   password: ****)");
		
		
		// Keep track of how much time this process takes
		long start = System.currentTimeMillis();
		
		// Set location of chromedriver
		String chrome_driver = System.getProperty("chrome_driver");
		if (testing_in_eclipse) {
			chrome_driver = "C:\\Users\\a78808\\Documents\\eclipse-workspace\\chromedriver_win32\\chromedriver.exe";
		}
		if (chrome_driver != null) {
			System.setProperty("webdriver.chrome.driver", chrome_driver);
		} else {
			System.out.println("Error, please specify location of Chrome driver: -Dchrome_driver="); 
		}
		
		ChromeOptions options = new ChromeOptions();
        
		// Check cmd line argument for headless-ness
		String headlessness = System.getProperty("headless");
		//System.out.println("headlessness: " + headlessness);
		if (headlessness != null) {
			if (headlessness.equals((String)"on")) {
				options.addArguments("headless");
				options.addArguments("window-size=600x600");
				options.addArguments("--proxy-server='direct://'");
				options.addArguments("--proxy-bypass-list=*");
			}
		}
		
		WebDriver driver = new ChromeDriver(options);
        driver.get(snow_url);
        
        // execution will wait until DOM finished loading SNOW login page
        System.out.println("Connected: " + driver.getTitle());
        if (!(driver.getTitle()).contains("ServiceNow Production Environment")) {
        	System.out.println("Error connecting to Service-Now login page, please verify snow_url json parameter + connectivity.");
        	driver.quit();
        	System.exit(1);
        }
        
        // login to Service-Now
		try {
			WebElement username_box = find_first_element("//*[@id=\"user_name\"]", driver);
			username_box.sendKeys(username);
		} catch (Exception NoSuchElementException) {
			System.out.println("Error, cannot locate Service-Now login input.");
        	driver.quit();
        	System.exit(1);
		}
		
		
		WebElement password_box = find_first_element("//*[@id=\"user_password\"]", driver);
		password_box.sendKeys(password);
		
		WebElement login_button = find_first_element("//*[@id=\"sysverb_login\"]", driver);
		login_button.click();
        
		// Check for login error message, end run if login failure
		if (checkForElement("//*[@id=\"output_messages\"]/div/div/div", driver)) {
			System.out.println("Error logging in to Service-Now, please verify login credentials.");
			driver.quit();
			System.exit(1);
		}
		
		//create an incident
    	WebElement allApps_tab = find_first_element("//*[@id=\"allApps_tab\"]", driver);
    	allApps_tab.click();
    	WebElement create_new_incident = find_first_element("//*[@id=\"14641d70c611228501114133b3cc88a1\"]/div/div", driver);
    	create_new_incident.click();
    	
    	//get incident number
    	//*[@id="sys_readonly.incident.number"]
    	WebElement new_inc_no = find_first_element("//*[@id=\"sys_readonly.incident.number\"]", driver);
    	String incident = new_inc_no.getAttribute("value");
    	System.out.println("incident number is: " + incident);
    	
    	//set caller info
    	//WebElement var_name = find_first_element("", driver);
    	WebElement caller = find_first_element("//*[@id=\"sys_display.incident.caller_id\"]", driver);  
    	caller.sendKeys(input_caller);
    	caller.sendKeys(Keys.TAB);
    	sleepytime(3000);
    	
    	//set category
    	WebElement category = find_first_element("//*[@id=\"incident.category\"]", driver);
    	Select category_dropdown = new Select(category);
    	try {
    		category_dropdown.selectByValue(input_category);
    	} catch (Exception NoSuchElementException) {
    		System.out.println("Service-Now reporting error in \"category\" field, please verify category input");
    		driver.quit();
    		System.exit(1);
    	}
    	sleepytime(3000);
    	
    	//set subcategory
    	WebElement subcategory = find_first_element("//*[@id=\"incident.subcategory\"]", driver);
    	Select subcategory_dropdown = new Select(subcategory);
    	try {
    		subcategory_dropdown.selectByValue(input_subcategory);
    	} catch (Exception NoSuchElementException) {
    		System.out.println("Service-Now reporting error in \"subcategory\" field, please verify subcategory input");
    		driver.quit();
    		System.exit(1);
    	}
    	sleepytime(1000);

    	//Set Configuration Item
    	WebElement configuration_item = find_first_element("//*[@id=\"sys_display.incident.cmdb_ci\"]", driver);  
    	configuration_item.sendKeys(input_configuration_item);
    	configuration_item.sendKeys(Keys.TAB);
    	sleepytime(2000);
    	
    	//Set assignment group
    	WebElement assignment_group = find_first_element("//*[@id=\"sys_display.incident.assignment_group\"]", driver);  
    	assignment_group.sendKeys(input_assignment_group);
    	assignment_group.sendKeys(Keys.TAB);
    	sleepytime(2000);
    	
    	//Set application CI
    	//Lab - WIGLE - APP012527
    	WebElement application_ci = find_first_element("//*[@id=\"sys_display.incident.u_serv_appl_ci\"]", driver);  
    	application_ci.sendKeys(input_application_ci);
    	application_ci.sendKeys(Keys.TAB);
    	//driver.switchTo().frame(0);
    	sleepytime(1000);
    	
    	//set location
    	WebElement location = find_first_element("//*[@id=\"incident.u_location\"]", driver);  
    	location.sendKeys(input_location);
    	location.sendKeys(Keys.TAB);

    	//set impact (dropdown)
    	WebElement impact = find_first_element("//*[@id=\"incident.impact\"]", driver);
    	impact.click();
    	impact.sendKeys(input_impact);
    	impact.sendKeys(Keys.ENTER);
    	
    	//Select impact_dropdown = new Select(subcategory);
    	//impact_dropdown.selectByValue("4");
        
    	//set urgency (dropdown)
    	WebElement urgency = find_first_element("//*[@id=\"incident.urgency\"]", driver);
    	urgency.click();
    	urgency.sendKeys(input_urgency);
    	urgency.sendKeys(Keys.ENTER);
    	
    	//set priority (dropdown)
    	WebElement priority = find_first_element("//*[@id=\"incident.priority\"]", driver);
    	priority.click();
    	priority.sendKeys(input_priority);
    	priority.sendKeys(Keys.ENTER);
    	
    	//set assigned_to
    	WebElement assigned_to = find_first_element("//*[@id=\"sys_display.incident.assigned_to\"]", driver);  
    	assigned_to.sendKeys(input_assigned_to);
    	assigned_to.sendKeys(Keys.TAB);
    	
    	//set short_description
    	WebElement short_description = find_first_element("//*[@id=\"incident.short_description\"]", driver);  
    	//java.util.Date date = new java.util.Date();
    	short_description.sendKeys(input_short_description);
    	short_description.sendKeys(Keys.TAB);
    	
    	//set worknotes
    	WebElement worknotes = find_first_element("//*[@id=\"incident.work_notes\"]", driver);  
    	//java.util.Date date_2 = new java.util.Date();
    	worknotes.sendKeys(input_worknotes);
    	worknotes.sendKeys(Keys.TAB);
    	sleepytime(2000);
    	
    	//
    	// Now check all the various input fields to see if Service-Now is providing a 
    	// 'red flag' for anything.  If there is an error, let the user know @ the console
    	//
    	// caller field:
    	WebElement caller_check = find_first_element("//*[@id=\"status.incident.caller_id\"]", driver);
    	if (!(caller_check.getAttribute("Title").equals("Field value has changed since last update"))) {
    		System.out.println("Service-Now reporting error in \"caller\" field, please verify caller input");
    		driver.quit();
    		System.exit(1);
    	}
    	
    	// category field:
    	WebElement category_check = find_first_element("//*[@id=\"status.incident.category\"]", driver);
    	if (!(category_check.getAttribute("Title").equals("Field value has changed since last update"))) {
    		System.out.println("Service-Now reporting error in \"category\" field, please verify category input");
    		driver.quit();
    		System.exit(1);
    	}
    	
    	// subcategory field:
    	WebElement subcategory_check = find_first_element("//*[@id=\"status.incident.subcategory\"]", driver);
    	if (!(subcategory_check.getAttribute("Title").equals("Field value has changed since last update"))) {
    		System.out.println("Service-Now reporting error in \"subcategory\" field, please verify subcategory input");
    		driver.quit();
    		System.exit(1);
    	}
    	
    	// configuration_item field: (uses input background-color CSS)
    	WebElement configuration_item_check = find_first_element("//*[@id=\"sys_display.incident.cmdb_ci\"]", driver);
    	//System.out.println("configuration_item_check.getCssValue(\"background-color\"): " + configuration_item_check.getCssValue("background-color"));
    	if (!(configuration_item_check.getCssValue("background-color").equals("rgba(255, 255, 255, 1)"))) {
    		System.out.println("Service-Now reporting error in \"configuration_item\" field, please verify configuration_item input");
    		driver.quit();
    		System.exit(1);
    	}
    	
    	// application_ci field:
    	WebElement application_ci_check = find_first_element("//*[@id=\"status.incident.u_serv_appl_ci\"]", driver);
    	if (!(application_ci_check.getAttribute("Title").equals("Field value has changed since last update"))) {
    		System.out.println("Service-Now reporting error in \"application_ci\" field, please verify application_ci input");
    		driver.quit();
    		System.exit(1);
    	}
    	
    	// application_ci field:
    	WebElement assignment_group_check = find_first_element("//*[@id=\"status.incident.assignment_group\"]", driver);
    	if (!(assignment_group_check.getAttribute("Title").equals("Field value has changed since last update"))) {
    		System.out.println("Service-Now reporting error in \"assignment_group\" field, please verify assignment_group input");
    		driver.quit();
    		System.exit(1);
    	}
    	
    	// location field:
    	WebElement location_check = find_first_element("//*[@id=\"status.incident.u_location\"]", driver);
    	if (!(location_check.getAttribute("Title").equals("Field value has changed since last update"))) {
    		System.out.println("Service-Now reporting error in \"location\" field, please verify location input");
    		driver.quit();
    		System.exit(1);
    	}
    	
    	// impact field:
    	WebElement impact_check = find_first_element("//*[@id=\"status.incident.impact\"]", driver);
    	if (!(impact_check.getAttribute("Title").equals("Field value has changed since last update"))) {
    		System.out.println("Service-Now reporting error in \"impact\" field, please verify impact input");
    		driver.quit();
    		System.exit(1);
    	}
    	
    	// urgency field:
    	WebElement urgency_check = find_first_element("//*[@id=\"status.incident.urgency\"]", driver);
    	if (!(urgency_check.getAttribute("Title").equals("Field value has changed since last update"))) {
    		System.out.println("Service-Now reporting error in \"urgency\" field, please verify impact urgency");
    		driver.quit();
    		System.exit(1);
    	}
    	
    	// priority field:
    	WebElement priority_check = find_first_element("//*[@id=\"status.incident.priority\"]", driver);
    	if (!(priority_check.getAttribute("Title").equals("Field value has changed since last update"))) {
    		System.out.println("Service-Now reporting error in \"priority\" field, please verify priority input");
    		driver.quit();
    		System.exit(1);
    	}
    	
    	// short_description field:
    	WebElement short_description_check = find_first_element("//*[@id=\"status.incident.priority\"]", driver);
    	if (!(short_description_check.getAttribute("Title").equals("Field value has changed since last update"))) {
    		System.out.println("Service-Now reporting error in \"short_description\" field, please verify short_description input");
    		driver.quit();
    		System.exit(1);
    	}
    	
    	// assigned_to field: (uses input background-color CSS)
    	WebElement assigned_to_check = find_first_element("//*[@id=\"sys_display.incident.assigned_to\"]", driver);
    	//System.out.println("assigned_to_check.getCssValue(\"background-color\"): " + assigned_to_check.getCssValue("background-color"));
    	if (!(assigned_to_check.getCssValue("background-color").equals("rgba(255, 255, 255, 1)"))) {
    		System.out.println("Service-Now reporting error in \"assigned_to\" field, please verify assigned_to_check input");
    		driver.quit();
    		System.exit(1);
    	}
    	
    	//hit submit button
    	WebElement submit_button = find_first_element("//*[@id=\"sysverb_insert\"]", driver);
    	sleepytime(2000);
    	submit_button.click();
    	
		
    	// Check and see if there was an error after the submission:
    	
    	//Set Configuration Item
    	try {
    		WebElement output_message = find_first_element("//*[@id=\"output_messages\"]/div/div/div", driver);  
	    	String submissions_failure = output_message.getText();
	    	if (submissions_failure.contains("Invalid update")) {
	    		System.out.println("Service-Now reported \"Invalid update\" at the time of submission.");
	    		System.out.println(" - suggest running with -Dheadless=off with console access to watch incident");
	    		System.out.println("   creation progress, and determine where the error is.");
	    		driver.quit();
	    		System.exit(1);
	    	} 
    	} catch (Exception NoSuchElementException) {
    		System.out.println("Incident submitted");
    	}
    	
        long finish = System.currentTimeMillis();
		long timeElapsed = finish - start;
		System.out.println("Elapsed milliseconds: " + timeElapsed);
        driver.quit();
		
	}
	
	public static WebElement find_first_element(String xpath_to_find, WebDriver driver) {
		WebElement generic_element;
		
		// try and get element from current driver context first
		try {
			generic_element = driver.findElement(By.xpath(xpath_to_find));
			//System.out.println("**** (found xpath in current driver context)");
			return generic_element;
		}
		catch (Exception NoSuchElementException) {
    		//System.out.println("NoSuchElementException!! (first try from default driver context)");
    	}
			
		// loop through any available iframes and return the first instance of the WebElement specified by xpath
		List<WebElement> iframes = driver.findElements(By.xpath("//iframe"));
		
		for(WebElement iframe : iframes){
	        //System.out.println("iframe:" + iframe);
	        driver.switchTo().frame(iframe);
	        try {
        		generic_element = driver.findElement(By.xpath(xpath_to_find));
        		//System.out.println("**** (found xpath in iframe context) : " + iframe);
        		return generic_element;
        	}
        	catch (Exception NoSuchElementException) {
        		//System.out.println("NoSuchElementException - " + xpath_to_find + " not found in this frame.");
        	}
	        
	    }
		
		// This is a failsafe designed to throw the NoSuchElementException if the above routines didn't find our element
		generic_element = driver.findElement(By.xpath(xpath_to_find));
		return generic_element;  
	}
	
	private static boolean checkForElement(String xpath, WebDriver driver) {
	    try {
	        driver.findElement(By.xpath(xpath));
	    } catch (Exception NoSuchElementException) {
	        return false;
	    }
	    return true;
	}
	
	// Sleep function
	public static void sleepytime(int a) {
		try        
		{
		    Thread.sleep(a);
		} 
		catch(InterruptedException ex) 
		{
		    Thread.currentThread().interrupt();
		}
	}
}
