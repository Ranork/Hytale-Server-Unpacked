package com.hypixel.hytale.server.core.modules.entity.item;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.protocol.ItemUpdate;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.model.BlockyModelBoundsParser;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.DynamicLight;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PropComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemSystems {
   public static class EnsureRequiredComponents extends HolderSystem<EntityStore> {
      private static final ComponentType<EntityStore, ItemComponent> ITEM_COMPONENT_TYPE = ItemComponent.getComponentType();

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return ITEM_COMPONENT_TYPE;
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         if (!holder.getArchetype().contains(NetworkId.getComponentType())) {
            holder.addComponent(NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));
         }

         holder.ensureComponent(ItemPhysicsComponent.getComponentType());
         if (!holder.getArchetype().contains(BoundingBox.getComponentType())) {
            ModelComponent modelComponent = holder.getComponent(ModelComponent.getComponentType());
            Box modelBox = modelComponent != null && modelComponent.getModel() != null ? modelComponent.getModel().getBoundingBox() : null;
            if (modelBox == null && holder.getArchetype().contains(PropComponent.getComponentType())) {
               modelBox = resolveItemBounds(holder);
            }

            BoundingBox box = new BoundingBox(modelBox != null ? modelBox : Box.horizontallyCentered(0.5, 0.5, 0.5));
            if (modelBox != null) {
               box.setBaseModelBox(modelBox);
               TransformComponent transform = holder.getComponent(TransformComponent.getComponentType());
               if (transform != null) {
                  Rotation3f rotation = transform.getRotation();
                  box.applyRotation(rotation.pitch(), rotation.yaw(), rotation.roll());
               }
            }

            holder.putComponent(BoundingBox.getComponentType(), box);
         }

         ItemComponent itemComponent = holder.getComponent(ItemComponent.getComponentType());

         assert itemComponent != null;

         ColorLight itemDynamicLight = itemComponent.computeDynamicLight();
         if (itemDynamicLight != null) {
            holder.putComponent(DynamicLight.getComponentType(), new DynamicLight(itemDynamicLight));
         }
      }

      @Nullable
      private static Box resolveItemBounds(@Nonnull Holder<EntityStore> holder) {
         ItemComponent itemComponent = holder.getComponent(ITEM_COMPONENT_TYPE);
         if (itemComponent == null) {
            return null;
         } else {
            ItemStack itemStack = itemComponent.getItemStack();
            if (itemStack == null) {
               return null;
            } else {
               Item item = Item.getAssetMap().getAsset(itemStack.getItemId());
               if (item == null) {
                  return null;
               } else {
                  String modelId = item.getModel();
                  if (modelId != null) {
                     ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(modelId);
                     if (modelAsset != null) {
                        return modelAsset.getBoundingBox();
                     }
                  }

                  if (modelId != null) {
                     Box blockyBounds = BlockyModelBoundsParser.computeBounds(modelId);
                     if (blockyBounds != null) {
                        return blockyBounds;
                     }
                  }

                  return null;
               }
            }
         }
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }
   }

   public static class TrackerSystem extends EntityTickingSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public TrackerSystem(@Nonnull ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType) {
         this.visibleComponentType = visibleComponentType;
         this.query = Query.and(visibleComponentType, ItemComponent.getComponentType());
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

         ItemComponent itemComponent = archetypeChunk.getComponent(index, ItemComponent.getComponentType());

         assert itemComponent != null;

         float entityScale = 0.0F;
         EntityScaleComponent entityScaleComponent = archetypeChunk.getComponent(index, EntityScaleComponent.getComponentType());
         if (entityScaleComponent != null) {
            entityScale = entityScaleComponent.getScale();
         }

         boolean scaleOutdated = false;
         if (entityScaleComponent != null) {
            scaleOutdated = entityScaleComponent.consumeNetworkOutdated();
         }

         if (itemComponent.consumeNetworkOutdated() || scaleOutdated) {
            queueUpdatesFor(archetypeChunk.getReferenceTo(index), itemComponent, entityScale, visibleComponent.visibleTo);
         } else if (!visibleComponent.newlyVisibleTo.isEmpty()) {
            queueUpdatesFor(archetypeChunk.getReferenceTo(index), itemComponent, entityScale, visibleComponent.newlyVisibleTo);
         }
      }

      private static void queueUpdatesFor(
         @Nonnull Ref<EntityStore> ref,
         @Nonnull ItemComponent item,
         float entityScale,
         @Nonnull Map<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> visibleTo
      ) {
         ItemStack itemStack = item.getItemStack();
         ItemUpdate update = new ItemUpdate(itemStack != null ? itemStack.toPacket() : null, entityScale);

         for (EntityTrackerSystems.EntityViewer viewer : visibleTo.values()) {
            viewer.queueUpdate(ref, update);
         }
      }
   }
}
