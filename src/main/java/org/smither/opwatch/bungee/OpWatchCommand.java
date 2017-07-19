package org.smither.opwatch.bungee;

import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class OpWatchCommand extends Command{
	String[] wipeMsg;

	public OpWatchCommand(String string) {
		super(string);
		List<String> wipeMsg=new ArrayList<String>();
		for (Object line : Opwatch.instance.config.getList("WipeMsg")){
			wipeMsg.add((String)line);
		}
		this.wipeMsg=wipeMsg.toArray(new String[4]);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		switch (args[0].toLowerCase()){
		case "viewsigns":
			if (args.length == 2){
	    		Opwatch.instance.getLogger().info("Listing signs to "+sender.getName());
	    		Opwatch.instance.getSignManager().listSigns(sender, Integer.parseInt(args[1]));
			}
    		Opwatch.instance.getLogger().info("Listing signs to "+sender.getName());
    		Opwatch.instance.getSignManager().listSigns(sender);
			break;
		case "wipesign":
			Opwatch.instance.getLogger().info("Wiping sign ID: "+args[1]);
			Opwatch.instance.wipeSign(Integer.parseInt(args[1]), wipeMsg);
			break;
		case "viewsign":
    		Opwatch.instance.getLogger().info("Viewing sign "+args[1]+" by "+sender.getName());
    		SignPlace sp=Opwatch.instance.getSignManager().viewSign(sender, Integer.parseInt(args[1]));
    		sender.sendMessage(
    				String.format("%d: \"%s, %s, %s, %s\" at %s %s %d,%d,%d placed by %s%s",
					sp.getID(),sp.getContent()[0],sp.getContent()[1],sp.getContent()[2],sp.getContent()[3],
					sp.getServer(), sp.getWorld(), sp.getX(), sp.getY(), sp.getZ(), sp.getPlayer(), sp.isWiped()?", Has been Wiped":sp.attemptwipe()?", Wipe was attempted":""));
			break;
		}
	}

}
