package com.hypixel.hytale.server.core.modules.entity.player;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.common.fastutil.HLongOpenHashSet;
import com.hypixel.hytale.common.fastutil.HLongSet;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.iterator.CircleSpiralIterator;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.metrics.MetricsRegistry;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.packets.world.UnloadChunk;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkFlag;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChunkTracker implements Component<EntityStore> {
   @Nonnull
   public static final MetricsRegistry<ChunkTracker> METRICS_REGISTRY = new MetricsRegistry<ChunkTracker>()
      .register("ViewRadius", tracker -> tracker.chunkViewRadius, Codec.INTEGER)
      .register("SentViewRadius", tracker -> tracker.sentViewRadius, Codec.INTEGER)
      .register("HotRadius", tracker -> tracker.hotRadius, Codec.INTEGER)
      .register("LoadedChunksCount", ChunkTracker::getLoadedChunksCount, Codec.INTEGER)
      .register("LoadingChunksCount", ChunkTracker::getLoadingChunksCount, Codec.INTEGER)
      .register("MaxChunksPerSecond", ChunkTracker::getMaxChunksPerSecond, Codec.INTEGER)
      .register("MaxChunksPerTick", ChunkTracker::getMaxChunksPerTick, Codec.INTEGER)
      .register("ReadyForChunks", ChunkTracker::isReadyForChunks, Codec.BOOLEAN)
      .register("LastChunkX", tracker -> tracker.lastChunkX, Codec.INTEGER)
      .register("LastChunkZ", tracker -> tracker.lastChunkZ, Codec.INTEGER);
   public static final int MAX_CHUNKS_PER_SECOND_LOCAL = 256;
   public static final int MAX_CHUNKS_PER_SECOND_LAN = 128;
   public static final int MAX_CHUNKS_PER_SECOND = 36;
   public static final int MAX_CHUNKS_PER_TICK = 4;
   public static final int MIN_LOADED_CHUNKS_RADIUS = 2;
   public static final int MAX_HOT_LOADED_CHUNKS_RADIUS = 8;
   public static final long MAX_FAILURE_BACKOFF_NANOS = TimeUnit.SECONDS.toNanos(10L);
   @Nullable
   private TransformComponent transformComponent;
   private int chunkViewRadius;
   @Nonnull
   private final CircleSpiralIterator spiralIterator = new CircleSpiralIterator();
   @Nonnull
   private final StampedLock loadedLock = new StampedLock();
   @Nonnull
   private final HLongSet loading = new HLongOpenHashSet();
   @Nonnull
   private final HLongSet loaded = new HLongOpenHashSet();
   @Nonnull
   private final HLongSet reload = new HLongOpenHashSet();
   private int maxChunksPerSecond;
   private float inverseMaxChunksPerSecond;
   private int maxChunksPerTick;
   private int minLoadedChunksRadius;
   private int maxHotLoadedChunksRadius;
   private float accumulator;
   private int sentViewRadius;
   private int hotRadius;
   private int lastChunkX;
   private int lastChunkZ;
   private boolean readyForChunks;

   public static ComponentType<EntityStore, ChunkTracker> getComponentType() {
      return EntityModule.get().getChunkTrackerComponentType();
   }

   public ChunkTracker() {
      this.minLoadedChunksRadius = 2;
      this.maxHotLoadedChunksRadius = 8;
      this.maxChunksPerTick = 4;
   }

   private ChunkTracker(@Nonnull ChunkTracker other) {
      this.copyFrom(other);
   }

   public void unloadAll(@Nonnull PlayerRef playerRefComponent) {
      long stamp = this.loadedLock.writeLock();

      try {
         this.loading.clear();
         LongIterator iterator = this.loaded.iterator();

         while (iterator.hasNext()) {
            long chunkIndex = iterator.nextLong();
            int chunkX = ChunkUtil.xOfChunkIndex(chunkIndex);
            int chunkZ = ChunkUtil.zOfChunkIndex(chunkIndex);
            playerRefComponent.getPacketHandler().writeNoCache(new UnloadChunk(chunkX, chunkZ));
         }

         this.loaded.clear();
         this.sentViewRadius = 0;
         this.hotRadius = 0;
      } finally {
         this.loadedLock.unlockWrite(stamp);
      }
   }

   public void clear() {
      long stamp = this.loadedLock.writeLock();

      try {
         this.loading.clear();
         this.loaded.clear();
         this.sentViewRadius = 0;
         this.hotRadius = 0;
      } finally {
         this.loadedLock.unlockWrite(stamp);
      }
   }

   public void tick(
      @Nonnull Player playerComponent,
      @Nonnull PlayerRef playerRefComponent,
      @Nonnull TransformComponent transformComponent,
      float dt,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      if (this.readyForChunks && playerRefComponent.getPacketHandler().getChannel(NetworkChannel.Chunks).isWritable()) {
         this.transformComponent = transformComponent;
         int chunkViewRadius = this.chunkViewRadius = playerComponent.getViewRadius();
         Vector3d position = transformComponent.getPosition();
         int chunkX = MathUtil.floor(position.getX()) >> 5;
         int chunkZ = MathUtil.floor(position.getZ()) >> 5;
         int xDiff = Math.abs(this.lastChunkX - chunkX);
         int zDiff = Math.abs(this.lastChunkZ - chunkZ);
         int chunkMoveDistance = xDiff <= 0 && zDiff <= 0 ? 0 : (int)Math.ceil(Math.sqrt(xDiff * xDiff + zDiff * zDiff));
         this.sentViewRadius = Math.max(0, this.sentViewRadius - chunkMoveDistance);
         this.hotRadius = Math.max(0, this.hotRadius - chunkMoveDistance);
         this.lastChunkX = chunkX;
         this.lastChunkZ = chunkZ;
         if (this.sentViewRadius != chunkViewRadius || this.hotRadius != Math.min(this.maxHotLoadedChunksRadius, chunkViewRadius) || !this.reload.isEmpty()) {
            if (this.sentViewRadius > chunkViewRadius) {
               this.sentViewRadius = chunkViewRadius;
            }

            if (this.hotRadius > chunkViewRadius) {
               this.hotRadius = chunkViewRadius;
            }

            World world = commandBuffer.getExternalData().getWorld();
            ChunkStore chunkStore = world.getChunkStore();
            int minLoadedRadius = Math.max(this.minLoadedChunksRadius, chunkViewRadius);
            int minLoadedRadiusSq = minLoadedRadius * minLoadedRadius;
            long stamp = this.loadedLock.writeLock();

            try {
               this.loaded.removeIf(ChunkTracker::tryUnloadChunk, minLoadedRadiusSq, chunkX, chunkZ, playerRefComponent, this.loading);
               this.accumulator += dt;
               int toLoad = Math.min((int)(this.maxChunksPerSecond * this.accumulator), this.maxChunksPerTick);
               int loadingSize = this.loading.size();
               toLoad -= loadingSize;
               if (!this.reload.isEmpty()) {
                  LongIterator iterator = this.reload.iterator();

                  while (iterator.hasNext()) {
                     long chunkCoordinates = iterator.nextLong();
                     if (!chunkStore.isChunkOnBackoff(chunkCoordinates, MAX_FAILURE_BACKOFF_NANOS) && this.loading.add(chunkCoordinates)) {
                        this.tryLoadChunkAsync(chunkStore, playerRefComponent, chunkCoordinates, transformComponent, commandBuffer);
                        iterator.remove();
                        toLoad--;
                        this.accumulator = this.accumulator - this.inverseMaxChunksPerSecond;
                     }
                  }
               }

               if (this.sentViewRadius < minLoadedRadius) {
                  boolean areAllLoaded = true;
                  this.spiralIterator.init(chunkX, chunkZ, this.sentViewRadius, minLoadedRadius);

                  while (toLoad > 0 && this.spiralIterator.hasNext()) {
                     long chunkCoordinates = this.spiralIterator.next();
                     if (!this.loaded.contains(chunkCoordinates)) {
                        areAllLoaded = false;
                        if (!chunkStore.isChunkOnBackoff(chunkCoordinates, MAX_FAILURE_BACKOFF_NANOS) && this.loading.add(chunkCoordinates)) {
                           this.tryLoadChunkAsync(chunkStore, playerRefComponent, chunkCoordinates, transformComponent, commandBuffer);
                           toLoad--;
                           this.accumulator = this.accumulator - this.inverseMaxChunksPerSecond;
                        }
                     } else if (areAllLoaded) {
                        this.sentViewRadius = this.spiralIterator.getCompletedRadius();
                     }
                  }

                  if (areAllLoaded) {
                     this.sentViewRadius = this.spiralIterator.getCompletedRadius();
                  }
               }
            } finally {
               this.loadedLock.unlockWrite(stamp);
            }

            int var28 = Math.min(this.maxHotLoadedChunksRadius, this.sentViewRadius);
            if (this.hotRadius < var28) {
               this.spiralIterator.init(chunkX, chunkZ, this.hotRadius, var28);

               while (this.spiralIterator.hasNext()) {
                  Ref<ChunkStore> chunkReference = chunkStore.getChunkReference(this.spiralIterator.next());
                  if (chunkReference != null && chunkReference.isValid()) {
                     WorldChunk worldChunkComponent = chunkStore.getStore().getComponent(chunkReference, WorldChunk.getComponentType());

                     assert worldChunkComponent != null;

                     if (!worldChunkComponent.is(ChunkFlag.TICKING)) {
                        commandBuffer.run(_store -> worldChunkComponent.setFlag(ChunkFlag.TICKING, true));
                     }
                  }
               }

               this.hotRadius = var28;
            }

            if (this.sentViewRadius == chunkViewRadius) {
               this.accumulator = 0.0F;
            }
         }
      }
   }

   public boolean isLoaded(long indexChunk) {
      long stamp = this.loadedLock.readLock();

      boolean var5;
      try {
         var5 = this.loaded.contains(indexChunk);
      } finally {
         this.loadedLock.unlockRead(stamp);
      }

      return var5;
   }

   public void removeForReload(long indexChunk) {
      if (this.shouldBeVisible(indexChunk)) {
         long stamp = this.loadedLock.writeLock();

         try {
            this.reload.add(indexChunk);
         } finally {
            this.loadedLock.unlockWrite(stamp);
         }
      }
   }

   public boolean shouldBeVisible(long chunkCoordinates) {
      if (this.transformComponent == null) {
         return false;
      } else {
         Vector3d position = this.transformComponent.getPosition();
         int chunkX = MathUtil.floor(position.getX()) >> 5;
         int chunkZ = MathUtil.floor(position.getZ()) >> 5;
         int x = ChunkUtil.xOfChunkIndex(chunkCoordinates);
         int z = ChunkUtil.zOfChunkIndex(chunkCoordinates);
         int minLoadedRadius = Math.max(this.minLoadedChunksRadius, this.chunkViewRadius);
         return shouldBeVisible(minLoadedRadius * minLoadedRadius, chunkX, chunkZ, x, z);
      }
   }

   @Nonnull
   public ChunkTracker.ChunkVisibility getChunkVisibility(long indexChunk) {
      if (this.transformComponent == null) {
         return ChunkTracker.ChunkVisibility.NONE;
      } else {
         Vector3d position = this.transformComponent.getPosition();
         int chunkX = MathUtil.floor(position.getX()) >> 5;
         int chunkZ = MathUtil.floor(position.getZ()) >> 5;
         int x = ChunkUtil.xOfChunkIndex(indexChunk);
         int z = ChunkUtil.zOfChunkIndex(indexChunk);
         int xDiff = Math.abs(x - chunkX);
         int zDiff = Math.abs(z - chunkZ);
         int distanceSq = xDiff * xDiff + zDiff * zDiff;
         int minLoadedRadius = Math.max(this.minLoadedChunksRadius, this.chunkViewRadius);
         boolean shouldBeVisible = distanceSq <= minLoadedRadius * minLoadedRadius;
         if (shouldBeVisible) {
            boolean isHot = distanceSq <= this.maxHotLoadedChunksRadius * this.maxHotLoadedChunksRadius;
            return isHot ? ChunkTracker.ChunkVisibility.HOT : ChunkTracker.ChunkVisibility.COLD;
         } else {
            return ChunkTracker.ChunkVisibility.NONE;
         }
      }
   }

   public int getMaxChunksPerSecond() {
      return this.maxChunksPerSecond;
   }

   public void setMaxChunksPerSecond(int maxChunksPerSecond) {
      this.maxChunksPerSecond = maxChunksPerSecond;
      this.inverseMaxChunksPerSecond = 1.0F / maxChunksPerSecond;
   }

   public void setDefaultMaxChunksPerSecond(@Nonnull PlayerRef playerRef) {
      if (playerRef.getPacketHandler().isLocalConnection()) {
         this.maxChunksPerSecond = 256;
      } else if (playerRef.getPacketHandler().isLANConnection()) {
         this.maxChunksPerSecond = 128;
      } else {
         this.maxChunksPerSecond = 36;
      }

      this.inverseMaxChunksPerSecond = 1.0F / this.maxChunksPerSecond;
   }

   public int getMaxChunksPerTick() {
      return this.maxChunksPerTick;
   }

   public void setMaxChunksPerTick(int maxChunksPerTick) {
      this.maxChunksPerTick = maxChunksPerTick;
   }

   public int getMinLoadedChunksRadius() {
      return this.minLoadedChunksRadius;
   }

   public void setMinLoadedChunksRadius(int minLoadedChunksRadius) {
      this.minLoadedChunksRadius = minLoadedChunksRadius;
   }

   public int getMaxHotLoadedChunksRadius() {
      return this.maxHotLoadedChunksRadius;
   }

   public void setMaxHotLoadedChunksRadius(int maxHotLoadedChunksRadius) {
      this.maxHotLoadedChunksRadius = maxHotLoadedChunksRadius;
   }

   public int getLoadedChunksCount() {
      long stamp = this.loadedLock.tryOptimisticRead();
      int size = this.loaded.size();
      if (this.loadedLock.validate(stamp)) {
         return size;
      } else {
         stamp = this.loadedLock.readLock();

         int var4;
         try {
            var4 = this.loaded.size();
         } finally {
            this.loadedLock.unlockRead(stamp);
         }

         return var4;
      }
   }

   public int getLoadingChunksCount() {
      long stamp = this.loadedLock.tryOptimisticRead();
      int size = this.loading.size();
      if (this.loadedLock.validate(stamp)) {
         return size;
      } else {
         stamp = this.loadedLock.readLock();

         int var4;
         try {
            var4 = this.loading.size();
         } finally {
            this.loadedLock.unlockRead(stamp);
         }

         return var4;
      }
   }

   @Nonnull
   private String getLoadedChunksGrid() {
      int viewRadius = this.chunkViewRadius;
      int chunkXMin = this.lastChunkX - viewRadius;
      int chunkZMin = this.lastChunkZ - viewRadius;
      int chunkXMax = this.lastChunkX + viewRadius;
      int chunkZMax = this.lastChunkZ + viewRadius;
      StringBuilder sb = new StringBuilder();
      sb.append("(").append(chunkXMin).append(", ").append(chunkZMin).append(") -> (").append(chunkXMax).append(", ").append(chunkZMax).append(")\n");

      for (int x = chunkXMin; x <= chunkXMax; x++) {
         for (int z = chunkZMin; z <= chunkZMax; z++) {
            long index = ChunkUtil.indexChunk(x, z);
            if (this.loaded.contains(index)) {
               ChunkTracker.ChunkVisibility chunkVisibility = this.getChunkVisibility(index);
               switch (chunkVisibility) {
                  case NONE:
                     sb.append('X');
                     break;
                  case HOT:
                     sb.append('#');
                     break;
                  case COLD:
                     sb.append('&');
               }
            } else if (this.loading.contains(index)) {
               sb.append('%');
            } else {
               sb.append(' ');
            }
         }

         sb.append('\n');
      }

      return sb.toString();
   }

   @Nonnull
   public Message getLoadedChunksMessage() {
      long stamp = this.loadedLock.readLock();

      Message var3;
      try {
         var3 = Message.translation("server.commands.chunkTracker.loaded")
            .monospace(true)
            .param("grid", this.getLoadedChunksGrid())
            .param("viewRadius", this.chunkViewRadius)
            .param("sentViewRadius", this.sentViewRadius)
            .param("hotRadius", this.hotRadius)
            .param("readyForChunks", this.readyForChunks)
            .param("loaded", this.loaded.size())
            .param("loading", this.loading.size());
      } finally {
         this.loadedLock.unlockRead(stamp);
      }

      return var3;
   }

   @Nonnull
   public String getLoadedChunksDebug() {
      long stamp = this.loadedLock.readLock();

      String var3;
      try {
         var3 = "Chunks (#: Loaded, &: Loading, ' ': Not loaded):\n"
            + this.getLoadedChunksGrid()
            + "\nView Radius: "
            + this.chunkViewRadius
            + "\nSent View Radius: "
            + this.sentViewRadius
            + "\nHot Radius: "
            + this.hotRadius
            + "\nReady For Chunks: "
            + this.readyForChunks
            + "\nLoaded: "
            + this.loaded.size()
            + "\nLoading: "
            + this.loading.size();
      } finally {
         this.loadedLock.unlockRead(stamp);
      }

      return var3;
   }

   public void setReadyForChunks(boolean readyForChunks) {
      this.readyForChunks = readyForChunks;
   }

   public boolean isReadyForChunks() {
      return this.readyForChunks;
   }

   public void copyFrom(@Nonnull ChunkTracker chunkTracker) {
      long stamp = this.loadedLock.writeLock();

      try {
         long otherStamp = chunkTracker.loadedLock.readLock();

         try {
            this.loading.addAll(chunkTracker.loading);
            this.loaded.addAll(chunkTracker.loaded);
            this.reload.addAll(chunkTracker.reload);
            this.sentViewRadius = 0;
         } finally {
            chunkTracker.loadedLock.unlockRead(otherStamp);
         }
      } finally {
         this.loadedLock.unlockWrite(stamp);
      }
   }

   private static boolean shouldBeVisible(int chunkViewRadiusSquared, int chunkX, int chunkZ, int x, int z) {
      int xDiff = Math.abs(x - chunkX);
      int zDiff = Math.abs(z - chunkZ);
      int distanceSq = xDiff * xDiff + zDiff * zDiff;
      return distanceSq <= chunkViewRadiusSquared;
   }

   public static boolean tryUnloadChunk(
      long chunkIndex, int chunkViewRadiusSquared, int chunkX, int chunkZ, @Nonnull PlayerRef playerRef, @Nonnull LongSet loading
   ) {
      int x = ChunkUtil.xOfChunkIndex(chunkIndex);
      int z = ChunkUtil.zOfChunkIndex(chunkIndex);
      if (shouldBeVisible(chunkViewRadiusSquared, x, z, chunkX, chunkZ)) {
         return false;
      } else {
         Ref<EntityStore> ref = playerRef.getReference();
         if (ref != null && ref.isValid()) {
            Store<EntityStore> store = ref.getStore();
            World world = store.getExternalData().getWorld();
            PacketHandler packetHandler = playerRef.getPacketHandler();
            ChunkStore chunkComponentStore = world.getChunkStore();
            Ref<ChunkStore> chunkRef = chunkComponentStore.getChunkReference(chunkIndex);
            if (chunkRef != null) {
               Store<ChunkStore> chunkStore = chunkComponentStore.getStore();
               ObjectArrayList<ToClientPacket> packets = new ObjectArrayList();
               chunkStore.fetch(Collections.singletonList(chunkRef), ChunkStore.UNLOAD_PACKETS_DATA_QUERY_SYSTEM_TYPE, playerRef, packets);

               for (int i = 0; i < packets.size(); i++) {
                  packetHandler.write((ToClientPacket)packets.get(i));
               }
            }

            packetHandler.writeNoCache(new UnloadChunk(x, z));
            loading.remove(chunkIndex);
            return true;
         } else {
            loading.remove(chunkIndex);
            return true;
         }
      }
   }

   public void tryLoadChunkAsync(
      @Nonnull ChunkStore chunkStore,
      @Nonnull PlayerRef playerRefComponent,
      long chunkIndex,
      @Nonnull TransformComponent transformComponent,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      World world = componentAccessor.getExternalData().getWorld();
      Vector3d position = transformComponent.getPosition();
      int chunkX = MathUtil.floor(position.getX()) >> 5;
      int chunkZ = MathUtil.floor(position.getZ()) >> 5;
      int x = ChunkUtil.xOfChunkIndex(chunkIndex);
      int z = ChunkUtil.zOfChunkIndex(chunkIndex);
      boolean isHot = shouldBeVisible(this.maxHotLoadedChunksRadius, chunkX, chunkZ, x, z);
      Ref<ChunkStore> chunkReference = chunkStore.getChunkReference(chunkIndex);
      if (chunkReference != null) {
         WorldChunk worldChunkComponent = chunkStore.getStore().getComponent(chunkReference, WorldChunk.getComponentType());

         assert worldChunkComponent != null;

         if (worldChunkComponent.is(ChunkFlag.TICKING)) {
            this._loadChunkAsync(chunkIndex, playerRefComponent, chunkReference, chunkStore);
            return;
         }
      }

      int flags = -2147483632;
      if (isHot) {
         flags |= 4;
      }

      chunkStore.getChunkReferenceAsync(chunkIndex, flags).thenComposeAsync(reference -> {
         if (reference != null && reference.isValid()) {
            long stamp = this.loadedLock.readLock();

            try {
               if (!this.loading.contains(chunkIndex)) {
                  return CompletableFuture.completedFuture(null);
               }
            } finally {
               this.loadedLock.unlockRead(stamp);
            }

            return this._loadChunkAsync(chunkIndex, playerRefComponent, (Ref<ChunkStore>)reference, chunkStore);
         } else {
            long stamp = this.loadedLock.writeLock();

            try {
               this.loading.remove(chunkIndex);
            } finally {
               this.loadedLock.unlockWrite(stamp);
            }

            return CompletableFuture.completedFuture(null);
         }
      }, world).exceptionallyAsync(throwable -> {
         long stamp = this.loadedLock.writeLock();

         try {
            this.loading.remove(chunkIndex);
         } finally {
            this.loadedLock.unlockWrite(stamp);
         }

         ((HytaleLogger.Api)HytaleLogger.getLogger().at(Level.SEVERE).withCause(throwable)).log("Failed to load chunk! %s, %s", chunkX, chunkZ);
         return null;
      });
   }

   @Nonnull
   private CompletableFuture<Void> _loadChunkAsync(
      long chunkIndex, @Nonnull PlayerRef playerRefComponent, @Nonnull Ref<ChunkStore> chunkRef, @Nonnull ChunkStore chunkComponentStore
   ) {
      List<ToClientPacket> packets = new ObjectArrayList();
      chunkComponentStore.getStore().fetch(Collections.singletonList(chunkRef), ChunkStore.LOAD_PACKETS_DATA_QUERY_SYSTEM_TYPE, playerRefComponent, packets);
      ObjectArrayList<CompletableFuture<ToClientPacket>> futurePackets = new ObjectArrayList();
      chunkComponentStore.getStore()
         .fetch(Collections.singletonList(chunkRef), ChunkStore.LOAD_FUTURE_PACKETS_DATA_QUERY_SYSTEM_TYPE, playerRefComponent, futurePackets);
      return CompletableFuture.allOf((CompletableFuture<?>[])futurePackets.toArray(CompletableFuture[]::new)).thenAcceptAsync(o -> {
         Iterator i$ = futurePackets.iterator();

         while (i$.hasNext()) {
            CompletableFuture<ToClientPacket> futurePacket = (CompletableFuture<ToClientPacket>)i$.next();
            ToClientPacket packet = futurePacket.join();
            if (packet != null) {
               packets.add(packet);
            }
         }

         long writeStamp = this.loadedLock.writeLock();

         try {
            if (this.loading.remove(chunkIndex)) {
               for (int i = 0; i < packets.size(); i++) {
                  playerRefComponent.getPacketHandler().write(packets.get(i));
               }

               this.loaded.add(chunkIndex);
            }
         } finally {
            this.loadedLock.unlockWrite(writeStamp);
         }
      });
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new ChunkTracker(this);
   }

   public static enum ChunkVisibility {
      NONE,
      HOT,
      COLD;
   }
}
