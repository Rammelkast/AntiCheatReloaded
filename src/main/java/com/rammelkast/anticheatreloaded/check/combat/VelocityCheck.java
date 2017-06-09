package com.rammelkast.anticheatreloaded.check.combat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.rammelkast.anticheatreloaded.AntiCheat;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.event.EventListener;

public class VelocityCheck {

	private static final Map<UUID, Integer> VL_COUNT = new HashMap<UUID, Integer>();
	
	public static void cleanPlayer(Player p) {
		VL_COUNT.remove(p.getUniqueId());
	}
	
	public static void runCheck(EntityDamageByEntityEvent e, final Player p) {
		if (AntiCheat.getManager().getCheckManager().isOpExempt(p) || AntiCheat.getManager().getCheckManager().isExempt(p, CheckType.VELOCITY))
			return;
		final Location then = p.getLocation();
		new BukkitRunnable() {
			@Override
			public void run() {
				if (!p.isOnline())
					return;
				if (then.distance(p.getLocation()) < 0.125) {
					if (!VL_COUNT.containsKey(p.getUniqueId()))
						VL_COUNT.put(p.getUniqueId(), 1);
					else {
						VL_COUNT.put(p.getUniqueId(), VL_COUNT.get(p.getUniqueId()) + 1);
						if (VL_COUNT.get(p.getUniqueId()) > AntiCheat.getManager().getBackend().getMagic().VELOCITY_AMT()) {
							AntiCheat.sendToStaff(ChatColor.RED + player.getName() + " failed Velocity!");
							EventListener.log(new CheckResult(CheckResult.Result.FAILED, p.getName() + " failed Velocity, had zero/low velocity " + VL_COUNT.get(p.getUniqueId()) + " times (max=" + AntiCheat.getManager().getBackend().getMagic().VELOCITY_AMT() + ", dist=" + then.distance(p.getLocation()) + ")").getMessage(), p, CheckType.VELOCITY);
							VL_COUNT.remove(p.getUniqueId());
						}
					}
				}else {
					VL_COUNT.remove(p.getUniqueId());
				}
			}
		}.runTaskLater(AntiCheat.getPlugin(), 4);
	}
	
}
