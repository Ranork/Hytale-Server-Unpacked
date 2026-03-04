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
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.FromPrefab;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.prefab.event.PrefabPlaceEntityEvent;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockMask;
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
   public static final Consumer<Ref<EntityStore>> DEFAULT_ENTITY_CONSUMER = ref -> {};
   public static final MetricsRegistry<BlockSelection> METRICS_REGISTRY = new MetricsRegistry<BlockSelection>()
      .register("BlocksLock", selection -> selection.blocksLock.toString(), Codec.STRING)
      .register("EntitiesLock", selection -> selection.entitiesLock.toString(), Codec.STRING)
      .register("Position", selection -> new Vector3i(selection.x, selection.y, selection.z), Vector3i.CODEC)
      .register("Anchor", selection -> new Vector3i(selection.anchorX, selection.anchorY, selection.anchorZ), Vector3i.CODEC)
      .register("Min", BlockSelection::getSelectionMin, Vector3i.CODEC)
      .register("Max", BlockSelection::getSelectionMax, Vector3i.CODEC)
      .register("BlockCount", BlockSelection::getBlockCount, Codec.INTEGER)
      .register("EntityCount", BlockSelection::getEntityCount, Codec.INTEGER);
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private int x;
   private int y;
   private int z;
   private int anchorX;
   private int anchorY;
   private int anchorZ;
   private int prefabId = -1;
   @Nonnull
   private Vector3i min = Vector3i.ZERO;
   @Nonnull
   private Vector3i max = Vector3i.ZERO;
   @Nonnull
   private final Long2ObjectMap<BlockSelection.BlockHolder> blocks;
   @Nonnull
   private final Long2ObjectMap<BlockSelection.FluidHolder> fluids;
   @Nonnull
   private final List<Holder<EntityStore>> entities;
   private final ReentrantReadWriteLock blocksLock = new ReentrantReadWriteLock();
   private final ReentrantReadWriteLock entitiesLock = new ReentrantReadWriteLock();

   public BlockSelection() {
      this.blocks = new Long2ObjectOpenHashMap();
      this.fluids = new Long2ObjectOpenHashMap();
      this.entities = new ObjectArrayList();
   }

   public BlockSelection(int initialBlockCapacity, int initialEntityCapacity) {
      this.blocks = new Long2ObjectOpenHashMap(initialBlockCapacity);
      this.fluids = new Long2ObjectOpenHashMap(initialBlockCapacity);
      this.entities = new ObjectArrayList(initialEntityCapacity);
   }

   public BlockSelection(@Nonnull BlockSelection other) {
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

   public boolean compare(@Nonnull BlockSelection.BlockComparingIterator iterator) {
      ObjectIterator var2 = this.blocks.long2ObjectEntrySet().iterator();

      while (var2.hasNext()) {
         Entry<BlockSelection.BlockHolder> entry = (Entry<BlockSelection.BlockHolder>)var2.next();
         long packed = entry.getLongKey();
         BlockSelection.BlockHolder value = (BlockSelection.BlockHolder)entry.getValue();
         int x1 = BlockUtil.unpackX(packed);
         int y1 = BlockUtil.unpackY(packed);
         int z1 = BlockUtil.unpackZ(packed);
         if (!iterator.test(x1, y1, z1, value)) {
            return false;
         }
      }

      return true;
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
         BlockSelection.BlockHolder blockHolder = (BlockSelection.BlockHolder)this.blocks.get(BlockUtil.pack(x, y, z));
         if (blockHolder != null) {
            return blockHolder.blockId();
         }

         var5 = Integer.MIN_VALUE;
      } finally {
         this.blocksLock.readLock().unlock();
      }

      return var5;
   }

   public BlockSelection.BlockHolder getBlockHolderAtWorldPos(int x, int y, int z) {
      return this.getBlockHolderAtLocalPos(x - this.x, y - this.y, z - this.z);
   }

   private BlockSelection.BlockHolder getBlockHolderAtLocalPos(int x, int y, int z) {
      this.blocksLock.readLock().lock();

      BlockSelection.BlockHolder var4;
      try {
         var4 = (BlockSelection.BlockHolder)this.blocks.get(BlockUtil.pack(x, y, z));
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
         BlockSelection.FluidHolder fluidStore = (BlockSelection.FluidHolder)this.fluids.get(BlockUtil.pack(x, y, z));
         if (fluidStore != null) {
            return fluidStore.fluidId();
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
         BlockSelection.FluidHolder fluidStore = (BlockSelection.FluidHolder)this.fluids.get(BlockUtil.pack(x, y, z));
         if (fluidStore != null) {
            return fluidStore.fluidLevel();
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

      byte var5;
      try {
         BlockSelection.BlockHolder blockHolder = (BlockSelection.BlockHolder)this.blocks.get(BlockUtil.pack(x, y, z));
         if (blockHolder != null) {
            return blockHolder.supportValue();
         }

         var5 = 0;
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

      Holder<ChunkStore> holder;
      try {
         BlockSelection.BlockHolder blockHolder = (BlockSelection.BlockHolder)this.blocks.get(BlockUtil.pack(x, y, z));
         if (blockHolder != null) {
            holder = blockHolder.holder();
            return holder != null ? holder.clone() : null;
         }

         holder = null;
      } finally {
         this.blocksLock.readLock().unlock();
      }

      return holder;
   }

   public void forEachBlock(@Nonnull BlockSelection.BlockIterator iterator) {
      this.blocksLock.readLock().lock();

      try {
         Long2ObjectMaps.fastForEach(this.blocks, e -> {
            long packed = e.getLongKey();
            BlockSelection.BlockHolder block = (BlockSelection.BlockHolder)e.getValue();
            int x1 = BlockUtil.unpackX(packed);
            int y1 = BlockUtil.unpackY(packed);
            int z1 = BlockUtil.unpackZ(packed);
            iterator.accept(x1, y1, z1, block);
         });
      } finally {
         this.blocksLock.readLock().unlock();
      }
   }

   public void forEachFluid(@Nonnull BlockSelection.FluidIterator iterator) {
      this.blocksLock.readLock().lock();

      try {
         Long2ObjectMaps.fastForEach(this.fluids, e -> {
            long packed = e.getLongKey();
            BlockSelection.FluidHolder block = (BlockSelection.FluidHolder)e.getValue();
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
      this.addBlockAtWorldPos(
         x,
         y,
         z,
         other.getBlock(x, y, z),
         other.getRotationIndex(x, y, z),
         other.getFiller(x, y, z),
         blockPhysics != null ? blockPhysics.get(x, y, z) : 0,
         other.getBlockComponentHolder(x, y, z)
      );
      this.addFluidAtWorldPos(x, y, z, other.getFluidId(x, y, z), other.getFluidLevel(x, y, z));
   }

   public void addEmptyAtWorldPos(int x, int y, int z) {
      this.addBlockAtWorldPos(x, y, z, 0, 0, 0, 0);
      this.addFluidAtWorldPos(x, y, z, 0, (byte)0);
   }

   public void addBlockAtWorldPos(int x, int y, int z, int block, int rotation, int filler, int supportValue) {
      this.addBlockAtWorldPos(x, y, z, block, rotation, filler, supportValue, null);
   }

   public void addBlockAtWorldPos(int x, int y, int z, int block, int rotation, int filler, int supportValue, Holder<ChunkStore> state) {
      this.addBlockAtLocalPos(x - this.x, y - this.y, z - this.z, block, rotation, filler, supportValue, state);
   }

   public void addBlockAtLocalPos(int x, int y, int z, int block, int rotation, int filler, int supportValue) {
      this.addBlockAtLocalPos(x, y, z, block, rotation, filler, supportValue, null);
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
      this.blocks.put(BlockUtil.pack(x, y, z), new BlockSelection.BlockHolder(block, rotation, filler, supportValue, state));
   }

   private void addBlock0(int x, int y, int z, @Nonnull BlockSelection.BlockHolder block) {
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
      this.fluids.put(BlockUtil.pack(x, y, z), new BlockSelection.FluidHolder(fluidId, fluidLevel));
   }

   private void addEntity0(Holder<EntityStore> holder) {
      this.entities.add(holder);
   }

   public void reserializeBlockStates(ChunkStore store, boolean destructive) {
      this.blocksLock.writeLock().lock();

      try {
         this.blocks
            .replaceAll(
               (k, b) -> {
                  Holder<ChunkStore> holder = b.holder();
                  if (holder == null && b.filler == 0) {
                     BlockType blockType = BlockType.getAssetMap().getAsset(b.blockId);
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

                        BlockState blockState = codec.decode(new BsonDocument());
                        if (blockState == null) {
                           return b;
                        }

                        blockState.setPosition(null, position);
                        holder = blockState.toHolder();
                     }
                  }

                  if (holder != null && b.filler != 0) {
                     return new BlockSelection.BlockHolder(b.blockId(), b.rotation(), b.filler(), b.supportValue(), null);
                  } else if (holder == null) {
                     return b;
                  } else {
                     try {
                        ComponentRegistry<ChunkStore> registry = ChunkStore.REGISTRY;
                        ComponentRegistry.Data<ChunkStore> data = registry.getData();
                        SystemType<ChunkStore, BlockModule.MigrationSystem> systemType = BlockModule.get().getMigrationSystemType();
                        BitSet systemIndexes = data.getSystemIndexesForType(systemType);
                        int systemIndex = -1;

                        while ((systemIndex = systemIndexes.nextSetBit(systemIndex + 1)) >= 0) {
                           BlockModule.MigrationSystem system = data.getSystem(systemIndex, systemType);
                           if (system.test(registry, holder.getArchetype())) {
                              system.onEntityAdd(holder, AddReason.LOAD, store.getStore());
                           }
                        }

                        systemIndex = -1;

                        while ((systemIndex = systemIndexes.nextSetBit(systemIndex + 1)) >= 0) {
                           BlockModule.MigrationSystem system = data.getSystem(systemIndex, systemType);
                           if (system.test(registry, holder.getArchetype())) {
                              system.onEntityRemoved(holder, RemoveReason.UNLOAD, store.getStore());
                           }
                        }

                        if (destructive) {
                           holder.tryRemoveComponent(registry.getUnknownComponentType());
                        }

                        return !holder.hasSerializableComponents(data)
                           ? new BlockSelection.BlockHolder(b.blockId(), b.rotation(), b.filler(), b.supportValue(), null)
                           : new BlockSelection.BlockHolder(b.blockId(), b.rotation(), b.filler(), b.supportValue(), holder.clone());
                     } catch (Throwable var11) {
                        throw new RuntimeException("Failed to read block state: " + b, var11);
                     }
                  }
               }
            );
      } finally {
         this.blocksLock.writeLock().unlock();
      }
   }

   public void clearAllSupportValues() {
      this.blocksLock.writeLock().lock();

      try {
         this.blocks.replaceAll((k, b) -> b.supportValue() == 0 ? b : new BlockSelection.BlockHolder(b.blockId(), b.rotation(), b.filler(), 0, b.holder()));
      } finally {
         this.blocksLock.writeLock().unlock();
      }
   }

   public void addEntityFromWorld(@Nonnull Holder<EntityStore> entityHolder) {
      TransformComponent transformComponent = entityHolder.getComponent(TransformComponent.getComponentType());

      assert transformComponent != null;

      transformComponent.getPosition().subtract(this.x, this.y, this.z);
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
            TransformComponent ta = a.getComponent(transformType);
            TransformComponent tb = b.getComponent(transformType);
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
      this.placeNoReturn(null, null, FeedbackConsumer.DEFAULT, world, position, null, componentAccessor);
   }

   public void placeNoReturn(String feedbackKey, CommandSender feedback, @Nonnull World outerWorld, ComponentAccessor<EntityStore> componentAccessor) {
      this.placeNoReturn(feedbackKey, feedback, FeedbackConsumer.DEFAULT, outerWorld, Vector3i.ZERO, null, componentAccessor);
   }

   public void placeNoReturn(
      String feedbackKey,
      CommandSender feedback,
      @Nonnull FeedbackConsumer feedbackConsumer,
      @Nonnull World outerWorld,
      ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.placeNoReturn(feedbackKey, feedback, feedbackConsumer, outerWorld, Vector3i.ZERO, null, componentAccessor);
   }

   public void placeNoReturn(
      @Nullable String feedbackKey,
      @Nullable CommandSender feedback,
      @Nonnull FeedbackConsumer feedbackConsumer,
      @Nonnull World outerWorld,
      @Nullable Vector3i position,
      @Nullable BlockMask blockMask,
      ComponentAccessor<EntityStore> componentAccessor
   ) {
      IntUnaryOperator xConvert;
      if (position != null && position.getX() != 0) {
         xConvert = localX -> localX + this.x + position.getX() - this.anchorX;
      } else {
         xConvert = localX -> localX + this.x - this.anchorX;
      }

      IntUnaryOperator yConvert;
      if (position != null && position.getY() != 0) {
         yConvert = localY -> localY + this.y + position.getY() - this.anchorY;
      } else {
         yConvert = localY -> localY + this.y - this.anchorY;
      }

      IntUnaryOperator zConvert;
      if (position != null && position.getZ() != 0) {
         zConvert = localZ -> localZ + this.z + position.getZ() - this.anchorZ;
      } else {
         zConvert = localZ -> localZ + this.z - this.anchorZ;
      }

      LongSet dirtyChunks = new LongOpenHashSet();
      this.blocksLock.readLock().lock();

      try {
         BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();
         int totalBlocks = this.blocks.size();
         AtomicInteger counter = new AtomicInteger();
         outerWorld.getBlockBulkRelative(
            this.blocks,
            xConvert,
            yConvert,
            zConvert,
            (world, blockHolder, chunkIndex, chunk, blockX, blockY, blockZ, localX, localY, localZ) -> {
               int newBlockId = blockHolder.blockId();
               Holder<ChunkStore> holder = blockHolder.holder();
               this.placeBlockNoReturn(
                  feedbackKey,
                  feedback,
                  feedbackConsumer,
                  outerWorld,
                  blockMask,
                  dirtyChunks,
                  assetMap,
                  totalBlocks,
                  counter.incrementAndGet(),
                  chunkIndex,
                  chunk,
                  blockX,
                  blockY,
                  blockZ,
                  newBlockId,
                  blockHolder.rotation(),
                  blockHolder.filler(),
                  holder != null ? holder.clone() : null,
                  componentAccessor
               );
            }
         );
         outerWorld.getBlockBulkRelative(
            this.fluids,
            xConvert,
            yConvert,
            zConvert,
            (world, fluidStore, chunkIndex, chunk, blockX, blockY, blockZ, localX, localY, localZ) -> this.placeFluidNoReturn(
               feedbackKey,
               feedback,
               feedbackConsumer,
               outerWorld,
               blockMask,
               dirtyChunks,
               assetMap,
               totalBlocks,
               counter.incrementAndGet(),
               chunkIndex,
               chunk,
               blockX,
               blockY,
               blockZ,
               fluidStore.fluidId,
               fluidStore.fluidLevel,
               componentAccessor
            )
         );
      } finally {
         this.blocksLock.readLock().unlock();
      }

      dirtyChunks.forEach(value -> outerWorld.getChunkLighting().invalidateLightInChunk(outerWorld.getChunkIfInMemory(value)));
      this.placeEntities(outerWorld, position);
      dirtyChunks.forEach(value -> outerWorld.getNotificationHandler().updateChunk(value));
   }

   private void placeBlockNoReturn(
      String feedbackKey,
      CommandSender feedback,
      @Nonnull FeedbackConsumer feedbackConsumer,
      @Nonnull World outerWorld,
      @Nullable BlockMask blockMask,
      @Nonnull LongSet dirtyChunks,
      @Nonnull BlockTypeAssetMap<String, BlockType> assetMap,
      int totalBlocks,
      int counter,
      long chunkIndex,
      @Nonnull WorldChunk chunk,
      int blockX,
      int blockY,
      int blockZ,
      int newBlockId,
      int newRotation,
      int newFiller,
      Holder<ChunkStore> holder,
      ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (blockY >= 0 && blockY < 320) {
         int oldBlockId = chunk.getBlock(blockX, blockY, blockZ);
         if (blockMask == null || !blockMask.isExcluded(outerWorld, blockX, blockY, blockZ, this.min, this.max, oldBlockId)) {
            BlockChunk blockChunk = chunk.getBlockChunk();
            if (blockChunk.setBlock(blockX, blockY, blockZ, newBlockId, newRotation, newFiller)) {
               BlockType newBlockType = assetMap.getAsset(newBlockId);
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

   private void placeFluidNoReturn(
      String feedbackKey,
      CommandSender feedback,
      @Nonnull FeedbackConsumer feedbackConsumer,
      @Nonnull World outerWorld,
      BlockMask blockMask,
      @Nonnull LongSet dirtyChunks,
      BlockTypeAssetMap<String, BlockType> assetMap,
      int totalBlocks,
      int counter,
      long chunkIndex,
      @Nonnull WorldChunk chunk,
      int blockX,
      int blockY,
      int blockZ,
      int newFluidId,
      byte newFluidLevel,
      ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (blockY >= 0 && blockY < 320) {
         int sectionY = ChunkUtil.chunkCoordinate(blockY);
         Store<ChunkStore> store = outerWorld.getChunkStore().getStore();
         ChunkColumn column = store.getComponent(chunk.getReference(), ChunkColumn.getComponentType());
         Ref<ChunkStore> section = column.getSection(sectionY);
         FluidSection fluidSection = store.ensureAndGetComponent(section, FluidSection.getComponentType());
         fluidSection.setFluid(blockX, blockY, blockZ, newFluidId, newFluidLevel);
         dirtyChunks.add(chunkIndex);
         feedbackConsumer.accept(feedbackKey, totalBlocks, counter, feedback, componentAccessor);
      }
   }

   private void clearFluidAtPosition(@Nonnull World world, @Nonnull WorldChunk chunk, int blockX, int blockY, int blockZ) {
      Ref<ChunkStore> ref = chunk.getReference();
      if (ref != null && ref.isValid()) {
         Store<ChunkStore> store = world.getChunkStore().getStore();
         ChunkColumn column = store.getComponent(ref, ChunkColumn.getComponentType());
         if (column != null) {
            Ref<ChunkStore> section = column.getSection(ChunkUtil.chunkCoordinate(blockY));
            if (section != null) {
               FluidSection fluidSection = store.getComponent(section, FluidSection.getComponentType());
               if (fluidSection != null) {
                  fluidSection.setFluid(blockX, blockY, blockZ, 0, (byte)0);
               }
            }
         }
      }
   }

   @Nonnull
   public BlockSelection place(CommandSender feedback, @Nonnull World outerWorld) {
      return this.place(feedback, outerWorld, Vector3i.ZERO, null);
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
   public BlockSelection place(
      CommandSender feedback,
      @Nonnull World outerWorld,
      @Nullable Vector3i position,
      @Nullable BlockMask blockMask,
      @Nonnull Consumer<Ref<EntityStore>> entityConsumer
   ) {
      BlockSelection before = new BlockSelection(this.getBlockCount(), 0);
      before.setAnchor(this.anchorX, this.anchorY, this.anchorZ);
      before.setPosition(this.x, this.y, this.z);
      IntUnaryOperator xConvert;
      if (position != null && position.getX() != 0) {
         xConvert = localX -> localX + this.x + position.getX() - this.anchorX;
      } else {
         xConvert = localX -> localX + this.x - this.anchorX;
      }

      IntUnaryOperator yConvert;
      if (position != null && position.getY() != 0) {
         yConvert = localY -> localY + this.y + position.getY() - this.anchorY;
      } else {
         yConvert = localY -> localY + this.y - this.anchorY;
      }

      IntUnaryOperator zConvert;
      if (position != null && position.getZ() != 0) {
         zConvert = localZ -> localZ + this.z + position.getZ() - this.anchorZ;
      } else {
         zConvert = localZ -> localZ + this.z - this.anchorZ;
      }

      LongSet dirtyChunks = new LongOpenHashSet();
      this.blocksLock.readLock().lock();

      try {
         BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();
         outerWorld.getBlockBulkRelative(
            this.blocks,
            xConvert,
            yConvert,
            zConvert,
            (world, blockHolder, chunkIndex, chunk, blockX, blockY, blockZ, localX, localY, localZ) -> {
               Holder<ChunkStore> holder = blockHolder.holder();
               this.placeBlock(
                  feedback,
                  outerWorld,
                  blockMask,
                  before,
                  dirtyChunks,
                  assetMap,
                  chunkIndex,
                  chunk,
                  blockX,
                  blockY,
                  blockZ,
                  localX,
                  localY,
                  localZ,
                  blockHolder.blockId(),
                  blockHolder.rotation(),
                  blockHolder.filler(),
                  holder != null ? holder.clone() : null,
                  blockHolder.supportValue()
               );
            }
         );
         IndexedLookupTableAssetMap<String, Fluid> fluidMap = Fluid.getAssetMap();
         outerWorld.getBlockBulkRelative(
            this.fluids,
            xConvert,
            yConvert,
            zConvert,
            (world, fluidStore, chunkIndex, chunk, blockX, blockY, blockZ, localX, localY, localZ) -> this.placeFluid(
               feedback,
               outerWorld,
               before,
               dirtyChunks,
               fluidMap,
               chunkIndex,
               chunk,
               blockX,
               blockY,
               blockZ,
               localX,
               localY,
               localZ,
               fluidStore.fluidId,
               fluidStore.fluidLevel
            )
         );
      } finally {
         this.blocksLock.readLock().unlock();
      }

      dirtyChunks.forEach(value -> outerWorld.getChunkLighting().invalidateLightInChunk(outerWorld.getChunkIfInMemory(value)));
      this.placeEntities(outerWorld, position, entityConsumer);
      dirtyChunks.forEach(value -> outerWorld.getNotificationHandler().updateChunk(value));
      return before;
   }

   private void placeBlock(
      CommandSender feedback,
      @Nonnull World outerWorld,
      @Nullable BlockMask blockMask,
      @Nonnull BlockSelection before,
      @Nonnull LongSet dirtyChunks,
      @Nonnull BlockTypeAssetMap<String, BlockType> assetMap,
      long chunkIndex,
      @Nonnull WorldChunk chunk,
      int blockX,
      int blockY,
      int blockZ,
      int localX,
      int localY,
      int localZ,
      int newBlockId,
      int newRotation,
      int newFiller,
      Holder<ChunkStore> holder,
      int newSupportValue
   ) {
      if (blockY >= 0 && blockY < 320) {
         Store<ChunkStore> chunkStore = chunk.getWorld().getChunkStore().getStore();
         ChunkColumn chunkColumn = chunkStore.getComponent(chunk.getReference(), ChunkColumn.getComponentType());
         Ref<ChunkStore> section = chunkColumn.getSection(ChunkUtil.chunkCoordinate(blockY));
         BlockSection blockSection = chunkStore.getComponent(section, BlockSection.getComponentType());
         int oldBlockId = chunk.getBlock(blockX, blockY, blockZ);
         if (blockMask == null || !blockMask.isExcluded(outerWorld, blockX, blockY, blockZ, this.min, this.max, oldBlockId)) {
            BlockPhysics blockPhysics = section != null ? chunkStore.getComponent(section, BlockPhysics.getComponentType()) : null;
            int supportValue = blockPhysics != null ? blockPhysics.get(blockX, blockY, blockZ) : 0;
            int filler = blockSection.getFiller(blockX, blockY, blockZ);
            int rotation = blockSection.getRotationIndex(blockX, blockY, blockZ);
            before.addBlockAtLocalPos(localX, localY, localZ, oldBlockId, rotation, filler, supportValue, chunk.getBlockComponentHolder(blockX, blockY, blockZ));
            BlockChunk blockChunk = chunk.getBlockChunk();
            if (blockChunk.setBlock(blockX, blockY, blockZ, newBlockId, newRotation, newFiller)) {
               BlockType newBlockType = assetMap.getAsset(newBlockId);
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
                        blockPhysics = chunkStore.ensureAndGetComponent(section, BlockPhysics.getComponentType());
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

   private void placeFluid(
      CommandSender feedback,
      @Nonnull World outerWorld,
      @Nonnull BlockSelection before,
      @Nonnull LongSet dirtyChunks,
      IndexedLookupTableAssetMap<String, Fluid> assetMap,
      long chunkIndex,
      @Nonnull WorldChunk chunk,
      int blockX,
      int blockY,
      int blockZ,
      int localX,
      int localY,
      int localZ,
      int newFluidId,
      byte newFluidLevel
   ) {
      if (blockY >= 0 && blockY < 320) {
         int sectionY = ChunkUtil.chunkCoordinate(blockY);
         Store<ChunkStore> store = outerWorld.getChunkStore().getStore();
         ChunkColumn column = store.getComponent(chunk.getReference(), ChunkColumn.getComponentType());
         Ref<ChunkStore> section = column.getSection(sectionY);
         FluidSection fluidSection = store.ensureAndGetComponent(section, FluidSection.getComponentType());
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
         for (Holder<EntityStore> entityHolder : this.entities) {
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
      TransformComponent transformComponent = entityHolder.getComponent(TransformComponent.getComponentType());

      assert transformComponent != null;

      transformComponent.getPosition().add(this.x + pos.getX() - this.anchorX, this.y + pos.getY() - this.anchorY, this.z + pos.getZ() - this.anchorZ);
      Store<EntityStore> store = world.getEntityStore().getStore();
      PrefabPlaceEntityEvent prefabPlaceEntityEvent = new PrefabPlaceEntityEvent(prefabId, entityHolder);
      store.invoke(prefabPlaceEntityEvent);
      entityHolder.addComponent(FromPrefab.getComponentType(), FromPrefab.INSTANCE);
      Ref<EntityStore> entityRef = new Ref<>(store);
      world.execute(() -> store.addEntity(entityHolder, entityRef, AddReason.LOAD));
      return entityRef;
   }

   @Nonnull
   public BlockSelection rotate(@Nonnull Axis axis, int angle) {
      BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();
      BlockSelection selection = new BlockSelection(this.getBlockCount(), this.getEntityCount());
      selection.copyPropertiesFrom(this);
      Vector3i mutable = new Vector3i(0, 0, 0);
      Rotation rotation = Rotation.ofDegrees(angle);
      this.forEachBlock(
         (x1, y1, z1, block) -> {
            mutable.assign(x1 - this.anchorX, y1 - this.anchorY, z1 - this.anchorZ);
            axis.rotate(mutable, angle);
            int blockId = block.blockId;
            Holder<ChunkStore> holder = block.holder;
            RotationTuple blockRotation = RotationTuple.get(block.rotation);

            RotationTuple rotatedRotation = switch (axis) {
               case X -> RotationTuple.of(blockRotation.yaw(), blockRotation.pitch().add(rotation), blockRotation.roll());
               case Y -> RotationTuple.of(blockRotation.yaw().add(rotation), blockRotation.pitch(), blockRotation.roll());
               case Z -> RotationTuple.of(blockRotation.yaw(), blockRotation.pitch(), blockRotation.roll().add(rotation));
            };
            if (rotatedRotation == null) {
               rotatedRotation = blockRotation;
            }

            int rotatedFiller = BlockRotationUtil.getRotatedFiller(block.filler, axis, rotation);
            selection.addBlock0(
               mutable.getX() + this.anchorX,
               mutable.getY() + this.anchorY,
               mutable.getZ() + this.anchorZ,
               blockId,
               rotatedRotation.index(),
               rotatedFiller,
               block.supportValue(),
               holder != null ? holder.clone() : null
            );
         }
      );
      this.forEachEntity(entityHolder -> {
         Holder<EntityStore> copy = entityHolder.clone();
         TransformComponent transformComponent = copy.getComponent(TransformComponent.getComponentType());

         assert transformComponent != null;

         Vector3d position = transformComponent.getPosition();
         HeadRotation headRotationComponent = copy.getComponent(HeadRotation.getComponentType());
         position.subtract(this.anchorX, this.anchorY, this.anchorZ).subtract(0.5, 0.0, 0.5);
         axis.rotate(position, angle);
         position.add(this.anchorX, this.anchorY, this.anchorZ).add(0.5, 0.0, 0.5);
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
      Vector3d mutable = new Vector3d(0.0, 0.0, 0.0);
      Rotation rotation = Rotation.ofDegrees(angle);
      Vector3f finalOriginOfRotation = originOfRotation.clone().subtract(this.x, this.y, this.z);
      this.forEachBlock(
         (x1, y1, z1, block) -> {
            mutable.assign(x1 - finalOriginOfRotation.x, y1 - finalOriginOfRotation.y, z1 - finalOriginOfRotation.z);
            axis.rotate(mutable, angle);
            int blockId = block.blockId;
            Holder<ChunkStore> holder = block.holder;
            int supportValue = block.supportValue();
            RotationTuple blockRotation = RotationTuple.get(block.rotation);

            RotationTuple rotatedRotation = switch (axis) {
               case X -> RotationTuple.of(blockRotation.yaw(), blockRotation.pitch().add(rotation), blockRotation.roll());
               case Y -> RotationTuple.of(blockRotation.yaw().add(rotation), blockRotation.pitch(), blockRotation.roll());
               case Z -> RotationTuple.of(blockRotation.yaw(), blockRotation.pitch(), blockRotation.roll().add(rotation));
            };
            if (rotatedRotation == null) {
               rotatedRotation = blockRotation;
            }

            int rotatedFiller = BlockRotationUtil.getRotatedFiller(block.filler, axis, rotation);
            selection.addBlock0(
               (int)(mutable.getX() + finalOriginOfRotation.x),
               (int)(mutable.getY() + finalOriginOfRotation.z),
               (int)(mutable.getZ() + finalOriginOfRotation.z),
               blockId,
               rotatedRotation.index(),
               rotatedFiller,
               supportValue,
               holder != null ? holder.clone() : null
            );
         }
      );
      this.forEachEntity(entityHolder -> {
         Holder<EntityStore> copy = entityHolder.clone();
         TransformComponent transformComponent = copy.getComponent(TransformComponent.getComponentType());

         assert transformComponent != null;

         Vector3d position = transformComponent.getPosition();
         HeadRotation headRotationComponent = copy.getComponent(HeadRotation.getComponentType());
         position.subtract(this.anchorX, this.anchorY, this.anchorZ).subtract(0.5, 0.0, 0.5);
         axis.rotate(position, angle);
         position.add(this.anchorX, this.anchorY, this.anchorZ).add(0.5, 0.0, 0.5);
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
      double pitchRad = Math.toRadians(pitchDegrees);
      double yawRad = Math.toRadians(yawDegrees);
      double rollRad = Math.toRadians(rollDegrees);
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

      try {
         ObjectIterator corners = this.blocks.long2ObjectEntrySet().iterator();

         while (corners.hasNext()) {
            Entry<BlockSelection.BlockHolder> entry = (Entry<BlockSelection.BlockHolder>)corners.next();
            long packed = entry.getLongKey();
            int bx = BlockUtil.unpackX(packed) - this.anchorX;
            int by = BlockUtil.unpackY(packed) - this.anchorY;
            int bz = BlockUtil.unpackZ(packed) - this.anchorZ;
            srcMinX = Math.min(srcMinX, bx);
            srcMinY = Math.min(srcMinY, by);
            srcMinZ = Math.min(srcMinZ, bz);
            srcMaxX = Math.max(srcMaxX, bx);
            srcMaxY = Math.max(srcMaxY, by);
            srcMaxZ = Math.max(srcMaxZ, bz);
         }
      } finally {
         this.blocksLock.readLock().unlock();
      }

      if (srcMinX == Integer.MAX_VALUE) {
         BlockSelection selection = new BlockSelection(0, this.getEntityCount());
         selection.copyPropertiesFrom(this);
         return selection;
      } else {
         int[][] corners = new int[][]{
            {srcMinX, srcMinY, srcMinZ},
            {srcMaxX, srcMinY, srcMinZ},
            {srcMinX, srcMaxY, srcMinZ},
            {srcMaxX, srcMaxY, srcMinZ},
            {srcMinX, srcMinY, srcMaxZ},
            {srcMaxX, srcMinY, srcMaxZ},
            {srcMinX, srcMaxY, srcMaxZ},
            {srcMaxX, srcMaxY, srcMaxZ}
         };

         for (int[] corner : corners) {
            tempVec.assign(corner[0], corner[1], corner[2]);
            rotation.multiplyDirection(tempVec);
            int rx = MathUtil.floor(tempVec.x);
            int ry = MathUtil.floor(tempVec.y);
            int rz = MathUtil.floor(tempVec.z);
            destMinX = Math.min(destMinX, rx);
            destMinY = Math.min(destMinY, ry);
            destMinZ = Math.min(destMinZ, rz);
            destMaxX = Math.max(destMaxX, rx + 1);
            destMaxY = Math.max(destMaxY, ry + 1);
            destMaxZ = Math.max(destMaxZ, rz + 1);
         }

         BlockSelection selection = new BlockSelection(this.getBlockCount(), this.getEntityCount());
         selection.copyPropertiesFrom(this);
         Rotation snappedYaw = Rotation.ofDegrees(Math.round(yawDegrees / 90.0F) * 90);
         Rotation snappedPitch = Rotation.ofDegrees(Math.round(pitchDegrees / 90.0F) * 90);
         Rotation snappedRoll = Rotation.ofDegrees(Math.round(rollDegrees / 90.0F) * 90);
         this.blocksLock.readLock().lock();

         try {
            for (int dx = destMinX; dx <= destMaxX; dx++) {
               for (int dy = destMinY; dy <= destMaxY; dy++) {
                  for (int dz = destMinZ; dz <= destMaxZ; dz++) {
                     tempVec.assign(dx, dy, dz);
                     inverse.multiplyDirection(tempVec);
                     int sx = (int)Math.round(tempVec.x);
                     int sy = (int)Math.round(tempVec.y);
                     int sz = (int)Math.round(tempVec.z);
                     long packedSource = BlockUtil.pack(sx + this.anchorX, sy + this.anchorY, sz + this.anchorZ);
                     BlockSelection.BlockHolder block = (BlockSelection.BlockHolder)this.blocks.get(packedSource);
                     if (block != null) {
                        RotationTuple blockRotation = RotationTuple.get(block.rotation());
                        RotationTuple rotatedRotation = RotationTuple.of(
                           blockRotation.yaw().add(snappedYaw), blockRotation.pitch().add(snappedPitch), blockRotation.roll().add(snappedRoll)
                        );
                        if (rotatedRotation == null) {
                           rotatedRotation = blockRotation;
                        }

                        int rotatedFiller = block.filler();
                        if (rotatedFiller != 0) {
                           int fillerX = FillerBlockUtil.unpackX(rotatedFiller);
                           int fillerY = FillerBlockUtil.unpackY(rotatedFiller);
                           int fillerZ = FillerBlockUtil.unpackZ(rotatedFiller);
                           tempVec.assign(fillerX, fillerY, fillerZ);
                           rotation.multiplyDirection(tempVec);
                           rotatedFiller = FillerBlockUtil.pack((int)Math.round(tempVec.x), (int)Math.round(tempVec.y), (int)Math.round(tempVec.z));
                        }

                        Holder<ChunkStore> holder = block.holder();
                        selection.addBlock0(
                           dx + this.anchorX,
                           dy + this.anchorY,
                           dz + this.anchorZ,
                           block.blockId(),
                           rotatedRotation.index(),
                           rotatedFiller,
                           block.supportValue(),
                           holder != null ? holder.clone() : null
                        );
                     }
                  }
               }
            }

            for (int dx = destMinX; dx <= destMaxX; dx++) {
               for (int dy = destMinY; dy <= destMaxY; dy++) {
                  for (int dzx = destMinZ; dzx <= destMaxZ; dzx++) {
                     tempVec.assign(dx, dy, dzx);
                     inverse.multiplyDirection(tempVec);
                     int sx = (int)Math.round(tempVec.x);
                     int sy = (int)Math.round(tempVec.y);
                     int sz = (int)Math.round(tempVec.z);
                     long packedSource = BlockUtil.pack(sx + this.anchorX, sy + this.anchorY, sz + this.anchorZ);
                     BlockSelection.FluidHolder fluid = (BlockSelection.FluidHolder)this.fluids.get(packedSource);
                     if (fluid != null) {
                        selection.addFluid0(dx + this.anchorX, dy + this.anchorY, dzx + this.anchorZ, fluid.fluidId(), fluid.fluidLevel());
                     }
                  }
               }
            }
         } finally {
            this.blocksLock.readLock().unlock();
         }

         float var64 = (float)yawRad;
         float var68 = (float)pitchRad;
         float var71 = (float)rollRad;
         this.forEachEntity(entityHolder -> {
            Holder<EntityStore> copy = entityHolder.clone();
            TransformComponent transformComponent = copy.getComponent(TransformComponent.getComponentType());

            assert transformComponent != null;

            Vector3d position = transformComponent.getPosition();
            HeadRotation headRotationComp = copy.getComponent(HeadRotation.getComponentType());
            position.subtract(this.anchorX, this.anchorY, this.anchorZ).subtract(0.5, 0.0, 0.5);
            rotation.multiplyDirection(position);
            position.add(this.anchorX, this.anchorY, this.anchorZ).add(0.5, 0.0, 0.5);
            Vector3f bodyRotation = transformComponent.getRotation();
            bodyRotation.addPitch(var68);
            bodyRotation.addYaw(var64);
            bodyRotation.addRoll(var71);
            if (headRotationComp != null) {
               Vector3f headRot = headRotationComp.getRotation();
               headRot.addPitch(var68);
               headRot.addYaw(var64);
               headRot.addRoll(var71);
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
      this.forEachBlock(
         (x1, y1, z1, block) -> {
            mutable.assign(x1 - this.anchorX, y1 - this.anchorY, z1 - this.anchorZ);
            axis.flip(mutable);
            int blockId = block.blockId;
            Holder<ChunkStore> holder = block.holder;
            int supportValue = block.supportValue();
            int filler = block.filler;
            BlockType blockType = assetMap.getAsset(blockId);
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
               selection.addBlock0(
                  mutable.getX() + this.anchorX,
                  mutable.getY() + this.anchorY,
                  mutable.getZ() + this.anchorZ,
                  blockId,
                  rotatedRotation.index(),
                  rotatedFiller,
                  supportValue,
                  holder != null ? holder.clone() : null
               );
            }
         }
      );
      this.forEachEntity(entityHolder -> {
         Holder<EntityStore> copy = entityHolder.clone();
         HeadRotation headRotationComponent = copy.getComponent(HeadRotation.getComponentType());

         assert headRotationComponent != null;

         Vector3f headRotation = headRotationComponent.getRotation();
         TransformComponent transformComponent = copy.getComponent(TransformComponent.getComponentType());

         assert transformComponent != null;

         Vector3d position = transformComponent.getPosition();
         Vector3f bodyRotation = transformComponent.getRotation();
         position.subtract(this.anchorX, this.anchorY, this.anchorZ).subtract(0.5, 0.0, 0.5);
         axis.flip(position);
         position.add(this.anchorX, this.anchorY, this.anchorZ).add(0.5, 0.0, 0.5);
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
         this.forEachBlock((x, y, z, block) -> selection.addBlock0(x - originX, y - originY, z - originZ, block));
         this.forEachEntity(holder -> {
            Holder<EntityStore> copy = holder.clone();
            TransformComponent transformComponent = copy.getComponent(TransformComponent.getComponentType());

            assert transformComponent != null;

            transformComponent.getPosition().subtract(originX, originY, originZ);
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
         Long2ObjectMaps.fastForEach(
            this.blocks, entry -> selection.blocks.put(entry.getLongKey(), ((BlockSelection.BlockHolder)entry.getValue()).cloneBlockHolder())
         );
         selection.fluids.putAll(this.fluids);
      } finally {
         this.blocksLock.readLock().unlock();
      }

      this.entitiesLock.readLock().lock();

      try {
         this.entities.forEach(holder -> selection.entities.add(holder.clone()));
      } finally {
         this.entitiesLock.readLock().unlock();
      }

      return selection;
   }

   public void add(@Nonnull BlockSelection other) {
      this.entitiesLock.writeLock().lock();

      try {
         other.forEachEntity(holder -> {
            Holder<EntityStore> copy = holder.clone();
            TransformComponent transformComponent = copy.getComponent(TransformComponent.getComponentType());

            assert transformComponent != null;

            transformComponent.getPosition().add(other.x, other.y, other.z).subtract(this.x, this.y, this.z);
            this.addEntity0(copy);
         });
      } finally {
         this.entitiesLock.writeLock().unlock();
      }

      this.blocksLock.writeLock().lock();

      try {
         other.forEachBlock((x1, y1, z1, block) -> this.addBlock0(x1 + other.x - this.x, y1 + other.y - this.y, z1 + other.z - this.z, block));
         other.forEachFluid(
            (x1, y1, z1, fluidId, fluidLevel) -> this.addFluid0(x1 + other.x - this.x, y1 + other.y - this.y, z1 + other.z - this.z, fluidId, fluidLevel)
         );
      } finally {
         this.blocksLock.writeLock().unlock();
      }
   }

   @Nonnull
   @Override
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
         packet.blocksChange = blockList.toArray(BlockChange[]::new);
         packet.fluidsChange = fluidList.toArray(FluidChange[]::new);
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

      while (it.hasNext()) {
         long packed = it.nextLong();
         int x = BlockUtil.unpackX(packed);
         int y = BlockUtil.unpackY(packed);
         int z = BlockUtil.unpackZ(packed);
         BlockSelection.BlockHolder blockHolder = this.getBlockHolderAtLocalPos(x, y, z);
         if (blockHolder != null) {
            int blockId = blockHolder.blockId;
            if (blockId != 0) {
               BlockType blockType = (BlockType)blockTypeAssetMap.getAsset(blockId);
               if (blockType != null) {
                  String id = blockType.getId();
                  if (blockHolder.filler != 0) {
                     int fillerX = FillerBlockUtil.unpackX(blockHolder.filler);
                     int fillerY = FillerBlockUtil.unpackY(blockHolder.filler);
                     int fillerZ = FillerBlockUtil.unpackZ(blockHolder.filler);
                     BlockSelection.BlockHolder baseBlockHolder = this.getBlockHolderAtLocalPos(x - fillerX, y - fillerY, z - fillerZ);
                     BlockType baseBlock = (BlockType)blockTypeAssetMap.getAsset(baseBlockHolder.blockId);
                     if (baseBlock == null) {
                        this.addBlockAtLocalPos(x, y, z, 0, 0, 0, 0);
                     } else {
                        String baseId = baseBlock.getId();
                        BlockBoundingBoxes hitbox = (BlockBoundingBoxes)hitboxAssetMap.getAsset(baseBlock.getHitboxTypeIndex());
                        if (hitbox != null
                           && (
                              !id.equals(baseId)
                                 || baseBlockHolder.rotation != blockHolder.rotation
                                 || !hitbox.get(blockHolder.rotation).getBoundingBox().containsBlock(fillerX, fillerY, fillerZ)
                           )) {
                           this.addBlockAtLocalPos(x, y, z, 0, 0, 0, 0);
                        }
                     }
                  } else {
                     BlockBoundingBoxes hitbox = (BlockBoundingBoxes)hitboxAssetMap.getAsset(blockType.getHitboxTypeIndex());
                     if (hitbox != null && hitbox.protrudesUnitBox()) {
                        FillerBlockUtil.forEachFillerBlock(
                           hitbox.get(blockHolder.rotation),
                           (x1, y1, z1) -> {
                              if (x1 != 0 || y1 != 0 || z1 != 0) {
                                 int worldX = x + x1;
                                 int worldY = y + y1;
                                 int worldZ = z + z1;
                                 BlockSelection.BlockHolder fillerBlockHolder = this.getBlockHolderAtLocalPos(worldX, worldY, worldZ);
                                 BlockType fillerBlock = (BlockType)blockTypeAssetMap.getAsset(fillerBlockHolder.blockId);
                                 int filler = FillerBlockUtil.pack(x1, y1, z1);
                                 if (fillerBlock == null || !fillerBlock.getId().equals(id) || filler != fillerBlockHolder.filler) {
                                    if (!allowDestructive && fillerBlockHolder.blockId != 0) {
                                       throw new IllegalArgumentException(
                                          "Cannot replace "
                                             + fillerBlock.getId()
                                             + " with "
                                             + blockType.getId()
                                             + " in order to repair filler\n at "
                                             + worldX
                                             + ", "
                                             + worldY
                                             + ", "
                                             + worldZ
                                             + "\n base "
                                             + x
                                             + ", "
                                             + y
                                             + ", "
                                             + z
                                       );
                                    }

                                    this.addBlockAtLocalPos(worldX, worldY, worldZ, blockId, blockHolder.rotation, filler, 0);
                                 }
                              }
                           }
                        );
                     }
                  }
               }
            }
         }
      }
   }

   public void reserializeEntities(@Nonnull Store<EntityStore> store, boolean destructive) throws IOException {
      this.entitiesLock.writeLock().lock();

      try {
         if (this.entities.isEmpty()) {
            return;
         }

         ComponentRegistry<EntityStore> registry = EntityStore.REGISTRY;
         ComponentRegistry.Data<EntityStore> data = registry.getData();
         SystemType<EntityStore, EntityModule.MigrationSystem> systemType = EntityModule.get().getMigrationSystemType();
         BitSet systemIndexes = data.getSystemIndexesForType(systemType);
         int systemIndex = -1;

         while ((systemIndex = systemIndexes.nextSetBit(systemIndex + 1)) >= 0) {
            EntityModule.MigrationSystem system = data.getSystem(systemIndex, systemType);

            for (int i = 0; i < this.entities.size(); i++) {
               Holder<EntityStore> holder = this.entities.get(i);
               if (system.test(registry, holder.getArchetype())) {
                  system.onEntityAdd(holder, AddReason.LOAD, store);
               }
            }
         }

         systemIndex = -1;

         while ((systemIndex = systemIndexes.nextSetBit(systemIndex + 1)) >= 0) {
            EntityModule.MigrationSystem system = data.getSystem(systemIndex, systemType);

            for (int ix = 0; ix < this.entities.size(); ix++) {
               Holder<EntityStore> holder = this.entities.get(ix);
               if (system.test(registry, holder.getArchetype())) {
                  system.onEntityRemoved(holder, RemoveReason.UNLOAD, store);
               }
            }
         }

         if (destructive) {
            for (int ixx = 0; ixx < this.entities.size(); ixx++) {
               Holder<EntityStore> holder = this.entities.get(ixx);
               holder.tryRemoveComponent(registry.getUnknownComponentType());
            }
         }
      } finally {
         this.entitiesLock.writeLock().unlock();
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "BlockSelection{blocksLock="
         + this.blocksLock
         + ", x="
         + this.x
         + ", y="
         + this.y
         + ", z="
         + this.z
         + ", originX="
         + this.anchorX
         + ", originY="
         + this.anchorY
         + ", originZ="
         + this.anchorZ
         + ", min="
         + this.min
         + ", max="
         + this.max
         + "}";
   }

   @FunctionalInterface
   public interface BlockComparingIterator {
      boolean test(int var1, int var2, int var3, BlockSelection.BlockHolder var4);
   }

   public record BlockHolder(int blockId, int rotation, int filler, int supportValue, Holder<ChunkStore> holder) {
      @Nonnull
      public BlockSelection.BlockHolder cloneBlockHolder() {
         return this.holder == null ? this : new BlockSelection.BlockHolder(this.blockId, this.rotation, this.filler, this.supportValue, this.holder.clone());
      }
   }

   @FunctionalInterface
   public interface BlockIterator {
      void accept(int var1, int var2, int var3, BlockSelection.BlockHolder var4);
   }

   public static enum FallbackMode {
      PASS_THOUGH,
      COPY;
   }

   public record FluidHolder(int fluidId, byte fluidLevel) {
   }

   @FunctionalInterface
   public interface FluidIterator {
      void accept(int var1, int var2, int var3, int var4, byte var5);
   }
}
