package com.hypixel.hytale.server.spawning.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import com.hypixel.hytale.server.spawning.assets.spawnmarker.config.SpawnMarker;
import com.hypixel.hytale.server.spawning.spawnmarkers.SpawnMarkerEntity;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import javax.annotation.Nonnull;

public class TriggerSpawnMarkersInteraction extends SimpleInstantInteraction {
   public static final BuilderCodec<TriggerSpawnMarkersInteraction> CODEC = BuilderCodec.builder(
         TriggerSpawnMarkersInteraction.class, TriggerSpawnMarkersInteraction::new, SimpleInstantInteraction.CODEC
      )
      .appendInherited(new KeyedCodec<>("MarkerType", Codec.STRING), (o, v) -> o.markerType = v, o -> o.markerType, (o, p) -> o.markerType = p.markerType)
      .addValidator(SpawnMarker.VALIDATOR_CACHE.getValidator())
      .documentation("The manual spawn marker type to trigger. If omitted, will trigger all manual spawners.")
      .add()
      .<Double>appendInherited(new KeyedCodec<>("Range", Codec.DOUBLE), (o, v) -> o.range = v, o -> o.range, (o, p) -> o.range = p.range)
      .addValidator(Validators.greaterThan(0.0))
      .documentation("Range within which to trigger spawn markers.")
      .add()
      .<Integer>appendInherited(new KeyedCodec<>("Count", Codec.INTEGER), (o, v) -> o.count = v, o -> o.count, (o, p) -> o.count = p.count)
      .addValidator(Validators.greaterThanOrEqual(0))
      .documentation("Max number of spawn markers to activate. Set to 0 to activate all spawn markers.")
      .add()
      .afterDecode(i -> i.rangeSquared = i.range * i.range)
      .build();
   private String markerType;
   private double range = 10.0;
   private double rangeSquared;
   private int count;

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
      Ref<EntityStore> self = context.getEntity();
      Vector3d position = commandBuffer.getComponent(self, TransformComponent.getComponentType()).getPosition();
      SpatialResource<Ref<EntityStore>, EntityStore> spatialResource = commandBuffer.getResource(SpawningPlugin.get().getSpawnMarkerSpatialResource());
      ObjectArrayList<Ref<EntityStore>> spawners = new ObjectArrayList();
      spatialResource.getSpatialStructure().collect(position, (int)this.range + 1, spawners);
      if (this.count == 0) {
         for (int i = 0; i < spawners.size(); i++) {
            Ref<EntityStore> spawnMarkerRef = this.filterMarker((Ref<EntityStore>)spawners.get(i), position, commandBuffer);
            if (spawnMarkerRef != null) {
               SpawnMarkerEntity spawnMarkerEntityComponent = commandBuffer.getComponent(spawnMarkerRef, SpawnMarkerEntity.getComponentType());

               assert spawnMarkerEntityComponent != null;

               commandBuffer.run(store -> spawnMarkerEntityComponent.trigger(spawnMarkerRef, store));
            }
         }
      } else {
         ObjectArrayList<Ref<EntityStore>> triggerList = new ObjectArrayList();
         RandomExtra.reservoirSample(
            spawners,
            (reference, _this, _commandBuffer) -> _this.filterMarker(reference, position, _commandBuffer),
            this.count,
            triggerList,
            this,
            commandBuffer
         );

         for (int ix = 0; ix < triggerList.size(); ix++) {
            Ref<EntityStore> spawnMarkerRef = (Ref<EntityStore>)triggerList.get(ix);
            SpawnMarkerEntity spawnMarkerEntityComponent = commandBuffer.getComponent(spawnMarkerRef, SpawnMarkerEntity.getComponentType());

            assert spawnMarkerEntityComponent != null;

            commandBuffer.run(store -> spawnMarkerEntityComponent.trigger(spawnMarkerRef, store));
         }
      }
   }

   protected Ref<EntityStore> filterMarker(@Nonnull Ref<EntityStore> targetRef, @Nonnull Vector3d position, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
      if (!targetRef.isValid()) {
         return null;
      } else {
         TransformComponent targetTransformComponent = commandBuffer.getComponent(targetRef, TransformComponent.getComponentType());

         assert targetTransformComponent != null;

         Vector3d targetPosition = targetTransformComponent.getPosition();
         SpawnMarkerEntity targetMarkerEntityComponent = commandBuffer.getComponent(targetRef, SpawnMarkerEntity.getComponentType());
         return targetMarkerEntityComponent == null
               || !targetMarkerEntityComponent.isManualTrigger()
               || !(position.distanceSquaredTo(targetPosition) <= this.rangeSquared)
               || this.markerType != null && !this.markerType.equals(targetMarkerEntityComponent.getSpawnMarkerId())
            ? null
            : targetRef;
      }
   }
}
