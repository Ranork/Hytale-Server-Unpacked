package com.hypixel.hytale.server.npc.asset.builder;

import com.google.gson.JsonElement;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.validation.Validator;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.util.BsonUtil;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderCodecObjectHelper<T> {
   protected final Codec<T> codec;
   protected final Class<?> classType;
   protected final Validator<T> validator;
   @Nullable
   protected T value;

   public BuilderCodecObjectHelper(Class<?> classType, Codec<T> codec, Validator<T> validator) {
      this.classType = classType;
      this.codec = codec;
      this.validator = validator;
   }

   @Nullable
   public T build() {
      return this.value;
   }

   public void readConfig(@Nonnull JsonElement data, @Nonnull ExtraInfo extraInfo) {
      this.value = this.codec.decode(BsonUtil.translateJsonToBson(data), extraInfo);
      if (this.validator != null) {
         this.validator.accept(this.value, extraInfo.getValidationResults());
      }

      extraInfo.getValidationResults()._processValidationResults();
      extraInfo.getValidationResults().logOrThrowValidatorExceptions(HytaleLogger.getLogger());
   }

   public boolean hasValue() {
      return this.value != null;
   }

   public Class<?> getClassType() {
      return this.classType;
   }
}
