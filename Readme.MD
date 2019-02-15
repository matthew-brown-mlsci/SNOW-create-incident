Service-Now Create Incident

2-12-2019
Matthew.Brown1@ascension.org

What is it?

This code provides a way to create an incident in Service-Now (Kingston, Patch 11)
from the windows command line.  It requires a java JRE 1.8 or greater installed, 
as well as Google's "ChromeDriver" aka "Web Driver for Chrome" (available at this link:
https://sites.google.com/a/chromium.org/chromedriver/downloads ).  This is a single
.exe file and does not require an installation process.


Definitions:

Incident parameters are defined in a file that follows JSON notation.  The
JSON file, ChromeDriver, and headless on/off options are defined on the command
line when executing the runnable .jar file.

Below is an example .json file.  Please adjust all key/value pairs
to reflect the values you would like for the generated incident.  Keys
are coded into the java program, and do not need to be changed.  In most cases, 
to verify values are valid, it's easiest to fill out the incident creation form
in Service-Now, and copy/paste the text directly from those fields into the 
values below.  Please ensure to use the \ escape character prefix for any 
double quotes or back slash characters that need to be in the value fields!

{
	"snow_url":"http://ascensionprod.service-now.com",
	"username":"windowsregistry::HKEY_LOCAL_MACHINE\\SOFTWARE\\SNOWcreateincident::username",
	"password":"windowsregistry::HKEY_LOCAL_MACHINE\\SOFTWARE\\SNOWcreateincident::password",
	"caller":"wfsi\\a78808",
	"category":"software",
	"subcategory":"corporate application",
	"configuration_item":"Lab - WIGLE - APP012527",
	"assignment_group":"LAB AND PLUS",
	"application_ci":"Lab - WIGLE - APP012527",
	"location":"Selenium Headless Chrome automaton",
	"impact":"4",
	"urgency":"4",
	"priority":"4",
	"assigned_to":"Brown, Matthew",
	
	"short_description":"Please ignore this! This is a test incident",

	"worknotes":"This is a test work note, automatically generated."
}

The username/password fields are your Service-Now credentials, typically 
domain\userid + password.  Do not prefix '\' or '"' characters  with the \
escape character in the registry definitions!

For the username/password fields, you can either use plain-text credentials, or use 
"windowsregistry::key path::key name" syntax to reference credentials stored in the
windows registry.  Permissions can be set on the windows key/value pairs such that
only the user executing the .jar file can read the username/password (as well as 
any motivated administrators who may decide to take ownership of the key, change 
perms, and view).

The file snow_ci.reg has been included to define the key as used in the example.  Please ensure
to adjust permissions after importing this .reg file (right click on key, permissions, advanced,
uncheck 'include inheritable permissions', edit, remove all entries, add id of the user who
plans to run the snow_ci.jar file, give that user full control.)


Running the jar file:

example command line to create an incident: (change as necessary to reflect the location of your JRE)
Presumes - snow_ci.jar file, create_incident.json file and chromedriver.exe are in c:\scripts

"C:\Program Files\Java\jdk-11.0.1\bin\java.exe" -Djson_input_file=c:\scripts\create_incident.json -Dheadless=on -Dchrome_driver=C:\scripts\chromedriver.exe -jar C:\scripts\snow_ci.jar

If run by hand you can see various console output that verifies the connection, login, and
reports the Service-Now incident number.  Run times vary between 30-50 seconds typically for
one incident.  To watch the process, change the -Dheadless=on to -Dheadless=off






