package com.hypixel.hytale.codec.validation;

import com.hypixel.hytale.codec.validation.validator.ArrayValidator;
import com.hypixel.hytale.codec.validation.validator.MapKeyValidator;
import com.hypixel.hytale.codec.validation.validator.MapValueValidator;
import javax.annotation.Nonnull;

public class ValidatorCache<T> {
   private final Validator<T> validator;
   private ArrayValidator<T> arrayValidator;
   private ArrayValidator<T[]> arrayofArrayValidator;
   private MapKeyValidator<T> mapKeyValidator;
   private MapKeyValidator<T[]> mapArrayKeyValidator;
   private MapValueValidator<T> mapValueValidator;
   private MapValueValidator<T[]> mapArrayValueValidator;

   public ValidatorCache(Validator<T> validator) {
      this.validator = validator;
   }

   public Validator<T> getValidator() {
      return this.validator;
   }

   @Nonnull
   public ArrayValidator<T> getArrayValidator() {
      if (this.arrayValidator == null) {
         this.arrayValidator = new ArrayValidator<>(this.getValidator());
      }

      return this.arrayValidator;
   }

   @Nonnull
   public ArrayValidator<T[]> getArrayOfArrayValidator() {
      if (this.arrayofArrayValidator == null) {
         this.arrayofArrayValidator = new ArrayValidator(this.getArrayValidator());
      }

      return this.arrayofArrayValidator;
   }

   @Nonnull
   public MapKeyValidator<T> getMapKeyValidator() {
      if (this.mapKeyValidator == null) {
         this.mapKeyValidator = new MapKeyValidator<>(this.getValidator());
      }

      return this.mapKeyValidator;
   }

   @Nonnull
   public MapKeyValidator<T[]> getMapArrayKeyValidator() {
      if (this.mapArrayKeyValidator == null) {
         this.mapArrayKeyValidator = new MapKeyValidator(this.getArrayValidator());
      }

      return this.mapArrayKeyValidator;
   }

   @Nonnull
   public MapValueValidator<T> getMapValueValidator() {
      if (this.mapValueValidator == null) {
         this.mapValueValidator = new MapValueValidator<>(this.getValidator());
      }

      return this.mapValueValidator;
   }

   @Nonnull
   public MapValueValidator<T[]> getMapArrayValueValidator() {
      if (this.mapArrayValueValidator == null) {
         this.mapArrayValueValidator = new MapValueValidator(this.getArrayValidator());
      }

      return this.mapArrayValueValidator;
   }
}
