package com.hypixel.hytale.server.npc.corecomponents.audiovisual.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.animations.NPCAnimationSlot;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.EnumHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.audiovisual.SensorAnimation;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import javax.annotation.Nonnull;

public class BuilderSensorAnimation extends BuilderSensorBase {
   protected final EnumHolder<NPCAnimationSlot> animationSlot = new EnumHolder<>();
   protected final StringHolder animationId = new StringHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Check if a given animation is being played";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Sensor build(@Nonnull BuilderSupport builderSupport) {
      return new SensorAnimation(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.requireEnum(data, "Slot", this.animationSlot, NPCAnimationSlot.class, BuilderDescriptorState.Stable, "The animation slot to check", null);
      this.requireString(
         data, "Animation", this.animationId, StringNotEmptyValidator.get(), BuilderDescriptorState.Stable, "The animation ID to check for", null
      );
      return this;
   }

   public NPCAnimationSlot getAnimationSlot(@Nonnull BuilderSupport support) {
      return this.animationSlot.get(support.getExecutionContext());
   }

   public String getAnimationId(@Nonnull BuilderSupport support) {
      return this.animationId.get(support.getExecutionContext());
   }
}
