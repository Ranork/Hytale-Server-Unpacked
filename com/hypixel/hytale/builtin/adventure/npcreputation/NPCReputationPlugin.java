package com.hypixel.hytale.builtin.adventure.npcreputation;

import com.hypixel.hytale.builtin.adventure.reputation.ReputationGroupComponent;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.blackboard.Blackboard;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import javax.annotation.Nonnull;

public class NPCReputationPlugin extends JavaPlugin {
   public NPCReputationPlugin(@Nonnull JavaPluginInit init) {
      super(init);
   }

   @Override
   protected void setup() {
      ComponentRegistryProxy<EntityStore> entityStoreRegistry = this.getEntityStoreRegistry();
      ResourceType<EntityStore, Blackboard> blackboardResourceType = Blackboard.getResourceType();
      ComponentType<EntityStore, Player> playerComponentType = Player.getComponentType();
      ComponentType<EntityStore, ReputationGroupComponent> reputationGroupComponentType = ReputationGroupComponent.getComponentType();
      ComponentType<EntityStore, NPCEntity> npcEntityComponentType = NPCEntity.getComponentType();
      entityStoreRegistry.registerSystem(new ReputationAttitudeSystem(blackboardResourceType, playerComponentType));
      entityStoreRegistry.registerSystem(new NPCReputationHolderSystem(reputationGroupComponentType, npcEntityComponentType));
   }
}
