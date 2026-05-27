package com.hypixel.hytale.server.npc.asset.builder.validators;

import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RequiresPrecedingValidator extends Validator {
   private final String className;
   private final BuilderDescriptorState state;
   private final String shortDescription;
   @Nullable
   private final String longDescription;

   public RequiresPrecedingValidator(
      @Nonnull Class<?> clazz, @Nonnull BuilderDescriptorState state, @Nonnull String shortDescription, @Nullable String longDescription
   ) {
      this.className = clazz.getSimpleName();
      this.state = state;
      this.shortDescription = shortDescription;
      this.longDescription = longDescription;
   }

   public String getClassName() {
      return this.className;
   }

   public BuilderDescriptorState getState() {
      return this.state;
   }

   public String getShortDescription() {
      return this.shortDescription;
   }

   @Nullable
   public String getLongDescription() {
      return this.longDescription;
   }
}
