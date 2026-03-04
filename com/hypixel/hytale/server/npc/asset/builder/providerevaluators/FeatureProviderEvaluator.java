package com.hypixel.hytale.server.npc.asset.builder.providerevaluators;

import com.hypixel.hytale.server.npc.asset.builder.Feature;
import java.util.EnumSet;

public interface FeatureProviderEvaluator extends ProviderEvaluator {
   boolean provides(EnumSet<Feature> var1);
}
