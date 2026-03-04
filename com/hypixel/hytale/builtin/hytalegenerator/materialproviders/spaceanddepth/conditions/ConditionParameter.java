package com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.conditions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import javax.annotation.Nonnull;

public enum ConditionParameter {
   SPACE_ABOVE_FLOOR,
   SPACE_BELOW_CEILING;

   @Nonnull
   public static final Codec<ConditionParameter> CODEC = new EnumCodec<>(ConditionParameter.class, EnumCodec.EnumStyle.LEGACY);
}
