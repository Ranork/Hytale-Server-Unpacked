package com.hypixel.hytale.server.core.inventory;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.component.system.EntityHolderEventSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.EquipmentUpdate;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemUtility;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemWeapon;
import com.hypixel.hytale.server.core.entity.StatModifiersManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.HotbarManager;
import com.hypixel.hytale.server.core.event.events.ecs.InventoryChangeEvent;
import com.hypixel.hytale.server.core.event.events.ecs.InventorySetActiveSlotEvent;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.SlotTransaction;
import com.hypixel.hytale.server.core.modules.entity.AllLegacyLivingEntityTypesQuery;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InventorySystems {
   public static class ActiveSlotChangedEntityEventSystem extends EntityEventSystem<EntityStore, InventorySetActiveSlotEvent> {
      public ActiveSlotChangedEntityEventSystem() {
         super(InventorySetActiveSlotEvent.class);
      }

      public void handle(
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer,
         @Nonnull InventorySetActiveSlotEvent event
      ) {
         InventoryComponent.Hotbar hotbarComponent = archetypeChunk.getComponent(index, InventoryComponent.Hotbar.getComponentType());
         if (hotbarComponent != null) {
            hotbarComponent.setOutdatedEquipment(true);
         }

         InventoryComponent.Utility utilityComponent = archetypeChunk.getComponent(index, InventoryComponent.Utility.getComponentType());
         if (utilityComponent != null) {
            utilityComponent.setOutdatedEquipment(true);
         }

         EntityStatMap entityStatMapComponent = archetypeChunk.getComponent(index, EntityStatMap.getComponentType());
         if (entityStatMapComponent != null) {
            StatModifiersManager statModifiersManager = entityStatMapComponent.getStatModifiersManager();
            statModifiersManager.scheduleRecalculate();

            int[] entityStatsToClear = switch (event.getInventorySectionId()) {
               case -5 -> {
                  if (utilityComponent == null) {
                     yield null;
                  } else {
                     ItemStack itemStack = utilityComponent.getActiveItem();
                     yield itemStack == null ? null : itemStack.getItem().getUtility().getEntityStatsToClear();
                  }
               }
               case -1 -> {
                  ItemStack itemInHand = InventoryComponent.getItemInHand(store, archetypeChunk.getReferenceTo(index));
                  if (itemInHand == null) {
                     yield null;
                  } else {
                     ItemWeapon weapon = itemInHand.getItem().getWeapon();
                     yield weapon != null ? weapon.getEntityStatsToClear() : null;
                  }
               }
               default -> null;
            };
            if (entityStatsToClear != null) {
               statModifiersManager.queueEntityStatsToClear(entityStatsToClear);
            }
         }
      }

      @Nullable
      @Override
      public Query<EntityStore> getQuery() {
         return InventoryComponent.Hotbar.getComponentType();
      }
   }

   public static class ActiveSlotChangedEntityHolderEventSystem extends EntityHolderEventSystem<EntityStore, InventorySetActiveSlotEvent> {
      public ActiveSlotChangedEntityHolderEventSystem() {
         super(InventorySetActiveSlotEvent.class);
      }

      public void handle(
         @Nonnull Holder<EntityStore> holder,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer,
         @Nonnull InventorySetActiveSlotEvent event
      ) {
         InventoryComponent.Hotbar hotbarComponent = holder.getComponent(InventoryComponent.Hotbar.getComponentType());
         if (hotbarComponent != null) {
            hotbarComponent.setOutdatedEquipment(true);
         }

         InventoryComponent.Utility utilityComponent = holder.getComponent(InventoryComponent.Utility.getComponentType());
         if (utilityComponent != null) {
            utilityComponent.setOutdatedEquipment(true);
         }

         EntityStatMap entityStatMapComponent = holder.getComponent(EntityStatMap.getComponentType());
         if (entityStatMapComponent != null) {
            StatModifiersManager statModifiersManager = entityStatMapComponent.getStatModifiersManager();
            statModifiersManager.scheduleRecalculate();

            int[] entityStatsToClear = switch (event.getInventorySectionId()) {
               case -5 -> {
                  if (utilityComponent == null) {
                     yield null;
                  } else {
                     ItemStack itemStack = utilityComponent.getActiveItem();
                     yield itemStack == null ? null : itemStack.getItem().getUtility().getEntityStatsToClear();
                  }
               }
               case -1 -> {
                  ItemStack itemInHand = InventoryComponent.getItemInHand(holder);
                  if (itemInHand == null) {
                     yield null;
                  } else {
                     ItemWeapon weapon = itemInHand.getItem().getWeapon();
                     yield weapon != null ? weapon.getEntityStatsToClear() : null;
                  }
               }
               default -> null;
            };
            if (entityStatsToClear != null) {
               statModifiersManager.queueEntityStatsToClear(entityStatsToClear);
            }
         }
      }

      @Nullable
      @Override
      public Query<EntityStore> getQuery() {
         return InventoryComponent.Hotbar.getComponentType();
      }
   }

   public static class ActiveSlotChangedToolsEventSystem extends EntityEventSystem<EntityStore, InventorySetActiveSlotEvent> {
      public ActiveSlotChangedToolsEventSystem() {
         super(InventorySetActiveSlotEvent.class);
      }

      public void handle(
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer,
         @Nonnull InventorySetActiveSlotEvent event
      ) {
         InventoryComponent.Tool toolComponent = archetypeChunk.getComponent(index, InventoryComponent.Tool.getComponentType());

         assert toolComponent != null;

         switch (event.getInventorySectionId()) {
            case -8:
               toolComponent.setUsingToolsItem(true);
               break;
            case -1:
               toolComponent.setUsingToolsItem(false);
         }
      }

      @Nullable
      @Override
      public Query<EntityStore> getQuery() {
         return InventoryComponent.Tool.getComponentType();
      }
   }

   public static class ActiveSlotChangedToolsHolderEventSystem extends EntityHolderEventSystem<EntityStore, InventorySetActiveSlotEvent> {
      public ActiveSlotChangedToolsHolderEventSystem() {
         super(InventorySetActiveSlotEvent.class);
      }

      public void handle(
         @Nonnull Holder<EntityStore> holder,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer,
         @Nonnull InventorySetActiveSlotEvent event
      ) {
         InventoryComponent.Tool toolComponent = holder.getComponent(InventoryComponent.Tool.getComponentType());
         if (toolComponent != null) {
            switch (event.getInventorySectionId()) {
               case -8:
                  toolComponent.setUsingToolsItem(true);
                  break;
               case -1:
                  toolComponent.setUsingToolsItem(false);
            }
         }
      }

      @Nullable
      @Override
      public Query<EntityStore> getQuery() {
         return InventoryComponent.Tool.getComponentType();
      }
   }

   public static class ArmorChangeEventSystem extends InventorySystems.InventoryChangeEventSystem<InventoryComponent.Armor> {
      public ArmorChangeEventSystem() {
         super(InventoryComponent.Armor.getComponentType());
      }
   }

   public static class BackpackChangeEventSystem extends InventorySystems.InventoryChangeEventSystem<InventoryComponent.Backpack> {
      public BackpackChangeEventSystem() {
         super(InventoryComponent.Backpack.getComponentType());
      }
   }

   public static class HotbarChangeEventSystem extends InventorySystems.InventoryChangeEventSystem<InventoryComponent.Hotbar> {
      public HotbarChangeEventSystem() {
         super(InventoryComponent.Hotbar.getComponentType());
      }
   }

   public abstract static class InventoryChangeEventSystem<Inv extends InventoryComponent> extends EntityTickingSystem<EntityStore> {
      protected final ComponentType<EntityStore, Inv> componentType;

      protected InventoryChangeEventSystem(ComponentType<EntityStore, Inv> componentType) {
         this.componentType = componentType;
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         Inv inventoryComponent = archetypeChunk.getComponent(index, this.componentType);

         assert inventoryComponent != null;

         ConcurrentLinkedQueue<ItemContainer.ItemContainerChangeEvent> changeEvents = inventoryComponent.getChangeEvents();
         Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);

         ItemContainer.ItemContainerChangeEvent changeEvent;
         while ((changeEvent = changeEvents.poll()) != null) {
            InventoryChangeEvent event = new InventoryChangeEvent(this.componentType, inventoryComponent, changeEvent.container(), changeEvent.transaction());
            commandBuffer.invoke(ref, event);
         }
      }

      @Nullable
      @Override
      public Query<EntityStore> getQuery() {
         return this.componentType;
      }
   }

   @Deprecated(forRemoval = true)
   public static class LegacyArmorChangeStatSystem extends EntityTickingSystem<EntityStore> {
      private final Query<EntityStore> query = Query.and(InventoryComponent.Armor.getComponentType(), AllLegacyLivingEntityTypesQuery.INSTANCE);
      private final Set<Dependency<EntityStore>> dependencies = Set.of(new SystemDependency<>(Order.BEFORE, InventorySystems.ArmorChangeEventSystem.class));

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         InventoryComponent.Armor inventory = archetypeChunk.getComponent(index, InventoryComponent.Armor.getComponentType());

         assert inventory != null;

         ConcurrentLinkedQueue<ItemContainer.ItemContainerChangeEvent> changeEvents = inventory.getChangeEvents();
         if (!changeEvents.isEmpty()) {
            inventory.setOutdatedEquipment(true);
            EntityStatMap entityStatMapComponent = archetypeChunk.getComponent(index, EntityStatMap.getComponentType());
            if (entityStatMapComponent != null) {
               entityStatMapComponent.getStatModifiersManager().scheduleRecalculate();
            }
         }
      }

      @Nullable
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }
   }

   @Deprecated(forRemoval = true)
   public static class LegacyHotbarChangeStatSystem extends EntityTickingSystem<EntityStore> {
      private final Query<EntityStore> query = Query.and(InventoryComponent.Hotbar.getComponentType(), AllLegacyLivingEntityTypesQuery.INSTANCE);
      private final Set<Dependency<EntityStore>> dependencies = Set.of(new SystemDependency<>(Order.BEFORE, InventorySystems.HotbarChangeEventSystem.class));

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         InventoryComponent.Hotbar hotbarComponent = archetypeChunk.getComponent(index, InventoryComponent.Hotbar.getComponentType());

         assert hotbarComponent != null;

         ConcurrentLinkedQueue<ItemContainer.ItemContainerChangeEvent> hotbarChangeEvents = hotbarComponent.getChangeEvents();
         if (!hotbarChangeEvents.isEmpty()) {
            boolean changed = false;
            byte activeHotbarSlot = hotbarComponent.getActiveSlot();

            for (ItemContainer.ItemContainerChangeEvent event : hotbarChangeEvents) {
               if (activeHotbarSlot != -1
                  && event.transaction().wasSlotModified(activeHotbarSlot)
                  && !(
                     event.transaction() instanceof SlotTransaction slot
                        && slot.getSlotAfter() != null
                        && ItemStack.isEquivalentType(slot.getSlotBefore(), slot.getSlotAfter())
                  )) {
                  changed = true;
               }
            }

            if (changed) {
               hotbarComponent.setOutdatedEquipment(true);
               EntityStatMap entityStatMapComponent = archetypeChunk.getComponent(index, EntityStatMap.getComponentType());
               if (entityStatMapComponent != null) {
                  StatModifiersManager statModifiersManager = entityStatMapComponent.getStatModifiersManager();
                  statModifiersManager.scheduleRecalculate();
                  ItemStack itemStack = hotbarComponent.getActiveItem();
                  if (itemStack != null) {
                     ItemWeapon itemWeapon = itemStack.getItem().getWeapon();
                     if (itemWeapon != null) {
                        int[] entityStatsToClear = itemWeapon.getEntityStatsToClear();
                        if (entityStatsToClear != null) {
                           statModifiersManager.queueEntityStatsToClear(entityStatsToClear);
                        }
                     }
                  }
               }
            }
         }
      }

      @Nullable
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }
   }

   @Deprecated(forRemoval = true)
   public static class LegacyUtilityChangeStatSystem extends EntityTickingSystem<EntityStore> {
      private final Query<EntityStore> query = Query.and(InventoryComponent.Utility.getComponentType(), AllLegacyLivingEntityTypesQuery.INSTANCE);
      private final Set<Dependency<EntityStore>> dependencies = Set.of(new SystemDependency<>(Order.BEFORE, InventorySystems.UtilityChangeEventSystem.class));

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         InventoryComponent.Utility inventory = archetypeChunk.getComponent(index, InventoryComponent.Utility.getComponentType());

         assert inventory != null;

         ConcurrentLinkedQueue<ItemContainer.ItemContainerChangeEvent> changeEvents = inventory.getChangeEvents();
         if (!changeEvents.isEmpty()) {
            boolean changed = false;
            byte activeHotbarSlot = inventory.getActiveSlot();

            for (ItemContainer.ItemContainerChangeEvent event : changeEvents) {
               if (activeHotbarSlot != -1
                  && event.transaction().wasSlotModified(activeHotbarSlot)
                  && !(
                     event.transaction() instanceof SlotTransaction slot
                        && slot.getSlotAfter() != null
                        && ItemStack.isEquivalentType(slot.getSlotBefore(), slot.getSlotAfter())
                  )) {
                  changed = true;
               }
            }

            if (changed) {
               inventory.setOutdatedEquipment(true);
               EntityStatMap entityStatMapComponent = archetypeChunk.getComponent(index, EntityStatMap.getComponentType());
               if (entityStatMapComponent != null) {
                  StatModifiersManager statModifiersManager = entityStatMapComponent.getStatModifiersManager();
                  statModifiersManager.scheduleRecalculate();
                  ItemStack itemStack = inventory.getActiveItem();
                  if (itemStack == null) {
                     return;
                  }

                  ItemUtility itemUtility = itemStack.getItem().getUtility();
                  if (itemUtility == null) {
                     return;
                  }

                  int[] entityStatsToClear = itemUtility.getEntityStatsToClear();
                  if (entityStatsToClear == null) {
                     return;
                  }

                  statModifiersManager.queueEntityStatsToClear(entityStatsToClear);
               }
            }
         }
      }

      @Nullable
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }
   }

   public static class PlayerInventoryChangeEventSystem extends EntityEventSystem<EntityStore, InventoryChangeEvent> {
      public PlayerInventoryChangeEventSystem() {
         super(InventoryChangeEvent.class);
      }

      public void handle(
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer,
         @Nonnull InventoryChangeEvent event
      ) {
         Player playerComponent = archetypeChunk.getComponent(index, Player.getComponentType());

         assert playerComponent != null;

         HotbarManager hotbarManager = playerComponent.getHotbarManager();
         if (!hotbarManager.getIsCurrentlyLoadingHotbar()) {
            if (playerComponent.getGameMode().equals(GameMode.Creative)) {
               InventoryComponent.Hotbar hotbarComponent = archetypeChunk.getComponent(index, InventoryComponent.Hotbar.getComponentType());

               assert hotbarComponent != null;

               ItemContainer hotbarInventory = hotbarComponent.getInventory();
               if (event.getItemContainer().equals(hotbarInventory)) {
                  Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
                  hotbarManager.saveHotbar(ref, (short)hotbarManager.getCurrentHotbarIndex(), store);
               }
            }
         }
      }

      @Nullable
      @Override
      public Query<EntityStore> getQuery() {
         return Query.and(Player.getComponentType(), InventoryComponent.Hotbar.getComponentType());
      }
   }

   public static class StorageChangeEventSystem extends InventorySystems.InventoryChangeEventSystem<InventoryComponent.Storage> {
      public StorageChangeEventSystem() {
         super(InventoryComponent.Storage.getComponentType());
      }
   }

   public static class SyncEquipmentSystem extends EntityTickingSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType;
      @Nonnull
      private final ComponentType<EntityStore, InventoryComponent.Armor> armorComponentType;
      @Nonnull
      private final ComponentType<EntityStore, InventoryComponent.Hotbar> hotbarComponentType;
      @Nonnull
      private final ComponentType<EntityStore, InventoryComponent.Utility> utilityComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public SyncEquipmentSystem(@Nonnull ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType) {
         this.visibleComponentType = visibleComponentType;
         this.armorComponentType = InventoryComponent.Armor.getComponentType();
         this.hotbarComponentType = InventoryComponent.Hotbar.getComponentType();
         this.utilityComponentType = InventoryComponent.Utility.getComponentType();
         this.query = Query.and(visibleComponentType, Query.or(this.armorComponentType, this.hotbarComponentType, this.utilityComponentType));
      }

      @Nullable
      @Override
      public SystemGroup<EntityStore> getGroup() {
         return EntityTrackerSystems.QUEUE_UPDATE_GROUP;
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
         EntityTrackerSystems.Visible visibleComponent = archetypeChunk.getComponent(index, this.visibleComponentType);

         assert visibleComponent != null;

         Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
         boolean shouldSync = false;
         InventoryComponent.Armor armorComponent = archetypeChunk.getComponent(index, this.armorComponentType);
         if (armorComponent != null && armorComponent.consumeOutdatedEquipment()) {
            shouldSync = true;
         }

         InventoryComponent.Hotbar hotbarComponent = archetypeChunk.getComponent(index, this.hotbarComponentType);
         if (hotbarComponent != null && hotbarComponent.consumeOutdatedEquipment()) {
            shouldSync = true;
         }

         InventoryComponent.Utility utilityComponent = archetypeChunk.getComponent(index, this.utilityComponentType);
         if (utilityComponent != null && utilityComponent.consumeOutdatedEquipment()) {
            shouldSync = true;
         }

         if (shouldSync) {
            queueUpdatesFor(ref, commandBuffer, visibleComponent.visibleTo, armorComponent, utilityComponent);
         } else if (!visibleComponent.newlyVisibleTo.isEmpty()) {
            queueUpdatesFor(ref, commandBuffer, visibleComponent.newlyVisibleTo, armorComponent, utilityComponent);
         }
      }

      private static void queueUpdatesFor(
         @Nonnull Ref<EntityStore> ref,
         @Nonnull CommandBuffer<EntityStore> commandBuffer,
         @Nonnull Map<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> visibleTo,
         @Nullable InventoryComponent.Armor armorComponent,
         @Nullable InventoryComponent.Utility utilityComponent
      ) {
         PlayerSettings playerSettings = commandBuffer.getComponent(ref, PlayerSettings.getComponentType());
         EquipmentUpdate update = InventoryUtils.createEquipmentUpdate(ref, commandBuffer, playerSettings, armorComponent, utilityComponent);

         for (EntityTrackerSystems.EntityViewer viewer : visibleTo.values()) {
            viewer.queueUpdate(ref, update);
         }
      }
   }

   public static class ToolChangeEventSystem extends InventorySystems.InventoryChangeEventSystem<InventoryComponent.Tool> {
      public ToolChangeEventSystem() {
         super(InventoryComponent.Tool.getComponentType());
      }
   }

   public static class UtilityChangeEventSystem extends InventorySystems.InventoryChangeEventSystem<InventoryComponent.Utility> {
      public UtilityChangeEventSystem() {
         super(InventoryComponent.Utility.getComponentType());
      }
   }
}
