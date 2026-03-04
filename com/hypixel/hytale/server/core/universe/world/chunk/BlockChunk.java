package com.hypixel.hytale.server.core.universe.world.chunk;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.function.predicate.ObjectPositionBlockFunction;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.CachedPacket;
import com.hypixel.hytale.protocol.Opacity;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.packets.world.SetChunkEnvironments;
import com.hypixel.hytale.protocol.packets.world.SetChunkHeightmap;
import com.hypixel.hytale.protocol.packets.world.SetChunkTintmap;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickStrategy;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.LegacyModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.chunk.environment.EnvironmentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.environment.EnvironmentColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.palette.IntBytePalette;
import com.hypixel.hytale.server.core.universe.world.chunk.palette.ShortBytePalette;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.util.io.ByteBufUtil;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.Int2ShortMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.lang.ref.SoftReference;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockChunk implements Component<ChunkStore> {
   public static final int VERSION = 3;
   public static final BuilderCodec<BlockChunk> CODEC = BuilderCodec.builder(BlockChunk.class, BlockChunk::new)
      .versioned()
      .codecVersion(3)
      .append(new KeyedCodec<>("Data", Codec.BYTE_ARRAY), BlockChunk::deserialize, BlockChunk::serialize)
      .add()
      .build();
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   public static boolean SEND_LOCAL_LIGHTING_DATA = true;
   public static boolean SEND_GLOBAL_LIGHTING_DATA = false;
   private long index;
   private int x;
   private int z;
   private final ShortBytePalette height;
   private final IntBytePalette tint;
   @Deprecated(forRemoval = true)
   private BlockSection[] chunkSections;
   @Nullable
   @Deprecated(forRemoval = true)
   private BlockSection[] migratedChunkSections;
   private EnvironmentChunk environments;
   private boolean needsPhysics = true;
   private boolean needsSaving = false;
   @Nullable
   private transient SoftReference<CompletableFuture<CachedPacket<SetChunkHeightmap>>> cachedHeightmapPacket;
   @Nullable
   private transient SoftReference<CompletableFuture<CachedPacket<SetChunkTintmap>>> cachedTintmapPacket;
   @Nullable
   private transient SoftReference<CompletableFuture<CachedPacket<SetChunkEnvironments>>> cachedEnvironmentsPacket;

   public static ComponentType<ChunkStore, BlockChunk> getComponentType() {
      return LegacyModule.get().getBlockChunkComponentType();
   }

   private BlockChunk() {
      this(new ShortBytePalette(), new IntBytePalette(), new EnvironmentChunk(), new BlockSection[10]);
   }

   public void load(int x, int z) {
      this.x = x;
      this.z = z;
      this.index = ChunkUtil.indexChunk(x, z);
   }

   public BlockChunk(int x, int z) {
      this(x, z, new ShortBytePalette(), new IntBytePalette(), new EnvironmentChunk());
   }

   public BlockChunk(int x, int z, ShortBytePalette height, IntBytePalette tint, EnvironmentChunk environments) {
      this(height, tint, environments, new BlockSection[10]);
      this.x = x;
      this.z = z;
      this.index = ChunkUtil.indexChunk(x, z);
   }

   public BlockChunk(ShortBytePalette height, IntBytePalette tint, EnvironmentChunk environments, BlockSection[] chunkSections) {
      this.height = height;
      this.tint = tint;
      this.environments = environments;
      this.chunkSections = chunkSections;
   }

   @Override
   public Component<ChunkStore> clone() {
      throw new UnsupportedOperationException("Not implemented!");
   }

   @Nonnull
   @Override
   public Component<ChunkStore> cloneSerializable() {
      return this;
   }

   public long getIndex() {
      return this.index;
   }

   public int getX() {
      return this.x;
   }

   public int getZ() {
      return this.z;
   }

   public EnvironmentChunk getEnvironmentChunk() {
      return this.environments;
   }

   public void setEnvironmentChunk(EnvironmentChunk environmentChunk) {
      this.environments = environmentChunk;
   }

   public short getHeight(int x, int z) {
      return this.height.get(x, z);
   }

   public short getHeight(int index) {
      return this.height.get(index);
   }

   public void setHeight(int x, int z, short height) {
      this.height.set(x, z, height);
      this.cachedHeightmapPacket = null;
      this.markNeedsSaving();
   }

   public void updateHeightmap() {
      for (int cx = 0; cx < 32; cx++) {
         for (int cz = 0; cz < 32; cz++) {
            this.updateHeight(cx, cz);
         }
      }
   }

   public short updateHeight(int x, int z) {
      return this.updateHeight(x, z, (short)320);
   }

   public short updateHeight(int x, int z, short startY) {
      short y = startY;

      while (--y > 0) {
         BlockSection section = this.getSectionAtBlockY(y);
         if (section.isSolidAir()) {
            y = (short)(ChunkUtil.indexSection(y) * 32);
            if (y == 0) {
               break;
            }
         } else {
            int blockId = section.get(x, y, z);
            BlockType type = BlockType.getAssetMap().getAsset(blockId);
            if (blockId != 0 && type != null && type.getOpacity() != Opacity.Transparent) {
               break;
            }
         }
      }

      this.setHeight(x, z, y);
      return y;
   }

   @Deprecated(forRemoval = true)
   public void loadFromHolder(@Nonnull Holder<ChunkStore> holder) {
      ChunkColumn column = holder.getComponent(ChunkColumn.getComponentType());
      if (column != null) {
         Holder<ChunkStore>[] sections = column.getSectionHolders();

         for (int i = 0; i < sections.length; i++) {
            Holder<ChunkStore> section = sections[i];
            this.chunkSections[i] = this.migratedChunkSections != null
               ? this.migratedChunkSections[i]
               : section.ensureAndGetComponent(BlockSection.getComponentType());
         }
      }
   }

   @Deprecated(forRemoval = false)
   public BlockSection getSectionAtIndex(int index) {
      if (index >= 0 && index < this.chunkSections.length) {
         return this.chunkSections[index];
      } else {
         throw new IllegalArgumentException("Section index must >=0 and <" + this.chunkSections.length + " but was given " + index);
      }
   }

   @Deprecated(forRemoval = false)
   public BlockSection getSectionAtBlockY(int y) {
      int index = ChunkUtil.indexSection(y);
      if (index >= 0 && index < this.chunkSections.length) {
         return this.chunkSections[index];
      } else {
         throw new IllegalArgumentException("Section y must >=0 and <320 but was given " + y);
      }
   }

   @Deprecated(forRemoval = false)
   public BlockSection[] getChunkSections() {
      return this.chunkSections;
   }

   public int getSectionCount() {
      return this.chunkSections.length;
   }

   public int getTint(int x, int z) {
      return this.tint.get(x, z);
   }

   public void setTint(int x, int z, int tint) {
      this.tint.set(x, z, tint);
      this.cachedTintmapPacket = null;
      this.markNeedsSaving();
   }

   public int getEnvironment(@Nonnull Vector3d position) {
      return this.getEnvironment(MathUtil.floor(position.x), MathUtil.floor(position.y), MathUtil.floor(position.z));
   }

   public int getEnvironment(@Nonnull Vector3i position) {
      return this.getEnvironment(position.x, position.y, position.z);
   }

   public int getEnvironment(int x, int y, int z) {
      return y >= 0 && y < 320 ? this.environments.get(x, y, z) : 0;
   }

   public EnvironmentColumn getEnvironmentColumn(int x, int z) {
      return this.environments.get(x, z);
   }

   public void setEnvironment(int x, int y, int z, int environment) {
      if (y >= 0 && y < 320) {
         this.environments.set(x, y, z, environment);
         this.cachedEnvironmentsPacket = null;
         this.markNeedsSaving();
      }
   }

   public byte getRedBlockLight(int x, int y, int z) {
      return y >= 0 && y < 320 ? this.getSectionAtBlockY(y).getGlobalLight().getRedBlockLight(x, y, z) : 0;
   }

   public byte getGreenBlockLight(int x, int y, int z) {
      return y >= 0 && y < 320 ? this.getSectionAtBlockY(y).getGlobalLight().getGreenBlockLight(x, y, z) : 0;
   }

   public byte getBlueBlockLight(int x, int y, int z) {
      return y >= 0 && y < 320 ? this.getSectionAtBlockY(y).getGlobalLight().getBlueBlockLight(x, y, z) : 0;
   }

   public short getBlockLight(int x, int y, int z) {
      return y >= 0 && y < 320 ? this.getSectionAtBlockY(y).getGlobalLight().getBlockLight(x, y, z) : 0;
   }

   public byte getSkyLight(int x, int y, int z) {
      return y >= 0 && y < 320 ? this.getSectionAtBlockY(y).getGlobalLight().getSkyLight(x, y, z) : 0;
   }

   public byte getBlockLightIntensity(int x, int y, int z) {
      return y >= 0 && y < 320 ? this.getSectionAtBlockY(y).getGlobalLight().getBlockLightIntensity(x, y, z) : 0;
   }

   public int getBlock(int x, int y, int z) {
      return y >= 0 && y < 320 ? this.getSectionAtBlockY(y).get(x, y, z) : 0;
   }

   public boolean setBlock(int x, int y, int z, int blockId, int rotation, int filler) {
      if (y >= 0 && y < 320) {
         int sectionIndex = ChunkUtil.indexSection(y);
         BlockSection section = this.chunkSections[sectionIndex];
         boolean changed = section.set(x, y, z, blockId, rotation, filler);
         if (changed) {
            this.invalidateChunkSection(sectionIndex);
            this.markNeedsSaving();
         }

         return changed;
      } else {
         throw new IllegalArgumentException(String.format("Failed to set block at %d, %d, %d to %d because it is outside the world bounds", x, y, z, blockId));
      }
   }

   public boolean contains(int blockId) {
      return this.count(blockId) != 0;
   }

   public int count(int blockId) {
      int count = 0;

      for (BlockSection section : this.chunkSections) {
         count += section.count(blockId);
      }

      return count;
   }

   @Nonnull
   public Int2IntMap blockCounts() {
      Int2IntMap map = new Int2IntOpenHashMap();

      for (BlockSection section : this.chunkSections) {
         ObjectIterator var6 = section.valueCounts().int2ShortEntrySet().iterator();

         while (var6.hasNext()) {
            Entry entry = (Entry)var6.next();
            int blockId = entry.getIntKey();
            short count = entry.getShortValue();
            map.mergeInt(blockId, count, Integer::sum);
         }
      }

      return map;
   }

   @Nonnull
   public IntSet blocks() {
      IntSet set = new IntOpenHashSet();

      for (BlockSection section : this.chunkSections) {
         set.addAll(section.values());
      }

      return set;
   }

   public int blockCount() {
      return this.blocks().size();
   }

   public void preTick(Instant gameTime) {
      for (int sectionIndex = 0; sectionIndex < this.chunkSections.length; sectionIndex++) {
         this.chunkSections[sectionIndex].preTick(gameTime);
      }
   }

   public <T, V> int forEachTicking(T t, V v, ObjectPositionBlockFunction<T, V, BlockTickStrategy> acceptor) {
      int ticked = 0;

      for (int sectionIndex = 0; sectionIndex < this.chunkSections.length; sectionIndex++) {
         BlockSection section = this.chunkSections[sectionIndex];
         ticked += section.forEachTicking(t, v, sectionIndex, acceptor);
      }

      if (ticked > 0) {
         this.markNeedsSaving();
      }

      return ticked;
   }

   public void mergeTickingBlocks() {
      for (BlockSection section : this.chunkSections) {
         section.mergeTickingBlocks();
      }
   }

   public boolean setTicking(int x, int y, int z, boolean ticking) {
      if (y >= 0 && y < 320) {
         boolean changed = this.getSectionAtBlockY(y).setTicking(x, y, z, ticking);
         if (changed) {
            this.markNeedsSaving();
         }

         return changed;
      } else {
         return false;
      }
   }

   public boolean isTicking(int x, int y, int z) {
      return y >= 0 && y < 320 ? this.getSectionAtBlockY(y).isTicking(x, y, z) : false;
   }

   public int getTickingBlocksCount() {
      int ticking = 0;

      for (BlockSection chunkSection : this.chunkSections) {
         ticking += chunkSection.getTickingBlocksCount();
      }

      return ticking;
   }

   public boolean setNeighbourBlocksTicking(int x, int y, int z) {
      boolean success = true;

      for (int ix = -1; ix < 2; ix++) {
         int wx = x + ix;

         for (int iz = -1; iz < 2; iz++) {
            int wz = z + iz;
            if (!ChunkUtil.isInsideChunkRelative(wx, wz)) {
               success = false;
            } else {
               for (int iy = -1; iy < 2; iy++) {
                  int wy = y + iy;
                  this.setTicking(wx, wy, wz, true);
               }
            }
         }
      }

      return success;
   }

   public void markNeedsSaving() {
      this.needsSaving = true;
   }

   public boolean getNeedsSaving() {
      return this.needsSaving;
   }

   public boolean consumeNeedsSaving() {
      boolean out = this.needsSaving;
      this.needsSaving = false;
      return out;
   }

   public void markNeedsPhysics() {
      this.needsPhysics = true;
   }

   public boolean consumeNeedsPhysics() {
      boolean old = this.needsPhysics;
      this.needsPhysics = false;
      return old;
   }

   public void invalidateChunkSection(int sectionIndex) {
      this.chunkSections[sectionIndex].invalidate();
   }

   @Nullable
   @Deprecated(forRemoval = true)
   public BlockSection[] takeMigratedSections() {
      BlockSection[] temp = this.migratedChunkSections;
      this.migratedChunkSections = null;
      return temp;
   }

   @Nullable
   @Deprecated(forRemoval = true)
   public BlockSection[] getMigratedSections() {
      return this.migratedChunkSections;
   }

   private byte[] serialize(ExtraInfo extraInfo) {
      ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();

      try {
         buf.writeBoolean(this.needsPhysics);
         this.height.serialize(buf);
         this.tint.serialize(buf);
         return ByteBufUtil.getBytesRelease(buf);
      } catch (Throwable var4) {
         buf.release();
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   private void deserialize(@Nonnull byte[] bytes, @Nonnull ExtraInfo extraInfo) {
      ByteBuf buf = Unpooled.wrappedBuffer(bytes);
      this.needsPhysics = buf.readBoolean();
      this.height.deserialize(buf);
      this.tint.deserialize(buf);
      if (extraInfo.getVersion() <= 2) {
         int sections = buf.readInt();
         this.migratedChunkSections = new BlockSection[sections];

         for (int y = 0; y < sections; y++) {
            BlockSection section = new BlockSection();
            section.deserialize(BlockType.KEY_DESERIALIZER, buf, extraInfo.getVersion());
            this.migratedChunkSections[y] = section;
         }
      }
   }

   @Nonnull
   private CompletableFuture<CachedPacket<SetChunkHeightmap>> getCachedHeightmapPacket() {
      SoftReference<CompletableFuture<CachedPacket<SetChunkHeightmap>>> ref = this.cachedHeightmapPacket;
      CompletableFuture<CachedPacket<SetChunkHeightmap>> future = ref != null ? ref.get() : null;
      if (future != null) {
         return future;
      } else {
         future = CompletableFuture.supplyAsync(() -> {
            SetChunkHeightmap packet = new SetChunkHeightmap(this.x, this.z, this.height.serialize());
            return CachedPacket.cache(packet);
         });
         this.cachedHeightmapPacket = new SoftReference<>(future);
         return future;
      }
   }

   @Nonnull
   private CompletableFuture<CachedPacket<SetChunkTintmap>> getCachedTintsPacket() {
      SoftReference<CompletableFuture<CachedPacket<SetChunkTintmap>>> ref = this.cachedTintmapPacket;
      CompletableFuture<CachedPacket<SetChunkTintmap>> future = ref != null ? ref.get() : null;
      if (future != null) {
         return future;
      } else {
         future = CompletableFuture.supplyAsync(() -> {
            SetChunkTintmap packet = new SetChunkTintmap(this.x, this.z, this.tint.serialize());
            return CachedPacket.cache(packet);
         });
         this.cachedTintmapPacket = new SoftReference<>(future);
         return future;
      }
   }

   @Nonnull
   private CompletableFuture<CachedPacket<SetChunkEnvironments>> getCachedEnvironmentsPacket() {
      SoftReference<CompletableFuture<CachedPacket<SetChunkEnvironments>>> ref = this.cachedEnvironmentsPacket;
      CompletableFuture<CachedPacket<SetChunkEnvironments>> future = ref != null ? ref.get() : null;
      if (future != null) {
         return future;
      } else {
         future = CompletableFuture.supplyAsync(() -> {
            SetChunkEnvironments packet = new SetChunkEnvironments(this.x, this.z, this.environments.serializeProtocol());
            return CachedPacket.cache(packet);
         });
         this.cachedEnvironmentsPacket = new SoftReference<>(future);
         return future;
      }
   }

   public static class LoadBlockChunkPacketSystem extends ChunkStore.LoadFuturePacketDataQuerySystem {
      private final ComponentType<ChunkStore, BlockChunk> componentType;

      public LoadBlockChunkPacketSystem(ComponentType<ChunkStore, BlockChunk> blockChunkComponentType) {
         this.componentType = blockChunkComponentType;
      }

      @Override
      public Query<ChunkStore> getQuery() {
         return this.componentType;
      }

      public void fetch(
         int index,
         @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
         Store<ChunkStore> store,
         CommandBuffer<ChunkStore> commandBuffer,
         PlayerRef player,
         @Nonnull List<CompletableFuture<ToClientPacket>> results
      ) {
         BlockChunk component = archetypeChunk.getComponent(index, this.componentType);
         results.add(component.getCachedHeightmapPacket().exceptionally(throwable -> {
            if (throwable != null) {
               ((HytaleLogger.Api)BlockChunk.LOGGER.at(Level.SEVERE).withCause(throwable)).log("Exception when compressing chunk heightmap:");
            }

            return null;
         }).thenApply(Function.identity()));
         results.add(component.getCachedTintsPacket().exceptionally(throwable -> {
            if (throwable != null) {
               ((HytaleLogger.Api)BlockChunk.LOGGER.at(Level.SEVERE).withCause(throwable)).log("Exception when compressing chunk tints:");
            }

            return null;
         }).thenApply(Function.identity()));
         results.add(component.getCachedEnvironmentsPacket().exceptionally(throwable -> {
            if (throwable != null) {
               ((HytaleLogger.Api)BlockChunk.LOGGER.at(Level.SEVERE).withCause(throwable)).log("Exception when compressing chunk environments:");
            }

            return null;
         }).thenApply(Function.identity()));

         for (int y = 0; y < component.chunkSections.length; y++) {
            BlockSection section = component.chunkSections[y];
            results.add(
               section.getCachedChunkPacket(component.getX(), y, component.getZ())
                  .exceptionally(
                     throwable -> {
                        if (throwable != null) {
                           ((HytaleLogger.Api)BlockChunk.LOGGER.at(Level.SEVERE).withCause(throwable))
                              .log("Exception while compressing set chunk (%d, %d):", component.x, component.z);
                        }

                        return null;
                     }
                  )
                  .thenApply(Function.identity())
            );
         }
      }
   }
}
