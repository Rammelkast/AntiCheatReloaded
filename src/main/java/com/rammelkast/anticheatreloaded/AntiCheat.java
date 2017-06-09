/*
 * AntiCheat for Bukkit.
 * Copyright (C) 2012-2014 AntiCheat Team | http://gravitydevelopment.net
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.rammelkast.anticheatreloaded;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.rammelkast.anticheatreloaded.check.combat.KillAuraCheck;
import com.rammelkast.anticheatreloaded.check.movement.BlinkCheck;
import com.rammelkast.anticheatreloaded.command.CommandHandler;
import com.rammelkast.anticheatreloaded.config.Configuration;
import com.rammelkast.anticheatreloaded.event.BlockListener;
import com.rammelkast.anticheatreloaded.event.EntityListener;
import com.rammelkast.anticheatreloaded.event.InventoryListener;
import com.rammelkast.anticheatreloaded.event.PlayerListener;
import com.rammelkast.anticheatreloaded.event.VehicleListener;
import com.rammelkast.anticheatreloaded.manage.AntiCheatManager;
import com.rammelkast.anticheatreloaded.metrics.Metrics;
import com.rammelkast.anticheatreloaded.util.User;
import com.rammelkast.anticheatreloaded.util.VersionUtil;

public class AntiCheat extends JavaPlugin {

	private static AntiCheatManager manager;
	private static AntiCheat plugin;
	private static List<Listener> eventList = new ArrayList<Listener>();
	private static Configuration config;
	private static boolean verbose;
	private static boolean developer;
	private static ProtocolManager protocolManager;
	private static Long loadTime;
	private static List<Player> onlineStaff = new ArrayList<Player>();
	
	@Override
	public void onEnable() {
		plugin = this;
		loadTime = System.currentTimeMillis();
		manager = new AntiCheatManager(this, getLogger());
		eventList.add(new PlayerListener());
		eventList.add(new BlockListener());
		eventList.add(new EntityListener());
		eventList.add(new VehicleListener());
		eventList.add(new InventoryListener());
		// Order is important in some cases, don't screw with these unless
		// needed, especially config
		setupConfig();
		setupEvents();
		setupCommands();
		// Enterprise must come before levels
		setupEnterprise();
		restoreLevels();
		
		if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
			setupProtocol();
		} else {
			getLogger().severe("Shutting down, ProtocolLib not found!");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		
		getLogger().info("Found ProtocolLib, enabling checks that use ProtcolLib...");
		// Enable packetlisteners
		KillAuraCheck.listenPackets();
		BlinkCheck.startTimer();
		BlinkCheck.listenPackets();
		
		// Check if other AC's are installed
		Bukkit.getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				if (Bukkit.getPluginManager().getPlugin("NoCheatPlus") != null) {
					getLogger().severe("*----------------------------------------------*");
					getLogger().severe("You are also running NoCheatPlus!");
					getLogger().severe("Multiple anticheats create false cheat detections.");
					getLogger().severe("Please remove or disable NoCheatPlus to silence this warning.");
					getLogger().severe("*----------------------------------------------*");
				}
				if (Bukkit.getPluginManager().getPlugin("AAC") != null) {
					getLogger().severe("*----------------------------------------------*");
					getLogger().severe("You are also running AAC!");
					getLogger().severe("Multiple anticheats create false cheat detections.");
					getLogger().severe("Please remove or disable AAC to silence this warning.");
					getLogger().severe("*----------------------------------------------*");
				}
				if (Bukkit.getPluginManager().getPlugin("AntiCheat") != null) {
					getLogger().severe("*----------------------------------------------*");
					getLogger().severe("You are also running AntiCheat!");
					getLogger().severe("Multiple anticheats create false cheat detections.");
					getLogger().severe("Please remove or disable AntiCheat to silence this warning.");
					getLogger().severe("*----------------------------------------------*");
				}
			}
		}, 60L);
		
		// End tests
		verboseLog("Finished loading.");
		getLogger().info("Running NMS version " + VersionUtil.getVersion() + "...");

		// Metrics
		try {
			Metrics metrics = new Metrics(this);
			metrics.addCustomChart(new Metrics.SingleLineChart("cheaters_kicked") {
				@Override
				public int getValue() {
					int kicked = playersKicked;
					// Reset so we don't keep sending the same value
					playersKicked = 0;
					return kicked;
				}
			});
			metrics.addCustomChart(new Metrics.SingleLineChart("killaura_violations") {
				@Override
				public int getValue() {
					int violations = killauraViolations;
					// Reset so we don't keep sending the same value
					killauraViolations = 0;
					return violations;
				}
			});
			metrics.addCustomChart(new Metrics.SingleLineChart("glide_violations") {
				@Override
				public int getValue() {
					int violations = glideViolations;
					// Reset so we don't keep sending the same value
					glideViolations = 0;
					return violations;
				}
			});
			metrics.addCustomChart(new Metrics.SimplePie("protocollib_version") {
				@Override
				public String getValue() {
					return Bukkit.getPluginManager().getPlugin("ProtocolLib").getDescription().getVersion();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDisable() {
		verboseLog("Saving user levels...");
		config.getLevels().saveLevelsFromUsers(getManager().getUserManager().getUsers());

		AntiCheatManager.close();
		getServer().getScheduler().cancelTasks(this);
		cleanup();
	}

	private void setupProtocol() {
		protocolManager = ProtocolLibrary.getProtocolManager();
		verboseLog("Hooked into ProtocolLib");
	}

	private void setupEvents() {
		for (Listener listener : eventList) {
			getServer().getPluginManager().registerEvents(listener, this);
			verboseLog("Registered events for ".concat(listener.toString().split("@")[0].split(".anticheat.")[1]));
		}
	}

	private void setupCommands() {
		getCommand("anticheat").setExecutor(new CommandHandler());
		verboseLog("Registered commands.");
	}

	private void setupConfig() {
		config = manager.getConfiguration();
		verboseLog("Setup the config.");
	}

	private void setupEnterprise() {
		if (config.getConfig().enterprise.getValue()) {
			if (config.getEnterprise().loggingEnabled.getValue()) {
				config.getEnterprise().database.cleanEvents();
			}
		}
	}

	private void restoreLevels() {
		for (Player player : getServer().getOnlinePlayers()) {
			UUID uuid = player.getUniqueId();

			User user = new User(uuid);
			user.setIsWaitingOnLevelSync(true);
			config.getLevels().loadLevelToUser(user);

			manager.getUserManager().addUser(user);
			verboseLog("Data for " + uuid + " loaded");
		}
	}

	public static AntiCheat getPlugin() {
		return plugin;
	}

	public static AntiCheatManager getManager() {
		return manager;
	}

	public static String getVersion() {
		return manager.getPlugin().getDescription().getVersion();
	}

	private void cleanup() {
		eventList = null;
		manager = null;
		config = null;
	}

	public static boolean developerMode() {
		return developer;
	}

	public static void setDeveloperMode(boolean b) {
		developer = b;
	}

	public static void debugLog(final String string) {
		Bukkit.getScheduler().runTask(getPlugin(), new Runnable() {
			public void run() {
				if (developer) {
					manager.debugLog("[DEBUG] " + string);
				}
			}
		});
	}

	public void verboseLog(final String string) {
		if (verbose) {
			getLogger().info(string);
		}
	}

	public void setVerbose(boolean b) {
		verbose = b;
	}

	public Long getLoadTime() {
		return loadTime;
	}

	public static ProtocolManager getProtocolManager() {
		return protocolManager;
	}

	/**
	 * Amount of players kicked since start
	 */
	private int playersKicked = 0;
	/**
	 * Amount of killaura violations since start
	 */
	private int killauraViolations = 0;
	/**
	 * Amount of glide violations since start
	 */
	private int glideViolations = 0;

	public void onPlayerKicked() {
		this.playersKicked++;
	}

	public void onKillAuraViolation() {
		this.killauraViolations++;
	}

	public void onGlideViolation() {
		this.killauraViolations++;
	}

	public static void addToStaff(Player player) {
		onlineStaff.add(player);
	}
	
	public static void sendToStaff(String message) {
		for(Player player : onlineStaff) {
			player.sendMessage(message);
		}
	}
	
}
