package com.hypixel.hytale.server.npc.asset.builder.validators;

import com.google.gson.GsonBuilder;
import javax.annotation.Nonnull;

public class ValidatorTypeRegistry {
   @Nonnull
   public static GsonBuilder registerTypes(@Nonnull GsonBuilder gsonBuilder) {
      SubTypeTypeAdapterFactory factory = SubTypeTypeAdapterFactory.of(Validator.class, "Type");
      factory.registerSubType(StringNotEmptyValidator.class, "StringNotEmpty");
      factory.registerSubType(StringNullOrNotEmptyValidator.class, "StringNullOrNotEmpty");
      factory.registerSubType(StringsAtMostOneValidator.class, "StringsAtMostOne");
      factory.registerSubType(StringsOneSetValidator.class, "StringsOneSet");
      factory.registerSubType(StringsNotEmptyValidator.class, "NotAllStringsEmpty");
      factory.registerSubType(IntSingleValidator.class, "Int");
      factory.registerSubType(IntOrValidator.class, "IntOr");
      factory.registerSubType(IntRangeValidator.class, "IntRange");
      factory.registerSubType(DoubleSingleValidator.class, "Double");
      factory.registerSubType(DoubleOrValidator.class, "DoubleOr");
      factory.registerSubType(DoubleRangeValidator.class, "DoubleRange");
      factory.registerSubType(AttributeRelationValidator.class, "NumericRelation");
      factory.registerSubType(ArrayNotEmptyValidator.class, "ArrayNotEmpty");
      factory.registerSubType(AnyPresentValidator.class, "AnyPresent");
      factory.registerSubType(OnePresentValidator.class, "OnePresent");
      factory.registerSubType(OneOrNonePresentValidator.class, "OneOrNonePresent");
      factory.registerSubType(AnyBooleanValidator.class, "AnyTrue");
      factory.registerSubType(StringArrayNotEmptyValidator.class, "StringListNotEmpty");
      factory.registerSubType(StringArrayNoEmptyStringsValidator.class, "StringListNoEmptyStrings");
      factory.registerSubType(DoubleSequenceValidator.class, "DoubleSequenceValidator");
      factory.registerSubType(IntSequenceValidator.class, "IntSequenceValidator");
      factory.registerSubType(ExistsIfParameterSetValidator.class, "ExistsIfParameterSet");
      factory.registerSubType(TemporalSequenceValidator.class, "TemporalSequenceValidator");
      factory.registerSubType(RequiresFeatureIfValidator.class, "RequiresFeatureIf");
      factory.registerSubType(RequiresOneOfFeaturesValidator.class, "RequiresOneOfFeatures");
      factory.registerSubType(StateStringValidator.class, "StateString");
      factory.registerSubType(ValidateIfEnumIsValidator.class, "ValidateIfEnumIs");
      factory.registerSubType(ValidateAssetIfEnumIsValidator.class, "ValidateAssetIfEnumIs");
      factory.registerSubType(ComponentOnlyValidator.class, "ComponentOnly");
      factory.registerSubType(RequiresFeatureIfEnumValidator.class, "RequiresFeatureIfEnum");
      factory.registerSubType(EnumArrayNoDuplicatesValidator.class, "EnumArrayNoDuplicates");
      factory.registerSubType(ArraysOneSetValidator.class, "ArraysOneSet");
      factory.registerSubType(BooleanImplicationValidator.class, "BooleanImplication");
      factory.registerSubType(InstructionContextValidator.class, "InstructionContext");
      factory.registerSubType(AtMostOneBooleanValidator.class, "AtMostOneBoolean");
      gsonBuilder.registerTypeAdapterFactory(factory);
      return gsonBuilder;
   }
}
