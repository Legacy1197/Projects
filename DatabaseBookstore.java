import java.sql.*;
import java.util.Calendar;
import java.util.Scanner;

public class Interface {
	public static void main(String args[]){
		Connection conn = null;
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:orcl", "book_admin", "passwordtest");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		Scanner in = new Scanner(System.in);
		String input;
		
		if(conn == null)
			System.out.println("Database could not be reached");
		else{
			boolean end = false;
			boolean invalidInput = false;
			do{
				boolean retry;
				do{
					
					retry = false;
					System.out.println("***************************************************************\n");
					System.out.println("\t    Welcome to the Online Book Store\n");
					System.out.println("***************************************************************\n");
					
					System.out.println("\t\t1. Member Login");
					System.out.println("\t\t2. New Member Registration");
					System.out.println("\t\t3. Quit");
					System.out.print("Please type in your option: ");
					input = in.nextLine();
					
					int inputNum = 0;
					try{
						inputNum = Integer.parseInt(input);
						if(inputNum > 3 || inputNum < 1){
							System.out.println("Please choose from the list.");
							retry = true;
						}
					}catch(NumberFormatException e){
						System.out.println("Please choose from the list.");
						retry = true;
					}
				}while(retry);
				if(input.equals("1")){
					
					boolean invalid;
					String username = "";
					do{
						invalid = false;
						
						String password;
						System.out.print("Please enter your username: ");
						username = in.nextLine();
						System.out.print("Please enter your password: ");
						password = in.nextLine();
						
						try {
							String sql = "SELECT 1 "
									+ "FROM members "
									+ "WHERE userid=?"
									+ "AND password=?";
							
							PreparedStatement ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
							ps.setString(1, username);
							ps.setString(2, password);
							ResultSet rs = ps.executeQuery();
							if(!rs.next()){
								invalid = true;
								System.out.println("Username/Password is incorrect");
							}
							
						} catch (SQLException e) {
							e.printStackTrace();
						}
					
					}while(invalid);
					
					boolean logout;
					do{
						logout = false;
						
						
						do{
							retry = false;
							System.out.println("***************************************************************\n");
							System.out.println("\t\t      Member Menu\n");
							System.out.println("***************************************************************\n");
							System.out.println("\t\t1. Browse by Subject");
							System.out.println("\t\t2. Search by Author/Title");
							System.out.println("\t\t3. View/Edit Shopping Cart");
							System.out.println("\t\t4. Check Order Status");
							System.out.println("\t\t5. Check Out");
							System.out.println("\t\t6. One Click Check Out");
							System.out.println("\t\t7. View/Edit Personal Information");
							System.out.println("\t\t8. Logout");
							System.out.print("Please type in your option: ");
							input = in.nextLine();
							int inputNum = 0;
							try{
								inputNum = Integer.parseInt(input);
								if(inputNum > 8 || inputNum < 1){
									System.out.println("Please choose from the list.");
									retry = true;
								}
							}catch(NumberFormatException e){
								System.out.println("Please choose from the list.");
								retry = true;
							}
						}while(retry);
						
						if(input.equals("1")){
							
							String subject = "";
							try {
								String sql = "SELECT DISTINCT subject "
										+ "FROM books";
								PreparedStatement ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
								ResultSet rs = ps.executeQuery();
								
								rs.last();
								int numRows = rs.getRow();
								int choice = 0;
								do{
									rs.beforeFirst();
									int count = 0;
									while(rs.next()){
										count++;
										System.out.println(count + ". " + rs.getString(1));
									}
									System.out.println("Please enter your choice");
									String ch = in.nextLine();
									try{
										choice = Integer.parseInt(ch);
									}catch(NumberFormatException e){
										System.out.println("Please choose from the list.");
									}
								}while(choice < 1 || choice > numRows);
								
								rs.absolute(choice);
								subject = rs.getString(1);
							} catch (SQLException e) {
								e.printStackTrace();
							}
							
							try {
								String sql = "SELECT * "
										+ "FROM books "
										+ "WHERE subject=?";
								PreparedStatement ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
								ps.setString(1, subject);
								ResultSet rs = ps.executeQuery();
								
								boolean finished = false;
								do{
									displayBooks(rs);
									System.out.print("Enter ISBN to add to Cart or"
											+ "\nENTER to go back to menu: ");
									String isbnchoice = in.nextLine();
									if(isbnchoice.isEmpty()){
										finished = true;
									}
									else{
										boolean found = false;
										rs.beforeFirst();
										while(!found && rs.next()){
											
											if(rs.getString("isbn").equals(isbnchoice)){
												found = true;
												int quantity = 0;
												do{
													invalid = false;
													System.out.print("Enter quantity: ");
													String qt = in.nextLine();
													try{
														quantity = Integer.parseInt(qt);
														if(quantity < 1 || quantity > 99999){
															System.out.println("Enter a valid quantity.");
															invalid = true;
														}
													}catch(NumberFormatException e){
														System.out.println("Enter a valid number.");
														invalid = true;
													}
												}while(invalid);
												
												
												sql = "SELECT qty "
														+ "FROM cart "
														+ "WHERE userid=? "
														+ "AND isbn=?";	
												ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
												ps.setString(1, username);
												ps.setString(2, isbnchoice);
												ResultSet check= ps.executeQuery();
												
												if(check.next()){
													
													sql = "UPDATE cart "
															+ "SET qty=?"
															+ "WHERE userid=?"
															+ "AND isbn=?";
													ps = conn.prepareStatement(sql);
													ps.setInt(1, (check.getInt(1)+quantity));
													ps.setString(2, username);
													ps.setString(3, isbnchoice);
												}
												else{
													sql = "INSERT INTO cart "
															+ "VALUES(?,?,?)";
													ps = conn.prepareStatement(sql);
													ps.setString(1, username);
													ps.setString(2, isbnchoice);
													ps.setInt(3, quantity);
												}
																										
												ps.executeUpdate();
											}
										}
										
									}
									
								}while(!finished);
																				
							} catch (SQLException e) {
								e.printStackTrace();
							}
							
						}
						else if(input.equals("2")){
							do{
								do{
									retry = false;
									System.out.println("\t\t1. Author Search");
									System.out.println("\t\t2. Title Search");
									System.out.println("\t\t3. Go back to Member Menu");
									System.out.print("Please type in your option: ");
									input = in.nextLine();
									int inputNum = 0;
									try{
										inputNum = Integer.parseInt(input);
										if(inputNum > 3 || inputNum < 1){
											System.out.println("Please choose from the list.");
											retry = true;
										}
									}catch(NumberFormatException e){
										System.out.println("Please choose from the list.");
										retry = true;
									}
								}while(retry);
								
								if(input.equals("1") || input.equals("2")){
								
									String sql = "";
									if(input.equals("1")){
										System.out.print("Enter part of Author: ");
										sql = "SELECT * "
												+ "FROM books "
												+ "WHERE author LIKE ?";
									}
									else if(input.equals("2")){
										System.out.print("Enter part of Title: ");
										sql = "SELECT * "
												+ "FROM books "
												+ "WHERE title LIKE ?";
									}
									String partial = in.nextLine();
									partial.toLowerCase();
									
									try {
										
										PreparedStatement ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
									    ps.setString(1, ("%" + partial + "%") );
										ResultSet rs = ps.executeQuery();
										
										if(!rs.next())
											System.out.println("No books found");
										else{
											boolean finished = false;
											do{
												displayBooks(rs);
												
												System.out.print("Enter ISBN to add to Cart or"
														+ "\nENTER to go back to menu: ");
												String isbnchoice = in.nextLine();
												if(isbnchoice.isEmpty()){
													finished = true;
												}
												else{
													boolean found = false;
													rs.beforeFirst();
													while(!found && rs.next()){
														if(rs.getString("isbn").equals(isbnchoice)){
															found = true;
															int quantity = 0;
															do{
																invalid = false;
																System.out.print("Enter quantity: ");
																String qt = in.nextLine();
																try{
																	quantity = Integer.parseInt(qt);
																	if(quantity < 1 || quantity > 99999){
																		System.out.println("Enter a valid quantity.");
																		invalid = true;
																	}
																}catch(NumberFormatException e){
																	System.out.println("Enter a valid number.");
																	invalid = true;
																}
															}while(invalid);
															
															sql = "SELECT qty "
																	+ "FROM cart "
																	+ "WHERE userid=? "
																	+ "AND isbn=?";	
															ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
															ps.setString(1, username);
															ps.setString(2, isbnchoice);
															ResultSet check= ps.executeQuery();
															
															if(check.next()){
																sql = "UPDATE cart "
																		+ "SET qty=?"
																		+ "WHERE userid=?"
																		+ "AND isbn=?";
																ps = conn.prepareStatement(sql);
																ps.setInt(1, (check.getInt(1)+quantity));
																ps.setString(2, username);
																ps.setString(3, isbnchoice);
															}
															else{
																sql = "INSERT INTO cart "
																		+ "VALUES(?,?,?)";
																ps = conn.prepareStatement(sql);
																ps.setString(1, username);
																ps.setString(2, isbnchoice);
																ps.setInt(3, quantity);
															}
																													
															ps.executeUpdate();
														}
													}
												}
											}while(!finished);
												
										}								
									} catch (SQLException e) {
										e.printStackTrace();
									}
								}
							}while(!input.equals("3"));	
						}
						else if(input.equals("3")){
							boolean finished = false;
							do{
								try{
									String sql = "SELECT * "
											+ "FROM cart c, books b "
											+ "WHERE c.userid=? "
											+ "AND c.isbn = b.isbn";
									PreparedStatement ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
									ps.setString(1, username);
									ResultSet rs = ps.executeQuery();
									
									if(!rs.next()){
										System.out.println("Cart is Empty");
										finished = true;
									}
									else{
										finished = false;
									
										displayCart(rs);
										
										System.out.print("Enter d to delete an item"
												+ "\ne to edit quantity"
												+ "\nor q to go back to menu:  ");
										String editChoice = in.nextLine();
										if(editChoice.equals("q")){
											finished = true;
										}
										else if(editChoice.equals("e") || editChoice.equals("d")){
											System.out.print("Please choose an isbn: ");
											String isbnchoice = in.nextLine();
											
											boolean found = false;
											rs.beforeFirst();
											while(!found && rs.next()){
												if(rs.getString("isbn").equals(isbnchoice)){
													found = true;
													
													if(editChoice.equals("e")){
														int quantity = 0;
														do{
															invalid = false;
															System.out.print("Enter new quantity: ");
															String qt = in.nextLine();
															try{
																quantity = Integer.parseInt(qt);
																if(quantity < 1 || quantity > 99999){
																	System.out.println("Enter a valid quantity.");
																	invalid = true;
																}
															}catch(NumberFormatException e){
																System.out.println("Enter a valid number.");
																invalid = true;
															}
														}while(invalid);
														
														
														sql = "UPDATE cart "
																	+ "SET qty=?"
																	+ "WHERE userid=?"
																	+ "AND isbn=?";
														ps = conn.prepareStatement(sql);
														ps.setInt(1, quantity);
														ps.setString(2, username);
														ps.setString(3, isbnchoice);
																												
														ps.executeUpdate();
													}
													else{
														sql = "DELETE FROM cart "
																+ "WHERE userid=?"
																+ "AND isbn=?";
														ps = conn.prepareStatement(sql);
														ps.setString(1, username);
														ps.setString(2, isbnchoice);
																												
														ps.executeUpdate();
													}
												}
											}
											if(!found){
												System.out.println("Please choose a valid ISBN.");
											}
										}
										else{
											System.out.println("Please select edit/delete/quit.");
										}
									
									}
									
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}while(!finished);
														
						}
						else if(input.equals("4")){
							boolean finished = false;
							do{
								try{
									String sql = "SELECT ono, received, shipped, fname, lname "
											+ "FROM orders o, members m "
											+ "WHERE o.userid=? "
											+ "AND o.userid=m.userid "
											+ "ORDER BY ono";
									PreparedStatement ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
									ps.setString(1, username);
									ResultSet rs = ps.executeQuery();
									
									if(!rs.next()){
										System.out.println("No Orders.");
										in.nextLine();
									}
									else{
										displayOrders(rs);
										
										System.out.print("Enter the Order No. to display its details or"
												+ " q to quit: ");
										String orderChoice = in.nextLine();
										if(orderChoice.equals("q")){
											finished = true;
										}
										else{											
											boolean found = false;
											rs.beforeFirst();
											while(!found && rs.next()){
												if(rs.getString("ono").equals(orderChoice)){
													found = true;
		
													sql = "SELECT * "
														+ "FROM books b, orders o, odetails od "
														+ "WHERE o.ono=? "
														+ "AND od.ono = o.ono "
														+ "AND od.isbn = b.isbn ";
													
													ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
													ps.setString(1, orderChoice);
													
													ResultSet orderdetails = ps.executeQuery();
													
													sql = "SELECT * "
														+ "FROM members "
														+ "WHERE userid=? ";
													ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
													ps.setString(1, username);													
													ResultSet memberdetails = ps.executeQuery();
													
													displayDetails(orderdetails, memberdetails);
													System.out.println("Press Enter to go back to Menu.");
													in.nextLine(); //Wait for enter
												}
											}
											if(!found){
												System.out.println("Please choose a valid Order No.");
											}
										}									
									}
									
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}while(!finished);
						}
						else if(input.equals("5")){
							try {
								String sql = "SELECT * "
										+ "FROM cart c, books b "
										+ "WHERE c.userid=? "
										+ "AND c.isbn = b.isbn";
								PreparedStatement ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
								ps.setString(1, username);
								ResultSet cartrs = ps.executeQuery();
								
								if(!cartrs.next())
									System.out.println("Nothing is in the cart to check out.");
								else{
									displayCart(cartrs);
									
									System.out.println("Proceed to checkout? (y/n): ");
									String cont = in.nextLine();
									if(cont.equals("Y") || cont.equals("y")){
										
										sql = "SELECT * "
												+ "FROM members "
												+ "WHERE userid=? ";
										ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
										ps.setString(1, username);													
										ResultSet memberdetails = ps.executeQuery();
										
										String fname, lname, street, city, state;
										int zip;
										System.out.println("Do you want to enter a new shipping address? (y/n): ");
										String newaddress = in.nextLine();
										if(newaddress.equals("Y") || newaddress.equals("y")){
											
											System.out.print("Enter First Name: ");
											fname = in.nextLine();
											System.out.print("Enter Last Name: ");
											lname = in.nextLine();
											System.out.print("Enter Street Address: ");
											street = in.nextLine();
											System.out.print("Enter City: ");
											city = in.nextLine();
											System.out.print("Enter State: ");
											state = in.nextLine();
											
											boolean incorrect;
											zip = 0;
											do{
												incorrect = false;
												System.out.print("Enter Zip Code: ");
												String z = in.nextLine();
												try{
													zip = Integer.parseInt(z);
												}catch(NumberFormatException e){
													incorrect = true;
													System.out.println("Invalid zip code");
												}
												if(zip > 99999 || zip < 0){
													incorrect = true;
													System.out.println("Invalid zip code");
												}
											}while(incorrect);	
										}
										else{
											memberdetails.next();
											fname = memberdetails.getString("fname");
											lname = memberdetails.getString("lname");
											street = memberdetails.getString("address");
											city = memberdetails.getString("city");
											state = memberdetails.getString("state");
											zip = memberdetails.getInt("zip");
										}
										
										System.out.print("Do you want to enter new credit card information? (y/n): ");
										String choice = in.nextLine();
										
										if(choice.equals("y") || choice.equals("Y")){
											String cardType = "";
											String cardNum = "";
											do{
												invalid = false;
												System.out.print("Enter Credit Card type: ");
												cardType = in.nextLine();
												if(!cardType.equals("amex") && !cardType.equals("discover") && !cardType.equals("mc") && !cardType.equals("visa")){
													invalid = true;
													System.out.println("Must be amex/discover/mc/visa.");
												}
											}while(invalid);
											do{
												invalid = false;
												System.out.print("Enter Credit Card number: ");
												cardNum = in.nextLine();
												try{
													Long.parseLong(cardNum);
													if(cardNum.length() != 16){
														invalid = true;
														System.out.println("Invalid card number");
													}
												}catch(NumberFormatException e){
													invalid = true;
													System.out.println("Invalid card number");
												}
												
											}while(invalid);
											
											sql = "UPDATE members "
													+ "SET creditcardtype=?, creditcardnumber=? "
													+ "WHERE userid=?";
											ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
											ps.setString(1, cardType);
											ps.setString(2, cardNum);
											ps.setString(3, username);
											ps.executeUpdate();
										}
										
										
										sql = "SELECT ono "
												+ "FROM orders "
												+ "ORDER BY ono ";
										ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
										ResultSet rs = ps.executeQuery();
										rs.last();
										int orderNum = rs.getInt("ono") + 1;
										
										java.sql.Date today = new java.sql.Date(Calendar.getInstance().getTime().getTime());
										
										sql = "INSERT INTO orders "
												+ "VALUES(?,?,?,?,?,?,?,?,?,?)";
										ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
										ps.setString(1, username);
										ps.setInt(2, orderNum);
										ps.setDate(3, today);
										ps.setDate(4, today);
										ps.setString(5, fname);
										ps.setString(6, lname);
										ps.setString(7, street);
										ps.setString(8, city);
										ps.setString(9, state);
										ps.setInt(10, zip);
										
										ps.executeUpdate();
											
										cartrs.beforeFirst();
										while(cartrs.next()){
											sql = "INSERT INTO odetails "
													+ "VALUES(?,?,?,?)";
											ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
											ps.setInt(1, orderNum);
											ps.setString(2, cartrs.getString("isbn"));
											ps.setInt(3, cartrs.getInt("qty"));
											ps.setInt(4, cartrs.getInt("price"));
											
											ps.executeUpdate();
										}
										
										sql = "DELETE FROM cart "
												+ "WHERE userid=?";
										ps = conn.prepareStatement(sql);
										ps.setString(1, username);
																								
										ps.executeUpdate();
										
										sql = "SELECT * "
												+ "FROM books b, orders o, odetails od "
												+ "WHERE o.ono=? "
												+ "AND od.ono = o.ono "
												+ "AND od.isbn = b.isbn ";
											
										ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
										ps.setInt(1, orderNum);
										
										ResultSet orderdetails = ps.executeQuery();
										
										displayDetails(orderdetails, memberdetails);
										System.out.println("Press Enter to go back to Menu.");
										in.nextLine(); //Wait for enter
										
									}
						
								}
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}
						else if(input.equals("6")){
							try{
								String sql = "SELECT * "
										+ "FROM cart c, books b "
										+ "WHERE c.userid=? "
										+ "AND c.isbn = b.isbn";
								PreparedStatement ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
								ps.setString(1, username);
								ResultSet cartrs = ps.executeQuery();
								
								if(!cartrs.next())
									System.out.println("Nothing is in the cart to check out.");
								else{
								
									sql = "SELECT * "
											+ "FROM members "
											+ "WHERE userid=? ";
									ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
									ps.setString(1, username);													
									ResultSet memberdetails = ps.executeQuery();
									memberdetails.first();
																	
									sql = "SELECT ono "
											+ "FROM orders "
											+ "ORDER BY ono ";
									ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
									ResultSet rs = ps.executeQuery();
									rs.last();
									int orderNum = rs.getInt("ono") + 1;
									
									java.sql.Date today = new java.sql.Date(Calendar.getInstance().getTime().getTime());
									
									sql = "INSERT INTO orders "
											+ "VALUES(?,?,?,?,?,?,?,?,?,?)";
									ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
									ps.setString(1, username);
									ps.setInt(2, orderNum);
									ps.setDate(3, today);
									ps.setDate(4, today);
									ps.setString(5, memberdetails.getString("fname"));
									ps.setString(6, memberdetails.getString("lname"));
									ps.setString(7, memberdetails.getString("address"));
									ps.setString(8, memberdetails.getString("city"));
									ps.setString(9, memberdetails.getString("state"));
									ps.setInt(10, memberdetails.getInt("zip"));
									
									ps.executeUpdate();
										
									
									cartrs.beforeFirst();
									while(cartrs.next()){
										sql = "INSERT INTO odetails "
												+ "VALUES(?,?,?,?)";
										ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
										ps.setInt(1, orderNum);
										ps.setString(2, cartrs.getString("isbn"));
										ps.setInt(3, cartrs.getInt("qty"));
										ps.setInt(4, cartrs.getInt("price"));
										
										ps.executeUpdate();
									}
									
									sql = "DELETE FROM cart "
											+ "WHERE userid=?";
									ps = conn.prepareStatement(sql);
									ps.setString(1, username);
																							
									ps.executeUpdate();
									
									sql = "SELECT * "
											+ "FROM books b, orders o, odetails od "
											+ "WHERE o.ono=? "
											+ "AND od.ono = o.ono "
											+ "AND od.isbn = b.isbn ";
										
									ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
									ps.setInt(1, orderNum);
									
									ResultSet orderdetails = ps.executeQuery();
									
									displayDetails(orderdetails, memberdetails);
									System.out.println("Press Enter to go back to Menu.");
									in.nextLine(); //Wait for enter
								
								}
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}
						else if(input.equals("7")){
							try{
								String sql = "SELECT * FROM members WHERE userid=?";
								PreparedStatement ps = conn.prepareStatement(sql);
								ps.setString(1, username);
								ResultSet rs = ps.executeQuery();
								rs.next();
								
								System.out.println("Name: " + rs.getString("fname") + " " + rs.getString("lname"));
								System.out.println("Address: " + rs.getString("address"));
								System.out.println("City/State/Zip: " + rs.getString("city") + ", " + rs.getString("state") + " " + rs.getInt("zip"));
								System.out.println("Phone: " + rs.getString("phone"));
								System.out.println("Email: " + rs.getString("email"));
								System.out.println("Credit Card Type: " + rs.getString("creditcardtype"));
								System.out.println("Credit Card Number: " + rs.getString("creditcardnumber"));
								
								System.out.println("Would you like to update? (y/n): ");
								String choice = in.nextLine();
								if(choice.equals("Y") || choice.equals("y")){
									System.out.print("Enter First Name: ");
									String fname = in.nextLine();
									System.out.print("Enter Last Name: ");
									String lname = in.nextLine();
									System.out.print("Enter Street Address: ");
									String street = in.nextLine();
									System.out.print("Enter City: ");
									String city = in.nextLine();
									System.out.print("Enter State: ");
									String state = in.nextLine();
									boolean incorrect;
									int zip = 0;
									do{
										incorrect = false;
										System.out.print("Enter Zip Code: ");
										String z = in.nextLine();
										try{
											zip = Integer.parseInt(z);
										}catch(NumberFormatException e){
											incorrect = true;
											System.out.println("Invalid zip code");
										}
										if(zip > 99999 || zip < 0){
											incorrect = true;
											System.out.println("Invalid zip code");
										}
									}while(incorrect);
									
									System.out.print("Enter Phone Number: ");
									String phone = in.nextLine();
									System.out.print("Enter Email Address: ");
									String email = in.nextLine();
									
									System.out.print("Do you want to store credit card information? (y/n): ");
									choice = in.nextLine();
									String cardType = rs.getString("creditcardtype");
									String cardNum = rs.getString("creditcardnumber");
									if(choice.equals("y") || choice.equals("Y")){
										
										do{
											invalid = false;
											System.out.print("Enter Credit Card type: ");
											cardType = in.nextLine();
											if(!cardType.equals("amex") && !cardType.equals("discover") && !cardType.equals("mc") && !cardType.equals("visa")){
												invalid = true;
												System.out.println("Must be amex/discover/mc/visa.");
											}
										}while(invalid);
										do{
											invalid = false;
											System.out.print("Enter Credit Card number: ");
											cardNum = in.nextLine();
											try{
												Long.parseLong(cardNum);
												if(cardNum.length() != 16){
													invalid = true;
													System.out.println("Invalid card number");
												}
											}catch(NumberFormatException e){
												invalid = true;
												System.out.println("Invalid card number");
											}
											
										}while(invalid);
									}
									
									sql = "UPDATE members "
											+ "SET fname=?, lname=?, address=?, city=?, state=?, zip=?, phone=?, email=?, creditcardtype=?, creditcardnumber=? "
											+ "WHERE userid=?";
									ps = conn.prepareStatement(sql);
									ps.setString(1, fname);
									ps.setString(2, lname);
									ps.setString(3, street);
									ps.setString(4, city);
									ps.setString(5, state);
									ps.setInt(6, zip);
									ps.setString(7, phone);
									ps.setString(8, email);
									ps.setString(9, cardType);
									ps.setString(10, cardNum);
									ps.setString(11, username);
									
									ps.executeUpdate();
									
									System.out.println("\nInformation was updated.");
									
								}
							}catch(SQLException e){
								e.printStackTrace();
							}
						}
						else{
							logout = true;
						}
						
					}while(!logout);
				}
				else if(input.equals("2")){
					System.out.println("***************************************************************\n");
					System.out.println("\t\tNew Member Registration\n");
					System.out.println("***************************************************************\n");
					System.out.print("Enter First Name: ");
					String fname = in.nextLine();
					System.out.print("Enter Last Name: ");
					String lname = in.nextLine();
					System.out.print("Enter Street Address: ");
					String street = in.nextLine();
					System.out.print("Enter City: ");
					String city = in.nextLine();
					System.out.print("Enter State: ");
					String state = in.nextLine();
					boolean invalid;
					int zip = 0;
					do{
						invalid = false;
						System.out.print("Enter Zip Code: ");
						String z = in.nextLine();
						try{
							zip = Integer.parseInt(z);
						}catch(NumberFormatException e){
							invalid = true;
							System.out.println("Invalid zip code");
						}
						if(zip > 99999 || zip < 0){
							invalid = true;
							System.out.println("Invalid zip code");
						}
					}while(invalid);
					
					System.out.print("Enter Phone Number: ");
					String phone = in.nextLine();
					System.out.print("Enter Email Address: ");
					String email = in.nextLine();
					
					boolean taken;
					String userid = "";
					do{
						taken = false;
								
						System.out.print("Enter UserID: ");
						userid = in.nextLine();
						try {
							
							String sql = "SELECT 1 "
									+ "FROM members "
									+ "WHERE userid=?";
							PreparedStatement ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
							ps.setString(1, userid);
							ResultSet rs = ps.executeQuery();
							
							if(rs.next()){
								taken = true;
								System.out.println("That UserID is already taken.");
							}
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}while(taken);
					
					System.out.print("Enter password: ");
					String password = in.nextLine();
					
					System.out.print("Do you want to store credit card information? (y/n): ");
					String choice = in.nextLine();
					String cardType = "";
					String cardNum = "";
					if(choice.equals("y") || choice.equals("Y")){
						
						do{
							invalid = false;
							System.out.print("Enter Credit Card type: ");
							cardType = in.nextLine();
							if(!cardType.equals("amex") && !cardType.equals("discover") && !cardType.equals("mc") && !cardType.equals("visa")){
								invalid = true;
								System.out.println("Must be amex/discover/mc/visa.");
							}
						}while(invalid);
						do{
							invalid = false;
							System.out.print("Enter Credit Card number: ");
							cardNum = in.nextLine();
							try{
								Long.parseLong(cardNum);
								if(cardNum.length() != 16){
									invalid = true;
									System.out.println("Invalid card number");
								}
							}catch(NumberFormatException e){
								invalid = true;
								System.out.println("Invalid card number");
							}
							
						}while(invalid);
					}
					
					try{
						String sql = "INSERT INTO members "
								+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
						PreparedStatement ps = conn.prepareStatement(sql);
						ps.setString(1, fname);
						ps.setString(2, lname);
						ps.setString(3, street);
						ps.setString(4, city);
						ps.setString(5, state);
						ps.setInt(6, zip);
						ps.setString(7, phone);
						ps.setString(8, email);
						ps.setString(9, userid);
						ps.setString(10, password);
						ps.setString(11, cardType);
						ps.setString(12, cardNum);
						
						ps.executeUpdate();
					}catch(SQLException e){
						e.printStackTrace();
					}
					
					System.out.println("You have registered successfully.");
					System.out.println("Name: " + fname + " " + lname);
					System.out.println("Address: " + street);
					System.out.println("City/State/Zip: " + city + " " + state + " " + zip);
					System.out.println("Phone: " + phone);
					System.out.println("Email: " + email);
					System.out.println("UserID: " + userid);
					System.out.println("Password: " + password);
					System.out.println("Credit Card Type: " + cardType);
					System.out.println("Credit Card Number: " + cardNum);
					
				}
				else if(input.equals("3"))
					end = true;
				else{
					System.out.println("Please choose from the list");
					invalidInput = true;
				}
			
			}while(invalidInput || !end);
		}
		
		
		in.close();
	}
	
	public static void displayBooks(ResultSet results){
		try {
			results.last();
			System.out.println(results.getRow() + " Results found.\n");
			results.beforeFirst();
			
			while(results.next()){
				System.out.println("Author: " + results.getString("author"));
				System.out.println("Title: " + results.getString("title"));
				System.out.println("ISBN: " + results.getString("isbn"));
				System.out.println("Subject: " + results.getString("subject"));
				System.out.println("Price: " + results.getString("price"));
				System.out.println();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static void displayCart(ResultSet results){
		try {
			System.out.println("\n\nCurrent Cart Contents:");
			System.out.printf("%-11s%-40s%-6s%-5s%-5s\n", "ISBN", "Title", "$", "Qty", "Total");
			System.out.println("------------------------------------------------------------------------");
			
			double runningTotal = 0;
			results.beforeFirst();
			while(results.next()){
				double total = results.getDouble("price") * results.getDouble("qty");
				System.out.printf("%-11s%-40s%-6s%-5s%-5.2f\n", results.getString("isbn"),
						results.getString("title"), results.getString("price"), results.getString("qty"),
						total);
				runningTotal += total;
			}
			System.out.println("------------------------------------------------------------------------");
			System.out.printf("%-11s%52s%.2f\n\n", "Total = ", "$", runningTotal);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static void displayOrders(ResultSet results){
		try {
			System.out.printf("\n\nOrders Placed by %s %s\n", results.getString("fname"), results.getString("lname"));
			System.out.println("------------------------------------------------------------------------");
			System.out.printf("%-15s%-15s%-15s\n", "Order No.", "Received Date", "Shipped Date");
			System.out.println("------------------------------------------------------------------------");
			
			results.beforeFirst();
			while(results.next()){
				System.out.printf("%-15s%-15s%-15s\n", results.getString("ono"), results.getString("received").substring(0, 10), results.getString("shipped").substring(0, 10));
				
			}
			System.out.println("------------------------------------------------------------------------");
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
	public static void displayDetails(ResultSet oresults, ResultSet mresults){
		try {
			oresults.first();
			mresults.first();
			System.out.printf("\n%30s", "Order No. " + oresults.getString("ono"));
			System.out.printf("\n%-50s%s\n", "Shipping Address:", "Billing Address:");
			System.out.printf("Name: %-44s%s\n", (oresults.getString("shipFname") + " " + oresults.getString("shipLname")),
					("Name: " + mresults.getString("fname") + " " + mresults.getString("lname")));
			System.out.printf("Address: %-41s%s\n", oresults.getString("shipAddress"),
					("Address: " + mresults.getString("address")) );
			System.out.printf("%-50s%s\n", oresults.getString("shipCity") + ", " + oresults.getString("shipState") + " " + oresults.getString("shipZip"),
					mresults.getString("city") + ", " + mresults.getString("state") + " " + mresults.getString("zip"));
			
			
			System.out.printf("\n\n%-11s%-40s%-6s%-5s%-5s\n", "ISBN", "Title", "$", "Qty", "Total");
			System.out.println("------------------------------------------------------------------------");
			
			double runningTotal = 0;
			oresults.beforeFirst();
			while(oresults.next()){
				double total = oresults.getDouble("price") * oresults.getDouble("qty");
				System.out.printf("%-11s%-40s%-6s%-5s%-5.2f\n", oresults.getString("isbn"),
						oresults.getString("title"), oresults.getString("price"), oresults.getString("qty"),
						total);
				runningTotal += total;
			}
			System.out.println("------------------------------------------------------------------------");
			System.out.printf("%-11s%52s%.2f\n\n", "Total = ", "$", runningTotal);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
