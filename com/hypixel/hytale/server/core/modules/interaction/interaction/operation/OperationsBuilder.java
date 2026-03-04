package com.hypixel.hytale.server.core.modules.interaction.interaction.operation;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.InteractionRules;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;

public class OperationsBuilder {
   @Nonnull
   private final List<Operation> operationList = new ObjectArrayList();

   @Nonnull
   public Label createLabel() {
      return new Label(this.operationList.size());
   }

   @Nonnull
   public Label createUnresolvedLabel() {
      return new Label(Integer.MIN_VALUE);
   }

   public void resolveLabel(@Nonnull Label label) {
      if (label.index != Integer.MIN_VALUE) {
         throw new IllegalArgumentException("Label already resolved");
      } else {
         label.index = this.operationList.size();
      }
   }

   public void jump(@Nonnull Label target) {
      this.operationList.add(new JumpOperation(target));
   }

   public void addOperation(@Nonnull Operation operation) {
      this.operationList.add(operation);
   }

   public void addOperation(@Nonnull Operation operation, Label... labels) {
      this.operationList.add(new OperationsBuilder.LabelOperation(operation, labels));
   }

   @Nonnull
   public Operation[] build() {
      return this.operationList.toArray(Operation[]::new);
   }

   private record LabelOperation(Operation inner, Label[] labels) implements Operation, Operation.NestedOperation {
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
         context.setLabels(this.labels);
         this.inner.tick(ref, entity, firstRun, time, type, context, cooldownHandler);
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
         context.setLabels(this.labels);
         this.inner.simulateTick(ref, entity, firstRun, time, type, context, cooldownHandler);
      }

      @Override
      public void handle(@Nonnull Ref<EntityStore> ref, boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context) {
         context.setLabels(this.labels);
         this.inner.handle(ref, firstRun, time, type, context);
      }

      @Override
      public WaitForDataFrom getWaitForDataFrom() {
         return this.inner.getWaitForDataFrom();
      }

      @Override
      public InteractionRules getRules() {
         return this.inner.getRules();
      }

      @Nonnull
      @Override
      public String toString() {
         return "LabelOperation{inner=" + this.inner + ", labels=" + Arrays.toString((Object[])this.labels) + "}";
      }
   }
}
