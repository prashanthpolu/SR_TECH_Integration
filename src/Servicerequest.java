import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ISelect;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileDriver;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.touch.LongPressOptions;
import io.appium.java_client.touch.TapOptions;
import io.appium.java_client.touch.offset.PointOption;

public class Servicerequest {
	public PrintWriter reportlogwriter; 
	public RemoteWebDriver autoDriver;
	public DateFormat dateFormat;
	public DateFormat filedateFormat;
	public DateFormat screenshotfilename;
	
	  RemoteWebDriver rdriver = null;
	@Test
  public void ServiceRequestVerify() {
	int tcpassed=0;
	int tcfailed=0;
	int tcblocked=0;
	String startdate="";
	String enddate="";
	filedateFormat = new SimpleDateFormat("MM-dd-YYYY_HH-mm-ss");
	  Date date = new Date();
	  startdate = filedateFormat.format(date);
	  
	  
	  
	  
	for(int servicecount=0;servicecount < 1;servicecount++) {
	  RemoteWebDriver rdriver = null;
	  FileInputStream filein =null;
	  FileOutputStream fileout=null;
	  Properties srproperty = new Properties();
	 
	  String reportstring="";
	  String intworkorderno="";
	  //Service request app launch
	  	String strservicerequestno = servicerequestapplaunch();  
	  	boolean testexecution = false;
       if(strservicerequestno.toLowerCase().contains("new")) {
    	   reportstring="SRRequestApp;FAILED;FAILED to createrquest";
    	   tcfailed++;
       }else {
    	   reportstring="SRRequestApp;PASSED;"+strservicerequestno;
    	   tcpassed++;
     //MAximo browser getting workorder no and change status as work dispatch
    	   intworkorderno =getServicerequest(strservicerequestno);
    	   testexecution = true;
       }
       String techcomp="";
	  	if(testexecution) {
		  if(intworkorderno.trim().length() == 0) {
			  //MAximo browser fails re-getting workorder no and change status as work dispatch
			    
			  intworkorderno =getServicerequest(strservicerequestno);
		  }else {
			  
		  }
		  if(intworkorderno.trim().length() > 0) {
			  reportstring=reportstring+";Maximo;PASSED;"+intworkorderno;
			  tcpassed++;
	///validate tech app and change status as completed for workorder
			  techcomp = validatetechnicanapp(intworkorderno);
			  if(techcomp.equalsIgnoreCase("no")) {
				///re-validate failed tech app and change status as completed for workorder
				  techcomp = validatetechnicanapp(intworkorderno);
			  }
		  }else {
			  reportstring=reportstring+";Maximo;FAILED;Failed to Dispatch workorder";
			  tcfailed++;
		  }
	  	}
		 
		 
		  String strcomp="";
		if(techcomp.equalsIgnoreCase("yes")) {
			 reportstring=reportstring+";TECHAPP;PASSED;WORK ORDER COMPLETED:"+intworkorderno;
			 tcpassed++;
//MAXIMO browser verify status as COMP for service request no			 
			       strcomp = completeservicerequest(strservicerequestno);
			       if(!strcomp.equalsIgnoreCase("comp")) {
//if "COMP" not displayed re-veirfy MAXIMO browser verify status as COMP for service request no			 

			    	   strcomp = completeservicerequest(strservicerequestno);
			       }
		}else {
			 reportstring=reportstring+";TECHAPP;FAILED;WORK ORDER NOT COMPLETED:"+intworkorderno;
			 tcfailed++;
		}
		if(strcomp.equalsIgnoreCase("comp")) {
			 reportstring=reportstring+";MAXIMO;PASSED;DISPLAED COMP STATUS for Work Order:"+intworkorderno;
			 tcpassed++;
//Servicereqeust app and check service request no in "COMPLETED" SR's		
			 String srcompleted = SRverifycompletestatus(strservicerequestno);
			 if(srcompleted.equalsIgnoreCase("yes")) {
				 reportstring=reportstring+";SRRequestApp;PASSED;DISPLAYED COMPLETED for SR#:"+strservicerequestno;
				tcpassed++;	
			 }else {
				 srcompleted = SRverifycompletestatus(strservicerequestno);
				 if(srcompleted.equalsIgnoreCase("yes")) {
					 reportstring=reportstring+";SRRequestApp;PASSED;DISPLAYED COMPLETED for SR#:"+strservicerequestno;
						tcpassed++;
				 }else {
				 reportstring=reportstring+";SRRequestApp;FAILED;NOT DISPLAYED COMPLETED for SR#:"+strservicerequestno;
			tcfailed++;
				 }
				 }
				
		}else {
			 reportstring=reportstring+";MAIXMO;FAILED;DISPLAYED STATUS: "+ strcomp +" for SR#:"+strservicerequestno;
				tcfailed++;
		}
		reportlogwriter.println(reportstring);			
		
		 date = new Date();
		  enddate = filedateFormat.format(date);}	
sendData(tcpassed, tcfailed, tcblocked, startdate, enddate);	
}
				

