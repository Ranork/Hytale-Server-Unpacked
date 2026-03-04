package com.hypixel.hytale.server.core.universe.world.worldmap;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec.Builder;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerConfigData;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerWorldData;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager.MarkerReference;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarker;
import java.util.ArrayList;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldMapManager$PlayerMarkerReference implements MarkerReference {
    public static final BuilderCodec<WorldMapManager$PlayerMarkerReference> CODEC = ((Builder)((Builder)((Builder)BuilderCodec.builder(
                        WorldMapManager$PlayerMarkerReference.class, WorldMapManager$PlayerMarkerReference::new
                    )
                    .addField(
                        new KeyedCodec("Player", Codec.UUID_BINARY),
                        (playerMarkerReference, uuid) -> playerMarkerReference.player = uuid,
                        playerMarkerReference -> playerMarkerReference.player
                    ))
                .addField(
                    new KeyedCodec("World", Codec.STRING),
                    (playerMarkerReference, s) -> playerMarkerReference.world = s,
                    playerMarkerReference -> playerMarkerReference.world
                ))
            .addField(
                new KeyedCodec("MarkerId", Codec.STRING),
                (playerMarkerReference, s) -> playerMarkerReference.markerId = s,
                playerMarkerReference -> playerMarkerReference.markerId
            ))
        .build();
    private UUID player;
    private String world;
    private String markerId;

    private WorldMapManager$PlayerMarkerReference() {
    }

    public WorldMapManager$PlayerMarkerReference(@Nonnull UUID player, @Nonnull String world, @Nonnull String markerId) {
        this.player = player;
        this.world = world;
        this.markerId = markerId;
    }

    public UUID getPlayer() {
        return this.player;
    }

    public String getMarkerId() {
        return this.markerId;
    }

    public void remove() {
        PlayerRef playerRef = Universe.get().getPlayer(this.player);
        if (playerRef != null) {
            Player playerComponent = (Player)playerRef.getComponent(Player.getComponentType());
            this.removeMarkerFromOnlinePlayer(playerComponent);
        } else {
            this.removeMarkerFromOfflinePlayer();
        }
    }

    private void removeMarkerFromOnlinePlayer(@Nonnull Player player) {
        PlayerConfigData data = player.getPlayerConfigData();
        String world = this.world;
        if (world == null) {
            world = player.getWorld().getName();
        }

        removeMarkerFromData(data, world, this.markerId);
    }

    private void removeMarkerFromOfflinePlayer() {
        Universe.get().getPlayerStorage().load(this.player).thenApply(holder -> {
            Player player = (Player)holder.getComponent(Player.getComponentType());
            PlayerConfigData data = player.getPlayerConfigData();
            String world = this.world;
            if (world == null) {
                world = data.getWorld();
            }

            removeMarkerFromData(data, world, this.markerId);
            return holder;
        }).thenCompose(holder -> Universe.get().getPlayerStorage().save(this.player, holder));
    }

    @Nullable
    private static void removeMarkerFromData(@Nonnull PlayerConfigData data, @Nonnull String worldName, @Nonnull String markerId) {
        PlayerWorldData perWorldData = data.getPerWorldData(worldName);
        ArrayList<? extends UserMapMarker> playerMarkers = new ArrayList(perWorldData.getUserMapMarkers());
        playerMarkers.removeIf(marker -> markerId.equals(marker.getId()));
        perWorldData.setUserMapMarkers(playerMarkers);
    }
}