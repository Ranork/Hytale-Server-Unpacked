package com.hypixel.hytale.server.npc.asset.builder.holder;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderParameters;
import com.hypixel.hytale.server.npc.asset.builder.expression.BuilderExpressionStaticBoolean;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;

public class BooleanHolder extends ValueHolder {
   protected List<BiConsumer<ExecutionContext, Boolean>> relationValidators;

   public BooleanHolder() {
      super(ValueType.BOOLEAN);
   }

   @Override
   public void readJSON(@Nonnull JsonElement requiredJsonElement, String name, @Nonnull BuilderParameters builderParameters) {
      super.readJSON(requiredJsonElement, name, builderParameters);
   }

   @Override
   public void validate(ExecutionContext context) {
      this.get(context);
   }

   public void readJSON(JsonElement optionalJsonElement, boolean defaultValue, String name, @Nonnull BuilderParameters builderParameters) {
      super.readJSON(optionalJsonElement, () -> new BuilderExpressionStaticBoolean(defaultValue), name, builderParameters);
   }

   public boolean get(ExecutionContext executionContext) {
      boolean value = this.rawGet(executionContext);
      this.validateRelations(executionContext, value);
      return value;
   }

   public boolean rawGet(ExecutionContext executionContext) {
      return this.expression.getBoolean(executionContext);
   }

   public void addRelationValidator(BiConsumer<ExecutionContext, Boolean> validator) {
      if (this.relationValidators == null) {
         this.relationValidators = new ObjectArrayList();
      }

      this.relationValidators.add(validator);
   }

   protected void validateRelations(ExecutionContext executionContext, boolean value) {
      if (this.relationValidators != null) {
         for (BiConsumer<ExecutionContext, Boolean> executionContextConsumer : this.relationValidators) {
            executionContextConsumer.accept(executionContext, value);
         }
      }
   }
}
