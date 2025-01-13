package com.rammelkast.anticheatreloaded.client;

import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import com.github.retrooper.packetevents.protocol.player.User;
import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.client.wrapper.ClientWrapper;
import io.github.retrooper.packetevents.util.GeyserUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientManager {

    private final ConcurrentHashMap<UUID, ClientWrapper> wrapperMap = new ConcurrentHashMap<>();

    public void enable(final Plugin plugin) {

    }

    public void terminate(final Plugin plugin) {

    }

    /**
     * Creates and registers a new {@link ClientWrapper} for this user
     *
     * @param user The user
     * @return the newly created client wrapper object
     */
    public boolean register(final User user) {
        final UUID uniqueId = user.getUUID();
        if (!ChannelHelper.isOpen(user.getChannel()) || uniqueId == null) {
            return false;
        }

        final Player player = Bukkit.getPlayer(uniqueId);
        if (player == null) {
            return false;
        }

        final ClientWrapper wrapper = new ClientWrapper(user, player);
        this.wrapperMap.put(uniqueId, wrapper);
        return true;
    }

    /**
     * Destroys the client wrapper object for this user
     *
     * @param uniqueId The user UUID
     * @return true if the player was registered
     */
    public boolean destroy(final UUID uniqueId) {
        return this.wrapperMap.remove(uniqueId) != null;
    }

    /**
     * Gets if we have a client wrapper object for this user
     *
     * @param user The user
     * @return true if the user is registered
     */
    public boolean has(final User user) {
        return this.wrapperMap.containsKey(user.getUUID());
    }

    /**
     * Gets if we have a client wrapper object for this player
     *
     * @param player The player
     * @return true if the player is registered
     */
    public boolean has(final Player player) {
        return this.wrapperMap.containsKey(player.getUniqueId());
    }

    /**
     * Gets the {@link ClientWrapper} for this user
     *
     * @param user The user
     * @return the client wrapper object
     */
    public ClientWrapper fetch(final User user) {
        if (user == null) {
            return null;
        }

        return this.wrapperMap.get(user.getUUID());
    }

    /**
     * Gets the {@link ClientWrapper} for this player
     *
     * @param player The player
     * @return the client wrapper object
     */
    public ClientWrapper fetch(final Player player) {
        if (player == null) {
            return null;
        }

        return this.wrapperMap.get(player.getUniqueId());
    }


    /**
     * Gets a collection of all {@link ClientWrapper} objects registered
     *
     * @return a collection of client wrappers
     */
    public Collection<ClientWrapper> getWrappers() {
        return Collections.unmodifiableCollection(this.wrapperMap.values());
    }

}
