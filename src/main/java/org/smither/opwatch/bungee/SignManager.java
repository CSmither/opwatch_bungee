package org.smither.opwatch.bungee;

import net.md_5.bungee.api.CommandSender;

public class SignManager {
	private SignQueue signQueue;
	private SignStore signStore;
	public SignManager() {
        signQueue=SignQueue.getInstance();
        signStore=SignStore.getInstance();
	}
	
	public void save(){
    	signQueue.save();
    	signStore.save();
	}

	public void addSign(SignPlace sign) {
    	signStore.add(sign);
    	signQueue.add(sign);
	}

	public SignPlace wipeSign(int id, String[] message) {
    	SignPlace sign=signStore.get(id);
    	signStore.updateSign(id, "attemptWipe", true);
		try {
			sign = sign.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
    	if (sign==null){
    		throw new NullPointerException("No Sign with that ID");
    	}
    	sign.setContent(message);
    	return sign;
	}

	public void shutdown() {
		signQueue.save();
		signStore.save();
	}

	public void listSigns(CommandSender sender) {
		signQueue.listSigns(sender);
	}

	public void listSigns(CommandSender sender, int amount) {
		signStore.listSigns(sender, amount);
	}

	public SignPlace viewSign(CommandSender sender, int id) {
		return signStore.get(id);
	}

	public SignPlace getSign(int id) {
    	return signStore.get(id);
	}

}
