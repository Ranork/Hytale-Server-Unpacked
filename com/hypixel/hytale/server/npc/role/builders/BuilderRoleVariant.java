package com.hypixel.hytale.server.npc.role.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.function.function.TriFunction;
import com.hypixel.hytale.function.function.TriToIntFunction;
import com.hypixel.hytale.logger.sentry.SkipSentryException;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderInfo;
import com.hypixel.hytale.server.npc.asset.builder.BuilderModifier;
import com.hypixel.hytale.server.npc.asset.builder.BuilderParameters;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.SpawnableWithModelBuilder;
import com.hypixel.hytale.server.npc.asset.builder.StateMappingHelper;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import com.hypixel.hytale.server.spawning.ISpawnableWithModel;
import com.hypixel.hytale.server.spawning.SpawnTestResult;
import com.hypixel.hytale.server.spawning.SpawningContext;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderRoleVariant extends SpawnableWithModelBuilder<Role> {
   protected final StringHolder reference = new StringHolder();
   protected int referenceIndex;
   protected BuilderModifier modifier;

   @Nullable
   public Role build(@Nonnull BuilderSupport builderSupport) {
      return this.executeOnSuperRole(builderSupport, Builder::build, () -> null);
   }

   @Override
   public StateMappingHelper getStateMappingHelper() {
      Builder<Object> parentBuilder = this.builderManager.getCachedBuilder(this.referenceIndex, Role.class);
      return parentBuilder.getStateMappingHelper();
   }

   @Override
   public boolean validate(
      String configName, @Nonnull NPCLoadTimeValidationHelper validationHelper, @Nonnull ExecutionContext context, Scope globalScope, List<String> errors
   ) {
      Builder<Object> roleBuilder = this.builderManager.getCachedBuilder(this.referenceIndex, Role.class);
      if (!(roleBuilder instanceof ISpawnableWithModel)) {
         NPCPlugin.get().getLogger().at(Level.SEVERE).log("Variant %s is of a non-spawnable template!", configName);
         return false;
      } else {
         validationHelper.setIsVariant();
         String combatConfig = this.modifier.getCombatConfig();
         if (combatConfig != null && context.getCombatConfig() == null) {
            context.setCombatConfig(combatConfig);
         }

         Map<String, String> interactionVars = this.modifier.getInteractionVars();
         if (interactionVars != null && context.getInteractionVars() == null) {
            context.setInteractionVars(interactionVars);
         }

         BuilderParameters builderParameters = roleBuilder.getBuilderParameters();
         Scope newScope = this.modifier.createScope(context, builderParameters, null);
         Scope oldScope = context.setScope(newScope);
         boolean result = roleBuilder.validate(configName, validationHelper, context, context.getScope(), errors);
         context.setScope(oldScope);
         return result;
      }
   }

   @Nonnull
   @Override
   public Builder<Role> readConfig(@Nonnull JsonElement data) {
      if (this.isCreatingSchema()) {
         Map<String, Schema> props = this.builderSchema.getProperties();
         StringSchema schema = new StringSchema();
         schema.setHytaleAssetRef("NPCRole");
         props.put("Reference", schema);
         props.put("Modify", BuilderModifier.toSchema(this.builderSchemaContext));
         return this;
      } else {
         if (!this.isCreatingDescriptor()) {
            BuilderModifier.readModifierObject(
               this.expectJsonObject(data, this.getLabel()),
               this.getBuilderParameters(),
               this.reference,
               holder -> {},
               modifier -> this.modifier = modifier,
               this.stateHelper,
               this.extraInfo
            );
            if (!this.reference.isStatic()) {
               throw new IllegalStateException("Computable component references are not supported for Role Variants");
            }

            this.referenceIndex = this.getBuilderManager().getOrCreateIndex(this.reference.get(null));
            this.builderParameters.addDependency(this.referenceIndex);
         }

         this.ignoreAttribute("Reference");
         this.ignoreAttribute("Modify");
         this.ignoreAttribute("$Label");
         return this;
      }
   }

   @Nonnull
   @Override
   public Class<Role> category() {
      return Role.class;
   }

   @Nonnull
   @Override
   public String getIdentifier() {
      BuilderInfo builderInfo = NPCPlugin.get().getBuilderInfo(this);
      Objects.requireNonNull(builderInfo, "Have builder but can't get builderInfo for it");
      return builderInfo.getKeyName();
   }

   @Nonnull
   @Override
   public SpawnTestResult canSpawn(@Nonnull SpawningContext spawningContext) {
      return this.executeOnSuperRole(
         spawningContext, (roleBuilder, _context) -> ((ISpawnableWithModel)roleBuilder).canSpawn(_context), () -> SpawnTestResult.FAIL_NOT_SPAWNABLE
      );
   }

   @Nullable
   @Override
   public String getSpawnModelName(@Nonnull ExecutionContext context, Scope modifierScope) {
      return this.executeOnSuperRole(
         context,
         modifierScope,
         (roleBuilder, _context, _modifierScope) -> ((ISpawnableWithModel)roleBuilder).getSpawnModelName(_context, _modifierScope),
         () -> null
      );
   }

   @Override
   public Scope createModifierScope(@Nonnull ExecutionContext executionContext) {
      Scope originalScope = executionContext.getScope();
      Builder<Role> roleBuilder = this;
      Scope scope = null;

      do {
         BuilderRoleVariant variantBuilder = (BuilderRoleVariant)roleBuilder;
         roleBuilder = this.builderManager.getCachedBuilder(variantBuilder.referenceIndex, Role.class);
         if (!(roleBuilder instanceof ISpawnableWithModel)) {
            throw new IllegalStateException("Cannot instantiate a variant of something that isn't a spawnable role!");
         }

         scope = variantBuilder.modifier.createScope(executionContext, roleBuilder.getBuilderParameters(), scope);
         executionContext.setScope(scope);
      } while (roleBuilder instanceof BuilderRoleVariant);

      executionContext.setScope(originalScope);
      return scope;
   }

   @Nonnull
   @Override
   public Scope createExecutionScope() {
      return this.getBuilderParameters().createScope();
   }

   @Override
   public void markNeedsReload() {
      NPCPlugin.get().setRoleBuilderNeedsReload(this);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Create a variant from an existing NPC JSON file";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Create a variant from an existing NPC JSON file";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.WorkInProgress;
   }

   @Override
   public final boolean isEnabled(ExecutionContext context) {
      return true;
   }

   public int getReferenceIndex() {
      return this.referenceIndex;
   }

   @Override
   public boolean isMemory(@Nonnull ExecutionContext context, Scope modifierScope) {
      Boolean result = this.executeOnSuperRole(
         context, modifierScope, (roleBuilder, _context, _modifierScope) -> ((ISpawnableWithModel)roleBuilder).isMemory(_context, _modifierScope), () -> null
      );
      return result != null ? result : false;
   }

   @Nullable
   @Override
   public String getMemoriesCategory(@Nonnull ExecutionContext context, Scope modifierScope) {
      return this.executeOnSuperRole(
         context,
         modifierScope,
         (roleBuilder, _context, _modifierScope) -> ((ISpawnableWithModel)roleBuilder).getMemoriesCategory(_context, _modifierScope),
         () -> null
      );
   }

   @Nullable
   @Override
   public String getMemoriesNameOverride(@Nonnull ExecutionContext context, Scope modifierScope) {
      return this.executeOnSuperRole(
         context,
         modifierScope,
         (roleBuilder, _context, _modifierScope) -> ((ISpawnableWithModel)roleBuilder).getMemoriesNameOverride(_context, _modifierScope),
         () -> null
      );
   }

   @Nonnull
   @Override
   public String getNameTranslationKey(ExecutionContext context, Scope modifierScope) {
      return this.executeOnSuperRole(
         context,
         modifierScope,
         (roleBuilder, _context, _modifierScope) -> ((ISpawnableWithModel)roleBuilder).getNameTranslationKey(_context, _modifierScope),
         () -> {
            throw new SkipSentryException(new IllegalStateException("Failed to get translation key for role!"));
         }
      );
   }

   protected <V> V executeOnSuperRole(
      @Nonnull BuilderSupport builderSupport, @Nonnull BiFunction<Builder<Role>, BuilderSupport, V> func, @Nonnull Supplier<V> failed
   ) {
      Builder<Role> roleBuilder = builderSupport.getBuilderManager().getCachedBuilder(this.referenceIndex, Role.class);
      if (!(roleBuilder instanceof ISpawnableWithModel)) {
         return failed.get();
      } else {
         BuilderParameters builderParameters = roleBuilder.getBuilderParameters();
         Scope newScope = this.modifier.createScope(builderSupport, builderParameters, null);
         ExecutionContext context = builderSupport.getExecutionContext();
         String combatConfig = this.modifier.getCombatConfig();
         if (combatConfig != null && context.getCombatConfig() == null) {
            context.setCombatConfig(combatConfig);
         }

         Map<String, String> interactionVars = this.modifier.getInteractionVars();
         if (interactionVars != null && context.getInteractionVars() == null) {
            context.setInteractionVars(interactionVars);
         }

         Scope oldScope = context.setScope(newScope);
         builderSupport.setGlobalScope(newScope);
         V v = func.apply(roleBuilder, builderSupport);
         context.setScope(oldScope);
         return v;
      }
   }

   protected <V> V executeOnSuperRole(
      @Nonnull SpawningContext spawningContext, @Nonnull BiFunction<Builder<Role>, SpawningContext, V> func, @Nonnull Supplier<V> failed
   ) {
      Builder<Role> roleBuilder = this.builderManager.getCachedBuilder(this.referenceIndex, Role.class);
      if (!(roleBuilder instanceof ISpawnableWithModel)) {
         return failed.get();
      } else {
         ExecutionContext executionContext = spawningContext.getExecutionContext();
         Scope oldScope = executionContext.setScope(spawningContext.getModifierScope());
         V v = func.apply(roleBuilder, spawningContext);
         executionContext.setScope(oldScope);
         return v;
      }
   }

   protected <V> V executeOnSuperRole(
      @Nonnull ExecutionContext context, Scope modifierScope, @Nonnull TriFunction<Builder<Role>, ExecutionContext, Scope, V> func, @Nonnull Supplier<V> failed
   ) {
      Builder<Role> roleBuilder = this.builderManager.getCachedBuilder(this.referenceIndex, Role.class);
      if (!(roleBuilder instanceof ISpawnableWithModel)) {
         return failed.get();
      } else {
         Scope oldScope = context.setScope(modifierScope);
         V v = func.apply(roleBuilder, context, modifierScope);
         context.setScope(oldScope);
         return v;
      }
   }

   protected int executeOnSuperRole(
      @Nonnull ExecutionContext context, Scope modifierScope, @Nonnull TriToIntFunction<Builder<Role>, ExecutionContext, Scope> func, int failed
   ) {
      Builder<Role> roleBuilder = this.builderManager.getCachedBuilder(this.referenceIndex, Role.class);
      if (!(roleBuilder instanceof ISpawnableWithModel)) {
         return failed;
      } else {
         Scope oldScope = context.setScope(modifierScope);
         int v = func.apply(roleBuilder, context, modifierScope);
         context.setScope(oldScope);
         return v;
      }
   }
}
