package com.hypixel.hytale.builtin.adventure.npcobjectives.npc.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.builtin.adventure.npcobjectives.npc.SensorHasTask;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.InstructionType;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringArrayNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import java.util.EnumSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderSensorHasTask extends BuilderSensorBase {
   @Nonnull
   protected final StringArrayHolder tasksById = new StringArrayHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Checks whether or not the player being iterated by the interaction instruction has any of the given tasks";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Sensor build(@Nonnull BuilderSupport builderSupport) {
      return new SensorHasTask(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.requireStringArray(
         data,
         "TasksById",
         this.tasksById,
         0,
         Integer.MAX_VALUE,
         StringArrayNotEmptyValidator.get(),
         BuilderDescriptorState.Stable,
         "Completable tasks to match by name",
         null
      );
      this.requireInstructionType(EnumSet.of(InstructionType.Interaction));
      return this;
   }

   @Nullable
   public String[] getTasksById(@Nonnull BuilderSupport support) {
      return this.tasksById.get(support.getExecutionContext());
   }
}
