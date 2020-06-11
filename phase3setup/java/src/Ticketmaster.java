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
	
	/*
	public static void AddUser(Ticketmaster esql){//1

		readChoice();
		System.out.print("Looks good");
		// Get input from user
		
		 Users have:
    	email VARCHAR(64) NOT NULL,
    	lname VARCHAR(32) NOT NULL,  -- Last name
    	fname VARCHAR(32) NOT NULL,  -- First name
    	phone NUMERIC(10, 0),
    	pwd CHAR(64) NOT NULL,  -- SHA256 hash of password
    	PRIMARY KEY(email)
		
		return;
	}*/
	

	public static void AddUser(Ticketmaster esql) throws SQLException {//1


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

	public static void AddMovieShowingToTheater(Ticketmaster esql){//3

	}

	public static void CancelPendingBookings(Ticketmaster esql) throws SQLException{//4
		/*
		Sets status to 'Cancelled' for all records that have status = 'Pending'
		 */
		esql.executeUpdate("UPDATE bookings " +
				"SET status = 'Cancelled' " +
				"WHERE status = 'Pending'");
	}

	public static void ChangeSeatsForBooking(Ticketmaster esql) throws Exception{//5

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

	public static void ClearCancelledBookings(Ticketmaster esql){//7

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
		//

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
		//

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

	public static void ListMovieAndShowInfoAtCinemaInDateRange(Ticketmaster esql){//13
		//

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
