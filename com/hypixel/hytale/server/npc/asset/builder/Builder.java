package com.hypixel.hytale.server.npc.asset.builder;

import com.google.gson.JsonElement;
import com.hypixel.hytale.codec.schema.NamedSchema;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.SchemaConvertable;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Builder<T> extends BuilderContext, SchemaConvertable<Void>, NamedSchema {
   @Nullable
   T build(BuilderSupport var1);

   boolean validate(String var1, NPCLoadTimeValidationHelper var2, ExecutionContext var3, Scope var4, List<String> var5);

   void readConfig(BuilderContext var1, JsonElement var2, BuilderManager var3, BuilderParameters var4, BuilderValidationHelper var5);

   void ignoreAttribute(String var1);

   Class<T> category();

   void setTypeName(String var1);

   String getTypeName();

   void setLabel(String var1);

   @Nonnull
   @Override
   Schema toSchema(@Nonnull SchemaContext var1);

   BuilderDescriptor getDescriptor(String var1, String var2, BuilderManager var3);

   default boolean isDeprecated() {
      return this.getBuilderDescriptorState() == BuilderDescriptorState.Deprecated;
   }

   @Nullable
   BuilderDescriptorState getBuilderDescriptorState();

   IntSet getDependencies();

   default boolean hasDynamicDependencies() {
      return false;
   }

   default void addDynamicDependency(int builderIndex) {
      throw new IllegalStateException("Builder: Adding dynamic dependencies is not supported");
   }

   @Nullable
   default IntSet getDynamicDependencies() {
      return null;
   }

   default void clearDynamicDependencies() {
   }

   BuilderParameters getBuilderParameters();

   FeatureEvaluatorHelper getEvaluatorHelper();

   StateMappingHelper getStateMappingHelper();

   InstructionContextHelper getInstructionContextHelper();

   boolean canRequireFeature();

   void validateReferencedProvidedFeatures(BuilderManager var1, ExecutionContext var2);

   boolean excludeFromRegularBuilding();

   boolean isEnabled(ExecutionContext var1);

   default boolean isSpawnable() {
      return false;
   }
}
