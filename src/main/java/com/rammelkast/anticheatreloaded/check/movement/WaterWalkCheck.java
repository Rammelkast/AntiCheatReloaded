package com.rammelkast.anticheatreloaded.check.movement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.rammelkast.anticheatreloaded.AntiCheat;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.util.Utilities;

/**
 * This check seems to be broken/outdated
 */
public class WaterWalkCheck {

	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);

	public static List<UUID> isInWater = new ArrayList<UUID>();
	public static List<UUID> isInWaterCache = new ArrayList<UUID>();
	public static Map<UUID, Integer> waterSpeedViolation = new HashMap<UUID, Integer>();
	public static Map<UUID, Integer> waterAscensionViolation = new HashMap<UUID, Integer>();

	/**
	 * TODO check & fix for 1.11? (see #25)
	 */
	public static CheckResult runCheck(Player player, double x, double y, double z) {
		Block block = player.getLocation().getBlock();
		UUID uuid = player.getUniqueId();

		if (player.getVehicle() == null && !player.isFlying()) {
			if (block.isLiquid()) {
				if (isInWater.contains(uuid)) {
					if (isInWaterCache.contains(uuid)) {
						if (player.getNearbyEntities(1, 1, 1).isEmpty()) {
							boolean b;
							if (!Utilities.sprintFly(player)) {
								b = x > AntiCheat.getManager().getBackend().getMagic().XZ_SPEED_MAX_WATER()
										|| z > AntiCheat.getManager().getBackend().getMagic().XZ_SPEED_MAX_WATER();
							} else {
								b = x > AntiCheat.getManager().getBackend().getMagic().XZ_SPEED_MAX_WATER_SPRINT()
										|| z > AntiCheat.getManager().getBackend().getMagic()
												.XZ_SPEED_MAX_WATER_SPRINT();
							}
							if (!b && !Utilities.isFullyInWater(player.getLocation())
									&& Utilities.isHoveringOverWater(player.getLocation(), 1) && y == 0D
									&& !block.getType().equals(Material.WATER_LILY)) {
								b = true;
							}
							if (b) {
								if (waterSpeedViolation.containsKey(uuid)) {
									int v = waterSpeedViolation.get(uuid);
									if (v >= AntiCheat.getManager().getBackend().getMagic()
											.WATER_SPEED_VIOLATION_MAX()) {
										waterSpeedViolation.put(uuid, 0);
										AntiCheat.sendToStaff(ChatColor.RED + player.getName() + " failed WaterWalk!");
										return new CheckResult(CheckResult.Result.FAILED,
												player.getName() + " stood on water " + v + " times (can't stand on "
														+ block.getType() + " or "
														+ block.getRelative(BlockFace.DOWN).getType() + ")");
									} else {
										waterSpeedViolation.put(uuid, v + 1);
									}
								} else {
									waterSpeedViolation.put(uuid, 1);
								}
							}
						}
					} else {
						isInWaterCache.add(uuid);
						return PASS;
					}
				} else {
					isInWater.add(uuid);
					return PASS;
				}
			} else if (block.getRelative(BlockFace.DOWN).isLiquid()
					&& !AntiCheat.getManager().getBackend().isAscending(player) && Utilities.cantStandAt(block)
					&& Utilities.cantStandAt(block.getRelative(BlockFace.DOWN))) {
				if (waterAscensionViolation.containsKey(uuid)) {
					int v = waterAscensionViolation.get(uuid);
					if (v >= AntiCheat.getManager().getBackend().getMagic().WATER_ASCENSION_VIOLATION_MAX()) {
						waterAscensionViolation.put(uuid, 0);
						return new CheckResult(CheckResult.Result.FAILED,
								player.getName() + " stood on water " + v + " times (can't stand on " + block.getType()
										+ " or " + block.getRelative(BlockFace.DOWN).getType() + ")");
					} else {
						waterAscensionViolation.put(uuid, v + 1);
					}
				} else {
					waterAscensionViolation.put(uuid, 1);
				}
			} else {
				isInWater.remove(uuid);
				isInWaterCache.remove(uuid);
			}
		}
		return PASS;
	}

}
