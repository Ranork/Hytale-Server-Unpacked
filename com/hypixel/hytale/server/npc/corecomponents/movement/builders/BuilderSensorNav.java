package com.hypixel.hytale.server.npc.corecomponents.movement.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.EnumSetHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.movement.SensorNav;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import com.hypixel.hytale.server.npc.movement.NavState;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class BuilderSensorNav extends BuilderSensorBase {
   protected final EnumSetHolder<NavState> navStateEnumSetHolder = new EnumSetHolder<>();
   protected final DoubleHolder throttleDuration = new DoubleHolder();
   protected final DoubleHolder targetDelta = new DoubleHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Queries navigation state";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Sensor build(@Nonnull BuilderSupport builderSupport) {
      return new SensorNav(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.getEnumSet(
         data,
         "NavStates",
         this.navStateEnumSetHolder,
         NavState.class,
         EnumSet.noneOf(NavState.class),
         BuilderDescriptorState.Stable,
         "Trigger when path finder is in one of the states or empty to match all",
         null
      );
      this.getDouble(
         data,
         "ThrottleDuration",
         this.throttleDuration,
         0.0,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.Stable,
         "Minimum time in seconds the path finder isn't able to reach target or 0 to ignore",
         null
      );
      this.getDouble(
         data,
         "TargetDelta",
         this.targetDelta,
         0.0,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.Stable,
         "Minimum distance target has moved since path was computed or 0 to ignore",
         null
      );
      return this;
   }

   public EnumSet<NavState> getNavStates(@Nonnull BuilderSupport builderSupport) {
      return this.navStateEnumSetHolder.get(builderSupport.getExecutionContext());
   }

   public double getThrottleDuration(@Nonnull BuilderSupport support) {
      return this.throttleDuration.get(support.getExecutionContext());
   }

   public double getTargetDelta(@Nonnull BuilderSupport support) {
      return this.targetDelta.get(support.getExecutionContext());
   }
}
