package org.smither.opwatch.bungee;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Base64;

import com.cnaude.purpleirc.PurpleBot;
import com.cnaude.purpleirc.PurpleIRC;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ChannelListener implements Listener{
	Opwatch plugin;
	private PurpleBot ircBot;
	private PurpleIRC purple;
	
	public ChannelListener(Opwatch plugin) {
		this.plugin=plugin;
	}
	
    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) {
        if (e.getTag().equalsIgnoreCase("BungeeCord")) {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(e.getData()));
            try {
                String channel = in.readUTF(); // channel we delivered
                String input;
                if(channel.equals("SignChange")){
                	input = in.readUTF(); // the inputstring
                    Object obj=fromString(input);
                    SignPlace sign=(SignPlace) obj ;
                    plugin.addSign(sign);
                }
                else if (channel.equals("WipeSign")){
                    input = in.readUTF(); // the inputstring
                	boolean success=input.startsWith("Y");
                	input=input.substring(1);
                    SignPlace sp=SignStore.getInstance().get(Integer.parseInt(input)) ;
                	sendIRC(String.format("Sign%swiped %d: \"%s, %s, %s, %s\" at %s %s %d,%d,%d placed by %s%s%s", success?" ":" NOT ",
        					sp.getID(),sp.getContent()[0],sp.getContent()[1],sp.getContent()[2],sp.getContent()[3],
        					sp.getServer(), sp.getWorld(), sp.getX(), sp.getY(), sp.getZ(), sp.getPlayer(), sp.isWiped()?", Has been Wiped":"", success?"":", probably the sign has been destroyed"));
                    SignStore.getInstance().setAttemptWipe(sp.getID(), true);
                	if (success){
                        SignStore.getInstance().setWiped(sp.getID(), true);
                    }
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
     
        }
    }

    public void sendToBukkit(String channel, String message, ServerInfo server) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);
        try {
            out.writeUTF(channel);
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!server.sendData("BungeeCord", stream.toByteArray(),true)) {
        	sendIRC("OPWatch >> Message failed to send to spigot servers");
        }
    }

    void sendIRC(String msg){
    	if (ircBot == null){
			purple=(PurpleIRC)ProxyServer.getInstance().getPluginManager().getPlugin("PurpleBungeeIRC");
			if (purple==null){
		        plugin.getLogger().severe("PURPLE IRC NOT LOADED! ! !");
			}
    		ircBot = purple.ircBots.get(plugin.config.get("PurpleBotName"));
        	if (ircBot != null){
        		plugin.getLogger().info("Hooked to Purple");
        	}
    	}
    	if (ircBot != null){
			ircBot.asyncIRCMessage((String)plugin.config.get("PurpleChannel"),msg);
    	}
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