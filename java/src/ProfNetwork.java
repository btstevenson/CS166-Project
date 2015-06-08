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
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;



/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */



public class ProfNetwork {

   // reference to physical database connection.
   private Connection _connection = null;
	
      // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   public static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of ProfNetwork
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public ProfNetwork (String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end ProfNetwork

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
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
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = false;
      while (rs.next()){
		if(outputHeader){
			for(int i = 1; i <= numCol; i++){
				System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		}
         for (int i=1; i<=numCol; ++i){
            System.out.print (rs.getString (i) + "\t");
		 }
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
	  System.out.println("\n");
      return rowCount;
   }//end executeQuery

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
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
          List<String> record = new ArrayList<String>();
         for (int i=1; i<=numCol; ++i){
            record.add(rs.getString (i));
		 }
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
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
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
	if (rs.next())
		return rs.getInt(1);
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
            "Usage: " +
            "java [-classpath <classpath>] " +
            ProfNetwork.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      ProfNetwork esql = null;
	  Profile prof = new Profile();
	  Messenger menu = new Messenger();
	  UserConnect conn = new UserConnect();
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the ProfNetwork object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new ProfNetwork (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
			  prof.GetCurrProfile(esql, authorisedUser);
              while(usermenu) {
                System.out.println("\nMAIN MENU");
                System.out.println("---------");
                System.out.println("1. Profile Menu");
                System.out.println("2. Messages Menu");
                System.out.println("3. Connections Menu");
				System.out.println("4. Search");
                System.out.println(".........................");
                System.out.println("9. Log out");
                switch (readChoice()){
                   case 1: prof.ProfileMenu(esql, authorisedUser); break;
                   case 2: menu.MessageService(esql, authorisedUser); break;
                   case 3: conn.ConnectMenu(esql, authorisedUser); break;
				   case 4: conn.Search(esql); break;
                   case 9: usermenu = false; 
						   TableCleanup(esql, authorisedUser); break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
			System.out.println();
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    * An empty block and contact list would be generated and associated with a user
    **/
   // need to add additional options for other details although these are not required
   public static void CreateUser(ProfNetwork esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user email: ");
         String email = in.readLine();

	 //Creating empty contact\block lists for a user
	 String query = String.format("INSERT INTO USR (userId, password, email) VALUES ('%s','%s','%s')", login, password, email);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         //System.err.println (e.getMessage ());
		 String error = e.getMessage();
		 if(error.toLowerCase().contains("duplicate")){
			 System.out.print("\t\nERROR: User login already exists. Please try again.\n");
		 }
      }
   }//end

   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(ProfNetwork esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USR WHERE userId = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0){
		 query = String.format("SELECT conn_search('"+login+"')");
		 userNum = esql.executeQuery(query);		 
		return login;
	 }
	 else
	 {
		System.out.println("\t\nThe credentials provided were not valid. Please try again.\n");
	 }
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

   public static void TableCleanup(ProfNetwork esql, String currentUser){
	   String query = String.format("DROP TABLE %s",  "\""+ currentUser +"\"");
	   try{
		esql.executeUpdate(query);
	   } catch (Exception e){
		   System.err.println(e.getMessage());
	   }
   }

}//end ProfNetwork

/********************************************************
* Messenger Class
* Programmer: Brandon Stevenson
* Date: 5/27/15
* Purpose: To provide a ui for the user in order to
*		   facilitate the reading, sending, deleting
*		   of messages from the DBMS.
*
********************************************************/
class Messenger{

	/*************************************
	* Method: MessageService
	* Programmer: Brandon Stevenson
	* Date: 5/27/15
	* Purpose: Provides ui to give access
	*          to message options
	*
	* Inputs: Profnetwork object
	*		  String currenUser
	*
	* Return: none
	*
	*************************************/
	public static void MessageService(ProfNetwork esql, String currentUser){
		boolean menuOn = true;
		while(menuOn){
			System.out.println("\nMessenger Menu");
			System.out.println("---------");
			System.out.println("1. Read Menu");
			System.out.println("2. Send Menu");
			System.out.println(".........................");
			System.out.println("9. Return to main menu\n");
			
			switch(esql.readChoice()){
				case 1: ReadMessageMenu(esql, currentUser);
						break;
				case 2: SendMessageMenu(esql, currentUser);
						break;
				case 9: menuOn = false; break;
				
				default: System.out.println("\nERROR: Choice is not valid. Please try again.\n");
						
			}
		}
	}

	public static void ReadMessageMenu(ProfNetwork esql, String currentUser){
		boolean getChoice = true;
		while(getChoice){
			System.out.println("\nRead Messages Menu");
			System.out.println("---------");
			System.out.println("1. Show new messages");
			System.out.println("2. Read Message");
			System.out.println("3. Show all received messages");
			System.out.println("4. Delete received message");
			System.out.println("---------");
			System.out.println("9. Return to Messenger Menu\n");

			switch(esql.readChoice()){
				case 1: try{
							String query = String.format("SELECT msgid, senderid FROM message WHERE receiverid = '%s' AND status = '%s' AND (delete_status = '0' OR delete_status = '1')", currentUser, "Delivered");

							int result = esql.executeQueryAndPrintResult(query);
							if(result < 1){
								System.out.println("There are no unread messages.");
							}
						}catch(Exception e){
							System.err.println(e.getMessage());
						}
						break;
				case 2: ReadMessage(esql, currentUser);	 
						break;
				case 3: try{
							String query = String.format("SELECT msgid,senderid FROM message WHERE receiverid = '%s' AND (status = '%s' OR status = '%s') AND (delete_status = '0' OR delete_status ='1')", currentUser, "Delivered", "Read");
							int result = esql.executeQueryAndPrintResult(query);
							if(result < 1){
								System.out.println("There are no messages in your inbox.");
							}
						}catch(Exception e){
							System.err.println(e.getMessage());
						}
						break;
				case 4: DeleteMessage(esql, currentUser, "read");
						break;
				case 9: getChoice = false; 
						break;
				default: System.out.println("\nERROR: Invalid input. Please try again.\n");
			}
		}
	}
	/* allows user to read recieved message */	
	public static void ReadMessage(ProfNetwork esql, String currentUser){
		System.out.print("\t\nPlease enter the message id you would like to read: ");
		try{
			String input = esql.in.readLine();
			int msgid = Integer.parseInt(input.trim());
			try{
				System.out.println();
				String query = String.format("SELECT contents FROM message WHERE msgid = '%s' AND (delete_status = '0' OR delete_status = '1')", msgid);
				int result = esql.executeQueryAndPrintResult(query);
				if(result < 1){
					System.out.println("No message found with that id. Please try again.");
				}
				else{
					try{
						query = String.format("UPDATE message SET status = '%s' WHERE msgid = '%s'", "Read", msgid);
						esql.executeUpdate(query);
					}catch (Exception e){
					}
				}
			}catch (Exception e){
				System.err.println(e.getMessage());
			}
		}catch (Exception e){
			System.out.println("Invalid input. The message id is an integer. Please try again");
		}
	}
	// maybe add a search option so a user can message anyone on network from here
	public static void SendMessageMenu(ProfNetwork esql, String currentUser){
		boolean getChoice = true;
		while(getChoice){
			System.out.println("\nSend Messages Menu");
			System.out.println("---------");
			System.out.println("1. Send Message");
			System.out.println("2. List of Sent Messages");
			System.out.println("3. View a Sent Message");
			System.out.println("4. Delete Sent Message");
			System.out.println("---------");
			System.out.println("9. Return to Messenger Menu");

			switch(esql.readChoice()){
				case 1: SendMessage(esql, currentUser);
						break;
				case 2: ListSentMessages(esql, currentUser);
						break;
				case 3: ViewSentMessage(esql, currentUser);
						break;
				case 4: DeleteMessage(esql, currentUser, "send");
						break;
				case 9: getChoice = false;
						break;
				default: System.out.println("Invalid Choice. Please try again.");
			}
		}
	}
	/* allows user to send a message to another user */
	public static void SendMessage(ProfNetwork esql, String currentUser){
		try{
			System.out.print("Please enter the userid of the user you want to send the message: ");
			String receiveid = esql.in.readLine();
			System.out.print("\nPlease enter the message you want to send: ");
			String contents = esql.in.readLine();
			try{
				String query = String.format("INSERT INTO message (senderid, receiverid, contents, delete_status, status) " + "VALUES('"+currentUser+"', '"+receiveid+"', '"+contents+"', 0, 'Delivered')");
					esql.executeUpdate(query);
			} catch (Exception e){
				System.err.println(e.getMessage());
			}
		} catch (Exception e){
			System.err.println(e.getMessage());
		}
	}
	/* create triggers for setting delete_status and status for inserting messages */
	// for sending messages from viewing someones profile
	public static void SendMessageProfile(ProfNetwork esql, String currentUser, String receiverId){
		try{
			System.out.print("Please enter the message you want to send: ");
			String contents = esql.in.readLine();
			try{
				String query = String.format("INSERT INTO message (senderId, receiverId, contents, delete_status, status) " + "VALUES('"+currentUser+"', '"+receiverId+"', '"+contents+"', 0, 'Delivered')");
				esql.executeUpdate(query);
			} catch (Exception e){
				System.err.println(e.getMessage());
			}
		} catch (Exception e){
			System.err.println(e.getMessage());
		}
	}
    /* lists all sent mesages */
	public static void ListSentMessages(ProfNetwork esql, String currentUser){
		try{
			String query = String.format("SELECT msgid, receiverid, status FROM message WHERE senderid = '%s' AND (delete_status = '0' OR delete_status = '2')", currentUser);
			int result = esql.executeQueryAndPrintResult(query);
			if(result < 1){
				System.out.println("You have no sent messages.");
			}
		} catch (Exception e){
			System.err.println(e.getMessage());
		}
	}
	/* allows user to view a sent message */
	public static void ViewSentMessage(ProfNetwork esql, String currentUser){
		try{
			System.out.print("Please enter the message id you want to view: ");
			String input = esql.in.readLine();
			try{
				int msgid = Integer.parseInt(input.trim());
				try{
					System.out.print("\nMessage: ");
					String query = String.format("SELECT contents FROM message WHERE msgid = '%s' AND senderid = '%s'", msgid, currentUser);
					int result = esql.executeQueryAndPrintResult(query);
					if(result < 1){
						System.out.println("There is no sent message with that id. Please try again.");
					}
				} catch (Exception e){
					System.err.println(e.getMessage());
				}
			} catch (Exception e){
				System.out.println("ERROR: Invalid input. Please enter an integer.");
			}
		} catch (Exception e){
			System.err.println(e.getMessage());
		}
	}
	/* deletes a message by prompting a user what msg they want to delete. might be a better 
	   approach to print out the numbers and have them choose just based off the number and not
	   msgid*/
	public static void DeleteMessage(ProfNetwork esql, String currentUser, String type){
		System.out.print("Please enter the message id that you would like to delete: ");
		try{
			String input = esql.in.readLine();
			int msgid = Integer.parseInt(input.trim());
			try{
				if(type.equals("send")){
					String query = String.format("SELECT delete_status FROM message WHERE msgid = '%s' AND senderid = '%s'", msgid, currentUser);
					List<List<String>> result = new ArrayList<List<String>>();
					result = esql.executeQueryAndReturnResult(query);
					try{
						if(result.get(0).get(0).equals("0")){
							query = String.format("UPDATE message set delete_status = '1' WHERE msgid = '%s'", msgid);
							esql.executeUpdate(query);
						} else{
							query = String.format("DELETE FROM message WHERE msgid = '%s'", msgid);
							esql.executeUpdate(query);
						}
					} catch (Exception e){
						System.err.println(e.getMessage());
					}
				} else {
					String query = String.format("SELECT delete_status FROM message WHERE msgid = '%s' AND receiverid = '%s'", msgid, currentUser);
					List<List<String>> result = new ArrayList<List<String>>();
					result = esql.executeQueryAndReturnResult(query);
					try{
						if(result.get(0).get(0).equals("0")){
				
							query = String.format("UPDATE message SET delete_status = '2' WHERE msgid = '%s'", msgid);
							esql.executeUpdate(query);
						} else{
							query = String.format("DELETE FROM message WHERE msgid = '%s'", msgid);
							esql.executeUpdate(query);
						}
					} catch (Exception e){
						System.err.println(e.getMessage());
					}
				}
			} catch (Exception e){
				System.err.println(e.getMessage());
			}
		} catch (Exception e){
			System.err.println(e.getMessage());
		}
	}
} // end Messenger

/**************************************************
* UserConnect class
* Programmer: Brandon Stevenson
* Date: 5/29/2015
* Purpose: Provides methods for sending connection
*		   requests and checking if request is
*		   in terms of connection depth.
*
**************************************************/
class UserConnect{
	//display the connection menu allowing user to accept/decline or send requests
	public static void ConnectMenu(ProfNetwork esql, String currentUser){
		boolean getChoice = true;
		while(getChoice){
			System.out.println("\nConnection Menu");
			System.out.println("1. View Connection List");
			System.out.println("2. Accept/Decline Requests");
			System.out.println("3. Send Connection Request");
			System.out.println("---------");
			System.out.println("9. Return to main menu");
				
			switch(esql.readChoice()){
				case 1: ConnectionList(esql, currentUser); break;
				case 2: ViewRequest(esql, currentUser); break;
				case 3: NonProfileRequest(esql, currentUser); break;
				case 9: getChoice = false; break;
				default: System.out.println("Invalid choice. Please try again.");
			}
		}
	}
	// displays connections of user
	// need to add menu to be able to view a friends profile
	// list the friends by number so user can select which one to view
	public static void ConnectionList(ProfNetwork esql, String currentUser){
	   try{
			String query = String.format("SELECT connectionid FROM connection_usr WHERE userID = '%s' AND status										= '%s' UNION SELECT userid FROM connection_usr WHERE connectionid = '%s' AND status= '%s'", 
						   currentUser, "Accept", currentUser, "Accept");
	   System.out.println("\nFriends List:");
	   int result = esql.executeQueryAndPrintResult(query);
	   if(result < 1){
			System.out.println("You currently do not have any friends. Try sending connection requests.\n");
	   }
	   }catch(Exception e){
		   System.err.println (e.getMessage());
	   }

   }

	//lists pending requests and allows user to accept or decline by using a menu
	public static void ViewRequest(ProfNetwork esql, String currentUser){
		List<List<String>> result = new ArrayList<List<String>>();
		try{
			String query = String.format("SELECT userid FROM connection_usr WHERE connectionid = '"+currentUser+"' AND status = 'Request'");		
			result = esql.executeQueryAndReturnResult(query);
			if(result.isEmpty()){
				System.out.println("There are no pending connection requests.");
			} else{
				/* gives an interactive menu for the user to accept/reject requests from */
				boolean getChoice = true;
				while(!result.isEmpty() && getChoice){
					int count = 1;
					System.out.println("Connection Requests: ");
					for(int i = 0; i < result.size(); i++){
						System.out.println(""+count+". " + ""+result.get(i).get(0)+"");
						count++;
					}
					System.out.println("\n1. Accept request");
					System.out.println("2. Reject request");
					System.out.println("---------");
					System.out.println("9. Return to Connection Menu");

					int reqChoice = 0;
					switch(esql.readChoice()){
						case 1: System.out.print("Please enter the number of the request to accept: ");
								try{
									reqChoice = Integer.parseInt(esql.in.readLine().trim()) - 1;
									query = String.format("UPDATE connection_usr SET status = 'Accept' WHERE userid = '"+result.get(reqChoice).get(0)+"' AND connectionid = '"+currentUser+"'");
									try{
										esql.executeUpdate(query);
										result.remove(reqChoice);
									} catch (Exception e){
										System.err.println(e.getMessage());
									}
								} catch (Exception e){
									System.err.println(e.getMessage());
								}
								break;
						case 2: System.out.print("Please enter the number of the request to reject: ");
								try{
									reqChoice = Integer.parseInt(esql.in.readLine().trim()) - 1;
									query = String.format("UPDATE connection_usr SET status = 'Reject' WHERE userid = '"+result.get(reqChoice).get(0)+"' AND connectionid = '"+currentUser+"'");
									try{
										esql.executeUpdate(query);
										result.remove(reqChoice);
									} catch (Exception e){
										System.err.println(e.getMessage());
									}
								} catch (Exception e){
									System.err.println(e.getMessage());
								}
								break;
						case 9: getChoice = false; break;
						default: System.out.println("Invalid choice. Please try again.");
					}
					System.out.println();	
				}
			}
					
		} catch (Exception e){
			System.err.println(e.getMessage());
		}
	}

	// for sending connection requests from connection menu, will prompt for input
	public static void NonProfileRequest(ProfNetwork esql, String currentUser){
		System.out.print("\nPlease enter the userid of the person you want to connect with: ");
		try{
			String userReq = esql.in.readLine();
			if(ConnectionDepthCheck(esql, currentUser, userReq)){
				// look into making a trigger that fills in the status part of connection_usr
				String query = String.format("INSERT INTO connection_usr (userId, connectionId, status) " + "VALUES('"+currentUser+"', '"+userReq+"', 'Request')");
				esql.executeUpdate(query);
			} else {
				System.out.println("\nCannot send request to this user. They are not within a valid connection level.");
			}
		} catch (Exception e){
			System.err.println(e.getMessage());
		}
	}
	
	// for sending connection requests from profile view
	public static void ProfileRequest(ProfNetwork esql, String currentUser, String userReq){
		if(ConnectionDepthCheck(esql, currentUser, userReq)){
			System.out.println("Sending connection request to '"+userReq+"'");
			String query = String.format("INSERT INTO connection_usr (userId, connectionId, status) " + "VALUES('"+currentUser+"', '"+userReq+"', 'Request'");
			try{
				esql.executeUpdate(query);
			} catch (Exception e){
				System.err.println(e.getMessage());
			}
		} else{
			System.out.println("Cannot send a connection request to this user.");
		}
	}
	/*this will need to modified later in order to better suit the rest of the project
	  the stored procedure should create a view that holds all connection possibilites for the user
	  allowing for a quick check when viewing profiles if the connection option should be allowed */
	public static boolean ConnectionDepthCheck(ProfNetwork esql, String currentUser, String userReq){
		boolean status = false;
		List<List<String>> result = new ArrayList<List<String>>();
		String query = String.format("SELECT count(*) FROM connection_usr WHERE userid = '%s' AND status = 'Accept' OR connectionid = '%s' AND status = 'Accept'", currentUser, currentUser);
		// add check for 5 count of friends and execute depth check only if count > 4
		try{
			result = esql.executeQueryAndReturnResult(query);
			int count = Integer.parseInt(result.get(0).get(0).trim());
			if(count > 4){	
				try{
					query = String.format("SELECT * FROM %s WHERE userid = '%s'", "\"" + currentUser + "\"", userReq);
					int size = esql.executeQuery(query);
					if(size > 0){
						status = true;
					} else {
						status = false;
					}
				} catch (Exception e){
					System.err.println(e.getMessage());
					status = false;
				}
			} else {
				status = true;
			}
		} catch (Exception e){
			status = false;
			System.err.println(e.getMessage());
		}
		return status;
	}
	
	// might need to add user paramater
	// will need to be updated to check for connection status or not
	// maybe create procedue to return partial matches of name
	public static void Search(ProfNetwork esql){
		List<List<String>> result = new ArrayList<List<String>>();
		try{
			System.out.print("Please enter the first name of user: ");
			String firstName = esql.in.readLine();
			try{
				System.out.print("Please enter the last name of user: ");
				String lastName = esql.in.readLine();
				String fullName = ""+firstName+" " + lastName;
				boolean getName = true;
				System.out.print("Search Results: ");
				String query = String.format("SELECT name FROM usr WHERE name = '"+fullName+"'");
				try{
					result = esql.executeQueryAndReturnResult(query);
					if(result.isEmpty()){
						System.out.println("No results");
					} else{
						int count = 1;
						for(int i = 0; i < result.size(); i++){
							//for now will not need inner loop
							//if selecting more will need to add inner loop
							System.out.println("\n\t"+count+". " + ""+result.get(i).get(0)+"");
							count++;
						}
					}
				} catch (Exception e){
					System.err.println(e.getMessage());
				}
				System.out.println("\n1. View Profile");
				System.out.println("2. Send Message");
				System.out.println("3. Search Again");
				System.out.println("---------");
				System.out.println("9. Return to Main Menu");
				switch(esql.readChoice()){
					case 1: break;
					case 2: break;
					case 3: Search(esql); break;
					case 4: break;
				}
			} catch (Exception e){
				System.err.println(e.getMessage());
			}
		} catch (Exception e){
			System.err.println(e.getMessage());
		}
	}
	
} // end UserConnect

class Profile{
	Messenger msg = new Messenger(); // local global for accessing messenger methods
	UserConnect conn = new UserConnect(); // local global for accessing userconnect methods
	
	public static void ProfileMenu(ProfNetwork esql, String currentUser){
		boolean getChoice = true;
		while(getChoice){
			// Potentially add friends list in here
			System.out.println("Profile Menu");
			System.out.println("1. Change E-mail");
			System.out.println("2. Change password");
			System.out.println("3. Change Full Name");
			System.out.println("4. Change Date of Birth");
			System.out.println("5. Add Work Experience");
			System.out.println("6. Update Work Experience");
			System.out.println("7. Delete Work Experience");
			System.out.println("8. Add Education Details");
			System.out.println("9. Update Education Details");
			System.out.println("10. Remove Education Details");
			System.out.println("11. Display Current Profile");
			System.out.println("---------");
			System.out.println("12. Return to Main Menu");

			switch(esql.readChoice()){
<<<<<<< HEAD
				case 1: break;
				case 2: break;
				case 3: break;
				case 4: break;
				case 5: break;
				case 6: break;
				case 7: break;
				case 8: break;
				case 10: getChoice = false; break;
=======
				case 1: UpdateEmail(esql,currentUser); break;
				case 2: UpdatePassword(esql,currentUser); break;
				case 3: UpdateFullName(esql,currentUser); break;
				case 4: UpdateDateofBirth(esql,currentUser); break;
				case 5: AddWorkExp(esql,currentUser); break;
				case 6: UpdateWorkExp(esql,currentUser); break;
				case 7: DeleteWorkExp(esql,currentUser); break;
				case 8: AddSchool(esql,currentUser); break; 
				case 9: UpdateSchool(esql,currentUser); break; 
				case 10: DeleteSchool(esql,currentUser); break;
				case 11: GetCurrProfile(esql,currentUser); break;
				case 12: getChoice = false; break;
>>>>>>> 57968d3949fe1df7c3456510f9f760548b176e52
				default: System.out.println("Invalid input. Please try again.");
			}
		}
	}

	// for listing the current users profile to them
	public static void GetCurrProfile(ProfNetwork esql, String currentUser){
		String query = String.format("SELECT name, email, date_of_birth FROM usr WHERE userid = '"+currentUser+"'");
		List<List<String>> result = new ArrayList<List<String>>();
		try{
			result =  esql.executeQueryAndReturnResult(query);
			System.out.println("Name: "+result.get(0).get(0)+"");
			System.out.println("Email: "+result.get(0).get(1)+"");
			System.out.println("Date of Birth: "+result.get(0).get(2)+"");
		} catch (Exception e){
			System.err.println(e.getMessage());
		}
		result.clear();
		query = String.format("SELECT company, role, location, start_date, end_date FROM work_expr WHERE userid = '"+currentUser+"'");
		int count = 1;
		try{
			// prints out work details
			System.out.println("Work Experience: ");
			result = esql.executeQueryAndReturnResult(query);
			if(result.isEmpty()){
				System.out.println("None");
			} else{
				for(int i = 0; i < result.size(); i++){
					System.out.print("\t "+count+". ");
					for(int j = 0; j < result.get(i).size(); j++){	
						System.out.print(""+result.get(i).get(j)+"");
						System.out.print(" ");
					}
					System.out.println();
					count++;
				}
				System.out.println();
			}
		} catch (Exception e){
			System.err.println(e.getMessage());
		}
		count = 1;
		result.clear();
		query = String.format("SELECT institution_name, major, degree, start_date, end_date FROM educational_details WHERE userid = '"+currentUser+"'");
		try{
			// prints out education details
			System.out.println("Education: ");
			result = esql.executeQueryAndReturnResult(query);
			if(result.isEmpty()){
				System.out.println("None\n");
			} else{
				for(int i = 0; i < result.size(); i++){
					System.out.print("\t "+count+". "); 
					for(int j = 0; j < result.get(i).size(); j++){
						System.out.print(""+result.get(i).get(j)+"");
						System.out.print(" ");
					}
					count++;
					System.out.println();
				}
				System.out.println();
			}
		} catch (Exception e){
			System.err.println(e.getMessage());
		}
	}
	
	// for displaying user profiles, will have check to see if connection or not
	public static void ViewUserProfile(ProfNetwork esql, String currentUser, String usrName){
	}
	
	// allows to search any user
	public static void SearchProfile(ProfNetwork esql){
	}

	//The users first chance to enter work experience information
	public static void AddWorkExp(ProfNetwork esql, String currentUser){
		try{
			System.out.print("\tEnter new Company Name: ");
         		String companyName = esql.in.readLine();
			System.out.print("\tEnter new Role: ");
         		String userRole = esql.in.readLine();
			System.out.print("\tEnter new Location: ");
         		String userLocation = esql.in.readLine();
			System.out.print("\tEnter new Start Date: ");
         		String startDate = esql.in.readLine();
			System.out.print("\tEnter new End Date: ");
         		String endDate = esql.in.readLine();
			
			String query = String.format("INSERT INTO work_expr (userId, company, role, location, start_date, end_date) VALUES('%s', '%s', '%s', '%s', '%s', '%s')",currentUser, companyName, userRole, userLocation, startDate, endDate);

			esql.executeUpdate(query);
		}catch (Exception e){
			System.err.println(e.getMessage());
			}
	}

	// Allows the user to update their work experience after they have already entered information before
	public static void UpdateWorkExp(ProfNetwork esql, String currentUser){
		String companyName;
		String userRole;
		String userLocation;
		String startDate;
		String endDate;
		try{
			String testQuery = String.format("SELECT company, role FROM work_expr WHERE userId = '%s'",currentUser);
			List<List<String>> result  = new ArrayList<List<String>>();
			result = esql.executeQueryAndReturnResult(testQuery);
			if (result.isEmpty()){
				System.out.println("You need to go back to the previous menu and Add Work Experience before you can update it.");
			}
			else{
				boolean keepon = true;
				System.out.println("\tEnter the company name of the entry you wish to update");
				companyName = esql.in.readLine();
				System.out.println("\tEnter the role of the entry you wish to update");
				userRole = esql.in.readLine();
				System.out.println("\tEnter the start date of the entry you wish to update");
				startDate = esql.in.readLine();

				while (keepon){
					

					System.out.println("UPDATE WORK EXPERIENCE");
					System.out.println("----------");
				//	System.out.println("1. Update Company Name");
				//	System.out.println("2. Update Role");
					System.out.println("1. Update Location");
				//	System.out.println("4. Update Start Date");
					System.out.println("2. Update End Date");
					System.out.println("9. Quit Updating Work Experience");

					switch (esql.readChoice()){
						/*case 1: 
							System.out.print("\tEnter new Company Name: ");
         						companyName = esql.in.readLine();
							String query1 = String.format("UPDATE work_expr set company = '%s' WHERE userId = '"+currentUser+"'",companyName);
							esql.executeUpdate(query1); break;
						case 2: 
							System.out.print("\tEnter new Role: ");
         						userRole = esql.in.readLine();
							String query2 = String.format("UPDATE work_expr set role = '%s' WHERE userId = '"+currentUser+"'",userRole);
							esql.executeUpdate(query2); break;*/
						case 1:
							System.out.print("\tEnter new Location: ");
         						userLocation = esql.in.readLine();
							String query3 = String.format("UPDATE work_expr set location = '%s' WHERE userId = '"+currentUser+"' AND company = '%s' AND role = '%s' AND start_date = '%s'",userLocation, companyName, userRole, startDate);
							esql.executeUpdate(query3); break;
						/*case 4:
							System.out.print("\tEnter new Start Date: ");
         						startDate = esql.in.readLine();
							String query4 = String.format("UPDATE work_expr set start_date = '%s' WHERE userId = '"+currentUser+"'",startDate);
							esql.executeUpdate(query4); break;*/
						case 2:
							System.out.print("\tEnter new End Date: ");
         						endDate = esql.in.readLine();
							String query5 = String.format("UPDATE work_expr set end_date = '%s' WHERE userId = '"+currentUser+"'AND company = '%s' AND role = '%s' AND start_date = '%s'",endDate, companyName, userRole, startDate);
							esql.executeUpdate(query5); break;
						case 9: 
							keepon = false; break;
						default: 	
							System.out.println("Invalid choice. Please try again.");
					}
				}
			}
		}catch (Exception e){
			System.err.println(e.getMessage());
			}
		
	}

	//deletes all of the users work experience information

	// add some code to tell user that the touple they are trying
	// to delete does not exist
	public static void DeleteWorkExp(ProfNetwork esql, String currentUser){
		try{
			boolean loop = true;
		
			System.out.println("\tEnter the company name of the entry you wish to delete");
			String companyName = esql.in.readLine();
			System.out.println("\tEnter the role of the entry you wish to delete");
			String userRole = esql.in.readLine();
			System.out.println("\tEnter the start date of the entry you wish to delete");
			String startDate = esql.in.readLine();

			System.out.println("Press 1 if you are sure you want to delete this Work Experience");
			System.out.println("Press 2 if you want to stop deleting");
			int choice = esql.readChoice();


			while (loop){
				if (choice == 1){
					String query = String.format("DELETE FROM work_expr WHERE userId = '%s' AND company = '%s' AND role = '%s' AND start_date = '%s'", currentUser, companyName, userRole, startDate);
					esql.executeUpdate(query);
					loop = false;
				}
				
				else if (choice == 2){ // do nothing and just let the function exit
					loop = false;
				}
				else{
					System.out.println("Wrong entry. Please enter a 1 or 2.");
					choice = esql.readChoice();
				}
			}
		}catch (Exception e){
			System.err.println(e.getMessage());
			}

	}

	//allows the user to initially fill out their school related information
	public static void AddSchool(ProfNetwork esql, String currentUser){
		try{
			System.out.print("\tEnter new Institution Name: ");
         		String schoolName = esql.in.readLine();
			System.out.print("\tEnter new Major: ");
         		String userMajor = esql.in.readLine();
			System.out.print("\tEnter new Degree: ");
         		String userDegree = esql.in.readLine();
			System.out.print("\tEnter new Start Date: ");
         		String startDate = esql.in.readLine();
			System.out.print("\tEnter new End Date: ");
         		String endDate = esql.in.readLine();
			
			String query = String.format("INSERT INTO educational_details (userid, institution_name, major, degree, start_date, end_date) VALUES('%s', '%s', '%s', '%s', '%s', '%s')",currentUser, schoolName, userMajor, userDegree, startDate, endDate);
			esql.executeUpdate(query);
		}catch (Exception e){
			System.err.println(e.getMessage());
			}
	}
	
	//allows the user to edit school information after previously entering some information
	public static void UpdateSchool(ProfNetwork esql, String currentUser){
		String schoolName;
		String userMajor;
		String userDegree;
		String startDate;
		String endDate;
		try{
			String testQuery = String.format("SELECT institution_name, major FROM educational_details WHERE userid = '%s'",currentUser);
			List<List<String>> result  = new ArrayList<List<String>>();
			result = esql.executeQueryAndReturnResult(testQuery);
			if (result.isEmpty()){
				System.out.println("You need to go back to the previous menu and Add Education Details before you can update it.");
			}
			else{
				boolean keepon = true;
				System.out.println("\tEnter the major of the entry you wish to update");
				userMajor = esql.in.readLine();
				System.out.println("\tEnter the degree type of the entry you wish to update");
				userDegree = esql.in.readLine();

				while (keepon){
					System.out.println("UPDATE EDUCATION MENU");
					System.out.println("----------");
					System.out.println("1. Update Institution Name");
				//	System.out.println("2. Update Major");
				//	System.out.println("3. Update Degree");
					System.out.println("2. Update Start Date");
					System.out.println("3. Update End Date");
					System.out.println("9. Quit Updating Education Information");
	
					switch (esql.readChoice()){
						case 1: 
							System.out.print("\tEnter new Institution Name: ");
         						schoolName = esql.in.readLine();
							String query1 = String.format("UPDATE educational_details set institution_name = '%s' WHERE userid = '"+currentUser+"' AND major = '%s' AND degree = '%s'",schoolName, userMajor, userDegree);
							esql.executeUpdate(query1); break;
					/*	case 2: 
							System.out.print("\tEnter new major: ");
         						userMajor = esql.in.readLine();
							String query2 = String.format("UPDATE educational_details set major = '%s' WHERE userid = '"+currentUser+"'",userMajor);
							esql.executeUpdate(query2); break;
						case 3:
							System.out.print("\tEnter new degree: ");
         						userDegree = esql.in.readLine();
							String query3 = String.format("UPDATE educational_details set degree = '%s' WHERE userid = '"+currentUser+"'",userDegree);
							esql.executeUpdate(query3); break;*/
						case 2:
							System.out.print("\tEnter new start Date: ");
         						startDate = esql.in.readLine();
							String query4 = String.format("UPDATE educational_details set start_date = '%s' WHERE userid = '"+currentUser+"'AND major = '%s' AND degree = '%s'",startDate, userMajor, userDegree);
							esql.executeUpdate(query4); break;
						case 3:
							System.out.print("\tEnter new end Date: ");
         						endDate = esql.in.readLine();
							String query5 = String.format("UPDATE educational_details set end_date = '%s' WHERE userid = '"+currentUser+"'AND major = '%s' AND degree = '%s'",endDate, userMajor, userDegree);
							esql.executeUpdate(query5); break;
						case 9: 
							keepon = false; break;
						default: 	
							System.out.println("Invalid choice. Please try again.");
					}
				}
			}
		}catch (Exception e){
			System.err.println(e.getMessage());
			}
		
	}
	
	//Deletes all of the users school information
	public static void DeleteSchool(ProfNetwork esql, String currentUser){
		try{
			boolean loop = true;
			System.out.println("\tEnter the major of the entry you wish to delete");
			String userMajor = esql.in.readLine();
			System.out.println("\tEnter the degree of the entry you wish to delete");
			String userDegree = esql.in.readLine();

			System.out.println("Are you sure you want to delete your stored school information?");
			System.out.println("Press 1 if yes");
			System.out.println("Press 2 if no");
			int choice = esql.readChoice();
			while (loop){
				if (choice == 1){
					String query = 	String.format("DELETE FROM educational_details WHERE userid = '%s' AND major = '%s' AND degree = '%s'", currentUser, userMajor, userDegree);
					esql.executeUpdate(query);
					loop = false;
				}
				else if (choice == 2){ // do nothing and just let the function exit
					loop = false;
				}
				else{
					System.out.println("Wrong entry. Please enter a 1 or 2.");
					choice = esql.readChoice();
				}
			}
		}catch (Exception e){
			System.err.println(e.getMessage());
			}
	}
	
	// no limit how far but to send connection will only be available to valid users
	public static void ViewUserFriends(ProfNetwork esql, String usrName){
	}
	
	//allows the user to update their password after initially entering it when creating login
	public static void UpdatePassword(ProfNetwork esql, String currentUser){
<<<<<<< HEAD
=======
		String password;
		try{
			System.out.print("\tEnter new password: ");
         		password = esql.in.readLine();
			String query = String.format("UPDATE usr set password = '%s' WHERE userid = '"+currentUser+"'",password);
			esql.executeUpdate(query);
		}catch (Exception e){
			System.err.println(e.getMessage());
			}
	}
	
	//allows the user the chance to enter their full name so they are searchable by other people
	public static void UpdateFullName(ProfNetwork esql, String currentUser){
		String fullName;
		try{
			System.out.print("\tEnter new Full Name: ");
         		fullName = esql.in.readLine();
			String query = String.format("UPDATE usr set name = '%s' WHERE userid = '"+currentUser+"'",fullName);
			esql.executeUpdate(query);
		}catch (Exception e){
			System.err.println(e.getMessage());
			}
	
	}
	
	//allows the user to update their date of birth
	public static void UpdateDateofBirth(ProfNetwork esql,String currentUser){
		String dateofBirth;
		try{
			System.out.print("\tEnter new Date of Birth: ");
         		dateofBirth = esql.in.readLine();
			String query = String.format("UPDATE usr set date_of_birth = '%s' WHERE userid = '"+currentUser+"'",dateofBirth);
			esql.executeUpdate(query);
		}catch (Exception e){
			System.err.println(e.getMessage());
			}
	
	}
	
	//allows the user to update their email associated with their account
	public static void UpdateEmail(ProfNetwork esql, String currentUser){
		String email;
		try{
			System.out.print("\tEnter new email: ");
         		email = esql.in.readLine();
			String query = String.format("UPDATE usr set email = '%s' WHERE userid = '"+currentUser+"'",email);
			esql.executeUpdate(query);
		}catch (Exception e){
			System.err.println(e.getMessage());
			}
	
>>>>>>> 57968d3949fe1df7c3456510f9f760548b176e52
	}
} //end Profile
