package com.hypixel.hytale.builtin.adventure.objectives.config.markerarea;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import javax.annotation.Nonnull;

public class ObjectiveLocationAreaBox extends ObjectiveLocationMarkerArea {
   @Nonnull
   public static final BuilderCodec<ObjectiveLocationAreaBox> CODEC = BuilderCodec.builder(ObjectiveLocationAreaBox.class, ObjectiveLocationAreaBox::new)
      .append(
         new KeyedCodec<>("EntryBox", Box.CODEC),
         (objectiveLocationAreaBox, box) -> objectiveLocationAreaBox.entryArea = box,
         objectiveLocationAreaBox -> objectiveLocationAreaBox.entryArea
      )
      .addValidator(Validators.nonNull())
      .add()
      .<Box>append(
         new KeyedCodec<>("ExitBox", Box.CODEC),
         (objectiveLocationAreaBox, box) -> objectiveLocationAreaBox.exitArea = box,
         objectiveLocationAreaBox -> objectiveLocationAreaBox.exitArea
      )
      .addValidator(Validators.nonNull())
      .add()
      .afterDecode(ObjectiveLocationAreaBox::computeAreaBoxes)
      .build();
   @Nonnull
   private static final Box DEFAULT_ENTRY_BOX = new Box(-5.0, -5.0, -5.0, 5.0, 5.0, 5.0);
   @Nonnull
   private static final Box DEFAULT_EXIT_BOX = new Box(-10.0, -10.0, -10.0, 10.0, 10.0, 10.0);
   private Box entryArea;
   private Box exitArea;

   public ObjectiveLocationAreaBox(Box entryBox, Box exitBox) {
      this.entryArea = entryBox;
      this.exitArea = exitBox;
      this.computeAreaBoxes();
   }

   protected ObjectiveLocationAreaBox() {
      this(DEFAULT_ENTRY_BOX, DEFAULT_EXIT_BOX);
   }

   public Box getEntryArea() {
      return this.entryArea;
   }

   public Box getExitArea() {
      return this.exitArea;
   }

   @Override
   public void getPlayersInEntryArea(
      @Nonnull SpatialResource<Ref<EntityStore>, EntityStore> spatialComponent, @Nonnull List<Ref<EntityStore>> results, @Nonnull Vector3d markerPosition
   ) {
      getPlayersInArea(spatialComponent, results, markerPosition, this.entryArea);
   }

   @Override
   public void getPlayersInExitArea(
      @Nonnull SpatialResource<Ref<EntityStore>, EntityStore> spatialComponent, @Nonnull List<Ref<EntityStore>> results, @Nonnull Vector3d markerPosition
   ) {
      getPlayersInArea(spatialComponent, results, markerPosition, this.exitArea);
   }

   @Override
   public boolean hasPlayerInExitArea(
      @Nonnull SpatialResource<Ref<EntityStore>, EntityStore> spatialComponent,
      @Nonnull ComponentType<EntityStore, PlayerRef> playerRefComponentType,
      @Nonnull Vector3d markerPosition,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      Ref<EntityStore> reference = spatialComponent.getSpatialStructure().closest(markerPosition);
      if (reference == null) {
         return false;
      } else {
         TransformComponent transformComponent = commandBuffer.getComponent(reference, TransformComponent.getComponentType());

         assert transformComponent != null;

         return this.exitArea.containsPosition(markerPosition, transformComponent.getPosition());
      }
   }

   @Override
   public boolean isPlayerInEntryArea(@Nonnull Vector3d playerPosition, @Nonnull Vector3d markerPosition) {
      return this.entryArea.containsPosition(markerPosition, playerPosition);
   }

   @Nonnull
   @Override
   public ObjectiveLocationMarkerArea getRotatedArea(float yaw, float pitch) {
      float snappedYaw = Math.round(yaw / (float) (Math.PI / 2)) * (float) (Math.PI / 2);
      if (Math.abs(snappedYaw % (float) (Math.PI * 2)) > (float) (Math.PI / 4)) {
         Box entry = this.entryArea.clone().rotateY(snappedYaw).normalize();
         Box exit = this.exitArea.clone().rotateY(snappedYaw).normalize();
         return new ObjectiveLocationAreaBox(entry, exit);
      } else {
         return this;
      }
   }

   @Override
   protected void computeAreaBoxes() {
      this.entryAreaBox = this.entryArea;
      this.exitAreaBox = this.exitArea;
   }

   private static void getPlayersInArea(
      @Nonnull SpatialResource<Ref<EntityStore>, EntityStore> spatialComponent,
      @Nonnull List<Ref<EntityStore>> results,
      @Nonnull Vector3d markerPosition,
      @Nonnull Box box
   ) {
      spatialComponent.getSpatialStructure().collect(markerPosition, box.getMaximumExtent(), results);
   }

   @Nonnull
   @Override
   public String toString() {
      return "ObjectiveLocationAreaBox{} " + super.toString();
   }
}
