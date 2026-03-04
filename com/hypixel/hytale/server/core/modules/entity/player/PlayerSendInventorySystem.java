package com.hypixel.hytale.server.core.modules.entity.player;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class PlayerSendInventorySystem extends EntityTickingSystem<EntityStore> {
   @Nonnull
   private final ComponentType<EntityStore, Player> componentType;
   @Nonnull
   private final ComponentType<EntityStore, PlayerRef> refComponentType = PlayerRef.getComponentType();
   @Nonnull
   private final Query<EntityStore> query;

   public PlayerSendInventorySystem(ComponentType<EntityStore, Player> componentType) {
      this.componentType = componentType;
      this.query = Query.and(componentType, this.refComponentType);
   }

   @Nonnull
   @Override
   public Query<EntityStore> getQuery() {
      return this.query;
   }

   @Override
   public boolean isParallel(int archetypeChunkSize, int taskCount) {
      return EntityTickingSystem.maybeUseParallel(archetypeChunkSize, taskCount);
   }

   @Override
   public void tick(
      float dt,
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      Player playerComponent = archetypeChunk.getComponent(index, this.componentType);

      assert playerComponent != null;

      Inventory inventory = playerComponent.getInventory();
      if (inventory.consumeIsDirty()) {
         PlayerRef playerRefComponent = archetypeChunk.getComponent(index, this.refComponentType);

         assert playerRefComponent != null;

         playerRefComponent.getPacketHandler().write(inventory.toPacket());
      }

      playerComponent.getWindowManager().updateWindows();
   }
}
