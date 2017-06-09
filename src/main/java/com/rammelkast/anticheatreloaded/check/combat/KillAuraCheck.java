package com.rammelkast.anticheatreloaded.check.combat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.rammelkast.anticheatreloaded.AntiCheat;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.event.EventListener;
import com.rammelkast.anticheatreloaded.util.NPC_1_11;
import com.rammelkast.anticheatreloaded.util.VersionUtil;

/**
 * @author Rammelkast
 */
public class KillAuraCheck {
	
	private static final Map<UUID, NPC_1_11> NPC_LIST = new HashMap<UUID, NPC_1_11>();
	private static final Map<UUID, Integer> VL_COUNT = new HashMap<UUID, Integer>();
	
	public static void cleanPlayer(Player p) {
		if (!VersionUtil.getVersion().equals("v1_11_R1")) {
			return;
		}
		NPC_LIST.remove(p.getUniqueId());
	}
	
	public static void listenPackets() {
		if (!VersionUtil.getVersion().equals("v1_11_R1")) {
			return;
		}
		AntiCheat.getProtocolManager().addPacketListener(new PacketAdapter(AntiCheat.getPlugin(), ListenerPriority.NORMAL, new PacketType[] {PacketType.Play.Client.USE_ENTITY}) {
			@Override
			public void onPacketReceiving(PacketEvent e) {
				Player p = e.getPlayer();
				if (!NPC_LIST.containsKey(p.getUniqueId()))
					return;
				int id = e.getPacket().getIntegers().read(0);
				if (NPC_LIST.containsKey(p.getUniqueId())) {
					if (NPC_LIST.get(p.getUniqueId()).getID() != id) {
						return;
					}
				}
				if (!VL_COUNT.containsKey(p.getUniqueId())) {
					VL_COUNT.put(p.getUniqueId(), 1);
					NPC_LIST.get(p.getUniqueId()).damage();
				}else {
					VL_COUNT.put(p.getUniqueId(), VL_COUNT.get(p.getUniqueId()) + 1);
					if (VL_COUNT.get(p.getUniqueId()) >= 5) {
						AntiCheat.sendToStaff(ChatColor.RED + player.getName() + " failed KillAura!");
						EventListener.log(new CheckResult(CheckResult.Result.FAILED, p.getName() + " failed KillAura (botcheck), hit the bot " + VL_COUNT.get(p.getUniqueId()) + " times (max=5)").getMessage(), p, CheckType.KILLAURA);
						AntiCheat.getPlugin().onKillAuraViolation();
						VL_COUNT.remove(p.getUniqueId());
					}
				}
			}
		});
	}

	public static void doDamageEvent(EntityDamageByEntityEvent e, Player p) {
		if (!VersionUtil.getVersion().equals("v1_11_R1")) {
			return;
		}
		if (NPC_LIST.containsKey(p.getUniqueId()))
			NPC_LIST.get(p.getUniqueId()).damage(e, p);
		else {
			NPC_1_11 npc = new NPC_1_11(p);
			NPC_LIST.put(p.getUniqueId(), npc);
			npc.spawn();
		}
	}
	
	public static void doMove(PlayerMoveEvent e) {
		if (!VersionUtil.getVersion().equals("v1_11_R1")) {
			return;
		}
		if (NPC_LIST.containsKey(e.getPlayer().getUniqueId())) {
			NPC_LIST.get(e.getPlayer().getUniqueId()).move(e);
		}
	}
	
}
