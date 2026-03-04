package com.hypixel.hytale.server.npc.corecomponents.entity.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNotEmptyValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNullOrNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.SensorBeacon;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import javax.annotation.Nonnull;

public class BuilderSensorBeacon extends BuilderSensorBase {
   protected final StringHolder message = new StringHolder();
   protected final DoubleHolder range = new DoubleHolder();
   protected String targetSlot;
   protected boolean consume;

   @Nonnull
   public SensorBeacon build(@Nonnull BuilderSupport builderSupport) {
      return new SensorBeacon(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Checks to see if any messages have been broadcasted by nearby NPCs";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Checks to see if any messages have been broadcasted by nearby NPCs.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.requireString(data, "Message", this.message, StringNotEmptyValidator.get(), BuilderDescriptorState.Experimental, "The message to listen for", null);
      this.getDouble(
         data,
         "Range",
         this.range,
         64.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Experimental,
         "The max distance beacons should be received from",
         null
      );
      this.getString(
         data,
         "TargetSlot",
         s -> this.targetSlot = s,
         null,
         StringNullOrNotEmptyValidator.get(),
         BuilderDescriptorState.Stable,
         "A slot to store the sender as a target. If omitted no target will be stored",
         null
      );
      this.getBoolean(
         data, "ConsumeMessage", b -> this.consume = b, true, BuilderDescriptorState.Stable, "Whether the message should be consumed by this sensor", null
      );
      this.provideFeature(Feature.AnyEntity);
      return this;
   }

   public int getMessageSlot(@Nonnull BuilderSupport builderSupport) {
      String name = this.message.get(builderSupport.getExecutionContext());
      return builderSupport.getBeaconMessageSlot(name);
   }

   public double getRange(@Nonnull BuilderSupport builderSupport) {
      return this.range.get(builderSupport.getExecutionContext());
   }

   public int getTargetSlot(@Nonnull BuilderSupport support) {
      return this.targetSlot == null ? Integer.MIN_VALUE : support.getTargetSlot(this.targetSlot);
   }

   public boolean isConsume() {
      return this.consume;
   }
}
