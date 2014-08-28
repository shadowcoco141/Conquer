package com.comze_instancelabs.conquer;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.ArenaSetup;
import com.comze_instancelabs.minigamesapi.ArenaState;
import com.comze_instancelabs.minigamesapi.MinigamesAPI;
import com.comze_instancelabs.minigamesapi.PluginInstance;
import com.comze_instancelabs.minigamesapi.config.ArenasConfig;
import com.comze_instancelabs.minigamesapi.config.DefaultConfig;
import com.comze_instancelabs.minigamesapi.config.MessagesConfig;
import com.comze_instancelabs.minigamesapi.config.StatsConfig;
import com.comze_instancelabs.minigamesapi.util.Util;
import com.comze_instancelabs.minigamesapi.util.Validator;

public class Main extends JavaPlugin implements Listener {

	// allow selecting team

	MinigamesAPI api = null;
	PluginInstance pli = null;
	static Main m = null;
	IArenaScoreboard scoreboard = new IArenaScoreboard(this);
	ICommandHandler cmdhandler = new ICommandHandler();

	public static HashMap<String, String> pteam = new HashMap<String, String>();

	public void onEnable() {
		m = this;
		api = MinigamesAPI.getAPI().setupAPI(this, "conquer", IArena.class, new ArenasConfig(this), new MessagesConfig(this), new IClassesConfig(this), new StatsConfig(this, false), new DefaultConfig(this, false), true);
		PluginInstance pinstance = api.pinstances.get(this);
		pinstance.addLoadedArenas(loadArenas(this, pinstance.getArenasConfig()));
		Bukkit.getPluginManager().registerEvents(this, this);
		pinstance.scoreboardManager = new IArenaScoreboard(this);
		pinstance.arenaSetup = new IArenaSetup();
		IArenaListener listener = new IArenaListener(this, pinstance, "conquer");
		pinstance.setArenaListener(listener);
		MinigamesAPI.getAPI().registerArenaListenerLater(this, listener);
		pli = pinstance;
	}

	public static ArrayList<Arena> loadArenas(JavaPlugin plugin, ArenasConfig cf) {
		ArrayList<Arena> ret = new ArrayList<Arena>();
		FileConfiguration config = cf.getConfig();
		if (!config.isSet("arenas")) {
			return ret;
		}
		for (String arena : config.getConfigurationSection("arenas.").getKeys(false)) {
			if (Validator.isArenaValid(plugin, arena, cf.getConfig())) {
				ret.add(initArena(arena));
			}
		}
		return ret;
	}

	public static IArena initArena(String arena) {
		IArena a = new IArena(m, arena);
		ArenaSetup s = MinigamesAPI.getAPI().pinstances.get(m).arenaSetup;
		a.init(Util.getSignLocationFromArena(m, arena), Util.getAllSpawns(m, arena), Util.getMainLobby(m), Util.getComponentForArena(m, arena, "lobby"), s.getPlayerCount(m, arena, true), s.getPlayerCount(m, arena, false), s.getArenaVIP(m, arena));
		return a;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		cmdhandler.handleArgs(this, "mgconquer", "/" + cmd.getName(), sender, args);
		if (args.length > 1) {
			if (args[0].equalsIgnoreCase("setcheckpoint")) {
				if (sender instanceof Player) {
					Player p = (Player) sender;
					p.sendMessage(ChatColor.AQUA + "Place down the DragonEgg at every checkpoint you want.");
					ItemStack item = new ItemStack(Material.DRAGON_EGG);
					ItemMeta im = item.getItemMeta();
					im.setDisplayName("mgconquer:" + args[1]);
					item.setItemMeta(im);
					p.getInventory().addItem(item);
					p.updateInventory();
				}
			}
		}
		return true;
	}

