package org.smither.opwatch.bungee;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class Store{
	private Connection connection;
	private String host, database, username, password;
	Set<Integer> pending=new HashSet<Integer>();
	private int port;
	private static Store instance;
	private int nextSignID;
	private int nextBookID;
	private int nextTagID;
	private Statement statement;
	static Store getInstance(){
		if (instance==null){
			instance = new Store();
			ResultSet result;
			instance.host = Opwatch.instance.config.getString("databaseHost");
			instance.port = Opwatch.instance.config.getInt("databasePort");
			instance.database = Opwatch.instance.config.getString("databaseName");
			instance.username = Opwatch.instance.config.getString("databaseUser");
			instance.password = Opwatch.instance.config.getString("databasePass");
			try {    
				instance.openConnection();
				instance.statement = instance.connection.createStatement();
				Opwatch.instance.getLogger().log(Level.INFO, "Connection is valid? "+instance.connection.isValid(1));
				boolean schemaOK = false;
				schemaOK=true;
				Opwatch.instance.getLogger().log(Level.INFO, "schema exists");
				result=instance.connection.getMetaData().getTables(null , instance.database, "signs", null);
				if (result.first()){
					String[] columns = {"id","line0","line1","line2","line3","server","world","x","y","z","player","wiped","attemptWipe"};
					for ( String column : columns){
						result = instance.connection.getMetaData().getColumns(null, instance.database, "signs", column);
						if(!result.next()){
							schemaOK=false;
							Opwatch.instance.getLogger().log(Level.SEVERE, "Tables NOT correct");
							Opwatch.instance.getLogger().severe("Table signs does not have column "+column+". Possible corruption or duplicate database.");
							continue;
						}
						result.close();
					}
				} else {
					schemaOK=false;
				}
				result=instance.connection.getMetaData().getTables(null , instance.database, "books", null);
				if (result.first()){
					String[] columns = {"id","content","server","world","x","y","z","player","wiped","attemptWipe"};
					for ( String column : columns){
						result = instance.connection.getMetaData().getColumns(null, instance.database, "books", column);
						if(!result.next()){
							schemaOK=false;
							Opwatch.instance.getLogger().log(Level.SEVERE, "Tables NOT correct");
							Opwatch.instance.getLogger().severe("Table books does not have column "+column+". Possible corruption or duplicate database.");
							continue;
						}
						result.close();
					}
				}
				Opwatch.instance.getLogger().log(Level.INFO, "Tables correct");
				result.close();
				if (!schemaOK){
					Opwatch.instance.getLogger().log(Level.SEVERE, "Database not correct. Please create database \""+instance.database+"\" or give full access to user \""+instance.username+"\" or ensure tables are correct.");
				} else {
					Opwatch.instance.getLogger().log(Level.INFO, "Database found and is correct");
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			instance.nextSignID=0;
			try {
				result = instance.statement.executeQuery("SELECT MAX(id) FROM `signs`");
				result.next();
				instance.nextSignID=result.getInt(1)+1;
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			instance.nextBookID=0;
			try {
				result = instance.statement.executeQuery("SELECT MAX(id) FROM `books`");
				result.next();
				instance.nextBookID=result.getInt(1)+1;
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			instance.nextTagID=0;
			try {
				result = instance.statement.executeQuery("SELECT MAX(id) FROM `tags`");
				result.next();
				instance.nextTagID=result.getInt(1)+1;
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		return instance;
	}
	private Store() {
		nextSignID=0;
	}
	public void openConnection() throws SQLException, ClassNotFoundException {
		if (connection != null && !connection.isClosed()) {
			return;
		}

		synchronized (this) {
			if (connection != null && !connection.isClosed()) {
				return;
			}
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://" + this.host+ ":" + this.port + "/" + this.database, this.username, this.password);
		}
	}
	public void add(final SignChange sp) {
		sp.setID(nextSignID++);
		pending.add(sp.getID());
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					if (!connection.isValid(0)){
						try {
							openConnection();
						} catch (ClassNotFoundException e) {
							System.err.println("Failed to reopen connection");
							e.printStackTrace();
						}
					}
					PreparedStatement stmt = connection.prepareStatement("INSERT INTO signs (id, line0, line1, line2, line3, server, world, x, y, z, player, wiped, attemptWipe) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
					stmt.setInt(1, sp.getID());
					stmt.setString(2, sp.getContent()[0]);
					stmt.setString(3, sp.getContent()[1]);
					stmt.setString(4, sp.getContent()[2]);
					stmt.setString(5, sp.getContent()[3]);
					stmt.setString(6, sp.getServer());
					stmt.setString(7, sp.getWorld());
					stmt.setInt(8, sp.getX());
					stmt.setInt(9, sp.getY());
					stmt.setInt(10, sp.getZ());
					stmt.setString(11, sp.getPlayer());
					stmt.setBoolean(12, sp.isWiped());
					stmt.setBoolean(13, sp.attemptwipe());
					stmt.executeUpdate();
					stmt.closeOnCompletion();
					pending.remove(sp.getID());
				} catch (SQLException e) {
					Opwatch.instance.getLogger().severe(e.getMessage());
					Opwatch.instance.getLogger().severe(e.getSQLState());
				}
			}
		};		
		r.run();
		return;
	}
	public SignChange get(int id) {
		ResultSet result;
		try {
			if (!connection.isValid(0)){
				try {
					openConnection();
				} catch (ClassNotFoundException e) {
					System.err.println("Failed to reopen connection");
					e.printStackTrace();
				}
			}
			result = statement.executeQuery("SELECT * FROM signs WHERE id = "+id+";");
			while (result.next()) {
				String[] content={result.getString("line0"),result.getString("line1"),result.getString("line2"),result.getString("line3")};
				SignChange signPlace=new SignChange(result.getInt("id"), content, result.getString("server"),result.getString("world"), result.getInt("x"), result.getInt("y"), result.getInt("z"), result.getString("player"), result.getBoolean("wiped"), result.getBoolean("attemptWipe"));
				return signPlace;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				statement.closeOnCompletion();
				statement = connection.createStatement();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	void save(){
	}
	public void listSigns(final CommandSender sender, final int amount) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				for (int i=nextSignID-1 ; i>=nextSignID-amount ; i-- ){
					SignChange sp=get(i);
					if (sp == null){
						break;
					}
					sender.sendMessage(new ComponentBuilder(
							String.format("%d: \"%s, %s, %s, %s\" at %s %s %d,%d,%d placed by %s%s",
									sp.getID(),sp.getContent()[0],sp.getContent()[1],sp.getContent()[2],sp.getContent()[3],
									sp.getServer(), sp.getWorld(), sp.getX(), sp.getY(), sp.getZ(), sp.getPlayer(), sp.isWiped()?", Has been Wiped":sp.attemptwipe()?", Wipe was attempted":"")
							).create());
				}
			}
		};
		r.run();
	}
	public void setAttemptWipe(final int id, final boolean newValue){
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					if (!connection.isValid(0)){
						try {
							openConnection();
						} catch (ClassNotFoundException e) {
							System.err.println("Failed to reopen connection");
							e.printStackTrace();
						}
					}
					PreparedStatement stmt = connection.prepareStatement("UPDATE `signs` SET `attemptWipe` = ? WHERE `id` = ?");
					stmt.setBoolean(1, newValue);
					stmt.setInt(2, id);
					stmt.executeUpdate();
					stmt.closeOnCompletion();
				} catch (SQLException e) {
					Opwatch.instance.getLogger().severe(e.getMessage());
					Opwatch.instance.getLogger().severe(e.getSQLState());
				}
			}
		};
		r.run();
		return;
	}
	public void setWiped(final int id, final boolean newValue){
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					if (!connection.isValid(0)){
						try {
							openConnection();
						} catch (ClassNotFoundException e) {
							System.err.println("Failed to reopen connection");
							e.printStackTrace();
						}
					}
					PreparedStatement stmt = connection.prepareStatement("UPDATE `signs` SET `wiped` = ? WHERE `id` = ?");
					stmt.setBoolean(1, newValue);
					stmt.setInt(2, id);
					stmt.executeUpdate();
					stmt.closeOnCompletion();
				} catch (SQLException e) {
					Opwatch.instance.getLogger().severe(e.getMessage());
					Opwatch.instance.getLogger().severe(e.getSQLState());
				}
			}
		};
		r.run();
		return;
	}

	public void add(final BookChange bc) {
		bc.setID(nextSignID++);
		pending.add(bc.getID());
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					if (!connection.isValid(0)){
						try {
							openConnection();
						} catch (ClassNotFoundException e) {
							System.err.println("Failed to reopen connection");
							e.printStackTrace();
						}
					}
					PreparedStatement stmt = connection.prepareStatement("INSERT INTO signs (id, line0, line1, line2, line3, server, world, x, y, z, player, wiped, attemptWipe) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
					stmt.setInt(1, bc.getID());
					stmt.setString(2, bc.getContent()[0]);
					stmt.setString(3, bc.getContent()[1]);
					stmt.setString(4, bc.getContent()[2]);
					stmt.setString(5, bc.getContent()[3]);
					stmt.setString(6, bc.getServer());
					stmt.setString(7, bc.getWorld());
					stmt.setInt(8, bc.getX());
					stmt.setInt(9, bc.getY());
					stmt.setInt(10, bc.getZ());
					stmt.setString(11, bc.getPlayer());
					stmt.setBoolean(12, bc.isWiped());
					stmt.setBoolean(13, bc.attemptwipe());
					stmt.executeUpdate();
					stmt.closeOnCompletion();
					pending.remove(bc.getID());
				} catch (SQLException e) {
					Opwatch.instance.getLogger().severe(e.getMessage());
					Opwatch.instance.getLogger().severe(e.getSQLState());
				}
			}
		};
		r.run();
		return;
	}
}
