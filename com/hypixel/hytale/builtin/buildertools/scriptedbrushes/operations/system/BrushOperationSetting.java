package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system;

import com.hypixel.hytale.codec.validation.Validator;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BrushOperationSetting<T> {
   private final String name;
   private final String description;
   private String input;
   private final T defaultValue;
   @Nullable
   private T value;
   private final ArgumentType<T> argumentType;
   @Nullable
   private final Validator<T> valueValidator;
   @Nullable
   private final Function<BrushOperationSetting<T>, String> toStringFunction;

   public BrushOperationSetting(String name, String description, T defaultValue, ArgumentType<T> argumentType) {
      this(name, description, defaultValue, argumentType, null, null);
   }

   public BrushOperationSetting(
      String name, String description, T defaultValue, ArgumentType<T> argumentType, Function<BrushOperationSetting<T>, String> toStringFunction
   ) {
      this(name, description, defaultValue, argumentType, null, toStringFunction);
   }

   public BrushOperationSetting(
      String name,
      String description,
      T defaultValue,
      ArgumentType<T> argumentType,
      @Nullable Validator<T> valueValidator,
      @Nullable Function<BrushOperationSetting<T>, String> toStringFunction
   ) {
      this.name = name;
      this.description = description;
      this.defaultValue = defaultValue;
      this.value = defaultValue;
      this.argumentType = argumentType;
      this.valueValidator = valueValidator;
      this.toStringFunction = toStringFunction;
   }

   @Nonnull
   public BrushOperationSetting<T> setValue(T value) {
      this.value = value;
      return this;
   }

   @Nonnull
   public BrushOperationSetting<T> setValueUnsafe(String input, Object value) {
      this.input = input;
      this.value = (T)value;
      return this;
   }

   @Nonnull
   public ParseResult parseAndSetValue(String[] input) {
      ParseResult parseResult = new ParseResult();
      T newValue = this.argumentType.parse(input, parseResult);
      if (!parseResult.failed()) {
         this.value = newValue;
      }

      return parseResult;
   }

   @Nullable
   public String getInput() {
      return this.input;
   }

   public String getName() {
      return this.name;
   }

   public String getDescription() {
      return this.description;
   }

   public T getDefaultValue() {
      return this.defaultValue;
   }

   public ArgumentType<T> getArgumentType() {
      return this.argumentType;
   }

   @Nullable
   public Validator<T> getValueValidator() {
      return this.valueValidator;
   }

   @Nullable
   public T getValue() {
      return this.value;
   }

   public String getValueString() {
      return this.toStringFunction != null ? this.toStringFunction.apply(this) : this.value.toString();
   }
}
