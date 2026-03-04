package com.hypixel.hytale.server.npc.corecomponents.movement.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.corecomponents.movement.BodyMotionWanderInCircle;
import javax.annotation.Nonnull;

public class BuilderBodyMotionWanderInCircle extends BuilderBodyMotionWanderBase {
   protected final DoubleHolder radius = new DoubleHolder();
   protected boolean flock;
   protected boolean useSphere;

   @Nonnull
   public BodyMotionWanderInCircle build(@Nonnull BuilderSupport builderSupport) {
      super.build(builderSupport);
      return new BodyMotionWanderInCircle(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Random movement in circle around spawn position";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Random movement in short linear pieces inside circle around spawn position.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderBodyMotionWanderInCircle readConfig(@Nonnull JsonElement data) {
      this.getDouble(data, "Radius", this.radius, 10.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Radius of circle to wander in", null);
      this.getBoolean(data, "Flock", b -> this.flock = b, false, BuilderDescriptorState.Experimental, "Do not use", null);
      this.getBoolean(data, "UseSphere", b -> this.useSphere = b, false, BuilderDescriptorState.Stable, "Use sphere", "Use a sphere instead of circle cylinder");
      return this;
   }

   public double getRadius(@Nonnull BuilderSupport builderSupport) {
      return this.radius.get(builderSupport.getExecutionContext());
   }

   public boolean isFlock() {
      return this.flock;
   }

   public boolean isUseSphere() {
      return this.useSphere;
   }
}
