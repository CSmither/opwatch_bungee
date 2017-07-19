package org.smither.opwatch.bungee;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class SignStore implements Serializable{
	private static final long serialVersionUID = 1L;
	private static SignStore instance;
    private Map <Integer,SignPlace> signs;
    private int maxID;
	static SignStore getInstance(){
		if (instance==null){
			try {
		        File file = new File(Opwatch.instance.getDataFolder(), "SignStore.data");
		        if (!file.exists()) {
		            try {
		            	file.createNewFile();
		            } catch (IOException e) {
		                throw new RuntimeException("Unable to create SignStore file", e);
		            }
		        }
		         FileInputStream fileIn = new FileInputStream(file);
		         ObjectInputStream in = new ObjectInputStream(fileIn);
		         instance = (SignStore) in.readObject();
		         in.close();
		         fileIn.close();
		      }catch(IOException i) {
		         i.printStackTrace();
		         instance=new SignStore();
		      }catch(ClassNotFoundException c) {
		         c.printStackTrace();
		         instance=new SignStore();
		      }
		}
		return instance;
	}
    private SignStore() {
		signs=new HashMap<Integer,SignPlace>();
		maxID=0;
	}
    public void add(SignPlace sp) {
    	sp.setID(maxID++);
    	signs.put(sp.getID(), sp);
    }
	public SignPlace get(int id) {
		return signs.get(id);
	}
	void save(){
		try {
	        FileOutputStream fileOut = new FileOutputStream(new File(Opwatch.instance.getDataFolder(), "SignStore.data"));
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(instance);
			out.close();
			fileOut.close();
			}catch(IOException i) {
				i.printStackTrace();
			}
	}
	public void listSigns(CommandSender sender, int amount) {
		for (int i=maxID ; i>=maxID-amount ; i++ ){
			SignPlace sp=signs.get(i);
			if (sp == null){
				break;
			}
			sender.sendMessage(new ComponentBuilder(
						String.format("%d: \"%s, %s, %s, %s\" at %s %s %d,%d,%d placed by %s%s",
						sp.getID(),sp.getContent()[0],sp.getContent()[1],sp.getContent()[2],sp.getContent()[3],
						sp.getServer(), sp.getWorld(), sp.getX(), sp.getY(), sp.getZ(), sp.getPlayer())
					).create());
		}
	}
}
