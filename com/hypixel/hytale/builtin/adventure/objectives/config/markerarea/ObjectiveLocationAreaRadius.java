package com.hypixel.hytale.builtin.adventure.objectives.config.markerarea;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.VectorSphereUtil;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import javax.annotation.Nonnull;

public class ObjectiveLocationAreaRadius extends ObjectiveLocationMarkerArea {
   @Nonnull
   public static final BuilderCodec<ObjectiveLocationAreaRadius> CODEC = BuilderCodec.builder(
         ObjectiveLocationAreaRadius.class, ObjectiveLocationAreaRadius::new
      )
      .append(
         new KeyedCodec<>("EntryRadius", Codec.INTEGER),
         (objectiveLocationAreaRadius, integer) -> objectiveLocationAreaRadius.entryArea = integer,
         objectiveLocationAreaRadius -> objectiveLocationAreaRadius.entryArea
      )
      .addValidator(Validators.greaterThan(0))
      .add()
      .<Integer>append(
         new KeyedCodec<>("ExitRadius", Codec.INTEGER),
         (objectiveLocationAreaRadius, integer) -> objectiveLocationAreaRadius.exitArea = integer,
         objectiveLocationAreaRadius -> objectiveLocationAreaRadius.exitArea
      )
      .addValidator(Validators.greaterThan(0))
      .add()
      .validator((objectiveLocationAreaRadius, validationResults) -> {
         if (objectiveLocationAreaRadius.exitArea < objectiveLocationAreaRadius.entryArea) {
            validationResults.fail("ExitRadius needs to be greater than EntryRadius");
         }
      })
      .afterDecode(ObjectiveLocationAreaRadius::computeAreaBoxes)
      .build();
   public static final int DEFAULT_ENTRY_RADIUS = 5;
   public static final int DEFAULT_EXIT_RADIUS = 10;
   protected int entryArea;
   protected int exitArea;

   public ObjectiveLocationAreaRadius(int entryRadius, int exitRadius) {
      this.entryArea = entryRadius;
      this.exitArea = exitRadius;
      this.computeAreaBoxes();
   }

   protected ObjectiveLocationAreaRadius() {
      this(5, 10);
   }

   public int getEntryArea() {
      return this.entryArea;
   }

   public int getExitArea() {
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

         return VectorSphereUtil.isInside(
            markerPosition.x, markerPosition.y, markerPosition.z, this.exitArea, this.exitArea, this.exitArea, transformComponent.getPosition()
         );
      }
   }

   @Override
   public boolean isPlayerInEntryArea(@Nonnull Vector3d playerPosition, @Nonnull Vector3d markerPosition) {
      return VectorSphereUtil.isInside(markerPosition.x, markerPosition.y, markerPosition.z, this.entryArea, playerPosition);
   }

   @Override
   protected void computeAreaBoxes() {
      this.entryAreaBox = new Box(-this.entryArea, -this.entryArea, -this.entryArea, this.entryArea, this.entryArea, this.entryArea);
      this.exitAreaBox = new Box(-this.exitArea, -this.exitArea, -this.exitArea, this.exitArea, this.exitArea, this.exitArea);
   }

   private static void getPlayersInArea(
      @Nonnull SpatialResource<Ref<EntityStore>, EntityStore> spatialComponent,
      @Nonnull List<Ref<EntityStore>> results,
      @Nonnull Vector3d markerPosition,
      int radius
   ) {
      spatialComponent.getSpatialStructure().collect(markerPosition, radius, results);
   }

   @Nonnull
   @Override
   public String toString() {
      return "ObjectiveLocationAreaRadius{} " + super.toString();
   }
}
