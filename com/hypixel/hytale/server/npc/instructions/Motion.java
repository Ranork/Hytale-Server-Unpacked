package com.hypixel.hytale.server.npc.instructions;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.movement.Steering;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.IAnnotatedComponent;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Motion extends RoleStateChange, IAnnotatedComponent {
   default void preComputeSteering(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nullable InfoProvider provider, @Nonnull Store<EntityStore> store) {
   }

   default void activate(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
   }

   default void deactivate(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
   }

   boolean computeSteering(
      @Nonnull Ref<EntityStore> var1,
      @Nonnull Role var2,
      @Nullable InfoProvider var3,
      double var4,
      @Nonnull Steering var6,
      @Nonnull ComponentAccessor<EntityStore> var7
   );
}
