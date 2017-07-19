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
import java.util.Map;
import java.util.regex.Pattern;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
class SignQueue implements Serializable{
	private static final long serialVersionUID = 1L;
	private static SignQueue instance;
	private static final List<Pattern> rxs = new ArrayList<>();
	private int maxSize;
	private LinkedList<SignPlace> queue;
	
	static SignQueue getInstance(){
		if (instance==null){
			try {
				Opwatch.instance.getLogger().info(Opwatch.instance.getDataFolder().getPath());
		        File file = new File(Opwatch.instance.getDataFolder(), "SignQueue.data");
		        if (!file.exists()) {
		            try {
		            	file.createNewFile();
		            } catch (IOException e) {
		                throw new RuntimeException("Unable to create SignQueue file", e);
		            }
		        }
		         FileInputStream fileIn = new FileInputStream(file);
		         ObjectInputStream in = new ObjectInputStream(fileIn);
		         instance = (SignQueue) in.readObject();
		         in.close();
		         fileIn.close();
		      }catch(IOException i) {
		         i.printStackTrace();
		         instance=new SignQueue();
		      }catch(ClassNotFoundException c) {
		         c.printStackTrace();
		         instance=new SignQueue();
		      }
			// Fill in rxs 
			try {
				Configuration regexFile = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(Opwatch.instance.config.getString("RegexCheckPath")));
				List<Map<String,String>> rules=(List<Map<String, String>>) regexFile.getList("rules");
				for (Map<String,String> rule : rules){
					rxs.add(Pattern.compile(rule.get("pattern")));
					Opwatch.instance.getLogger().info(Integer.toString(rxs.size())+" patterns loaded");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			// completed rxs
		}
		return instance;
	}
	void save(){
		try {
	        FileOutputStream fileOut = new FileOutputStream(new File(Opwatch.instance.getDataFolder(), "SignQueue.data"));
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(instance);
			out.close();
			fileOut.close();
			}catch(IOException i) {
				i.printStackTrace();
			}
	}
	
	private SignQueue() {
		maxSize=Opwatch.instance.config.getInt("MaxQueueSize");
		queue=new LinkedList<SignPlace>();
	}
	public void add(SignPlace sp) {  
		queue.addLast(sp);
		String content=String.join(" ", sp.getContent()).toLowerCase();
		Opwatch.instance.getLogger().info("SIGN: "+String.join("", sp.getContent()));
		for (Pattern rx : rxs){
			if (rx.matcher(content).find()){
				Opwatch.instance.getLogger().info("true");
				Opwatch.instance.sendIRC(String.format("OPWATCH TRIGGERED!! %d: \"%s, %s, %s, %s\" at %s %s %d,%d,%d placed by %s",
					sp.getID(),sp.getContent()[0],sp.getContent()[1],sp.getContent()[2],sp.getContent()[3],
					sp.getServer(), sp.getWorld(), sp.getX(), sp.getY(), sp.getZ(), sp.getPlayer()));
				Opwatch.instance.getLogger().info("SIGN TRIGGERED OPWATCH");
				break;
			}
		}
		if (queue.size()>maxSize){
			Opwatch.instance.sendIRC("Sign queue size is at "+queue.size()+". Please review all signs with the command viewSigns");
		}
	}
	public void listSigns(CommandSender sender) {
		while(!queue.isEmpty()){
			SignPlace sp=queue.removeFirst();
			sender.sendMessage(new ComponentBuilder(
						String.format("%d: \"%s, %s, %s, %s\" at %s %s %d,%d,%d placed by %s%s",
						sp.getID(),sp.getContent()[0],sp.getContent()[1],sp.getContent()[2],sp.getContent()[3],
						sp.getServer(), sp.getWorld(), sp.getX(), sp.getY(), sp.getZ(), sp.getPlayer(), sp.isWiped()?", Has been Wiped":sp.attemptwipe()?", Wipe was attempted":"")
					).create());
		}
	}

}
