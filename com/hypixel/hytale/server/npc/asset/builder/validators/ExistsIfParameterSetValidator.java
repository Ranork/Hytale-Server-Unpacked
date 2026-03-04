package com.hypixel.hytale.server.npc.asset.builder.validators;

import javax.annotation.Nonnull;

public class ExistsIfParameterSetValidator extends Validator {
   private final String parameter;
   private final String attribute;

   private ExistsIfParameterSetValidator(String parameter, String attribute) {
      this.parameter = parameter;
      this.attribute = attribute;
   }

   @Nonnull
   public static String errorMessage(String parameter, String attribute) {
      return String.format("If %s is set, %s must be present", parameter, attribute);
   }

   @Nonnull
   public static ExistsIfParameterSetValidator withAttributes(String parameter, String attribute) {
      return new ExistsIfParameterSetValidator(parameter, attribute);
   }
}
