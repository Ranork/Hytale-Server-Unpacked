package com.hypixel.hytale.server.npc.blackboard.view;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import javax.annotation.Nonnull;

public interface IBlackboardView<View extends IBlackboardView<View>> {
   boolean isOutdated(@Nonnull Ref<EntityStore> var1, @Nonnull Store<EntityStore> var2);

   View getUpdatedView(@Nonnull Ref<EntityStore> var1, @Nonnull ComponentAccessor<EntityStore> var2);

   void initialiseEntity(@Nonnull Ref<EntityStore> var1, @Nonnull NPCEntity var2);

   void cleanup();

   void onWorldRemoved();
}
