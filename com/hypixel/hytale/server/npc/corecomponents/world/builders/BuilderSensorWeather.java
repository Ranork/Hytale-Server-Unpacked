package com.hypixel.hytale.server.npc.corecomponents.world.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.WeatherExistsValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.world.SensorWeather;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import java.util.EnumSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderSensorWeather extends BuilderSensorBase {
   protected final AssetArrayHolder weathers = new AssetArrayHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Matches the current weather at the NPCs position against a set of weather globs";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Sensor build(@Nonnull BuilderSupport builderSupport) {
      return new SensorWeather(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.requireAssetArray(
         data,
         "Weathers",
         this.weathers,
         0,
         Integer.MAX_VALUE,
         WeatherExistsValidator.withConfig(EnumSet.of(AssetValidator.Config.MATCHER)),
         BuilderDescriptorState.Stable,
         "The glob patterns to match against weather",
         null
      );
      return this;
   }

   @Nullable
   public String[] getWeathers(@Nonnull BuilderSupport support) {
      return this.weathers.get(support.getExecutionContext());
   }
}
