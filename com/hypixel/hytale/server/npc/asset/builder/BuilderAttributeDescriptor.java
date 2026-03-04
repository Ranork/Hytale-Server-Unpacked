package com.hypixel.hytale.server.npc.asset.builder;

import com.hypixel.hytale.server.npc.asset.builder.validators.Validator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderAttributeDescriptor {
   private final String name;
   private final String type;
   private BuilderAttributeDescriptor.RequirementType required;
   private boolean computable;
   private final BuilderDescriptorState state;
   private final String shortDescription;
   private final String longDescription;
   @Nullable
   private String defaultValue;
   @Nullable
   private String domain;
   private int minSize;
   private int maxSize;
   @Nullable
   private Validator validator;
   @Nullable
   private Map<String, String> flagDescriptions;

   public BuilderAttributeDescriptor(String name, String type, BuilderDescriptorState state, String shortDescription, String longDescription) {
      this.name = name;
      this.type = type;
      this.state = state;
      this.shortDescription = shortDescription;
      this.longDescription = longDescription;
      this.required = BuilderAttributeDescriptor.RequirementType.OPTIONAL;
      this.computable = false;
      this.defaultValue = null;
      this.domain = null;
      this.validator = null;
      this.flagDescriptions = null;
      this.minSize = -1;
      this.maxSize = -1;
   }

   @Nonnull
   public BuilderAttributeDescriptor required() {
      this.required = BuilderAttributeDescriptor.RequirementType.REQUIRED;
      this.defaultValue = null;
      return this;
   }

   @Nonnull
   public BuilderAttributeDescriptor requiredIfNotOverridden() {
      this.required = BuilderAttributeDescriptor.RequirementType.REQUIRED_IF_NOT_OVERRIDDEN;
      this.defaultValue = null;
      return this;
   }

   @Nonnull
   public BuilderAttributeDescriptor optional(String defaultValue) {
      this.required = BuilderAttributeDescriptor.RequirementType.OPTIONAL;
      this.defaultValue = defaultValue;
      return this;
   }

   @Nonnull
   public BuilderAttributeDescriptor optional(double[] defaultValue) {
      this.required = BuilderAttributeDescriptor.RequirementType.OPTIONAL;
      this.defaultValue = Arrays.toString(defaultValue);
      return this;
   }

   @Nonnull
   public BuilderAttributeDescriptor optional(int[] defaultValue) {
      this.required = BuilderAttributeDescriptor.RequirementType.OPTIONAL;
      this.defaultValue = Arrays.toString(defaultValue);
      return this;
   }

   @Nonnull
   public BuilderAttributeDescriptor optional(String[] defaultValue) {
      this.required = BuilderAttributeDescriptor.RequirementType.OPTIONAL;
      this.defaultValue = Arrays.toString((Object[])defaultValue);
      return this;
   }

   @Nonnull
   public BuilderAttributeDescriptor optional(boolean[] defaultValue) {
      this.required = BuilderAttributeDescriptor.RequirementType.OPTIONAL;
      this.defaultValue = Arrays.toString(defaultValue);
      return this;
   }

   @Nonnull
   public BuilderAttributeDescriptor computable() {
      this.computable = true;
      return this;
   }

   @Nonnull
   public <E extends Enum<E>> BuilderAttributeDescriptor setBasicEnum(@Nonnull Class<E> clazz) {
      E[] enumConstants = (E[])clazz.getEnumConstants();
      this.domain = BuilderBase.getDomain(enumConstants);
      HashMap<String, String> result = new HashMap<>();

      for (E E : enumConstants) {
         result.put(E.toString(), E.toString());
      }

      this.flagDescriptions = result;
      return this;
   }

   @Nonnull
   public <E extends Enum<E> & Supplier<String>> BuilderAttributeDescriptor setEnum(@Nonnull Class<E> clazz) {
      E[] enumConstants = (E[])clazz.getEnumConstants();
      this.domain = BuilderBase.getDomain(enumConstants);
      HashMap<String, String> result = new HashMap<>();

      for (E E : enumConstants) {
         result.put(E.toString(), (String)E.get());
      }

      this.flagDescriptions = result;
      return this;
   }

   @Nonnull
   public BuilderAttributeDescriptor domain(String domain) {
      this.domain = domain;
      return this;
   }

   @Nonnull
   public BuilderAttributeDescriptor validator(Validator validator) {
      this.validator = validator;
      return this;
   }

   @Nonnull
   public BuilderAttributeDescriptor length(int size) {
      return this.length(size, size);
   }

   @Nonnull
   public BuilderAttributeDescriptor length(int minSize, int maxSize) {
      this.minSize = minSize;
      this.maxSize = maxSize;
      return this;
   }

   @Nonnull
   @Override
   public String toString() {
      return "BuilderAttributeDescriptor{name='"
         + this.name
         + "', type='"
         + this.type
         + "', required="
         + this.required
         + ", computable="
         + this.computable
         + ", state="
         + this.state
         + ", shortDescription='"
         + this.shortDescription
         + "', longDescription='"
         + this.longDescription
         + "', defaultValue='"
         + this.defaultValue
         + "', domain='"
         + this.domain
         + "', minSize="
         + this.minSize
         + ", maxSize="
         + this.maxSize
         + ", validator="
         + this.validator
         + ", flagDescriptions="
         + this.flagDescriptions
         + "}";
   }

   private static enum RequirementType {
      REQUIRED,
      OPTIONAL,
      REQUIRED_IF_NOT_OVERRIDDEN;
   }
}
