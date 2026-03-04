package com.hypixel.hytale.builtin.hytalegenerator.propdistributions;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import com.hypixel.hytale.math.vector.Vector3d;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class FieldFunctionAssignments extends Assignments {
   @Nonnull
   private final Density density;
   @Nonnull
   private final List<FieldFunctionAssignments.FieldDelimiter> fieldDelimiters;
   private final int runtime;

   public FieldFunctionAssignments(@Nonnull Density functionTree, @Nonnull List<FieldFunctionAssignments.FieldDelimiter> fieldDelimiters, int runtime) {
      this.runtime = runtime;
      this.density = functionTree;
      this.fieldDelimiters = new ArrayList<>(fieldDelimiters);
   }

   @Override
   public Prop propAt(@Nonnull Vector3d position, @Nonnull WorkerIndexer.Id id, double distanceTOBiomeEdge) {
      if (this.fieldDelimiters.isEmpty()) {
         return Prop.noProp();
      } else {
         Density.Context context = new Density.Context();
         context.position = position;
         context.distanceToBiomeEdge = distanceTOBiomeEdge;
         double fieldValue = this.density.process(context);

         for (FieldFunctionAssignments.FieldDelimiter fd : this.fieldDelimiters) {
            if (fd.isInside(fieldValue)) {
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

      for (FieldFunctionAssignments.FieldDelimiter f : this.fieldDelimiters) {
         list.addAll(f.assignments.getAllPossibleProps());
      }

      return list;
   }

   public static class FieldDelimiter {
      double top;
      double bottom;
      Assignments assignments;

      public FieldDelimiter(@Nonnull Assignments propDistributions, double bottom, double top) {
         this.bottom = bottom;
         this.top = top;
         this.assignments = propDistributions;
      }

      boolean isInside(double fieldValue) {
         return fieldValue < this.top && fieldValue >= this.bottom;
      }
   }
}
