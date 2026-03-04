package com.hypixel.hytale.server.npc.corecomponents.interaction.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.InstructionType;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.interaction.SensorInteractionContext;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class BuilderSensorInteractionContext extends BuilderSensorBase {
   protected final StringHolder interactionContext = new StringHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Checks whether the currently iterated player in the interaction instruction has interacted with this NPC in the given context";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Sensor build(@Nonnull BuilderSupport builderSupport) {
      return new SensorInteractionContext(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.requireString(
         data, "Context", this.interactionContext, StringNotEmptyValidator.get(), BuilderDescriptorState.Stable, "The context of the interaction", null
      );
      this.requireInstructionType(EnumSet.of(InstructionType.Interaction));
      return this;
   }

   public String getInteractionContext(@Nonnull BuilderSupport support) {
      return this.interactionContext.get(support.getExecutionContext());
   }
}
