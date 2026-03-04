package com.hypixel.hytale.server.npc.asset.builder.validators;

import com.hypixel.hytale.server.npc.asset.builder.ComponentContext;
import com.hypixel.hytale.server.npc.asset.builder.InstructionType;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class InstructionContextValidator extends Validator {
   private final EnumSet<InstructionType> instructionTypes;
   private final EnumSet<ComponentContext> componentContexts;

   private InstructionContextValidator(EnumSet<InstructionType> instructionTypes, EnumSet<ComponentContext> componentContexts) {
      this.instructionTypes = instructionTypes;
      this.componentContexts = componentContexts;
   }

   @Nonnull
   public static String getErrorMessage(
      @Nonnull String value,
      @Nonnull InstructionType instructionContext,
      boolean instructionMatched,
      @Nonnull ComponentContext componentContext,
      boolean extraMatched,
      String breadcrumbs
   ) {
      StringBuilder sb = new StringBuilder(value).append(" not valid");
      if (!instructionMatched) {
         sb.append(" in instruction ").append(instructionContext.get());
      }

      if (!extraMatched) {
         sb.append(" in context ").append(componentContext.get());
      }

      sb.append(" at: ").append(breadcrumbs);
      return sb.toString();
   }

   @Nonnull
   public static InstructionContextValidator inInstructions(EnumSet<InstructionType> instructionTypes, EnumSet<ComponentContext> componentContexts) {
      return new InstructionContextValidator(instructionTypes, componentContexts);
   }
}
