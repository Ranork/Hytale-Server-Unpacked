package com.hypixel.hytale.server.core.prefab.selection.standard;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemType;
import com.hypixel.hytale.component.ComponentRegistry.Data;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.block.BlockUtil;
import com.hypixel.hytale.math.matrix.Matrix4d;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.metrics.MetricProvider;
import com.hypixel.hytale.metrics.MetricResults;
import com.hypixel.hytale.metrics.MetricsRegistry;
import com.hypixel.hytale.protocol.Opacity;
import com.hypixel.hytale.protocol.packets.interface_.BlockChange;
import com.hypixel.hytale.protocol.packets.interface_.EditorBlocksChange;
import com.hypixel.hytale.protocol.packets.interface_.EditorSelection;
import com.hypixel.hytale.protocol.packets.interface_.FluidChange;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.StateData;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.VariantRotation;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.asset.type.fluid.FluidTicker;
import com.hypixel.hytale.server.core.blocktype.component.BlockPhysics;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.BlockModule.MigrationSystem;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.FromPrefab;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.prefab.event.PrefabPlaceEntityEvent;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockMask;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.1;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockRotationUtil;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.IOException;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;

public class BlockSelection implements NetworkSerializable<EditorBlocksChange>, MetricProvider {
   public static final Consumer<Ref<EntityStore>> DEFAULT_ENTITY_CONSUMER = (ref) -> {
   };
   public static final MetricsRegistry<BlockSelection> METRICS_REGISTRY;
   private static final HytaleLogger LOGGER;
   private int x;
   private int y;
   private int z;
   private int anchorX;
   private int anchorY;
   private int anchorZ;
   private int prefabId = -1;
   @Nonnull
   private Vector3i min;
   @Nonnull
   private Vector3i max;
   @Nonnull
   private final Long2ObjectMap<com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.BlockHolder> blocks;
   @Nonnull
   private final Long2ObjectMap<com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.FluidHolder> fluids;
   @Nonnull
   private final List<Holder<EntityStore>> entities;
   private final ReentrantReadWriteLock blocksLock;
   private final ReentrantReadWriteLock entitiesLock;

   public BlockSelection() {
      this.min = Vector3i.ZERO;
      this.max = Vector3i.ZERO;
      this.blocksLock = new ReentrantReadWriteLock();
      this.entitiesLock = new ReentrantReadWriteLock();
      this.blocks = new Long2ObjectOpenHashMap();
      this.fluids = new Long2ObjectOpenHashMap();
      this.entities = new ObjectArrayList();
   }

   public BlockSelection(int initialBlockCapacity, int initialEntityCapacity) {
      this.min = Vector3i.ZERO;
      this.max = Vector3i.ZERO;
      this.blocksLock = new ReentrantReadWriteLock();
      this.entitiesLock = new ReentrantReadWriteLock();
      this.blocks = new Long2ObjectOpenHashMap(initialBlockCapacity);
      this.fluids = new Long2ObjectOpenHashMap(initialBlockCapacity);
      this.entities = new ObjectArrayList(initialEntityCapacity);
   }

