package com.hypixel.hytale.builtin.adventure.objectives.config.task;

import com.hypixel.hytale.builtin.adventure.objectives.markers.reachlocation.ReachLocationMarkerAsset;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import java.util.Objects;
import javax.annotation.Nonnull;

public class ReachLocationTaskAsset extends ObjectiveTaskAsset {
   @Nonnull
   public static final BuilderCodec<ReachLocationTaskAsset> CODEC = BuilderCodec.builder(ReachLocationTaskAsset.class, ReachLocationTaskAsset::new, BASE_CODEC)
      .append(
         new KeyedCodec<>("TargetLocation", Codec.STRING),
         (reachLocationTaskAsset, vector3i) -> reachLocationTaskAsset.targetLocationId = vector3i,
         reachLocationTaskAsset -> reachLocationTaskAsset.targetLocationId
      )
      .addValidator(Validators.nonNull())
      .addValidator(ReachLocationMarkerAsset.VALIDATOR_CACHE.getValidator())
      .add()
      .build();
   protected String targetLocationId;

   @Nonnull
   @Override
   public ObjectiveTaskAsset.TaskScope getTaskScope() {
      return ObjectiveTaskAsset.TaskScope.PLAYER;
   }

   public String getTargetLocationId() {
      return this.targetLocationId;
   }

   @Override
   protected boolean matchesAsset0(ObjectiveTaskAsset task) {
      return task instanceof ReachLocationTaskAsset asset ? Objects.equals(asset.targetLocationId, this.targetLocationId) : false;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ReachLocationTaskAsset{targetLocationId=" + this.targetLocationId + "} " + super.toString();
   }
}
