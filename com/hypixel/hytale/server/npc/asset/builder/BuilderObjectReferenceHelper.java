package com.hypixel.hytale.server.npc.asset.builder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hypixel.hytale.logger.sentry.SkipSentryException;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.providerevaluators.ReferenceProviderEvaluator;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringArrayNotEmptyValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNotEmptyValidator;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderObjectReferenceHelper<T> extends BuilderObjectHelper<T> {
   public static final String KEY_REFERENCE = "Reference";
   public static final String KEY_LOCAL = "Local";
   public static final String KEY_INTERFACE_LIST = "Interfaces";
   public static final String KEY_NULLABLE = "Nullable";
   public static final String NULL_COMPONENT = "$Null";
   public static final String KEY_LABEL = "$Label";
   @Nullable
   protected Builder<T> builder;
   protected final StringHolder fileReference = new StringHolder();
   protected String[] componentInterfaces;
   protected int referenceIndex;
   protected boolean isReference;
   protected boolean isNullable;
   @Nullable
   protected BuilderModifier modifier;
   protected FeatureEvaluatorHelper evaluatorHelper;
   protected InternalReferenceResolver internalReferenceResolver;
   protected boolean isInternalReference;
   protected String label;

   public BuilderObjectReferenceHelper(Class<?> classType, BuilderContext owner) {
      super(classType, owner);
      this.builder = null;
      this.referenceIndex = Integer.MIN_VALUE;
      this.modifier = null;
   }

   public boolean excludeFromRegularBuild() {
      return this.builder == null ? false : this.builder.excludeFromRegularBuilding();
   }

   @Nullable
   @Override
   public T build(@Nonnull BuilderSupport builderSupport) {
      if (!this.isPresent()) {
         return null;
      } else {
         Builder<T> builder = this.getBuilder(builderSupport.getBuilderManager(), builderSupport, this.isNullable);
         if (builder == null) {
            return null;
         } else {
            StateMappingHelper mappingHelper = builder.getStateMappingHelper();
            boolean hasLocalComponentStates = this.builder == null && mappingHelper != null && mappingHelper.hasComponentStates();
            if (hasLocalComponentStates) {
               mappingHelper.initialiseComponentState(builderSupport);
            }

            if (this.modifier == null) {
               this.validateRequiredFeatures(builder, builderSupport.getBuilderManager(), builderSupport.getExecutionContext());
               T instance = builder.isEnabled(builderSupport.getExecutionContext()) ? builder.build(builderSupport) : null;
               if (hasLocalComponentStates) {
                  mappingHelper.popComponentState(builderSupport);
               }

               return instance;
            } else {
               Scope globalScope = null;
               if (this.isInternalReference) {
                  globalScope = builderSupport.getGlobalScope();
                  Objects.requireNonNull(globalScope, "Global scope should not be null when applying to an internal component");
               }

               if (this.modifier.exportedStateCount() != builder.getStateMappingHelper().importedStateCount()) {
                  throw new SkipSentryException(
                     new IllegalStateException(
                        String.format(
                           "Number of exported states does not match imported states in component %s",
                           this.fileReference.get(builderSupport.getExecutionContext())
                        )
                     )
                  );
               } else {
                  ExecutionContext context = builderSupport.getExecutionContext();
                  Scope newScope = this.modifier.createScope(builderSupport, builder.getBuilderParameters(), globalScope);
                  Scope oldScope = context.setScope(newScope);
                  if (this.modifier.exportedStateCount() > 0) {
                     this.modifier.applyComponentStateMap(builderSupport);
                  }

                  this.validateRequiredFeatures(builder, builderSupport.getBuilderManager(), context);
                  this.validateInstructionContext(builder, builderSupport);
                  T instance = builder.isEnabled(builderSupport.getExecutionContext()) ? builder.build(builderSupport) : null;
                  if (this.modifier.exportedStateCount() > 0) {
                     this.modifier.popComponentStateMap(builderSupport);
                  }

                  if (hasLocalComponentStates) {
                     mappingHelper.popComponentState(builderSupport);
                  }

                  builderSupport.getExecutionContext().setScope(oldScope);
                  return instance;
               }
            }
         }
      }
   }

   @Override
   public boolean validate(
      String configName,
      NPCLoadTimeValidationHelper loadTimeValidationHelper,
      @Nonnull BuilderManager manager,
      @Nonnull ExecutionContext context,
      Scope globalScope,
      @Nonnull List<String> errors
   ) {
      if (!this.isPresent()) {
         return true;
      } else {
         Builder<T> builder;
         try {
            builder = this.getBuilder(manager, context, null);
         } catch (Exception var16) {
            errors.add(String.format("%s: %s", configName, var16.getMessage()));
            return false;
         }

         if (builder == null) {
            if (this.isNullable) {
               return true;
            } else {
               errors.add(
                  String.format(
                     "%s: %s is not a nullable component reference but a null component was passed", configName, this.fileReference.getExpressionString()
                  )
               );
               return false;
            }
         } else if (this.modifier == null) {
            if (!builder.isEnabled(context)) {
               return true;
            } else {
               boolean result = true;

               try {
                  this.validateRequiredFeatures(builder, manager, context);
               } catch (Exception var13) {
                  errors.add(String.format("%s: %s", configName, var13.getMessage()));
                  result = false;
               }

               return result & builder.validate(configName, loadTimeValidationHelper, context, globalScope, errors);
            }
         } else {
            boolean result = true;
            if (this.modifier.exportedStateCount() != builder.getStateMappingHelper().importedStateCount()) {
               errors.add(
                  String.format("%s: Number of exported states does not match imported states in component %s", configName, this.fileReference.get(context))
               );
               result = false;
            }

            Scope additionalScope = this.isInternalReference ? globalScope : null;

            Scope newScope;
            try {
               newScope = this.modifier.createScope(context, builder.getBuilderParameters(), additionalScope);
            } catch (Exception var15) {
               errors.add(String.format("%s: %s", configName, var15.getMessage()));
               return false;
            }

            Scope oldScope = context.setScope(newScope);
            if (builder.isEnabled(context)) {
               try {
                  this.validateRequiredFeatures(builder, manager, context);
               } catch (Exception var14) {
                  errors.add(String.format("%s: %s", configName, var14.getMessage()));
                  result = false;
               }

               result &= builder.validate(configName, loadTimeValidationHelper, context, globalScope, errors);
            }

            context.setScope(oldScope);
            return result;
         }
      }
   }

   @Nullable
   public Builder<T> getBuilder(@Nonnull BuilderManager builderManager, @Nonnull BuilderSupport support, boolean nullable) {
      Builder<T> builder = this.getBuilder(builderManager, support.getExecutionContext(), support.getParentSpawnable());
      if (!nullable && builder == null) {
         throw new NullPointerException(String.format("ReferenceHelper failed to get builder: %s", this.getClassType().getSimpleName()));
      } else {
         return builder;
      }
   }

   @Nullable
   public Builder<T> getBuilder(@Nonnull BuilderManager builderManager, ExecutionContext context, @Nullable Builder<?> parentSpawnable) {
      if (this.builder != null) {
         return this.builder;
      } else if (this.isInternalReference) {
         return this.internalReferenceResolver.getBuilder(this.referenceIndex, this.classType);
      } else if (this.referenceIndex >= 0) {
         Builder<T> builder = builderManager.tryGetCachedValidBuilder(this.referenceIndex, this.classType);
         if (builder == null) {
            throw new SkipSentryException(
               new IllegalStateException(String.format("Builder %s exists but is not valid!", builderManager.lookupName(this.referenceIndex)))
            );
         } else {
            return builder;
         }
      } else {
         String reference = this.fileReference.get(context);
         if (reference.equals("$Null")) {
            return null;
         } else {
            int idx = builderManager.getIndex(reference);
            if (idx >= 0) {
               if (parentSpawnable != null) {
                  parentSpawnable.addDynamicDependency(idx);
               }

               Builder<T> builder = builderManager.getCachedBuilder(idx, this.classType);
               String builderInterfaceCode = builder.getBuilderParameters().getInterfaceCode();
               this.validateComponentInterfaceMatch(builderInterfaceCode);
               return builder;
            } else if (!reference.isEmpty()) {
               throw new SkipSentryException(new IllegalStateException("Failed to find builder for: " + reference));
            } else {
               return null;
            }
         }
      }
   }

   @Override
   public void readConfig(
      @Nonnull JsonElement data,
      @Nonnull BuilderManager builderManager,
      @Nonnull BuilderParameters builderParameters,
      @Nonnull BuilderValidationHelper builderValidationHelper
   ) {
      this.readConfig(data, builderManager.getFactory(this.classType), builderManager, builderParameters, builderValidationHelper);
   }

   public void readConfig(
      @Nonnull JsonElement data,
      @Nonnull BuilderFactory<T> factory,
      @Nonnull BuilderManager builderManager,
      @Nonnull BuilderParameters builderParameters,
      @Nonnull BuilderValidationHelper builderValidationHelper
   ) {
      super.readConfig(data, builderManager, builderParameters, builderValidationHelper);
      if (data.isJsonNull()) {
         this.builder = null;
      } else if (data.isJsonPrimitive() && data.getAsJsonPrimitive().isString()) {
         builderValidationHelper.getReadErrors()
            .add(
               builderValidationHelper.getName()
                  + ": String reference '"
                  + data.getAsString()
                  + "' to a component is deprecated. Use the 'Reference' parameter instead."
            );
      } else {
         JsonObject jsonObject = data.isJsonObject() ? data.getAsJsonObject() : null;
         JsonElement referenceValue = jsonObject != null ? jsonObject.get("Reference") : null;
         if (referenceValue != null) {
            try {
               if (BuilderBase.readBoolean(jsonObject, "Local", false)) {
                  BuilderModifier.readModifierObject(
                     data.getAsJsonObject(),
                     builderParameters,
                     this.fileReference,
                     holder -> this.setInternalReference(holder, builderValidationHelper.getInternalReferenceResolver()),
                     modifier -> this.modifier = modifier,
                     builderValidationHelper.getStateMappingHelper(),
                     builderValidationHelper.getExtraInfo()
                  );
               } else {
                  JsonObject dataObj = data.getAsJsonObject();
                  BuilderModifier.readModifierObject(
                     dataObj,
                     builderParameters,
                     this.fileReference,
                     holder -> this.setFileReference(holder, dataObj, builderManager),
                     modifier -> this.modifier = modifier,
                     builderValidationHelper.getStateMappingHelper(),
                     builderValidationHelper.getExtraInfo()
                  );
               }

               FeatureEvaluatorHelper evaluatorHelper = builderValidationHelper.getFeatureEvaluatorHelper();
               if (evaluatorHelper != null) {
                  if (evaluatorHelper.canAddProvider()) {
                     evaluatorHelper.add(new ReferenceProviderEvaluator(this.referenceIndex, this.classType));
                     evaluatorHelper.setContainsReference();
                     return;
                  }

                  this.evaluatorHelper = evaluatorHelper;
               }
            } catch (IllegalStateException | IllegalArgumentException var9) {
               builderValidationHelper.getReadErrors().add(builderValidationHelper.getName() + ": " + var9.getMessage() + " at " + this.getBreadCrumbs());
            }
         } else {
            this.builder = factory.createBuilder(data);
            if (this.builder.isDeprecated()) {
               builderManager.checkIfDeprecated(this.builder, factory, data, builderParameters.getFileName(), this.getBreadCrumbs());
            }

            if (data.isJsonObject() && data.getAsJsonObject().has("$Label") && data.getAsJsonObject().get("$Label").isJsonPrimitive()) {
               this.builder.setLabel(data.getAsJsonObject().get("$Label").getAsString());
            } else {
               this.builder.setLabel(factory.getKeyName(data));
            }

            this.builder.readConfig(this, data, builderManager, builderParameters, builderValidationHelper);
         }
      }
   }

   protected void setInternalReference(@Nonnull StringHolder holder, InternalReferenceResolver referenceResolver) {
      this.isInternalReference = true;
      this.isReference = true;
      if (holder.isStatic()) {
         this.internalReferenceResolver = referenceResolver;
         this.referenceIndex = this.internalReferenceResolver.getOrCreateIndex(holder.get(null));
      }
   }

   protected void setFileReference(@Nonnull StringHolder holder, @Nonnull JsonObject jsonObject, @Nonnull BuilderManager builderManager) {
      this.isInternalReference = false;
      this.isReference = true;
      this.componentInterfaces = BuilderBase.readStringArray(jsonObject, "Interfaces", StringNotEmptyValidator.get(), null);
      this.isNullable = BuilderBase.readBoolean(jsonObject, "Nullable", false);
      if (holder.isStatic()) {
         this.referenceIndex = builderManager.getOrCreateIndex(holder.get(null));
         this.builderParameters.addDependency(this.referenceIndex);
      } else if (!StringArrayNotEmptyValidator.get().test(this.componentInterfaces)) {
         throw new SkipSentryException(
            new IllegalStateException("Computable references must define a list of 'Interfaces' to control which components can be attached.")
         );
      }
   }

   private void validateRequiredFeatures(@Nonnull Builder<T> builder, BuilderManager manager, ExecutionContext context) {
      builder.validateReferencedProvidedFeatures(manager, context);
      if (this.evaluatorHelper != null) {
         this.evaluatorHelper.validateProviderReferences(manager, context);
         builder.getEvaluatorHelper().validateComponentRequirements(this.evaluatorHelper, context);
      }
   }

   private void validateInstructionContext(@Nonnull Builder<T> builder, @Nonnull BuilderSupport support) {
      InstructionContextHelper instructionContextHelper = builder.getInstructionContextHelper();
      if (instructionContextHelper != null && !this.isInternalReference) {
         instructionContextHelper.validateComponentContext(support.getCurrentInstructionContext(), support.getCurrentComponentContext());
      }
   }

   private void validateComponentInterfaceMatch(String builderInterfaceCode) {
      for (String componentInterface : this.componentInterfaces) {
         if (componentInterface.equals(builderInterfaceCode)) {
            return;
         }
      }

      throw new SkipSentryException(
         new IllegalStateException(
            String.format("Component code %s does not match any of slot codes: %s.", builderInterfaceCode, Arrays.toString((Object[])this.componentInterfaces))
         )
      );
   }

   @Override
   public boolean isPresent() {
      return this.isFinal() || this.isReference;
   }

   public boolean isFinal() {
      return this.builder != null;
   }

   @Override
   public String getLabel() {
      return this.label;
   }

   public void setLabel(String label) {
      this.label = label;
   }
}
