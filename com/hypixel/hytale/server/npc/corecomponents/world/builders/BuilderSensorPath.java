package com.hypixel.hytale.server.npc.corecomponents.world.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.EnumHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.world.SensorPath;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import java.util.Set;
import javax.annotation.Nonnull;

public class BuilderSensorPath extends BuilderSensorBase {
   protected final StringHolder name = new StringHolder();
   protected final DoubleHolder range = new DoubleHolder();
   protected final EnumHolder<SensorPath.PathType> pathType = new EnumHolder<>();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Find a path based on various criteria";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Find a path based on various criteria. Provides the position of the nearest waypoint and the path itself";
   }

   @Override
   public void registerTags(@Nonnull Set<String> tags) {
      super.registerTags(tags);
      tags.add("path");
   }

   @Nonnull
   public Sensor build(@Nonnull BuilderSupport builderSupport) {
      return new SensorPath(this, builderSupport);
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.getString(
         data, "Path", this.name, null, null, BuilderDescriptorState.Stable, "The name of the path. If left blank, will find the nearest path", null
      );
      this.getDouble(
         data,
         "Range",
         this.range,
         10.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "The range to test to nearest waypoint. 0 is unlimited",
         null
      );
      this.getEnum(
         data,
         "PathType",
         this.pathType,
         SensorPath.PathType.class,
         SensorPath.PathType.AnyPrefabPath,
         BuilderDescriptorState.Stable,
         "The type of path to search for",
         null
      );
      this.validateStringIfEnumIs(this.name, StringNotEmptyValidator.get(), this.pathType, SensorPath.PathType.WorldPath);
      this.provideFeature(Feature.Position);
      this.provideFeature(Feature.Path);
      return this;
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   public String getPath(@Nonnull BuilderSupport support) {
      return this.name.get(support.getExecutionContext());
   }

   public double getRange(@Nonnull BuilderSupport support) {
      return this.range.get(support.getExecutionContext());
   }

   public SensorPath.PathType getPathType(@Nonnull BuilderSupport support) {
      return this.pathType.get(support.getExecutionContext());
   }
}
