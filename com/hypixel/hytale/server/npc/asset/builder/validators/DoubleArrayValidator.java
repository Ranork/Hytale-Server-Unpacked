package com.hypixel.hytale.server.npc.asset.builder.validators;

public abstract class DoubleArrayValidator extends Validator {
   public abstract boolean test(double[] var1);

   public abstract String errorMessage(double[] var1, String var2);

   public abstract String errorMessage(double[] var1);
}
