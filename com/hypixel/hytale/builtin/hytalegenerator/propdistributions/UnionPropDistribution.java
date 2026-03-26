package com.hypixel.hytale.builtin.hytalegenerator.propdistributions;

import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class UnionPropDistribution extends PropDistribution {
   @Nonnull
   private final List<PropDistribution> propDistributions;

   public UnionPropDistribution(@Nonnull List<PropDistribution> propDistributions) {
      this.propDistributions = new ArrayList<>(propDistributions);
   }

   @Override
   public void distribute(@NonNullDecl PropDistribution.Context context) {
      for (PropDistribution propDistribution : this.propDistributions) {
         propDistribution.distribute(context);
      }
   }

   @Override
   public void forEachPossibleProp(@NonNullDecl Consumer<Prop> consumer) {
      for (PropDistribution propDistribution : this.propDistributions) {
         propDistribution.forEachPossibleProp(consumer);
      }
   }
}
