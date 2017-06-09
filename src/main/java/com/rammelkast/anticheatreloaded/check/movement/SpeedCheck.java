package com.rammelkast.anticheatreloaded.check.movement;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.rammelkast.anticheatreloaded.AntiCheat;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.util.VersionUtil;

public class SpeedCheck {

	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);
	public static Map<UUID, Integer> speedViolation = new HashMap<UUID, Integer>();

	public static boolean isSpeedExempt(Player player) {
		return AntiCheat.getManager().getBackend().isMovingExempt(player)
				|| AntiCheat.getManager().getBackend().justVelocity(player) || VersionUtil.isFlying(player);
	}

	public static CheckResult checkXZSpeed(Player player, double x, double z) {
		if (!isSpeedExempt(player) && player.getVehicle() == null) {
			String reason = "";
			double max = AntiCheat.getManager().getBackend().getMagic().XZ_SPEED_MAX();
			if (player.getLocation().getBlock().getType() == Material.SOUL_SAND) {
				if (player.isSprinting()) {
					reason = "on soulsand while sprinting ";
					max = AntiCheat.getManager().getBackend().getMagic().XZ_SPEED_MAX_SOULSAND_SPRINT();
				} else if (player.hasPotionEffect(PotionEffectType.SPEED)) {
					reason = "on soulsand with speed potion ";
					max = AntiCheat.getManager().getBackend().getMagic().XZ_SPEED_MAX_SOULSAND_POTION();
				} else {
					reason = "on soulsand ";
					max = AntiCheat.getManager().getBackend().getMagic().XZ_SPEED_MAX_SOULSAND();
				}
			} else if (VersionUtil.isFlying(player)) {
				reason = "while flying ";
				max = AntiCheat.getManager().getBackend().getMagic().XZ_SPEED_MAX_FLY();
			} else if (player.hasPotionEffect(PotionEffectType.SPEED)) {
				if (player.isSprinting()) {
					reason = "with speed potion while sprinting ";
					max = AntiCheat.getManager().getBackend().getMagic().XZ_SPEED_MAX_POTION_SPRINT();
				} else {
					reason = "with speed potion ";
					max = AntiCheat.getManager().getBackend().getMagic().XZ_SPEED_MAX_POTION();
				}
			} else if (player.isSprinting()) {
				reason = "while sprinting ";
				max = AntiCheat.getManager().getBackend().getMagic().XZ_SPEED_MAX_SPRINT();
			}

			float speed = player.getWalkSpeed();
			max += speed > 0 ? player.getWalkSpeed() - 0.2f : 0;

			if (x > max || z > max) {
				int num = AntiCheat.getManager().getBackend().increment(player, speedViolation,
						AntiCheat.getManager().getBackend().getMagic().SPEED_MAX());
				if (num >= AntiCheat.getManager().getBackend().getMagic().SPEED_MAX()) {
					AntiCheat.sendToStaff(ChatColor.RED + player.getName() + " was too fast " + reason + "!");
					return new CheckResult(CheckResult.Result.FAILED,
							player.getName() + "'s speed was too high " + reason + num + " times in a row (max="
									+ AntiCheat.getManager().getBackend().getMagic().SPEED_MAX() + ", speed="
									+ (x > z ? x : z) + ", max speed=" + max + ")");
				} else {
					return PASS;
				}
			} else {
				speedViolation.put(player.getUniqueId(), 0);
				return PASS;
			}
		} else {
			return PASS;
		}
	}

}