	public String getServicerequest(String strservicerequestno) {
		 String returnworkorderno="";
		try {
		 DesiredCapabilities desiredCapabilities = new DesiredCapabilities().android();
			
	     
			/// brower script starting
		      
				 System.setProperty("webdriver.chrome.driver", "driver/chromedriver.exe");
					// autoDriver = new ChromeDriver();
					 DesiredCapabilities capbilities = new DesiredCapabilities().chrome();
					 ChromeOptions options = new ChromeOptions();
					// options.addArguments("test-type");
					// options.addArguments("user-data-dir=C:\\Users\\US_YSawhney\\AppData\\Local\\Google\\Chrome\\User Data\\Profile 1");
					 DesiredCapabilities capabilities = DesiredCapabilities.chrome();
					 capabilities.setCapability(ChromeOptions.CAPABILITY, options);
					try {
						rdriver=new ChromeDriver(capabilities);
					//	autoDriver = new ChromeDriver(capabilities);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					rdriver.manage().timeouts().implicitlyWait(180, TimeUnit.SECONDS);
					
					rdriver.manage().window().maximize();
					rdriver.navigate().to("http://usadcmxm170.tnd.us.cbre.net:9083/mea/maximo/webclient/login/login.jsp");
					//rdriver.navigate().to("http://gwsmaximo-train.cbre.com/maximo/webclient/login/login.jsp?welcome=true");
					
				//	String strservicerequestno="SR2528486";
					
					
					String strusername="US\\FESTLICK";
					String strpasswrod="Maximo123";
					String strsrrequesttext= strservicerequestno;
					
					
				//	
					
					//servicerequestsend(rdriver,strusername,strpasswrod);
					rdriver.findElement(By.xpath("//input[@id='username']")).sendKeys(strusername);
					rdriver.findElement(By.xpath("//input[@id='password']")).sendKeys(strpasswrod);
					rdriver.findElement(By.xpath("//button[@id='loginbutton']")).click();
					
					rdriver.findElement(By.xpath("//a[@title='Service Requests (SP)']")).click();
					
					//rdriver.findElement(By.xpath("//*[@id='m6a7dfd2f_tfrow_[C:1]_txt-tb']")).sendKeys(strsrrequesttext);
					 try {
							Thread.sleep(10000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					rdriver.findElement(By.xpath("//input[@id='quicksearch']")).sendKeys(strsrrequesttext+Keys.ENTER);
					 try {
							Thread.sleep(20000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					// rdriver.findElement(By.xpath("//input[@id='quicksearchQSImage']")).click();
					
					//enter or click//rdriver.findElement(By.xpath("//input[@id='quicksearchQSImage']")).click();
					
					
					 
					//rdriver.findElement(By.xpath("//*[@id='m6a7dfd2f_tfrow_[C:1]_txt-tb']")).sendKeys(Keys.ENTER);
					//scroll scroll scroll scroll

				
					
					WebElement Status = rdriver.findElement(By.xpath("//input[@id='m281bd70d-tb']"));
					String StatusValue = Status.getAttribute("value");
					//input[@id='m281bd70d-tb']
					int countsearch=0;
					Search:
					while (!StatusValue.equals("QUEUED"))
					{
						//rdriver.findElement(By.xpath("//input[@id='quicksearch']")).click();
						rdriver.findElement(By.xpath("//input[@id='quicksearch']")).clear();
						rdriver.findElement(By.xpath("//input[@id='quicksearch']")).sendKeys(strsrrequesttext+Keys.ENTER);
						countsearch++;
						try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						Thread.sleep(5000);
						rdriver.findElement(By.xpath("//input[@id='quicksearch']")).clear();
						rdriver.findElement(By.xpath("//input[@id='quicksearch']")).sendKeys(strsrrequesttext+Keys.ENTER);
					// TODO Auto-generated catch block
						e.printStackTrace();
					}//rdriver.findElement(By.xpath("//input[@id='quicksearch']")).sendKeys(Keys.ENTER);
						 //rdriver.findElement(By.xpath("//input[@id='quicksearchQSImage']")).click();
						Status = rdriver.findElement(By.xpath("//input[@id='m281bd70d-tb']"));
						 StatusValue = Status.getAttribute("value");
						 if(countsearch > 10) {
							 break;
						 }
					continue Search ;	
					}
					//Scroll to workorder
					WebElement scrollToElement = rdriver.findElement(By.xpath("//label[@id='m6f25024c-lb']"));
					((JavascriptExecutor)rdriver).executeScript("arguments[0].scrollIntoView(false);",scrollToElement);
					
					
					
					try {
						WebElement ElementValue = rdriver.findElement(By.xpath("//input[@id='m6f25024c_tdrow_[C:1]_txt-tb[R:0]']"));
					String WorkOrder = ElementValue.getAttribute("value");
					if (WorkOrder!=null)
					{System.out.println(WorkOrder);
					}
					} catch(Exception e){
						System.out.println("Workorder Not Generated");
				
					}
					
					
					try{
						String WAPPR=rdriver.findElement(By.xpath("//td[@id='m6f25024c_tdrow_[C:3]-c[R:0]']")).getText(); 
						System.out.println(WAPPR);
						
						if(WAPPR=="WAPPR")
							System.out.println("WAPPR Validated");
					} catch (Exception e) {System.out.println("Status is not WAPPR");}

					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					rdriver.findElement(By.xpath("//img[@id='m6f25024c_tdrow_[C:1]_txt-img[R:0]']")).click();
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					rdriver.findElement(By.xpath("//span[@id='WORKORDER_applink_undefined_a_tnode']")).click();

					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					WebElement scrollToWODates = rdriver.findElement(By.xpath("//label[@id='m51497993-lb']"));
					((JavascriptExecutor)rdriver).executeScript("arguments[0].scrollIntoView(false);",scrollToWODates);
					
					DateFormat  dateFormat = new SimpleDateFormat("MM/dd/yy");
					Date date1 = new Date();
					String date2 = dateFormat.format(date1);
					System.out.println(date2);
					rdriver.findElement(By.xpath("//input[@id='m8b35a058-tb']")).sendKeys(date2);
					
					Calendar c = Calendar.getInstance();
					c.add(Calendar.DATE, 7);
					String date3 = dateFormat.format(c.getTime());
					System.out.println(date3);
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					rdriver.findElement(By.xpath("//input[@id='ma018f39b-tb']"));
					//rdriver.findElement(By.xpath("//INPUT[@id='m8b35a058-tb']/../../../../../../..//INPUT[@id='ma018f39b-tb']")).click();
					//input[@id='ma018f39b-tb']
					//rdriver.findElement(By.xpath("//input[@id='ma018f39b-tb']")).clear();
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//rdriver.findElement(By.xpath("//INPUT[@id='m8b35a058-tb']/../../../../../../..//INPUT[@id='ma018f39b-tb']")).sendKeys(date2);
					//rdriver.findElement(By.xpath("//input[@id='m8b35a058-tb']/../../../../../../..//input[@id='ma018f39b-tb']")).getAttribute("Value");
					rdriver.findElement(By.xpath("//input[@id='m8b35a058-tb']/../../../../../../..//input[@id='ma018f39b-tb']")).click();
					rdriver.findElement(By.xpath("//input[@id='m8b35a058-tb']/../../../../../../..//input[@id='ma018f39b-tb']")).clear();
					rdriver.findElement(By.xpath("//input[@id='m8b35a058-tb']/../../../../../../..//input[@id='ma018f39b-tb']")).sendKeys(date2);
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					//rdriver.findElement(By.xpath("//input[@id='m8b35a058-tb']/../../../../../../..//input[@id='ma018f39b-tb']")).getAttribute("Value");
					rdriver.findElement(By.xpath("//input[@id='m8b35a058-tb']/../../../../../../..//input[@id='ma018f39b-tb']")).click();
					rdriver.findElement(By.xpath("//input[@id='m8b35a058-tb']/../../../../../../..//input[@id='ma018f39b-tb']")).clear();
					rdriver.findElement(By.xpath("//input[@id='m8b35a058-tb']/../../../../../../..//input[@id='ma018f39b-tb']")).sendKeys(date3);
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					//Routeworkflow
					WebElement scrollToRoute = rdriver.findElement(By.xpath("//img[@id='ROUTEWF_JCIWOMAN_-tbb_image']"));
					((JavascriptExecutor)rdriver).executeScript("arguments[0].scrollIntoView(false);",scrollToRoute);
					rdriver.findElement(By.xpath("//img[@id='ROUTEWF_JCIWOMAN_-tbb_image']")).click();
					try{Thread.sleep(15000);}catch (Exception ie) {};
			
					//Job Plan
					WebElement scrollToJobPlan= rdriver.findElement(By.xpath("//label[@id='m9f7e1d3e-lb']"));
					((JavascriptExecutor)rdriver).executeScript("arguments[0].scrollIntoView(false);",scrollToJobPlan);
					String Jobplan = rdriver.findElement(By.xpath("//label[@id='mfe7bb84-lb']")).getText();
					System.out.println(Jobplan);
					if( Jobplan.contains("Job Plan"))
					{
						WebElement Jptab = rdriver.findElement(By.xpath("//input[@id='mfe7bb84-tb']"));
						Jptab.clear();
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
					////img[@id='ROUTEWF_JCIWOMAN_-tbb_image']
					
					 Jptab.sendKeys("51823");
					 Jptab.clear();
					 
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Jptab.sendKeys("51823");
			try{Thread.sleep(5000);}catch (Exception ie) {};
			
			Jptab.sendKeys(Keys.TAB);
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
					}
					else 
						{System.out.println("JobPlan not found");
						
						}
					
					
					
					
					
			
		//Resposibility
					
					WebElement ScrolltoResponsibility= rdriver.findElement(By.xpath("//label[@id='mc8402829-lb']"));
					((JavascriptExecutor)rdriver).executeScript("arguments[0].scrollIntoView(false);",ScrolltoResponsibility);
					//String Realm = rdriver.findElement(By.xpath("//input[@id='mf63324a0-tb']")).getText();
					WebElement Realm = rdriver.findElement(By.xpath("//*[@id='mf63324a0-tb']"));
					String RealmValue = Realm.getAttribute("value");
					WebElement Realmold= rdriver.findElement(By.xpath("//input[@id='mf63324a0-tb']"));
					String Realmoldvalue= Realmold.getAttribute("Value");
					
					System.out.println(RealmValue);
					System.out.println(Realmoldvalue);
					rdriver.findElement(By.xpath("//input[@id='mf63324a0-tb']")).clear();
					rdriver.findElement(By.xpath("//input[@id='mf63324a0-tb']")).clear();
							if (RealmValue.equals( "  " ))
							{//rdriver.findElement(By.xpath("//input[@id='mec969533-tb']")).click();
								rdriver.findElement(By.xpath("//input[@id='mf63324a0-tb']")).clear();
								rdriver.findElement(By.xpath("//input[@id='mec969533-tb']")).click();
							rdriver.findElement(By.xpath("//input[@id='mec969533-tb']")).sendKeys("US\\PPOLU");
							try{Thread.sleep(1000);}catch (Exception ie) {};
							rdriver.findElement(By.xpath("//input[@id='mec969533-tb']")).clear();
							try{Thread.sleep(1000);}catch (Exception ie) {};
							rdriver.findElement(By.xpath("//input[@id='mec969533-tb']")).sendKeys("US\\PPOLU");
							try{Thread.sleep(1000);}catch (Exception ie) {};}
							
							else {rdriver.findElement(By.xpath("//input[@id='mf63324a0-tb']")).clear();
							rdriver.findElement(By.xpath("//*[@id='mf63324a0-tb']")).getAttribute("Value");
							System.out.println();
							rdriver.findElement(By.xpath("//*[@id='mf63324a0-tb']")).clear();
							try{Thread.sleep(1000);}catch (Exception ie) {};
							
							rdriver.findElement(By.xpath("//input[@id='mf63324a0-tb']")).sendKeys(Keys.TAB);
							try{Thread.sleep(1000);}catch (Exception ie) {};
							rdriver.findElement(By.xpath("//input[@id='mec969533-tb']")).sendKeys("US\\PPOLU");
							try{Thread.sleep(1000);}catch (Exception ie) {};
							rdriver.findElement(By.xpath("//input[@id='mec969533-tb']")).clear();
							try{Thread.sleep(1000);}catch (Exception ie) {};
							rdriver.findElement(By.xpath("//input[@id='mec969533-tb']")).sendKeys("US\\PPOLU");
							
							rdriver.findElement(By.xpath("//input[@id='mec969533-tb']")).sendKeys(Keys.TAB);
							
							try{Thread.sleep(10000);}catch (Exception ie) {};
							
							}
							//Approve WorkOrder
							
							rdriver.findElement(By.xpath("//img[@id='toolactions_APPR-tbb_image']")).click();
							try{Thread.sleep(10000);}catch (Exception ie) {};
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					//Popup approval
					rdriver.switchTo().activeElement().sendKeys(Keys.ENTER);
					
					try{Thread.sleep(10000);}catch (Exception ie) {};
					//Validate status after approving
					String statustext = rdriver.findElement(By.xpath("//*[@id='md3801d08-tb2']")).getText();
					try{Thread.sleep(20000);}catch (Exception ie) {};
					
					//Perform Some action-Refresh

					
					
					
					//Change Status
					
					rdriver.findElement(By.xpath("//*[@id='toolactions_STATUS-tbb_image']")).click();
					try{Thread.sleep(15000);}catch (Exception ie) {};
					
					
					//dispatch in Popup
				//	rdriver.switchTo().activeElement().findElement(By.xpath("//*[@id='mc927149a-tb']")).click();
					try{Thread.sleep(10000);}catch (Exception ie) {}
					
						/*	WebElement Dd=rdriver.findElement(By.xpath("//*[@id='mc927149a-tb']"));
							try{Thread.sleep(10000);}catch (Exception ie) {}
							Dd.sendKeys(Keys.DOWN);*/
						
							try{Thread.sleep(10000);}catch (Exception ie) {}
							String WorkDispatched=rdriver.findElement(By.xpath("//*[@id='mc927149a-tb']")).getAttribute("value");
							Search:
								try{Thread.sleep(10000);}catch (Exception ie) {}
							while (!WorkDispatched.equals("Work Dispatched"))
							{
								rdriver.findElement(By.xpath("//*[@id='mc927149a-tb']")).click();
								rdriver.findElement(By.id("menu0_DISPATCH_OPTION_a_tnode")).click();
								try{Thread.sleep(5000);}catch (Exception ie) {}
								rdriver.findElement(By.xpath("//button[text()='OK']")).click();
								
								try {
									Thread.sleep(10000);
									rdriver.findElement(By.xpath("//table[@id='msgbox-bg_table']/tbody/tr/td/button[text()='OK']")).click();
									Thread.sleep(5000);
									rdriver.findElement(By.xpath("//button[text()='Cancel']")).click();
									try{Thread.sleep(15000);}catch (Exception ie) {};
									
									rdriver.findElement(By.xpath("//*[@id='toolactions_STATUS-tbb_image']")).click();
									try{Thread.sleep(15000);}catch (Exception ie) {};
									rdriver.findElement(By.xpath("//*[@id='mc927149a-tb']")).click();
									rdriver.findElement(By.id("menu0_DISPATCH_OPTION_a_tnode")).click();
									Thread.sleep(1000);
									rdriver.findElement(By.xpath("//button[text()='OK']")).click();
									 WorkDispatched=rdriver.findElement(By.xpath("//*[@id='mc927149a-tb']")).getAttribute("value");
								}catch(Exception e) {
									System.out.println("no error");
								}
							}
								WebElement workorderelement = rdriver.findElement(By.xpath("//*[@id='m3946d7c2-tb']"));
								String intworkorderno = workorderelement.getAttribute("value");
								
								
									System.out.println(intworkorderno);
									returnworkorderno=intworkorderno;
									rdriver.close();
									rdriver.quit();
									
		}catch(Exception e) {
			
		}
		return returnworkorderno;
		
	}
  
  public String validatetechnicanapp(String intworkorderno) {
	  String techcompleted = "no";
	  DesiredCapabilities desiredCapabilities = new DesiredCapabilities().android();
	  desiredCapabilities.setCapability("platformName", "Android");
		desiredCapabilities.setCapability("deviceName", "android");
		desiredCapabilities.setCapability("appPackage","com.cbre.technician.uat");
			
		desiredCapabilities.setCapability("appActivity","com.rhomobile.rhodes.RhodesActivity");
		desiredCapabilities.setCapability("noReset", false);
		try {
			rdriver = new AndroidDriver(new URL("http://127.0.0.1:4723/wd/hub"), desiredCapabilities);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		String techusername="US\\PPOLU";
		String techpassword="Simple15!@#";
				rdriver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
				try {
					rdriver.findElement(By.xpath("//android.view.View[@text='English']")).click();
					Thread.sleep(2000);
					AndroidDriver adriver = (AndroidDriver) rdriver;
					scrollup(adriver);
					Thread.sleep(2000);
					scrollup(adriver);
					Thread.sleep(2000);
					scrollup(adriver);
					Thread.sleep(3000);
					try {
						rdriver.findElement(By.xpath("//android.view.View[@text='USD ($)']")).click();
					}catch(Exception e) {
						rdriver.findElement(By.xpath("//android.view.View[@text='USD ($)']")).click();
					}
				}catch(Exception e) {
					System.out.println("element not found");
				}
		try {
			rdriver.findElement(By.xpath("//android.widget.EditText[@resource-id='i1']")).sendKeys(techusername);
			rdriver.findElement(By.xpath("//android.widget.EditText[@resource-id='i2']")).sendKeys(techpassword);
			rdriver.findElement(By.xpath("//android.widget.Button[@text='Login']")).click();
		
		}catch(Exception e) {
			
		}
		
		try {
			//String strwrkorder="4836";
			//rdriver.findElement(By.xpath("//android.view.View[@text='End My Day']")).click();
			//rdriver.findElement(By.xpath("//android.widget.Button[@text='End Day']")).click();
			rdriver.findElement(By.xpath("//android.view.View[@text='Start My Day']")).click();
			rdriver.findElement(By.xpath("//android.widget.Button[@text='Start']")).click();
			Thread.sleep(20000);
			 AndroidDriver adriver = (AndroidDriver) rdriver;
			 for(int i = 0 ;i < 10;i++ ) {
				 try {
					 rdriver.findElement(By.xpath("//android.view.View[@text='Syncing']"));
					 Thread.sleep(5000);
				 }catch(Exception e) {
					 break;
				 }
			 }
			
			try {
				rdriver.findElement(By.xpath("//android.view.View[@text='Return To Open Workorder']")).click();
				  rdriver.findElement(By.xpath("//android.widget.ListView/android.view.View[3]/android.view.View")).click();
				 rdriver.findElement(By.xpath("//android.view.View[@text='Complete']")).click();
			       scrollup(adriver);
			       Thread.sleep(2000);
			       scrollup(adriver);
			       Thread.sleep(2000);
			       rdriver.findElement(By.xpath("//android.widget.Button[@text='Save']")).click();
			       Thread.sleep(2000);
			       rdriver.findElement(By.xpath("//android.view.View[@text='Back']")).click();
		    	   rdriver.findElement(By.xpath("//android.view.View[@text='Back']")).click();
		    	   Thread.sleep(3000);
			}catch(Exception e) {
				
			}
			if(intworkorderno.trim().length() > 0) {
		   rdriver.findElement(By.xpath("//android.widget.EditText[@resource-id='i4']")).sendKeys(intworkorderno);
		   
		  
		   adriver.hideKeyboard();
		   rdriver.findElement(By.xpath("//android.view.View[@text='Search']")).click();
		   Thread.sleep(5000);
	       rdriver.findElement(By.xpath("//android.view.View[@text='#"+intworkorderno+"']")).click();
	   
	       Thread.sleep(5000);
	       rdriver.findElement(By.xpath("//android.widget.ListView/android.view.View[1]/android.view.View")).click();
	       adriver = (AndroidDriver) rdriver;
	       scrollup(adriver);
	       Thread.sleep(2000);
	       scrollup(adriver);
	       Thread.sleep(2000);
	       scrollup(adriver);
	       Thread.sleep(2000);
	       scrollup(adriver);
	       Thread.sleep(2000);
	       rdriver.findElement(By.xpath("//android.widget.Button[@text='Start']")).click();
	       rdriver.findElement(By.xpath("//android.widget.ListView/android.view.View[3]/android.view.View")).click();
	       rdriver.findElement(By.xpath("//android.view.View[@text='Complete']")).click();
	       scrollup(adriver);
	       Thread.sleep(2000);
	       scrollup(adriver);
	       Thread.sleep(2000);
	       rdriver.findElement(By.xpath("//android.widget.Button[@text='Save']")).click();
	       Thread.sleep(2000);
	       System.out.println("test completed save");
	       techcompleted = "Yes";
			}
		}catch(Exception e) {
			
		}
		System.out.println("test");
		rdriver.quit();
		return techcompleted;
  }
 
  public String completeservicerequest(String strservicerequestno) {
	  System.setProperty("webdriver.chrome.driver", "driver/chromedriver.exe");
		// autoDriver = new ChromeDriver();
		 DesiredCapabilities capbilities = new DesiredCapabilities().chrome();
		 ChromeOptions options = new ChromeOptions();
		// options.addArguments("test-type");
		// options.addArguments("user-data-dir=C:\\Users\\US_YSawhney\\AppData\\Local\\Google\\Chrome\\User Data\\Profile 1");
		 DesiredCapabilities capabilities = DesiredCapabilities.chrome();
		 capabilities.setCapability(ChromeOptions.CAPABILITY, options);
		try {
			rdriver=new ChromeDriver(capabilities);
		//	autoDriver = new ChromeDriver(capabilities);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rdriver.manage().timeouts().implicitlyWait(120, TimeUnit.SECONDS);
		
		rdriver.manage().window().maximize();
		rdriver.navigate().to("http://usadcmxm170.tnd.us.cbre.net:9083/mea/maximo/webclient/login/login.jsp");
		//rdriver.navigate().to("http://gwsmaximo-train.cbre.com/maximo/webclient/login/login.jsp?welcome=true");
		
	//	String strservicerequestno="SR2528486";
		
		
		String strusername="US\\FESTLICK";
		String strpasswrod="Maximo123";
		String strsrrequesttext= strservicerequestno;
		
		
	//	
		
		//servicerequestsend(rdriver,strusername,strpasswrod);
		rdriver.findElement(By.xpath("//input[@id='username']")).sendKeys(strusername);
		rdriver.findElement(By.xpath("//input[@id='password']")).sendKeys(strpasswrod);
		rdriver.findElement(By.xpath("//button[@id='loginbutton']")).click();
		
		rdriver.findElement(By.xpath("//a[@title='Service Requests (SP)']")).click();
		
		//rdriver.findElement(By.xpath("//*[@id='m6a7dfd2f_tfrow_[C:1]_txt-tb']")).sendKeys(strsrrequesttext);
		 try {
				Thread.sleep(10000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		rdriver.findElement(By.xpath("//input[@id='quicksearch']")).sendKeys(strsrrequesttext+Keys.ENTER);
		 try {
				Thread.sleep(20000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		// rdriver.findElement(By.xpath("//input[@id='quicksearchQSImage']")).click();
		 String COMP="";

			try{
				COMP=rdriver.findElement(By.xpath("//td[@id='m6f25024c_tdrow_[C:3]-c[R:0]']")).getText(); 
				System.out.println(COMP);
				
				
			} catch (Exception e) {System.out.println("Status is not COMP");}
			rdriver.close();
			rdriver.quit();
return COMP;
  }
  
  public String servicerequestapplaunch() {
	  
	  FileInputStream filein =null;
	  FileOutputStream fileout=null;
	  Properties srproperty = new Properties();
	  try {
		fileout = new FileOutputStream("Servicerequest.properties");
		filein = new FileInputStream("Servicerequest.properties");
		srproperty.load(filein);
		filein.close();
	} catch (Exception e2) {
		// TODO Auto-generated catch block
		e2.printStackTrace();
	}
	  DesiredCapabilities desiredCapabilities = new DesiredCapabilities().android();
		desiredCapabilities.setCapability("platformName", "Android");
		desiredCapabilities.setCapability("deviceName", "android");
		
		desiredCapabilities.setCapability("appPackage","com.cbre.request.uat");
			
		desiredCapabilities.setCapability("appActivity","com.jci.request.activity.HomeActivity");
		desiredCapabilities.setCapability("noReset", true);
		try {
			rdriver = new AndroidDriver(new URL("http://127.0.0.1:4723/wd/hub"), desiredCapabilities);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	rdriver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
		DateFormat srvdateformat = new SimpleDateFormat("MM-dd-YYYY-HHmmss");
		Date date = new Date();
		String strdescription = "ServiceRequest-AutomationTesting"+srvdateformat.format(date);
		
		rdriver.findElement(By.xpath("//android.widget.TextView[contains(@resource-id,'newRequest')]")).click();
		rdriver.findElement(By.xpath("//android.widget.ListView[@resource-id='android:id/list']/android.widget.TextView[1]")).click();
	    rdriver.findElement(By.xpath("//android.widget.EditText[contains(@resource-id,'description')]")).click();
	    rdriver.findElement(By.xpath("//android.widget.EditText[contains(@resource-id,'description')]")).sendKeys(strdescription);;
	   
	   AppiumDriver adriver = (AppiumDriver) rdriver;
	   adriver.hideKeyboard();
	   
	  
	    
	    rdriver.findElement(By.xpath("//android.widget.Button[contains(@resource-id,'btnLocation')]")).click();
		rdriver.findElement(By.xpath("//android.widget.ListView[@resource-id='android:id/list']/android.widget.TextView[1]")).click();
		rdriver.findElement(By.xpath("//android.widget.ListView[@resource-id='android:id/list']/android.widget.TextView[1]")).click();
		
		
	
		WebElement seek_bar=rdriver.findElement(By.xpath("//android.widget.SeekBar[contains(@resource-id,'slider')]"));
		  int start=seek_bar.getLocation().getX();
       
        int end=seek_bar.getSize().getWidth();
       
        int y=seek_bar.getLocation().getY();
        end = (int) (end * 0.4);
    
    TouchAction action=new TouchAction((AndroidDriver)rdriver);

    //Move it will the end
    PointOption pointoption = new PointOption().withCoordinates(start, y);
    PointOption pointoptionend = new PointOption().withCoordinates(end, y);
    action.press(pointoption).moveTo(pointoptionend).release().perform();

   rdriver.findElement(By.xpath("//android.widget.Button[contains(@resource-id,'btnSubmit')]")).click();
	     try {
			Thread.sleep(10000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	     
	     AndroidDriver adriver1 = (AndroidDriver) rdriver;
	     adriver1.closeApp();
	     adriver1.launchApp();
	     
	     String strservicerequestno ="";
	     try {
	    	 strservicerequestno= rdriver.findElement(By.xpath("//android.widget.TextView[@resource-id='com.cbre.request.uat:id/request_id'][1]")).getText();
	         if(strservicerequestno.toLowerCase().contains("sr")) {
	        	// rdriver.findElement(By.xpath("//android.widget.TextView[@resource-id='com.cbre.request.uat:id/request_id'][1]")).click();
	         }else {
	        	 Thread.sleep(5000);
	        	  adriver1.closeApp();
	     	     adriver1.launchApp();
	     	  	 strservicerequestno= rdriver.findElement(By.xpath("//android.widget.TextView[@resource-id='com.cbre.request.uat:id/request_id'][1]")).getText();
	     	  	//rdriver.findElement(By.xpath("//android.widget.TextView[@resource-id='com.cbre.request.uat:id/request_id'][1]")).click();
	         }
	     }catch(Exception e) {
	    	 System.out.println("no sr # generated by Device");
	     }
	     
	    System.out.println(strservicerequestno);
	     srproperty.setProperty(strservicerequestno, strdescription);
	     try {
			srproperty.store(fileout, "srrequest");
			fileout.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	     
    rdriver.quit();
	return strservicerequestno;
  }
  
public String SRverifycompletestatus(String servicerequestno) { 
	  FileInputStream filein =null;
	  FileOutputStream fileout=null;
	  Properties srproperty = new Properties();
	  try {
		fileout = new FileOutputStream("Servicerequest.properties");
		filein = new FileInputStream("Servicerequest.properties");
		srproperty.load(filein);
		filein.close();
	} catch (Exception e2) {
		// TODO Auto-generated catch block
		e2.printStackTrace();
	}
	  DesiredCapabilities desiredCapabilities = new DesiredCapabilities().android();
		desiredCapabilities.setCapability("platformName", "Android");
		desiredCapabilities.setCapability("deviceName", "android");
		
		desiredCapabilities.setCapability("appPackage","com.cbre.request.uat");
			
		desiredCapabilities.setCapability("appActivity","com.jci.request.activity.HomeActivity");
		desiredCapabilities.setCapability("noReset", true);
		try {
			rdriver = new AndroidDriver(new URL("http://127.0.0.1:4723/wd/hub"), desiredCapabilities);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	rdriver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
		DateFormat srvdateformat = new SimpleDateFormat("MM-dd-YYYY-HHmmss");
		Date date = new Date();
		String strservicerequestno="";
		String servicecompleted="no";
		 try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	     rdriver.findElement(By.xpath("//android.widget.TextView[@text='COMPLETED']")).click();
	    
	     try {
	    	 Thread.sleep(5000);
	    	 rdriver.findElement(By.xpath("//android.widget.TextView[@text='" + strservicerequestno +"]"));
	    	 servicecompleted="yes";
	     }catch(Exception e) {
	    	 servicecompleted="no";
	    	 System.out.println("not completed");
	     }
	     rdriver.quit();
	     
	     return servicecompleted;
  }

  public void scrollup(AndroidDriver driver) {
	  int pressX = rdriver.manage().window().getSize().width/2;
	  int bottomY = rdriver.manage().window().getSize().height * 4/5;
	  int topY = rdriver.manage().window().getSize().height /8;
	  TouchAction touchaction = new TouchAction(driver);
	  touchaction.longPress(PointOption.point(pressX, bottomY)).moveTo(PointOption.point(pressX, topY)).release().perform();
  }
 
  @BeforeSuite
  public void beforeSuite() {
	  filedateFormat = new SimpleDateFormat("MM-dd-YYYY_HH-mm-ss");
	  
	  Date date = new Date();
	  try {
		reportlogwriter = new PrintWriter("Run-log"+filedateFormat.format(date)+".log","UTF-8");
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
  }
  
  @AfterSuite
  public void afterSuite() {
	  // calling method to send test execution metrics for execution cycle
	  filedateFormat = new SimpleDateFormat("MM-dd-YYYY_HH-mm-ss");
	  Date date = new Date();
	  reportlogwriter.close();
	 	
  }
  
  @DataProvider
  public Object[][] getTestData() {
	  File envfile = new File("globalvalues.properties");
	  InputStream envpropinput=null;
	  Properties envproperties = new Properties();
	  try {
		  envpropinput = new FileInputStream(envfile);
		  envproperties.load(envpropinput);
	  }catch(Exception e) {
		  System.out.println("no file ");
	  }
	  int dataendno =Integer.parseInt(envproperties.getProperty("endno"));
	  int datastartno =Integer.parseInt(envproperties.getProperty("startno"));
	  Object[][] temptestdata=null;
	  // intializing end number to execution
	 
	  int j=0;
	  for(int i=datastartno;i<dataendno;i++) {
	 	   temptestdata[j]=new Object[] {j,"test"};
	 	   System.out.println(temptestdata[j].toString());
	 	   j++;
	    }
   return temptestdata;
  }
  
  public static void sendData(int passed,int failed,int blocked,String startdate,String enddate){
		try {
			JSONObject json = new JSONObject();
			json.put("ApplicationId", 86);// 132
			json.put("AutomationProcessId", 16); // 14
			json.put("AutomationToolId", 19); // 19
			json.put("Description", "Maximo");
			json.put("Version", "4.0"); // 4.0
			json.put("Cycle", "Cycle-2");// First Build
			json.put("Passed", "1398");
			json.put("Failed", "0");
			json.put("Blocked", "2");
			json.put("StartDateTime", "5/27/2018 11:45");
			json.put("EndDateTime", "5/27/2018 13:25");
			json.put("Metadata", "metadata test");
			URL url = new URL("https://devopsmetricsservice.azurewebsites.net/api/metrics/submit");
			HttpURLConnection httpcon = (HttpURLConnection) url.openConnection();
			httpcon.setDoOutput(true);
			httpcon.setDoInput(true);
			httpcon.setRequestMethod("POST");
			httpcon.setRequestProperty("Accept", "application/json");
			httpcon.setRequestProperty("Content-type", "application/json");
			httpcon.connect();

			// OutputStream os = httpcon.getOutputStream();
			OutputStreamWriter output = new OutputStreamWriter(httpcon.getOutputStream());
			output.write(json.toString());
			output.flush();
			System.out.println(httpcon.getRequestMethod());
			System.out.println(httpcon.getResponseMessage());

		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
}
  
}
