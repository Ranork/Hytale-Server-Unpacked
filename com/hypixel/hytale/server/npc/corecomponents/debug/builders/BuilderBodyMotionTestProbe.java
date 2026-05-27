package com.hypixel.hytale.server.npc.corecomponents.debug.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderBodyMotionBase;
import com.hypixel.hytale.server.npc.corecomponents.debug.BodyMotionTestProbe;
import com.hypixel.hytale.server.npc.movement.constraints.RelaxedConstraint;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class BuilderBodyMotionTestProbe extends BuilderBodyMotionBase {
   private static final String[] AVOID_OR_RELAXED_ATTRIBUTES = new String[]{"AvoidBlockDamage", "RelaxedConstraints"};
   private static final String[] LEGACY_OR_RELAXED_ATTRIBUTES = new String[]{"RelaxedMoveConstraints", "RelaxedConstraints"};
   protected double adjustX;
   protected double adjustZ;
   protected double adjustDistance;
   protected float snapAngle;
   protected boolean isAvoidingBlockDamage;
   protected boolean isLegacyRelaxedMoveConstraints;
   protected EnumSet<RelaxedConstraint> relaxedConstraints = EnumSet.noneOf(RelaxedConstraint.class);
   private final boolean[] avoidOrRelaxedPresent = new boolean[2];
   private final boolean[] legacyOrRelaxedPresent = new boolean[2];

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
      this.avoidOrRelaxedPresent[0] = this.getBoolean(
         data,
         "AvoidBlockDamage",
         v -> this.isAvoidingBlockDamage = v,
         true,
         BuilderDescriptorState.Deprecated,
         "Should avoid environmental damage from blocks",
         null
      );
      this.legacyOrRelaxedPresent[0] = this.getBoolean(
         data,
         "RelaxedMoveConstraints",
         v -> this.isLegacyRelaxedMoveConstraints = v,
         false,
         BuilderDescriptorState.Deprecated,
         "NPC can do movements like wading (depends on motion controller type)",
         null
      );
      this.avoidOrRelaxedPresent[1] = this.getEnumSet(
         data,
         "RelaxedConstraints",
         s -> this.relaxedConstraints = (EnumSet<RelaxedConstraint>)s,
         RelaxedConstraint.class,
         () -> EnumSet.noneOf(RelaxedConstraint.class),
         EnumSet.noneOf(RelaxedConstraint.class),
         BuilderDescriptorState.Stable,
         "List of constraints to relax for this motion (new mode; empty = no relaxed constraints)",
         null
      );
      this.legacyOrRelaxedPresent[1] = this.avoidOrRelaxedPresent[1];
      this.validateOneOrNonePresent(AVOID_OR_RELAXED_ATTRIBUTES, this.avoidOrRelaxedPresent);
      this.validateOneOrNonePresent(LEGACY_OR_RELAXED_ATTRIBUTES, this.legacyOrRelaxedPresent);
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

   public boolean isLegacyRelaxedMoveConstraints() {
      return this.isLegacyRelaxedMoveConstraints;
   }

   @Nonnull
   public EnumSet<RelaxedConstraint> getRelaxedConstraints() {
      return this.relaxedConstraints;
   }

   public boolean isRelaxedConstraintsPresent() {
      return this.avoidOrRelaxedPresent[1];
   }
}
