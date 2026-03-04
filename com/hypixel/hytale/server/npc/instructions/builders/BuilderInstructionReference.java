package com.hypixel.hytale.server.npc.instructions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.instructions.Instruction;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import it.unimi.dsi.fastutil.ints.IntSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderInstructionReference extends BuilderInstruction {
   @Nullable
   protected IntSet internalDependencies;

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Prioritized instruction list that can be referenced from elsewhere in the file";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Prioritized instruction list that can be referenced from elsewhere in the file. Otherwise works like the default";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nullable
   @Override
   public Instruction build(@Nonnull BuilderSupport builderSupport) {
      if (!this.enabled.get(builderSupport.getExecutionContext())) {
         return null;
      } else {
         Sensor sensor;
         if (this.sensorBuilderObjectReferenceHelper.isPresent()) {
            sensor = this.getSensor(builderSupport);
            if (sensor == null) {
               return null;
            }
         } else {
            sensor = Sensor.NULL;
         }

         if (this.currentStateName != null) {
            builderSupport.pushCurrentStateName(this.currentStateName);
         }

         Instruction[] instructionList = this.hasNestedInstructions() ? this.getSteps(builderSupport) : null;
         if (instructionList == null && !this.hasActions() && !this.hasBodyMotion() && !this.hasHeadMotion()) {
            if (this.currentStateName != null) {
               builderSupport.popCurrentStateName();
            }

            return null;
         } else {
            if (this.currentStateName != null) {
               builderSupport.popCurrentStateName();
            }

            return new Instruction(this, sensor, instructionList, builderSupport);
         }
      }
   }

   @Override
   public boolean excludeFromRegularBuilding() {
      return true;
   }

   @Override
   protected boolean requiresName() {
      return true;
   }

   @Nullable
   @Override
   public String getName() {
      return null;
   }

   @Nonnull
   @Override
   public Builder<Instruction> readConfig(@Nonnull JsonElement data) {
      if (!this.isCreatingDescriptor()) {
         this.internalReferenceResolver.setRecordDependencies();
      }

      this.getParameterBlock(data, BuilderDescriptorState.Stable, "The parameter block for defining variables", null);
      super.readConfig(data);
      this.cleanupParameters();
      if (!this.isCreatingDescriptor()) {
         this.internalDependencies = this.internalReferenceResolver.getRecordedDependenices();
         this.internalReferenceResolver.stopRecordingDependencies();
         int index = this.internalReferenceResolver.getOrCreateIndex(this.name);
         this.internalReferenceResolver.addBuilder(index, this);
      }

      return this;
   }

   @Nullable
   public IntSet getInternalDependencies() {
      return this.internalDependencies;
   }
}
