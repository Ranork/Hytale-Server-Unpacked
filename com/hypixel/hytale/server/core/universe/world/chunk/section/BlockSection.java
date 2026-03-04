package com.hypixel.hytale.server.core.universe.world.chunk.section;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.util.BitSetUtil;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.function.predicate.ObjectPositionBlockFunction;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.CachedPacket;
import com.hypixel.hytale.protocol.packets.world.PaletteType;
import com.hypixel.hytale.protocol.packets.world.SetChunk;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickStrategy;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockMigration;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.blocktype.component.BlockPhysics;
import com.hypixel.hytale.server.core.modules.LegacyModule;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.palette.EmptySectionPalette;
import com.hypixel.hytale.server.core.universe.world.chunk.section.palette.ISectionPalette;
import com.hypixel.hytale.server.core.universe.world.chunk.section.palette.PaletteTypeEnum;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import com.hypixel.hytale.server.core.util.io.ByteBufUtil;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ShortMap;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;
import java.lang.ref.SoftReference;
import java.time.Instant;
import java.util.BitSet;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.ToIntFunction;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockSection implements Component<ChunkStore> {
   public static final int VERSION = 6;
   public static final BuilderCodec<BlockSection> CODEC = BuilderCodec.builder(BlockSection.class, BlockSection::new)
      .versioned()
      .codecVersion(6)
      .append(new KeyedCodec<>("Data", Codec.BYTE_ARRAY), BlockSection::deserialize, BlockSection::serialize)
      .add()
      .build();
   private final StampedLock chunkSectionLock = new StampedLock();
   public boolean loaded = false;
   @Nonnull
   private IntOpenHashSet changedPositions = new IntOpenHashSet(0);
   @Nonnull
   private IntOpenHashSet swapChangedPositions = new IntOpenHashSet(0);
   private ISectionPalette chunkSection;
   private ISectionPalette fillerSection;
   private ISectionPalette rotationSection;
   private ChunkLightData localLight;
   private short localChangeCounter;
   private ChunkLightData globalLight;
   private short globalChangeCounter;
   private BitSet tickingBlocks;
   private final BitSet tickingBlocksCopy;
   @Nonnull
   private final BitSet tickingWaitAdjacentBlocks;
   private int tickingBlocksCount;
   private int tickingBlocksCountCopy;
   private int tickingWaitAdjacentBlockCount;
   private final ObjectHeapPriorityQueue<BlockSection.TickRequest> tickRequests;
   private double maximumHitboxExtent;
   @Nullable
   private transient SoftReference<CompletableFuture<CachedPacket<SetChunk>>> cachedChunkPacket;
   @Nullable
   @Deprecated(forRemoval = true)
   private FluidSection migratedFluidSection;
   @Nullable
   @Deprecated(forRemoval = true)
   private BlockPhysics migratedBlockPhysics;
   private static final Comparator<BlockSection.TickRequest> TICK_REQUEST_COMPARATOR = Comparator.comparing(t -> t.requestedGameTime);

   public static ComponentType<ChunkStore, BlockSection> getComponentType() {
      return LegacyModule.get().getBlockSectionComponentType();
   }

   public BlockSection() {
      this(EmptySectionPalette.INSTANCE, EmptySectionPalette.INSTANCE, EmptySectionPalette.INSTANCE);
   }

   public BlockSection(ISectionPalette chunkSection, ISectionPalette fillerSection, ISectionPalette rotationSection) {
      this.tickRequests = new ObjectHeapPriorityQueue(TICK_REQUEST_COMPARATOR);
      this.maximumHitboxExtent = -1.0;
      this.chunkSection = chunkSection;
      this.fillerSection = fillerSection;
      this.rotationSection = rotationSection;
      this.tickingBlocks = new BitSet();
      this.tickingBlocksCopy = new BitSet();
      this.tickingWaitAdjacentBlocks = new BitSet();
      this.tickingBlocksCount = 0;
      this.tickingBlocksCountCopy = 0;
      this.localLight = ChunkLightData.EMPTY;
      this.localChangeCounter = 0;
      this.globalLight = ChunkLightData.EMPTY;
      this.globalChangeCounter = 0;
   }

   public ISectionPalette getChunkSection() {
      return this.chunkSection;
   }

   public void setChunkSection(ISectionPalette chunkSection) {
      this.chunkSection = chunkSection;
   }

   public void setLocalLight(@Nonnull ChunkLightDataBuilder localLight) {
      Objects.requireNonNull(localLight);
      this.localLight = localLight.build();
   }

   public void setGlobalLight(@Nonnull ChunkLightDataBuilder globalLight) {
      Objects.requireNonNull(globalLight);
      this.globalLight = globalLight.build();
   }

   public ChunkLightData getLocalLight() {
      return this.localLight;
   }

   public ChunkLightData getGlobalLight() {
      return this.globalLight;
   }

   public boolean hasLocalLight() {
      return this.localLight.getChangeId() == this.localChangeCounter;
   }

   public boolean hasGlobalLight() {
      return this.globalLight.getChangeId() == this.globalChangeCounter;
   }

   public void invalidateLocalLight() {
      this.localChangeCounter++;
      this.invalidateGlobalLight();
   }

   public void invalidateGlobalLight() {
      this.globalChangeCounter++;
   }

   public short getLocalChangeCounter() {
      return this.localChangeCounter;
   }

   public short getGlobalChangeCounter() {
      return this.globalChangeCounter;
   }

   public void invalidate() {
      this.cachedChunkPacket = null;
   }

   public int get(int index) {
      long lock = this.chunkSectionLock.tryOptimisticRead();
      int i = this.chunkSection.get(index);
      if (!this.chunkSectionLock.validate(lock)) {
         lock = this.chunkSectionLock.readLock();

         int var5;
         try {
            var5 = this.chunkSection.get(index);
         } finally {
            this.chunkSectionLock.unlockRead(lock);
         }

         return var5;
      } else {
         return i;
      }
   }

   public int getFiller(int index) {
      long lock = this.chunkSectionLock.tryOptimisticRead();
      int i = this.fillerSection.get(index);
      if (!this.chunkSectionLock.validate(lock)) {
         lock = this.chunkSectionLock.readLock();

         int var5;
         try {
            var5 = this.fillerSection.get(index);
         } finally {
            this.chunkSectionLock.unlockRead(lock);
         }

         return var5;
      } else {
         return i;
      }
   }

   public int getFiller(int x, int y, int z) {
      return this.getFiller(ChunkUtil.indexBlock(x, y, z));
   }

   public int getRotationIndex(int index) {
      long lock = this.chunkSectionLock.tryOptimisticRead();
      int i = this.rotationSection.get(index);
      if (!this.chunkSectionLock.validate(lock)) {
         lock = this.chunkSectionLock.readLock();

         int var5;
         try {
            var5 = this.rotationSection.get(index);
         } finally {
            this.chunkSectionLock.unlockRead(lock);
         }

         return var5;
      } else {
         return i;
      }
   }

   public int getRotationIndex(int x, int y, int z) {
      return this.getRotationIndex(ChunkUtil.indexBlock(x, y, z));
   }

   public RotationTuple getRotation(int index) {
      return RotationTuple.get(this.getRotationIndex(index));
   }

   public RotationTuple getRotation(int x, int y, int z) {
      return this.getRotation(ChunkUtil.indexBlock(x, y, z));
   }

   public boolean set(int blockIdx, int blockId, int rotation, int filler) {
      if (rotation >= 0 && rotation < RotationTuple.VALUES.length) {
         long lock = this.chunkSectionLock.writeLock();

         boolean changed;
         try {
            ISectionPalette.SetResult result = this.chunkSection.set(blockIdx, blockId);
            if (result == ISectionPalette.SetResult.REQUIRES_PROMOTE) {
               this.chunkSection = this.chunkSection.promote();
               ISectionPalette.SetResult repeatResult = this.chunkSection.set(blockIdx, blockId);
               if (repeatResult != ISectionPalette.SetResult.ADDED_OR_REMOVED) {
                  throw new IllegalStateException("Promoted chunk section failed to correctly add the new block!");
               }
            } else {
               if (result == ISectionPalette.SetResult.ADDED_OR_REMOVED) {
                  this.maximumHitboxExtent = -1.0;
               }

               if (this.chunkSection.shouldDemote()) {
                  this.chunkSection = this.chunkSection.demote();
               }
            }

            changed = result != ISectionPalette.SetResult.UNCHANGED;
            result = this.fillerSection.set(blockIdx, filler);
            if (result == ISectionPalette.SetResult.REQUIRES_PROMOTE) {
               this.fillerSection = this.fillerSection.promote();
               ISectionPalette.SetResult repeatResult = this.fillerSection.set(blockIdx, filler);
               if (repeatResult != ISectionPalette.SetResult.ADDED_OR_REMOVED) {
                  throw new IllegalStateException("Promoted chunk section failed to correctly add the new block!");
               }
            } else if (this.fillerSection.shouldDemote()) {
               this.fillerSection = this.fillerSection.demote();
            }

            changed |= result != ISectionPalette.SetResult.UNCHANGED;
            result = this.rotationSection.set(blockIdx, rotation);
            if (result == ISectionPalette.SetResult.REQUIRES_PROMOTE) {
               this.rotationSection = this.rotationSection.promote();
               ISectionPalette.SetResult repeatResult = this.rotationSection.set(blockIdx, rotation);
               if (repeatResult != ISectionPalette.SetResult.ADDED_OR_REMOVED) {
                  throw new IllegalStateException("Promoted chunk section failed to correctly add the new block!");
               }
            } else if (this.rotationSection.shouldDemote()) {
               this.rotationSection = this.rotationSection.demote();
            }

            changed |= result != ISectionPalette.SetResult.UNCHANGED;
            if (changed && this.loaded) {
               this.changedPositions.add(blockIdx);
            }
         } finally {
            this.chunkSectionLock.unlockWrite(lock);
         }

         if (changed) {
            this.invalidateLocalLight();
         }

         return changed;
      } else {
         throw new IllegalArgumentException("Rotation index out of bounds. Got " + rotation + " but expected 0-" + (RotationTuple.VALUES.length - 1));
      }
   }

   @Nonnull
   public IntOpenHashSet getAndClearChangedPositions() {
      long stamp = this.chunkSectionLock.writeLock();

      IntOpenHashSet var4;
      try {
         this.swapChangedPositions.clear();
         IntOpenHashSet tmp = this.changedPositions;
         this.changedPositions = this.swapChangedPositions;
         this.swapChangedPositions = tmp;
         var4 = tmp;
      } finally {
         this.chunkSectionLock.unlockWrite(stamp);
      }

      return var4;
   }

   public boolean contains(int id) {
      long lock = this.chunkSectionLock.tryOptimisticRead();
      boolean contains = this.chunkSection.contains(id);
      if (!this.chunkSectionLock.validate(lock)) {
         lock = this.chunkSectionLock.readLock();

         boolean var5;
         try {
            var5 = this.chunkSection.contains(id);
         } finally {
            this.chunkSectionLock.unlockRead(lock);
         }

         return var5;
      } else {
         return contains;
      }
   }

   public boolean containsAny(IntList ids) {
      long lock = this.chunkSectionLock.tryOptimisticRead();
      boolean contains = this.chunkSection.containsAny(ids);
      if (!this.chunkSectionLock.validate(lock)) {
         lock = this.chunkSectionLock.readLock();

         boolean var5;
         try {
            var5 = this.chunkSection.containsAny(ids);
         } finally {
            this.chunkSectionLock.unlockRead(lock);
         }

         return var5;
      } else {
         return contains;
      }
   }

   public int count() {
      long lock = this.chunkSectionLock.tryOptimisticRead();
      int count = this.chunkSection.count();
      if (!this.chunkSectionLock.validate(lock)) {
         lock = this.chunkSectionLock.readLock();

         int var4;
         try {
            var4 = this.chunkSection.count();
         } finally {
            this.chunkSectionLock.unlockRead(lock);
         }

         return var4;
      } else {
         return count;
      }
   }

   public int count(int id) {
      long lock = this.chunkSectionLock.tryOptimisticRead();
      int count = this.chunkSection.count(id);
      if (!this.chunkSectionLock.validate(lock)) {
         lock = this.chunkSectionLock.readLock();

         int var5;
         try {
            var5 = this.chunkSection.count(id);
         } finally {
            this.chunkSectionLock.unlockRead(lock);
         }

         return var5;
      } else {
         return count;
      }
   }

   public IntSet values() {
      long lock = this.chunkSectionLock.tryOptimisticRead();
      IntSet values = this.chunkSection.values();
      if (!this.chunkSectionLock.validate(lock)) {
         lock = this.chunkSectionLock.readLock();

         IntSet var4;
         try {
            var4 = this.chunkSection.values();
         } finally {
            this.chunkSectionLock.unlockRead(lock);
         }

         return var4;
      } else {
         return values;
      }
   }

   public void forEachValue(IntConsumer consumer) {
      long lock = this.chunkSectionLock.readLock();

      try {
         this.chunkSection.forEachValue(consumer);
      } finally {
         this.chunkSectionLock.unlockRead(lock);
      }
   }

   public Int2ShortMap valueCounts() {
      long lock = this.chunkSectionLock.tryOptimisticRead();
      Int2ShortMap valueCounts = this.chunkSection.valueCounts();
      if (!this.chunkSectionLock.validate(lock)) {
         lock = this.chunkSectionLock.readLock();

         Int2ShortMap var4;
         try {
            var4 = this.chunkSection.valueCounts();
         } finally {
            this.chunkSectionLock.unlockRead(lock);
         }

         return var4;
      } else {
         return valueCounts;
      }
   }

   public boolean isSolidAir() {
      long lock = this.chunkSectionLock.tryOptimisticRead();
      boolean isSolid = this.chunkSection.isSolid(0);
      if (!this.chunkSectionLock.validate(lock)) {
         lock = this.chunkSectionLock.readLock();

         boolean var4;
         try {
            var4 = this.chunkSection.isSolid(0);
         } finally {
            this.chunkSectionLock.unlockRead(lock);
         }

         return var4;
      } else {
         return isSolid;
      }
   }

   public void find(IntList ids, IntSet internalIdHolder, IntConsumer indexConsumer) {
      long lock = this.chunkSectionLock.readLock();

      try {
         this.chunkSection.find(ids, internalIdHolder, indexConsumer);
      } finally {
         this.chunkSectionLock.unlockRead(lock);
      }
   }

   public boolean setTicking(int blockIdx, boolean ticking) {
      long readStamp = this.chunkSectionLock.readLock();

      try {
         if (this.tickingBlocks.get(blockIdx) == ticking) {
            return false;
         }
      } finally {
         this.chunkSectionLock.unlockRead(readStamp);
      }

      long writeStamp = this.chunkSectionLock.writeLock();

      boolean var7;
      try {
         if (this.tickingBlocks.get(blockIdx) == ticking) {
            return false;
         }

         if (ticking) {
            this.tickingBlocksCount++;
         } else {
            this.tickingBlocksCount--;
         }

         this.tickingBlocks.set(blockIdx, ticking);
         var7 = true;
      } finally {
         this.chunkSectionLock.unlockWrite(writeStamp);
      }

      return var7;
   }

   public int getTickingBlocksCount() {
      return this.tickingBlocksCount > 0 ? this.tickingBlocksCount : 0;
   }

   public int getTickingBlocksCountCopy() {
      return this.tickingBlocksCountCopy;
   }

   public boolean hasTicking() {
      return this.tickingBlocksCount > 0;
   }

   public boolean isTicking(int blockIdx) {
      if (this.tickingBlocksCount > 0) {
         long readStamp = this.chunkSectionLock.readLock();

         boolean var4;
         try {
            var4 = this.tickingBlocks.get(blockIdx);
         } finally {
            this.chunkSectionLock.unlockRead(readStamp);
         }

         return var4;
      } else {
         return false;
      }
   }

   public void scheduleTick(int index, @Nullable Instant gameTime) {
      if (gameTime != null) {
         this.tickRequests.enqueue(new BlockSection.TickRequest(index, gameTime));
      }
   }

   public void preTick(Instant gameTime) {
      BlockSection.TickRequest request;
      while (!this.tickRequests.isEmpty() && (request = (BlockSection.TickRequest)this.tickRequests.first()).requestedGameTime.isBefore(gameTime)) {
         this.tickRequests.dequeue();
         this.setTicking(request.index, true);
      }

      long writeStamp = this.chunkSectionLock.writeLock();

      try {
         if (this.tickingBlocksCount != 0) {
            BitSetUtil.copyValues(this.tickingBlocks, this.tickingBlocksCopy);
            this.tickingBlocksCountCopy = this.tickingBlocksCount;
            this.tickingBlocks.clear();
            this.tickingBlocksCount = 0;
            return;
         }

         this.tickingBlocksCountCopy = 0;
      } finally {
         this.chunkSectionLock.unlockWrite(writeStamp);
      }
   }

   public <T, V> int forEachTicking(T t, V v, int sectionIndex, @Nonnull ObjectPositionBlockFunction<T, V, BlockTickStrategy> acceptor) {
      if (this.tickingBlocksCountCopy == 0) {
         return 0;
      } else {
         int sectionStartYBlock = sectionIndex << 5;
         int ticked = 0;

         for (int index = this.tickingBlocksCopy.nextSetBit(0); index >= 0; index = this.tickingBlocksCopy.nextSetBit(index + 1)) {
            int x = ChunkUtil.xFromIndex(index);
            int y = ChunkUtil.yFromIndex(index);
            int z = ChunkUtil.zFromIndex(index);
            BlockTickStrategy strategy = acceptor.accept(t, v, x, y | sectionStartYBlock, z, this.get(index));
            long writeStamp = this.chunkSectionLock.writeLock();

            try {
               switch (strategy) {
                  case WAIT_FOR_ADJACENT_CHUNK_LOAD:
                     if (!this.tickingWaitAdjacentBlocks.get(index)) {
                        this.tickingWaitAdjacentBlockCount++;
                        this.tickingWaitAdjacentBlocks.set(index, true);
                     }
                     break;
                  case CONTINUE:
                     if (!this.tickingBlocks.get(index)) {
                        this.tickingBlocksCount++;
                        this.tickingBlocks.set(index, true);
                     }
               }
            } finally {
               this.chunkSectionLock.unlockWrite(writeStamp);
            }

            ticked++;
         }

         return ticked;
      }
   }

   public void mergeTickingBlocks() {
      long writeStamp = this.chunkSectionLock.writeLock();

      try {
         this.tickingBlocks.or(this.tickingWaitAdjacentBlocks);
         this.tickingBlocksCount = this.tickingBlocks.cardinality();
         this.tickingWaitAdjacentBlocks.clear();
         this.tickingWaitAdjacentBlockCount = 0;
      } finally {
         this.chunkSectionLock.unlockWrite(writeStamp);
      }
   }

   public double getMaximumHitboxExtent() {
      double extent = this.maximumHitboxExtent;
      if (extent != -1.0) {
         return extent;
      } else {
         double maximumExtent = BlockBoundingBoxes.UNIT_BOX_MAXIMUM_EXTENT;
         long lock = this.chunkSectionLock.readLock();

         try {
            IndexedLookupTableAssetMap<String, BlockBoundingBoxes> hitBoxAssetMap = BlockBoundingBoxes.getAssetMap();
            BlockTypeAssetMap<String, BlockType> blockTypeMap = BlockType.getAssetMap();

            for (int idx = 0; idx < 32768; idx++) {
               int blockId = this.chunkSection.get(idx);
               if (blockId != 0) {
                  int rotation = this.rotationSection.get(idx);
                  BlockType blockType = blockTypeMap.getAsset(blockId);
                  if (blockType != null && !blockType.isUnknown()) {
                     BlockBoundingBoxes asset = hitBoxAssetMap.getAsset(blockType.getHitboxTypeIndex());
                     if (asset != BlockBoundingBoxes.UNIT_BOX) {
                        double boxMaximumExtent = asset.get(rotation).getBoundingBox().getMaximumExtent();
                        if (boxMaximumExtent > maximumExtent) {
                           maximumExtent = boxMaximumExtent;
                        }
                     }
                  }
               }
            }
         } finally {
            this.chunkSectionLock.unlockRead(lock);
         }

         return this.maximumHitboxExtent = maximumExtent;
      }
   }

   @Deprecated
   public void invalidateBlock(int x, int y, int z) {
      long stamp = this.chunkSectionLock.writeLock();

      try {
         this.changedPositions.add(ChunkUtil.indexBlock(x, y, z));
      } finally {
         this.chunkSectionLock.unlockWrite(stamp);
      }
   }

   @Nullable
   @Deprecated(forRemoval = true)
   public FluidSection takeMigratedFluid() {
      FluidSection temp = this.migratedFluidSection;
      this.migratedFluidSection = null;
      return temp;
   }

   @Nullable
   @Deprecated(forRemoval = true)
   public BlockPhysics takeMigratedDecoBlocks() {
      BlockPhysics temp = this.migratedBlockPhysics;
      this.migratedBlockPhysics = null;
      return temp;
   }

   public void serializeForPacket(@Nonnull ByteBuf buf) {
      long lock = this.chunkSectionLock.readLock();

      try {
         PaletteType paletteType = this.chunkSection.getPaletteType();
         byte paletteTypeId = (byte)paletteType.ordinal();
         buf.writeByte(paletteTypeId);
         this.chunkSection.serializeForPacket(buf);
         PaletteType fillerType = this.fillerSection.getPaletteType();
         byte fillerTypeId = (byte)fillerType.ordinal();
         buf.writeByte(fillerTypeId);
         this.fillerSection.serializeForPacket(buf);
         PaletteType rotationType = this.rotationSection.getPaletteType();
         byte rotationTypeId = (byte)rotationType.ordinal();
         buf.writeByte(rotationTypeId);
         this.rotationSection.serializeForPacket(buf);
      } finally {
         this.chunkSectionLock.unlockRead(lock);
      }
   }

   public void serialize(ISectionPalette.KeySerializer keySerializer, @Nonnull ByteBuf buf) {
      long lock = this.chunkSectionLock.readLock();

      try {
         buf.writeInt(BlockMigration.getAssetMap().getAssetCount());
         PaletteType paletteType = this.chunkSection.getPaletteType();
         buf.writeByte(paletteType.ordinal());
         this.chunkSection.serialize(keySerializer, buf);
         if (paletteType != PaletteType.Empty) {
            BitSet combinedTickingBlock = (BitSet)this.tickingBlocks.clone();
            combinedTickingBlock.or(this.tickingWaitAdjacentBlocks);
            buf.writeShort(combinedTickingBlock.cardinality());
            long[] data = combinedTickingBlock.toLongArray();
            buf.writeShort(data.length);

            for (long l : data) {
               buf.writeLong(l);
            }
         }

         buf.writeByte(this.fillerSection.getPaletteType().ordinal());
         this.fillerSection.serialize(ByteBuf::writeShort, buf);
         buf.writeByte(this.rotationSection.getPaletteType().ordinal());
         this.rotationSection.serialize(ByteBuf::writeByte, buf);
         this.localLight.serialize(buf);
         this.globalLight.serialize(buf);
         buf.writeShort(this.localChangeCounter);
         buf.writeShort(this.globalChangeCounter);
      } finally {
         this.chunkSectionLock.unlockRead(lock);
      }
   }

   public byte[] serialize(ExtraInfo extraInfo) {
      ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();

      try {
         this.serialize(BlockType.KEY_SERIALIZER, buf);
         return ByteBufUtil.getBytesRelease(buf);
      } catch (Throwable var4) {
         buf.release();
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public void deserialize(ToIntFunction<ByteBuf> keyDeserializer, @Nonnull ByteBuf buf, int version) {
      int blockMigrationVersion = 0;
      if (version >= 6) {
         blockMigrationVersion = buf.readInt();
      }

      Function<String, String> blockMigration = null;
      Map<Integer, BlockMigration> blockMigrationMap = BlockMigration.getAssetMap().getAssetMap();

      for (BlockMigration migration = blockMigrationMap.get(blockMigrationVersion);
         migration != null;
         migration = blockMigrationMap.get(++blockMigrationVersion)
      ) {
         if (blockMigration == null) {
            blockMigration = migration::getMigration;
         } else {
            blockMigration = blockMigration.andThen(migration::getMigration);
         }
      }

      PaletteTypeEnum typeEnum = PaletteTypeEnum.get(buf.readByte());
      PaletteType paletteType = typeEnum.getPaletteType();
      this.chunkSection = typeEnum.getConstructor().get();
      if (version <= 4) {
         ISectionPalette tempSection = typeEnum.getConstructor().get();
         boolean[] foundMigratable = new boolean[]{false};
         boolean[] needsPhysics = new boolean[]{false};
         int[] nextTempIndex = new int[]{-1};
         Int2ObjectOpenHashMap<String> types = new Int2ObjectOpenHashMap();
         Object2IntOpenHashMap<String> typesRev = new Object2IntOpenHashMap();
         typesRev.defaultReturnValue(Integer.MIN_VALUE);
         Function<String, String> finalBlockMigration = blockMigration;
         tempSection.deserialize(
            bytebuf -> {
               String keyx = ByteBufUtil.readUTF(bytebuf);
               if (finalBlockMigration != null) {
                  keyx = finalBlockMigration.apply(keyx);
               }

               int indexx = typesRev.getInt(keyx);
               if (indexx != Integer.MIN_VALUE) {
                  return indexx;
               } else {
                  boolean migratable = keyx.startsWith("Fluid_")
                     || keyx.contains("|Fluid=")
                     || keyx.contains("|Deco")
                     || keyx.contains("|Support")
                     || keyx.contains("|Filler")
                     || keyx.contains("|Yaw=")
                     || keyx.contains("|Pitch=")
                     || keyx.contains("|Roll=");
                  foundMigratable[0] |= migratable;
                  Object var10x;
                  if (migratable) {
                     var10x = nextTempIndex[0]--;
                  } else {
                     var10x = BlockType.getBlockIdOrUnknown(keyx, "Unknown BlockType %s", keyx);
                     needsPhysics[0] |= BlockType.getAssetMap().getAsset((int)var10x).hasSupport();
                  }

                  types.put((int)var10x, keyx);
                  typesRev.put(keyx, (int)var10x);
                  return (int)var10x;
               }
            },
            buf,
            version
         );
         if (needsPhysics[0]) {
            this.migratedBlockPhysics = new BlockPhysics();
         }

         if (foundMigratable[0]) {
            for (int index = 0; index < 32768; index++) {
               int id = tempSection.get(index);
               if (id >= 0) {
                  this.chunkSection.set(index, id);
               } else {
                  Rotation rotationYaw = Rotation.None;
                  Rotation rotationPitch = Rotation.None;
                  Rotation rotationRoll = Rotation.None;
                  String key = (String)types.get(id);
                  if (key.startsWith("Fluid_") || key.contains("|Fluid=")) {
                     if (this.migratedFluidSection == null) {
                        this.migratedFluidSection = new FluidSection();
                     }

                     Fluid.ConversionResult result = Fluid.convertBlockToFluid(key);
                     if (result == null) {
                        throw new RuntimeException("Invalid Fluid Key " + key);
                     }

                     if (result.blockTypeStr == null) {
                        this.migratedFluidSection.setFluid(index, result.fluidId, result.fluidLevel);
                        continue;
                     }

                     key = result.blockTypeStr;
                     this.migratedFluidSection.setFluid(index, result.fluidId, result.fluidLevel);
                  }

                  if (key.contains("|Deco")) {
                     if (this.migratedBlockPhysics == null) {
                        this.migratedBlockPhysics = new BlockPhysics();
                     }

                     this.migratedBlockPhysics.set(index, 15);
                  }

                  if (key.contains("|Support=")) {
                     if (this.migratedBlockPhysics == null) {
                        this.migratedBlockPhysics = new BlockPhysics();
                     }

                     int start = key.indexOf("|Support=") + "|Support=".length();
                     int end = key.indexOf(124, start);
                     if (end == -1) {
                        end = key.length();
                     }

                     this.migratedBlockPhysics.set(index, Integer.parseInt(key, start, end, 10));
                  }

                  if (key.contains("|Filler=")) {
                     int start = key.indexOf("|Filler=") + "|Filler=".length();
                     int firstComma = key.indexOf(44, start);
                     if (firstComma == -1) {
                        throw new IllegalArgumentException("Invalid filler metadata! Missing comma");
                     }

                     int secondComma = key.indexOf(44, firstComma + 1);
                     if (secondComma == -1) {
                        throw new IllegalArgumentException("Invalid filler metadata! Missing second comma");
                     }

                     int end = key.indexOf(124, start);
                     if (end == -1) {
                        end = key.length();
                     }

                     int fillerX = Integer.parseInt(key, start, firstComma, 10);
                     int fillerY = Integer.parseInt(key, firstComma + 1, secondComma, 10);
                     int fillerZ = Integer.parseInt(key, secondComma + 1, end, 10);
                     int filler = FillerBlockUtil.pack(fillerX, fillerY, fillerZ);
                     ISectionPalette.SetResult resultx = this.fillerSection.set(index, filler);
                     if (resultx == ISectionPalette.SetResult.REQUIRES_PROMOTE) {
                        this.fillerSection = this.fillerSection.promote();
                        this.fillerSection.set(index, filler);
                     }
                  }

                  if (key.contains("|Yaw=")) {
                     int startx = key.indexOf("|Yaw=") + "|Yaw=".length();
                     int endx = key.indexOf(124, startx);
                     if (endx == -1) {
                        endx = key.length();
                     }

                     rotationYaw = Rotation.ofDegrees(Integer.parseInt(key, startx, endx, 10));
                  }

                  if (key.contains("|Pitch=")) {
                     int startx = key.indexOf("|Pitch=") + "|Pitch=".length();
                     int endx = key.indexOf(124, startx);
                     if (endx == -1) {
                        endx = key.length();
                     }

                     rotationPitch = Rotation.ofDegrees(Integer.parseInt(key, startx, endx, 10));
                  }

                  if (key.contains("|Roll=")) {
                     int startx = key.indexOf("|Roll=") + "|Roll=".length();
                     int endx = key.indexOf(124, startx);
                     if (endx == -1) {
                        endx = key.length();
                     }

                     rotationRoll = Rotation.ofDegrees(Integer.parseInt(key, startx, endx, 10));
                  }

                  if (rotationYaw != Rotation.None || rotationPitch != Rotation.None || rotationRoll != Rotation.None) {
                     int rotation = RotationTuple.index(rotationYaw, rotationPitch, rotationRoll);
                     ISectionPalette.SetResult resultx = this.rotationSection.set(index, rotation);
                     if (resultx == ISectionPalette.SetResult.REQUIRES_PROMOTE) {
                        this.rotationSection = this.rotationSection.promote();
                        this.rotationSection.set(index, rotation);
                     }
                  }

                  int endOfName = key.indexOf(124);
                  if (endOfName != -1) {
                     key = key.substring(0, endOfName);
                  }

                  this.chunkSection.set(index, BlockType.getBlockIdOrUnknown(key, "Unknown BlockType: %s", key));
               }
            }

            if (this.chunkSection.shouldDemote()) {
               this.chunkSection.demote();
            }
         } else {
            this.chunkSection = tempSection;
         }
      } else if (blockMigration != null) {
         this.chunkSection.deserialize(bytebuf -> {
            String keyx = ByteBufUtil.readUTF(bytebuf);
            keyx = blockMigration.apply(keyx);
            return BlockType.getBlockIdOrUnknown(keyx, "Unknown BlockType %s", keyx);
         }, buf, version);
      } else {
         this.chunkSection.deserialize(keyDeserializer, buf, version);
      }

      if (paletteType != PaletteType.Empty) {
         this.tickingBlocksCount = buf.readUnsignedShort();
         int len = buf.readUnsignedShort();
         long[] tickingBlocksData = new long[len];

         for (int i = 0; i < tickingBlocksData.length; i++) {
            tickingBlocksData[i] = buf.readLong();
         }

         this.tickingBlocks = BitSet.valueOf(tickingBlocksData);
         this.tickingBlocksCount = this.tickingBlocks.cardinality();
      }

      if (version >= 4) {
         PaletteTypeEnum fillerTypeEnum = PaletteTypeEnum.get(buf.readByte());
         this.fillerSection = fillerTypeEnum.getConstructor().get();
         this.fillerSection.deserialize(ByteBuf::readUnsignedShort, buf, version);
      }

      if (version >= 5) {
         PaletteTypeEnum rotationTypeEnum = PaletteTypeEnum.get(buf.readByte());
         this.rotationSection = rotationTypeEnum.getConstructor().get();
         this.rotationSection.deserialize(ByteBuf::readUnsignedByte, buf, version);
      }

      this.localLight = ChunkLightData.deserialize(buf, version);
      this.globalLight = ChunkLightData.deserialize(buf, version);
      this.localChangeCounter = buf.readShort();
      this.globalChangeCounter = buf.readShort();
   }

   public void deserialize(@Nonnull byte[] bytes, @Nonnull ExtraInfo extraInfo) {
      ByteBuf buf = Unpooled.wrappedBuffer(bytes);
      this.deserialize(BlockType.KEY_DESERIALIZER, buf, extraInfo.getVersion());
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

   @Nonnull
   public CompletableFuture<CachedPacket<SetChunk>> getCachedChunkPacket(int x, int y, int z) {
      SoftReference<CompletableFuture<CachedPacket<SetChunk>>> ref = this.cachedChunkPacket;
      CompletableFuture<CachedPacket<SetChunk>> future = ref != null ? ref.get() : null;
      if (future != null) {
         return future;
      } else {
         future = CompletableFuture.supplyAsync(() -> {
            byte[] localLightArr = null;
            byte[] globalLightArr = null;
            byte[] data = null;
            if (BlockChunk.SEND_LOCAL_LIGHTING_DATA && this.hasLocalLight()) {
               ChunkLightData localLight = this.getLocalLight();
               ByteBuf buffer = Unpooled.buffer();
               localLight.serializeForPacket(buffer);
               if (this.getLocalChangeCounter() == localLight.getChangeId()) {
                  localLightArr = ByteBufUtil.getBytesRelease(buffer);
               }
            }

            if (BlockChunk.SEND_GLOBAL_LIGHTING_DATA && this.hasGlobalLight()) {
               ByteBuf buffer = Unpooled.buffer();
               ChunkLightData globalLight = this.getGlobalLight();
               globalLight.serializeForPacket(buffer);
               if (this.getGlobalChangeCounter() == globalLight.getChangeId()) {
                  globalLightArr = ByteBufUtil.getBytesRelease(buffer);
               }
            }

            if (!this.isSolidAir()) {
               ByteBuf buf = Unpooled.buffer(65536);
               this.serializeForPacket(buf);
               data = ByteBufUtil.getBytesRelease(buf);
            }

            SetChunk setChunk = new SetChunk(x, y, z, localLightArr, globalLightArr, data);
            return CachedPacket.cache(setChunk);
         });
         this.cachedChunkPacket = new SoftReference<>(future);
         return future;
      }
   }

   public int get(int x, int y, int z) {
      return this.get(ChunkUtil.indexBlock(x, y, z));
   }

   public boolean set(int x, int y, int z, int blockId, int rotation, int filler) {
      return this.set(ChunkUtil.indexBlock(x, y, z), blockId, rotation, filler);
   }

   public boolean setTicking(int x, int y, int z, boolean ticking) {
      return this.setTicking(ChunkUtil.indexBlock(x, y, z), ticking);
   }

   public boolean isTicking(int x, int y, int z) {
      return this.isTicking(ChunkUtil.indexBlock(x, y, z));
   }

   private record TickRequest(int index, @Nonnull Instant requestedGameTime) {
   }
}
