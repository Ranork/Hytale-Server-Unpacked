package com.hypixel.hytale.server.core.event.events.player;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.event.ICancellable;
import com.hypixel.hytale.protocol.MouseMotionEvent;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import org.joml.Vector2fc;
import org.joml.Vector3i;

public class PlayerMouseMotionEvent extends PlayerEvent<Void> implements ICancellable {
   private final long clientUseTime;
   private final Item itemInHand;
   private final Vector3i targetBlock;
   private final Ref<EntityStore> targetEntityRef;
   private final Vector2fc screenPoint;
   private final MouseMotionEvent mouseMotion;
   private boolean cancelled;

   public PlayerMouseMotionEvent(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Player player,
      long clientUseTime,
      Item itemInHand,
      Vector3i targetBlock,
      Ref<EntityStore> targetEntityRef,
      Vector2fc screenPoint,
      MouseMotionEvent mouseMotion
   ) {
      super(ref, player);
      this.clientUseTime = clientUseTime;
      this.itemInHand = itemInHand;
      this.targetBlock = targetBlock;
      this.targetEntityRef = targetEntityRef;
      this.screenPoint = screenPoint;
      this.mouseMotion = mouseMotion;
   }

   @Override
   public boolean isCancelled() {
      return this.cancelled;
   }

   @Override
   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   public long getClientUseTime() {
      return this.clientUseTime;
   }

   public Item getItemInHand() {
      return this.itemInHand;
   }

   public Vector3i getTargetBlock() {
      return this.targetBlock;
   }

   public Ref<EntityStore> getTargetEntityRef() {
      return this.targetEntityRef;
   }

   public Vector2fc getScreenPoint() {
      return this.screenPoint;
   }

   public MouseMotionEvent getMouseMotion() {
      return this.mouseMotion;
   }

   @Nonnull
   @Override
   public String toString() {
      return "PlayerMouseMotionEvent{clientUseTime="
         + this.clientUseTime
         + ", itemInHand="
         + this.itemInHand
         + ", targetBlock="
         + this.targetBlock
         + ", targetEntityRef="
         + this.targetEntityRef
         + ", screenPoint="
         + this.screenPoint
         + ", mouseMotion="
         + this.mouseMotion
         + ", cancelled="
         + this.cancelled
         + "} "
         + super.toString();
   }
}
