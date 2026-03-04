package com.hypixel.hytale.server.npc.corecomponents.lifecycle;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.lifecycle.builders.BuilderActionDelayDespawn;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;

public class ActionDelayDespawn extends ActionBase {
   protected final float time;
   protected final boolean shorten;

   public ActionDelayDespawn(@Nonnull BuilderActionDelayDespawn builderActionDelayDespawn) {
      super(builderActionDelayDespawn);
      this.time = builderActionDelayDespawn.getTime();
      this.shorten = builderActionDelayDespawn.getShorten();
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      NPCEntity npcEntity = store.getComponent(ref, NPCEntity.getComponentType());

      assert npcEntity != null;

      double delay = npcEntity.getDespawnTime();
      if (this.shorten && delay < this.time || !this.shorten && delay > this.time) {
         npcEntity.setDespawnTime(this.time);
      }

      return true;
   }
}
