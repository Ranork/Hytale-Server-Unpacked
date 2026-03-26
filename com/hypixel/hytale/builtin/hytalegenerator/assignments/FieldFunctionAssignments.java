package com.hypixel.hytale.builtin.hytalegenerator.assignments;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.props.EmptyProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.workerindexer.WorkerIndexer;
import com.hypixel.hytale.math.vector.Vector3d;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class FieldFunctionAssignments extends Assignments {
   @Nonnull
   private final Density density;
   @Nonnull
   private final List<FieldFunctionAssignments.FieldDelimiter> delimiters;
   @Nonnull
   private final Density.Context rDensityContext;

   public FieldFunctionAssignments(@Nonnull Density functionTree, @Nonnull List<FieldFunctionAssignments.FieldDelimiter> delimiters) {
      this.density = functionTree;
      this.delimiters = new ArrayList<>(delimiters);
      this.rDensityContext = new Density.Context();
   }

   @Override
   public Prop propAt(@Nonnull Vector3d position, @Nonnull WorkerIndexer.Id id, double distanceFromBiomeEdge) {
      if (this.delimiters.isEmpty()) {
         return EmptyProp.INSTANCE;
      } else {
         this.rDensityContext.position.assign(position);
         this.rDensityContext.distanceToBiomeEdge = distanceFromBiomeEdge;
         double fieldValue = this.density.process(this.rDensityContext);

         for (FieldFunctionAssignments.FieldDelimiter delimiter : this.delimiters) {
            if (delimiter.isInside(fieldValue)) {
               return delimiter.assignments.propAt(position, id, distanceFromBiomeEdge);
            }
         }

         return EmptyProp.INSTANCE;
      }
   }

   @Nonnull
   @Override
   public List<Prop> getAllPossibleProps() {
      ArrayList<Prop> list = new ArrayList<>();

      for (FieldFunctionAssignments.FieldDelimiter f : this.delimiters) {
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
