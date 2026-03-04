package com.hypixel.hytale.builtin.adventure.npcreputation;

import com.hypixel.hytale.builtin.adventure.reputation.ReputationPlugin;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.StoreSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.blackboard.Blackboard;
import com.hypixel.hytale.server.npc.blackboard.view.attitude.AttitudeView;
import javax.annotation.Nonnull;

public class ReputationAttitudeSystem extends StoreSystem<EntityStore> {
   @Nonnull
   private final ResourceType<EntityStore, Blackboard> blackboardResourceType;
   @Nonnull
   private final ComponentType<EntityStore, Player> playerComponentType;

   public ReputationAttitudeSystem(
      @Nonnull ResourceType<EntityStore, Blackboard> blackboardResourceType, @Nonnull ComponentType<EntityStore, Player> playerComponentType
   ) {
      this.blackboardResourceType = blackboardResourceType;
      this.playerComponentType = playerComponentType;
   }

   @Override
   public void onSystemAddedToStore(@Nonnull Store<EntityStore> store) {
      Blackboard blackboardResource = store.getResource(this.blackboardResourceType);
      AttitudeView attitudeView = blackboardResource.getView(AttitudeView.class, 0L);
      attitudeView.registerProvider(100, (ref, role, targetRef, accessor) -> {
         Player playerComponent = store.getComponent(targetRef, this.playerComponentType);
         return playerComponent == null ? null : ReputationPlugin.get().getAttitude(store, targetRef, ref);
      });
   }

   @Override
   public void onSystemRemovedFromStore(@Nonnull Store<EntityStore> store) {
   }
}
