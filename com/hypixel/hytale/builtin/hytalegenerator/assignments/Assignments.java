package com.hypixel.hytale.builtin.hytalegenerator.assignments;

import com.hypixel.hytale.builtin.hytalegenerator.props.EmptyProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.workerindexer.WorkerIndexer;
import com.hypixel.hytale.math.vector.Vector3d;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

public abstract class Assignments {
   public abstract Prop propAt(@Nonnull Vector3d var1, @Nonnull WorkerIndexer.Id var2, double var3);

   public abstract List<Prop> getAllPossibleProps();

   @Nonnull
   public static Assignments noPropDistribution() {
      return new Assignments() {
         @Nonnull
         @Override
         public Prop propAt(@Nonnull Vector3d position, @Nonnull WorkerIndexer.Id id, double distanceTOBiomeEdge) {
            return EmptyProp.INSTANCE;
         }

         @Nonnull
         @Override
         public List<Prop> getAllPossibleProps() {
            return Collections.singletonList(EmptyProp.INSTANCE);
         }
      };
   }
}
