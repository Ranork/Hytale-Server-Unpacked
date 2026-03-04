package com.hypixel.hytale.builtin.creativehub.systems;

import com.hypixel.hytale.builtin.creativehub.CreativeHubPlugin;
import com.hypixel.hytale.builtin.creativehub.config.CreativeHubEntityConfig;
import com.hypixel.hytale.builtin.creativehub.ui.ReturnToHubButtonUI;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Optional;
import javax.annotation.Nonnull;

public class ReturnToHubButtonSystem extends RefSystem<EntityStore> {
   @Override
   public void onEntityAdded(
      @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      Optional<Boolean> isInChildWorld = this.getCreativeHubWorldStatus(store, commandBuffer, ref);
      if (!isInChildWorld.isEmpty()) {
         PlayerRef playerRef = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
         boolean disabled = !isInChildWorld.get();
         ReturnToHubButtonUI.send(playerRef, disabled);
      }
   }

   @Override
   public void onEntityRemove(
      @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      if (!this.getCreativeHubWorldStatus(store, commandBuffer, ref).isEmpty()) {
         PlayerRef playerRef = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
         ReturnToHubButtonUI.clear(playerRef);
      }
   }

   @Override
   public Query<EntityStore> getQuery() {
      return Query.and(Player.getComponentType(), PlayerRef.getComponentType());
   }

   private Optional<Boolean> getCreativeHubWorldStatus(
      @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull Ref<EntityStore> ref
   ) {
      CreativeHubEntityConfig hubEntityConfig = commandBuffer.getComponent(ref, CreativeHubEntityConfig.getComponentType());
      if (hubEntityConfig != null && hubEntityConfig.getParentHubWorldUuid() != null) {
         World parentWorld = Universe.get().getWorld(hubEntityConfig.getParentHubWorldUuid());
         if (parentWorld == null) {
            return Optional.empty();
         } else {
            World currentWorld = store.getExternalData().getWorld();
            World hubInstance = CreativeHubPlugin.get().getActiveHubInstance(parentWorld);
            return Optional.of(!currentWorld.equals(hubInstance));
         }
      } else {
         return Optional.empty();
      }
   }
}
