import java.awt.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;


public class Generator {

	// fill out these
	static String Database = "hackathon";
	static String Collection = "products";
	static int numDocs = 50000000;
	
	static Random rand = new Random();
	
	// if you can hold the entire dataset in memory, this is a good way to go. 
	static boolean inMemory = true;
	
	// ***** parts of the doc *****
	
	// typical order form stuff
	
	static boolean order = true;
	static boolean paymentType = true;
	static boolean isMember = true;  
	static boolean addDate = true;
	static boolean addOrders = true;
	static boolean total = true;
	static boolean points = true;
	
	static double totalOrderCost = 0.0;
	
	private static MongoClient client;
	static Member memby; 
	static ArrayList<ObjectId> regPriceProducts;
	static ArrayList<ObjectId> saleProducts;
	static ArrayList<ObjectId> clearanceProducts;

	public static void main(String[] args) throws IOException {
	
		System.out.println("Welcome to the JSON Data Generator");
		
		client = new MongoClient();
		MongoDatabase database = client.getDatabase(Database);
		MongoCollection<Document> collection = database.getCollection(Collection);
		MongoCollection<Document> ordersCol = database.getCollection("orders");
		MongoCollection<Document> membersCol = database.getCollection("members");	
		
		
		//initialize member.
		memby = new Member();
		
		//if in Memory option selected, initialize the products array. 
		//change to reflect what you want.
		
		if(inMemory) {
			
			regPriceProducts = new ArrayList<ObjectId>();
			saleProducts = new ArrayList<ObjectId>();
			clearanceProducts = new ArrayList<ObjectId>();
			
			Document docc = new Document();
			
			int count = 1;
			//load regular price docs
			docc.append("clearance", false);
			docc.append("onSale", false);
			System.out.println("Getting Regular priced Docs");
			for (Document docum : collection.find(docc)) {
			    regPriceProducts.add(docum.getObjectId("_id"));
			    System.out.println(count++);
			   
			}
			
			count = 1;
			// load clearance docs
			docc.clear();
			docc.append("clearance", true);
			System.out.println("Getting clearance Docs");
			for (Document docum : collection.find(docc)) {
			    clearanceProducts.add(docum.getObjectId("_id"));
			    System.out.println(count++);
			   
			}
			
			count = 1;
			// load sale docs
			docc.clear();
			docc.append("onSale", true);
			docc.append("clearance", false);
			for (Document docum : collection.find(docc)) {
			    saleProducts.add(docum.getObjectId("_id"));
			    System.out.println(count++);
			    ;
			}
			
		}
		
		Document doc = new Document();
		Document member;
		// document to create
		
		System.out.println("Creating Documents");
		for(int i = 0; i < numDocs; i++){
		
			 doc = CreateDoc(i, collection) ; 
			
			 // if the document has a member, calculate the points and add the member.
			 if(doc.containsKey("member")){
				 member = calcPoints(doc);
				 System.out.println(member);
				 membersCol.insertOne(member);
			 }
			//insert it.
			System.out.println(doc);
			ordersCol.insertOne(doc);
		}
		
		
		
		System.out.println("Thanks for using the JSON Data Generator");
		
	}

	
	// Method to create a document based on the 'true' attributes
	private static Document CreateDoc(int i, MongoCollection<Document> coll) throws IOException {
		
		Document doc = new Document(); 
		
		// add order_id's in ascending order. (for now)
		// Eventually check last order_id number and start from there.
		if (order)
			doc.append("order_id", i );
			
	
		//add a paymentType
		if (paymentType) 
			doc.append("PayType", getPayType());
			
		if (isMember) {
			
			// check based on a percentage chance they are a member
			
			boolean isMem = getIsMember();
			
			//boolean isMem = true;
			
			doc.append("isLoyaltyMember",isMem);
			
			if (isMem){ 
				// create loyalty document/information
				doc.append("member", memby.generateMember());
			}
		
		}
		
		if (addDate) {
			
			//TODO Change the date to after the date of loyalty 
			doc.append("PurchaseDate", getDate());
			
			}
		
		
		// make anywhere from 1-5 array of documents 
		if (addOrders) { 
			
			// how many
			int numProds = (int) random(1.0, 5.0, 2.0);
			
			System.out.println("NumProds: " + numProds);
			
			//Document[] prods = new Document[numProds];
			ArrayList<Document> prods = new ArrayList<Document>();
			
			for(int x = 0; x < numProds; x++)
					prods.add( addOrder(coll));
			
			doc.append("products", prods);
			
		}
		
		if (total){
			doc.append("total", totalOrderCost);
			totalOrderCost = 0.0;
			
		}
		
		if (points){
			// TODO add points to the loyalty user
			
			
			
		}
			
		return doc;
	}

	

