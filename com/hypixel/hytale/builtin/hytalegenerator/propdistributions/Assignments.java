package com.hypixel.hytale.builtin.hytalegenerator.propdistributions;

import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import com.hypixel.hytale.math.vector.Vector3d;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

public abstract class Assignments {
   public abstract Prop propAt(@Nonnull Vector3d var1, @Nonnull WorkerIndexer.Id var2, double var3);

   public abstract int getRuntime();

   public abstract List<Prop> getAllPossibleProps();

   @Nonnull
   public static Assignments noPropDistribution(final int runtime) {
      return new Assignments() {
         @Nonnull
         @Override
         public Prop propAt(@Nonnull Vector3d position, @Nonnull WorkerIndexer.Id id, double distanceTOBiomeEdge) {
            return Prop.noProp();
         }

         @Override
         public int getRuntime() {
            return runtime;
         }

         @Nonnull
         @Override
         public List<Prop> getAllPossibleProps() {
            return Collections.singletonList(Prop.noProp());
         }
      };
   }
}
