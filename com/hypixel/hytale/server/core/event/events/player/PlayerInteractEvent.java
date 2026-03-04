package com.hypixel.hytale.server.core.event.events.player;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.event.ICancellable;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

@Deprecated
public class PlayerInteractEvent extends PlayerEvent<String> implements ICancellable {
   private final InteractionType actionType;
   private final long clientUseTime;
   private final ItemStack itemInHand;
   private final Vector3i targetBlock;
   private final Ref<EntityStore> targetRef;
   private final Entity targetEntity;
   private boolean cancelled;

   public PlayerInteractEvent(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Player player,
      long clientUseTime,
      InteractionType actionType,
      ItemStack itemInHand,
      Vector3i targetBlock,
      Ref<EntityStore> targetRef,
      Entity targetEntity
   ) {
      super(ref, player);
      this.actionType = actionType;
      this.clientUseTime = clientUseTime;
      this.itemInHand = itemInHand;
      this.targetBlock = targetBlock;
      this.targetRef = targetRef;
      this.targetEntity = targetEntity;
   }

   @Override
   public boolean isCancelled() {
      return this.cancelled;
   }

   @Override
   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   public InteractionType getActionType() {
      return this.actionType;
   }

   public long getClientUseTime() {
      return this.clientUseTime;
   }

   public ItemStack getItemInHand() {
      return this.itemInHand;
   }

   public Vector3i getTargetBlock() {
      return this.targetBlock;
   }

   public Entity getTargetEntity() {
      return this.targetEntity;
   }

   public Ref<EntityStore> getTargetRef() {
      return this.targetRef;
   }

   @Nonnull
   @Override
   public String toString() {
      return "PlayerInteractEvent{actionType="
         + this.actionType
         + ", clientUseTime="
         + this.clientUseTime
         + ", itemInHand="
         + this.itemInHand
         + ", targetBlock="
         + this.targetBlock
         + ", targetEntity="
         + this.targetEntity
         + ", cancelled="
         + this.cancelled
         + "} "
         + super.toString();
   }
}
