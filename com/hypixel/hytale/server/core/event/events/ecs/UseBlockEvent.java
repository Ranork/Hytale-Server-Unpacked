package com.hypixel.hytale.server.core.event.events.ecs;

import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.component.system.ICancellableEcsEvent;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import javax.annotation.Nonnull;

public abstract class UseBlockEvent extends EcsEvent {
   @Nonnull
   private final InteractionType interactionType;
   @Nonnull
   private final InteractionContext context;
   @Nonnull
   private final Vector3i targetBlock;
   @Nonnull
   private final BlockType blockType;

   public UseBlockEvent(
      @Nonnull InteractionType interactionType, @Nonnull InteractionContext context, @Nonnull Vector3i targetBlock, @Nonnull BlockType blockType
   ) {
      this.interactionType = interactionType;
      this.context = context;
      this.targetBlock = targetBlock;
      this.blockType = blockType;
   }

   @Nonnull
   public InteractionType getInteractionType() {
      return this.interactionType;
   }

   @Nonnull
   public InteractionContext getContext() {
      return this.context;
   }

   @Nonnull
   public Vector3i getTargetBlock() {
      return this.targetBlock;
   }

   @Nonnull
   public BlockType getBlockType() {
      return this.blockType;
   }

   public static final class Post extends UseBlockEvent {
      public Post(@Nonnull InteractionType interactionType, @Nonnull InteractionContext context, @Nonnull Vector3i targetBlock, @Nonnull BlockType blockType) {
         super(interactionType, context, targetBlock, blockType);
      }
   }

   public static final class Pre extends UseBlockEvent implements ICancellableEcsEvent {
      private boolean cancelled = false;

      public Pre(@Nonnull InteractionType interactionType, @Nonnull InteractionContext context, @Nonnull Vector3i targetBlock, @Nonnull BlockType blockType) {
         super(interactionType, context, targetBlock, blockType);
      }

      @Override
      public boolean isCancelled() {
         return this.cancelled;
      }

      @Override
      public void setCancelled(boolean cancelled) {
         this.cancelled = cancelled;
      }
   }
}
