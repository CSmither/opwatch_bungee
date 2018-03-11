package org.smither.opwatch.spigot;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Base64;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.smither.opwatch.bungee.BookChange;
import org.smither.opwatch.bungee.SignChange;

import com.google.common.io.ByteStreams;

public final class Opwatch extends JavaPlugin implements Listener {
	FileConfiguration config;
	PluginChannelListener pcl;

	public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                try (InputStream is = getResource("spigotConfig.yml");
                        OutputStream os = new FileOutputStream(configFile)) {
                       ByteStreams.copy(is, os);
                   }
            } catch (IOException e) {
                throw new RuntimeException("Unable to create configuration file", e);
            }
        }
		pcl = new PluginChannelListener();
		config = getConfig();
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        // allow to send to BungeeCord
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", pcl);
        // gets a Message from Bungee
        getServer().getPluginManager().registerEvents(this, this);
	}
	
	@EventHandler
	public void onSignChangeEvent(SignChangeEvent event){
		if (!String.join("", event.getLines()).replace(" ", "").equals("")){
			try {
				SignChange sc=new SignChange(event);
				Bukkit.broadcastMessage(sc.getWorld());
				pcl.sendToBungeeCord(event.getPlayer(), "SignChange", toString(sc) );
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@EventHandler
	public void onPlayerEditBookEvent(PlayerEditBookEvent event){
		if (!String.join("", event.getNewBookMeta().getPages()).equals("")){
			try {
				pcl.sendToBungeeCord(event.getPlayer(), "BookChange", toString(new BookChange(event)) );
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	boolean wipeSign(SignChange signPlace){
		Location loc=new Location(Bukkit.getWorld(signPlace.getWorld()), signPlace.getX(), signPlace.getY(), signPlace.getZ());
		try{
			Sign sign=(Sign)(loc.getBlock().getState());
			sign.setLine(0, signPlace.getContent()[0]);
			sign.setLine(1, signPlace.getContent()[1]);
			sign.setLine(2, signPlace.getContent()[2]);
			sign.setLine(3, signPlace.getContent()[3]);
			sign.update();
			return true;
		} catch (ClassCastException e){
			return false;
		}
	}

    /** Write the object to a Base64 string. */
    private static String toString( Serializable o ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( o );
        oos.close();
        String out=Base64.getEncoder().encodeToString(baos.toByteArray());
        try {
			SignChange nsc=(SignChange)fromString(out);
			Bukkit.broadcastMessage(nsc.getWorld()+", "+nsc.getX());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return out;
    }
	
    /** Read the object from Base64 string. */
   private static Object fromString( String s ) throws IOException, ClassNotFoundException {
        byte [] data = Base64.getDecoder().decode( s );
        ObjectInputStream ois = new ObjectInputStream( new ByteArrayInputStream( data ) );
        Object o  = ois.readObject();
        ois.close();
        return o;
   }
}