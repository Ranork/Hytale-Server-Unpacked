package com.hypixel.hytale.builtin.triggervolumes.system;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEventType;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3i;

public final class TriggerVolumeBlockEventSystems {
   private TriggerVolumeBlockEventSystems() {
   }

   public static final class BlockBroken extends EntityEventSystem<EntityStore, BreakBlockEvent> {
      @Nonnull
      private final ResourceType<EntityStore, TriggerVolumeManager> managerResourceType;

      public BlockBroken(@Nonnull ResourceType<EntityStore, TriggerVolumeManager> managerResourceType) {
         super(BreakBlockEvent.class);
         this.managerResourceType = managerResourceType;
      }

      public void handle(
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer,
         @Nonnull BreakBlockEvent event
      ) {
         TriggerVolumeManager manager = store.getResource(this.managerResourceType);
         if (manager != null) {
            Ref<EntityStore> actorRef = archetypeChunk.getReferenceTo(index);
            UUIDComponent uuidComponent = store.getComponent(actorRef, UUIDComponent.getComponentType());
            if (uuidComponent != null) {
               Vector3i targetBlock = event.getTargetBlock();
               manager.enqueueBlockEvent(
                  TriggerEventType.BLOCK_BROKEN,
                  actorRef,
                  uuidComponent.getUuid(),
                  new Vector3d(targetBlock.x() + 0.5, targetBlock.y() + 0.5, targetBlock.z() + 0.5),
                  event.getBlockType().getId()
               );
            }
         }
      }

      @Nullable
      @Override
      public Query<EntityStore> getQuery() {
         return Archetype.empty();
      }
   }

   public static final class BlockPlaced extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
      @Nonnull
      private final ResourceType<EntityStore, TriggerVolumeManager> managerResourceType;

      public BlockPlaced(@Nonnull ResourceType<EntityStore, TriggerVolumeManager> managerResourceType) {
         super(PlaceBlockEvent.class);
         this.managerResourceType = managerResourceType;
      }

      public void handle(
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer,
         @Nonnull PlaceBlockEvent event
      ) {
         ItemStack itemStack = event.getItemInHand();
         if (itemStack != null && itemStack.getItem() != null && itemStack.getItem().hasBlockType()) {
            this.enqueue(
               index,
               archetypeChunk,
               store,
               event.getTargetBlock().x(),
               event.getTargetBlock().y(),
               event.getTargetBlock().z(),
               itemStack.getItem().getBlockId(),
               TriggerEventType.BLOCK_PLACED
            );
         }
      }

      @Nullable
      @Override
      public Query<EntityStore> getQuery() {
         return Archetype.empty();
      }

      private void enqueue(
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         int blockX,
         int blockY,
         int blockZ,
         @Nonnull String blockId,
         @Nonnull TriggerEventType eventType
      ) {
         TriggerVolumeManager manager = store.getResource(this.managerResourceType);
         if (manager != null) {
            Ref<EntityStore> actorRef = archetypeChunk.getReferenceTo(index);
            UUIDComponent uuidComponent = store.getComponent(actorRef, UUIDComponent.getComponentType());
            if (uuidComponent != null) {
               manager.enqueueBlockEvent(eventType, actorRef, uuidComponent.getUuid(), new Vector3d(blockX + 0.5, blockY + 0.5, blockZ + 0.5), blockId);
            }
         }
      }
   }
}
