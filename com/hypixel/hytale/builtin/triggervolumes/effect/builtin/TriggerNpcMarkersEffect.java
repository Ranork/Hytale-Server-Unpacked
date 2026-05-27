package com.hypixel.hytale.builtin.triggervolumes.effect.builtin;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.builtin.triggervolumes.shape.TriggerVolumeShape;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import com.hypixel.hytale.server.spawning.spawnmarkers.SpawnMarkerEntity;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public class TriggerNpcMarkersEffect extends TriggerEffect {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   public static final BuilderCodec<TriggerNpcMarkersEffect> CODEC = BuilderCodec.builder(
         TriggerNpcMarkersEffect.class, TriggerNpcMarkersEffect::new, BASE_CODEC
      )
      .append(new KeyedCodec<>("MarkerType", Codec.STRING, false), (effect, markerType) -> effect.markerType = markerType, effect -> effect.markerType)
      .add()
      .append(new KeyedCodec<>("Range", Codec.DOUBLE, false), (effect, range) -> effect.range = range, effect -> effect.range > 0.0 ? effect.range : null)
      .add()
      .append(new KeyedCodec<>("MatchTag", Codec.STRING, false), (effect, matchTag) -> effect.matchTag = matchTag, effect -> effect.matchTag)
      .add()
      .append(new KeyedCodec<>("Radius", Codec.DOUBLE, false), (effect, radius) -> effect.radius = radius, effect -> effect.radius > 0.0 ? effect.radius : null)
      .add()
      .append(
         new KeyedCodec<>("Center", new EnumCodec<>(TaggedVolumeEffectUtil.Center.class), false),
         (effect, center) -> effect.center = center,
         effect -> effect.center
      )
      .add()
      .build();
   @Nullable
   private String markerType;
   private double range;
   @Nullable
   private String matchTag;
   private double radius;
   @Nonnull
   private TaggedVolumeEffectUtil.Center center = TaggedVolumeEffectUtil.Center.VOLUME;

   @Override
   public void execute(@Nonnull TriggerContext context) {
      Store<EntityStore> store = context.getStore();
      SpatialResource<Ref<EntityStore>, EntityStore> spatialResource = store.getResource(SpawningPlugin.get().getSpawnMarkerSpatialResource());
      if (spatialResource != null) {
         List<VolumeEntry> areaVolumes = this.resolveAreaVolumes(context);
         if (areaVolumes.isEmpty()) {
            LOGGER.at(Level.FINE).log("TriggerNpcMarkers: no area volumes matched for volume '%s' (MatchTag: %s)", context.getVolume().getId(), this.matchTag);
         } else {
            ReferenceArrayList<Ref<EntityStore>> candidates = new ReferenceArrayList();
            ReferenceOpenHashSet<Ref<EntityStore>> seenMarkers = new ReferenceOpenHashSet();
            double rangeSquared = this.range > 0.0 ? this.range * this.range : 0.0;
            int triggered = 0;

            for (VolumeEntry areaVolume : areaVolumes) {
               Vector3d origin = areaVolume.getPosition();
               TriggerVolumeShape shape = areaVolume.getShape();
               int searchRadius = (int)Math.ceil(Math.max(shape.getBoundingRadius(), this.range)) + 1;
               candidates.clear();
               spatialResource.getSpatialStructure().collect(origin, searchRadius, candidates);

               for (int i = 0; i < candidates.size(); i++) {
                  Ref<EntityStore> markerRef = (Ref<EntityStore>)candidates.get(i);
                  if (markerRef.isValid() && seenMarkers.add(markerRef)) {
                     SpawnMarkerEntity markerComponent = store.getComponent(markerRef, SpawnMarkerEntity.getComponentType());
                     if (markerComponent != null
                        && markerComponent.isManualTrigger()
                        && (this.markerType == null || this.markerType.equals(markerComponent.getSpawnMarkerId()))) {
                        TransformComponent transform = store.getComponent(markerRef, TransformComponent.getComponentType());
                        if (transform != null && matchesArea(shape, origin, transform.getPosition(), rangeSquared) && markerComponent.trigger(markerRef, store)
                           )
                         {
                           triggered++;
                        }
                     }
                  }
               }
            }

            if (triggered == 0) {
               LOGGER.at(Level.FINE)
                  .log(
                     "TriggerNpcMarkers: no manual spawn markers matched in volume '%s'%s",
                     context.getVolume().getId(),
                     this.markerType != null ? " (MarkerType filter: " + this.markerType + ")" : ""
                  );
            }
         }
      }
   }

   @Nonnull
   private List<VolumeEntry> resolveAreaVolumes(@Nonnull TriggerContext context) {
      return this.matchTag != null && !this.matchTag.isBlank()
         ? TaggedVolumeEffectUtil.collectTargets(context, this.matchTag, this.radius, this.center)
         : context.getSpatialVolumes();
   }

   private static boolean matchesArea(@Nonnull TriggerVolumeShape shape, @Nonnull Vector3d origin, @Nonnull Vector3d position, double rangeSquared) {
      return shape.contains(origin, position) ? true : rangeSquared > 0.0 && origin.distanceSquared(position) <= rangeSquared;
   }
}
