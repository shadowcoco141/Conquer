package com.comze_instancelabs.conquer;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.ArenaType;
import com.comze_instancelabs.minigamesapi.MinigamesAPI;
import com.comze_instancelabs.minigamesapi.util.Util;

public class IArena extends Arena {

	public static Main m;

	int blue = 0;
	int red = 0;

	int bluecp = 0;
	int redcp = 0;

	int winscore = 50;

	boolean cteam = true;

	ArrayList<CheckPoint> cps = new ArrayList<CheckPoint>();

	public IArena(Main m, String arena_id) {
		super(m, arena_id, ArenaType.DEFAULT);
		this.m = m;
		this.loadCheckpoints();
	}

	public boolean addBluePoints() {
		blue++;
		if (blue > 100) {
			for (String p_ : this.getAllPlayers()) {
				if (m.pteam.containsKey(p_)) {
					if (m.pteam.get(p_).equalsIgnoreCase("red")) {
						MinigamesAPI.getAPI().pinstances.get(m).global_lost.put(p_, this);
					}
				}
			}
			this.stop();
			return true;
		}
		return false;
	}

	public boolean addRedPoints() {
		red++;
		if (red > 100) {
			for (String p_ : this.getAllPlayers()) {
				if (m.pteam.containsKey(p_)) {
					if (m.pteam.get(p_).equalsIgnoreCase("blue")) {
						MinigamesAPI.getAPI().pinstances.get(m).global_lost.put(p_, this);
					}
				}
			}
			this.stop();
			return true;
		}
		return false;
	}

	@Override
	public void joinPlayerLobby(String playername) {
		super.joinPlayerLobby(playername);
		if (cteam) {
			m.pteam.put(playername, "red");
			cteam = false;
		} else {
			m.pteam.put(playername, "blue");
			cteam = true;
		}
	}

	@Override
	public void spectate(String playername) {
		// TODO tp to spawn back
	}

	BukkitTask tt;
	int currentingamecount;

	@Override
	public void start(boolean tp) {
		red = 0;
		blue = 0;
		final IArena a = this;

		for (String p_ : a.getArena().getAllPlayers()) {
			Player p = Bukkit.getPlayer(p_);
			if (m.pteam.get(p_).equalsIgnoreCase("red")) {
				Util.teleportPlayerFixed(p, a.getSpawns().get(0));
			} else if (m.pteam.get(p_).equalsIgnoreCase("blue")) {
				Util.teleportPlayerFixed(p, a.getSpawns().get(1));
			}
		}

		super.start(false);

		tt = Bukkit.getScheduler().runTaskTimer(m, new Runnable() {
			public void run() {
				ArrayList<String> temp = new ArrayList<String>(a.getAllPlayers());
				for (String p_ : temp) {
					Player p = Bukkit.getPlayer(p_);
					CheckPoint c = isInCP(p);
					if (c != null) {
						c.evaluate(m.pteam.get(p.getName()));
					}
				}
			}
		}, 5L, 5L);

		m.scoreboard.updateScoreboard(this);
	}

	@Override
	public void started() {
		final IArena a = this;
		Bukkit.getScheduler().runTaskLater(m, new Runnable() {
			public void run() {
				for (String p_ : a.getAllPlayers()) {
					m.addGear(p_);
				}
			}
		}, 20L);
	}

	@Override
	public void stop() {
		if(tt != null){
			tt.cancel();
		}
		super.stop();
		resetCPs();
		this.redcp = 0;
		this.bluecp = 0;
		this.red = 0;
		this.blue = 0;
	}

	public void loadCheckpoints() {
		FileConfiguration config = MinigamesAPI.getAPI().pinstances.get(m).getArenasConfig().getConfig();
		if (config.isSet("arenas." + this.getName() + ".checkpoints.")) {
			for (String cp : config.getConfigurationSection("arenas." + this.getName() + ".checkpoints.").getKeys(false)) {
				CheckPoint cp_ = new CheckPoint(m, this, Util.getComponentForArena(m, this.getName(), "checkpoints." + cp));
				cps.add(cp_);
			}
		}
	}

	public CheckPoint isInCP(Player p) {
		for (CheckPoint cp : this.cps) {
			Location l = p.getLocation();
			Location l2 = cp.getCenter();
			if ((Math.abs(l.getBlockX() - l2.getBlockX()) < 3) && (Math.abs(l.getBlockZ() - l2.getBlockZ()) < 3)) {
				return cp;
			}
		}
		return null;
	}

	public void resetCPs() {
		for (CheckPoint cp : cps) {
			Location l = cp.getCenter();
			for (int x = -2; x < 3; x++) {
				for (int z = -2; z < 3; z++) {
					l.clone().add(x, 0D, z).getBlock().setTypeIdAndData(35, (byte) 0, true);
				}
			}
			cp.red = 0;
			cp.blue = 0;
			cp.cx_b = -2;
			cp.cz_b = -2;
			cp.cx_r = -2;
			cp.cz_r = -2;
			cp.wasblue = false;
			cp.wasred = false;
		}
	}

}
