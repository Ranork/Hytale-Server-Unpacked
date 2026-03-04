package com.hypixel.hytale.builtin.adventure.npcobjectives.task;

import com.hypixel.hytale.builtin.adventure.npcobjectives.assets.KillObjectiveTaskAsset;
import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.builtin.adventure.objectives.task.CountObjectiveTask;
import com.hypixel.hytale.builtin.tagset.TagSetPlugin;
import com.hypixel.hytale.builtin.tagset.config.NPCGroup;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import javax.annotation.Nonnull;

public abstract class KillObjectiveTask extends CountObjectiveTask implements KillTask {
   @Nonnull
   public static final BuilderCodec<KillObjectiveTask> CODEC = BuilderCodec.abstractBuilder(KillObjectiveTask.class, CountObjectiveTask.CODEC).build();

   public KillObjectiveTask(@Nonnull KillObjectiveTaskAsset asset, int taskSetIndex, int taskIndex) {
      super(asset, taskSetIndex, taskIndex);
   }

   protected KillObjectiveTask() {
   }

   @Nonnull
   public KillObjectiveTaskAsset getAsset() {
      return (KillObjectiveTaskAsset)super.getAsset();
   }

   @Override
   public void checkKilledEntity(
      @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> npcRef, @Nonnull Objective objective, @Nonnull NPCEntity npc, @Nonnull Damage info
   ) {
      String key = this.getAsset().getNpcGroupId();
      int index = NPCGroup.getAssetMap().getIndex(key);
      if (index == Integer.MIN_VALUE) {
         throw new IllegalArgumentException("Unknown npc group! " + key);
      } else if (TagSetPlugin.get(NPCGroup.class).tagInSet(index, npc.getNPCTypeIndex())) {
         if (info.getSource() instanceof Damage.EntitySource entitySource) {
            Ref var11 = entitySource.getRef();
            if (store.getArchetype(var11).contains(Player.getComponentType())) {
               UUIDComponent sourceUuidComponent = store.getComponent(var11, UUIDComponent.getComponentType());
               if (sourceUuidComponent != null) {
                  if (objective.getActivePlayerUUIDs().contains(sourceUuidComponent.getUuid())) {
                     this.increaseTaskCompletion(store, npcRef, 1, objective);
                  }
               }
            }
         }
      }
   }
}
