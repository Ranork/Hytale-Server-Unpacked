package com.hypixel.hytale.builtin.hytalegenerator.propdistributions;

import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import com.hypixel.hytale.math.vector.Vector3d;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

public class ConstantAssignments extends Assignments {
   @Nonnull
   private final Prop prop;
   private final int runtime;

   public ConstantAssignments(@Nonnull Prop prop, int runtime) {
      this.prop = prop;
      this.runtime = runtime;
   }

   @Nonnull
   @Override
   public Prop propAt(@Nonnull Vector3d position, @Nonnull WorkerIndexer.Id id, double distanceTOBiomeEdge) {
      return this.prop;
   }

   @Override
   public int getRuntime() {
      return this.runtime;
   }

   @Nonnull
   @Override
   public List<Prop> getAllPossibleProps() {
      return Collections.singletonList(this.prop);
   }
}
