package com.hypixel.hytale.server.npc.corecomponents.entity.filters;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.role.Role;
import java.util.List;
import javax.annotation.Nonnull;

public class EntityFilterAnd extends EntityFilterMany {
   public EntityFilterAnd(@Nonnull List<IEntityFilter> filters) {
      super(filters);
   }

   @Override
   public boolean matchesEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull Role role, @Nonnull Store<EntityStore> store) {
      for (IEntityFilter filter : this.filters) {
         if (!filter.matchesEntity(ref, targetRef, role, store)) {
            return false;
         }
      }

      return true;
   }
}
