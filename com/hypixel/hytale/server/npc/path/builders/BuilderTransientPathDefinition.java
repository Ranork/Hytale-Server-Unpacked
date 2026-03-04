package com.hypixel.hytale.server.npc.path.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.builtin.path.path.TransientPathDefinition;
import com.hypixel.hytale.builtin.path.waypoint.RelativeWaypointDefinition;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderBase;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderObjectListHelper;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.BuilderValidationHelper;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderTransientPathDefinition extends BuilderBase<TransientPathDefinition> {
   protected final BuilderObjectListHelper<RelativeWaypointDefinition> waypoints = new BuilderObjectListHelper<>(RelativeWaypointDefinition.class, this);
   protected final DoubleHolder scale = new DoubleHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "List of transient path points";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public TransientPathDefinition build(@Nonnull BuilderSupport builderSupport) {
      return new TransientPathDefinition(this.getWaypoints(builderSupport), this.getScale(builderSupport));
   }

   @Nonnull
   @Override
   public Class<TransientPathDefinition> category() {
      return TransientPathDefinition.class;
   }

   @Override
   public final boolean isEnabled(ExecutionContext context) {
      return true;
   }

   @Nonnull
   @Override
   public Builder<TransientPathDefinition> readConfig(@Nonnull JsonElement data) {
      this.requireArray(
         data,
         "Waypoints",
         this.waypoints,
         null,
         BuilderDescriptorState.Stable,
         "List of transient path points",
         null,
         new BuilderValidationHelper(this.fileName, null, this.internalReferenceResolver, null, null, this.extraInfo, null, this.readErrors)
      );
      this.getDouble(data, "Scale", this.scale, 1.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Overall path scale", null);
      return this;
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Override
   public boolean validate(
      String configName,
      @Nonnull NPCLoadTimeValidationHelper validationHelper,
      @Nonnull ExecutionContext context,
      Scope globalScope,
      @Nonnull List<String> errors
   ) {
      return super.validate(configName, validationHelper, context, globalScope, errors)
         & this.waypoints.validate(configName, validationHelper, this.builderManager, context, globalScope, errors);
   }

   @Nullable
   public List<RelativeWaypointDefinition> getWaypoints(@Nonnull BuilderSupport support) {
      return this.waypoints.build(support);
   }

   public double getScale(@Nonnull BuilderSupport support) {
      return this.scale.get(support.getExecutionContext());
   }
}
