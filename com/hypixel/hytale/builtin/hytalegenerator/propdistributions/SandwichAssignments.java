package com.hypixel.hytale.builtin.hytalegenerator.propdistributions;

import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import com.hypixel.hytale.math.vector.Vector3d;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class SandwichAssignments extends Assignments {
   @Nonnull
   private final List<SandwichAssignments.VerticalDelimiter> verticalDelimiters;
   private final int runtime;

   public SandwichAssignments(@Nonnull List<SandwichAssignments.VerticalDelimiter> verticalDelimiters, int runtime) {
      this.runtime = runtime;
      this.verticalDelimiters = new ArrayList<>(verticalDelimiters);
   }

   @Override
   public Prop propAt(@Nonnull Vector3d position, @Nonnull WorkerIndexer.Id id, double distanceTOBiomeEdge) {
      if (this.verticalDelimiters.isEmpty()) {
         return Prop.noProp();
      } else {
         for (SandwichAssignments.VerticalDelimiter fd : this.verticalDelimiters) {
            if (fd.isInside(position.y)) {
               return fd.assignments.propAt(position, id, distanceTOBiomeEdge);
            }
         }

         return Prop.noProp();
      }
   }

   @Override
   public int getRuntime() {
      return this.runtime;
   }

   @Nonnull
   @Override
   public List<Prop> getAllPossibleProps() {
      ArrayList<Prop> list = new ArrayList<>();

      for (SandwichAssignments.VerticalDelimiter f : this.verticalDelimiters) {
         list.addAll(f.assignments.getAllPossibleProps());
      }

      return list;
   }

   public static class VerticalDelimiter {
      double maxY;
      double minY;
      Assignments assignments;

      public VerticalDelimiter(@Nonnull Assignments propDistributions, double minY, double maxY) {
         this.minY = minY;
         this.maxY = maxY;
         this.assignments = propDistributions;
      }

      boolean isInside(double fieldValue) {
         return fieldValue < this.maxY && fieldValue >= this.minY;
      }
   }
}
