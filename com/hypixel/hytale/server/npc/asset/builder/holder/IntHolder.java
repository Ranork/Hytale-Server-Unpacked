package com.hypixel.hytale.server.npc.asset.builder.holder;

import com.google.gson.JsonElement;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.npc.asset.builder.BuilderParameters;
import com.hypixel.hytale.server.npc.asset.builder.expression.BuilderExpressionStaticNumber;
import com.hypixel.hytale.server.npc.asset.builder.validators.IntValidator;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.function.ObjIntConsumer;
import javax.annotation.Nonnull;

public class IntHolder extends ValueHolder {
   protected List<ObjIntConsumer<ExecutionContext>> relationValidators;
   protected IntValidator intValidator;

   public IntHolder() {
      super(ValueType.NUMBER);
   }

   @Override
   public void validate(ExecutionContext context) {
      this.get(context);
   }

   public void readJSON(@Nonnull JsonElement requiredJsonElement, IntValidator validator, String name, @Nonnull BuilderParameters builderParameters) {
      this.readJSON(requiredJsonElement, name, builderParameters);
      this.intValidator = validator;
      if (this.isStatic()) {
         this.validate(MathUtil.floor(this.expression.getNumber(null)));
      }
   }

   public void readJSON(JsonElement optionalJsonElement, int defaultValue, IntValidator validator, String name, @Nonnull BuilderParameters builderParameters) {
      this.readJSON(optionalJsonElement, () -> new BuilderExpressionStaticNumber(defaultValue), name, builderParameters);
      this.intValidator = validator;
      if (this.isStatic()) {
         this.validate(MathUtil.floor(this.expression.getNumber(null)));
      }
   }

   public int get(ExecutionContext executionContext) {
      int value = this.rawGet(executionContext);
      this.validateRelations(executionContext, value);
      return value;
   }

   public int rawGet(ExecutionContext executionContext) {
      int value = MathUtil.floor(this.expression.getNumber(executionContext));
      if (!this.isStatic()) {
         this.validate(value);
      }

      return value;
   }

   public void validate(int value) {
      if (this.intValidator != null && !this.intValidator.test(value)) {
         throw new IllegalStateException(this.intValidator.errorMessage(value, this.name));
      }
   }

   public void addRelationValidator(ObjIntConsumer<ExecutionContext> validator) {
      if (this.relationValidators == null) {
         this.relationValidators = new ObjectArrayList();
      }

      this.relationValidators.add(validator);
   }

   protected void validateRelations(ExecutionContext executionContext, int value) {
      if (this.relationValidators != null) {
         for (ObjIntConsumer<ExecutionContext> executionContextConsumer : this.relationValidators) {
            executionContextConsumer.accept(executionContext, value);
         }
      }
   }
}
