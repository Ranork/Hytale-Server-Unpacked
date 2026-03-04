package com.hypixel.hytale.server.npc.asset.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BuilderObjectArrayHelper<T, U> extends BuilderObjectHelper<T> {
   @Nullable
   protected BuilderObjectReferenceHelper[] builders;
   protected String label;

   public BuilderObjectArrayHelper(Class<?> classType, BuilderContext owner) {
      super(classType, owner);
   }

   @Override
   public void readConfig(
      @Nonnull JsonElement data,
      @Nonnull BuilderManager builderManager,
      @Nonnull BuilderParameters builderParameters,
      @Nonnull BuilderValidationHelper builderValidationHelper
   ) {
      super.readConfig(data, builderManager, builderParameters, builderValidationHelper);
      if (data.isJsonNull()) {
         this.builders = null;
      } else {
         if (!data.isJsonArray()) {
            String string = data.toString();
            throw new IllegalArgumentException(
               String.format(
                  "Expected a JSON array of '%s' at %s (JSON: %s)",
                  this.classType.getSimpleName(),
                  this.getBreadCrumbs(),
                  string.length() > 60 ? string.substring(60) + "..." : string
               )
            );
         }

         JsonArray array = data.getAsJsonArray();
         BuilderFactory<U> factory = builderManager.getFactory(this.classType);
         this.builders = new BuilderObjectReferenceHelper[array.size()];
         int index = 0;

         for (JsonElement element : array) {
            BuilderObjectReferenceHelper<U> builderObjectReferenceHelper = this.createReferenceHelper();
            builderObjectReferenceHelper.readConfig(element, factory, builderManager, builderParameters, builderValidationHelper);
            if (!builderObjectReferenceHelper.isPresent()) {
               throw new IllegalStateException("Missing builder reference at " + this.getBreadCrumbs() + ": " + builderParameters.getFileName());
            }

            this.builders[index++] = builderObjectReferenceHelper;
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
      if (this.hasNoElements()) {
         return true;
      } else {
         boolean result = true;

         for (BuilderObjectReferenceHelper builder : this.builders) {
            if (!builder.excludeFromRegularBuild()) {
               result &= builder.validate(configName, loadTimeValidationHelper, manager, context, globalScope, errors);
            }
         }

         return result;
      }
   }

   @Override
   public boolean isPresent() {
      return this.builders != null;
   }

   public boolean isEmpty() {
      return this.isPresent() && this.builders.length == 0;
   }

   public boolean hasNoElements() {
      return this.builders == null || this.builders.length == 0;
   }

   @Override
   public String getLabel() {
      return this.label;
   }

   public void setLabel(String label) {
      this.label = label;
   }

   @Nonnull
   protected BuilderObjectReferenceHelper<U> createReferenceHelper() {
      return new BuilderObjectReferenceHelper(this.classType, this);
   }
}
