package org.smither.opwatch.bungee;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Base64;

import com.google.common.io.ByteStreams;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class Opwatch extends Plugin {
	private ChannelListener cl;
	Configuration config;
	public static Opwatch instance;
	private SignManager signManager;
	public boolean debug=true;
	
    @Override
    public void onEnable(){
    	instance=this;
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                try (InputStream is = getResourceAsStream("bungeeConfig.yml");
                     OutputStream os = new FileOutputStream(configFile)) {
                    ByteStreams.copy(is, os);
                }
            } catch (IOException e) {
                throw new RuntimeException("Unable to create configuration file", e);
            }
        }
    	try {
			config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
    	debug=config.getBoolean("debug");
    	cl=new ChannelListener(this);
    	signManager=new SignManager();
        this.getProxy().getPluginManager().registerListener(this, cl);
        this.getProxy().registerChannel("BungeeCord");
        getProxy().getPluginManager().registerCommand(this, new OpWatchCommand("OpWatch"));
        getProxy().getPluginManager().registerCommand(this, new OpWatchCommand("ow"));
        getLogger().info("OPWATCH ready to go!");
    }

    @Override
    public void onDisable(){
    	try {
			signManager.shutdown();
			ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, new File(getDataFolder(), "config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    void reload(){
    	instance=this;
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                try (InputStream is = getResourceAsStream("bungeeConfig.yml");
                     OutputStream os = new FileOutputStream(configFile)) {
                    ByteStreams.copy(is, os);
                }
            } catch (IOException e) {
                throw new RuntimeException("Unable to create configuration file", e);
            }
        }
    	try {
			config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
    	cl=new ChannelListener(this);
    	signManager=new SignManager();
        this.getProxy().getPluginManager().registerListener(this, cl);
        this.getProxy().registerChannel("BungeeCord");
        getProxy().getPluginManager().registerCommand(this, new OpWatchCommand("OpWatch"));
        getProxy().getPluginManager().registerCommand(this, new OpWatchCommand("ow"));
        getLogger().info("OPWATCH ready to go!");
    }

    public void addSign(SignPlace sign){
    	signManager.addSign(sign);
    }

    void wipeSign(int id, String[] message){
    	SignPlace sign;
		try {
			sign = signManager.wipeSign(id, message);
			cl.sendToBukkit("WipeSign", toString(sign), getProxy().getServerInfo(sign.getServer()));
		} catch (IOException e) {
			e.printStackTrace();
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

	public void sendIRC(String msg) {
		cl.sendIRC(msg);
	}

	public SignManager getSignManager() {
		return signManager;
	}
}