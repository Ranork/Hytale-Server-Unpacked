package com.hypixel.hytale.builtin.hytalegenerator.assignments;

import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.workerindexer.WorkerIndexer;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class ConstantAssignments extends Assignments {
   @Nonnull
   private final Prop prop;

   public ConstantAssignments(@Nonnull Prop prop) {
      this.prop = prop;
   }

   @Nonnull
   @Override
   public Prop propAt(@Nonnull Vector3d position, @Nonnull WorkerIndexer.Id id, double distanceTOBiomeEdge) {
      return this.prop;
   }

   @Nonnull
   @Override
   public List<Prop> getAllPossibleProps() {
      return Collections.singletonList(this.prop);
   }
}
