package com.hypixel.hytale.server.core.entity.damage;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.server.core.modules.entity.AllLegacyLivingEntityTypesQuery;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class DamageDataSetupSystem extends HolderSystem<EntityStore> {
   @Nonnull
   private final ComponentType<EntityStore, DamageDataComponent> damageDataComponentType;

   public DamageDataSetupSystem(@Nonnull ComponentType<EntityStore, DamageDataComponent> damageDataComponentType) {
      this.damageDataComponentType = damageDataComponentType;
   }

   @Override
   public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
      holder.ensureComponent(this.damageDataComponentType);
   }

   @Override
   public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
   }

   @Nonnull
   @Override
   public Query<EntityStore> getQuery() {
      return AllLegacyLivingEntityTypesQuery.INSTANCE;
   }
}
