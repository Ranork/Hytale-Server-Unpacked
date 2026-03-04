package com.hypixel.hytale.server.npc.asset.builder;

import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.server.npc.decisionmaker.core.Evaluator;
import java.util.List;

public class BuilderValidationHelper {
   private final String name;
   private final FeatureEvaluatorHelper featureEvaluatorHelper;
   private final InternalReferenceResolver internalReferenceResolver;
   private final StateMappingHelper stateMappingHelper;
   private final InstructionContextHelper instructionContextHelper;
   private final ExtraInfo extraInfo;
   private final List<Evaluator<?>> evaluators;
   private final List<String> readErrors;

   public BuilderValidationHelper(
      String name,
      FeatureEvaluatorHelper featureEvaluator,
      InternalReferenceResolver internalReferenceResolver,
      StateMappingHelper stateMappingHelper,
      InstructionContextHelper instructionContextHelper,
      ExtraInfo extraInfo,
      List<Evaluator<?>> evaluators,
      List<String> readErrors
   ) {
      this.name = name;
      this.featureEvaluatorHelper = featureEvaluator;
      this.internalReferenceResolver = internalReferenceResolver;
      this.stateMappingHelper = stateMappingHelper;
      this.instructionContextHelper = instructionContextHelper;
      this.extraInfo = extraInfo;
      this.evaluators = evaluators;
      this.readErrors = readErrors;
   }

   public String getName() {
      return this.name;
   }

   public FeatureEvaluatorHelper getFeatureEvaluatorHelper() {
      return this.featureEvaluatorHelper;
   }

   public InternalReferenceResolver getInternalReferenceResolver() {
      return this.internalReferenceResolver;
   }

   public StateMappingHelper getStateMappingHelper() {
      return this.stateMappingHelper;
   }

   public InstructionContextHelper getInstructionContextHelper() {
      return this.instructionContextHelper;
   }

   public ExtraInfo getExtraInfo() {
      return this.extraInfo;
   }

   public List<String> getReadErrors() {
      return this.readErrors;
   }

   public List<Evaluator<?>> getEvaluators() {
      return this.evaluators;
   }
}
