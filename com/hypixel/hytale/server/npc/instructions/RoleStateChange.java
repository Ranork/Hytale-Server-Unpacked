package com.hypixel.hytale.server.npc.instructions;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.role.Role;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface RoleStateChange {
   default void registerWithSupport(Role role) {
   }

   default void motionControllerChanged(
      @Nullable Ref<EntityStore> ref,
      @Nonnull NPCEntity npcComponent,
      MotionController motionController,
      @Nullable ComponentAccessor<EntityStore> componentAccessor
   ) {
   }

   default void loaded(Role role) {
   }

   default void spawned(Role role) {
   }

   default void unloaded(Role role) {
   }

   default void removed(Role role) {
   }

   default void teleported(Role role, World from, World to) {
   }
}
