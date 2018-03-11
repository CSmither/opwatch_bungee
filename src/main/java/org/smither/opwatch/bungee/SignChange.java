package org.smither.opwatch.bungee;

import java.io.Serializable;

import org.bukkit.Bukkit;
import org.bukkit.event.block.SignChangeEvent;

public class SignChange extends Change implements Serializable, Cloneable{
	@Override
	protected SignChange clone() throws CloneNotSupportedException {
		SignChange clone=(SignChange) super.clone();
		clone.content=content.clone();
		return clone;
	}
	private static final long serialVersionUID = -4294635711232780552L;
	private String[] content;
	public SignChange(SignChangeEvent event) {
		content=event.getLines();
		setServer(event.getPlayer().getServer().getServerName());
		setWorld(event.getPlayer().getWorld().getName());
		Bukkit.broadcastMessage(event.getPlayer().getWorld().getName());
		Bukkit.broadcastMessage(getWorld());
		setX(event.getBlock().getX());
		setY(event.getBlock().getY());
		setZ(event.getBlock().getZ());
		setPlayer(event.getPlayer().getDisplayName());
	}
	SignChange(int id, String[] content, String server, String world, int x, int y, int z, String player, boolean wiped, boolean attemptWipe) {
		this.setID(id);
		this.content=content;
		this.setServer(server);
		this.setWorld(world);
		this.setX(x);
		this.setY(y);
		this.setZ(z);
		this.setPlayer(player);
		this.wiped(wiped);
		this.attemptWipe(attemptWipe);
	}
	public String[] getContent() {
		return content;
	}
	public void setContent(String[] content) {
		this.content=content;
	}
}
