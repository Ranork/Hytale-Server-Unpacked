package com.hypixel.hytale.server.npc.asset.builder.holder;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderBase;
import com.hypixel.hytale.server.npc.asset.builder.BuilderParameters;
import com.hypixel.hytale.server.npc.asset.builder.expression.BuilderExpressionStaticString;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;

public class EnumHolder<E extends Enum<E>> extends StringHolderBase {
   protected List<BiConsumer<ExecutionContext, E>> enumRelationValidators;
   private E[] enumConstants;
   private E value;

   @Override
   public void validate(ExecutionContext context) {
      this.get(context);
   }

   public void readJSON(@Nonnull JsonElement requiredJsonElement, Class<E> clazz, String name, @Nonnull BuilderParameters builderParameters) {
      this.enumConstants = clazz.getEnumConstants();
      this.readJSON(requiredJsonElement, name, builderParameters);
      if (this.isStatic()) {
         this.value = BuilderBase.stringToEnum(this.expression.getString(null), this.enumConstants, this.getName());
      }
   }

   public void readJSON(JsonElement optionalJsonElement, Class<E> clazz, @Nonnull E defaultValue, String name, @Nonnull BuilderParameters builderParameters) {
      this.enumConstants = clazz.getEnumConstants();
      this.readJSON(optionalJsonElement, () -> new BuilderExpressionStaticString(defaultValue.toString()), name, builderParameters);
      if (this.isStatic()) {
         this.value = BuilderBase.stringToEnum(this.expression.getString(null), this.enumConstants, this.getName());
      }
   }

   public E get(ExecutionContext executionContext) {
      E value = this.rawGet(executionContext);
      this.validateEnumRelations(executionContext, value);
      return value;
   }

   public void addEnumRelationValidator(BiConsumer<ExecutionContext, E> validator) {
      if (this.enumRelationValidators == null) {
         this.enumRelationValidators = new ObjectArrayList();
      }

      this.enumRelationValidators.add(validator);
   }

   public E rawGet(ExecutionContext executionContext) {
      if (!this.isStatic()) {
         this.value = BuilderBase.stringToEnum(this.expression.getString(executionContext), this.enumConstants, this.getName());
      }

      return this.value;
   }

   private void validateEnumRelations(ExecutionContext context, E value) {
      if (this.enumRelationValidators != null) {
         for (BiConsumer<ExecutionContext, E> executionContextConsumer : this.enumRelationValidators) {
            executionContextConsumer.accept(context, value);
         }
      }
   }
}
