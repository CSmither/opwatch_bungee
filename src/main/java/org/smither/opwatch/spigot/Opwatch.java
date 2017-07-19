package org.smither.opwatch.spigot;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.bukkit.plugin.java.JavaPlugin;
import org.smither.opwatch.bungee.SignPlace;

import com.google.common.io.ByteStreams;

public final class Opwatch extends JavaPlugin implements Listener {
	private FileConfiguration config;
	PluginChannelListener pcl;

	public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                try (InputStream is = new FileInputStream(new File("src/main/resources/spigotConfig.yml"));
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
		String[] empty={"","","",""};
		if (!event.getLines().equals(empty)){
			try {
				pcl.sendToBungeeCord(event.getPlayer(), "SignChange", toString(new SignPlace(event)) );
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	boolean wipeSign(SignPlace signPlace){
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
        return Base64.getEncoder().encodeToString(baos.toByteArray()); 
    }
}