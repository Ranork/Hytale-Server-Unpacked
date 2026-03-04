package com.hypixel.hytale.server.core.modules.interaction.interaction.operation;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class JumpOperation implements Operation {
   private final Label target;

   protected JumpOperation(Label target) {
      this.target = target;
   }

   @Override
   public void tick(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull LivingEntity entity,
      boolean firstRun,
      float time,
      @Nonnull InteractionType type,
      @Nonnull InteractionContext context,
      @Nonnull CooldownHandler cooldownHandler
   ) {
      context.setOperationCounter(this.target.getIndex());
      context.getState().state = InteractionState.Finished;
   }

   @Override
   public void simulateTick(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull LivingEntity entity,
      boolean firstRun,
      float time,
      @Nonnull InteractionType type,
      @Nonnull InteractionContext context,
      @Nonnull CooldownHandler cooldownHandler
   ) {
      context.setOperationCounter(this.target.getIndex());
      context.getState().state = InteractionState.Finished;
   }

   @Nonnull
   @Override
   public WaitForDataFrom getWaitForDataFrom() {
      return WaitForDataFrom.None;
   }

   @Nonnull
   @Override
   public String toString() {
      return "JumpOperation{target=" + this.target + "}";
   }
}
