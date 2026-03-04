package com.hypixel.hytale.codec.validation;

import com.hypixel.hytale.codec.validation.validator.ArraySizeRangeValidator;
import com.hypixel.hytale.codec.validation.validator.ArraySizeValidator;
import com.hypixel.hytale.codec.validation.validator.ArrayValidator;
import com.hypixel.hytale.codec.validation.validator.DeprecatedValidator;
import com.hypixel.hytale.codec.validation.validator.DoubleArraySizeValidator;
import com.hypixel.hytale.codec.validation.validator.EqualValidator;
import com.hypixel.hytale.codec.validation.validator.IntArraySizeValidator;
import com.hypixel.hytale.codec.validation.validator.ListValidator;
import com.hypixel.hytale.codec.validation.validator.NonEmptyArrayValidator;
import com.hypixel.hytale.codec.validation.validator.NonEmptyDoubleArrayValidator;
import com.hypixel.hytale.codec.validation.validator.NonEmptyFloatArrayValidator;
import com.hypixel.hytale.codec.validation.validator.NonEmptyMapValidator;
import com.hypixel.hytale.codec.validation.validator.NonEmptyStringValidator;
import com.hypixel.hytale.codec.validation.validator.NonNullValidator;
import com.hypixel.hytale.codec.validation.validator.NotEqualValidator;
import com.hypixel.hytale.codec.validation.validator.OrValidator;
import com.hypixel.hytale.codec.validation.validator.RangeValidator;
import com.hypixel.hytale.codec.validation.validator.RequiredMapKeysValidator;
import com.hypixel.hytale.codec.validation.validator.SequentialDoubleArrayValidator;
import com.hypixel.hytale.codec.validation.validator.UniqueInArrayValidator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

public class Validators {
   @Nonnull
   public static <T> DeprecatedValidator<T> deprecated() {
      return (DeprecatedValidator<T>)DeprecatedValidator.INSTANCE;
   }

   @Nonnull
   public static <T> Validator<T> nonNull() {
      return (Validator<T>)NonNullValidator.INSTANCE;
   }

   @Nonnull
   public static <T> ArrayValidator<T> nonNullArrayElements() {
      return new ArrayValidator<>(nonNull());
   }

   @Nonnull
   public static Validator<String> nonEmptyString() {
      return NonEmptyStringValidator.INSTANCE;
   }

   @Nonnull
   public static <T> Validator<T[]> nonEmptyArray() {
      return (Validator<T[]>)NonEmptyArrayValidator.INSTANCE;
   }

   @Nonnull
   public static <K, V> Validator<Map<K, V>> nonEmptyMap() {
      return NonEmptyMapValidator.INSTANCE;
   }

   @Nonnull
   public static <T> Validator<T[]> uniqueInArray() {
      return (Validator<T[]>)UniqueInArrayValidator.INSTANCE;
   }

   @Nonnull
   public static <T> Validator<Map<T, ?>> requiredMapKeysValidator(T[] array) {
      return new RequiredMapKeysValidator<>(array);
   }

   @Nonnull
   public static <T extends Comparable<T>> Validator<T> greaterThan(T greaterThan) {
      return new RangeValidator<>(greaterThan, null, false);
   }

   @Nonnull
   public static <T extends Comparable<T>> Validator<T> greaterThanOrEqual(T greaterThan) {
      return new RangeValidator<>(greaterThan, null, true);
   }

   @Nonnull
   public static <T extends Comparable<T>> Validator<T> lessThan(T lessThan) {
      return new RangeValidator<>(null, lessThan, false);
   }

   @Nonnull
   public static <T extends Comparable<T>> Validator<T> insideRange(T greaterthan, T lessThan) {
      return new RangeValidator<>(greaterthan, lessThan, false);
   }

   @Nonnull
   public static <T extends Comparable<T>> Validator<T> min(T min) {
      return new RangeValidator<>(min, null, true);
   }

   @Nonnull
   public static <T extends Comparable<T>> Validator<T> max(T max) {
      return new RangeValidator<>(null, max, true);
   }

   @Nonnull
   public static <T extends Comparable<T>> Validator<T> range(T min, T max) {
      return new RangeValidator<>(min, max, true);
   }

   @Nonnull
   public static <T> Validator<T[]> arraySizeRange(int min, int max) {
      return new ArraySizeRangeValidator<>(min, max);
   }

   @Nonnull
   public static <T> Validator<T[]> arraySize(int size) {
      return new ArraySizeValidator<>(size);
   }

   @Nonnull
   public static Validator<int[]> intArraySize(int size) {
      return new IntArraySizeValidator(size);
   }

   @Nonnull
   public static Validator<double[]> doubleArraySize(int size) {
      return new DoubleArraySizeValidator(size);
   }

   @Nonnull
   public static <T extends Comparable<T>> Validator<T> equal(@Nonnull T value) {
      return new EqualValidator<>(value);
   }

   @Nonnull
   public static <T extends Comparable<T>> Validator<T> notEqual(@Nonnull T value) {
      return new NotEqualValidator<>(value);
   }

   @Nonnull
   public static Validator<double[]> nonEmptyDoubleArray() {
      return NonEmptyDoubleArrayValidator.INSTANCE;
   }

   @Nonnull
   public static Validator<float[]> nonEmptyFloatArray() {
      return NonEmptyFloatArrayValidator.INSTANCE;
   }

   @Nonnull
   public static Validator<double[]> monotonicSequentialDoubleArrayValidator() {
      return SequentialDoubleArrayValidator.NEQ_INSTANCE;
   }

   @Nonnull
   public static Validator<double[]> weaklyMonotonicSequentialDoubleArrayValidator() {
      return SequentialDoubleArrayValidator.ALLOW_EQ_INSTANCE;
   }

   @Nonnull
   public static <T> Validator<T> or(Validator<T>... validators) {
      return new OrValidator<>(validators);
   }

   @Nonnull
   public static <T> Validator<List<T>> listItem(Validator<T> validator) {
      return new ListValidator<>(validator);
   }
}
