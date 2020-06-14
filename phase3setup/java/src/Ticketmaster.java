/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.*;
//import java.io.File;
//import java.io.FileReader;
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;


/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class Ticketmaster{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	static public int nxtMvid = 60;		//	tracks next available movie id
	static public int nxtSid = 200;		//	tracks next available show id
	public int nxtTid = 1000;		//	tracks next available theater id

	public Ticketmaster(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count number of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + Ticketmaster.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		Ticketmaster esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new Ticketmaster (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add User");
				System.out.println("2. Add Booking");
				System.out.println("3. Add Movie Showing for an Existing Theater");
				System.out.println("4. Cancel Pending Bookings");
				System.out.println("5. Change Seats Reserved for a Booking");
				System.out.println("6. Remove a Payment");
				System.out.println("7. Clear Cancelled Bookings");
				System.out.println("8. Remove Shows on a Given Date");
				System.out.println("9. List all Theaters in a Cinema Playing a Given Show");
				System.out.println("10. List all Shows that Start at a Given Time and Date");
				System.out.println("11. List Movie Titles Containing \"love\" Released After 2010");
				System.out.println("12. List the First Name, Last Name, and Email of Users with a Pending Booking");
				System.out.println("13. List the Title, Duration, Date, and Time of Shows Playing a Given Movie at a Given Cinema During a Date Range");
				System.out.println("14. List the Movie Title, Show Date & Start Time, Theater Name, and Cinema Seat Number for all Bookings of a Given User");
				System.out.println("15. EXIT");
				
				/*
				 * FOLLOW THE SPECIFICATION IN THE PROJECT DESCRIPTION
				 */
				switch (readChoice()){
					case 1: AddUser(esql); break;
					case 2: AddBooking(esql); break;
					case 3: AddMovieShowingToTheater(esql); break;
					case 4: CancelPendingBookings(esql); break;
					case 5: ChangeSeatsForBooking(esql); break;
					case 6: RemovePayment(esql); break;
					case 7: ClearCancelledBookings(esql); break;
					case 8: RemoveShowsOnDate(esql); break;
					case 9: ListTheatersPlayingShow(esql); break;
					case 10: ListShowsStartingOnTimeAndDate(esql); break;
					case 11: ListMovieTitlesContainingLoveReleasedAfter2010(esql); break;
					case 12: ListUsersWithPendingBooking(esql); break;
					case 13: ListMovieAndShowInfoAtCinemaInDateRange(esql); break;
					case 14: ListBookingInfoForUser(esql); break;
					case 15: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				// parseInt() converts string to int
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice
	

	public static void AddUser(Ticketmaster esql) throws IOException, SQLException {//1
		Scanner input = new Scanner(System.in);

		//Prompt user to enter email, lname, fname, phone, and pwd
		System.out.print("Enter email: ");
		String email = in.readLine();
		
		System.out.print("Enter last name: ");
		String lname = in.readLine();
		
		System.out.print("Enter first name: ");
		String fname = in.readLine();
		
		System.out.print("Enter phone number: ");
		String phone = in.readLine();
		
		System.out.print("Enter password: ");
		String pwd = in.readLine();

		//Insert values into the query
		String insertQuery = "INSERT INTO Users VALUES(" + "\'"+ email+ "\'" + "," +  "\'"+ lname+ "\'"
				+ "," + "\'" +fname+ "\'" +"," + phone+ "," + "\'" + pwd+ "\'" + ")";
		//Show the query to the console.
		//System.out.println(insertQuery);
		//Execute the query
		esql.executeUpdate(insertQuery);
		System.out.print("User has been added to database!\n");

	}

	public static void AddBooking(Ticketmaster esql) throws IOException, SQLException {//2
		Scanner input = new Scanner(System.in);
		/*
		Grab the values: bid, dateTime, email, sets, showing id, and status to insert into the query.
		 */
		String query = "SELECT MAX(bid) FROM bookings";
		//Return result as a list
		List<List<String>> maxBid = esql.executeQueryAndReturnResult(query);
		//Grab highest bid and add 1
		Integer bid = Integer.parseInt(maxBid.get(0).get(0)) + 1;
		//Grab the current date/time
		String dateTime = "(SELECT CURRENT_TIMESTAMP)";
		//Prompt user to enter email, seats, id, and status
		System.out.print("Enter email: ");
		String email = in.readLine();
		System.out.print("Enter number of seats: ");
		int numSeats = input.nextInt();
		System.out.print("Enter showing id: ");
		int sid = input.nextInt();
		System.out.print("Paid or Pending: ");
		String status = in.readLine().toLowerCase();
		boolean statusChoice = true;
		while(statusChoice)
		{
			if(status.equals("paid"))
			{
				status = "Paid";
				statusChoice = false;
			}
			else if(status.equals("pending"))
			{
				status = "Pending";
				statusChoice = false;
			}
			else
			{
				System.out.print("Invalid choice. \n Paid or Pending: ");
				status = in.readLine().toLowerCase();
			}


		}

		//Insert values into the query
		String insertQuery = "INSERT INTO bookings VALUES(" + bid+ "," +  "\'"+ status + "\'"
				+ "," + dateTime + "," + numSeats + "," + sid
				+ "," + "\'" + email +"\'" + ")";
		//Show the query to the console.
		//System.out.println(insertQuery);
		//Execute the query
		esql.executeUpdate(insertQuery);



	}

	public static void AddMovieShowingToTheater(Ticketmaster esql) throws IOException, SQLException {//3
		/* Add Movie Showing for an Existing Theater
		 * Add a showing of a new movie using the Shows, Plays, and Movie tables for a given
		 * theater.
		 * You should provide an interface that takes as input the information of a new
		 * movie (i.e. title, duration) and show(i.e. start time) and checks if the provided information 
		 * is valid based on the constraints of the database schema. 
		 * Note: The order matters for this query. 
		 * What happens when you try inserting in the wrong order? 
		 * Think about why that happens and you’ll know what the correct order should be.
		 * MUST DO inserts in the order movies -> shows -> plays
		 *
		 * MAKE mvid > 60 and sid > 200
		 * should automate this action
		 *
		 *
		 * NEED to get input from user;
		 * 
		 * QUERY these must be ran in this order through seperate function calls:
		 * insert into movies (mvid, title, rdate, country, description, duration, lang, genre) 
		 * values(60, 'fakefilm', '1988-10-02', 'United States', 'OK movie', 5555, 'en', 'documentary');
		 * 
		 * insert into shows (sid, mvid, sdate, sttime, edtime)
		 * values (201, 60, '1/2/2015', '03:00', '05:00'); 
		 * 
		 * insert into plays (sid, tid) values (201, 50);
		 */
		Scanner input = new Scanner(System.in);

		//Prompt user to enter title, rdate, country, description, duration, lang, genre
		System.out.print("Please enter the new movie title: ");
		String title = in.readLine();

		System.out.print("Please enter the movie release date: ");
		String rdate = in.readLine();

		System.out.print("Please enter the production Country of the new movie: ");
		String country = in.readLine();

		System.out.print("Please enter a short movie description: ");
		String description = in.readLine();

		System.out.print("Please enter the new movie duration: ");
		int duration = input.nextInt();

		System.out.print("Please enter the new movie language code, such as en, de, etc.: ");
		String lang = in.readLine();

		System.out.print("Please enter the genre of the new movie: ");
		String genre = in.readLine();

		// Prompt user for sdate, sttime, edtime
		System.out.print("Please enter the show date: ");
		String sdate = in.readLine();

		System.out.print("Please enter show start time: ");
		String sttime = in.readLine();

		System.out.print("Please enter show endtime: ");
		String edtime = in.readLine();

		// Insert values into the insert statement for movies
		String insrtMovies = "INSERT INTO movies VALUES(" + nxtMvid + "\'"+ title + "\'" + "," +  "\'"+ rdate + "\'"
				+ "," + "\'" + country + "\'" + "," + "\'" + description + "\'" + "," + duration + "," + "\'" + lang + "\'" + "," + "\'" + genre + "\'" + ")";
		// Show the query to the console.
		// System.out.println(insertQuery);
		// Execute the query
		esql.executeUpdate(insrtMovies);
		System.out.print("Your new Movie ID is " + nxtMvid);
		// input this new movie into the database then continue

		// Insert values into the insert statement for shows
		String insrtShows = "INSERT INTO shows VALUES(" + nxtSid + "," + nxtMvid + "," + "\'"+ sdate + "\'" + "," +  "\'"+ sttime + "\'"
				+ "," + "\'" + edtime + "\'" + ")";
		// Execute the query
		esql.executeUpdate(insrtShows);
		System.out.print("Your new Show ID is " + nxtSid);

		System.out.print("Please enter the TID that you would like your show assigned to: ");
		int tid = input.nextInt();

		String insrtPlays = "INSERT INTO plays VALUES(" + nxtSid + "," + tid + ")";
		esql.executeUpdate(insrtPlays);

		System.out.print("Your new movie has been scheduled to play! at " + tid +"\n");

		//nxtTid += 1;
		nxtMvid += 1;
		nxtSid = nxtSid++;
		// if mvid <= nxtMvid print(please enter a value greater than)
	}

	public static void CancelPendingBookings(Ticketmaster esql) throws IOException, SQLException{//4
		/*
		Sets status to 'Cancelled' for all records that have status = 'Pending'
		 */
		esql.executeUpdate("UPDATE bookings " +
				"SET status = 'Cancelled' " +
				"WHERE status = 'Pending'");
		System.out.print("All pending bookings have been cancelled!\n");
	}

	public static void ChangeSeatsForBooking(Ticketmaster esql) throws SQLException{//5
		/* Change Seats Reserved for a Booking
		 * JUST override the seats dont worry about if someone is sitting there;
		 * Replace the seats reserved for a given booking with different seats in the same theater.
		 * For example, a user changes their mind about where they want to sit. 
		 * They have already booked seats 10 and 11 but would like to move back one row to seats 20 and 21.
		 * This should only work if the new seats are available and they are the same price.
		 * 
		 * bookings -> showseats -> cinemaseats
		 * Ask user for bid and new seat numbers
		 * 
		 * THIS returns seat numbers for a specific booking ID
		 * select c.sno from bookings b, showseats s, cinemaseats c 
		 * where b.bid = 401 and b.sid = s.sid and s.csid = c.csid;
		 * 
		 * THIS changed all of the seat numbers to 4 in the users booking:
		 * update cinemaseats set sno = 4 where sno in 
		 * (select c.sno from bookings b, showseats s, cinemaseats c 
		 * where b.bid = 401 and b.sid = s.sid and s.csid = c.csid);
		 * 
		 * ASK the user for their booking number and what seat # they want to change & what they want to 
		 * change it to.
		 * 
		 * 
		 * QUERY:
		 * 
		 * 
		 * */
	}

	public static void RemovePayment(Ticketmaster esql)throws IOException, SQLException{//6
		Scanner input = new Scanner(System.in);
		/*
		Grab the booking id to set the status to 'Cancelled'
		 */
		//Grab the booking id
		System.out.print("Input the booking id: ");
		int bid = input.nextInt();

		//Execute the query to change the status from 'Paid' to 'Cancelled' based on the booking id
		esql.executeUpdate("UPDATE bookings " +
				"SET status = 'Cancelled' " +
				"WHERE bid = " + bid);

		/*
		Deletes payments based on the booking that was changed from 'Paid' to 'Cancelled'
		*/
		esql.executeUpdate("DELETE FROM payments " +
				"USING bookings " +
				"WHERE payments.bid = " + bid + " " +
				"AND bookings.status = 'Cancelled'");

	}

	public static void ClearCancelledBookings(Ticketmaster esql) throws SQLException {//7
		/* Clear Cancelled Bookings
		 * QUERY:
		 * delete from bookings where status = 'Cancelled'
		*/
		String clrBooks = "delete from bookings where status = 'Cancelled'";
		esql.executeUpdate(clrBooks);
		System.out.print("All bookings with status cancelled have been removed from DataBase.\n");
	}

	public static void RemoveShowsOnDate(Ticketmaster esql) throws IOException, SQLException {//8
		/*
		Remove all shows on a given date based on a specific cinema (cinema theater?)
		 */
		//Grab the show id
		System.out.print("Input the date that you want the shows removed (yyyy-mm-dd): ");
		String date = in.readLine();

		//Grab the cinema theater name
		System.out.print("Input the cinema theater where you want the show removed: ");
		String cinematheater = in.readLine();

		//Execute query to remove shows based on a given date and cinema theater
		esql.executeUpdate("DELETE FROM plays " +
				"USING shows, theaters " +
				"WHERE shows.sdate = " + "\'" + date + "\' " +
				"AND theaters.tname = " + "\'" + cinematheater + "\' " +
				"AND shows.sid = plays.sid " +
				"AND theaters.tid = plays.tid");
		/*
		If there are nay bookings on this day, set the status to 'Cancelled'
		 */
		//Execute query to update the table if there are any bookings on the given day
		esql.executeUpdate("UPDATE bookings " +
				"SET status = 'Cancelled' " +
				"WHERE bdatetime::text LIKE " + "\'" + date + "%\'");


	}

	public static void ListTheatersPlayingShow(Ticketmaster esql){//9
		/*
		 * List all Theaters in a Cinema -DO THIS FIRST// Doesn't work every cinema has 1 theater
		 * Playing a Given Show
		 * 
		 * Given only a movie title, so in this QUERY 'The Lion King' would be user input
		 * 
		 * QUERY:
		 * select t.tname, s.sttime from movies m, shows s, plays p, theaters t 
		 * where m.title = 'The Lion King' and m.mvid = s.mvid and s.sid = p.sid and t.tid = p.tid;
		 * 
		 * */
		
		
	}

	public static void ListShowsStartingOnTimeAndDate(Ticketmaster esql) throws IOException, SQLException {//10
		/*
		List the shows that start on the given time and date
		 */
		//Grab the date
		System.out.print("Input the date that you are searching for (yyyy-mm-dd): ");
		String date = in.readLine();
		//Grab the time
		System.out.print("Input the start time that you are searching for (hh:mm:ss): ");
		String time = in.readLine();

		//Execute query
		esql.executeQueryAndPrintResult("SELECT * " +
				"FROM shows " +
				"WHERE sdate = " + "\'" + date + "\'" +
				"AND sttime = " + "\'" + time + "\'");	
	}

	public static void ListMovieTitlesContainingLoveReleasedAfter2010(Ticketmaster esql){//11
		/* List Movie Titles Containing “love” Released After 2010
		 * 
		 * QUERY:
		 * select * from movies where title like '%Love%' and rdate > '2010-01-01';
		 */ 
	}

	public static void ListUsersWithPendingBooking(Ticketmaster esql) throws SQLException{//12
		/*
		List the First Name, Last Name, and email of Users with 'Pending' bookings
		 */
		//Execute query
		esql.executeQueryAndPrintResult("SELECT u.fname, u.lname, u.email " +
				"FROM users u, bookings b " +
				"WHERE u.email = b.email " +
				"AND b.status = 'Pending' ");

	}

	public static void ListMovieAndShowInfoAtCinemaInDateRange(Ticketmaster esql)throws IOException, SQLException {//13
		//I adjusted the results to include theaters that are actually playing the requested movie.
		/* List the Title, Duration, Date, and Time of Shows Playing a Given Movie at a 
		 * Given Cinema During a Date Range:
		 * 	List the Movie Title, Movie Duration, Show Date, and Show Start Time of all Shows
		 * 	playing a given Movie at a given Cinema within a date range. 
		 * 	This would be useful for users to find the best time for their schedule to book a 
		 * 	showing of a movie at a given cinema.
		 * 
		 * we get tid and movie title and dates range begin and end
		 * 
		 * QUERY:
		 * select m.title, m.duration, s.sdate, s.sttime 
		 * from movies m, shows s, plays p, theaters t 
		 * where m.title = 'Titanic' and s.sdate between '1995-01-01' and '2019-03-01' 
		 * and m.mvid = s.mvid and s.sid = p.sid and t.tid = p.tid;
		 * 
		 */
		// Implementing this input usually yeilded no search results
		// "t.cid =" +  cid
		// Scanner input = new Scanner(System.in);
		// System.out.print("Please enter the theater ID that you would like to search: ");
		// int cid = input.nextInt();

		System.out.print("Please enter the movie title that you would like to search: ");
		String title = in.readLine();
		
		System.out.print("Please enter the start date for your search (yyyy-mm-dd): ");
		String bgnRange = in.readLine();

		System.out.print("Please enter the end date for your search (yyyy-mm-dd): ");
		String endRange = in.readLine();

		String rangeQuery = "select t.tname, m.title, m.duration, s.sdate, s.sttime from movies m, shows s, plays p, theaters t where m.title = " + "\'" + title + "\'" + "and s.sdate between" + "\'" + bgnRange + "\'" + " and " + "\'" + endRange + "\'" + "and m.mvid = s.mvid and s.sid = p.sid and p.tid =  + t.tid";

		esql.executeQueryAndPrintResult(rangeQuery);
		System.out.print(esql.executeQuery(rangeQuery) + " Results matched your criteria\n");
	}

	public static void ListBookingInfoForUser(Ticketmaster esql)throws IOException, SQLException{//14
		/*
		List the Movie Title, Show Date & Start Time, Theater Name, and Cinema Seat Number fora all Bookings
		of a Given User
		 */
		//Grab the email
		System.out.print("Input the email to search for: ");
		String email = in.readLine();

		//execute query
		//**Also include a check for booking status to make sure the booking has been paid for. Will not display
		//any results if status is 'Pending' or 'Cancelled'**
		esql.executeQueryAndPrintResult("SELECT DISTINCT m.title, s.sdate, s.sttime, t.tname, c.sno " +
				"FROM movies m, shows s, theaters t, cinemaseats c, bookings b, plays p " +
				"WHERE b.email = " + "\'" + email + "\'" +
				"AND b.sid = s.sid " +
				"AND s.mvid = m.mvid " +
				"AND s.sid = p.sid " +
				"AND t.tid = p.tid " +
				"AND c.tid = t.tid " +
				"AND b.status = 'Paid'");

	}
	
}
