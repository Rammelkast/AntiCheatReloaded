package com.rammelkast.anticheatreloaded.check.movement;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.rammelkast.anticheatreloaded.AntiCheat;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.util.Distance;
import com.rammelkast.anticheatreloaded.util.Utilities;
import com.rammelkast.anticheatreloaded.util.VersionUtil;

/**
 * @author Rammelkast
 * TODO rewrite this check
 */
public class FlightCheck {

	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);

	public static Map<UUID, Double> blocksOverFlight = new HashMap<UUID, Double>();
	public static Map<UUID, Long> movingExempt = new HashMap<UUID, Long>();

	public static CheckResult runCheck(Player player, Distance distance) {
		if (distance.getYDifference() > AntiCheat.getManager().getBackend().getMagic().TELEPORT_MIN()
				|| VersionUtil.isFlying(player)) {
			// This was a teleport or user is flying/using elytra, so we don't care
			// about it.
			return PASS;
		}
		final UUID uuid = player.getUniqueId();
		final double y1 = distance.fromY();
		final double y2 = distance.toY();
		if (!isMovingExempt(player) && !Utilities.isHoveringOverWater(player.getLocation(), 1)
				&& Utilities.cantStandAtExp(player.getLocation())
				&& Utilities.blockIsnt(player.getLocation().getBlock().getRelative(BlockFace.DOWN),
						new Material[] { Material.FENCE, Material.FENCE_GATE, Material.COBBLE_WALL })) {

			if (!blocksOverFlight.containsKey(uuid)) {
				blocksOverFlight.put(uuid, 0D);
			}

			blocksOverFlight.put(uuid, (blocksOverFlight.get(uuid) + distance.getXDifference()
					+ distance.getYDifference() + distance.getZDifference()));

			if (y1 > y2) {
				blocksOverFlight.put(uuid, (blocksOverFlight.get(uuid) - distance.getYDifference()));
			}

			if (blocksOverFlight.get(uuid) > AntiCheat.getManager().getBackend().getMagic().FLIGHT_BLOCK_LIMIT()
					&& (y1 <= y2)) {
				AntiCheat.sendToStaff(ChatColor.RED + player.getName() + " failed Fly!");
				return new CheckResult(CheckResult.Result.FAILED,
						player.getName() + " flew over " + blocksOverFlight.get(uuid) + " blocks (max="
								+ AntiCheat.getManager().getBackend().getMagic().FLIGHT_BLOCK_LIMIT() + ")");
			}
		} else {
			blocksOverFlight.put(uuid, 0D);
		}

		return PASS;
	}

	public static boolean isMovingExempt(Player player) {
		return isDoing(player, movingExempt, -1);
	}

	private static boolean isDoing(Player player, Map<UUID, Long> map, double max) {
		if (map.containsKey(player.getUniqueId())) {
			if (max != -1) {
				if (((System.currentTimeMillis() - map.get(player.getUniqueId())) / 1000) > max) {
					map.remove(player.getUniqueId());
					return false;
				} else {
					return true;
				}
			} else {
				// Termination time has already been calculated
				if (map.get(player.getUniqueId()) < System.currentTimeMillis()) {
					map.remove(player.getUniqueId());
					return false;
				} else {
					return true;
				}
			}
		} else {
			return false;
		}
	}

}
