package com.hypixel.hytale.server.core.universe.world;

import com.hypixel.hytale.common.fastutil.HLongOpenHashSet;
import com.hypixel.hytale.common.fastutil.HLongSet;
import com.hypixel.hytale.common.thread.ticking.Tickable;
import com.hypixel.hytale.common.util.CompletableFutureUtil;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.iterator.CircleSpiralIterator;
import com.hypixel.hytale.math.shape.Box2D;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.packets.worldmap.ClearWorldMap;
import com.hypixel.hytale.protocol.packets.worldmap.MapChunk;
import com.hypixel.hytale.protocol.packets.worldmap.MapImage;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.protocol.packets.worldmap.UpdateWorldMap;
import com.hypixel.hytale.protocol.packets.worldmap.UpdateWorldMapSettings;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.gameplay.WorldMapConfig;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.DiscoverZoneEvent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapSettings;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MapMarkerTracker;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldMapTracker implements Tickable {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   public static final float UPDATE_SPEED = 1.0F;
   public static final int RADIUS_MAX = 512;
   public static final int EMPTY_UPDATE_WORLD_MAP_SIZE = 13;
   private static final int EMPTY_MAP_CHUNK_SIZE = 10;
   private static final int FULL_MAP_CHUNK_SIZE = 23;
   public static final int MAX_IMAGE_GENERATION = 20;
   public static final int MAX_FRAME = 2621440;
   private final Player player;
   private final CircleSpiralIterator spiralIterator = new CircleSpiralIterator();
   private final ReentrantReadWriteLock loadedLock = new ReentrantReadWriteLock();
   private final HLongSet loaded = new HLongOpenHashSet();
   private final HLongSet pendingReloadChunks = new HLongOpenHashSet();
   private final Long2ObjectOpenHashMap<CompletableFuture<MapImage>> pendingReloadFutures = new Long2ObjectOpenHashMap();
   private final MapMarkerTracker markerTracker;
   private float updateTimer;
   private Integer viewRadiusOverride;
   private boolean started;
   private int sentViewRadius;
   private int lastChunkX;
   private int lastChunkZ;
   @Nullable
   private String currentBiomeName;
   @Nullable
   private WorldMapTracker.ZoneDiscoveryInfo currentZone;
   private boolean allowTeleportToCoordinates = true;
   private boolean allowTeleportToMarkers = true;
   private boolean clientHasWorldMapVisible;
   @Nullable
   private TransformComponent transformComponent;

   public WorldMapTracker(@Nonnull Player player) {
      this.player = player;
      this.markerTracker = new MapMarkerTracker(this);
   }

   @Override
   public void tick(float dt) {
      if (!this.started) {
         this.started = true;
         LOGGER.at(Level.INFO).log("Started Generating Map!");
      }

      World world = this.player.getWorld();
      if (world != null && this.player.getPlayerConnection().getChannel(NetworkChannel.WorldMap).isWritable()) {
         if (this.transformComponent == null) {
            this.transformComponent = this.player.getTransformComponent();
            if (this.transformComponent == null) {
               return;
            }
         }

         WorldMapManager worldMapManager = world.getWorldMapManager();
         WorldMapSettings worldMapSettings = worldMapManager.getWorldMapSettings();
         int viewRadius;
         if (this.viewRadiusOverride != null) {
            viewRadius = this.viewRadiusOverride;
         } else {
            viewRadius = worldMapSettings.getViewRadius(this.player.getViewRadius());
         }

         Vector3d position = this.transformComponent.getPosition();
         int playerX = MathUtil.floor(position.getX());
         int playerZ = MathUtil.floor(position.getZ());
         int playerChunkX = playerX >> 5;
         int playerChunkZ = playerZ >> 5;
         if (world.isCompassUpdating()) {
            this.markerTracker.updatePointsOfInterest(dt, world, viewRadius, playerChunkX, playerChunkZ);
         }

         if (worldMapManager.isWorldMapEnabled()) {
            this.updateWorldMap(world, dt, worldMapSettings, viewRadius, playerChunkX, playerChunkZ);
         }
      }
   }

   public void updateCurrentZoneAndBiome(
      @Nonnull Ref<EntityStore> ref,
      @Nullable WorldMapTracker.ZoneDiscoveryInfo zoneDiscoveryInfo,
      @Nullable String biomeName,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.currentBiomeName = biomeName;
      this.currentZone = zoneDiscoveryInfo;
      Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      if (!playerComponent.isWaitingForClientReady()) {
         World world = componentAccessor.getExternalData().getWorld();
         if (zoneDiscoveryInfo != null && this.discoverZone(world, zoneDiscoveryInfo.regionName())) {
            this.onZoneDiscovered(ref, zoneDiscoveryInfo, componentAccessor);
         }
      }
   }

   private void onZoneDiscovered(
      @Nonnull Ref<EntityStore> ref, @Nonnull WorldMapTracker.ZoneDiscoveryInfo zoneDiscoveryInfo, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      WorldMapTracker.ZoneDiscoveryInfo discoverZoneEventInfo = zoneDiscoveryInfo.clone();
      DiscoverZoneEvent.Display discoverZoneEvent = new DiscoverZoneEvent.Display(discoverZoneEventInfo);
      componentAccessor.invoke(ref, discoverZoneEvent);
      if (!discoverZoneEvent.isCancelled() && discoverZoneEventInfo.display()) {
         PlayerRef playerRefComponent = componentAccessor.getComponent(ref, PlayerRef.getComponentType());

         assert playerRefComponent != null;

         EventTitleUtil.showEventTitleToPlayer(
            playerRefComponent,
            Message.translation(String.format("server.map.region.%s", discoverZoneEventInfo.regionName())),
            Message.translation(String.format("server.map.zone.%s", discoverZoneEventInfo.zoneName())),
            discoverZoneEventInfo.major(),
            discoverZoneEventInfo.icon(),
            discoverZoneEventInfo.duration(),
            discoverZoneEventInfo.fadeInDuration(),
            discoverZoneEventInfo.fadeOutDuration()
         );
         String discoverySoundEventId = discoverZoneEventInfo.discoverySoundEventId();
         if (discoverySoundEventId != null) {
            int assetIndex = SoundEvent.getAssetMap().getIndex(discoverySoundEventId);
            if (assetIndex != Integer.MIN_VALUE) {
               SoundUtil.playSoundEvent2d(ref, assetIndex, SoundCategory.UI, componentAccessor);
            }
         }
      }
   }

   private void updateWorldMap(
      @Nonnull World world, float dt, @Nonnull WorldMapSettings worldMapSettings, int chunkViewRadius, int playerChunkX, int playerChunkZ
   ) {
      this.processPendingReloadChunks(world);
      Box2D worldMapArea = worldMapSettings.getWorldMapArea();
      if (worldMapArea == null) {
         int xDiff = Math.abs(this.lastChunkX - playerChunkX);
         int zDiff = Math.abs(this.lastChunkZ - playerChunkZ);
         int chunkMoveDistance = xDiff <= 0 && zDiff <= 0 ? 0 : (int)Math.ceil(Math.sqrt(xDiff * xDiff + zDiff * zDiff));
         this.sentViewRadius = Math.max(0, this.sentViewRadius - chunkMoveDistance);
         this.lastChunkX = playerChunkX;
         this.lastChunkZ = playerChunkZ;
         this.updateTimer -= dt;
         if (this.updateTimer > 0.0F) {
            return;
         }

         if (this.sentViewRadius != chunkViewRadius) {
            if (this.sentViewRadius > chunkViewRadius) {
               this.sentViewRadius = chunkViewRadius;
            }

            this.unloadImages(chunkViewRadius, playerChunkX, playerChunkZ);
            if (this.sentViewRadius < chunkViewRadius) {
               this.loadImages(world, chunkViewRadius, playerChunkX, playerChunkZ, 20);
            }
         } else {
            this.updateTimer = 1.0F;
         }
      } else {
         this.updateTimer -= dt;
         if (this.updateTimer > 0.0F) {
            return;
         }

         this.loadWorldMap(world, worldMapArea, 20);
      }
   }

   private void unloadImages(int chunkViewRadius, int playerChunkX, int playerChunkZ) {
      List<MapChunk> currentUnloadList = null;
      List<List<MapChunk>> allUnloadLists = null;
      this.loadedLock.writeLock().lock();

      try {
         int packetSize = 2621427;
         LongIterator iterator = this.loaded.iterator();

         while (iterator.hasNext()) {
            long chunkCoordinates = iterator.nextLong();
            int mapChunkX = ChunkUtil.xOfChunkIndex(chunkCoordinates);
            int mapChunkZ = ChunkUtil.zOfChunkIndex(chunkCoordinates);
            if (!shouldBeVisible(chunkViewRadius, playerChunkX, playerChunkZ, mapChunkX, mapChunkZ)) {
               if (currentUnloadList == null) {
                  currentUnloadList = new ObjectArrayList(packetSize / 10);
               }

               currentUnloadList.add(new MapChunk(mapChunkX, mapChunkZ, null));
               packetSize -= 10;
               iterator.remove();
               if (packetSize < 10) {
                  packetSize = 2621427;
                  if (allUnloadLists == null) {
                     allUnloadLists = new ObjectArrayList(this.loaded.size() / (packetSize / 10));
                  }

                  allUnloadLists.add(currentUnloadList);
                  currentUnloadList = new ObjectArrayList(packetSize / 10);
               }
            }
         }

         if (allUnloadLists != null) {
            for (List<MapChunk> unloadList : allUnloadLists) {
               this.writeUpdatePacket(unloadList);
            }
         }

         this.writeUpdatePacket(currentUnloadList);
      } finally {
         this.loadedLock.writeLock().unlock();
      }
   }

   private void processPendingReloadChunks(@Nonnull World world) {
      List<MapChunk> chunksToSend = null;
      this.loadedLock.writeLock().lock();

      try {
         if (!this.pendingReloadChunks.isEmpty()) {
            int imageSize = MathUtil.fastFloor(32.0F * world.getWorldMapManager().getWorldMapSettings().getImageScale());
            int fullMapChunkSize = 23 + 4 * imageSize * imageSize;
            int packetSize = 2621427;
            LongIterator iterator = this.pendingReloadChunks.iterator();

            while (iterator.hasNext()) {
               long chunkCoordinates = iterator.nextLong();
               CompletableFuture<MapImage> future = (CompletableFuture<MapImage>)this.pendingReloadFutures.get(chunkCoordinates);
               if (future == null) {
                  future = world.getWorldMapManager().getImageAsync(chunkCoordinates);
                  this.pendingReloadFutures.put(chunkCoordinates, future);
               }

               if (future.isDone()) {
                  iterator.remove();
                  this.pendingReloadFutures.remove(chunkCoordinates);
                  if (chunksToSend == null) {
                     chunksToSend = new ObjectArrayList(packetSize / fullMapChunkSize);
                  }

                  int mapChunkX = ChunkUtil.xOfChunkIndex(chunkCoordinates);
                  int mapChunkZ = ChunkUtil.zOfChunkIndex(chunkCoordinates);
                  chunksToSend.add(new MapChunk(mapChunkX, mapChunkZ, future.getNow(null)));
                  this.loaded.add(chunkCoordinates);
                  packetSize -= fullMapChunkSize;
                  if (packetSize < fullMapChunkSize) {
                     this.writeUpdatePacket(chunksToSend);
                     chunksToSend = new ObjectArrayList(2621440 - 13 / fullMapChunkSize);
                     packetSize = 2621427;
                  }
               }
            }

            this.writeUpdatePacket(chunksToSend);
            return;
         }
      } finally {
         this.loadedLock.writeLock().unlock();
      }
   }

   private int loadImages(@Nonnull World world, int chunkViewRadius, int playerChunkX, int playerChunkZ, int maxGeneration) {
      List<MapChunk> currentLoadList = null;
      List<List<MapChunk>> allLoadLists = null;
      this.loadedLock.writeLock().lock();

      try {
         int packetSize = 2621427;
         int imageSize = MathUtil.fastFloor(32.0F * world.getWorldMapManager().getWorldMapSettings().getImageScale());
         int fullMapChunkSize = 23 + 4 * imageSize * imageSize;
         boolean areAllLoaded = true;
         this.spiralIterator.init(playerChunkX, playerChunkZ, this.sentViewRadius, chunkViewRadius);

         while (maxGeneration > 0 && this.spiralIterator.hasNext()) {
            long chunkCoordinates = this.spiralIterator.next();
            if (!this.loaded.contains(chunkCoordinates)) {
               areAllLoaded = false;
               CompletableFuture<MapImage> future = world.getWorldMapManager().getImageAsync(chunkCoordinates);
               if (!future.isDone()) {
                  maxGeneration--;
               } else if (this.loaded.add(chunkCoordinates)) {
                  if (currentLoadList == null) {
                     currentLoadList = new ObjectArrayList(packetSize / fullMapChunkSize);
                  }

                  int mapChunkX = ChunkUtil.xOfChunkIndex(chunkCoordinates);
                  int mapChunkZ = ChunkUtil.zOfChunkIndex(chunkCoordinates);
                  currentLoadList.add(new MapChunk(mapChunkX, mapChunkZ, future.getNow(null)));
                  packetSize -= fullMapChunkSize;
                  if (packetSize < fullMapChunkSize) {
                     packetSize = 2621427;
                     if (allLoadLists == null) {
                        allLoadLists = new ObjectArrayList();
                     }

                     allLoadLists.add(currentLoadList);
                     currentLoadList = new ObjectArrayList(packetSize / fullMapChunkSize);
                  }
               }
            } else if (areAllLoaded) {
               this.sentViewRadius = this.spiralIterator.getCompletedRadius();
            }
         }

         if (areAllLoaded) {
            this.sentViewRadius = this.spiralIterator.getCompletedRadius();
         }

         if (allLoadLists != null) {
            for (List<MapChunk> unloadList : allLoadLists) {
               this.writeUpdatePacket(unloadList);
            }
         }

         this.writeUpdatePacket(currentLoadList);
      } finally {
         this.loadedLock.writeLock().unlock();
      }

      return maxGeneration;
   }

   private int loadWorldMap(@Nonnull World world, @Nonnull Box2D worldMapArea, int maxGeneration) {
      List<MapChunk> currentLoadList = null;
      List<List<MapChunk>> allLoadLists = null;
      this.loadedLock.writeLock().lock();

      try {
         int packetSize = 2621427;
         int imageSize = MathUtil.fastFloor(32.0F * world.getWorldMapManager().getWorldMapSettings().getImageScale());
         int fullMapChunkSize = 23 + 4 * imageSize * imageSize;

         for (int mapChunkX = MathUtil.floor(worldMapArea.min.x); mapChunkX < MathUtil.ceil(worldMapArea.max.x) && maxGeneration > 0; mapChunkX++) {
            for (int mapChunkZ = MathUtil.floor(worldMapArea.min.y); mapChunkZ < MathUtil.ceil(worldMapArea.max.y) && maxGeneration > 0; mapChunkZ++) {
               long chunkCoordinates = ChunkUtil.indexChunk(mapChunkX, mapChunkZ);
               if (!this.loaded.contains(chunkCoordinates)) {
                  CompletableFuture<MapImage> future = CompletableFutureUtil._catch(world.getWorldMapManager().getImageAsync(chunkCoordinates));
                  if (!future.isDone()) {
                     maxGeneration--;
                  } else {
                     if (currentLoadList == null) {
                        currentLoadList = new ObjectArrayList(packetSize / fullMapChunkSize);
                     }

                     currentLoadList.add(new MapChunk(mapChunkX, mapChunkZ, future.getNow(null)));
                     this.loaded.add(chunkCoordinates);
                     packetSize -= fullMapChunkSize;
                     if (packetSize < fullMapChunkSize) {
                        packetSize = 2621427;
                        if (allLoadLists == null) {
                           allLoadLists = new ObjectArrayList(Math.max(packetSize / fullMapChunkSize, 1));
                        }

                        allLoadLists.add(currentLoadList);
                        currentLoadList = new ObjectArrayList(packetSize / fullMapChunkSize);
                     }
                  }
               }
            }
         }
      } finally {
         this.loadedLock.writeLock().unlock();
      }

      if (allLoadLists != null) {
         for (List<MapChunk> unloadList : allLoadLists) {
            this.writeUpdatePacket(unloadList);
         }
      }

      this.writeUpdatePacket(currentLoadList);
      return maxGeneration;
   }

   private void writeUpdatePacket(@Nullable List<MapChunk> list) {
      if (list != null) {
         UpdateWorldMap packet = new UpdateWorldMap(list.toArray(MapChunk[]::new), null, null);
         LOGGER.at(Level.FINE).log("Sending world map update to %s - %d chunks", this.player.getUuid(), list.size());
         this.player.getPlayerConnection().write(packet);
      }
   }

   @Nonnull
   public Map<String, MapMarker> getSentMarkers() {
      return this.markerTracker.getSentMarkers();
   }

   @Nonnull
   public Player getPlayer() {
      return this.player;
   }

   @Nullable
   public TransformComponent getTransformComponent() {
      return this.transformComponent;
   }

   public void clear() {
      this.loadedLock.writeLock().lock();

      try {
         this.loaded.clear();
         this.sentViewRadius = 0;
         this.markerTracker.getSentMarkers().clear();
      } finally {
         this.loadedLock.writeLock().unlock();
      }

      this.player.getPlayerConnection().write(new ClearWorldMap());
   }

   public void clearChunks(@Nonnull LongSet chunkIndices) {
      this.loadedLock.writeLock().lock();

      try {
         chunkIndices.forEach(index -> {
            this.loaded.remove(index);
            this.pendingReloadChunks.add(index);
            this.pendingReloadFutures.remove(index);
         });
      } finally {
         this.loadedLock.writeLock().unlock();
      }

      this.updateTimer = 0.0F;
   }

   public void sendSettings(@Nonnull World world) {
      UpdateWorldMapSettings worldMapSettingsPacket = new UpdateWorldMapSettings(world.getWorldMapManager().getWorldMapSettings().getSettingsPacket());
      world.execute(() -> {
         Store<EntityStore> store = world.getEntityStore().getStore();
         Ref<EntityStore> ref = this.player.getReference();
         if (ref != null) {
            Player playerComponent = store.getComponent(ref, Player.getComponentType());

            assert playerComponent != null;

            PlayerRef playerRefComponent = store.getComponent(ref, PlayerRef.getComponentType());

            assert playerRefComponent != null;

            worldMapSettingsPacket.allowTeleportToCoordinates = this.allowTeleportToCoordinates && playerComponent.getGameMode() != GameMode.Adventure;
            worldMapSettingsPacket.allowTeleportToMarkers = this.allowTeleportToMarkers && playerComponent.getGameMode() != GameMode.Adventure;
            WorldMapConfig worldMapConfig = world.getGameplayConfig().getWorldMapConfig();
            worldMapSettingsPacket.allowCreatingMapMarkers = worldMapConfig.getUserMapMarkerConfig().isAllowCreatingMarkers();
            worldMapSettingsPacket.allowShowOnMapToggle = worldMapConfig.canTogglePlayersInMap();
            worldMapSettingsPacket.allowCompassTrackingToggle = worldMapConfig.canTrackPlayersInCompass();
            worldMapSettingsPacket.allowRemovingOtherPlayersMarkers = worldMapConfig.getUserMapMarkerConfig().isAllowDeleteOtherPlayersSharedMarkers();
            playerRefComponent.getPacketHandler().write(worldMapSettingsPacket);
         }
      });
   }

   private boolean hasDiscoveredZone(@Nonnull String zoneName) {
      return this.player.getPlayerConfigData().getDiscoveredZones().contains(zoneName);
   }

   public boolean discoverZone(@Nonnull World world, @Nonnull String zoneName) {
      Set<String> discoveredZones = this.player.getPlayerConfigData().getDiscoveredZones();
      if (!discoveredZones.contains(zoneName)) {
         Set<String> var4 = new HashSet<>(discoveredZones);
         var4.add(zoneName);
         this.player.getPlayerConfigData().setDiscoveredZones(var4);
         this.sendSettings(world);
         return true;
      } else {
         return false;
      }
   }

   public boolean undiscoverZone(@Nonnull World world, @Nonnull String zoneName) {
      Set<String> discoveredZones = this.player.getPlayerConfigData().getDiscoveredZones();
      if (discoveredZones.contains(zoneName)) {
         Set<String> var4 = new HashSet<>(discoveredZones);
         var4.remove(zoneName);
         this.player.getPlayerConfigData().setDiscoveredZones(var4);
         this.sendSettings(world);
         return true;
      } else {
         return false;
      }
   }

   public boolean discoverZones(@Nonnull World world, @Nonnull Set<String> zoneNames) {
      Set<String> discoveredZones = this.player.getPlayerConfigData().getDiscoveredZones();
      if (!discoveredZones.containsAll(zoneNames)) {
         Set<String> var4 = new HashSet<>(discoveredZones);
         var4.addAll(zoneNames);
         this.player.getPlayerConfigData().setDiscoveredZones(var4);
         this.sendSettings(world);
         return true;
      } else {
         return false;
      }
   }

   public boolean undiscoverZones(@Nonnull World world, @Nonnull Set<String> zoneNames) {
      Set<String> discoveredZones = this.player.getPlayerConfigData().getDiscoveredZones();
      if (discoveredZones.containsAll(zoneNames)) {
         Set<String> var4 = new HashSet<>(discoveredZones);
         var4.removeAll(zoneNames);
         this.player.getPlayerConfigData().setDiscoveredZones(var4);
         this.sendSettings(world);
         return true;
      } else {
         return false;
      }
   }

   public boolean isAllowTeleportToCoordinates() {
      return this.player.hasPermission("hytale.world_map.teleport.coordinate") && this.player.getGameMode() != GameMode.Adventure;
   }

   public boolean isAllowTeleportToMarkers() {
      return this.player.hasPermission("hytale.world_map.teleport.marker") && this.player.getGameMode() != GameMode.Adventure;
   }

   public void setPlayerMapFilter(Predicate<PlayerRef> playerMapFilter) {
      this.markerTracker.setPlayerMapFilter(playerMapFilter);
   }

   public void setClientHasWorldMapVisible(boolean visible) {
      this.clientHasWorldMapVisible = visible;
   }

   @Nullable
   public Integer getViewRadiusOverride() {
      return this.viewRadiusOverride;
   }

   @Nullable
   public String getCurrentBiomeName() {
      return this.currentBiomeName;
   }

   @Nullable
   public WorldMapTracker.ZoneDiscoveryInfo getCurrentZone() {
      return this.currentZone;
   }

   public void setViewRadiusOverride(@Nullable Integer viewRadiusOverride) {
      this.viewRadiusOverride = viewRadiusOverride;
      this.clear();
   }

   public int getEffectiveViewRadius(@Nonnull World world) {
      return this.viewRadiusOverride != null
         ? this.viewRadiusOverride
         : world.getWorldMapManager().getWorldMapSettings().getViewRadius(this.player.getViewRadius());
   }

   public boolean shouldBeVisible(int chunkViewRadius, long chunkCoordinates) {
      if (this.player != null && this.transformComponent != null) {
         Vector3d position = this.transformComponent.getPosition();
         int chunkX = MathUtil.floor(position.getX()) >> 5;
         int chunkZ = MathUtil.floor(position.getZ()) >> 5;
         int x = ChunkUtil.xOfChunkIndex(chunkCoordinates);
         int z = ChunkUtil.zOfChunkIndex(chunkCoordinates);
         return shouldBeVisible(chunkViewRadius, chunkX, chunkZ, x, z);
      } else {
         return false;
      }
   }

   public void copyFrom(@Nonnull WorldMapTracker worldMapTracker) {
      this.loadedLock.writeLock().lock();

      try {
         worldMapTracker.loadedLock.readLock().lock();

         try {
            this.loaded.addAll(worldMapTracker.loaded);
            this.markerTracker.copyFrom(worldMapTracker.markerTracker);
         } finally {
            worldMapTracker.loadedLock.readLock().unlock();
         }
      } finally {
         this.loadedLock.writeLock().unlock();
      }
   }

   public static boolean shouldBeVisible(int chunkViewRadius, int chunkX, int chunkZ, int x, int z) {
      int xDiff = Math.abs(x - chunkX);
      int zDiff = Math.abs(z - chunkZ);
      int distanceSq = xDiff * xDiff + zDiff * zDiff;
      return distanceSq <= chunkViewRadius * chunkViewRadius;
   }

   public record ZoneDiscoveryInfo(
      @Nonnull String zoneName,
      @Nonnull String regionName,
      boolean display,
      @Nullable String discoverySoundEventId,
      @Nullable String icon,
      boolean major,
      float duration,
      float fadeInDuration,
      float fadeOutDuration
   ) {
      @Nonnull
      public WorldMapTracker.ZoneDiscoveryInfo clone() {
         return new WorldMapTracker.ZoneDiscoveryInfo(
            this.zoneName,
            this.regionName,
            this.display,
            this.discoverySoundEventId,
            this.icon,
            this.major,
            this.duration,
            this.fadeInDuration,
            this.fadeOutDuration
         );
      }
   }
}
