package com.hypixel.hytale.builtin.hytalegenerator.propdistributions;

import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class NoPropDistribution extends PropDistribution {
   public static final PropDistribution INSTANCE = new NoPropDistribution();

   private NoPropDistribution() {
   }

   @Override
   public void distribute(@NonNullDecl PropDistribution.Context context) {
   }

   @Override
   public void forEachPossibleProp(@NonNullDecl Consumer<Prop> consumer) {
   }
}
