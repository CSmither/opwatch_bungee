package org.smither.opwatch.bungee;

import java.io.Serializable;

import org.bukkit.event.block.SignChangeEvent;

public class SignPlace implements Serializable, Cloneable{
	@Override
	protected SignPlace clone() throws CloneNotSupportedException {
		SignPlace clone=(SignPlace) super.clone();
		clone.content=content.clone();
		return clone;
	}
	private static final long serialVersionUID = 1L;
	private int id;
	private String[] content;
	private String server;
	private String world;
	private int x;
	private int y;
	private int z;
	private String player;
	private boolean wiped=false;
	private boolean attemptWipe=false;
	public SignPlace(SignChangeEvent sign) {
		content=sign.getLines();
		server=sign.getPlayer().getServer().getServerName();
		world=sign.getBlock().getWorld().getName();
		x=sign.getBlock().getX();
		y=sign.getBlock().getY();
		z=sign.getBlock().getZ();
		this.player=sign.getPlayer().getDisplayName();
	}
	SignPlace(int id, String[] content, String server, String world, int x, int y, int z, String player, boolean wiped, boolean attemptWipe) {
		this.id=id;
		this.content=content;
		this.server=server;
		this.world=world;
		this.x=x;
		this.y=y;
		this.z=z;
		this.player=player;
		this.wiped=wiped;
		this.attemptWipe=attemptWipe;
	}
	public String[] getContent() {
		return content;
	}
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public int getZ() {
		return z;
	}
	public String getPlayer() {
		return player;
	}
	public String getServer() {
		return server;
	}
	public void setServer(String server) {
		this.server = server;
	}
	public void setContent(String[] message) {
		content=message;
	}
	public String getWorld() {
		return world;
	}
	public void setID(int id) {
		if (this.id==0){
			this.id=id;
		}
	}
	public int getID() {
		return id;
	}
	public void wiped(boolean wiped) {
		this.wiped=wiped;
	}
	public boolean isWiped() {
		return wiped;
	}
	public void attemptwipe(boolean b) {
		attemptWipe = true;
	}
	public boolean attemptwipe() {
		return attemptWipe;
	}

}
