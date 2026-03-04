package com.hypixel.hytale.server.npc.asset.builder.validators;

public abstract class EnumArrayValidator extends Validator {
   public abstract <T extends Enum<T>> boolean test(T[] var1, Class<T> var2);

   public abstract <T extends Enum<T>> String errorMessage(String var1, T[] var2);
}
