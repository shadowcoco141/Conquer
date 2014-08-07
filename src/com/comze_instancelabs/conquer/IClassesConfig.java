package com.comze_instancelabs.conquer;

import org.bukkit.plugin.java.JavaPlugin;

import com.comze_instancelabs.minigamesapi.config.ClassesConfig;

public class IClassesConfig extends ClassesConfig {

	public IClassesConfig(JavaPlugin plugin) {
		super(plugin, true);
		this.getConfig().options().header("Used for saving classes. Default class:");
		this.getConfig().addDefault("config.kits.default.name", "Default");
		this.getConfig().addDefault("config.kits.default.items", "267#KNOCKBACK:1*1");
		this.getConfig().addDefault("config.kits.default.lore", "The Default class.");
		this.getConfig().addDefault("config.kits.default.requires_money", false);
		this.getConfig().addDefault("config.kits.default.requires_permission", false);
		this.getConfig().addDefault("config.kits.default.money_amount", 100);
		this.getConfig().addDefault("config.kits.default.permission_node", "minigames.kits.default");

		this.getConfig().addDefault("config.kits.pro.name", "Pro");
		this.getConfig().addDefault("config.kits.pro.items", "276#KNOCKBACK:3*1");
		this.getConfig().addDefault("config.kits.pro.lore", "The Pro class.");
		this.getConfig().addDefault("config.kits.pro.requires_money", true);
		this.getConfig().addDefault("config.kits.pro.requires_permission", false);
		this.getConfig().addDefault("config.kits.pro.money_amount", 100);
		this.getConfig().addDefault("config.kits.pro.permission_node", "minigames.kits.pro");

		
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
	}

}
