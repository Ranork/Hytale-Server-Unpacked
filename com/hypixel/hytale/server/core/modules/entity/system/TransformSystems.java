package com.hypixel.hytale.server.core.modules.entity.system;

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
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.ModelTransform;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.TransformUpdate;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.PositionUtil;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TransformSystems {
   public static class EntityTrackerUpdate extends EntityTickingSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType = EntityTrackerSystems.Visible.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, TransformComponent> transformComponentType = TransformComponent.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, HeadRotation> headRotationComponentType = HeadRotation.getComponentType();
      @Nonnull
      private final Query<EntityStore> query = Query.and(this.visibleComponentType, this.transformComponentType);

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

         TransformComponent transformComponent = archetypeChunk.getComponent(index, this.transformComponentType);

         assert transformComponent != null;

         HeadRotation headRotationComponent = archetypeChunk.getComponent(index, this.headRotationComponentType);
         ModelTransform sentTransform = transformComponent.getSentTransform();
         Vector3d position = transformComponent.getPosition();
         Vector3f headRotation = headRotationComponent != null ? headRotationComponent.getRotation() : Vector3f.ZERO;
         Vector3f bodyRotation = transformComponent.getRotation();
         Position sentPosition = sentTransform.position;
         Direction sentLookOrientation = sentTransform.lookOrientation;
         Direction sentBodyOrientation = sentTransform.bodyOrientation;
         if (!PositionUtil.equals(position, sentPosition)
            || !PositionUtil.equals(headRotation, sentLookOrientation)
            || !PositionUtil.equals(bodyRotation, sentBodyOrientation)) {
            PositionUtil.assign(sentPosition, position);
            PositionUtil.assign(sentLookOrientation, headRotation);
            PositionUtil.assign(sentBodyOrientation, bodyRotation);
            queueUpdatesFor(archetypeChunk.getReferenceTo(index), sentTransform, visibleComponent.visibleTo, false);
         } else if (!visibleComponent.newlyVisibleTo.isEmpty()) {
            queueUpdatesFor(archetypeChunk.getReferenceTo(index), sentTransform, visibleComponent.newlyVisibleTo, true);
         }
      }

      private static void queueUpdatesFor(
         @Nonnull Ref<EntityStore> ref,
         @Nonnull ModelTransform sentTransform,
         @Nonnull Map<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> visibleTo,
         boolean newlyVisible
      ) {
         TransformUpdate update = new TransformUpdate(sentTransform);

         for (Entry<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> entry : visibleTo.entrySet()) {
            if (newlyVisible || !ref.equals(entry.getKey())) {
               entry.getValue().queueUpdate(ref, update);
            }
         }
      }
   }

   public static class OnRemove extends HolderSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, TransformComponent> transformComponentType = TransformComponent.getComponentType();

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
         holder.getComponent(this.transformComponentType).setChunkLocation(null, null);
      }

      @Override
      public Query<EntityStore> getQuery() {
         return this.transformComponentType;
      }
   }
}