	private static Document addOrder(MongoCollection<Document> col) {
		
		Document orderDoc = new Document(); 
		Document retrievedDoc = new Document();
		
		// find if its a sale/clearance/full price
		
		System.out.println("Getting Order");
		
		int rando = rand.nextInt(10);
				
		FindIterable<Document> fi;
		Document docc = new Document();
		
		
		if (!inMemory) {
			if (rando >= 6) {
				docc.append("onSale", true);
				fi = col.find(docc).skip(rand.nextInt(40911)).limit(1);
				
				
			} else if (rando == 4){
				docc.append("clearance", true);
				fi = col.find(docc).skip(rand.nextInt(29948)).limit(1);
				
			} else{
				docc.append("clearance", false);
				docc.append("onSale", false);
				fi = col.find(docc).skip(rand.nextInt(1965462)).limit(1);
			}
			
			retrievedDoc = (Document) fi.first();
			
		} else {
			
			ObjectId id = new ObjectId();
			Document filter = new Document();
			if (rando >= 6){
				
				id = saleProducts.get(rand.nextInt(saleProducts.size()));
				filter.append("_id", id);
				fi = col.find(filter); 
				
			} else if (rando == 4){
				id = clearanceProducts.get(rand.nextInt(clearanceProducts.size()));
				filter.append("_id", id);
				fi = col.find(filter); 
				
			} else {
				
				id = regPriceProducts.get(rand.nextInt(regPriceProducts.size()));
				filter.append("_id", id);
				fi = col.find(filter); 
			}
			
			retrievedDoc = (Document) fi.first();
		}
		
		
		
		if(retrievedDoc != null) {
				orderDoc.append("_id", retrievedDoc.getObjectId("_id"));
			if (retrievedDoc.get("sku") != null)
				orderDoc.append("sku", retrievedDoc.get("sku"));
			if (retrievedDoc.get("productId") != null)
				orderDoc.append("productId", retrievedDoc.get("productId"));
			if (retrievedDoc.get("name") != null)
				orderDoc.append("product_name", retrievedDoc.get("name") );
			if (retrievedDoc.get("regularPrice") != null)
				orderDoc.append("regularPrice", retrievedDoc.get("regularPrice"));
			if (retrievedDoc.get("salePrice") != null)
				orderDoc.append("salePrice", retrievedDoc.get("salePrice"));
			if (retrievedDoc.get("clearance") != null)
				orderDoc.append("isClearance", retrievedDoc.get("clearance"));
			if (retrievedDoc.get("onSale") != null)
				orderDoc.append("isSale", retrievedDoc.get("onSale"));
		
			if (total){
				if (retrievedDoc.get("salePrice") != null )
					totalOrderCost += (double) retrievedDoc.getDouble("salePrice");
				else 
					totalOrderCost += (double) retrievedDoc.getDouble("regularPrice");
			}
		
		}
		
		return orderDoc;
	}

	private static Date getDate() {
		
		int year = 2000; 
		int month = 0;
		
		// set year with skew to 2016
		year += (int) random(0.0, 17.0, 0.5);
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

	private static boolean getIsMember() {
		
		boolean isMember = false;
		
		int x = rand.nextInt(1000000000);
		
		if (x <= 670459293) 
			isMember = true;
	
		return isMember;
	}
	
		
	
	
	
	private static String getPayType() {
		
		int x = rand.nextInt(10);
		
		String pt = "";
		
		if (x < 5 ) {  // credit card
			
			int y = rand.nextInt(2); 
			
			if (y==1)
				pt = "MasterCard";
			else
				pt = "Visa";
		
		} else if (x < 8)    // debit 
			pt = "Debit";
			
		 else  // cash
			pt = "Cash";
			
		return pt;
		
	}
	@SuppressWarnings("unused")
	

	private static double random(double low, double high, double bias)
	{
		double r = rand.nextFloat();    // random between 0 and 1
	    r = Math.pow(r, bias);
	    return low + (high - low) * r;
	}
	
private static Document calcPoints(Document doc) {
		
		Document member = new Document();
	
		
		DecimalFormat df = new DecimalFormat("#.##");
		df.setRoundingMode(RoundingMode.DOWN);
		
		member = (Document) doc.get("member");
	
		int points = 0;
		
		ArrayList<Document> prods = (ArrayList<Document>) doc.get("products");
		double cost = 0.0;
		
		ArrayList<ObjectId> prodIds = new ArrayList<ObjectId>();
		
		//itterate over list of products purchased.
		for (Document pDoc : prods ) {
//			System.out.println(points);
//			System.out.println("calulcating points for product:");
//			System.out.println(pDoc);
			
			
				
			prodIds.add(pDoc.getObjectId("_id"));
			
			
			// regular price
			if(pDoc.getBoolean("isClearance") != null && pDoc.getBoolean("isSale") != null){
				if (pDoc.getBoolean("isClearance") == false && pDoc.getBoolean("isSale") == false){
					cost = pDoc.getDouble("regularPrice");
					points += (cost * 100);
				// sale price
				} else if (pDoc.getBoolean("isClearance") == false && pDoc.getBoolean("isSale") == true){
					cost = pDoc.getDouble("salePrice");
					points += (cost * 100) * .75;
				// clearance price
				} else if (pDoc.getBoolean("isClearance") == true && pDoc.getBoolean("isSale") == true){
					cost = pDoc.getDouble("salePrice");
					points += (cost * 100) * .50;
				}
			}
		}
		
		//add list of products
		member.append("purchasedProducts", prodIds);
		
		
		//check VIP
		if (points >= 100000) {
			
			member.append("isVIP", true);
			
			// find status
			
			if (points < 250000)
				member.append("VIPStatus", "Bronze");
			else if (points < 600000)
				member.append("VIPStatus", "Silver");
			else if (points < 1000000)
				member.append("VIPStatus", "Gold");
			else if (points < 2500000)
				member.append("VIPStatus", "Platinum");
			else 
				member.append("VIPStatus", "Diamond");
			
		}
		
		//System.out.println("Total points for member is: " +  points + "\n\n");
		
		member.append("points", points );
		
		return member;
	}
	
	

}
