package com.hypixel.hytale.server.npc.corecomponents.entity.filters;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.role.Role;
import javax.annotation.Nonnull;

public class EntityFilterLineOfSight extends EntityFilterBase {
   public static final int COST = 400;

   @Override
   public boolean matchesEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull Role role, @Nonnull Store<EntityStore> store) {
      return role.getPositionCache().hasLineOfSight(ref, targetRef, store);
   }

   @Override
   public int cost() {
      return 400;
   }
}
