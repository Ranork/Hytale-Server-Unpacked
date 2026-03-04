package com.hypixel.hytale.builtin.adventure.npcreputation;

import com.hypixel.hytale.builtin.adventure.reputation.ReputationGroupComponent;
import com.hypixel.hytale.builtin.adventure.reputation.assets.ReputationGroup;
import com.hypixel.hytale.builtin.tagset.TagSetPlugin;
import com.hypixel.hytale.builtin.tagset.config.NPCGroup;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class NPCReputationHolderSystem extends HolderSystem<EntityStore> {
   @Nonnull
   private final ComponentType<EntityStore, ReputationGroupComponent> reputationGroupComponentType;
   @Nonnull
   private final ComponentType<EntityStore, NPCEntity> npcEntityComponentType;
   @Nonnull
   private final Query<EntityStore> query;

   public NPCReputationHolderSystem(
      @Nonnull ComponentType<EntityStore, ReputationGroupComponent> reputationGroupComponentType,
      @Nonnull ComponentType<EntityStore, NPCEntity> npcEntityComponentType
   ) {
      this.reputationGroupComponentType = reputationGroupComponentType;
      this.npcEntityComponentType = npcEntityComponentType;
      this.query = Query.and(npcEntityComponentType, Query.not(reputationGroupComponentType));
   }

   @Nonnull
   @Override
   public Query<EntityStore> getQuery() {
      return this.query;
   }

   @Override
   public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
      NPCEntity npcComponent = holder.getComponent(this.npcEntityComponentType);

      assert npcComponent != null;

      int npcTypeIndex = npcComponent.getNPCTypeIndex();

      for (Entry<String, ReputationGroup> reputationEntry : ReputationGroup.getAssetMap().getAssetMap().entrySet()) {
         for (String npcGroup : reputationEntry.getValue().getNpcGroups()) {
            int index = NPCGroup.getAssetMap().getIndex(npcGroup);
            if (index == Integer.MIN_VALUE) {
               throw new IllegalArgumentException("Unknown npc group! " + npcGroup);
            }

            if (TagSetPlugin.get(NPCGroup.class).tagInSet(index, npcTypeIndex)) {
               holder.addComponent(this.reputationGroupComponentType, new ReputationGroupComponent(reputationEntry.getKey()));
               return;
            }
         }
      }
   }

   @Override
   public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
   }
}
