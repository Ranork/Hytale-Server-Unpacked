package com.hypixel.hytale.server.npc.corecomponents.lifecycle;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.lifecycle.builders.BuilderActionDespawn;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;

public class ActionDespawn extends ActionBase {
   protected final boolean force;

   public ActionDespawn(@Nonnull BuilderActionDespawn builderActionDespawn) {
      super(builderActionDespawn);
      this.force = builderActionDespawn.isForced();
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      if (this.force) {
         store.removeEntity(ref, RemoveReason.REMOVE);
      } else {
         NPCEntity npcComponent = store.getComponent(ref, NPCEntity.getComponentType());

         assert npcComponent != null;

         npcComponent.setToDespawn();
      }

      return true;
   }
}
