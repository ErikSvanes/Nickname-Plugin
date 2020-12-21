package me.FireImpossible.SupRyze.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.FireImpossible.SupRyze.Main;
import me.FireImpossible.SupRyze.commands.NickCommand;

public class TestListener implements Listener {

	private Main plugin;
	
	// variables
	String currentNick = null;
	String ogName = null;
	String pUUID = null;
	Boolean nicked = null;
	int lastColorIndex = 0;
	String lastColorString = null;
	boolean removeLetter = false;
	
	public TestListener(Main plugin) {
		this.plugin = plugin;
	}
	
	// event handler
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		
		// file stuff?
		try {
			pUUID = p.getUniqueId().toString();
			currentNick = plugin.getConfig().getString("players." + pUUID + ".nick");
			ogName = plugin.getConfig().getString("players." + pUUID + ".ogName");
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		
		if (currentNick != null && !currentNick.equals(ogName)) {
			System.out.println("currentNick -> " + currentNick);
			
			currentNick = NickCommand.applyColor(currentNick);
			
			// get the last color for the suffix
	        if (currentNick.length() > 1) {
	        	if (currentNick.length() > 15) {
		        	if (currentNick.charAt(15) == '§') {
			        	lastColorString = ChatColor.getLastColors(currentNick.substring(0, 15)) + currentNick.substring(15, 17);
			        }
			        else if (currentNick.charAt(14) == '§') {
			        	lastColorString = ChatColor.getLastColors(currentNick.substring(0, 14)) + currentNick.substring(14, 16);
			        	removeLetter = true;
			        }
			        else {
			        	lastColorString = ChatColor.getLastColors(currentNick.substring(0, 15));
			        }
	        	}
	        }
			
	        // change the name
			NickCommand.teamThings(p, currentNick, pUUID, lastColorString, removeLetter);
			p.setDisplayName(currentNick);
			p.setPlayerListName(currentNick);			

			// change the join message
			event.setJoinMessage(currentNick + ChatColor.YELLOW + " has joined the game");
		}

	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		
		// variables
		String currentNick = null;
		String ogName = null;
		String pUUID = null;
		
		// file stuff?
		try {
			pUUID = p.getUniqueId().toString();
			currentNick = plugin.getConfig().getString("players." + pUUID + ".nick");
			currentNick = NickCommand.applyColor(currentNick);
			ogName = plugin.getConfig().getString("players." + pUUID + ".ogName");
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		
		if (currentNick != null && !currentNick.equals(ogName)) {
			event.setQuitMessage(currentNick + ChatColor.YELLOW + " has left the game");
		}

	}
}