   public BlockSelection(@Nonnull BlockSelection other) {
      this.min = Vector3i.ZERO;
      this.max = Vector3i.ZERO;
      this.blocksLock = new ReentrantReadWriteLock();
      this.entitiesLock = new ReentrantReadWriteLock();
      if (other == this) {
         throw new IllegalArgumentException("Cannot duplicate a BlockSelection with this method! Use clone()!");
      } else {
         this.blocks = new Long2ObjectOpenHashMap(other.getBlockCount());
         this.fluids = new Long2ObjectOpenHashMap(other.getFluidCount());
         this.entities = new ObjectArrayList(other.getEntityCount());
         this.copyPropertiesFrom(other);
         this.add(other);
      }
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public int getZ() {
      return this.z;
   }

   public int getAnchorX() {
      return this.anchorX;
   }

   public int getAnchorY() {
      return this.anchorY;
   }

   public int getAnchorZ() {
      return this.anchorZ;
   }

   @Nonnull
   public Vector3i getSelectionMin() {
      return this.min.clone();
   }

   @Nonnull
   public Vector3i getSelectionMax() {
      return this.max.clone();
   }

   public boolean hasSelectionBounds() {
      return !this.min.equals(Vector3i.ZERO) || !this.max.equals(Vector3i.ZERO);
   }

   public int getBlockCount() {
      this.blocksLock.readLock().lock();

      int var1;
      try {
         var1 = this.blocks.size();
      } finally {
         this.blocksLock.readLock().unlock();
      }

      return var1;
   }

   public int getFluidCount() {
      this.blocksLock.readLock().lock();

      int var1;
      try {
         var1 = this.fluids.size();
      } finally {
         this.blocksLock.readLock().unlock();
      }

      return var1;
   }

   public int getSelectionVolume() {
      int xLength = this.max.x - this.min.x;
      int yLength = this.max.y - this.min.y;
      int zLength = this.max.z - this.min.z;
      return xLength * yLength & zLength;
   }

   public int getEntityCount() {
      this.entitiesLock.readLock().lock();

      int var1;
      try {
         var1 = this.entities.size();
      } finally {
         this.entitiesLock.readLock().unlock();
      }

      return var1;
   }

   public void setPosition(int x, int y, int z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public void setAnchorAtWorldPos(int anchorX, int anchorY, int anchorZ) {
      this.setAnchor(anchorX - this.x, anchorY - this.y, anchorZ - this.z);
   }

   public void setAnchor(int anchorX, int anchorY, int anchorZ) {
      this.anchorX = anchorX;
      this.anchorY = anchorY;
      this.anchorZ = anchorZ;
   }

   public void setSelectionArea(@Nonnull Vector3i min, @Nonnull Vector3i max) {
      this.min = Vector3i.min(min, max);
      this.max = Vector3i.max(min, max);
   }

   public void setPrefabId(int id) {
      this.prefabId = id;
   }

   public void copyPropertiesFrom(@Nonnull BlockSelection other) {
      this.x = other.x;
      this.y = other.y;
      this.z = other.z;
      this.anchorX = other.anchorX;
      this.anchorY = other.anchorY;
      this.anchorZ = other.anchorZ;
      this.min = other.min.clone();
      this.max = other.max.clone();
   }

   public boolean canPlace(@Nonnull World world, @Nonnull Vector3i position, @Nullable IntList mask) {
      return this.compare((x1, y1, z1, block) -> {
         int blockX = x1 + position.getX() - this.anchorX;
         int blockY = y1 + position.getY() - this.anchorY;
         int blockZ = z1 + position.getZ() - this.anchorZ;
         int blockId = world.getBlock(blockX, blockY, blockZ);
         return blockId == 0 || mask == null || mask.contains(blockId);
      });
   }

   public boolean matches(@Nonnull World world, @Nonnull Vector3i position) {
      return this.compare((x1, y1, z1, block) -> {
         int blockX = x1 + position.getX() - this.anchorX;
         int blockY = y1 + position.getY() - this.anchorY;
         int blockZ = z1 + position.getZ() - this.anchorZ;
         int blockId = world.getBlock(blockX, blockY, blockZ);
         return block.blockId == blockId;
      });
   }

   public boolean compare(@Nonnull com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.BlockComparingIterator iterator) {
      ObjectIterator var2 = this.blocks.long2ObjectEntrySet().iterator();

      com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.BlockHolder value;
      int x1;
      int y1;
      int z1;
      do {
         if (!var2.hasNext()) {
            return true;
         }

         Entry<com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.BlockHolder> entry = (Entry)var2.next();
         long packed = entry.getLongKey();
         value = (com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.BlockHolder)entry.getValue();
         x1 = BlockUtil.unpackX(packed);
         y1 = BlockUtil.unpackY(packed);
         z1 = BlockUtil.unpackZ(packed);
      } while(iterator.test(x1, y1, z1, value));

      return false;
   }

   public boolean hasBlockAtWorldPos(int x, int y, int z) {
      return this.hasBlockAtLocalPos(x - this.x, y - this.y, z - this.z);
   }

   public boolean hasBlockAtLocalPos(int x, int y, int z) {
      this.blocksLock.readLock().lock();

      boolean var4;
      try {
         var4 = this.blocks.containsKey(BlockUtil.pack(x, y, z));
      } finally {
         this.blocksLock.readLock().unlock();
      }

      return var4;
   }

   public int getBlockAtWorldPos(int x, int y, int z) {
      return this.getBlockAtLocalPos(x - this.x, y - this.y, z - this.z);
   }

   private int getBlockAtLocalPos(int x, int y, int z) {
      this.blocksLock.readLock().lock();

      int var5;
      try {
         com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.BlockHolder blockHolder = (com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.BlockHolder)this.blocks.get(BlockUtil.pack(x, y, z));
         if (blockHolder == null) {
            var5 = Integer.MIN_VALUE;
            return var5;
         }

         var5 = blockHolder.blockId();
      } finally {
         this.blocksLock.readLock().unlock();
      }

      return var5;
   }

   public com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.BlockHolder getBlockHolderAtWorldPos(int x, int y, int z) {
      return this.getBlockHolderAtLocalPos(x - this.x, y - this.y, z - this.z);
   }

   private com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.BlockHolder getBlockHolderAtLocalPos(int x, int y, int z) {
      this.blocksLock.readLock().lock();

      com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.BlockHolder var4;
      try {
         var4 = (com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.BlockHolder)this.blocks.get(BlockUtil.pack(x, y, z));
      } finally {
         this.blocksLock.readLock().unlock();
      }

      return var4;
   }

   public int getFluidAtWorldPos(int x, int y, int z) {
      return this.getFluidAtLocalPos(x - this.x, y - this.y, z - this.z);
   }

   private int getFluidAtLocalPos(int x, int y, int z) {
      this.blocksLock.readLock().lock();

      int var5;
      try {
         com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.FluidHolder fluidStore = (com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.FluidHolder)this.fluids.get(BlockUtil.pack(x, y, z));
         if (fluidStore != null) {
            var5 = fluidStore.fluidId();
            return var5;
         }

         var5 = Integer.MIN_VALUE;
      } finally {
         this.blocksLock.readLock().unlock();
      }

      return var5;
   }

   public byte getFluidLevelAtWorldPos(int x, int y, int z) {
      return this.getFluidLevelAtLocalPos(x - this.x, y - this.y, z - this.z);
   }

   private byte getFluidLevelAtLocalPos(int x, int y, int z) {
      this.blocksLock.readLock().lock();

      byte var5;
      try {
         com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.FluidHolder fluidStore = (com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.FluidHolder)this.fluids.get(BlockUtil.pack(x, y, z));
         if (fluidStore != null) {
            byte var9 = fluidStore.fluidLevel();
            return var9;
         }

         var5 = 0;
      } finally {
         this.blocksLock.readLock().unlock();
      }

      return var5;
   }

   public int getSupportValueAtWorldPos(int x, int y, int z) {
      return this.getSupportValueAtLocalPos(x - this.x, y - this.y, z - this.z);
   }

   private int getSupportValueAtLocalPos(int x, int y, int z) {
      this.blocksLock.readLock().lock();

      int var5;
      try {
         com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.BlockHolder blockHolder = (com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.BlockHolder)this.blocks.get(BlockUtil.pack(x, y, z));
         if (blockHolder == null) {
            byte var9 = 0;
            return var9;
         }

         var5 = blockHolder.supportValue();
      } finally {
         this.blocksLock.readLock().unlock();
      }

      return var5;
   }

   @Nullable
   public Holder<ChunkStore> getStateAtWorldPos(int x, int y, int z) {
      return this.getStateAtLocalPos(x - this.x, y - this.y, z - this.z);
   }

   @Nullable
   private Holder<ChunkStore> getStateAtLocalPos(int x, int y, int z) {
      this.blocksLock.readLock().lock();

      Holder var6;
      try {
         com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.BlockHolder blockHolder = (com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.BlockHolder)this.blocks.get(BlockUtil.pack(x, y, z));
         Holder holder;
         if (blockHolder == null) {
            holder = null;
            return holder;
         }

         holder = blockHolder.holder();
         var6 = holder != null ? holder.clone() : null;
      } finally {
         this.blocksLock.readLock().unlock();
      }

      return var6;
   }

   public void forEachBlock(@Nonnull com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.BlockIterator iterator) {
      this.blocksLock.readLock().lock();

      try {
         Long2ObjectMaps.fastForEach(this.blocks, (e) -> {
            long packed = e.getLongKey();
            com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.BlockHolder block = (com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.BlockHolder)e.getValue();
            int x1 = BlockUtil.unpackX(packed);
            int y1 = BlockUtil.unpackY(packed);
            int z1 = BlockUtil.unpackZ(packed);
            iterator.accept(x1, y1, z1, block);
         });
      } finally {
         this.blocksLock.readLock().unlock();
      }

   }

   public void forEachFluid(@Nonnull com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.FluidIterator iterator) {
      this.blocksLock.readLock().lock();

      try {
         Long2ObjectMaps.fastForEach(this.fluids, (e) -> {
            long packed = e.getLongKey();
            com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.FluidHolder block = (com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.FluidHolder)e.getValue();
            int x1 = BlockUtil.unpackX(packed);
            int y1 = BlockUtil.unpackY(packed);
            int z1 = BlockUtil.unpackZ(packed);
            iterator.accept(x1, y1, z1, block.fluidId(), block.fluidLevel());
         });
      } finally {
         this.blocksLock.readLock().unlock();
      }

   }

   public void forEachEntity(Consumer<Holder<EntityStore>> consumer) {
      this.entitiesLock.readLock().lock();

      try {
         this.entities.forEach(consumer);
      } finally {
         this.entitiesLock.readLock().unlock();
      }

   }

   public void copyFromAtWorld(int x, int y, int z, @Nonnull WorldChunk other, @Nullable BlockPhysics blockPhysics) {
      this.addBlockAtWorldPos(x, y, z, other.getBlock(x, y, z), other.getRotationIndex(x, y, z), other.getFiller(x, y, z), blockPhysics != null ? blockPhysics.get(x, y, z) : 0, other.getBlockComponentHolder(x, y, z));
      this.addFluidAtWorldPos(x, y, z, other.getFluidId(x, y, z), other.getFluidLevel(x, y, z));
   }

   public void addEmptyAtWorldPos(int x, int y, int z) {
      this.addBlockAtWorldPos(x, y, z, 0, 0, 0, 0);
      this.addFluidAtWorldPos(x, y, z, 0, (byte)0);
   }

   public void addBlockAtWorldPos(int x, int y, int z, int block, int rotation, int filler, int supportValue) {
      this.addBlockAtWorldPos(x, y, z, block, rotation, filler, supportValue, (Holder)null);
   }

   public void addBlockAtWorldPos(int x, int y, int z, int block, int rotation, int filler, int supportValue, Holder<ChunkStore> state) {
      this.addBlockAtLocalPos(x - this.x, y - this.y, z - this.z, block, rotation, filler, supportValue, state);
   }

   public void addBlockAtLocalPos(int x, int y, int z, int block, int rotation, int filler, int supportValue) {
      this.addBlockAtLocalPos(x, y, z, block, rotation, filler, supportValue, (Holder)null);
   }

   public void addBlockAtLocalPos(int x, int y, int z, int block, int rotation, int filler, int supportValue, Holder<ChunkStore> state) {
      this.blocksLock.writeLock().lock();

      try {
         this.addBlock0(x, y, z, block, rotation, filler, supportValue, state);
      } finally {
         this.blocksLock.writeLock().unlock();
      }

   }

   private void addBlock0(int x, int y, int z, int block, int rotation, int filler, int supportValue, Holder<ChunkStore> state) {
      this.blocks.put(BlockUtil.pack(x, y, z), new com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.BlockHolder(block, rotation, filler, supportValue, state));
   }

   private void addBlock0(int x, int y, int z, @Nonnull com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.BlockHolder block) {
      this.blocks.put(BlockUtil.pack(x, y, z), block.cloneBlockHolder());
   }

   public void addFluidAtWorldPos(int x, int y, int z, int fluidId, byte fluidLevel) {
      this.addFluidAtLocalPos(x - this.x, y - this.y, z - this.z, fluidId, fluidLevel);
   }

   public void addFluidAtLocalPos(int x, int y, int z, int fluidId, byte fluidLevel) {
      this.blocksLock.writeLock().lock();

      try {
         this.addFluid0(x, y, z, fluidId, fluidLevel);
      } finally {
         this.blocksLock.writeLock().unlock();
      }

   }

   private void addFluid0(int x, int y, int z, int fluidId, byte fluidLevel) {
      this.fluids.put(BlockUtil.pack(x, y, z), new com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.FluidHolder(fluidId, fluidLevel));
   }

   private void addEntity0(Holder<EntityStore> holder) {
      this.entities.add(holder);
   }

   public void reserializeBlockStates(ChunkStore store, boolean destructive) {
      this.blocksLock.writeLock().lock();

      try {
         this.blocks.replaceAll((k, b) -> {
            Holder<ChunkStore> holder = b.holder();
            if (holder == null && b.filler == 0) {
               BlockType blockType = (BlockType)BlockType.getAssetMap().getAsset(b.blockId);
               if (blockType == null) {
                  return b;
               }

               if (blockType.getBlockEntity() != null) {
                  holder = blockType.getBlockEntity().clone();
               }

               StateData state = blockType.getState();
               if (state != null && state.getId() != null) {
                  Vector3i position = new Vector3i(BlockUtil.unpackX(k), BlockUtil.unpackY(k), BlockUtil.unpackZ(k));
                  Codec<? extends BlockState> codec = BlockState.CODEC.getCodecFor(state.getId());
                  if (codec == null) {
                     return b;
                  }

                  BlockState blockState = (BlockState)codec.decode(new BsonDocument());
                  if (blockState == null) {
                     return b;
                  }

                  blockState.setPosition((WorldChunk)null, position);
                  holder = blockState.toHolder();
               }
            }

            if (holder != null && b.filler != 0) {
               return new com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.BlockHolder(b.blockId(), b.rotation(), b.filler(), b.supportValue(), (Holder)null);
            } else if (holder == null) {
               return b;
            } else {
               try {
                  ComponentRegistry<ChunkStore> registry = ChunkStore.REGISTRY;
                  Data<ChunkStore> data = registry.getData();
                  SystemType<ChunkStore, MigrationSystem> systemType = BlockModule.get().getMigrationSystemType();
                  BitSet systemIndexes = data.getSystemIndexesForType(systemType);
                  int systemIndex = -1;

                  MigrationSystem system;
                  while((systemIndex = systemIndexes.nextSetBit(systemIndex + 1)) >= 0) {
                     system = (MigrationSystem)data.getSystem(systemIndex, systemType);
                     if (system.test(registry, holder.getArchetype())) {
                        system.onEntityAdd(holder, AddReason.LOAD, store.getStore());
                     }
                  }

                  systemIndex = -1;

                  while((systemIndex = systemIndexes.nextSetBit(systemIndex + 1)) >= 0) {
                     system = (MigrationSystem)data.getSystem(systemIndex, systemType);
                     if (system.test(registry, holder.getArchetype())) {
                        system.onEntityRemoved(holder, RemoveReason.UNLOAD, store.getStore());
                     }
                  }

                  if (destructive) {
                     holder.tryRemoveComponent(registry.getUnknownComponentType());
                  }

                  if (!holder.hasSerializableComponents(data)) {
                     return new com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.BlockHolder(b.blockId(), b.rotation(), b.filler(), b.supportValue(), (Holder)null);
                  } else {
                     return new com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.BlockHolder(b.blockId(), b.rotation(), b.filler(), b.supportValue(), holder.clone());
                  }
               } catch (Throwable var11) {
                  throw new RuntimeException("Failed to read block state: " + String.valueOf(b), var11);
               }
            }
         });
      } finally {
         this.blocksLock.writeLock().unlock();
      }

   }

   public void addEntityFromWorld(@Nonnull Holder<EntityStore> entityHolder) {
      TransformComponent transformComponent = (TransformComponent)entityHolder.getComponent(TransformComponent.getComponentType());

      assert transformComponent != null;

      transformComponent.getPosition().subtract((double)this.x, (double)this.y, (double)this.z);
      this.addEntityHolderRaw(entityHolder);
   }

   public void addEntityHolderRaw(Holder<EntityStore> entityHolder) {
      this.entitiesLock.writeLock().lock();

      try {
         this.entities.add(entityHolder);
      } finally {
         this.entitiesLock.writeLock().unlock();
      }

   }

   public void sortEntitiesByPosition() {
      this.entitiesLock.writeLock().lock();

      try {
         ComponentType<EntityStore, TransformComponent> transformType = TransformComponent.getComponentType();
         this.entities.sort((a, b) -> {
            TransformComponent ta = (TransformComponent)a.getComponent(transformType);
            TransformComponent tb = (TransformComponent)b.getComponent(transformType);
            if (ta == null && tb == null) {
               return 0;
            } else if (ta == null) {
               return 1;
            } else if (tb == null) {
               return -1;
            } else {
               Vector3d pa = ta.getPosition();
               Vector3d pb = tb.getPosition();
               if (pa == null && pb == null) {
                  return 0;
               } else if (pa == null) {
                  return 1;
               } else if (pb == null) {
                  return -1;
               } else {
                  int cmp = Double.compare(pa.getX(), pb.getX());
                  if (cmp != 0) {
                     return cmp;
                  } else {
                     cmp = Double.compare(pa.getY(), pb.getY());
                     return cmp != 0 ? cmp : Double.compare(pa.getZ(), pb.getZ());
                  }
               }
            }
         });
      } finally {
         this.entitiesLock.writeLock().unlock();
      }

   }

   public void placeNoReturn(@Nonnull World world, Vector3i position, ComponentAccessor<EntityStore> componentAccessor) {
      this.placeNoReturn((String)null, (CommandSender)null, FeedbackConsumer.DEFAULT, world, position, (BlockMask)null, componentAccessor);
   }

   public void placeNoReturn(String feedbackKey, CommandSender feedback, @Nonnull World outerWorld, ComponentAccessor<EntityStore> componentAccessor) {
      this.placeNoReturn(feedbackKey, feedback, FeedbackConsumer.DEFAULT, outerWorld, Vector3i.ZERO, (BlockMask)null, componentAccessor);
   }

   public void placeNoReturn(String feedbackKey, CommandSender feedback, @Nonnull FeedbackConsumer feedbackConsumer, @Nonnull World outerWorld, ComponentAccessor<EntityStore> componentAccessor) {
      this.placeNoReturn(feedbackKey, feedback, feedbackConsumer, outerWorld, Vector3i.ZERO, (BlockMask)null, componentAccessor);
   }

   public void placeNoReturn(@Nullable String feedbackKey, @Nullable CommandSender feedback, @Nonnull FeedbackConsumer feedbackConsumer, @Nonnull World outerWorld, @Nullable Vector3i position, @Nullable BlockMask blockMask, ComponentAccessor<EntityStore> componentAccessor) {
      IntUnaryOperator xConvert;
      if (position != null && position.getX() != 0) {
         xConvert = (localX) -> {
            return localX + this.x + position.getX() - this.anchorX;
         };
      } else {
         xConvert = (localX) -> {
            return localX + this.x - this.anchorX;
         };
      }

      IntUnaryOperator yConvert;
      if (position != null && position.getY() != 0) {
         yConvert = (localY) -> {
            return localY + this.y + position.getY() - this.anchorY;
         };
      } else {
         yConvert = (localY) -> {
            return localY + this.y - this.anchorY;
         };
      }

      IntUnaryOperator zConvert;
      if (position != null && position.getZ() != 0) {
         zConvert = (localZ) -> {
            return localZ + this.z + position.getZ() - this.anchorZ;
         };
      } else {
         zConvert = (localZ) -> {
            return localZ + this.z - this.anchorZ;
         };
      }

      LongSet dirtyChunks = new LongOpenHashSet();
      this.blocksLock.readLock().lock();

      try {
         BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();
         int totalBlocks = this.blocks.size();
         AtomicInteger counter = new AtomicInteger();
         outerWorld.getBlockBulkRelative(this.blocks, xConvert, yConvert, zConvert, (world, blockHolder, chunkIndex, chunk, blockX, blockY, blockZ, localX, localY, localZ) -> {
            int newBlockId = blockHolder.blockId();
            Holder<ChunkStore> holder = blockHolder.holder();
            this.placeBlockNoReturn(feedbackKey, feedback, feedbackConsumer, outerWorld, blockMask, dirtyChunks, assetMap, totalBlocks, counter.incrementAndGet(), chunkIndex, chunk, blockX, blockY, blockZ, newBlockId, blockHolder.rotation(), blockHolder.filler(), holder != null ? holder.clone() : null, componentAccessor);
         });
         outerWorld.getBlockBulkRelative(this.fluids, xConvert, yConvert, zConvert, (world, fluidStore, chunkIndex, chunk, blockX, blockY, blockZ, localX, localY, localZ) -> {
            this.placeFluidNoReturn(feedbackKey, feedback, feedbackConsumer, outerWorld, blockMask, dirtyChunks, assetMap, totalBlocks, counter.incrementAndGet(), chunkIndex, chunk, blockX, blockY, blockZ, fluidStore.fluidId, fluidStore.fluidLevel, componentAccessor);
         });
      } finally {
         this.blocksLock.readLock().unlock();
      }

      dirtyChunks.forEach((value) -> {
         outerWorld.getChunkLighting().invalidateLightInChunk(outerWorld.getChunkIfInMemory(value));
      });
      this.placeEntities(outerWorld, position);
      dirtyChunks.forEach((value) -> {
         outerWorld.getNotificationHandler().updateChunk(value);
      });
   }

   private void placeBlockNoReturn(String feedbackKey, CommandSender feedback, @Nonnull FeedbackConsumer feedbackConsumer, @Nonnull World outerWorld, @Nullable BlockMask blockMask, @Nonnull LongSet dirtyChunks, @Nonnull BlockTypeAssetMap<String, BlockType> assetMap, int totalBlocks, int counter, long chunkIndex, @Nonnull WorldChunk chunk, int blockX, int blockY, int blockZ, int newBlockId, int newRotation, int newFiller, Holder<ChunkStore> holder, ComponentAccessor<EntityStore> componentAccessor) {
      if (blockY >= 0 && blockY < 320) {
         int oldBlockId = chunk.getBlock(blockX, blockY, blockZ);
         if (blockMask == null || !blockMask.isExcluded(outerWorld, blockX, blockY, blockZ, this.min, this.max, oldBlockId)) {
            BlockChunk blockChunk = chunk.getBlockChunk();
            if (blockChunk.setBlock(blockX, blockY, blockZ, newBlockId, newRotation, newFiller)) {
               BlockType newBlockType = (BlockType)assetMap.getAsset(newBlockId);
               if (newBlockType != null && FluidTicker.isFullySolid(newBlockType)) {
                  this.clearFluidAtPosition(outerWorld, chunk, blockX, blockY, blockZ);
               }

               short height = blockChunk.getHeight(blockX, blockZ);
               if (height <= blockY) {
                  if (height == blockY && newBlockId == 0) {
                     blockChunk.updateHeight(blockX, blockZ, (short)blockY);
                  } else if (height < blockY && newBlockId != 0 && newBlockType != null && newBlockType.getOpacity() != Opacity.Transparent) {
                     blockChunk.setHeight(blockX, blockZ, (short)blockY);
                  }
               }
            }

            chunk.setState(blockX, blockY, blockZ, holder);
            dirtyChunks.add(chunkIndex);
            feedbackConsumer.accept(feedbackKey, totalBlocks, counter, feedback, componentAccessor);
         }
      }
   }

   private void placeFluidNoReturn(String feedbackKey, CommandSender feedback, @Nonnull FeedbackConsumer feedbackConsumer, @Nonnull World outerWorld, BlockMask blockMask, @Nonnull LongSet dirtyChunks, BlockTypeAssetMap<String, BlockType> assetMap, int totalBlocks, int counter, long chunkIndex, @Nonnull WorldChunk chunk, int blockX, int blockY, int blockZ, int newFluidId, byte newFluidLevel, ComponentAccessor<EntityStore> componentAccessor) {
      if (blockY >= 0 && blockY < 320) {
         int sectionY = ChunkUtil.chunkCoordinate(blockY);
         Store<ChunkStore> store = outerWorld.getChunkStore().getStore();
         ChunkColumn column = (ChunkColumn)store.getComponent(chunk.getReference(), ChunkColumn.getComponentType());
         Ref<ChunkStore> section = column.getSection(sectionY);
         FluidSection fluidSection = (FluidSection)store.ensureAndGetComponent(section, FluidSection.getComponentType());
         fluidSection.setFluid(blockX, blockY, blockZ, newFluidId, newFluidLevel);
         dirtyChunks.add(chunkIndex);
         feedbackConsumer.accept(feedbackKey, totalBlocks, counter, feedback, componentAccessor);
      }
   }

   private void clearFluidAtPosition(@Nonnull World world, @Nonnull WorldChunk chunk, int blockX, int blockY, int blockZ) {
      Ref<ChunkStore> ref = chunk.getReference();
      if (ref != null && ref.isValid()) {
         Store<ChunkStore> store = world.getChunkStore().getStore();
         ChunkColumn column = (ChunkColumn)store.getComponent(ref, ChunkColumn.getComponentType());
         if (column != null) {
            Ref<ChunkStore> section = column.getSection(ChunkUtil.chunkCoordinate(blockY));
            if (section != null) {
               FluidSection fluidSection = (FluidSection)store.getComponent(section, FluidSection.getComponentType());
               if (fluidSection != null) {
                  fluidSection.setFluid(blockX, blockY, blockZ, 0, (byte)0);
               }

            }
         }
      }
   }

   @Nonnull
   public BlockSelection place(CommandSender feedback, @Nonnull World outerWorld) {
      return this.place(feedback, outerWorld, Vector3i.ZERO, (BlockMask)null);
   }

   @Nonnull
   public BlockSelection place(CommandSender feedback, @Nonnull World outerWorld, BlockMask blockMask) {
      return this.place(feedback, outerWorld, Vector3i.ZERO, blockMask);
   }

   @Nonnull
   public BlockSelection place(CommandSender feedback, @Nonnull World outerWorld, Vector3i position, BlockMask blockMask) {
      return this.place(feedback, outerWorld, position, blockMask, DEFAULT_ENTITY_CONSUMER);
   }

   @Nonnull
   public BlockSelection place(CommandSender feedback, @Nonnull World outerWorld, @Nullable Vector3i position, @Nullable BlockMask blockMask, @Nonnull Consumer<Ref<EntityStore>> entityConsumer) {
      BlockSelection before = new BlockSelection(this.getBlockCount(), 0);
      before.setAnchor(this.anchorX, this.anchorY, this.anchorZ);
      before.setPosition(this.x, this.y, this.z);
      IntUnaryOperator xConvert;
      if (position != null && position.getX() != 0) {
         xConvert = (localX) -> {
            return localX + this.x + position.getX() - this.anchorX;
         };
      } else {
         xConvert = (localX) -> {
            return localX + this.x - this.anchorX;
         };
      }

      IntUnaryOperator yConvert;
      if (position != null && position.getY() != 0) {
         yConvert = (localY) -> {
            return localY + this.y + position.getY() - this.anchorY;
         };
      } else {
         yConvert = (localY) -> {
            return localY + this.y - this.anchorY;
         };
      }

      IntUnaryOperator zConvert;
      if (position != null && position.getZ() != 0) {
         zConvert = (localZ) -> {
            return localZ + this.z + position.getZ() - this.anchorZ;
         };
      } else {
         zConvert = (localZ) -> {
            return localZ + this.z - this.anchorZ;
         };
      }

      LongSet dirtyChunks = new LongOpenHashSet();
      this.blocksLock.readLock().lock();

      try {
         BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();
         outerWorld.getBlockBulkRelative(this.blocks, xConvert, yConvert, zConvert, (world, blockHolder, chunkIndex, chunk, blockX, blockY, blockZ, localX, localY, localZ) -> {
            Holder<ChunkStore> holder = blockHolder.holder();
            this.placeBlock(feedback, outerWorld, blockMask, before, dirtyChunks, assetMap, chunkIndex, chunk, blockX, blockY, blockZ, localX, localY, localZ, blockHolder.blockId(), blockHolder.rotation(), blockHolder.filler(), holder != null ? holder.clone() : null, blockHolder.supportValue());
         });
         IndexedLookupTableAssetMap<String, Fluid> fluidMap = Fluid.getAssetMap();
         outerWorld.getBlockBulkRelative(this.fluids, xConvert, yConvert, zConvert, (world, fluidStore, chunkIndex, chunk, blockX, blockY, blockZ, localX, localY, localZ) -> {
            this.placeFluid(feedback, outerWorld, before, dirtyChunks, fluidMap, chunkIndex, chunk, blockX, blockY, blockZ, localX, localY, localZ, fluidStore.fluidId, fluidStore.fluidLevel);
         });
      } finally {
         this.blocksLock.readLock().unlock();
      }

      dirtyChunks.forEach((value) -> {
         outerWorld.getChunkLighting().invalidateLightInChunk(outerWorld.getChunkIfInMemory(value));
      });
      this.placeEntities(outerWorld, position, entityConsumer);
      dirtyChunks.forEach((value) -> {
         outerWorld.getNotificationHandler().updateChunk(value);
      });
      return before;
   }

   private void placeBlock(CommandSender feedback, @Nonnull World outerWorld, @Nullable BlockMask blockMask, @Nonnull BlockSelection before, @Nonnull LongSet dirtyChunks, @Nonnull BlockTypeAssetMap<String, BlockType> assetMap, long chunkIndex, @Nonnull WorldChunk chunk, int blockX, int blockY, int blockZ, int localX, int localY, int localZ, int newBlockId, int newRotation, int newFiller, Holder<ChunkStore> holder, int newSupportValue) {
      if (blockY >= 0 && blockY < 320) {
         Store<ChunkStore> chunkStore = chunk.getWorld().getChunkStore().getStore();
         ChunkColumn chunkColumn = (ChunkColumn)chunkStore.getComponent(chunk.getReference(), ChunkColumn.getComponentType());
         Ref<ChunkStore> section = chunkColumn.getSection(ChunkUtil.chunkCoordinate(blockY));
         BlockSection blockSection = (BlockSection)chunkStore.getComponent(section, BlockSection.getComponentType());
         int oldBlockId = chunk.getBlock(blockX, blockY, blockZ);
         if (blockMask == null || !blockMask.isExcluded(outerWorld, blockX, blockY, blockZ, this.min, this.max, oldBlockId)) {
            BlockPhysics blockPhysics = section != null ? (BlockPhysics)chunkStore.getComponent(section, BlockPhysics.getComponentType()) : null;
            int supportValue = blockPhysics != null ? blockPhysics.get(blockX, blockY, blockZ) : 0;
            int filler = blockSection.getFiller(blockX, blockY, blockZ);
            int rotation = blockSection.getRotationIndex(blockX, blockY, blockZ);
            before.addBlockAtLocalPos(localX, localY, localZ, oldBlockId, rotation, filler, supportValue, chunk.getBlockComponentHolder(blockX, blockY, blockZ));
            BlockChunk blockChunk = chunk.getBlockChunk();
            if (blockChunk.setBlock(blockX, blockY, blockZ, newBlockId, newRotation, newFiller)) {
               BlockType newBlockType = (BlockType)assetMap.getAsset(newBlockId);
               if (newBlockType != null && FluidTicker.isFullySolid(newBlockType)) {
                  this.clearFluidAtPosition(outerWorld, chunk, blockX, blockY, blockZ);
               }

               short height = blockChunk.getHeight(blockX, blockZ);
               if (height <= blockY) {
                  if (height == blockY && newBlockId == 0) {
                     blockChunk.updateHeight(blockX, blockZ, (short)blockY);
                  } else if (height < blockY && newBlockId != 0 && newBlockType.getOpacity() != Opacity.Transparent) {
                     blockChunk.setHeight(blockX, blockZ, (short)blockY);
                  }
               }

               if (newSupportValue != supportValue) {
                  if (newSupportValue != 0) {
                     if (blockPhysics == null) {
                        blockPhysics = (BlockPhysics)chunkStore.ensureAndGetComponent(section, BlockPhysics.getComponentType());
                     }

                     blockPhysics.set(blockX, blockY, blockZ, newSupportValue);
                  } else if (blockPhysics != null) {
                     blockPhysics.set(blockX, blockY, blockZ, 0);
                  }
               }
            }

            chunk.setState(blockX, blockY, blockZ, holder);
            dirtyChunks.add(chunkIndex);
         }
      }
   }

   private void placeFluid(CommandSender feedback, @Nonnull World outerWorld, @Nonnull BlockSelection before, @Nonnull LongSet dirtyChunks, IndexedLookupTableAssetMap<String, Fluid> assetMap, long chunkIndex, @Nonnull WorldChunk chunk, int blockX, int blockY, int blockZ, int localX, int localY, int localZ, int newFluidId, byte newFluidLevel) {
      if (blockY >= 0 && blockY < 320) {
         int sectionY = ChunkUtil.chunkCoordinate(blockY);
         Store<ChunkStore> store = outerWorld.getChunkStore().getStore();
         ChunkColumn column = (ChunkColumn)store.getComponent(chunk.getReference(), ChunkColumn.getComponentType());
         Ref<ChunkStore> section = column.getSection(sectionY);
         FluidSection fluidSection = (FluidSection)store.ensureAndGetComponent(section, FluidSection.getComponentType());
         int oldFluidId = fluidSection.getFluidId(blockX, blockY, blockZ);
         byte oldFluidLevel = fluidSection.getFluidLevel(blockX, blockY, blockZ);
         before.addFluidAtLocalPos(localX, localY, localZ, oldFluidId, oldFluidLevel);
         fluidSection.setFluid(blockX, blockY, blockZ, newFluidId, newFluidLevel);
         dirtyChunks.add(chunkIndex);
      }
   }

   private void placeEntities(@Nonnull World world, @Nonnull Vector3i pos) {
      this.placeEntities(world, pos, DEFAULT_ENTITY_CONSUMER);
   }

   private void placeEntities(@Nonnull World world, @Nonnull Vector3i pos, @Nonnull Consumer<Ref<EntityStore>> entityConsumer) {
      this.entitiesLock.readLock().lock();

      try {
         Iterator var4 = this.entities.iterator();

         while(var4.hasNext()) {
            Holder<EntityStore> entityHolder = (Holder)var4.next();
            Ref<EntityStore> entity = this.placeEntity(world, entityHolder.clone(), pos, this.prefabId);
            if (entity == null) {
               LOGGER.at(Level.WARNING).log("Failed to spawn entity in world %s! Data: %s", world.getName(), entityHolder);
            } else {
               entityConsumer.accept(entity);
            }
         }
      } finally {
         this.entitiesLock.readLock().unlock();
      }

   }

   @Nonnull
   private Ref<EntityStore> placeEntity(@Nonnull World world, @Nonnull Holder<EntityStore> entityHolder, @Nonnull Vector3i pos, int prefabId) {
      TransformComponent transformComponent = (TransformComponent)entityHolder.getComponent(TransformComponent.getComponentType());

      assert transformComponent != null;

      transformComponent.getPosition().add((double)(this.x + pos.getX() - this.anchorX), (double)(this.y + pos.getY() - this.anchorY), (double)(this.z + pos.getZ() - this.anchorZ));
      Store<EntityStore> store = world.getEntityStore().getStore();
      PrefabPlaceEntityEvent prefabPlaceEntityEvent = new PrefabPlaceEntityEvent(prefabId, entityHolder);
      store.invoke(prefabPlaceEntityEvent);
      entityHolder.addComponent(FromPrefab.getComponentType(), FromPrefab.INSTANCE);
      Ref<EntityStore> entityRef = new Ref(store);
      world.execute(() -> {
         store.addEntity(entityHolder, entityRef, AddReason.LOAD);
      });
      return entityRef;
   }

   @Nonnull
   public BlockSelection rotate(@Nonnull Axis axis, int angle) {
      BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();
      BlockSelection selection = new BlockSelection(this.getBlockCount(), this.getEntityCount());
      selection.copyPropertiesFrom(this);
      Vector3i mutable = new Vector3i(0, 0, 0);
      Rotation rotation = Rotation.ofDegrees(angle);
      this.forEachBlock((x1, y1, z1, block) -> {
         mutable.assign(x1 - this.anchorX, y1 - this.anchorY, z1 - this.anchorZ);
         axis.rotate(mutable, angle);
         int blockId = block.blockId;
         Holder<ChunkStore> holder = block.holder;
         RotationTuple blockRotation = RotationTuple.get(block.rotation);
         RotationTuple var10000;
         switch(1.$SwitchMap$com$hypixel$hytale$math$Axis[axis.ordinal()]) {
         case 1:
            var10000 = RotationTuple.of(blockRotation.yaw(), blockRotation.pitch().add(rotation), blockRotation.roll());
            break;
         case 2:
            var10000 = RotationTuple.of(blockRotation.yaw().add(rotation), blockRotation.pitch(), blockRotation.roll());
            break;
         case 3:
            var10000 = RotationTuple.of(blockRotation.yaw(), blockRotation.pitch(), blockRotation.roll().add(rotation));
            break;
         default:
            throw new MatchException((String)null, (Throwable)null);
         }

         RotationTuple rotatedRotation = var10000;
         if (rotatedRotation == null) {
            rotatedRotation = blockRotation;
         }

         int rotatedFiller = BlockRotationUtil.getRotatedFiller(block.filler, axis, rotation);
         selection.addBlock0(mutable.getX() + this.anchorX, mutable.getY() + this.anchorY, mutable.getZ() + this.anchorZ, blockId, rotatedRotation.index(), rotatedFiller, block.supportValue(), holder != null ? holder.clone() : null);
      });
      this.forEachEntity((entityHolder) -> {
         Holder<EntityStore> copy = entityHolder.clone();
         TransformComponent transformComponent = (TransformComponent)copy.getComponent(TransformComponent.getComponentType());

         assert transformComponent != null;

         Vector3d position = transformComponent.getPosition();
         HeadRotation headRotationComponent = (HeadRotation)copy.getComponent(HeadRotation.getComponentType());
         position.subtract((double)this.anchorX, (double)this.anchorY, (double)this.anchorZ).subtract(0.5D, 0.0D, 0.5D);
         axis.rotate(position, angle);
         position.add((double)this.anchorX, (double)this.anchorY, (double)this.anchorZ).add(0.5D, 0.0D, 0.5D);
         transformComponent.getRotation().addRotationOnAxis(axis, angle);
         if (headRotationComponent != null) {
            headRotationComponent.getRotation().addRotationOnAxis(axis, angle);
         }

         selection.addEntity0(copy);
      });
      return selection;
   }

   @Nonnull
   public BlockSelection rotate(@Nonnull Axis axis, int angle, @Nonnull Vector3f originOfRotation) {
      BlockSelection selection = new BlockSelection(this.getBlockCount(), this.getEntityCount());
      selection.copyPropertiesFrom(this);
      Vector3d mutable = new Vector3d(0.0D, 0.0D, 0.0D);
      Rotation rotation = Rotation.ofDegrees(angle);
      Vector3f finalOriginOfRotation = originOfRotation.clone().subtract((float)this.x, (float)this.y, (float)this.z);
      this.forEachBlock((x1, y1, z1, block) -> {
         mutable.assign((double)((float)x1 - finalOriginOfRotation.x), (double)((float)y1 - finalOriginOfRotation.y), (double)((float)z1 - finalOriginOfRotation.z));
         axis.rotate(mutable, angle);
         int blockId = block.blockId;
         Holder<ChunkStore> holder = block.holder;
         int supportValue = block.supportValue();
         RotationTuple blockRotation = RotationTuple.get(block.rotation);
         RotationTuple var10000;
         switch(1.$SwitchMap$com$hypixel$hytale$math$Axis[axis.ordinal()]) {
         case 1:
            var10000 = RotationTuple.of(blockRotation.yaw(), blockRotation.pitch().add(rotation), blockRotation.roll());
            break;
         case 2:
            var10000 = RotationTuple.of(blockRotation.yaw().add(rotation), blockRotation.pitch(), blockRotation.roll());
            break;
         case 3:
            var10000 = RotationTuple.of(blockRotation.yaw(), blockRotation.pitch(), blockRotation.roll().add(rotation));
            break;
         default:
            throw new MatchException((String)null, (Throwable)null);
         }

         RotationTuple rotatedRotation = var10000;
         if (rotatedRotation == null) {
            rotatedRotation = blockRotation;
         }

         int rotatedFiller = BlockRotationUtil.getRotatedFiller(block.filler, axis, rotation);
         selection.addBlock0((int)(mutable.getX() + (double)finalOriginOfRotation.x), (int)(mutable.getY() + (double)finalOriginOfRotation.z), (int)(mutable.getZ() + (double)finalOriginOfRotation.z), blockId, rotatedRotation.index(), rotatedFiller, supportValue, holder != null ? holder.clone() : null);
      });
      this.forEachEntity((entityHolder) -> {
         Holder<EntityStore> copy = entityHolder.clone();
         TransformComponent transformComponent = (TransformComponent)copy.getComponent(TransformComponent.getComponentType());

         assert transformComponent != null;

         Vector3d position = transformComponent.getPosition();
         HeadRotation headRotationComponent = (HeadRotation)copy.getComponent(HeadRotation.getComponentType());
         position.subtract((double)this.anchorX, (double)this.anchorY, (double)this.anchorZ).subtract(0.5D, 0.0D, 0.5D);
         axis.rotate(position, angle);
         position.add((double)this.anchorX, (double)this.anchorY, (double)this.anchorZ).add(0.5D, 0.0D, 0.5D);
         transformComponent.getRotation().addRotationOnAxis(axis, angle);
         if (headRotationComponent != null) {
            headRotationComponent.getRotation().addRotationOnAxis(axis, angle);
         }

         selection.addEntity0(copy);
      });
      return selection;
   }

   @Nonnull
   public BlockSelection rotateArbitrary(float yawDegrees, float pitchDegrees, float rollDegrees) {
      double pitchRad = Math.toRadians((double)pitchDegrees);
      double yawRad = Math.toRadians((double)yawDegrees);
      double rollRad = Math.toRadians((double)rollDegrees);
      Matrix4d rotation = new Matrix4d();
      rotation.setRotateEuler(pitchRad, yawRad, rollRad);
      Matrix4d inverse = new Matrix4d(rotation);
      inverse.invert();
      Vector3d tempVec = new Vector3d();
      int destMinX = Integer.MAX_VALUE;
      int destMinY = Integer.MAX_VALUE;
      int destMinZ = Integer.MAX_VALUE;
      int destMaxX = Integer.MIN_VALUE;
      int destMaxY = Integer.MIN_VALUE;
      int destMaxZ = Integer.MIN_VALUE;
      int srcMinX = Integer.MAX_VALUE;
      int srcMinY = Integer.MAX_VALUE;
      int srcMinZ = Integer.MAX_VALUE;
      int srcMaxX = Integer.MIN_VALUE;
      int srcMaxY = Integer.MIN_VALUE;
      int srcMaxZ = Integer.MIN_VALUE;
      this.blocksLock.readLock().lock();

      int dx;
      int dy;
      try {
         for(ObjectIterator var25 = this.blocks.long2ObjectEntrySet().iterator(); var25.hasNext(); srcMaxZ = Math.max(srcMaxZ, dy)) {
            Entry<com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.BlockHolder> entry = (Entry)var25.next();
            long packed = entry.getLongKey();
            int bx = BlockUtil.unpackX(packed) - this.anchorX;
            dx = BlockUtil.unpackY(packed) - this.anchorY;
            dy = BlockUtil.unpackZ(packed) - this.anchorZ;
            srcMinX = Math.min(srcMinX, bx);
            srcMinY = Math.min(srcMinY, dx);
            srcMinZ = Math.min(srcMinZ, dy);
            srcMaxX = Math.max(srcMaxX, bx);
            srcMaxY = Math.max(srcMaxY, dx);
         }
      } finally {
         this.blocksLock.readLock().unlock();
      }

      if (srcMinX == Integer.MAX_VALUE) {
         BlockSelection selection = new BlockSelection(0, this.getEntityCount());
         selection.copyPropertiesFrom(this);
         return selection;
      } else {
         int[][] corners = new int[][]{{srcMinX, srcMinY, srcMinZ}, {srcMaxX, srcMinY, srcMinZ}, {srcMinX, srcMaxY, srcMinZ}, {srcMaxX, srcMaxY, srcMinZ}, {srcMinX, srcMinY, srcMaxZ}, {srcMaxX, srcMinY, srcMaxZ}, {srcMinX, srcMaxY, srcMaxZ}, {srcMaxX, srcMaxY, srcMaxZ}};
         int[][] var54 = corners;
         int var56 = corners.length;

         int dz;
         for(int var28 = 0; var28 < var56; ++var28) {
            int[] corner = var54[var28];
            tempVec.assign((double)corner[0], (double)corner[1], (double)corner[2]);
            rotation.multiplyDirection(tempVec);
            dx = MathUtil.floor(tempVec.x);
            dy = MathUtil.floor(tempVec.y);
            dz = MathUtil.floor(tempVec.z);
            destMinX = Math.min(destMinX, dx);
            destMinY = Math.min(destMinY, dy);
            destMinZ = Math.min(destMinZ, dz);
            destMaxX = Math.max(destMaxX, dx + 1);
            destMaxY = Math.max(destMaxY, dy + 1);
            destMaxZ = Math.max(destMaxZ, dz + 1);
         }

         BlockSelection selection = new BlockSelection(this.getBlockCount(), this.getEntityCount());
         selection.copyPropertiesFrom(this);
         Rotation snappedYaw = Rotation.ofDegrees(Math.round(yawDegrees / 90.0F) * 90);
         Rotation snappedPitch = Rotation.ofDegrees(Math.round(pitchDegrees / 90.0F) * 90);
         Rotation snappedRoll = Rotation.ofDegrees(Math.round(rollDegrees / 90.0F) * 90);
         this.blocksLock.readLock().lock();

         try {
            int sx;
            int sy;
            int sz;
            long packedSource;
            for(dx = destMinX; dx <= destMaxX; ++dx) {
               for(dy = destMinY; dy <= destMaxY; ++dy) {
                  for(dz = destMinZ; dz <= destMaxZ; ++dz) {
                     tempVec.assign((double)dx, (double)dy, (double)dz);
                     inverse.multiplyDirection(tempVec);
                     sx = (int)Math.round(tempVec.x);
                     sy = (int)Math.round(tempVec.y);
                     sz = (int)Math.round(tempVec.z);
                     packedSource = BlockUtil.pack(sx + this.anchorX, sy + this.anchorY, sz + this.anchorZ);
                     com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.BlockHolder block = (com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.BlockHolder)this.blocks.get(packedSource);
                     if (block != null) {
                        RotationTuple blockRotation = RotationTuple.get(block.rotation());
                        RotationTuple rotatedRotation = RotationTuple.of(blockRotation.yaw().add(snappedYaw), blockRotation.pitch().add(snappedPitch), blockRotation.roll().add(snappedRoll));
                        if (rotatedRotation == null) {
                           rotatedRotation = blockRotation;
                        }

                        int rotatedFiller = block.filler();
                        if (rotatedFiller != 0) {
                           int fillerX = FillerBlockUtil.unpackX(rotatedFiller);
                           int fillerY = FillerBlockUtil.unpackY(rotatedFiller);
                           int fillerZ = FillerBlockUtil.unpackZ(rotatedFiller);
                           tempVec.assign((double)fillerX, (double)fillerY, (double)fillerZ);
                           rotation.multiplyDirection(tempVec);
                           rotatedFiller = FillerBlockUtil.pack((int)Math.round(tempVec.x), (int)Math.round(tempVec.y), (int)Math.round(tempVec.z));
                        }

                        Holder<ChunkStore> holder = block.holder();
                        selection.addBlock0(dx + this.anchorX, dy + this.anchorY, dz + this.anchorZ, block.blockId(), rotatedRotation.index(), rotatedFiller, block.supportValue(), holder != null ? holder.clone() : null);
                     }
                  }
               }
            }

            for(dx = destMinX; dx <= destMaxX; ++dx) {
               for(dy = destMinY; dy <= destMaxY; ++dy) {
                  for(dz = destMinZ; dz <= destMaxZ; ++dz) {
                     tempVec.assign((double)dx, (double)dy, (double)dz);
                     inverse.multiplyDirection(tempVec);
                     sx = (int)Math.round(tempVec.x);
                     sy = (int)Math.round(tempVec.y);
                     sz = (int)Math.round(tempVec.z);
                     packedSource = BlockUtil.pack(sx + this.anchorX, sy + this.anchorY, sz + this.anchorZ);
                     com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.FluidHolder fluid = (com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.FluidHolder)this.fluids.get(packedSource);
                     if (fluid != null) {
                        selection.addFluid0(dx + this.anchorX, dy + this.anchorY, dz + this.anchorZ, fluid.fluidId(), fluid.fluidLevel());
                     }
                  }
               }
            }
         } finally {
            this.blocksLock.readLock().unlock();
         }

         float yawRadF = (float)yawRad;
         float pitchRadF = (float)pitchRad;
         float rollRadF = (float)rollRad;
         this.forEachEntity((entityHolder) -> {
            Holder<EntityStore> copy = entityHolder.clone();
            TransformComponent transformComponent = (TransformComponent)copy.getComponent(TransformComponent.getComponentType());

            assert transformComponent != null;

            Vector3d position = transformComponent.getPosition();
            HeadRotation headRotationComp = (HeadRotation)copy.getComponent(HeadRotation.getComponentType());
            position.subtract((double)this.anchorX, (double)this.anchorY, (double)this.anchorZ).subtract(0.5D, 0.0D, 0.5D);
            rotation.multiplyDirection(position);
            position.add((double)this.anchorX, (double)this.anchorY, (double)this.anchorZ).add(0.5D, 0.0D, 0.5D);
            Vector3f bodyRotation = transformComponent.getRotation();
            bodyRotation.addPitch(pitchRadF);
            bodyRotation.addYaw(yawRadF);
            bodyRotation.addRoll(rollRadF);
            if (headRotationComp != null) {
               Vector3f headRot = headRotationComp.getRotation();
               headRot.addPitch(pitchRadF);
               headRot.addYaw(yawRadF);
               headRot.addRoll(rollRadF);
            }

            selection.addEntity0(copy);
         });
         return selection;
      }
   }

   @Nonnull
   public BlockSelection flip(@Nonnull Axis axis) {
      BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();
      BlockSelection selection = new BlockSelection(this.getBlockCount(), this.getEntityCount());
      selection.copyPropertiesFrom(this);
      Vector3i mutable = new Vector3i(0, 0, 0);
      this.forEachBlock((x1, y1, z1, block) -> {
         mutable.assign(x1 - this.anchorX, y1 - this.anchorY, z1 - this.anchorZ);
         axis.flip(mutable);
         int blockId = block.blockId;
         Holder<ChunkStore> holder = block.holder;
         int supportValue = block.supportValue();
         int filler = block.filler;
         BlockType blockType = (BlockType)assetMap.getAsset(blockId);
         VariantRotation variantRotation = blockType.getVariantRotation();
         if (variantRotation == VariantRotation.None) {
            selection.addBlock0(mutable.getX() + this.anchorX, mutable.getY() + this.anchorY, mutable.getZ() + this.anchorZ, block);
         } else {
            RotationTuple blockRotation = RotationTuple.get(block.rotation);
            RotationTuple rotatedRotation = BlockRotationUtil.getFlipped(blockRotation, blockType.getFlipType(), axis, variantRotation);
            if (rotatedRotation != null) {
               rotatedRotation = blockRotation;
            }

            int rotatedFiller = BlockRotationUtil.getFlippedFiller(filler, axis);
            selection.addBlock0(mutable.getX() + this.anchorX, mutable.getY() + this.anchorY, mutable.getZ() + this.anchorZ, blockId, rotatedRotation.index(), rotatedFiller, supportValue, holder != null ? holder.clone() : null);
         }
      });
      this.forEachEntity((entityHolder) -> {
         Holder<EntityStore> copy = entityHolder.clone();
         HeadRotation headRotationComponent = (HeadRotation)copy.getComponent(HeadRotation.getComponentType());

         assert headRotationComponent != null;

         Vector3f headRotation = headRotationComponent.getRotation();
         TransformComponent transformComponent = (TransformComponent)copy.getComponent(TransformComponent.getComponentType());

         assert transformComponent != null;

         Vector3d position = transformComponent.getPosition();
         Vector3f bodyRotation = transformComponent.getRotation();
         position.subtract((double)this.anchorX, (double)this.anchorY, (double)this.anchorZ).subtract(0.5D, 0.0D, 0.5D);
         axis.flip(position);
         position.add((double)this.anchorX, (double)this.anchorY, (double)this.anchorZ).add(0.5D, 0.0D, 0.5D);
         axis.flipRotation(bodyRotation);
         axis.flipRotation(headRotation);
         selection.addEntity0(copy);
      });
      return selection;
   }

   @Nonnull
   public BlockSelection relativize() {
      return this.relativize(this.anchorX, this.anchorY, this.anchorZ);
   }

   @Nonnull
   public BlockSelection relativize(int originX, int originY, int originZ) {
      if (originX == 0 && originY == 0 && originZ == 0) {
         return this.cloneSelection();
      } else {
         BlockSelection selection = new BlockSelection(this.getBlockCount(), this.getEntityCount());
         selection.setAnchor(this.anchorX - originX, this.anchorY - originY, this.anchorZ - originZ);
         selection.setPosition(this.x - originX, this.y - originY, this.z - originZ);
         selection.setSelectionArea(this.min.clone().subtract(originX, originY, originZ), this.max.clone().subtract(originX, originY, originZ));
         this.forEachBlock((x, y, z, block) -> {
            selection.addBlock0(x - originX, y - originY, z - originZ, block);
         });
         this.forEachEntity((holder) -> {
            Holder<EntityStore> copy = holder.clone();
            TransformComponent transformComponent = (TransformComponent)copy.getComponent(TransformComponent.getComponentType());

            assert transformComponent != null;

            transformComponent.getPosition().subtract((double)originX, (double)originY, (double)originZ);
            selection.addEntity0(copy);
         });
         return selection;
      }
   }

   @Nonnull
   public BlockSelection cloneSelection() {
      BlockSelection selection = new BlockSelection(this.getBlockCount(), this.getEntityCount());
      selection.copyPropertiesFrom(this);
      this.blocksLock.readLock().lock();

      try {
         Long2ObjectMaps.fastForEach(this.blocks, (entry) -> {
            selection.blocks.put(entry.getLongKey(), ((com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.BlockHolder)entry.getValue()).cloneBlockHolder());
         });
         selection.fluids.putAll(this.fluids);
      } finally {
         this.blocksLock.readLock().unlock();
      }

      this.entitiesLock.readLock().lock();

      try {
         this.entities.forEach((holder) -> {
            selection.entities.add(holder.clone());
         });
      } finally {
         this.entitiesLock.readLock().unlock();
      }

      return selection;
   }

   public void add(@Nonnull BlockSelection other) {
      this.entitiesLock.writeLock().lock();

      try {
         other.forEachEntity((holder) -> {
            Holder<EntityStore> copy = holder.clone();
            TransformComponent transformComponent = (TransformComponent)copy.getComponent(TransformComponent.getComponentType());

            assert transformComponent != null;

            transformComponent.getPosition().add((double)other.x, (double)other.y, (double)other.z).subtract((double)this.x, (double)this.y, (double)this.z);
            this.addEntity0(copy);
         });
      } finally {
         this.entitiesLock.writeLock().unlock();
      }

      this.blocksLock.writeLock().lock();

      try {
         other.forEachBlock((x1, y1, z1, block) -> {
            this.addBlock0(x1 + other.x - this.x, y1 + other.y - this.y, z1 + other.z - this.z, block);
         });
         other.forEachFluid((x1, y1, z1, fluidId, fluidLevel) -> {
            this.addFluid0(x1 + other.x - this.x, y1 + other.y - this.y, z1 + other.z - this.z, fluidId, fluidLevel);
         });
      } finally {
         this.blocksLock.writeLock().unlock();
      }

   }

   @Nonnull
   public MetricResults toMetricResults() {
      return METRICS_REGISTRY.toMetricResults(this);
   }

   @Nonnull
   public EditorBlocksChange toPacket() {
      EditorBlocksChange packet = new EditorBlocksChange();
      this.blocksLock.readLock().lock();

      try {
         int blockCount = this.getBlockCount();
         List<BlockChange> blockList = new ObjectArrayList(blockCount);
         this.forEachBlock((x1, y1, z1, block) -> {
            if (block.filler == 0) {
               blockList.add(new BlockChange(x1 - this.anchorX, y1 - this.anchorY, z1 - this.anchorZ, block.blockId, (byte)block.rotation));
            }
         });
         List<FluidChange> fluidList = new ObjectArrayList();
         this.forEachFluid((x1, y1, z1, fluidId, fluidLevel) -> {
            if (fluidId != 0) {
               fluidList.add(new FluidChange(x1 - this.anchorX, y1 - this.anchorY, z1 - this.anchorZ, fluidId, fluidLevel));
            }

         });
         packet.blocksChange = (BlockChange[])blockList.toArray((x$0) -> {
            return new BlockChange[x$0];
         });
         packet.fluidsChange = (FluidChange[])fluidList.toArray((x$0) -> {
            return new FluidChange[x$0];
         });
         packet.advancedPreview = true;
         packet.blocksCount = blockCount;
      } finally {
         this.blocksLock.readLock().unlock();
      }

      return packet;
   }

   @Nonnull
   public EditorBlocksChange toSelectionPacket() {
      EditorBlocksChange packet = new EditorBlocksChange();
      EditorSelection selection = new EditorSelection();
      if (this.min != null) {
         selection.minX = this.min.getX();
         selection.minY = this.min.getY();
         selection.minZ = this.min.getZ();
      }

      if (this.max != null) {
         selection.maxX = this.max.getX();
         selection.maxY = this.max.getY();
         selection.maxZ = this.max.getZ();
      }

      packet.selection = selection;
      return packet;
   }

   @Nonnull
   public EditorBlocksChange toPacketWithSelection() {
      EditorBlocksChange packet = this.toPacket();
      if (this.min != null && this.max != null) {
         EditorSelection selection = new EditorSelection();
         selection.minX = this.min.getX();
         selection.minY = this.min.getY();
         selection.minZ = this.min.getZ();
         selection.maxX = this.max.getX();
         selection.maxY = this.max.getY();
         selection.maxZ = this.max.getZ();
         packet.selection = selection;
      }

      return packet;
   }

   public void tryFixFiller(boolean allowDestructive) {
      this.blocksLock.readLock().lock();

      LongOpenHashSet blockPositions;
      try {
         blockPositions = new LongOpenHashSet(this.blocks.keySet());
      } finally {
         this.blocksLock.readLock().unlock();
      }

      BlockTypeAssetMap blockTypeAssetMap = BlockType.getAssetMap();
      IndexedLookupTableAssetMap hitboxAssetMap = BlockBoundingBoxes.getAssetMap();
      LongIterator it = blockPositions.iterator();

      while(true) {
         while(true) {
            while(true) {
               int x;
               int y;
               int z;
               com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.BlockHolder blockHolder;
               int blockId;
               BlockType blockType;
               do {
                  do {
                     do {
                        if (!it.hasNext()) {
                           return;
                        }

                        long packed = it.nextLong();
                        x = BlockUtil.unpackX(packed);
                        y = BlockUtil.unpackY(packed);
                        z = BlockUtil.unpackZ(packed);
                        blockHolder = this.getBlockHolderAtLocalPos(x, y, z);
                     } while(blockHolder == null);

                     blockId = blockHolder.blockId;
                  } while(blockId == 0);

                  blockType = (BlockType)blockTypeAssetMap.getAsset(blockId);
               } while(blockType == null);

               String id = blockType.getId();
               if (blockHolder.filler != 0) {
                  int fillerX = FillerBlockUtil.unpackX(blockHolder.filler);
                  int fillerY = FillerBlockUtil.unpackY(blockHolder.filler);
                  int fillerZ = FillerBlockUtil.unpackZ(blockHolder.filler);
                  com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.BlockHolder baseBlockHolder = this.getBlockHolderAtLocalPos(x - fillerX, y - fillerY, z - fillerZ);
                  BlockType baseBlock = (BlockType)blockTypeAssetMap.getAsset(baseBlockHolder.blockId);
                  if (baseBlock == null) {
                     this.addBlockAtLocalPos(x, y, z, 0, 0, 0, 0);
                  } else {
                     String baseId = baseBlock.getId();
                     BlockBoundingBoxes hitbox = (BlockBoundingBoxes)hitboxAssetMap.getAsset(baseBlock.getHitboxTypeIndex());
                     if (hitbox != null && (!id.equals(baseId) || baseBlockHolder.rotation != blockHolder.rotation || !hitbox.get(blockHolder.rotation).getBoundingBox().containsBlock(fillerX, fillerY, fillerZ))) {
                        this.addBlockAtLocalPos(x, y, z, 0, 0, 0, 0);
                     }
                  }
               } else {
                  BlockBoundingBoxes hitbox = (BlockBoundingBoxes)hitboxAssetMap.getAsset(blockType.getHitboxTypeIndex());
                  if (hitbox != null && hitbox.protrudesUnitBox()) {
                     FillerBlockUtil.forEachFillerBlock(hitbox.get(blockHolder.rotation), (x1, y1, z1) -> {
                        if (x1 != 0 || y1 != 0 || z1 != 0) {
                           int worldX = x + x1;
                           int worldY = y + y1;
                           int worldZ = z + z1;
                           com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection.BlockHolder fillerBlockHolder = this.getBlockHolderAtLocalPos(worldX, worldY, worldZ);
                           BlockType fillerBlock = (BlockType)blockTypeAssetMap.getAsset(fillerBlockHolder.blockId);
                           int filler = FillerBlockUtil.pack(x1, y1, z1);
                           if (fillerBlock == null || !fillerBlock.getId().equals(id) || filler != fillerBlockHolder.filler) {
                              if (!allowDestructive && fillerBlockHolder.blockId != 0) {
                                 String var10002 = fillerBlock.getId();
                                 throw new IllegalArgumentException("Cannot replace " + var10002 + " with " + blockType.getId() + " in order to repair filler\n at " + worldX + ", " + worldY + ", " + worldZ + "\n base " + x + ", " + y + ", " + z);
                              }

                              this.addBlockAtLocalPos(worldX, worldY, worldZ, blockId, blockHolder.rotation, filler, 0);
                           }

                        }
                     });
                  }
               }
            }
         }
      }
   }

   public void reserializeEntities(@Nonnull Store<EntityStore> store, boolean destructive) throws IOException {
      this.entitiesLock.writeLock().lock();

      try {
         if (!this.entities.isEmpty()) {
            ComponentRegistry<EntityStore> registry = EntityStore.REGISTRY;
            Data<EntityStore> data = registry.getData();
            SystemType<EntityStore, com.hypixel.hytale.server.core.modules.entity.EntityModule.MigrationSystem> systemType = EntityModule.get().getMigrationSystemType();
            BitSet systemIndexes = data.getSystemIndexesForType(systemType);
            int systemIndex = -1;

            com.hypixel.hytale.server.core.modules.entity.EntityModule.MigrationSystem system;
            int i;
            Holder holder;
            while((systemIndex = systemIndexes.nextSetBit(systemIndex + 1)) >= 0) {
               system = (com.hypixel.hytale.server.core.modules.entity.EntityModule.MigrationSystem)data.getSystem(systemIndex, systemType);

               for(i = 0; i < this.entities.size(); ++i) {
                  holder = (Holder)this.entities.get(i);
                  if (system.test(registry, holder.getArchetype())) {
                     system.onEntityAdd(holder, AddReason.LOAD, store);
                  }
               }
            }

            systemIndex = -1;

            while((systemIndex = systemIndexes.nextSetBit(systemIndex + 1)) >= 0) {
               system = (com.hypixel.hytale.server.core.modules.entity.EntityModule.MigrationSystem)data.getSystem(systemIndex, systemType);

               for(i = 0; i < this.entities.size(); ++i) {
                  holder = (Holder)this.entities.get(i);
                  if (system.test(registry, holder.getArchetype())) {
                     system.onEntityRemoved(holder, RemoveReason.UNLOAD, store);
                  }
               }
            }

            if (!destructive) {
               return;
            }

            for(int i = 0; i < this.entities.size(); ++i) {
               Holder<EntityStore> holder = (Holder)this.entities.get(i);
               holder.tryRemoveComponent(registry.getUnknownComponentType());
            }

            return;
         }
      } finally {
         this.entitiesLock.writeLock().unlock();
      }

   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.blocksLock);
      return "BlockSelection{blocksLock=" + var10000 + ", x=" + this.x + ", y=" + this.y + ", z=" + this.z + ", originX=" + this.anchorX + ", originY=" + this.anchorY + ", originZ=" + this.anchorZ + ", min=" + String.valueOf(this.min) + ", max=" + String.valueOf(this.max) + "}";
   }

   static {
      METRICS_REGISTRY = (new MetricsRegistry()).register("BlocksLock", (selection) -> {
         return selection.blocksLock.toString();
      }, Codec.STRING).register("EntitiesLock", (selection) -> {
         return selection.entitiesLock.toString();
      }, Codec.STRING).register("Position", (selection) -> {
         return new Vector3i(selection.x, selection.y, selection.z);
      }, Vector3i.CODEC).register("Anchor", (selection) -> {
         return new Vector3i(selection.anchorX, selection.anchorY, selection.anchorZ);
      }, Vector3i.CODEC).register("Min", BlockSelection::getSelectionMin, Vector3i.CODEC).register("Max", BlockSelection::getSelectionMax, Vector3i.CODEC).register("BlockCount", BlockSelection::getBlockCount, Codec.INTEGER).register("EntityCount", BlockSelection::getEntityCount, Codec.INTEGER);
      LOGGER = HytaleLogger.forEnclosingClass();
   }
}
