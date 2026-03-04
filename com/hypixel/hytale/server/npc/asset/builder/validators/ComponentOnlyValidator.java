package com.hypixel.hytale.server.npc.asset.builder.validators;

public class ComponentOnlyValidator extends Validator {
   public static final ComponentOnlyValidator INSTANCE = new ComponentOnlyValidator();

   private ComponentOnlyValidator() {
   }

   public static ComponentOnlyValidator get() {
      return INSTANCE;
   }
}
