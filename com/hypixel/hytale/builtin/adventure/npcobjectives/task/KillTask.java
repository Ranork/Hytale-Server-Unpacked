package com.hypixel.hytale.builtin.adventure.npcobjectives.task;

import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import javax.annotation.Nonnull;

public interface KillTask {
   void checkKilledEntity(
      @Nonnull Store<EntityStore> var1, @Nonnull Ref<EntityStore> var2, @Nonnull Objective var3, @Nonnull NPCEntity var4, @Nonnull Damage var5
   );
}
