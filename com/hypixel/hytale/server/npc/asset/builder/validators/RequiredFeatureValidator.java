package com.hypixel.hytale.server.npc.asset.builder.validators;

import com.hypixel.hytale.server.npc.asset.builder.FeatureEvaluatorHelper;
import javax.annotation.Nullable;

public abstract class RequiredFeatureValidator extends Validator {
   public abstract boolean validate(FeatureEvaluatorHelper var1);

   @Nullable
   public abstract String getErrorMessage(String var1);
}
