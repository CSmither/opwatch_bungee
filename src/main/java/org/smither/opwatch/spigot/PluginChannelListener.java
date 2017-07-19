package org.smither.opwatch.spigot;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Base64;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.smither.opwatch.bungee.SignPlace;

public class PluginChannelListener implements PluginMessageListener{
	
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
        try {
            String subchannel = in.readUTF();
            if(subchannel.equals("WipeSign")){
                Object obj = fromString(in.readUTF());
            	SignPlace sign=(SignPlace)obj;
            	if (Opwatch.getPlugin(Opwatch.class).wipeSign(sign)){
            		sendToBungeeCord(player, "WipeSign", "Y"+Integer.toString(sign.getID()));
            	} else {
            		sendToBungeeCord(player, "WipeSign", "N"+Integer.toString(sign.getID()));
            	}
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void sendToBungeeCord(Player p, String channel, String sub){
    	Opwatch.getPlugin(Opwatch.class).getLogger().info(channel+" : "+sub);
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF(channel);
            out.writeUTF(sub);
        } catch (IOException e) {
            e.printStackTrace();
        }
        p.sendPluginMessage(Opwatch.getPlugin(Opwatch.class), "BungeeCord", b.toByteArray());
    }
	
    /** Read the object from Base64 string. */
   private static Object fromString( String s ) throws IOException, ClassNotFoundException {
        byte [] data = Base64.getDecoder().decode( s );
        ObjectInputStream ois = new ObjectInputStream( 
                                        new ByteArrayInputStream(  data ) );
        Object o  = ois.readObject();
        ois.close();
        return o;
   }

}