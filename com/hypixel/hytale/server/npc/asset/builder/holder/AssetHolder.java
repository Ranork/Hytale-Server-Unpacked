package com.hypixel.hytale.server.npc.asset.builder.holder;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderBase;
import com.hypixel.hytale.server.npc.asset.builder.BuilderParameters;
import com.hypixel.hytale.server.npc.asset.builder.expression.BuilderExpressionStaticString;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import javax.annotation.Nonnull;

public class AssetHolder extends StringHolderBase {
   protected AssetValidator assetValidator;

   @Override
   public void validate(ExecutionContext context) {
      this.get(context);
   }

   public void readJSON(@Nonnull JsonElement requiredJsonElement, AssetValidator validator, String name, @Nonnull BuilderParameters builderParameters) {
      this.readJSON(requiredJsonElement, name, builderParameters);
      this.assetValidator = validator;
   }

   public void readJSON(
      JsonElement optionalJsonElement, String defaultValue, AssetValidator validator, String name, @Nonnull BuilderParameters builderParameters
   ) {
      this.readJSON(optionalJsonElement, () -> new BuilderExpressionStaticString(defaultValue), name, builderParameters);
      this.assetValidator = validator;
   }

   public String get(ExecutionContext executionContext) {
      String value = this.rawGet(executionContext);
      this.validateRelations(executionContext, value);
      return value;
   }

   public String rawGet(ExecutionContext executionContext) {
      String value = this.expression.getString(executionContext);
      if (this.assetValidator != null) {
         BuilderBase.validateAsset(value, this.assetValidator, this.name, true);
      }

      return value;
   }

   public void staticValidate() {
      if (this.assetValidator != null && this.isStatic()) {
         BuilderBase.validateAsset(this.expression.getString(null), this.assetValidator, this.name, true);
      }
   }
}
