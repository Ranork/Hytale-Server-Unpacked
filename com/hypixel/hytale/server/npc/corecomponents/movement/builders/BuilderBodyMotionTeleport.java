package com.hypixel.hytale.server.npc.corecomponents.movement.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleRangeValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSequenceValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderBodyMotionBase;
import com.hypixel.hytale.server.npc.corecomponents.movement.BodyMotionTeleport;
import com.hypixel.hytale.server.npc.instructions.BodyMotion;
import javax.annotation.Nonnull;

public class BuilderBodyMotionTeleport extends BuilderBodyMotionBase {
   public static final double[] DEFAULT_OFFSET_RADIUS = new double[]{0.0, 0.0};
   protected double[] offsetRadius;
   protected double maxYOffset;
   protected float sector;
   protected BodyMotionTeleport.Orientation orientation;

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Teleport NPC to a position given by a sensor";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Teleport NPC to a position given by a sensor or to a random position nearby with an optional minimum offset up to a maximum offset";
   }

   @Nonnull
   public BodyMotion build(BuilderSupport builderSupport) {
      return new BodyMotionTeleport(this);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Experimental;
   }

   @Nonnull
   public BuilderBodyMotionTeleport readConfig(@Nonnull JsonElement data) {
      this.getDoubleRange(
         data,
         "OffsetRange",
         v -> this.offsetRadius = v,
         DEFAULT_OFFSET_RADIUS,
         DoubleSequenceValidator.betweenWeaklyMonotonic(0.0, Double.MAX_VALUE),
         BuilderDescriptorState.Experimental,
         "The minimum and maximum offset the NPC can be spawned from the target position",
         null
      );
      this.getDouble(
         data,
         "MaxYOffset",
         v -> this.maxYOffset = v,
         5.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Experimental,
         "Maximum vertical offset from the target position in case of terrain obstacles",
         null
      );
      this.getFloat(
         data,
         "OffsetSector",
         v -> this.sector = v,
         0.0F,
         DoubleRangeValidator.between(0.0, 360.0),
         BuilderDescriptorState.Experimental,
         "The sector around the target in which to teleport to",
         "The sector around the target in which to teleport to. The origin point is directly between the target and the NPC teleporting"
      );
      this.getEnum(
         data,
         "Orientation",
         v -> this.orientation = v,
         BodyMotionTeleport.Orientation.class,
         BodyMotionTeleport.Orientation.Unchanged,
         BuilderDescriptorState.Experimental,
         "The direction to face after teleporting",
         null
      );
      this.requireFeature(Feature.AnyPosition);
      this.requireFeatureIf("Orientation", BodyMotionTeleport.Orientation.UseTarget, this.orientation, Feature.AnyEntity);
      return this;
   }

   public double[] getOffsetRadius() {
      return this.offsetRadius;
   }

   public double getMaxYOffset() {
      return this.maxYOffset;
   }

   public float getSectorRadians() {
      return (float) (Math.PI / 180.0) * this.sector;
   }

   public BodyMotionTeleport.Orientation getOrientation() {
      return this.orientation;
   }
}
