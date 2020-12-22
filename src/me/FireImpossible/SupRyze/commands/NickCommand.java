package me.FireImpossible.SupRyze.commands;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import me.FireImpossible.SupRyze.Main;

public class NickCommand implements CommandExecutor {

	private Main plugin;
	
	public NickCommand(Main plugin) {
		this.plugin = plugin;
		plugin.getCommand("nick").setExecutor(this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		// variables
		Player p = null;
		String newName = null;
		String ogName = null;
		String currentNick = null;
		String pUUID = null;
		String lastColorString = null;
		String helpCommandString = null;
		int helpCommandPage = 1;
		boolean removeLetter = false;
		
		// if its not a player that does this
		if (!(sender instanceof Player)) {
			sender.sendMessage("Only players may execute this command!");
			return true;
		}
		
		// setting the player variable
		p = (Player) sender;

		// file stuff
		try {
			pUUID = p.getUniqueId().toString();
			ogName = plugin.getConfig().getString("players." + pUUID + ".ogName");
			currentNick = plugin.getConfig().getString("players." + pUUID + ".nick");
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		
		// seeing if they already have a nick
		if (ogName == null) {
			ogName = p.getName();
		}
		
		//----------------------------
		// the start of the name logic
		//----------------------------
		
		// help
		if (args.length < 1 || args[0].equalsIgnoreCase("help")) {
			// if they just do '/nick help' without a page number 
			if (args.length == 0 || args.length == 1) {
				p.sendMessage(helpMessage(1, helpCommandString));
				return true;
			}
			helpCommandPage = Integer.parseInt(args[1]);
			p.sendMessage(helpMessage(helpCommandPage, helpCommandString));
			
			return true;
		}
		// clear | reset
		else if (args[0].equalsIgnoreCase("clear") || args[0].equalsIgnoreCase("reset")) {
			System.out.println("ogName -> " + ogName + "\nnick -> " + currentNick);
			if (currentNick != ogName) {
				plugin.getConfig().set("players." + pUUID + ".nick", ogName);
				// save file
				plugin.saveConfig();

				System.out.println("ogName -> " + ogName);
				p.setDisplayName(ogName);
				p.setPlayerListName(ogName);
				changeName(ogName, p);
				p.sendMessage(ChatColor.YELLOW + "Changed your name back to " + ogName + "!");
			}
			else {
				p.sendMessage(ChatColor.YELLOW + "Your name is already your original name");
			}
		}
		// everything else
		else {
			StringBuilder newNameBuilder = new StringBuilder();
			for (int i = 0; i < args.length; i++) {
				if (i > 0) {
					newNameBuilder.append(' ');
				}
				newNameBuilder.append(args[i]);
			}
			newName = newNameBuilder.toString();
			
			newName = applyColor(newName) + ChatColor.WHITE;
			System.out.println(newName);
			
			if (newName.length() - 2 > 77) {
				p.sendMessage(ChatColor.YELLOW + "-------------------------------------------------\n" + 
						"You found my limit!\nPlease choose a name that is less than 77 characters :)\n" + 
						"-------------------------------------------------");
				return true;
			}
			
			// chat and tab list
			p.setDisplayName(newName); // chat
			p.setPlayerListName(newName); // tab list
			
			// get the last color for the suffix
	        if (newName.length() > 1) {
	        	if (newName.length() > 15) {
		        	if (newName.charAt(15) == '§') {
			        	lastColorString = ChatColor.getLastColors(newName.substring(0, 15)) + newName.substring(15, 17);
			        }
			        else if (newName.charAt(14) == '§') {
			        	lastColorString = ChatColor.getLastColors(newName.substring(0, 14)) + newName.substring(14, 16);
			        	removeLetter = true;
			        }
			        else {
			        	lastColorString = ChatColor.getLastColors(newName.substring(0, 15));
			        }
	        	}
	        }
	        
			// change player nametag
			teamThings(p, newName, pUUID, lastColorString, removeLetter);
			
			p.sendMessage("Set your name to -> " + newName);
			
			// change stuff in the file
			plugin.getConfig().set("players." + pUUID + ".nick", newName);
			plugin.getConfig().set("players." + pUUID + ".ogName", ogName);
			// save file
			plugin.saveConfig();
		}
		
		return true;
	}
	
	public static void teamThings(Player p, String newName, String pUUID, String lastColor, boolean removeLetter) {
		Team pTeam = null;
		pUUID = pUUID.substring(0, 15);
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getMainScoreboard();
		
        pTeam = board.getTeam(pUUID);
        
        if (pTeam == null) {
        	pTeam = board.registerNewTeam(pUUID);
        	pTeam.addEntry(p.getName());
        }
        else; // you better fucking do it
        
        // reset the prefix and suffix
        pTeam.setPrefix("");
        pTeam.setSuffix("");
        
        // change the names
        pTeam.removeEntry(p.getName());
        
        if (newName.length() > 15) {
            changeName(newName.substring(0, 15), p);
        	if (removeLetter) {
        		pTeam.setSuffix(lastColor + newName.substring(16));
        	} else {
            	pTeam.setSuffix(lastColor + newName.substring(15));        		
        	}
        }
        // if the name is not too long
        else if (newName.length() <= 15) {
            changeName(newName.substring(0), p);
        }
        
        pTeam.addEntry(p.getName());

	}

	// convert all occurrences of {red} to ChatColor.valueOf("RED")
	public static String applyColor(String name) {
		Pattern p = Pattern.compile("\\{([A-Za-z_]+?)\\}");
		Matcher m = p.matcher(name);
		StringBuilder sb = new StringBuilder();
		while (m.find()) {
		    m.appendReplacement(sb, ChatColor.valueOf(m.group(1).toUpperCase()).toString());
		    System.out.println(m.group(1) + " " + ChatColor.valueOf(m.group(1).toUpperCase()).toString());
		}
		m.appendTail(sb);
		return sb.toString();
	}
	
	// this is not my code
	@SuppressWarnings("deprecation")
	public static void changeName(String name, Player player) {
		try {
			Method getHandle = player.getClass().getMethod("getHandle", (Class<?>[]) null);
			// Object entityPlayer = getHandle.invoke(player);
			// Class<?> entityHuman = entityPlayer.getClass().getSuperclass();
			/**
			 * These methods are no longer needed, as we can just access the profile using
			 * handle.getProfile. Also, because we can just use the method, which will not
			 * change, we don't have to do any field-name look-ups.
			 */
			try {
				Class.forName("com.mojang.authlib.GameProfile");
				// By having the line above, only 1.8+ servers will run this.
			} catch (ClassNotFoundException e) {
				/**
				 * Currently, there is no field that can be easily modified for lower versions.
				 * The "name" field is final, and cannot be modified in runtime. The only
				 * workaround for this that I can think of would be if the server creates a
				 * "dummy" entity that takes in the player's input and plays the player's
				 * animations (which will be a lot more lines)
				 */
				Bukkit.broadcastMessage("CHANGE NAME METHOD DOES NOT WORK IN 1.7 OR LOWER!");
				return;
			}
			Object profile = getHandle.invoke(player).getClass().getMethod("getProfile")
					.invoke(getHandle.invoke(player));
			Field ff = profile.getClass().getDeclaredField("name");
			ff.setAccessible(true);
			ff.set(profile, name);
			for (Player players : Bukkit.getOnlinePlayers()) {
				players.hidePlayer(player);
				players.showPlayer(player);
			}
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchFieldException e) {
			/**
			 * Merged all the exceptions. Less lines
			 */
			e.printStackTrace();
		}
	}
	
	public static String helpMessage(int helpCommandPage, String helpCommandString) {
		if (helpCommandPage < 1 || helpCommandPage > 9) {
			helpCommandString = "Please input a page number between 1 and 9";
		}
		switch(helpCommandPage) {
		case 1:
			helpCommandString = " \nNick Help pg " + ChatColor.BOLD + "1" + " \n" +
				ChatColor.WHITE + "----------------------------- \n" +
				ChatColor.YELLOW + "Welcome to the nickname plugin! \n" + ChatColor.RESET +
				" \n" +
				"With this plugin, you can change your nickname to anything you want \n" +
				"There is a max of 77 characters, which you shouldn't even touch \n" +
				"...that can be changed if needed \n" +
				"Type ‘/nick help <pg number>’ to access the other pages \n" +
				" \n" +
				"Page 2 - how to change your nickname \n" +
				"Page 3 - how to add a color \n" +
				"Page 4 - how to add multiple colors \n" +
				"Page 5 - how to add some text effects \n" +
				"Page 6 - tips and tricks for the effects \n" +
				"Page 7 & 8 - list of all the colors you can use \n" +
				"Page 9 - list of all effects \n" +
				" \n" +
				ChatColor.YELLOW + "or you can go through them chronologically for a full tutorial! \n" +
				ChatColor.WHITE + "-----------------------------";
			break;
		case 2:
			helpCommandString = " \nNick Help pg " + ChatColor.BOLD + "2" + " \n" +
				ChatColor.WHITE + "----------------------------- \n" +
				ChatColor.YELLOW + "Here is how to give yourself a nickname! \n" + ChatColor.RESET +
				" \n" +
				"To give or change your nickname, do ‘/nick <name>’ \n" +
				"Let’s say I wanted to name myself ‘Bob Jones’ \n" +
				"To do this, I would type ‘/nick Bob Jones’ \n" +
				"It would display your name as: ‘Bob Jones’ \n" +
				" \n" +
				"If you want to clear your nickname, do ‘/nick clear’ or ‘/nick reset’ \n" +
				" \n" +
				ChatColor.YELLOW + "Go to page 3 to see how to add color to your nickname! \n" +
				ChatColor.WHITE + "-----------------------------";
			break;
		case 3:
			helpCommandString = " \nNick Help pg " + ChatColor.BOLD + "3" + " \n" +
				ChatColor.WHITE + "----------------------------- \n" +
				ChatColor.YELLOW + "Here is how to add color into your nickname \n" + ChatColor.RESET +
				" \n" +
				"Let’s say I want to make my name red \n" +
				"I would type ‘/nick {red}Bob Jones’ \n" +
				"Which would display as: ‘" + ChatColor.RED + "Bob Jones" + ChatColor.RESET + "’ \n" +
				"It has to be a minecraft color, which are displayed on pages 7 and 8 \n" +
				"However this could change in later versions \n" +
				" \n" +
				ChatColor.YELLOW + "Go to page 4 to see how to have multiple colors! \n" +
				ChatColor.WHITE + "-----------------------------";
			break;
		case 4:
			helpCommandString = " \nNick Help pg " + ChatColor.BOLD + "4" + " \n" +
				ChatColor.WHITE + "----------------------------- \n" +
				ChatColor.YELLOW + "Here is how to have multiple colors! \n" + ChatColor.RESET +
				" \n" +
				"Now I want to keep Bob red, but make Jones blue \n" +
				"What I will do, is type ‘/nick {red}Bob {blue}Jones’ \n" +
				"This will display as: ‘" + ChatColor.RED + "Bob " + ChatColor.BLUE + "Jones" + ChatColor.RESET + "’ \n" +
				"Each time you type a new color, it will change the rest of the text to that color \n" +
				" \n" +
				ChatColor.YELLOW + "Go to page 5 to see how to add some text effects! \n" +
				ChatColor.WHITE + "-----------------------------";
			break;
		case 5:
			helpCommandString = " \nNick Help pg " + ChatColor.BOLD + "5" + " \n" +
				ChatColor.WHITE + "----------------------------- \n" +
				ChatColor.YELLOW + "Here is how to add some text effects to your nickname \n \n" + ChatColor.RESET +
				"Now I want to bold the name and keep the colors \n" +
				"So I will type ‘/nick {red}{bold}Bob {blue}Jones’ \n" +
				"However this makes it look like: ‘" + ChatColor.RED + ChatColor.BOLD + "Bob " + ChatColor.RESET + ChatColor.BLUE + "Jones " + ChatColor.RESET + "’ \n" +
				"As you can see, this makes only the Bob bold \n" +
				" \n" +
				"Each time that you change the color of something that has an effect on it llike bold or italics, it will remove that effect \n" +
				"This means that you will have to type {bold} again \n" +
				"So for this example, I will type ‘/nick {red}{bold}Bob {blue}{bold}Jones’ \n" +
				"Which would look like: ‘" + ChatColor.RED + ChatColor.BOLD + "Bob " + ChatColor.BLUE + ChatColor.BOLD + "Jones" + ChatColor.RESET + "’ \n" +
				" \n" +
				ChatColor.YELLOW + "Go to page 6 to see some more rules for these effects! \n" +
				ChatColor.WHITE + "-----------------------------";
			break;
		case 6:
			helpCommandString = " \nNick Help pg " + ChatColor.BOLD + "5" + " \n" +
				ChatColor.WHITE + "----------------------------- \n" +
				ChatColor.YELLOW + "Here are some more rules for the text effects" +ChatColor.RESET + " \n" +
				" \n" +
				"You have to put the color before the effect \n" +
				"So if I put ‘/nick {bold}{red}Bob Jones’ \n" +
				"I will only get: ‘" + ChatColor.RED + "Bob Jones" + ChatColor.RESET + "’ \n" +
				" \n" +
				"You can also use multiple text effects! \n" +
				"So if I wanted to use every effect except magic, I would type: \n" +
				"‘/nick {red}{bold}{italic}{underline}{strikethrough}Bob Jones’ \n" +
				"I would get: ‘" + ChatColor.RED + ChatColor.BOLD + ChatColor.ITALIC + ChatColor.UNDERLINE + ChatColor.STRIKETHROUGH + "Bob Jones" + ChatColor.RESET + "’ \n" +
				"Something to think about is that it actually doesn't matter which order you put the effects in \n" +
				" \n" +
				ChatColor.YELLOW + "Go to page 7 for the full list of colors you can use! \n" +
				ChatColor.WHITE + "-----------------------------";
			break;
		case 7:
			helpCommandString = " \nNick Help pg " + ChatColor.BOLD + "7" + " \n" + 
				ChatColor.WHITE + "----------------------------- \n" +
				ChatColor.YELLOW + "First List of Colors: \n" +
				" \n" +
				ChatColor.WHITE + "{white} " +
				ChatColor.BLACK + "{black} " +
				ChatColor.GRAY + "{gray} \n" +
				ChatColor.BLUE + "{blue} " +
				ChatColor.GREEN + "{green} " +
				ChatColor.AQUA + "{aqua} \n" +
				ChatColor.RED + "{red} " +
				ChatColor.YELLOW + "{yellow} " +
				ChatColor.GOLD + "{gold} \n \n" +
				ChatColor.YELLOW + "Go to page 8 to see more colors! \n" +
				ChatColor.WHITE + "-----------------------------";
			break;
		case 8:
			helpCommandString = " \nNick Help pg " + ChatColor.BOLD + "8" + " \n" + 
				ChatColor.WHITE + "----------------------------- \n"  +
				ChatColor.YELLOW + "Second List of Colors: \n" +
				" \n" +
				ChatColor.DARK_BLUE + "{dark_blue} " +
				ChatColor.DARK_GREEN + "{dark_green} \n" +
				ChatColor.DARK_AQUA + "{dark_aqua} " +
				ChatColor.DARK_RED + "{dark_red} " +
				ChatColor.LIGHT_PURPLE + "{light_purple} \n" +
				ChatColor.DARK_PURPLE + "{dark_purple} " +
				ChatColor.DARK_GRAY + "{dark_gray} \n" +
				" \n" +
				ChatColor.YELLOW + "Go to page 9 to see the list of text effects! \n" +
				ChatColor.WHITE + "-----------------------------";			
			break;
		case 9:
			helpCommandString = " \nNick Help pg " + ChatColor.BOLD + "9" + " \n" + 
				ChatColor.WHITE + "----------------------------- \n"  +
				ChatColor.YELLOW + "List of Text Effects: \n \n" + ChatColor.RESET +
				ChatColor.BOLD + "{bold} \n" + ChatColor.RESET +
				ChatColor.ITALIC + "{italic} " + ChatColor.WHITE + "- notice how there is no 's' at the end \n" + ChatColor.RESET +
				ChatColor.UNDERLINE + "{underline} \n" + ChatColor.RESET +
				ChatColor.STRIKETHROUGH + "{strikethrough}" + ChatColor.RESET + " \n" +
				ChatColor.MAGIC + "magic " + ChatColor.RESET + "- {magic} \n" +
				" \n" +
				ChatColor.YELLOW + "This is the end of the help, enjoy! :) \n" +
				ChatColor.WHITE + "-----------------------------";			
			break;
		}
		return helpCommandString;
	}
	
}
