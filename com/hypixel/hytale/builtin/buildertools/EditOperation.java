package com.hypixel.hytale.builtin.buildertools;

import com.hypixel.hytale.builtin.buildertools.snapshot.EntityTransformSnapshot;
import com.hypixel.hytale.builtin.buildertools.utils.Material;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockMask;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.accessor.BlockAccessor;
import com.hypixel.hytale.server.core.universe.world.accessor.LocalCachedChunkAccessor;
import com.hypixel.hytale.server.core.universe.world.accessor.OverridableChunkAccessor;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class EditOperation {
   private final BlockMask blockMask;
   @Nonnull
   private final OverridableChunkAccessor accessor;
   @Nonnull
   private final BlockSelection before;
   @Nonnull
   private final BlockSelection after;
   @Nonnull
   private final World world;
   private final Vector3i min;
   private final Vector3i max;
   private final List<Ref<EntityStore>> spawnedEntityRefs = new ReferenceArrayList();
   private final List<EntityTransformSnapshot> movedEntitySnapshots = new ArrayList<>();

   public EditOperation(@Nonnull World world, int x, int y, int z, int editRange, Vector3i min, Vector3i max, BlockMask blockMask) {
      this.blockMask = blockMask;
      this.accessor = LocalCachedChunkAccessor.atWorldCoords(world, x, z, editRange);
      this.world = world;
      this.min = min;
      this.max = max;
      this.before = new BlockSelection();
      this.before.setPosition(x, y, z);
      if (min != null && max != null) {
         this.before.setSelectionArea(min, max);
      }

      this.after = new BlockSelection(this.before);
   }

   public BlockMask getBlockMask() {
      return this.blockMask;
   }

   @Nonnull
   public BlockSelection getBefore() {
      return this.before;
   }

   @Nonnull
   public BlockSelection getAfter() {
      return this.after;
   }

   @Nonnull
   public OverridableChunkAccessor getAccessor() {
      return this.accessor;
   }

   public int getBlock(int x, int y, int z) {
      return this.accessor.getBlock(x, y, z);
   }

   public boolean setBlock(int x, int y, int z, int blockId) {
      return this.setBlock(x, y, z, blockId, 0);
   }

   public boolean setBlock(int x, int y, int z, int blockId, int rotation) {
      int currentBlock = this.getBlock(x, y, z);
      int currentFluid = this.getFluid(x, y, z);
      if (this.blockMask != null && this.blockMask.isExcluded(this.accessor, x, y, z, this.min, this.max, currentBlock, currentFluid)) {
         return false;
      } else {
         BlockAccessor blocks = this.accessor.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(x, z));
         if (blocks == null) {
            return false;
         } else {
            if (!this.before.hasBlockAtWorldPos(x, y, z)) {
               this.before
                  .addBlockAtWorldPos(
                     x,
                     y,
                     z,
                     currentBlock,
                     blocks.getRotationIndex(x, y, z),
                     blocks.getFiller(x, y, z),
                     blocks.getSupportValue(x, y, z),
                     blocks.getBlockComponentHolder(x, y, z)
                  );
            }

            this.after.addBlockAtWorldPos(x, y, z, blockId, rotation, 0, 0);
            if (blockId == 0) {
               this.setFluid(x, y, z, 0, (byte)0);
            }

            return true;
         }
      }
   }

   private boolean setFluid(int x, int y, int z, int fluidId, byte fluidLevel) {
      BlockAccessor chunk = this.accessor.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(x, z));
      if (chunk == null) {
         return false;
      } else {
         int currentBlock = this.getBlock(x, y, z);
         int currentFluid = this.getFluid(x, y, z);
         if (this.blockMask != null && this.blockMask.isExcluded(this.accessor, x, y, z, this.min, this.max, currentBlock, currentFluid)) {
            return false;
         } else {
            int beforeFluid = this.before.getFluidAtWorldPos(x, y, z);
            if (beforeFluid < 0) {
               int originalFluidId = chunk.getFluidId(x, y, z);
               byte originalFluidLevel = chunk.getFluidLevel(x, y, z);
               this.before.addFluidAtWorldPos(x, y, z, originalFluidId, originalFluidLevel);
            }

            this.after.addFluidAtWorldPos(x, y, z, fluidId, fluidLevel);
            return true;
         }
      }
   }

   public int getFluid(int x, int y, int z) {
      BlockAccessor chunk = this.accessor.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(x, z));
      return chunk != null ? chunk.getFluidId(x, y, z) : 0;
   }

   public boolean setMaterial(int x, int y, int z, @Nonnull Material material) {
      return material.isFluid()
         ? this.setFluid(x, y, z, material.getFluidId(), material.getFluidLevel())
         : this.setBlock(x, y, z, material.getBlockId(), material.getRotation());
   }

   public boolean setTint(int x, int z, int color, double opacity) {
      if (!this.before.hasTintAtWorldPos(x, z)) {
         long chunkIdx = ChunkUtil.indexChunkFromBlock(x, z);
         WorldChunk chunk = this.world.getNonTickingChunk(chunkIdx);
         int beforeColor = chunk.getBlockChunk().getTint(x, z);
         int r = (int)MathUtil.lerp((double)(beforeColor >> 16 & 0xFF), (double)(color >> 16 & 0xFF), 1.0 - opacity);
         int g = (int)MathUtil.lerp((double)(beforeColor >> 8 & 0xFF), (double)(color >> 8 & 0xFF), 1.0 - opacity);
         int b = (int)MathUtil.lerp((double)(beforeColor & 0xFF), (double)(color & 0xFF), 1.0 - opacity);
         int merged = r << 16 | g << 8 | b;
         this.before.addTintAtWorldPos(x, z, beforeColor);
         this.after.addTintAtWorldPos(x, z, merged);
         return true;
      } else {
         return false;
      }
   }

   public int getTint(int x, int z) {
      long chunkIdx = ChunkUtil.indexChunkFromBlock(x, z);
      WorldChunk chunk = this.world.getNonTickingChunk(chunkIdx);
      if (chunk == null) {
         return 0;
      } else {
         return chunk.getBlockChunk() == null ? 0 : chunk.getBlockChunk().getTint(x, z);
      }
   }

   public void removeEntity(Ref<EntityStore> entityRef, Holder<EntityStore> entityStoreHolder) {
      this.before.addEntityFromWorld(entityStoreHolder.clone());
      Store<EntityStore> entityStore = this.world.getEntityStore().getStore();
      this.world.execute(() -> entityStore.removeEntity(entityRef, RemoveReason.UNLOAD));
   }

   public void trackSpawnedEntity(Ref<EntityStore> ref) {
      this.spawnedEntityRefs.add(ref);
   }

   public List<Ref<EntityStore>> getSpawnedEntityRefs() {
      return this.spawnedEntityRefs;
   }

   public void trackMovedEntity(Ref<EntityStore> entityRef, ComponentAccessor<EntityStore> componentAccessor) {
      this.movedEntitySnapshots.add(new EntityTransformSnapshot(entityRef, componentAccessor));
   }

   public List<EntityTransformSnapshot> getMovedEntitySnapshots() {
      return this.movedEntitySnapshots;
   }
}
