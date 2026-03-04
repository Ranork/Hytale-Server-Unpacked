package com.hypixel.hytale.builtin.adventure.npcobjectives.assets;

import com.hypixel.hytale.builtin.adventure.objectives.config.task.ObjectiveTaskAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.taskcondition.TaskConditionAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.worldlocationproviders.WorldLocationProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.Objects;
import javax.annotation.Nonnull;

public class BountyObjectiveTaskAsset extends ObjectiveTaskAsset {
   @Nonnull
   public static final BuilderCodec<BountyObjectiveTaskAsset> CODEC = BuilderCodec.builder(
         BountyObjectiveTaskAsset.class, BountyObjectiveTaskAsset::new, ObjectiveTaskAsset.BASE_CODEC
      )
      .append(
         new KeyedCodec<>("NpcId", Codec.STRING),
         (bountyObjectiveTaskAsset, s) -> bountyObjectiveTaskAsset.npcId = s,
         bountyObjectiveTaskAsset -> bountyObjectiveTaskAsset.npcId
      )
      .add()
      .<WorldLocationProvider>append(
         new KeyedCodec<>("WorldLocationCondition", WorldLocationProvider.CODEC),
         (bountyObjectiveTaskAsset, worldLocationCondition) -> bountyObjectiveTaskAsset.worldLocationProvider = worldLocationCondition,
         bountyObjectiveTaskAsset -> bountyObjectiveTaskAsset.worldLocationProvider
      )
      .addValidator(Validators.nonNull())
      .add()
      .build();
   protected String npcId;
   protected WorldLocationProvider worldLocationProvider;

   public BountyObjectiveTaskAsset(
      String descriptionId, TaskConditionAsset[] taskConditions, Vector3i[] mapMarkers, String npcId, WorldLocationProvider worldLocationProvider
   ) {
      super(descriptionId, taskConditions, mapMarkers);
      this.npcId = npcId;
      this.worldLocationProvider = worldLocationProvider;
   }

   protected BountyObjectiveTaskAsset() {
   }

   @Nonnull
   @Override
   public ObjectiveTaskAsset.TaskScope getTaskScope() {
      return ObjectiveTaskAsset.TaskScope.PLAYER;
   }

   public String getNpcId() {
      return this.npcId;
   }

   public WorldLocationProvider getWorldLocationProvider() {
      return this.worldLocationProvider;
   }

   @Override
   protected boolean matchesAsset0(ObjectiveTaskAsset task) {
      if (task instanceof BountyObjectiveTaskAsset asset) {
         return !Objects.equals(asset.npcId, this.npcId) ? false : Objects.equals(asset.worldLocationProvider, this.worldLocationProvider);
      } else {
         return false;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "BountyObjectiveTaskAsset{npcId='" + this.npcId + "', worldLocationCondition=" + this.worldLocationProvider + "} " + super.toString();
   }
}