	public void addGear(String p_) {
		Player p = Bukkit.getPlayer(p_);

		p.getInventory().clear();
		p.updateInventory();

		pli.getClassesHandler().getClass(p_);

		ItemStack lhelmet = new ItemStack(Material.LEATHER_HELMET, 1);
		LeatherArmorMeta lam = (LeatherArmorMeta) lhelmet.getItemMeta();

		ItemStack lboots = new ItemStack(Material.LEATHER_BOOTS, 1);
		LeatherArmorMeta lam1 = (LeatherArmorMeta) lboots.getItemMeta();

		ItemStack lchestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
		LeatherArmorMeta lam2 = (LeatherArmorMeta) lchestplate.getItemMeta();

		ItemStack lleggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
		LeatherArmorMeta lam3 = (LeatherArmorMeta) lleggings.getItemMeta();

		if (m.pteam.containsKey(p_)) {
			Color c = Color.BLACK;
			if (m.pteam.get(p_).equalsIgnoreCase("red")) {
				c = Color.RED;
			} else {
				c = Color.BLUE;
			}
			lam3.setColor(c);
			lam2.setColor(c);
			lam1.setColor(c);
			lam.setColor(c);
		}

		lhelmet.setItemMeta(lam);
		lboots.setItemMeta(lam1);
		lchestplate.setItemMeta(lam2);
		lleggings.setItemMeta(lam3);

		p.getInventory().setBoots(lboots);
		p.getInventory().setHelmet(lhelmet);
		p.getInventory().setChestplate(lchestplate);
		p.getInventory().setLeggings(lleggings);
		p.updateInventory();

	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMove(PlayerMoveEvent event) {
		final Player p = event.getPlayer();
		if (pli.global_players.containsKey(p.getName())) {
			IArena a = (IArena) pli.global_players.get(p.getName());
			if (a.getArenaState() == ArenaState.INGAME) {
				if (p.getLocation().getY() < 0) {
					// player fell
					if (pteam.containsKey(p.getName())) {
						String team = pteam.get(p.getName());
						if (team.equalsIgnoreCase("red")) {
							if (!a.addBluePoints()) {
								Util.teleportPlayerFixed(p, a.getSpawns().get(0));
								m.addGear(p.getName());
							}
						} else {
							if (!a.addRedPoints()) {
								Util.teleportPlayerFixed(p, a.getSpawns().get(1));
								m.addGear(p.getName());
							}
						}
						scoreboard.updateScoreboard(a);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (event.getEntity() instanceof Player) {
			final Player p = (Player) event.getEntity();
			if (pli.global_players.containsKey(p.getName())) {
				event.setDeathMessage(null);
				IArena a = (IArena) pli.global_players.get(p.getName());
				if (a.getArenaState() == ArenaState.INGAME) {
					p.setHealth(20D);
					if (p.getKiller() instanceof Player) {
						Player killer = (Player) p.getKiller();
						if (pteam.get(killer.getName()).equalsIgnoreCase("red")) {
							if (!a.addRedPoints()) {
								Util.teleportPlayerFixed(p, a.getSpawns().get(1));
								Bukkit.getScheduler().runTaskLater(this, new Runnable() {
									public void run() {
										m.addGear(p.getName());
									}
								}, 20L);
							}
						} else {
							if (!a.addBluePoints()) {
								Util.teleportPlayerFixed(p, a.getSpawns().get(0));
								Bukkit.getScheduler().runTaskLater(this, new Runnable() {
									public void run() {
										m.addGear(p.getName());
									}
								}, 20L);
							}
						}
						p.sendMessage(ChatColor.RED + "You have been killed by " + ChatColor.DARK_RED + killer.getName() + ChatColor.RED + ".");
					}
				}
			}
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player) {
			final Player p = (Player) event.getEntity();
			if (pli.global_players.containsKey(p.getName())) {
				IArena a = (IArena) pli.global_players.get(p.getName());
				if (a.getArenaState() == ArenaState.INGAME) {
					if (event.getDamager() instanceof Player) {
						Player p2 = (Player) event.getDamager();
						if (m.pteam.get(p.getName()).equalsIgnoreCase(m.pteam.get(p2.getName()))) {
							// same team
							event.setCancelled(true);
						}
					} else if (event.getDamager() instanceof Arrow) {
						Arrow ar = (Arrow) event.getDamager();
						if (ar.getShooter() instanceof Player) {
							Player p2 = (Player) ar.getShooter();
							if (m.pteam.get(p.getName()).equalsIgnoreCase(m.pteam.get(p2.getName()))) {
								// same team
								event.setCancelled(true);
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player p = event.getPlayer();
		if (pli.global_players.containsKey(p.getName())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		final Player p = event.getPlayer();
		if (pli.global_players.containsKey(p.getName())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.getBlock().getType() == Material.DRAGON_EGG) {
			Player p = event.getPlayer();
			if (p.hasPermission("mgconquer.setup")) {
				String arenaname_ = event.getItemInHand().getItemMeta().getDisplayName();

				if (arenaname_ == null)
					return;

				String args[] = arenaname_.split(":");
				String plugin = args[0];
				if (!plugin.equalsIgnoreCase("mgconquer")) {
					return;
				}
				String arenaname = args[1];

				Location l = event.getBlock().getLocation();

				int count = getAllCheckPoints(arenaname);
				Util.saveComponentForArena(this, arenaname, "checkpoints.cp" + Integer.toString(count), l.clone().add(0D, -1D, 0D));
				event.getPlayer().sendMessage(pli.getMessagesConfig().successfully_set.replaceAll("<component>", "checkpoint " + Integer.toString(count)));

				for (int x = -2; x < 3; x++) {
					for (int z = -2; z < 3; z++) {
						l.clone().add(x, -1D, z).getBlock().setType(Material.WOOL);
					}
				}
				event.setCancelled(true);
			}
		}
	}

	public int getAllCheckPoints(String arenaname) {
		int ret = 0;
		FileConfiguration config = MinigamesAPI.getAPI().pinstances.get(m).getArenasConfig().getConfig();
		if (config.isSet("arenas." + arenaname + ".checkpoints.")) {
			for (String cp : config.getConfigurationSection("arenas." + arenaname + ".checkpoints.").getKeys(false)) {
				ret++;
			}
		}
		return ret;
	}

}
