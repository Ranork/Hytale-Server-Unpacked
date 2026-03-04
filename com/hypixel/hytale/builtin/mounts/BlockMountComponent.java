package com.hypixel.hytale.builtin.mounts;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockMountType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.mountpoints.BlockMountPoint;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockMountComponent implements Component<ChunkStore> {
   private BlockMountType type;
   private Vector3i blockPos;
   private BlockType expectedBlockType;
   private int expectedRotation;
   @Nonnull
   private Map<BlockMountPoint, Ref<EntityStore>> entitiesByMountPoint = new Object2ObjectOpenHashMap();
   @Nonnull
   private Map<Ref<EntityStore>, BlockMountPoint> mountPointByEntity = new Object2ObjectOpenHashMap();

   public static ComponentType<ChunkStore, BlockMountComponent> getComponentType() {
      return MountPlugin.getInstance().getBlockMountComponentType();
   }

   public BlockMountComponent() {
   }

   public BlockMountComponent(BlockMountType type, Vector3i blockPos, BlockType expectedBlockType, int expectedRotation) {
      this.type = type;
      this.blockPos = blockPos;
      this.expectedBlockType = expectedBlockType;
      this.expectedRotation = expectedRotation;
   }

   public BlockMountType getType() {
      return this.type;
   }

   public Vector3i getBlockPos() {
      return this.blockPos;
   }

   public BlockType getExpectedBlockType() {
      return this.expectedBlockType;
   }

   public int getExpectedRotation() {
      return this.expectedRotation;
   }

   public boolean isDead() {
      this.clean();
      return this.entitiesByMountPoint.isEmpty();
   }

   private void clean() {
      this.entitiesByMountPoint.values().removeIf(ref -> !ref.isValid());
      this.mountPointByEntity.keySet().removeIf(ref -> !ref.isValid());
   }

   public void putSeatedEntity(@Nonnull BlockMountPoint mountPoint, @Nonnull Ref<EntityStore> seatedEntity) {
      this.entitiesByMountPoint.put(mountPoint, seatedEntity);
      this.mountPointByEntity.put(seatedEntity, mountPoint);
   }

   public void removeSeatedEntity(@Nonnull Ref<EntityStore> seatedEntity) {
      BlockMountPoint seat = this.mountPointByEntity.remove(seatedEntity);
      if (seat != null) {
         this.entitiesByMountPoint.remove(seat);
      }
   }

   @Nullable
   public BlockMountPoint getSeatBlockBySeatedEntity(Ref<EntityStore> seatedEntity) {
      return this.mountPointByEntity.get(seatedEntity);
   }

   @Nonnull
   public Collection<? extends Ref<EntityStore>> getSeatedEntities() {
      return this.entitiesByMountPoint.values();
   }

   @Nullable
   public BlockMountPoint findAvailableSeat(@Nonnull Vector3i targetBlock, @Nonnull BlockMountPoint[] choices, @Nonnull Vector3f whereWasClicked) {
      this.clean();
      double minDistSq = Double.MAX_VALUE;
      BlockMountPoint closestSeat = null;

      for (BlockMountPoint choice : choices) {
         if (!this.entitiesByMountPoint.containsKey(choice)) {
            Vector3f seatInWorldSpace = choice.computeWorldSpacePosition(targetBlock);
            double distSq = whereWasClicked.distanceSquaredTo(seatInWorldSpace);
            if (distSq < minDistSq) {
               minDistSq = distSq;
               closestSeat = choice;
            }
         }
      }

      return closestSeat;
   }

   @Nonnull
   @Override
   public Component<ChunkStore> clone() {
      BlockMountComponent seat = new BlockMountComponent();
      seat.type = this.type;
      seat.blockPos = this.blockPos;
      seat.expectedBlockType = this.expectedBlockType;
      seat.entitiesByMountPoint = new Object2ObjectOpenHashMap(this.entitiesByMountPoint);
      seat.mountPointByEntity = new Object2ObjectOpenHashMap(this.mountPointByEntity);
      return seat;
   }
}
