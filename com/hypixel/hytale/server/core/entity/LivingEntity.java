package com.hypixel.hytale.server.core.entity;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.modules.collision.WorldUtil;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public abstract class LivingEntity extends Entity {
   @Nonnull
   public static final BuilderCodec<LivingEntity> CODEC = BuilderCodec.abstractBuilder(LivingEntity.class, Entity.CODEC)
      .append(
         new KeyedCodec<>("Inventory", Inventory.CODEC),
         (livingEntity, inventory, extraInfo) -> livingEntity.setInventory(inventory),
         (livingEntity, extraInfo) -> livingEntity.inventory
      )
      .add()
      .afterDecode(livingEntity -> {
         if (livingEntity.inventory == null) {
            livingEntity.setInventory(new Inventory());
         }
      })
      .build();
   public static final int DEFAULT_ITEM_THROW_SPEED = 6;
   private Inventory inventory;
   protected double currentFallDistance;

   public LivingEntity() {
      this.setInventory(new Inventory());
   }

   public LivingEntity(@Nonnull World world) {
      super(world);
      this.setInventory(new Inventory());
   }

   public static long getPackedMaterialAndFluidAtBreathingHeight(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      World world = componentAccessor.getExternalData().getWorld();
      Transform lookVec = TargetUtil.getLook(ref, componentAccessor);
      Vector3d position = lookVec.getPosition();
      ChunkStore chunkStore = world.getChunkStore();
      long chunkIndex = ChunkUtil.indexChunkFromBlock(position.x, position.z);
      Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(chunkIndex);
      return chunkRef != null && chunkRef.isValid()
         ? WorldUtil.getPackedMaterialAndFluidAtPosition(chunkRef, chunkStore.getStore(), position.x, position.y, position.z)
         : MathUtil.packLong(BlockMaterial.Empty.ordinal(), 0);
   }

   public Inventory getInventory() {
      return this.inventory;
   }

   @Nonnull
   private Inventory setInventory(Inventory inventory) {
      if (this.inventory != null) {
         this.inventory.unregister();
      }

      inventory.setEntity(this);
      this.inventory = inventory;
      return inventory;
   }

   @Override
   public void moveTo(@Nonnull Ref<EntityStore> ref, double locX, double locY, double locZ, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      MovementStatesComponent movementStatesComponent = componentAccessor.getComponent(ref, MovementStatesComponent.getComponentType());

      assert movementStatesComponent != null;

      MovementStates movementStates = movementStatesComponent.getMovementStates();
      boolean fallDamageActive = !movementStates.inFluid && !movementStates.climbing && !movementStates.flying && !movementStates.gliding;
      if (fallDamageActive) {
         Vector3d position = transformComponent.getPosition();
         if (!movementStates.onGround) {
            if (position.y() > locY) {
               this.currentFallDistance = this.currentFallDistance + (position.y() - locY);
            }
         } else {
            this.currentFallDistance = 0.0;
         }
      } else {
         this.currentFallDistance = 0.0;
      }

      super.moveTo(ref, locX, locY, locZ, componentAccessor);
   }

   @Deprecated(forRemoval = true)
   @Nullable
   public ItemStackSlotTransaction updateItemStackDurability(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull ItemStack itemStack,
      ItemContainer container,
      int slotId,
      double durabilityChange,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      return ItemUtils.updateItemStackDurability(ref, itemStack, container, slotId, durabilityChange, componentAccessor);
   }

   public double getCurrentFallDistance() {
      return this.currentFallDistance;
   }

   public void setCurrentFallDistance(double currentFallDistance) {
      this.currentFallDistance = currentFallDistance;
   }

   @Nonnull
   @Override
   public String toString() {
      return "LivingEntity{, " + super.toString() + "}";
   }
}
