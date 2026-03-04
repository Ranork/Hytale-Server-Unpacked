package com.hypixel.hytale.server.core.event.events.player;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.event.ICancellable;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.MouseButtonEvent;
import com.hypixel.hytale.protocol.Vector2f;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class PlayerMouseButtonEvent extends PlayerEvent<Void> implements ICancellable {
   @Nonnull
   private final PlayerRef playerRef;
   private final long clientUseTime;
   private final Item itemInHand;
   private final Vector3i targetBlock;
   private final Entity targetEntity;
   private final Vector2f screenPoint;
   private final MouseButtonEvent mouseButton;
   private boolean cancelled;

   public PlayerMouseButtonEvent(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Player player,
      @Nonnull PlayerRef playerRefComponent,
      long clientUseTime,
      @Nonnull Item itemInHand,
      @Nonnull Vector3i targetBlock,
      @Nonnull Entity targetEntity,
      @Nonnull Vector2f screenPoint,
      @Nonnull MouseButtonEvent mouseButton
   ) {
      super(ref, player);
      this.playerRef = playerRefComponent;
      this.clientUseTime = clientUseTime;
      this.itemInHand = itemInHand;
      this.targetBlock = targetBlock;
      this.targetEntity = targetEntity;
      this.screenPoint = screenPoint;
      this.mouseButton = mouseButton;
   }

   @Nonnull
   public PlayerRef getPlayerRefComponent() {
      return this.playerRef;
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

   public Entity getTargetEntity() {
      return this.targetEntity;
   }

   public Vector2f getScreenPoint() {
      return this.screenPoint;
   }

   public MouseButtonEvent getMouseButton() {
      return this.mouseButton;
   }

   @Nonnull
   @Override
   public String toString() {
      return "PlayerMouseButtonEvent{clientUseTime="
         + this.clientUseTime
         + ", itemInHand="
         + this.itemInHand
         + ", targetBlock="
         + this.targetBlock
         + ", targetEntity="
         + this.targetEntity
         + ", screenPoint="
         + this.screenPoint
         + ", mouseButton="
         + this.mouseButton
         + ", cancelled="
         + this.cancelled
         + "} "
         + super.toString();
   }
}
