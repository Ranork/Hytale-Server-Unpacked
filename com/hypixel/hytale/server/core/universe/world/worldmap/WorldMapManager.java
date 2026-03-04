package com.hypixel.hytale.server.core.universe.world.worldmap;

import com.hypixel.fastutil.longs.Long2ObjectConcurrentHashMap;
import com.hypixel.hytale.common.util.CompletableFutureUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.packets.player.RemoveMapMarker;
import com.hypixel.hytale.protocol.packets.worldmap.CreateUserMarker;
import com.hypixel.hytale.protocol.packets.worldmap.MapImage;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerWorldData;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager.ImageEntry;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager.MarkerAndItsStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager.MarkerProvider;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager.MarkerReference;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager.PlayerMarkerReference;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.providers.DeathMarkerProvider;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.providers.OtherPlayersMarkerProvider;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.providers.POIMarkerProvider;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.providers.PersonalMarkersProvider;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.providers.RespawnMarkerProvider;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.providers.SharedMarkersProvider;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.providers.SpawnMarkerProvider;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarker;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarkersStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMarkerValidator;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMarkerValidator.CanRemove;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMarkerValidator.CanSpawn;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMarkerValidator.Fail;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMarkerValidator.PlaceResult;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMarkerValidator.RemoveResult;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.worldstore.WorldMarkersResource;
import com.hypixel.hytale.server.core.util.thread.TickingThread;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldMapManager extends TickingThread {
    private static final int IMAGE_KEEP_ALIVE = 60;
    private static final float DEFAULT_UNLOAD_DELAY = 1.0F;
    @Nonnull
    private final HytaleLogger logger;
    @Nonnull
    private final World world;
    private final Long2ObjectConcurrentHashMap<ImageEntry> images = new Long2ObjectConcurrentHashMap(
        true, ChunkUtil.indexChunk(Integer.MIN_VALUE, Integer.MIN_VALUE)
    );
    private final Long2ObjectConcurrentHashMap<CompletableFuture<MapImage>> generating = new Long2ObjectConcurrentHashMap(
        true, ChunkUtil.indexChunk(Integer.MIN_VALUE, Integer.MIN_VALUE)
    );
    private final Map<String, MarkerProvider> markerProviders = new ConcurrentHashMap();
    private final Map<String, MapMarker> pointsOfInterest = new ConcurrentHashMap();
    @Nonnull
    private WorldMapSettings worldMapSettings = WorldMapSettings.DISABLED;
    @Nullable
    private IWorldMap generator;
    @Nonnull
    private CompletableFuture<Void> generatorLoaded = new CompletableFuture();
    private float unloadDelay = 1.0F;

    public WorldMapManager(@Nonnull World world) {
        super("WorldMap - " + world.getName(), 10, true);
        this.logger = HytaleLogger.get("World|" + world.getName() + "|M");
        this.world = world;
        this.addMarkerProvider("spawn", SpawnMarkerProvider.INSTANCE);
        this.addMarkerProvider("playerIcons", OtherPlayersMarkerProvider.INSTANCE);
        this.addMarkerProvider("death", DeathMarkerProvider.INSTANCE);
        this.addMarkerProvider("respawn", RespawnMarkerProvider.INSTANCE);
        this.addMarkerProvider("personal", PersonalMarkersProvider.INSTANCE);
        this.addMarkerProvider("shared", SharedMarkersProvider.INSTANCE);
        this.addMarkerProvider("poi", POIMarkerProvider.INSTANCE);
    }

    @Nullable
    public IWorldMap getGenerator() {
        return this.generator;
    }

    public void setGenerator(@Nullable IWorldMap generator) {
        boolean before = this.shouldTick();
        if (this.generator != null) {
            this.generator.shutdown();
        }

        this.generator = generator;
        if (generator != null) {
            this.logger.at(Level.INFO).log("Initializing world map generator: %s", generator.toString());
            this.generatorLoaded.complete(null);
            this.generatorLoaded = new CompletableFuture();
            this.worldMapSettings = generator.getWorldMapSettings();
            this.images.clear();
            this.generating.clear();

            for (Player worldPlayer : this.world.getPlayers()) {
                worldPlayer.getWorldMapTracker().clear();
            }

            this.updateTickingState(before);
            this.sendSettings();
            this.logger.at(Level.INFO).log("Generating Points of Interest...");
            CompletableFutureUtil._catch(generator.generatePointsOfInterest(this.world).thenAcceptAsync(pointsOfInterest -> {
                this.pointsOfInterest.putAll(pointsOfInterest);
                this.logger.at(Level.INFO).log("Finished Generating Points of Interest!");
            }));
        } else {
            this.logger.at(Level.INFO).log("World map disabled!");
            this.worldMapSettings = WorldMapSettings.DISABLED;
            this.sendSettings();
        }
    }

    protected boolean isIdle() {
        return this.world.getPlayerCount() == 0;
    }

    protected void tick(float dt) {
        for (Player player : this.world.getPlayers()) {
            player.getWorldMapTracker().tick(dt);
        }

        this.unloadDelay -= dt;
        if (this.unloadDelay <= 0.0F) {
            this.unloadDelay = 1.0F;
            this.unloadImages();
        }
    }

    protected void onShutdown() {
    }

    public void unloadImages() {
        int imagesCount = this.images.size();
        if (imagesCount != 0) {
            List<Player> players = this.world.getPlayers();
            LongSet toRemove = new LongOpenHashSet();
            this.images.forEach((index, chunk) -> {
                if (this.isWorldMapEnabled() && isWorldMapImageVisibleToAnyPlayer(players, index, this.worldMapSettings)) {
                    chunk.keepAlive.set(60);
                } else {
                    if (chunk.keepAlive.decrementAndGet() <= 0) {
                        toRemove.add(index);
                    }
                }
            });
            if (!toRemove.isEmpty()) {
                toRemove.forEach(value -> {
                    this.logger.at(Level.FINE).log("Unloading world map image: %s", value);
                    this.images.remove(value);
                });
            }

            int toRemoveSize = toRemove.size();
            if (toRemoveSize > 0) {
                this.logger
                    .at(Level.FINE)
                    .log("Cleaned %s world map images from memory, with %s images remaining in memory.", toRemoveSize, imagesCount - toRemoveSize);
            }
        }
    }

    public boolean isWorldMapEnabled() {
        return this.worldMapSettings.getSettingsPacket().enabled;
    }

    public static boolean isWorldMapImageVisibleToAnyPlayer(@Nonnull List<Player> players, long imageIndex, @Nonnull WorldMapSettings settings) {
        for (Player player : players) {
            int viewRadius = settings.getViewRadius(player.getViewRadius());
            if (player.getWorldMapTracker().shouldBeVisible(viewRadius, imageIndex)) {
                return true;
            }
        }

        return false;
    }

    @Nonnull
    public World getWorld() {
        return this.world;
    }

    @Nonnull
    public WorldMapSettings getWorldMapSettings() {
        return this.worldMapSettings;
    }

    public Map<String, MarkerProvider> getMarkerProviders() {
        return this.markerProviders;
    }

    public void addMarkerProvider(@Nonnull String key, @Nonnull MarkerProvider provider) {
        this.markerProviders.put(key, provider);
    }

    public Map<String, MapMarker> getPointsOfInterest() {
        return this.pointsOfInterest;
    }

    @Nullable
    public MapImage getImageIfInMemory(int x, int z) {
        return this.getImageIfInMemory(ChunkUtil.indexChunk(x, z));
    }

    @Nullable
    public MapImage getImageIfInMemory(long index) {
        ImageEntry pair = (ImageEntry)this.images.get(index);
        return pair != null ? pair.image : null;
    }

    @Nonnull
    public CompletableFuture<MapImage> getImageAsync(int x, int z) {
        return this.getImageAsync(ChunkUtil.indexChunk(x, z));
    }

    @Nonnull
    public CompletableFuture<MapImage> getImageAsync(long index) {
        ImageEntry pair = (ImageEntry)this.images.get(index);
        MapImage image = pair != null ? pair.image : null;
        if (image != null) {
            return CompletableFuture.completedFuture(image);
        } else {
            CompletableFuture<MapImage> gen = (CompletableFuture<MapImage>)this.generating.get(index);
            if (gen != null) {
                return gen;
            } else {
                int imageSize = MathUtil.fastFloor(32.0F * this.worldMapSettings.getImageScale());
                LongSet chunksToGenerate = new LongOpenHashSet();
                chunksToGenerate.add(index);
                CompletableFuture<MapImage> future = CompletableFutureUtil._catch(
                    this.generator.generate(this.world, imageSize, imageSize, chunksToGenerate).thenApplyAsync(worldMap -> {
                        MapImage newImage = (MapImage)worldMap.getChunks().get(index);
                        if (this.generating.remove(index) != null) {
                            this.images.put(index, new ImageEntry(newImage));
                        }
    
                        return newImage;
                    })
                );
                this.generating.put(index, future);
                return future;
            }
        }
    }

    public void generate() {
    }

    public void sendSettings() {
        for (Player player : this.world.getPlayers()) {
            player.getWorldMapTracker().sendSettings(this.world);
        }
    }

    public boolean shouldTick() {
        return this.world.getWorldConfig().isCompassUpdating() || this.isWorldMapEnabled();
    }

    public void updateTickingState(boolean before) {
        boolean after = this.shouldTick();
        if (before != after) {
            if (after) {
                this.start();
            } else {
                this.stop();
            }
        }
    }

    public void handleUserCreateMarker(PlayerRef playerRef, CreateUserMarker packet) {
        Ref<EntityStore> ref = playerRef.getReference();
        if (ref != null) {
            PlaceResult validation = UserMarkerValidator.validatePlacing(ref, packet);
            if (validation instanceof CanSpawn canSpawn) {
                Store var13 = ref.getStore();
                UserMapMarkersStore markersStore = canSpawn.markersStore();
                DisplayNameComponent displayNameComponent = (DisplayNameComponent)var13.getComponent(ref, DisplayNameComponent.getComponentType());
                String createdByName = displayNameComponent == null ? playerRef.getUsername() : displayNameComponent.getDisplayName().getRawText();
                UserMapMarker userMapMarker = new UserMapMarker();
                userMapMarker.setId("user_" + (packet.shared ? "shared" : "personal") + "_" + UUID.randomUUID());
                userMapMarker.setPosition(packet.x, packet.z);
                userMapMarker.setName(packet.name);
                userMapMarker.setIcon(packet.markerImage == null ? "User1.png" : packet.markerImage);
                userMapMarker.setColorTint(packet.tintColor == null ? new Color((byte)0, (byte)0, (byte)0) : packet.tintColor);
                userMapMarker.withCreatedByName(createdByName);
                userMapMarker.withCreatedByUuid(playerRef.getUuid());
                markersStore.addUserMapMarker(userMapMarker);
            } else {
                if (validation instanceof Fail store) {
                    Fail var10000 = store;

                    try {
                        var15 = var10000.errorMsg();
                    } catch (Throwable var12) {
                        throw new MatchException(var12.toString(), var12);
                    }

                    Message displayNameComponent = var15;
                    playerRef.sendMessage(displayNameComponent.color("#ffc800"));
                }
            }
        }
    }

    public void handleUserRemoveMarker(PlayerRef playerRef, RemoveMapMarker packet) {
        Ref<EntityStore> ref = playerRef.getReference();
        if (ref != null) {
            Store<EntityStore> store = ref.getStore();
            World world = ((EntityStore)store.getExternalData()).getWorld();
            Player player = (Player)store.getComponent(ref, Player.getComponentType());
            PlayerWorldData perWorldData = player.getPlayerConfigData().getPerWorldData(world.getName());
            boolean removedDeathMarker = perWorldData.removeLastDeath(packet.markerId);
            if (!removedDeathMarker) {
                MarkerAndItsStore userMarkerAndStore = this.findUserMapMarker(packet.markerId, player, world);
                if (userMarkerAndStore == null) {
                    playerRef.sendMessage(Message.translation("server.worldmap.markers.edit.notFound").color("#ffc800"));
                    HytaleLogger.getLogger()
                        .at(Level.WARNING)
                        .log("Couldn't find marker to remove '" + packet.markerId + "' from " + playerRef.getUsername() + " " + playerRef.getUuid());
                } else {
                    RemoveResult validation = UserMarkerValidator.validateRemove(ref, userMarkerAndStore.marker);
                    if (!(validation instanceof CanRemove)) {
                        if (validation instanceof Fail var11) {
                            Fail var10000 = var11;

                            try {
                                var15 = var10000.errorMsg();
                            } catch (Throwable var14) {
                                throw new MatchException(var14.toString(), var14);
                            }

                            Message var13 = var15;
                            playerRef.sendMessage(var13.color("#ffc800"));
                        }
                    } else {
                        userMarkerAndStore.store.removeUserMapMarker(userMarkerAndStore.marker.getId());
                    }
                }
            }
        }
    }

    @Nullable
    private MarkerAndItsStore findUserMapMarker(String markerId, Player player, World world) {
        PlayerWorldData perWorldData = player.getPlayerConfigData().getPerWorldData(world.getName());
        UserMapMarker personalMarker = perWorldData.getUserMapMarker(markerId);
        if (personalMarker != null) {
            return new MarkerAndItsStore(personalMarker, perWorldData);
        } else {
            WorldMarkersResource sharedMarkers = (WorldMarkersResource)world.getChunkStore().getStore().getResource(WorldMarkersResource.getResourceType());
            UserMapMarker sharedMarker = sharedMarkers.getUserMapMarker(markerId);
            return sharedMarker != null ? new MarkerAndItsStore(sharedMarker, sharedMarkers) : null;
        }
    }

    public void clearImages() {
        this.images.clear();
        this.generating.clear();
    }

    public void clearImagesInChunks(@Nonnull LongSet chunkIndices) {
        chunkIndices.forEach(index -> {
            this.images.remove(index);
            this.generating.remove(index);
        });
    }

    public static void sendSettingsToAllWorlds() {
        for (World world : Universe.get().getWorlds().values()) {
            world.execute(() -> world.getWorldMapManager().sendSettings());
        }
    }

    static {
        MarkerReference.CODEC.register("Player", PlayerMarkerReference.class, PlayerMarkerReference.CODEC);
    }
}