package com.hypixel.hytale.server.npc.asset.builder.validators;

import java.time.temporal.TemporalAmount;

public abstract class TemporalArrayValidator extends Validator {
   public abstract boolean test(TemporalAmount[] var1);

   public abstract String errorMessage(String var1, TemporalAmount[] var2);
}
