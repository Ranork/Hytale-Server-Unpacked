package com.hypixel.hytale.server.npc.asset.builder.holder;

import com.google.gson.JsonElement;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.npc.asset.builder.BuilderParameters;
import com.hypixel.hytale.server.npc.asset.builder.expression.BuilderExpression;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ValueHolder {
   protected static final boolean LOG_VALUES = false;
   protected static final HytaleLogger LOGGER = HytaleLogger.get("BuilderManager");
   protected ValueType valueType;
   protected String name;
   protected BuilderExpression expression;

   protected ValueHolder(ValueType valueType) {
      this.valueType = valueType;
   }

   public abstract void validate(ExecutionContext var1);

   protected void readJSON(@Nonnull JsonElement requiredJsonElement, String name, @Nonnull BuilderParameters builderParameters) {
      this.name = name;
      this.expression = BuilderExpression.fromJSON(requiredJsonElement, builderParameters, this.valueType);
   }

   protected void readJSON(
      @Nullable JsonElement optionalJsonElement, @Nonnull Supplier<BuilderExpression> defaultValue, String name, @Nonnull BuilderParameters builderParameters
   ) {
      this.name = name;
      this.expression = optionalJsonElement != null ? BuilderExpression.fromJSON(optionalJsonElement, builderParameters, this.valueType) : defaultValue.get();
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public boolean isStatic() {
      return this.expression.isStatic();
   }

   public String getExpressionString() {
      return this.expression.getExpression();
   }
}
