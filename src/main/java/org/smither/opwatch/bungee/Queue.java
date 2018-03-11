package org.smither.opwatch.bungee;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
class Queue implements Serializable{
	private static final long serialVersionUID = 1L;
	private static Queue instance;
	private static final List<Pattern> rxs = new ArrayList<>();
	private int maxSize;
	private LinkedList<Change> signQueue;
	private LinkedList<Change> bookQueue;
	private LinkedList<Change> nameQueue;
	
	@SuppressWarnings("unchecked")
	static Queue getInstance(){
		if (instance==null){
			try {
				Opwatch.instance.getLogger().info(Opwatch.instance.getDataFolder().getPath());
		        File file = new File(Opwatch.instance.getDataFolder(), "Queue.data");
		        if (!file.exists()) {
		            try {
		            	file.createNewFile();
		            } catch (IOException e) {
		                throw new RuntimeException("Unable to create Queue file", e);
		            }
		        }
		         FileInputStream fileIn = new FileInputStream(file);
		         ObjectInputStream in = new ObjectInputStream(fileIn);
		         instance = (Queue) in.readObject();
		         in.close();
		         fileIn.close();
		      }catch(ClassNotFoundException|IOException e) {
		         instance=new Queue();
		      }
			// Fill in rxs 
			try {
				Configuration regexFile = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(Opwatch.instance.config.getString("RegexCheckPath")));
				List<String> rules=(List<String>) regexFile.getList("patterns");
				Opwatch.instance.getLogger().severe("size="+rules.size());
				for (String rule : rules){
					Opwatch.instance.getLogger().severe("new rule: "+rule);
					try {
						rxs.add(Pattern.compile(rule));
					}catch(PatternSyntaxException ex) {
						Opwatch.instance.sendIRC("OPWATCH ERROR! Regex Pattern load failed on regex \""+ex.getPattern()+"\"");
						Opwatch.instance.getLogger().log(Level.SEVERE,"OPWATCH ERROR! Regex Pattern load failed on regex \""+ex.getPattern()+"\"");
					}
				}
				Opwatch.instance.getLogger().info(rxs.size()+" patterns loaded");
			} catch (IOException e) {
				e.printStackTrace();
			}
			// completed rxs
		}
		return instance;
	}
	void save(){
		try {
	        FileOutputStream fileOut = new FileOutputStream(new File(Opwatch.instance.getDataFolder(), "Queue.data"));
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(instance);
			out.close();
			fileOut.close();
			}catch(IOException i) {
				i.printStackTrace();
			}
	}
	
	private Queue() {
		maxSize=Opwatch.instance.config.getInt("MaxQueueSize");
		signQueue=new LinkedList<Change>();
	}
	public void add(final SignChange sc) {  
		signQueue.addLast(sc);
		Runnable r = new Runnable() {
			@Override
			public void run() {
				String content=String.join(" ", sc.getContent()).toLowerCase();
				for (Pattern rx : rxs){
					if (rx.matcher(content).find()){
						Opwatch.instance.sendIRC(String.format("OPWATCH TRIGGERED!! - Sign %d: \"%s\" at %s %s %d,%d,%d placed by %s",
							sc.getID(),String.join(", ",sc.getContent()),
							sc.getServer(), sc.getWorld(), sc.getX(), sc.getY(), sc.getZ(), sc.getPlayer()));
						Opwatch.instance.getLogger().info("! ! ! SIGN TRIGGERED OPWATCH ! ! !");
						if (Opwatch.instance.config.getBoolean("AutoWipeSigns")) {
							while (Store.getInstance().pending.contains(sc.getID())) {
								try {
									Thread.sleep(500);
								} catch (InterruptedException e) {
								}
							}
							Opwatch.instance.wipeSign(sc.getID(),Opwatch.instance.wipeMsg);
						}
						break;
					}
				}
			}
		};
		r.run();
		if (signQueue.size()>maxSize){
			Opwatch.instance.sendIRC("Sign queue size is at "+signQueue.size()+". Please review all signs with the command viewSigns");
		}
	}
	public void listSigns(final CommandSender sender) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				while(!signQueue.isEmpty()){
					SignChange sp=(SignChange) signQueue.removeFirst();
					sender.sendMessage(new ComponentBuilder(
								String.format("%d: \"%s, %s, %s, %s\" at %s %s %d,%d,%d placed by %s%s",
								sp.getID(),sp.getContent()[0],sp.getContent()[1],sp.getContent()[2],sp.getContent()[3],
								sp.getServer(), sp.getWorld(), sp.getX(), sp.getY(), sp.getZ(), sp.getPlayer(), sp.isWiped()?", Has been Wiped":sp.attemptwipe()?", Wipe was attempted":"")
							).create());
				}
				sender.sendMessage(new ComponentBuilder("Signs all checked").create());
			}
		};
		r.run();
	}
	public void add(final BookChange bc) {  
		bookQueue.addLast(bc);
		Runnable r = new Runnable() {
			@Override
			public void run() {
				String content=String.join(" ", bc.getContent()).toLowerCase();
				for (Pattern rx : rxs){
					if (rx.matcher(content).find()){
						Opwatch.instance.sendIRC(String.format("OPWATCH TRIGGERED!! - Book %d: %s in %s %s:\n%s\nEOM",
							bc.getID(), bc.getPlayer(), bc.getServer(), bc.getWorld(), String.join("\n", bc.getContent())));
						Opwatch.instance.getLogger().info("! ! ! BOOK TRIGGERED OPWATCH ! ! !");
						break;
					}
				}
			}
		};
		r.run();
		if (bookQueue.size()>maxSize){
			Opwatch.instance.sendIRC("Book queue size is at "+bookQueue.size()+". Please review all books with the command viewBooks");
		}
	}
	public void listBooks(final CommandSender sender) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				while(!bookQueue.isEmpty()){
					BookChange bc=(BookChange) bookQueue.removeFirst();
					sender.sendMessage(new ComponentBuilder(
								String.format("%d: \"%s, %s, %s, %s\" at %s %s %d,%d,%d placed by %s%s",
								bc.getID(),String.join("\nEOP\n", bc.getContent()),
								bc.getServer(), bc.getWorld(), bc.getX(), bc.getY(), bc.getZ(), bc.getPlayer(), bc.isWiped()?", Has been Wiped":bc.attemptwipe()?", Wipe was attempted":"")
							).create());
				}
				sender.sendMessage(new ComponentBuilder("Signs all checked").create());
			}
		};
		r.run();
	}
}
