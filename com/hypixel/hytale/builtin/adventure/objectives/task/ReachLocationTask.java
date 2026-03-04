package com.hypixel.hytale.builtin.adventure.objectives.task;

import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.builtin.adventure.objectives.config.task.ObjectiveTaskAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.task.ReachLocationTaskAsset;
import com.hypixel.hytale.builtin.adventure.objectives.markers.ObjectiveTaskMarker;
import com.hypixel.hytale.builtin.adventure.objectives.markers.reachlocation.ReachLocationMarker;
import com.hypixel.hytale.builtin.adventure.objectives.transaction.TransactionRecord;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ReachLocationTask extends ObjectiveTask {
   @Nonnull
   public static final BuilderCodec<ReachLocationTask> CODEC = BuilderCodec.builder(ReachLocationTask.class, ReachLocationTask::new, BASE_CODEC)
      .append(
         new KeyedCodec<>("Completed", Codec.BOOLEAN),
         (reachLocationTask, aBoolean) -> reachLocationTask.completed = aBoolean,
         reachLocationTask -> reachLocationTask.completed
      )
      .add()
      .append(
         new KeyedCodec<>("MarkerLoaded", Codec.BOOLEAN),
         (reachLocationTask, transactionRecord) -> reachLocationTask.markerLoaded = transactionRecord,
         reachLocationTask -> reachLocationTask.markerLoaded
      )
      .add()
      .build();
   @Nonnull
   public static String MARKER_ICON = "Home.png";
   @Nonnull
   private static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
   @Nonnull
   private static final ComponentType<EntityStore, ReachLocationMarker> REACH_LOCATION_MARKER_COMPONENT_TYPE = ReachLocationMarker.getComponentType();
   private boolean completed;
   private boolean markerLoaded;

   public ReachLocationTask(@Nonnull ObjectiveTaskAsset asset, int taskSetIndex, int taskIndex) {
      super(asset, taskSetIndex, taskIndex);
   }

   protected ReachLocationTask() {
   }

   @Override
   public boolean checkCompletion() {
      return this.completed;
   }

   @Nonnull
   public ReachLocationTaskAsset getAsset() {
      return (ReachLocationTaskAsset)this.asset;
   }

   @Nonnull
   private String getMarkerId(@Nonnull Objective objective) {
      return String.format("ReachLocation_%s_%d", objective.getObjectiveUUID(), this.taskIndex);
   }

   @Nullable
   @Override
   protected TransactionRecord[] setup0(@Nonnull Objective objective, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      if (!this.markers.isEmpty()) {
         return null;
      } else {
         String targetLocationId = this.getAsset().getTargetLocationId();
         List<Ref<EntityStore>> reachLocationMarkerEntities = new ObjectArrayList();
         store.forEachChunk(REACH_LOCATION_MARKER_COMPONENT_TYPE, (archetypeChunk, componentStoreCommandBuffer) -> {
            for (int index = 0; index < archetypeChunk.size(); index++) {
               ReachLocationMarker reachLocationMarkerComponent = archetypeChunk.getComponent(index, REACH_LOCATION_MARKER_COMPONENT_TYPE);
               if (reachLocationMarkerComponent != null && reachLocationMarkerComponent.getMarkerId().equals(targetLocationId)) {
                  reachLocationMarkerEntities.add(archetypeChunk.getReferenceTo(index));
               }
            }
         });
         if (!reachLocationMarkerEntities.isEmpty()) {
            Vector3d currentLocation = objective.getPosition(store);
            Ref<EntityStore> closestMarker = reachLocationMarkerEntities.getFirst();
            TransformComponent closestMarkerTransformComponent = store.getComponent(closestMarker, TRANSFORM_COMPONENT_TYPE);

            assert closestMarkerTransformComponent != null;

            ReachLocationMarker closestMarkerReachComponent = store.getComponent(closestMarker, REACH_LOCATION_MARKER_COMPONENT_TYPE);

            assert closestMarkerReachComponent != null;

            Vector3d closestPosition = closestMarkerTransformComponent.getPosition();
            double shortestDistance = closestPosition.distanceSquaredTo(currentLocation);
            String closestLocationName = closestMarkerReachComponent.getLocationName();

            for (int i = 1; i < reachLocationMarkerEntities.size(); i++) {
               Ref<EntityStore> markerEntityReference = reachLocationMarkerEntities.get(i);
               TransformComponent markerTransformComponent = store.getComponent(markerEntityReference, TRANSFORM_COMPONENT_TYPE);

               assert markerTransformComponent != null;

               ReachLocationMarker markerReachLocationComponent = store.getComponent(markerEntityReference, REACH_LOCATION_MARKER_COMPONENT_TYPE);

               assert markerReachLocationComponent != null;

               Vector3d pos = markerTransformComponent.getPosition();
               double distance = pos.distanceSquaredTo(currentLocation);
               String locationName = markerReachLocationComponent.getLocationName();
               if (distance < shortestDistance && locationName != null) {
                  shortestDistance = distance;
                  closestPosition = pos;
                  closestLocationName = locationName;
               }
            }

            if (closestLocationName != null) {
               ObjectiveTaskMarker marker = new ObjectiveTaskMarker(
                  this.getMarkerId(objective), new Transform(closestPosition), MARKER_ICON, Message.raw(closestLocationName)
               );
               this.addMarker(marker);
               this.markerLoaded = true;
               return null;
            }
         }

         return null;
      }
   }

   public void setupMarker(
      @Nonnull Objective objective,
      @Nonnull ReachLocationMarker locationMarkerEntity,
      @Nonnull Vector3d position,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      if (!this.markerLoaded) {
         String markerId = locationMarkerEntity.getMarkerId();
         if (markerId.equals(this.getAsset().getTargetLocationId())) {
            String locationName = locationMarkerEntity.getLocationName();
            if (locationName != null) {
               ObjectiveTaskMarker marker = new ObjectiveTaskMarker(
                  this.getMarkerId(objective), new Transform(position), MARKER_ICON, Message.raw(locationName)
               );
               this.addMarker(marker);
               this.markerLoaded = true;
            }
         }
      }
   }

   @Nonnull
   public com.hypixel.hytale.protocol.ObjectiveTask toPacket(@Nonnull Objective objective) {
      com.hypixel.hytale.protocol.ObjectiveTask packet = new com.hypixel.hytale.protocol.ObjectiveTask();
      packet.taskDescriptionKey = Message.translation(this.asset.getDescriptionKey(objective.getObjectiveId(), this.taskSetIndex, this.taskIndex))
         .getFormattedMessage();
      packet.currentCompletion = this.completed ? 1 : 0;
      packet.completionNeeded = 1;
      return packet;
   }

   public void onPlayerReachLocationMarker(
      @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull String locationMarkerId, @Nonnull Objective objective
   ) {
      if (locationMarkerId.equals(this.getAsset().getTargetLocationId())) {
         UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());

         assert uuidComponent != null;

         if (objective.getActivePlayerUUIDs().contains(uuidComponent.getUuid())) {
            if (this.areTaskConditionsFulfilled(store, ref, null)) {
               this.completed = true;
               objective.markDirty();
               this.sendUpdateObjectiveTaskPacket(objective);
               this.consumeTaskConditions(store, ref, objective.getActivePlayerUUIDs());
               this.complete(objective, store);
               objective.checkTaskSetCompletion(store);
            }
         }
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "ReachLocationTask{completed=" + this.completed + "} " + super.toString();
   }
}
