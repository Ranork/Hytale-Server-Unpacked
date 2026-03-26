package com.hypixel.hytale.server.core.modules.entity.player;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.packets.inventory.UpdatePlayerInventory;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
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

      InventoryComponent.Storage storage = archetypeChunk.getComponent(index, InventoryComponent.Storage.getComponentType());
      InventoryComponent.Armor armor = archetypeChunk.getComponent(index, InventoryComponent.Armor.getComponentType());
      InventoryComponent.Hotbar hotbar = archetypeChunk.getComponent(index, InventoryComponent.Hotbar.getComponentType());
      InventoryComponent.Utility utility = archetypeChunk.getComponent(index, InventoryComponent.Utility.getComponentType());
      InventoryComponent.Tool tool = archetypeChunk.getComponent(index, InventoryComponent.Tool.getComponentType());
      InventoryComponent.Backpack backpack = archetypeChunk.getComponent(index, InventoryComponent.Backpack.getComponentType());
      boolean isStorageDirty = storage != null && storage.consumeIsDirty();
      boolean isArmorDirty = armor != null && armor.consumeIsDirty();
      boolean isHotbarDirty = hotbar != null && hotbar.consumeIsDirty();
      boolean isUtilityDirty = utility != null && utility.consumeIsDirty();
      boolean isToolDirty = tool != null && tool.consumeIsDirty();
      boolean isBackpackDirty = backpack != null && backpack.consumeIsDirty();
      if (isStorageDirty || isArmorDirty || isHotbarDirty || isUtilityDirty || isToolDirty || isBackpackDirty) {
         PlayerRef playerRefComponent = archetypeChunk.getComponent(index, this.refComponentType);

         assert playerRefComponent != null;

         playerRefComponent.getPacketHandler()
            .writeNoCache(
               new UpdatePlayerInventory(
                  isStorageDirty ? storage.getInventory().toPacket() : null,
                  isArmorDirty ? armor.getInventory().toPacket() : null,
                  isHotbarDirty ? hotbar.getInventory().toPacket() : null,
                  isUtilityDirty ? utility.getInventory().toPacket() : null,
                  isToolDirty ? tool.getInventory().toPacket() : null,
                  isBackpackDirty ? backpack.getInventory().toPacket() : null
               )
            );
      }

      playerComponent.getWindowManager().updateWindows();
   }
}
