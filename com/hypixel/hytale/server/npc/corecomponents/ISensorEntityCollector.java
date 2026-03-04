package com.hypixel.hytale.server.npc.corecomponents;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.instructions.RoleStateChange;
import com.hypixel.hytale.server.npc.role.Role;
import javax.annotation.Nonnull;

public interface ISensorEntityCollector extends RoleStateChange {
   ISensorEntityCollector DEFAULT = new ISensorEntityCollector() {
      @Override
      public void init(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      }

      @Override
      public void collectMatching(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      }

      @Override
      public void collectNonMatching(@Nonnull Ref<EntityStore> targetRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      }

      @Override
      public boolean terminateOnFirstMatch() {
         return true;
      }

      @Override
      public void cleanup() {
      }
   };

   void init(@Nonnull Ref<EntityStore> var1, @Nonnull Role var2, @Nonnull ComponentAccessor<EntityStore> var3);

   void collectMatching(@Nonnull Ref<EntityStore> var1, @Nonnull Ref<EntityStore> var2, @Nonnull ComponentAccessor<EntityStore> var3);

   void collectNonMatching(@Nonnull Ref<EntityStore> var1, @Nonnull ComponentAccessor<EntityStore> var2);

   boolean terminateOnFirstMatch();

   void cleanup();
}
