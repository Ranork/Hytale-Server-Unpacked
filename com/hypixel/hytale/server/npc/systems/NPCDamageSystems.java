package com.hypixel.hytale.server.npc.systems;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.asset.type.gameplay.DeathConfig;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import com.hypixel.hytale.server.core.modules.item.ItemModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.blackboard.Blackboard;
import com.hypixel.hytale.server.npc.blackboard.view.event.entity.EntityEventType;
import com.hypixel.hytale.server.npc.blackboard.view.event.entity.EntityEventView;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.util.DamageData;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NPCDamageSystems {
   public static class DamageDealtSystem extends DamageEventSystem {
      @Nullable
      @Override
      public SystemGroup<EntityStore> getGroup() {
         return DamageModule.get().getInspectDamageGroup();
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return Archetype.empty();
      }

      public void handle(
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer,
         @Nonnull Damage damage
      ) {
         if (damage.getSource() instanceof Damage.EntitySource entitySource) {
            Ref<EntityStore> sourceRef = entitySource.getRef();
            if (sourceRef.isValid()) {
               NPCEntity sourceNpcComponent = commandBuffer.getComponent(sourceRef, NPCEntity.getComponentType());
               if (sourceNpcComponent != null) {
                  sourceNpcComponent.getDamageData().onInflictedDamage(archetypeChunk.getReferenceTo(index), damage.getAmount());
               }
            }
         }
      }
   }

   public static class DamageReceivedEventViewSystem extends DamageEventSystem {
      @Nonnull
      private final ComponentType<EntityStore, Player> playerComponentType = Player.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, TransformComponent> transformComponentType = TransformComponent.getComponentType();
      @Nonnull
      private final ResourceType<EntityStore, Blackboard> blackboardResourceType = Blackboard.getResourceType();
      @Nonnull
      private final Query<EntityStore> query = Query.and(Query.or(NPCEntity.getComponentType(), this.playerComponentType), this.transformComponentType);

      @Nullable
      @Override
      public SystemGroup<EntityStore> getGroup() {
         return DamageModule.get().getInspectDamageGroup();
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      public void handle(
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer,
         @Nonnull Damage damage
      ) {
         TransformComponent transformComponent = archetypeChunk.getComponent(index, this.transformComponentType);

         assert transformComponent != null;

         Blackboard blackboard = commandBuffer.getResource(this.blackboardResourceType);
         EntityEventView view = blackboard.getView(
            EntityEventView.class, ChunkUtil.chunkCoordinate(transformComponent.getPosition().x), ChunkUtil.chunkCoordinate(transformComponent.getPosition().z)
         );
         if (damage.getSource() instanceof Damage.EntitySource entitySource) {
            Ref<EntityStore> sourceRef = entitySource.getRef();
            if (sourceRef.isValid()) {
               Player sourcePlayerComponent = commandBuffer.getComponent(sourceRef, Player.getComponentType());
               if (sourcePlayerComponent != null && sourcePlayerComponent.getGameMode() == GameMode.Creative) {
                  PlayerSettings playerSettingsComponent = commandBuffer.getComponent(sourceRef, PlayerSettings.getComponentType());
                  if (playerSettingsComponent == null || !playerSettingsComponent.creativeSettings().allowNPCDetection()) {
                     return;
                  }
               }

               view.processAttackedEvent(archetypeChunk.getReferenceTo(index), sourceRef, commandBuffer, EntityEventType.DAMAGE);
            }
         }
      }
   }

   public static class DamageReceivedSystem extends DamageEventSystem {
      @Nonnull
      private final Query<EntityStore> query = NPCEntity.getComponentType();

      @Nullable
      @Override
      public SystemGroup<EntityStore> getGroup() {
         return DamageModule.get().getInspectDamageGroup();
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      public void handle(
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer,
         @Nonnull Damage damage
      ) {
         NPCEntity npcComponent = archetypeChunk.getComponent(index, NPCEntity.getComponentType());

         assert npcComponent != null;

         DamageData damageData = npcComponent.getDamageData();
         damageData.onSufferedDamage(commandBuffer, damage);
      }
   }

   public static class DropDeathItems extends DeathSystems.OnDeathSystem {
      @Nonnull
      private static final Query<EntityStore> QUERY = Query.and(
         NPCEntity.getComponentType(), TransformComponent.getComponentType(), HeadRotation.getComponentType(), Query.not(Player.getComponentType())
      );

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return QUERY;
      }

      public void onComponentAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         if (component.getItemsLossMode() == DeathConfig.ItemsLossMode.ALL) {
            NPCEntity npcComponent = commandBuffer.getComponent(ref, NPCEntity.getComponentType());

            assert npcComponent != null;

            Role role = npcComponent.getRole();
            if (role != null) {
               List<ItemStack> itemsToDrop = new ObjectArrayList();
               if (role.isPickupDropOnDeath()) {
                  Inventory inventory = npcComponent.getInventory();
                  itemsToDrop.addAll(inventory.getStorage().dropAllItemStacks());
               }

               String dropListId = role.getDropListId();
               if (dropListId != null) {
                  ItemModule itemModule = ItemModule.get();
                  if (itemModule.isEnabled()) {
                     List<ItemStack> randomItemsToDrop = itemModule.getRandomItemDrops(dropListId);
                     itemsToDrop.addAll(randomItemsToDrop);
                  }
               }

               if (!itemsToDrop.isEmpty()) {
                  TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

                  assert transformComponent != null;

                  Vector3d position = transformComponent.getPosition();
                  HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());

                  assert headRotationComponent != null;

                  Vector3f headRotation = headRotationComponent.getRotation();
                  Vector3d dropPosition = position.clone().add(0.0, 1.0, 0.0);
                  Holder<EntityStore>[] drops = ItemComponent.generateItemDrops(store, itemsToDrop, dropPosition, headRotation.clone());
                  commandBuffer.addEntities(drops, AddReason.SPAWN);
               }
            }
         }
      }
   }

   public static class FilterDamageSystem extends DamageEventSystem {
      @Nonnull
      private final Query<EntityStore> query = NPCEntity.getComponentType();

      @Nullable
      @Override
      public SystemGroup<EntityStore> getGroup() {
         return DamageModule.get().getFilterDamageGroup();
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      public void handle(
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer,
         @Nonnull Damage damage
      ) {
         NPCEntity npcComponent = archetypeChunk.getComponent(index, NPCEntity.getComponentType());

         assert npcComponent != null;

         if (damage.getSource() instanceof Damage.EntitySource entitySource) {
            Ref<EntityStore> sourceRef = entitySource.getRef();
            if (sourceRef.isValid()) {
               if (!npcComponent.getCanCauseDamage(sourceRef, commandBuffer)) {
                  damage.setCancelled(true);
               }
            }
         }
      }
   }
}
