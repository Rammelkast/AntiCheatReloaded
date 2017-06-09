package com.rammelkast.anticheatreloaded.check.movement;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.rammelkast.anticheatreloaded.AntiCheat;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.event.EventListener;

/**
 * @author Rammelkast
 */
public class BlinkCheck {

	public static final Map<UUID, Integer> MOVE_COUNT = new HashMap<UUID, Integer>();
	
	public static void startTimer() {
		new BukkitRunnable() {
			@Override
			public void run() {
				MOVE_COUNT.clear();
			}
		}.runTaskTimer(AntiCheat.getPlugin(), 20, 20);
	}
	
	public static void listenPackets() {
		AntiCheat.getProtocolManager().addPacketListener(new PacketAdapter(AntiCheat.getPlugin(), ListenerPriority.NORMAL, new PacketType[] {PacketType.Play.Client.POSITION, PacketType.Play.Client.POSITION_LOOK}) {
			@Override
			public void onPacketReceiving(PacketEvent e) {
				Player p = e.getPlayer();
				Location cur = e.getPlayer().getLocation();
				if (!MOVE_COUNT.containsKey(p.getUniqueId())) {
					MOVE_COUNT.put(p.getUniqueId(), 1);
				} else {
					MOVE_COUNT.put(p.getUniqueId(), MOVE_COUNT.get(p.getUniqueId()) + 1);
					if (AntiCheat.getManager().getCheckManager().checkInWorld(p) && !AntiCheat.getManager().getCheckManager().isOpExempt(p) && !AntiCheat.getManager().getCheckManager().isExempt(p, CheckType.BLINK)) {
						if (MOVE_COUNT.get(p.getUniqueId()) > AntiCheat.getManager().getBackend().getMagic().BLINK_PACKET()) {
							AntiCheat.sendToStaff(ChatColor.RED + player.getName() + " failed Blink!");
							EventListener.log(new CheckResult(CheckResult.Result.FAILED, p.getName() + " failed Blink, sent " + MOVE_COUNT.get(p.getUniqueId()) + " packets in one second (max=" + AntiCheat.getManager().getBackend().getMagic().BLINK_PACKET() + ")").getMessage(), p, CheckType.BLINK);
							MOVE_COUNT.remove(p.getUniqueId());
							e.setCancelled(true);
							e.getPlayer().teleport(cur);
						}
					}
				}
			}
		});
	}
	
}
