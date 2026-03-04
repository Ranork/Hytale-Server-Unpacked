package com.hypixel.hytale.server.npc.path.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.builtin.path.waypoint.RelativeWaypointDefinition;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderBase;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleRangeValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import javax.annotation.Nonnull;

public class BuilderRelativeWaypointDefinition extends BuilderBase<RelativeWaypointDefinition> {
   protected float rotation;
   protected double distance;

   @Nonnull
   @Override
   public String getShortDescription() {
      return "A simple path waypoint definition where each waypoint is relative to the previous";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public RelativeWaypointDefinition build(BuilderSupport builderSupport) {
      return new RelativeWaypointDefinition(this.getRotation(), this.getDistance());
   }

   @Nonnull
   @Override
   public Class<RelativeWaypointDefinition> category() {
      return RelativeWaypointDefinition.class;
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<RelativeWaypointDefinition> readConfig(@Nonnull JsonElement data) {
      this.getFloat(
         data,
         "Rotation",
         f -> this.rotation = f * (float) (Math.PI / 180.0),
         0.0F,
         DoubleRangeValidator.fromExclToExcl(-360.0, 360.0),
         BuilderDescriptorState.Stable,
         "Rotation to turn from previous waypoint",
         null
      );
      this.requireDouble(
         data,
         "Distance",
         d -> this.distance = d,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "A distance to move from the previous waypoint",
         null
      );
      return this;
   }

   @Override
   public final boolean isEnabled(ExecutionContext context) {
      return true;
   }

   public float getRotation() {
      return this.rotation;
   }

   public double getDistance() {
      return this.distance;
   }
}
