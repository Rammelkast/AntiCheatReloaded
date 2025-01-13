/*
 * AntiCheatReloaded for Bukkit and Spigot.
 * Copyright (c) 2024 Rammelkast
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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.EventManager;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.rammelkast.anticheatreloaded.client.ClientManager;
import com.rammelkast.anticheatreloaded.protocol.PacketLoginLogoutListener;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;

public final class AntiCheatReloaded extends JavaPlugin {

	/**
	 * General message prefix
	 */
	public static final String MESSAGE_PREFIX = ChatColor.GOLD + "" + ChatColor.BOLD + "ACR " + ChatColor.DARK_GRAY + "> "
			+ ChatColor.GRAY;
	/**
	 * List of supported NMS versions
	 */
	private static final List<String> SUPPORTED_VERSIONS = Arrays.asList(new String[] {
		// 1.21.4
		"1.21.4/R0.1",
	});
	/**
	 * Plugin Id for bStats metrics
	 */
	private static final int METRICS_ID = 202;
	
	/**
	 * Plugin instance
	 */
	@Getter
	private static AntiCheatReloaded instance;

	/**
	 * bStats metrics handler
	 */
	private Metrics metrics;
	/**
	 * If Floodgate/Geyser is available on the server
	 */
	@Getter
	private static boolean usingFloodgateSupport;

	@Getter
	private ClientManager clientManager;
	
	@Override
	public void onLoad() {
		instance = this;

		this.clientManager = new ClientManager();

		// Load our PacketEvents
		loadPacketEvents();

		// Check if Floodgate is available on the server
		if (Bukkit.getPluginManager().getPlugin("Floodgate") != null) {
			usingFloodgateSupport = true;
			sendPrefixedMessage("Floodgate support enabled");
		}
	}

	/**
	 * Loads PacketEvents and registers our packet listeners
	 */
	private void loadPacketEvents() {
		PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
		PacketEvents.getAPI().getSettings().checkForUpdates(false);
		PacketEvents.getAPI().load();

		final EventManager eventManager = PacketEvents.getAPI().getEventManager();
		eventManager.registerListener(new PacketLoginLogoutListener(), PacketListenerPriority.MONITOR);
	}
	
	@Override
	public void onEnable() {
		// Make sure we are running a compatible version
		if (!checkVersionCompatibility()) {
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		// Enable client manager
		this.clientManager.enable(this);

		// Initialize PacketEvents
		PacketEvents.getAPI().init();
		
		// Create bStats metrics handler
		getServer().getScheduler().runTaskAsynchronously(instance, () -> {
			this.metrics = new Metrics(this, METRICS_ID);

			this.metrics.addCustomChart(new SimplePie("floodgate_enabled", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return usingFloodgateSupport ? "Yes" : "No";
				}
			}));
			
			this.metrics.addCustomChart(new SimplePie("nms_version", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return getServer().getBukkitVersion().replace('-', '/').replaceAll("/SNAPSHOT", "");
				}
			}));
		});

		sendPrefixedMessage("Startup complete.");
	}
	
	@Override
	public void onDisable() {
		// Shut down metrics
		if (this.metrics != null) {
			this.metrics.shutdown();;
		}

		// Cancel all running tasks
		getServer().getScheduler().cancelTasks(this);

		// Terminate PacketEvents
		PacketEvents.getAPI().terminate();

		// Terminate client manager
		this.clientManager.terminate(this);
		
		// Nullify all non-final objects
		nullify();
	}
	
	/**
	 * Checks if this plugin version is compatible with the server version
	 * @return true if compatible - false otherwise
	 */
	private boolean checkVersionCompatibility() {
		final String versionPackage = getServer().getBukkitVersion().replace('-', '/').replaceAll("/SNAPSHOT", "");

		final boolean supported = SUPPORTED_VERSIONS.contains(versionPackage);
		if (!supported) {
			sendPrefixedMessage(ChatColor.DARK_RED + "This version of AntiCheatReloaded does not support Minecraft "
					+ versionPackage + "! Shutting down.");
		} else {
			sendPrefixedMessage("Running on Minecraft " + versionPackage);
		}

		return supported;
	}

	/**
	 * Nullifies all non-final objects
	 */
	private void nullify() {
		instance = null;
		
		this.metrics = null;
	}

	/**
	 * Sends a message to the console, prepended with the plugin prefix
	 * @param message The message
	 */
	private void sendPrefixedMessage(final String message) {
		Bukkit.getConsoleSender().sendMessage(MESSAGE_PREFIX + message);
	}

}
