package com.rammelkast.anticheatreloaded.check.movement;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.rammelkast.anticheatreloaded.AntiCheat;
import com.rammelkast.anticheatreloaded.check.Backend;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.util.Distance;
import com.rammelkast.anticheatreloaded.util.Utilities;
import com.rammelkast.anticheatreloaded.util.VersionUtil;

public class GlideCheck {

	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);
	
	public static Map<UUID, Double> lastDiff = new HashMap<UUID, Double>();
	public static Map<UUID, Float> lastFallDistance = new HashMap<UUID, Float>();
	public static Map<UUID, Integer> violations = new HashMap<UUID, Integer>();
	
	public static CheckResult runCheck(Player player, Distance distance) {
		if (VersionUtil.isFlying(player)) {
			return PASS;
		}
		if (!lastDiff.containsKey(player.getUniqueId())) {
			lastDiff.put(player.getUniqueId(), distance.getYDifference());
			return PASS;
		}
		if (!lastFallDistance.containsKey(player.getUniqueId())) {
			lastFallDistance.put(player.getUniqueId(), player.getFallDistance());
			return PASS;
		}
		double yDiff = distance.getYDifference();
		float fallDistance = player.getFallDistance();
		if (yDiff == lastDiff.get(player.getUniqueId()) && fallDistance > lastFallDistance.get(player.getUniqueId())) {
			if (!violations.containsKey(player.getUniqueId())) {
				violations.put(player.getUniqueId(), 1);
			} else {
				if (violations.get(player.getUniqueId()) + 1 >= AntiCheat.getManager().getBackend().getMagic().GLIDE_LIMIT()) {
					violations.remove(player.getUniqueId());
					Location to = player.getLocation();
					to.setY(to.getY() - distanceToFall(to));
					player.teleport(to);
					// Report glide violation to statistics, just for the lulz jk
					AntiCheat.getPlugin().onGlideViolation();
					AntiCheat.sendToStaff(ChatColor.RED + player.getName() + " failed Glide!");
					return new CheckResult(CheckResult.Result.FAILED,
							player.getName() + " was set back for gliding (yDiff=" + new BigDecimal(yDiff).setScale(2, BigDecimal.ROUND_UP) + ")");
				} else {
					violations.put(player.getUniqueId(), violations.get(player.getUniqueId()) + 1);
				}
			}
		}
		lastDiff.put(player.getUniqueId(), distance.getYDifference());
		lastFallDistance.put(player.getUniqueId(), player.getFallDistance());
    	return PASS;
	}

	private static double distanceToFall(Location location) {
		location = location.clone();
		double firstY = location.getY();
		while (location.clone().add(0, -0.1, 0).getBlock().getType() == Material.AIR)
			location.add(0, -0.1, 0);
		return firstY - location.getY();
	}

}
