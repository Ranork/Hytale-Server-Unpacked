package com.hypixel.hytale.server.npc.corecomponents.utility.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringArrayNoEmptyStringsValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.utility.ActionResetInstructions;
import com.hypixel.hytale.server.npc.instructions.Action;
import javax.annotation.Nonnull;

public class BuilderActionResetInstructions extends BuilderActionBase {
   protected final StringArrayHolder instructions = new StringArrayHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Force reset instructionList";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Force reset instructionList, either by name, or as a whole";
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionResetInstructions(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionResetInstructions readConfig(@Nonnull JsonElement data) {
      this.getStringArray(
         data,
         "Instructions",
         this.instructions,
         null,
         0,
         Integer.MAX_VALUE,
         StringArrayNoEmptyStringsValidator.get(),
         BuilderDescriptorState.Stable,
         "The instructionList to reset",
         "The instructionList to reset. If left empty, will reset all instructionList"
      );
      return this;
   }

   public int[] getInstructions(@Nonnull BuilderSupport support) {
      String[] instructionNames = this.instructions.get(support.getExecutionContext());
      if (instructionNames == null) {
         return ArrayUtil.EMPTY_INT_ARRAY;
      } else {
         int[] instructionIndexes = new int[instructionNames.length];

         for (int i = 0; i < instructionIndexes.length; i++) {
            instructionIndexes[i] = support.getInstructionSlot(instructionNames[i]);
         }

         return instructionIndexes;
      }
   }
}
