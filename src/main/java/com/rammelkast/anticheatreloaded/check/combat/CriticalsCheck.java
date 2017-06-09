package com.rammelkast.anticheatreloaded.check.combat;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffectType;

import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.event.EventListener;
import com.rammelkast.anticheatreloaded.util.Utilities;

public class CriticalsCheck {

	public static void doDamageEvent(EntityDamageByEntityEvent e, Player damager) {
		if (!(e.getDamager() instanceof Player) || e.getCause() != DamageCause.ENTITY_ATTACK)
			return;
		Player p = (Player)e.getDamager();
		if (isCritical(p)) {
			if ((p.getLocation().getY() % 1.0 == 0 || p.getLocation().getY() % 0.5 == 0) && p.getLocation().clone().subtract(0, 1.0, 0).getBlock().getType().isSolid()) {
				e.setCancelled(true);
				AntiCheat.sendToStaff(ChatColor.RED + player.getName() + " failed Criticals!");
				EventListener.log(new CheckResult(CheckResult.Result.FAILED, p.getName() + " failed Criticals, tried to do a critical without needed conditions").getMessage(), p, CheckType.CRITICALS);
			}
		}
	}

	private static boolean isCritical(Player player) {
		return player.getFallDistance() > 0.0f && !player.isOnGround() && !player.isInsideVehicle() && !player.hasPotionEffect(PotionEffectType.BLINDNESS) && !Utilities.isHoveringOverWater(player.getLocation()) && player.getEyeLocation().getBlock().getType() != Material.LADDER;
	}

}
