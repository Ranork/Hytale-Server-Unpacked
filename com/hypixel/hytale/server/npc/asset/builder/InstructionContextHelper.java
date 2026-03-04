package com.hypixel.hytale.server.npc.asset.builder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InstructionContextHelper {
   private final InstructionType context;
   private ComponentContext componentContext;
   private List<BiConsumer<InstructionType, ComponentContext>> componentContextEvaluators;

   public InstructionContextHelper(InstructionType context) {
      this.context = context;
   }

   public boolean isComponent() {
      return this.context == InstructionType.Component;
   }

   public void setComponentContext(ComponentContext context) {
      this.componentContext = context;
   }

   public boolean isInCorrectInstruction(@Nonnull EnumSet<InstructionType> validTypes) {
      return validTypes.contains(this.context);
   }

   public static boolean isInCorrectInstruction(@Nonnull EnumSet<InstructionType> validTypes, InstructionType instructionContext) {
      return validTypes.contains(instructionContext);
   }

   public boolean extraContextMatches(@Nullable EnumSet<ComponentContext> contexts) {
      return contexts == null || contexts.contains(this.componentContext);
   }

   public static boolean extraContextMatches(@Nullable EnumSet<ComponentContext> validContexts, ComponentContext context) {
      return validContexts == null || validContexts.contains(context);
   }

   public void addComponentContextEvaluator(BiConsumer<InstructionType, ComponentContext> evaluator) {
      if (this.componentContextEvaluators == null) {
         this.componentContextEvaluators = new ObjectArrayList();
      }

      this.componentContextEvaluators.add(evaluator);
   }

   public void validateComponentContext(InstructionType instructionContext, ComponentContext componentContext) {
      if (!this.isComponent()) {
         throw new IllegalStateException("Calling validateComponentContext on a InstructionContextHelper that is not part of a component!");
      } else if (this.componentContextEvaluators != null) {
         for (BiConsumer<InstructionType, ComponentContext> evaluator : this.componentContextEvaluators) {
            evaluator.accept(instructionContext, componentContext);
         }
      }
   }

   public InstructionType getInstructionContext() {
      return this.context;
   }

   public ComponentContext getComponentContext() {
      return this.componentContext;
   }
}
