package org.smither.opwatch.bungee;

import net.md_5.bungee.api.CommandSender;

public class Manager {
	private Queue signQueue;
	private Store signStore;
	public Manager() {
        signQueue=Queue.getInstance();
        signStore=Store.getInstance();
	}
	
	public void save(){
    	signQueue.save();
    	signStore.save();
	}

	public void addSign(SignChange sign) {
    	signStore.add(sign);
    	signQueue.add(sign);
	}

	public void addBook(BookChange book) {
    	signStore.add(book);
    	signQueue.add(book);
	}

	public SignChange wipeSign(int id, String[] message) {
    	SignChange sign=signStore.get(id);
    	signStore.setAttemptWipe(id, true);
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

	/*public BookChange wipeBook(int id, String message) {
    	SignChange sign=signStore.get(id);
    	signStore.setAttemptWipe(id, true);
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
*/
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

	public SignChange viewSign(CommandSender sender, int id) {
		return signStore.get(id);
	}

	public SignChange getSign(int id) {
    	return signStore.get(id);
	}

}
