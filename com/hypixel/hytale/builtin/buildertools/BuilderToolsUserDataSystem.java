package com.hypixel.hytale.builtin.buildertools;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class BuilderToolsUserDataSystem extends HolderSystem<EntityStore> {
   private static final Query<EntityStore> QUERY = Query.and(Player.getComponentType(), Query.not(BuilderToolsUserData.getComponentType()));

   @Nonnull
   @Override
   public Query<EntityStore> getQuery() {
      return QUERY;
   }

   @Override
   public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
      holder.ensureComponent(BuilderToolsUserData.getComponentType());
   }

   @Override
   public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
   }
}
