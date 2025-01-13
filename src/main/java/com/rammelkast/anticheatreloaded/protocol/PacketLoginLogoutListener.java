package com.rammelkast.anticheatreloaded.protocol;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.UserDisconnectEvent;
import com.github.retrooper.packetevents.event.UserLoginEvent;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.player.User;
import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import org.bukkit.entity.Player;

public final class PacketLoginLogoutListener implements PacketListener {

    private static final AntiCheatReloaded instance = AntiCheatReloaded.getInstance();

    @Override
    public void onUserLogin(final UserLoginEvent event) {
        final User user = event.getUser();
        if (user == null) {
            return;
        }

        final ConnectionState connectionState = user.getConnectionState();
        if (connectionState != ConnectionState.PLAY) {
            return;
        }

        final boolean success = instance.getClientManager().register(user);
    }

    @Override
    public void onUserDisconnect(final UserDisconnectEvent event) {
        final User user = event.getUser();
        if (user == null) {
            return;
        }

        final ConnectionState connectionState = user.getConnectionState();
        if (connectionState != ConnectionState.PLAY) {
            return;
        }

        final boolean existed = instance.getClientManager().destroy(user.getUUID());
    }


}
