package com.hypixel.hytale.builtin.hytalegenerator.propdistributions;

import com.hypixel.hytale.builtin.hytalegenerator.datastructures.WeightedMap;
import com.hypixel.hytale.builtin.hytalegenerator.framework.math.SeedGenerator;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.math.vector.Vector3d;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class WeightedAssignments extends Assignments {
   @Nonnull
   private final WeightedMap<Assignments> weightedDistributions;
   @Nonnull
   private final SeedGenerator seedGenerator;
   private final int runtime;
   private final double noneProbability;

   public WeightedAssignments(@Nonnull WeightedMap<Assignments> props, int seed, double noneProbability, int runtime) {
      this.weightedDistributions = new WeightedMap<>(props);
      this.runtime = runtime;
      this.seedGenerator = new SeedGenerator(seed);
      this.noneProbability = noneProbability;
   }

   @Override
   public Prop propAt(@Nonnull Vector3d position, @Nonnull WorkerIndexer.Id id, double distanceTOBiomeEdge) {
      if (this.weightedDistributions.size() == 0) {
         return Prop.noProp();
      } else {
         long x = (long)(position.x * 10000.0);
         long y = (long)(position.y * 10000.0);
         long z = (long)(position.z * 10000.0);
         FastRandom rand = new FastRandom(this.seedGenerator.seedAt(x, y, z));
         return rand.nextDouble() < this.noneProbability ? Prop.noProp() : this.weightedDistributions.pick(rand).propAt(position, id, distanceTOBiomeEdge);
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

      for (Assignments d : this.weightedDistributions.allElements()) {
         list.addAll(d.getAllPossibleProps());
      }

      return list;
   }
}
