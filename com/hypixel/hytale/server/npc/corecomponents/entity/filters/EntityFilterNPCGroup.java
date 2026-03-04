package com.hypixel.hytale.server.npc.corecomponents.entity.filters;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders.BuilderEntityFilterNPCGroup;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.WorldSupport;
import javax.annotation.Nonnull;

public class EntityFilterNPCGroup extends EntityFilterBase {
   public static final int COST = 200;
   protected final int[] includeGroups;
   protected final int[] excludeGroups;

   public EntityFilterNPCGroup(@Nonnull BuilderEntityFilterNPCGroup builder, @Nonnull BuilderSupport support) {
      this.includeGroups = builder.getIncludeGroups(support);
      this.excludeGroups = builder.getExcludeGroups(support);
   }

   @Override
   public boolean matchesEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull Role role, @Nonnull Store<EntityStore> store) {
      if (this.includeGroups != null && this.includeGroups.length > 0 && !WorldSupport.isGroupMember(role.getRoleIndex(), targetRef, this.includeGroups, store)
         )
       {
         return false;
      } else {
         return this.excludeGroups != null && this.excludeGroups.length > 0
            ? !WorldSupport.isGroupMember(role.getRoleIndex(), targetRef, this.excludeGroups, store)
            : true;
      }
   }

   @Override
   public int cost() {
      return 200;
   }
}
