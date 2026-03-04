package com.hypixel.hytale.builtin.adventure.objectives.markers.objectivelocation;

import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import com.hypixel.hytale.builtin.adventure.objectives.config.ObjectiveLocationMarkerAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.markerarea.ObjectiveLocationMarkerArea;
import com.hypixel.hytale.builtin.adventure.objectives.config.triggercondition.ObjectiveLocationTriggerCondition;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.assets.UntrackObjective;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ObjectiveLocationMarker implements Component<EntityStore> {
   @Nonnull
   public static final BuilderCodec<ObjectiveLocationMarker> CODEC = BuilderCodec.builder(ObjectiveLocationMarker.class, ObjectiveLocationMarker::new)
      .append(
         new KeyedCodec<>("ObjectiveLocationMarkerId", Codec.STRING),
         (objectiveLocationMarkerEntity, s) -> objectiveLocationMarkerEntity.objectiveLocationMarkerId = s,
         objectiveLocationMarkerEntity -> objectiveLocationMarkerEntity.objectiveLocationMarkerId
      )
      .addValidator(Validators.nonNull())
      .addValidator(ObjectiveLocationMarkerAsset.VALIDATOR_CACHE.getValidator())
      .add()
      .append(
         new KeyedCodec<>("ActiveObjectiveUUID", Codec.UUID_BINARY),
         (objectiveLocationMarkerEntity, uuid) -> objectiveLocationMarkerEntity.activeObjectiveUUID = uuid,
         objectiveLocationMarkerEntity -> objectiveLocationMarkerEntity.activeObjectiveUUID
      )
      .add()
      .build();
   protected String objectiveLocationMarkerId;
   protected UUID activeObjectiveUUID;
   protected ObjectiveLocationMarkerArea area;
   protected int[] environmentIndexes;
   protected ObjectiveLocationTriggerCondition[] triggerConditions;
   @Nullable
   private Objective activeObjective;
   private UntrackObjective untrackPacket;

   public static ComponentType<EntityStore, ObjectiveLocationMarker> getComponentType() {
      return ObjectivePlugin.get().getObjectiveLocationMarkerComponentType();
   }

   public ObjectiveLocationMarker() {
   }

   public ObjectiveLocationMarker(String objectiveLocationMarkerId) {
      this.objectiveLocationMarkerId = objectiveLocationMarkerId;
   }

   public void setObjectiveLocationMarkerId(String objectiveLocationMarkerId) {
      this.objectiveLocationMarkerId = objectiveLocationMarkerId;
   }

   public void setActiveObjectiveUUID(UUID activeObjectiveUUID) {
      this.activeObjectiveUUID = activeObjectiveUUID;
   }

   @Nullable
   public Objective getActiveObjective() {
      return this.activeObjective;
   }

   public void setActiveObjective(@Nonnull Objective activeObjective) {
      this.activeObjective = activeObjective;
   }

   public String getObjectiveLocationMarkerId() {
      return this.objectiveLocationMarkerId;
   }

   public UntrackObjective getUntrackPacket() {
      return this.untrackPacket;
   }

   public void setUntrackPacket(@Nonnull UntrackObjective untrackPacket) {
      this.untrackPacket = untrackPacket;
   }

   public ObjectiveLocationMarkerArea getArea() {
      return this.area;
   }

   public void updateLocationMarkerValues(@Nonnull ObjectiveLocationMarkerAsset objectiveLocationMarkerAsset, float yaw, @Nonnull Store<EntityStore> store) {
      if (this.activeObjective != null
         && !this.activeObjective.getObjectiveId().equals(objectiveLocationMarkerAsset.getObjectiveTypeSetup().getObjectiveIdToStart())) {
         ObjectivePlugin.get().cancelObjective(this.activeObjectiveUUID, store);
         this.activeObjective = null;
      }

      this.environmentIndexes = objectiveLocationMarkerAsset.getEnvironmentIndexes();
      this.area = objectiveLocationMarkerAsset.getArea().getRotatedArea(yaw, 0.0F);
      this.triggerConditions = objectiveLocationMarkerAsset.getTriggerConditions();
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      ObjectiveLocationMarker marker = new ObjectiveLocationMarker(this.objectiveLocationMarkerId);
      marker.activeObjectiveUUID = this.activeObjectiveUUID;
      marker.area = this.area;
      marker.environmentIndexes = this.environmentIndexes;
      marker.triggerConditions = this.triggerConditions;
      marker.activeObjective = this.activeObjective;
      marker.untrackPacket = this.untrackPacket;
      return marker;
   }
}
