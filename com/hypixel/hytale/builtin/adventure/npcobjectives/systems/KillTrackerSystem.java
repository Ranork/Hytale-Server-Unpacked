package com.hypixel.hytale.builtin.adventure.npcobjectives.systems;

import com.hypixel.hytale.builtin.adventure.npcobjectives.resources.KillTrackerResource;
import com.hypixel.hytale.builtin.adventure.npcobjectives.transaction.KillTaskTransaction;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import java.util.List;
import javax.annotation.Nonnull;

public class KillTrackerSystem extends DeathSystems.OnDeathSystem {
   @Nonnull
   private final ComponentType<EntityStore, NPCEntity> npcEntityComponentType;
   @Nonnull
   private final ResourceType<EntityStore, KillTrackerResource> killTrackerResourceType;

   public KillTrackerSystem(
      @Nonnull ComponentType<EntityStore, NPCEntity> npcEntityComponentType, @Nonnull ResourceType<EntityStore, KillTrackerResource> killTrackerResourceType
   ) {
      this.npcEntityComponentType = npcEntityComponentType;
      this.killTrackerResourceType = killTrackerResourceType;
   }

   @Nonnull
   @Override
   public Query<EntityStore> getQuery() {
      return this.npcEntityComponentType;
   }

   public void onComponentAdded(
      @Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      NPCEntity npcEntityComponent = store.getComponent(ref, this.npcEntityComponentType);

      assert npcEntityComponent != null;

      KillTrackerResource killTrackerResource = store.getResource(this.killTrackerResourceType);
      List<KillTaskTransaction> killTasks = killTrackerResource.getKillTasks();
      Damage deathInfo = component.getDeathInfo();
      if (deathInfo != null) {
         int size = killTasks.size();

         for (int i = size - 1; i >= 0; i--) {
            KillTaskTransaction entry = killTasks.get(i);
            entry.getTask().checkKilledEntity(store, ref, entry.getObjective(), npcEntityComponent, deathInfo);
         }
      }
   }
}
