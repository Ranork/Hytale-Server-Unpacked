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
import com.hypixel.hytale.function.consumer.BiIntConsumer;
import com.hypixel.hytale.function.predicate.ObjectPositionBlockFunction;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.CachedPacket;
import com.hypixel.hytale.protocol.packets.world.PaletteType;
import com.hypixel.hytale.protocol.packets.world.SetChunk;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickStrategy;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockMigration;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.modules.LegacyModule;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.palette.AbstractSectionPalette;
import com.hypixel.hytale.server.core.universe.world.chunk.section.palette.EmptySectionPalette;
import com.hypixel.hytale.server.core.universe.world.chunk.section.palette.PaletteTypeEnum;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.util.io.MemorySegmentUtil;
import it.unimi.dsi.fastutil.ints.Int2ShortMap;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
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
   private AbstractSectionPalette chunkSection;
   private AbstractSectionPalette fillerSection;
   private AbstractSectionPalette rotationSection;
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
   private static final AbstractSectionPalette.KeyMemorySerializer FILLER_SERIALIZER = new AbstractSectionPalette.KeyMemorySerializer() {
      @Override
      public int serialize(MemorySegment memorySegment, int offset, int index) {
         memorySegment.set(MemorySegmentUtil.SHORT_BE, (long)offset, (short)index);
         return 2;
      }

      @Override
      public int keySize(int index) {
         return 2;
      }
   };
   private static final AbstractSectionPalette.KeyMemorySerializer ROTATION_SERIALIZER = new AbstractSectionPalette.KeyMemorySerializer() {
      @Override
      public int serialize(MemorySegment memorySegment, int offset, int index) {
         memorySegment.set(ValueLayout.JAVA_BYTE, (long)offset, (byte)index);
         return 1;
      }

      @Override
      public int keySize(int index) {
         return 1;
      }
   };
   private static final AbstractSectionPalette.KeyMemoryDeserializer FILLER_DESERIALIZER = new AbstractSectionPalette.KeyMemoryDeserializer() {
      @Override
      public int deserialize(MemorySegment mem, int offset) {
         return Short.toUnsignedInt(mem.get(MemorySegmentUtil.SHORT_BE, (long)offset));
      }

      @Override
      public int keySize(MemorySegment mem, int offset) {
         return 2;
      }
   };
   private static final AbstractSectionPalette.KeyMemoryDeserializer ROTATION_DESERIALIZER = new AbstractSectionPalette.KeyMemoryDeserializer() {
      @Override
      public int deserialize(MemorySegment mem, int offset) {
         return Byte.toUnsignedInt(mem.get(ValueLayout.JAVA_BYTE, (long)offset));
      }

      @Override
      public int keySize(MemorySegment mem, int offset) {
         return 1;
      }
   };
   private static final Comparator<BlockSection.TickRequest> TICK_REQUEST_COMPARATOR = Comparator.comparing(t -> t.requestedGameTime);

   public static ComponentType<ChunkStore, BlockSection> getComponentType() {
      return LegacyModule.get().getBlockSectionComponentType();
   }

   public BlockSection() {
      this(EmptySectionPalette.INSTANCE, EmptySectionPalette.INSTANCE, EmptySectionPalette.INSTANCE);
   }

   public BlockSection(AbstractSectionPalette chunkSection, AbstractSectionPalette fillerSection, AbstractSectionPalette rotationSection) {
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

   public AbstractSectionPalette getChunkSection() {
      return this.chunkSection;
   }

   public void setChunkSection(AbstractSectionPalette chunkSection) {
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
            AbstractSectionPalette.SetResult result = this.chunkSection.set(blockIdx, blockId);
            if (result == AbstractSectionPalette.SetResult.REQUIRES_PROMOTE) {
               this.chunkSection = this.chunkSection.promote();
               AbstractSectionPalette.SetResult repeatResult = this.chunkSection.set(blockIdx, blockId);
               if (repeatResult != AbstractSectionPalette.SetResult.ADDED_OR_REMOVED) {
                  throw new IllegalStateException("Promoted chunk section failed to correctly add the new block!");
               }
            } else {
               if (result == AbstractSectionPalette.SetResult.ADDED_OR_REMOVED) {
                  this.maximumHitboxExtent = -1.0;
               }

               if (this.chunkSection.shouldDemote()) {
                  this.chunkSection = this.chunkSection.demote();
               }
            }

            changed = result != AbstractSectionPalette.SetResult.UNCHANGED;
            result = this.fillerSection.set(blockIdx, filler);
            if (result == AbstractSectionPalette.SetResult.REQUIRES_PROMOTE) {
               this.fillerSection = this.fillerSection.promote();
               AbstractSectionPalette.SetResult repeatResult = this.fillerSection.set(blockIdx, filler);
               if (repeatResult != AbstractSectionPalette.SetResult.ADDED_OR_REMOVED) {
                  throw new IllegalStateException("Promoted chunk section failed to correctly add the new block!");
               }
            } else if (this.fillerSection.shouldDemote()) {
               this.fillerSection = this.fillerSection.demote();
            }

            changed |= result != AbstractSectionPalette.SetResult.UNCHANGED;
            result = this.rotationSection.set(blockIdx, rotation);
            if (result == AbstractSectionPalette.SetResult.REQUIRES_PROMOTE) {
               this.rotationSection = this.rotationSection.promote();
               AbstractSectionPalette.SetResult repeatResult = this.rotationSection.set(blockIdx, rotation);
               if (repeatResult != AbstractSectionPalette.SetResult.ADDED_OR_REMOVED) {
                  throw new IllegalStateException("Promoted chunk section failed to correctly add the new block!");
               }
            } else if (this.rotationSection.shouldDemote()) {
               this.rotationSection = this.rotationSection.demote();
            }

            changed |= result != AbstractSectionPalette.SetResult.UNCHANGED;
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

   @Deprecated(since = "2026-02-26", forRemoval = true)
   public void find(IntList ids, IntSet ignoredInternalIdHolder, IntConsumer indexConsumer) {
      this.find(ids, indexConsumer);
   }

   public void find(IntList ids, IntConsumer indexConsumer) {
      long lock = this.chunkSectionLock.readLock();

      try {
         this.chunkSection.find(ids, indexConsumer);
      } finally {
         this.chunkSectionLock.unlockRead(lock);
      }
   }

   public void find(IntList ids, BiIntConsumer indexBlockConsumer) {
      long lock = this.chunkSectionLock.readLock();

      try {
         this.chunkSection.find(ids, indexBlockConsumer);
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

   public int setTicking(@Nonnull IntList indices, boolean ticking) {
      long writeStamp = this.chunkSectionLock.writeLock();

      int var11;
      try {
         int count = 0;

         for (int i = 0; i < indices.size(); i++) {
            int blockIdx = indices.getInt(i);
            if (this.tickingBlocks.get(blockIdx) != ticking) {
               if (ticking) {
                  this.tickingBlocksCount++;
               } else {
                  this.tickingBlocksCount--;
               }

               this.tickingBlocks.set(blockIdx, ticking);
               count++;
            }
         }

         var11 = count;
      } finally {
         this.chunkSectionLock.unlockWrite(writeStamp);
      }

      return var11;
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
                  case SLEEP:
                  case IGNORED:
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

   public byte[] serializeForPacket() {
      long lock = this.chunkSectionLock.readLock();

      byte[] var12;
      try {
         byte[] result = new byte[1
            + this.chunkSection.serializedPacketByteSize()
            + 1
            + this.fillerSection.serializedPacketByteSize()
            + 1
            + this.rotationSection.serializedPacketByteSize()];
         MemorySegment data = MemorySegment.ofArray(result);
         PaletteType paletteType = this.chunkSection.getPaletteType();
         byte paletteTypeId = (byte)paletteType.ordinal();
         data.set(ValueLayout.JAVA_BYTE, 0L, paletteTypeId);
         int offset = 1 + this.chunkSection.serializeForPacket(data, 1);
         PaletteType fillerType = this.fillerSection.getPaletteType();
         byte fillerTypeId = (byte)fillerType.ordinal();
         data.set(ValueLayout.JAVA_BYTE, (long)offset, fillerTypeId);
         offset += 1 + this.fillerSection.serializeForPacket(data, offset + 1);
         PaletteType rotationType = this.rotationSection.getPaletteType();
         byte rotationTypeId = (byte)rotationType.ordinal();
         data.set(ValueLayout.JAVA_BYTE, (long)offset, rotationTypeId);
         this.rotationSection.serializeForPacket(data, offset + 1);
         var12 = result;
      } finally {
         this.chunkSectionLock.unlockRead(lock);
      }

      return var12;
   }

   public byte[] serialize(ExtraInfo extraInfo) {
      long lock = this.chunkSectionLock.readLock();

      byte[] var10;
      try {
         PaletteType paletteType = this.chunkSection.getPaletteType();
         BitSet combinedTickingBlock;
         long[] tickingData;
         if (paletteType != PaletteType.Empty) {
            combinedTickingBlock = (BitSet)this.tickingBlocks.clone();
            combinedTickingBlock.or(this.tickingWaitAdjacentBlocks);
            tickingData = combinedTickingBlock.toLongArray();
         } else {
            combinedTickingBlock = null;
            tickingData = null;
         }

         byte[] result = new byte[5
            + this.chunkSection.serializedByteSize(BlockType.KEY_MEMORY_SERIALIZER)
            + (paletteType != PaletteType.Empty ? 4 + tickingData.length * 8 : 0)
            + 1
            + this.fillerSection.serializedByteSize(FILLER_SERIALIZER)
            + 1
            + this.rotationSection.serializedByteSize(ROTATION_SERIALIZER)
            + this.localLight.serializedByteSize()
            + this.globalLight.serializedByteSize()
            + 2
            + 2];
         MemorySegment data = MemorySegment.ofArray(result);
         data.set(MemorySegmentUtil.INT_BE, 0L, BlockMigration.getAssetMap().getAssetCount());
         data.set(ValueLayout.JAVA_BYTE, 4L, (byte)paletteType.ordinal());
         int offset = 5 + this.chunkSection.serialize(BlockType.KEY_MEMORY_SERIALIZER, data, 5);
         if (paletteType != PaletteType.Empty) {
            data.set(MemorySegmentUtil.SHORT_BE, (long)offset, (short)combinedTickingBlock.cardinality());
            data.set(MemorySegmentUtil.SHORT_BE, (long)(offset + 2), (short)tickingData.length);
            MemorySegment.copy(tickingData, 0, data, MemorySegmentUtil.LONG_BE, offset + 4, tickingData.length);
            offset += 4 + tickingData.length * 8;
         }

         data.set(ValueLayout.JAVA_BYTE, (long)offset, (byte)this.fillerSection.getPaletteType().ordinal());
         offset += 1 + this.fillerSection.serialize(FILLER_SERIALIZER, data, offset + 1);
         data.set(ValueLayout.JAVA_BYTE, (long)offset, (byte)this.rotationSection.getPaletteType().ordinal());
         offset += 1 + this.rotationSection.serialize(ROTATION_SERIALIZER, data, offset + 1);
         offset += this.localLight.serialize(data, offset);
         offset += this.globalLight.serialize(data, offset);
         data.set(MemorySegmentUtil.SHORT_BE, (long)offset, this.localChangeCounter);
         data.set(MemorySegmentUtil.SHORT_BE, (long)(offset + 2), this.globalChangeCounter);
         var10 = result;
      } finally {
         this.chunkSectionLock.unlockRead(lock);
      }

      return var10;
   }

   public void deserialize(@Nonnull byte[] bytes, @Nonnull ExtraInfo extraInfo) {
      MemorySegment data = MemorySegment.ofArray(bytes);
      int version = extraInfo.getVersion();
      if (version < 6) {
         throw new IllegalArgumentException("Version not supported: " + version);
      } else {
         int blockMigrationVersion = data.get(MemorySegmentUtil.INT_BE, 0L);
         final Function<String, String> blockMigration = null;
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

         PaletteTypeEnum typeEnum = PaletteTypeEnum.get(data.get(ValueLayout.JAVA_BYTE, 4L));
         PaletteType paletteType = typeEnum.getPaletteType();
         this.chunkSection = typeEnum.getConstructor().get();
         int offset = 5;
         if (blockMigration != null) {
            offset += this.chunkSection.deserialize(new AbstractSectionPalette.KeyMemoryDeserializer() {
               {
                  Objects.requireNonNull(BlockSection.this);
               }

               @Override
               public int deserialize(MemorySegment mem, int offsetx) {
                  String key = blockMigration.apply(MemorySegmentUtil.readUTF(mem, offsetx));
                  return BlockType.getBlockIdOrUnknown(key, "Unknown BlockType %s", key);
               }

               @Override
               public int keySize(MemorySegment mem, int offsetx) {
                  return MemorySegmentUtil.utf8Size(mem, offsetx);
               }
            }, data, offset);
         } else {
            offset += this.chunkSection.deserialize(BlockType.KEY_MEMORY_DESERIALIZER, data, offset);
         }

         if (paletteType != PaletteType.Empty) {
            this.tickingBlocksCount = Short.toUnsignedInt(data.get(MemorySegmentUtil.SHORT_BE, (long)offset));
            int len = Short.toUnsignedInt(data.get(MemorySegmentUtil.SHORT_BE, (long)(offset + 2)));
            offset += 4;
            long[] tickingBlocksData = data.asSlice(offset, len * 8).toArray(MemorySegmentUtil.LONG_BE);
            offset += len * 8;
            this.tickingBlocks = BitSet.valueOf(tickingBlocksData);
            this.tickingBlocksCount = this.tickingBlocks.cardinality();
         }

         PaletteTypeEnum fillerTypeEnum = PaletteTypeEnum.get(data.get(ValueLayout.JAVA_BYTE, (long)offset));
         this.fillerSection = fillerTypeEnum.getConstructor().get();
         offset += 1 + this.fillerSection.deserialize(FILLER_DESERIALIZER, data, offset + 1);
         PaletteTypeEnum rotationTypeEnum = PaletteTypeEnum.get(data.get(ValueLayout.JAVA_BYTE, (long)offset));
         this.rotationSection = rotationTypeEnum.getConstructor().get();
         offset += 1 + this.rotationSection.deserialize(ROTATION_DESERIALIZER, data, offset + 1);
         this.localLight = ChunkLightData.deserialize(data, offset);
         offset += this.localLight.serializedByteSize();
         this.globalLight = ChunkLightData.deserialize(data, offset);
         offset += this.globalLight.serializedByteSize();
         this.localChangeCounter = data.get(MemorySegmentUtil.SHORT_BE, (long)offset);
         this.globalChangeCounter = data.get(MemorySegmentUtil.SHORT_BE, (long)(offset + 2));
      }
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
               localLightArr = new byte[localLight.serializedForPacketByteSize()];
               MemorySegment mem = MemorySegment.ofArray(localLightArr);
               localLight.serializeForPacket(mem, 0);
               if (this.getLocalChangeCounter() != localLight.getChangeId()) {
                  localLightArr = null;
               }
            }

            if (BlockChunk.SEND_GLOBAL_LIGHTING_DATA && this.hasGlobalLight()) {
               ChunkLightData globalLight = this.getGlobalLight();
               globalLightArr = new byte[globalLight.serializedForPacketByteSize()];
               MemorySegment mem = MemorySegment.ofArray(globalLightArr);
               globalLight.serializeForPacket(mem, 0);
               if (this.getGlobalChangeCounter() != globalLight.getChangeId()) {
                  globalLightArr = null;
               }
            }

            if (!this.isSolidAir()) {
               data = this.serializeForPacket();
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
