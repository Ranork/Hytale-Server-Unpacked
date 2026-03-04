package com.hypixel.hytale.server.npc.corecomponents.debug.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderBodyMotionBase;
import com.hypixel.hytale.server.npc.corecomponents.debug.BodyMotionTestProbe;
import javax.annotation.Nonnull;

public class BuilderBodyMotionTestProbe extends BuilderBodyMotionBase {
   protected double adjustX;
   protected double adjustZ;
   protected double adjustDistance;
   protected float snapAngle;
   protected boolean isAvoidingBlockDamage;
   protected boolean isRelaxedMoveConstraints;

   @Nonnull
   public BodyMotionTestProbe build(BuilderSupport builderSupport) {
      return new BodyMotionTestProbe(this);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Debugging - Test probing";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Experimental;
   }

   @Nonnull
   public BuilderBodyMotionTestProbe readConfig(@Nonnull JsonElement data) {
      this.getDouble(data, "AdjustX", v -> this.adjustX = v, -1.0, null, BuilderDescriptorState.Experimental, "X block position adjustment", null);
      this.getDouble(data, "AdjustZ", v -> this.adjustZ = v, -1.0, null, BuilderDescriptorState.Experimental, "Y block position adjustment", null);
      this.getDouble(
         data,
         "AdjustDistance",
         v -> this.adjustDistance = v,
         -1.0,
         null,
         BuilderDescriptorState.Experimental,
         "Set probe direction length for debugging",
         null
      );
      this.getFloat(
         data, "SnapAngle", v -> this.snapAngle = v, -1.0F, null, BuilderDescriptorState.Experimental, "Snap angle to multiples of value for debugging", null
      );
      this.getBoolean(
         data,
         "AvoidBlockDamage",
         v -> this.isAvoidingBlockDamage = v,
         true,
         BuilderDescriptorState.Stable,
         "Should avoid environmental damage from blocks",
         null
      );
      this.getBoolean(
         data,
         "RelaxedMoveConstraints",
         v -> this.isRelaxedMoveConstraints = v,
         false,
         BuilderDescriptorState.Stable,
         "NPC can do movements like wading (depends on motion controller type)",
         null
      );
      return this;
   }

   public double getAdjustX() {
      return this.adjustX;
   }

   public double getAdjustZ() {
      return this.adjustZ;
   }

   public double getAdjustDistance() {
      return this.adjustDistance;
   }

   public float getSnapAngle() {
      return this.snapAngle;
   }

   public boolean isAvoidingBlockDamage() {
      return this.isAvoidingBlockDamage;
   }

   public boolean isRelaxedMoveConstraints() {
      return this.isRelaxedMoveConstraints;
   }
}
