package me.FireImpossible.SupRyze;

import me.FireImpossible.SupRyze.commands.*;
import me.FireImpossible.SupRyze.listeners.*;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

	@Override
	public void onEnable() {
		new NickCommand(this);
		
		PluginManager pm = getServer().getPluginManager();
		TestListener listener = new TestListener(this);
		pm.registerEvents(listener, this);
		
	}
	
}
