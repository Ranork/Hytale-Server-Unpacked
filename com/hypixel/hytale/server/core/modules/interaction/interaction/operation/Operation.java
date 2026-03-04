package com.hypixel.hytale.server.core.modules.interaction.interaction.operation;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.InteractionRules;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.IntSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Operation {
   void tick(
      @Nonnull Ref<EntityStore> var1,
      @Nonnull LivingEntity var2,
      boolean var3,
      float var4,
      @Nonnull InteractionType var5,
      @Nonnull InteractionContext var6,
      @Nonnull CooldownHandler var7
   );

   void simulateTick(
      @Nonnull Ref<EntityStore> var1,
      @Nonnull LivingEntity var2,
      boolean var3,
      float var4,
      @Nonnull InteractionType var5,
      @Nonnull InteractionContext var6,
      @Nonnull CooldownHandler var7
   );

   default void handle(@Nonnull Ref<EntityStore> ref, boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context) {
   }

   WaitForDataFrom getWaitForDataFrom();

   @Nullable
   default InteractionRules getRules() {
      return null;
   }

   default Int2ObjectMap<IntSet> getTags() {
      return Int2ObjectMaps.emptyMap();
   }

   default Operation getInnerOperation() {
      Operation op = this;

      while (op instanceof Operation.NestedOperation) {
         op = ((Operation.NestedOperation)op).inner();
      }

      return op;
   }

   public interface NestedOperation {
      Operation inner();
   }
}
