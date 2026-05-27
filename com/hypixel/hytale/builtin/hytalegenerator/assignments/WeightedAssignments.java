package com.hypixel.hytale.builtin.hytalegenerator.assignments;

import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.builtin.hytalegenerator.props.EmptyProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.rng.RngField;
import com.hypixel.hytale.builtin.hytalegenerator.workerindexer.WorkerIndexer;
import com.hypixel.hytale.math.util.FastRandom;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class WeightedAssignments extends Assignments {
   @Nonnull
   private final WeightedMap<Assignments> weightedDistributions;
   @Nonnull
   private final RngField rngField;
   private final double skipCHance;
   @Nonnull
   private final FastRandom rRandom;

   public WeightedAssignments(@Nonnull WeightedMap<Assignments> props, int seed, double skipCHance) {
      this.weightedDistributions = new WeightedMap<>(props);
      this.rngField = new RngField(seed);
      this.skipCHance = skipCHance;
      this.rRandom = new FastRandom();
   }

   @Override
   public Prop propAt(@Nonnull Vector3d position, @Nonnull WorkerIndexer.Id id, double distanceTOBiomeEdge) {
      if (this.weightedDistributions.size() == 0) {
         return EmptyProp.INSTANCE;
      } else {
         this.rRandom.setSeed(this.rngField.get(position.x, position.y, position.z));
         return (Prop)(this.rRandom.nextDouble() < this.skipCHance
            ? EmptyProp.INSTANCE
            : this.weightedDistributions.pick(this.rRandom).propAt(position, id, distanceTOBiomeEdge));
      }
   }

   @Nonnull
   @Override
   public List<Prop> getAllPossibleProps() {
      ArrayList<Prop> list = new ArrayList<>();

      for (Assignments d : this.weightedDistributions.allElements()) {
         list.addAll(d.getAllPossibleProps());
      }

      return list;
   }
}
