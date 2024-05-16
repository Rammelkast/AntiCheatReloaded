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

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import com.cryptomorin.xseries.ReflectionUtils;

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
		// 1.20.x
		"1.20.R1",
		"1.20.R2",
		"1.20.R3"
	});
	/**
	 * Plugin Id for bStats metrics
	 */
	private static final int METRICS_ID = 202;
	
	/**
	 * Plugin instance
	 */
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
	
	@Override
	public void onLoad() {
		instance = this;
		
		// Check if ProtocolLib is available on the server
		if (Bukkit.getPluginManager().getPlugin("ProtocolLib") == null) {
			Bukkit.getConsoleSender().sendMessage(MESSAGE_PREFIX + ChatColor.RED
					+ "Could not find ProtocolLib! AntiCheatReloaded requires ProtocolLib to work, please download and install it.");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		
		// Check if Floodgate is available on the server
		if (Bukkit.getPluginManager().getPlugin("Floodgate") != null) {
			usingFloodgateSupport = true;
			Bukkit.getConsoleSender().sendMessage(MESSAGE_PREFIX + ChatColor.WHITE + "Floodgate support enabled");
		}
	}
	
	@Override
	public void onEnable() {
		// Make sure we are running a compatible version
		if (!checkVersionCompatibility()) {
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		
		// Create bStats metrics handler
		getServer().getScheduler().runTaskAsynchronously(instance, () -> {
			this.metrics = new Metrics(this, METRICS_ID);
			
			this.metrics.addCustomChart(new SimplePie("protocollib_version", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return Bukkit.getPluginManager().getPlugin("ProtocolLib").getDescription().getVersion();
				}
			}));
			
			this.metrics.addCustomChart(new SimplePie("floodgate_enabled", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return usingFloodgateSupport ? "Yes" : "No";
				}
			}));
			
			this.metrics.addCustomChart(new SimplePie("nms_version", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return ReflectionUtils.findNMSVersionString().substring(1).replace('_', '.');
				}
			}));
		});
	}
	
	@Override
	public void onDisable() {
		// Cancel all running tasks
		getServer().getScheduler().cancelTasks(this);
		
		// Nullify all non-final objects
		nullify();
	}
	
	/**
	 * Checks if this plugin version is compatible with the server version
	 * @return true if compatible - false otherwise
	 */
	private boolean checkVersionCompatibility() {
		final String versionPackage = ReflectionUtils.findNMSVersionString().substring(1).replace('_', '.');

		final boolean supported = SUPPORTED_VERSIONS.contains(versionPackage);
		if (!supported) {
			Bukkit.getConsoleSender().sendMessage(MESSAGE_PREFIX + ChatColor.RED + "This version of AntiCheatReloaded does not support Minecraft "
					+ versionPackage.substring(1) + "! Shutting down.");
		} else {
			Bukkit.getConsoleSender().sendMessage(MESSAGE_PREFIX + ("Running on Minecraft " + versionPackage));
		}

		return supported;
	}

	private void nullify() {
		instance = null;
		
		this.metrics = null;
	}
	
}
