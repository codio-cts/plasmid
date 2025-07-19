package xyz.nucleoid.plasmid.widget;

import net.minecraft.server.network.ServerPlayerEntity;

/**
 * A widget which should be displayed on the GUI for all added players.
 *
 * @see SidebarWidget
 * @see BossBarWidget
 * @see GlobalWidgets
 */

public interface GameWidget extends AutoCloseable {
    void addPlayer(ServerPlayerEntity player);

    void removePlayer(ServerPlayerEntity player);

    @Override
    void close();
}
