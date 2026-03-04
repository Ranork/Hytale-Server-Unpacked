package com.hypixel.hytale.server.npc.corecomponents.interaction.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.InstructionType;
import com.hypixel.hytale.server.npc.asset.builder.holder.EnumSetHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.FloatHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleRangeValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.interaction.SensorCanInteract;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class BuilderSensorCanInteract extends BuilderSensorBase {
   protected final FloatHolder viewSector = new FloatHolder();
   protected final EnumSetHolder<Attitude> attitudes = new EnumSetHolder<>();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Checks whether or not the player being iterated by the interaction instruction can interact with this NPC";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Sensor build(@Nonnull BuilderSupport builderSupport) {
      return new SensorCanInteract(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.getFloat(
         data,
         "ViewSector",
         this.viewSector,
         0.0,
         DoubleRangeValidator.between(0.0, 360.0),
         BuilderDescriptorState.Stable,
         "View sector to test the player in",
         null
      );
      this.getEnumSet(
         data,
         "Attitudes",
         this.attitudes,
         Attitude.class,
         EnumSet.of(Attitude.NEUTRAL, Attitude.FRIENDLY, Attitude.REVERED),
         BuilderDescriptorState.Stable,
         "A set of attitudes to match",
         null
      );
      this.requireInstructionType(EnumSet.of(InstructionType.Interaction));
      return this;
   }

   public float getViewSectorRadians(@Nonnull BuilderSupport builderSupport) {
      return (float) (Math.PI / 180.0) * this.viewSector.get(builderSupport.getExecutionContext());
   }

   public EnumSet<Attitude> getAttitudes(@Nonnull BuilderSupport support) {
      return this.attitudes.get(support.getExecutionContext());
   }
}
