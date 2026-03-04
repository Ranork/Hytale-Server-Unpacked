package com.hypixel.hytale.server.npc.asset.builder;

import com.hypixel.hytale.server.npc.asset.builder.providerevaluators.ProviderEvaluator;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;

public class FeatureEvaluatorHelper {
   @Nonnull
   private List<ProviderEvaluator> evaluators = new ObjectArrayList();
   private List<BiConsumer<BuilderManager, ExecutionContext>> providerReferenceValidators;
   private List<BiConsumer<FeatureEvaluatorHelper, ExecutionContext>> componentRequirementValidators;
   private boolean locked;
   private boolean containsProviderReference;
   private boolean isFeatureRequiringComponent;
   private boolean disallowParameterProviders;

   public FeatureEvaluatorHelper() {
   }

   public FeatureEvaluatorHelper(boolean couldRequireFeature) {
      this.isFeatureRequiringComponent = couldRequireFeature;
   }

   public boolean isDisallowParameterProviders() {
      return this.disallowParameterProviders;
   }

   public void add(ProviderEvaluator evaluator) {
      this.evaluators.add(evaluator);
   }

   public boolean canAddProvider() {
      return !this.locked;
   }

   @Nonnull
   public FeatureEvaluatorHelper lock() {
      this.locked = true;
      this.evaluators = Collections.unmodifiableList(this.evaluators);
      return this;
   }

   public void setContainsReference() {
      this.containsProviderReference = true;
   }

   public void addProviderReferenceValidator(BiConsumer<BuilderManager, ExecutionContext> referenceValidator) {
      if (this.providerReferenceValidators == null) {
         this.providerReferenceValidators = new ObjectArrayList();
      }

      this.providerReferenceValidators.add(referenceValidator);
   }

   public void addComponentRequirementValidator(BiConsumer<FeatureEvaluatorHelper, ExecutionContext> validator) {
      if (this.componentRequirementValidators == null) {
         this.componentRequirementValidators = new ObjectArrayList();
      }

      this.componentRequirementValidators.add(validator);
   }

   @Nonnull
   public List<ProviderEvaluator> getProviders() {
      return this.evaluators;
   }

   public boolean requiresProviderReferenceEvaluation() {
      return this.containsProviderReference;
   }

   public boolean belongsToFeatureRequiringComponent() {
      return this.isFeatureRequiringComponent;
   }

   public void validateProviderReferences(BuilderManager manager, ExecutionContext context) {
      if (this.providerReferenceValidators != null) {
         for (BiConsumer<BuilderManager, ExecutionContext> validator : this.providerReferenceValidators) {
            validator.accept(manager, context);
         }
      }
   }

   public void validateComponentRequirements(FeatureEvaluatorHelper providers, ExecutionContext context) {
      if (this.componentRequirementValidators != null) {
         for (BiConsumer<FeatureEvaluatorHelper, ExecutionContext> validator : this.componentRequirementValidators) {
            validator.accept(providers, context);
         }
      }
   }

   public void disallowParameterProviders() {
      this.disallowParameterProviders = true;
   }
}
