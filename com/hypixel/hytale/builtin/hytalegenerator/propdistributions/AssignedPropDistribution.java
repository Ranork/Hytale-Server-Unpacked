package com.hypixel.hytale.builtin.hytalegenerator.propdistributions;

import com.hypixel.hytale.builtin.hytalegenerator.assignments.Assignments;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.builtin.hytalegenerator.props.EmptyProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.workerindexer.WorkerIndexer;
import com.hypixel.hytale.math.vector.Vector3d;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class AssignedPropDistribution extends PropDistribution {
   @Nonnull
   private final PropDistribution propDistribution;
   @Nonnull
   private final Assignments assignments;
   private final boolean isOverrideAllProps;
   @Nonnull
   private final PropDistribution.Context rPropDistributionContext;
   @Nonnull
   private PropDistribution.Context rContext;
   @Nonnull
   private final Pipe.Two<Vector3d, Prop> rChildPipe = new Pipe.Two<Vector3d, Prop>() {
      public void accept(@NonNullDecl Vector3d position, @NonNullDecl Prop existingProp, @NonNullDecl Control control) {
         if (AssignedPropDistribution.this.isOverrideAllProps || existingProp == EmptyProp.INSTANCE) {
            Prop newProp = AssignedPropDistribution.this.assignments
               .propAt(position, WorkerIndexer.Id.MAIN, AssignedPropDistribution.this.rContext.distanceFromBiomeEdge);
            AssignedPropDistribution.this.rContext.pipe.accept(position, newProp, control);
         }
      }
   };

   public AssignedPropDistribution(@Nonnull PropDistribution propDistribution, @Nonnull Assignments assignments, boolean isOverrideAllProps) {
      this.propDistribution = propDistribution;
      this.assignments = assignments;
      this.isOverrideAllProps = isOverrideAllProps;
      this.rPropDistributionContext = new PropDistribution.Context();
      this.rContext = new PropDistribution.Context();
   }

   @Override
   public void distribute(@NonNullDecl PropDistribution.Context context) {
      this.rContext = context;
      this.rPropDistributionContext.assign(context);
      this.rPropDistributionContext.pipe = this.rChildPipe;
      this.propDistribution.distribute(this.rPropDistributionContext);
   }

   @Override
   public void forEachPossibleProp(@NonNullDecl Consumer<Prop> consumer) {
      this.assignments.getAllPossibleProps().forEach(consumer);
      this.propDistribution.forEachPossibleProp(consumer);
   }
}
