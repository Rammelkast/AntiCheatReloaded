package com.rammelkast.anticheatreloaded.client.wrapper;

import com.github.retrooper.packetevents.protocol.player.User;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ClientWrapper {

    @Getter
    private final User user;
    @Getter
    private final UUID uniqueId;

    @Getter
    private Player player;

    public ClientWrapper(final User user, final Player player) {
        this.user = user;
        this.uniqueId = user.getUUID();

        this.player = player;
    }
}
