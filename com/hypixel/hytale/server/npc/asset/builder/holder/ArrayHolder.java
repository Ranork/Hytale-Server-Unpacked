package com.hypixel.hytale.server.npc.asset.builder.holder;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderParameters;
import com.hypixel.hytale.server.npc.asset.builder.expression.BuilderExpressionStaticBooleanArray;
import com.hypixel.hytale.server.npc.asset.builder.expression.BuilderExpressionStaticNumberArray;
import com.hypixel.hytale.server.npc.asset.builder.expression.BuilderExpressionStaticStringArray;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import javax.annotation.Nonnull;

public abstract class ArrayHolder extends ValueHolder {
   protected int minLength;
   protected int maxLength = Integer.MAX_VALUE;

   public ArrayHolder(ValueType valueType) {
      super(valueType);
   }

   protected void readJSON(@Nonnull JsonElement requiredJsonElement, int minLength, int maxLength, String name, @Nonnull BuilderParameters builderParameters) {
      this.setLength(minLength, maxLength);
      this.readJSON(requiredJsonElement, name, builderParameters);
   }

   protected void readJSON(
      JsonElement optionalJsonElement, int minLength, int maxLength, double[] defaultValue, String name, @Nonnull BuilderParameters builderParameters
   ) {
      this.setLength(minLength, maxLength);
      this.readJSON(optionalJsonElement, () -> new BuilderExpressionStaticNumberArray(defaultValue), name, builderParameters);
   }

   protected void readJSON(
      JsonElement optionalJsonElement, int minLength, int maxLength, String[] defaultValue, String name, @Nonnull BuilderParameters builderParameters
   ) {
      this.setLength(minLength, maxLength);
      this.readJSON(optionalJsonElement, () -> new BuilderExpressionStaticStringArray(defaultValue), name, builderParameters);
   }

   protected void readJSON(
      JsonElement optionalJsonElement, int minLength, int maxLength, boolean[] defaultValue, String name, @Nonnull BuilderParameters builderParameters
   ) {
      this.setLength(minLength, maxLength);
      this.readJSON(optionalJsonElement, () -> new BuilderExpressionStaticBooleanArray(defaultValue), name, builderParameters);
   }

   protected void validateLength(int length) {
      if (length < this.minLength || length > this.maxLength) {
         StringBuilder errorString = new StringBuilder(100);
         errorString.append(this.name).append(": Invalid array size in array holder (Should be ");
         if (this.minLength == this.maxLength) {
            errorString.append(this.minLength);
         } else if (this.maxLength < Integer.MAX_VALUE) {
            errorString.append("between ").append(this.minLength).append(" and ").append(this.maxLength);
         } else {
            errorString.append("a minimum length of ").append(this.minLength);
         }

         errorString.append(" but is ").append(length).append(')');
         throw new IllegalStateException(errorString.toString());
      }
   }

   protected void setLength(int minLength, int maxLength) {
      if (minLength > maxLength) {
         throw new IllegalArgumentException("Illegal length for array in array holder specified");
      } else if (minLength < 0) {
         throw new IllegalArgumentException("Illegal minimum length for array in array holder specified");
      } else {
         this.minLength = minLength;
         this.maxLength = maxLength;
      }
   }

   protected void setLength(int length) {
      this.setLength(length, length);
   }
}
