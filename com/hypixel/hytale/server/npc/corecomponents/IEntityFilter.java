package com.hypixel.hytale.server.npc.corecomponents;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.instructions.RoleStateChange;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.util.IAnnotatedComponent;
import java.util.Arrays;
import java.util.Comparator;
import javax.annotation.Nonnull;

public interface IEntityFilter extends RoleStateChange, IAnnotatedComponent {
   IEntityFilter[] EMPTY_ARRAY = new IEntityFilter[0];
   int MINIMAL_COST = 0;
   int LOW_COST = 100;
   int MID_COST = 200;
   int HIGH_COST = 300;
   int EXTREME_COST = 400;

   boolean matchesEntity(@Nonnull Ref<EntityStore> var1, @Nonnull Ref<EntityStore> var2, @Nonnull Role var3, @Nonnull Store<EntityStore> var4);

   int cost();

   static void prioritiseFilters(@Nonnull IEntityFilter[] filters) {
      Arrays.sort(filters, Comparator.comparingInt(IEntityFilter::cost));
   }
}
