package com.hypixel.hytale.server.core.event.events.ecs;

import com.hypixel.hytale.component.system.CancellableEcsEvent;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BreakBlockEvent extends CancellableEcsEvent {
   @Nullable
   private final ItemStack itemInHand;
   @Nonnull
   private Vector3i targetBlock;
   @Nonnull
   private final BlockType blockType;

   public BreakBlockEvent(@Nullable ItemStack itemInHand, @Nonnull Vector3i targetBlock, @Nonnull BlockType blockType) {
      this.itemInHand = itemInHand;
      this.targetBlock = targetBlock;
      this.blockType = blockType;
   }

   @Nullable
   public ItemStack getItemInHand() {
      return this.itemInHand;
   }

   @Nonnull
   public Vector3i getTargetBlock() {
      return this.targetBlock;
   }

   @Nonnull
   public BlockType getBlockType() {
      return this.blockType;
   }

   public void setTargetBlock(@Nonnull Vector3i targetBlock) {
      this.targetBlock = targetBlock;
   }
}
