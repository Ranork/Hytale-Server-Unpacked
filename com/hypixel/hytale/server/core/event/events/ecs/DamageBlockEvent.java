package com.hypixel.hytale.server.core.event.events.ecs;

import com.hypixel.hytale.component.system.CancellableEcsEvent;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DamageBlockEvent extends CancellableEcsEvent {
   @Nullable
   private final ItemStack itemInHand;
   @Nonnull
   private Vector3i targetBlock;
   @Nonnull
   private final BlockType blockType;
   private final float currentDamage;
   private float damage;

   public DamageBlockEvent(@Nullable ItemStack itemInHand, @Nonnull Vector3i targetBlock, @Nonnull BlockType blockType, float currentDamage, float damage) {
      this.itemInHand = itemInHand;
      this.targetBlock = targetBlock;
      this.blockType = blockType;
      this.currentDamage = currentDamage;
      this.damage = damage;
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
      this.targetBlock = targetBlock;
   }

   @Nonnull
   public BlockType getBlockType() {
      return this.blockType;
   }

   public float getCurrentDamage() {
      return this.currentDamage;
   }

   public float getDamage() {
      return this.damage;
   }

   public void setDamage(float damage) {
      this.damage = damage;
   }
}
