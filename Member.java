import java.awt.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import org.bson.Document;


public class Member  {


	Document memberDoc;
	
	static boolean gender = true;
	static boolean name = true;
	static boolean LoyaltyNumber = true;
	static boolean email = true;
	static boolean points = true;  
	static boolean isVIP = true;
	static boolean age = true;
	static boolean dateJoined = true;
	
	static Random rand = new Random();
	
	List fnMale = new List();
	List fnFemale = new List();
	List ln = new List();
	
	int LoyaltyNum = 0;
	
	public Member () throws IOException{
		
		
		 // initialize name lists 
		 fnMale = fillList("first_male");
		 fnFemale = fillList("first_female");
		 ln = fillList("last");
		
		 //initialize the loyalty number
		 //TODO get the highest loyalty number
		 
	}
	
	public Document generateMember(){
		
		 memberDoc = new Document();
		
		
		// get gender
				 if(gender){
					
					 if(rand.nextBoolean())
						 memberDoc.append("gender", "Male");
					 else 
						 memberDoc.append("gender", "Female");
					 
				 }
				 
				 //generate the name only iff there is a gender
				 if (name && gender) {
					 
					 if ( memberDoc.getString("gender").equalsIgnoreCase("Male") )
						 memberDoc.append("Name", getName("Male"));
					 else 
						 memberDoc.append("Name", getName("Female"));
					 
					 
				 }
				 
				 if(LoyaltyNumber)
					 memberDoc.append("LoyaltyNumber", LoyaltyNum++);
					 
				 if(points)
					 memberDoc.append("points", 0);
				 
				 if(isVIP)
					 memberDoc.append("isVIP", false);
				 
				 if(age){
					 
					 if(rand.nextBoolean()){
						 
						 int age = (int) random(15.0, 45.0, 0.3);
						 memberDoc.append("age", age);
						 
					 } else {
						 
						 int age = (int) random(45.0, 82.0, 3.0);
						 memberDoc.append("age", age);
					 }
				 }
				 
				 if(dateJoined)
					 memberDoc.append("dateJoined", getDate());
					 
				 
				 System.out.println(memberDoc);
				 
		return memberDoc;
		
	}
	
	private String getName(String string) {
		
		String fname = "";
				
		if (string.equalsIgnoreCase("Male"))
			fname = fnMale.getItem(rand.nextInt(fnMale.getItemCount()));
		else 	
			fname = fnFemale.getItem(rand.nextInt(fnFemale.getItemCount()));
		
		String lname = ln.getItem(rand.nextInt(ln.getItemCount()));
		
		if (email) {
			memberDoc.append("email", (fname + "." + lname + "@gmail.com"));
		}
		
		return fname + " " + lname;
	}
	
	private static List fillList(String file) throws IOException {
		
		List names = new List();
		
		BufferedReader br = new BufferedReader(new FileReader("lib/" + file));
	
		    String line = br.readLine();
		   
		    
		    while (line != null) {
		    	names.add(line); 
			    line = br.readLine();
			    }
		
		    
		  br.close();
		
		return names;
	}
	
	private static double random(double low, double high, double bias)
	{
		double r = rand.nextFloat();    // random between 0 and 1
	    r = Math.pow(r, bias);
	    return low + (high - low) * r;
	}
	
private static Date getDate() {
		
		int year = 2000; 
		int month = 0;
		
		// set year with skew to 2016
		year += (int) random(0.0, 17.0, 0.8);
		// set month with skew to EoY
		month = (int) random(0.0, 12.0, 0.8);
		
		// set day (tough to skew? ) bias at some point. 
		int day = rand.nextInt(30);	
		// skew this better too.
		int hour = rand.nextInt(12) + 9; 			
		int min = rand.nextInt(60);
		int sec = rand.nextInt(60);
		
		Calendar calendar = new GregorianCalendar(year,month,day,hour,min,sec);
		
		Date date = new Date();
		date = calendar.getTime();
		//System.out.println(calendar.getTime());
		
		return date;
		
	}
	
}
