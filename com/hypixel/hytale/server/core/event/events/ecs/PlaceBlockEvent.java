package com.hypixel.hytale.server.core.event.events.ecs;

import com.hypixel.hytale.component.system.CancellableEcsEvent;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlaceBlockEvent extends CancellableEcsEvent {
   @Nullable
   private final ItemStack itemInHand;
   @Nonnull
   private Vector3i targetBlock;
   @Nonnull
   private RotationTuple rotation;

   public PlaceBlockEvent(@Nullable ItemStack itemInHand, @Nonnull Vector3i targetBlock, @Nonnull RotationTuple rotation) {
      this.itemInHand = itemInHand;
      this.targetBlock = targetBlock;
      this.rotation = rotation;
   }

   @Nullable
   public ItemStack getItemInHand() {
      return this.itemInHand;
   }

   @Nonnull
   public Vector3i getTargetBlock() {
      return this.targetBlock;
   }

   public void setTargetBlock(@Nonnull Vector3i targetBlock) {
      Objects.requireNonNull(targetBlock, "Block can't be null");
      this.targetBlock = targetBlock;
   }

   @Nonnull
   public RotationTuple getRotation() {
      return this.rotation;
   }

   public void setRotation(@Nonnull RotationTuple rotation) {
      this.rotation = rotation;
   }
}
