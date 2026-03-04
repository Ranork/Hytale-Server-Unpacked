package com.hypixel.hytale.builtin.commandmacro;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType;
import javax.annotation.Nonnull;

public class MacroCommandParameter {
   @Nonnull
   public static final BuilderCodec<MacroCommandParameter> CODEC = BuilderCodec.builder(MacroCommandParameter.class, MacroCommandParameter::new)
      .append(new KeyedCodec<>("Name", Codec.STRING, true), (macroParameter, name) -> macroParameter.name = name, macroParameter -> macroParameter.name)
      .add()
      .append(
         new KeyedCodec<>("Description", Codec.STRING, true),
         (macroParameter, description) -> macroParameter.description = description,
         macroParameter -> macroParameter.description
      )
      .add()
      .append(
         new KeyedCodec<>("Requirement", new EnumCodec<>(MacroCommandParameter.ParameterRequirement.class), true),
         (macroParameter, requirement) -> macroParameter.requirement = requirement,
         macroParameter -> macroParameter.requirement
      )
      .add()
      .append(
         new KeyedCodec<>("ArgType", new EnumCodec<>(MacroCommandParameter.ArgumentTypeEnum.class)),
         (macroParameter, argumentType) -> macroParameter.argumentType = argumentType,
         macroParameter -> macroParameter.argumentType
      )
      .add()
      .append(
         new KeyedCodec<>("DefaultValue", Codec.STRING),
         (macroParameter, defaultValue) -> macroParameter.defaultValue = defaultValue,
         macroParameter -> macroParameter.defaultValue
      )
      .add()
      .append(
         new KeyedCodec<>("DefaultValueDescription", Codec.STRING),
         (macroParameter, defaultValueDescription) -> macroParameter.defaultValueDescription = defaultValueDescription,
         macroParameter -> macroParameter.defaultValueDescription
      )
      .add()
      .build();
   private String name;
   private String description;
   private MacroCommandParameter.ParameterRequirement requirement;
   private MacroCommandParameter.ArgumentTypeEnum argumentType;
   private String defaultValue;
   private String defaultValueDescription;

   public MacroCommandParameter.ParameterRequirement getRequirement() {
      return this.requirement;
   }

   public String getName() {
      return this.name;
   }

   public String getDescription() {
      return this.description;
   }

   public MacroCommandParameter.ArgumentTypeEnum getArgumentType() {
      return this.argumentType;
   }

   public String getDefaultValue() {
      return this.defaultValue;
   }

   public String getDefaultValueDescription() {
      return this.defaultValueDescription;
   }

   public static enum ArgumentTypeEnum {
      BOOLEAN(ArgTypes.BOOLEAN),
      INTEGER(ArgTypes.INTEGER),
      STRING(ArgTypes.STRING),
      FLOAT(ArgTypes.FLOAT),
      DOUBLE(ArgTypes.DOUBLE),
      UUID(ArgTypes.UUID),
      RELATIVE_DOUBLE_COORD(ArgTypes.RELATIVE_DOUBLE_COORD),
      RELATIVE_INT_COORD(ArgTypes.RELATIVE_INT_COORD),
      RELATIVE_INTEGER(ArgTypes.RELATIVE_INTEGER),
      INT_RANGE(ArgTypes.INT_RANGE),
      RELATIVE_INT_RANGE(ArgTypes.RELATIVE_INT_RANGE),
      VECTOR3I(ArgTypes.VECTOR3I),
      RELATIVE_VECTOR3I(ArgTypes.RELATIVE_VECTOR3I),
      BLOCK_ID(ArgTypes.BLOCK_ID),
      WEIGHTED_BLOCK_TYPE(ArgTypes.WEIGHTED_BLOCK_TYPE),
      BLOCK_PATTERN(ArgTypes.BLOCK_PATTERN),
      BLOCK_MASK(ArgTypes.BLOCK_MASK),
      WORLD(ArgTypes.WORLD),
      RELATIVE_BLOCK_POSITION(ArgTypes.RELATIVE_BLOCK_POSITION),
      RELATIVE_POSITION(ArgTypes.RELATIVE_POSITION),
      ROTATION(ArgTypes.ROTATION),
      MODEL_ASSET(ArgTypes.MODEL_ASSET),
      WEATHER_ASSET(ArgTypes.WEATHER_ASSET),
      INTERACTION_ASSET(ArgTypes.INTERACTION_ASSET),
      EFFECT_ASSET(ArgTypes.EFFECT_ASSET),
      ENVIRONMENT_ASSET(ArgTypes.ENVIRONMENT_ASSET),
      ITEM_ASSET(ArgTypes.ITEM_ASSET),
      BLOCK_TYPE_ASSET(ArgTypes.BLOCK_TYPE_ASSET),
      BLOCK_TYPE_KEY(ArgTypes.BLOCK_TYPE_KEY);

      private final ArgumentType<?> argumentType;

      private ArgumentTypeEnum(ArgumentType<?> argumentType) {
         this.argumentType = argumentType;
      }

      public ArgumentType<?> getArgumentType() {
         return this.argumentType;
      }
   }

   public static enum ParameterRequirement {
      REQUIRED,
      OPTIONAL,
      DEFAULT,
      FLAG;
   }
}
