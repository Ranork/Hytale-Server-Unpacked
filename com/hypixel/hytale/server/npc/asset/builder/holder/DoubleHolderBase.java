package com.hypixel.hytale.server.npc.asset.builder.holder;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderParameters;
import com.hypixel.hytale.server.npc.asset.builder.expression.BuilderExpressionStaticNumber;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleValidator;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.function.ObjDoubleConsumer;
import javax.annotation.Nonnull;

public abstract class DoubleHolderBase extends ValueHolder {
   protected List<ObjDoubleConsumer<ExecutionContext>> relationValidators;
   protected DoubleValidator doubleValidator;

   protected DoubleHolderBase() {
      super(ValueType.NUMBER);
   }

   public void readJSON(@Nonnull JsonElement requiredJsonElement, DoubleValidator validator, String name, @Nonnull BuilderParameters builderParameters) {
      this.readJSON(requiredJsonElement, name, builderParameters);
      this.doubleValidator = validator;
      if (this.isStatic()) {
         this.validate(this.expression.getNumber(null));
      }
   }

   public void readJSON(
      JsonElement optionalJsonElement, double defaultValue, DoubleValidator validator, String name, @Nonnull BuilderParameters builderParameters
   ) {
      this.readJSON(optionalJsonElement, () -> new BuilderExpressionStaticNumber(defaultValue), name, builderParameters);
      this.doubleValidator = validator;
      if (this.isStatic()) {
         this.validate(this.expression.getNumber(null));
      }
   }

   public void addRelationValidator(ObjDoubleConsumer<ExecutionContext> validator) {
      if (this.relationValidators == null) {
         this.relationValidators = new ObjectArrayList();
      }

      this.relationValidators.add(validator);
   }

   protected void validateRelations(ExecutionContext executionContext, double value) {
      if (this.relationValidators != null) {
         for (ObjDoubleConsumer<ExecutionContext> executionContextConsumer : this.relationValidators) {
            executionContextConsumer.accept(executionContext, value);
         }
      }
   }

   public double rawGet(ExecutionContext executionContext) {
      double value = this.expression.getNumber(executionContext);
      if (!this.isStatic()) {
         this.validate(value);
      }

      return value;
   }

   public void validate(double value) {
      if (this.doubleValidator != null && !this.doubleValidator.test(value)) {
         throw new IllegalStateException(this.doubleValidator.errorMessage(value, this.name));
      }
   }
}
