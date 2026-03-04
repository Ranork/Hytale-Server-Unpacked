package com.hypixel.hytale.server.flock.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.codec.validation.validator.DoubleArrayValidator;
import com.hypixel.hytale.math.random.RandomExtra;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class WeightedSizeFlockAsset extends FlockAsset {
   public static final BuilderCodec<WeightedSizeFlockAsset> CODEC = BuilderCodec.builder(
         WeightedSizeFlockAsset.class, WeightedSizeFlockAsset::new, FlockAsset.ABSTRACT_CODEC
      )
      .documentation("A flock definition where the initial random size is picked from a weighted map of sizes.")
      .<Integer>appendInherited(
         new KeyedCodec<>("MinSize", Codec.INTEGER), (flock, i) -> flock.minSize = i, flock -> flock.minSize, (flock, parent) -> flock.minSize = parent.minSize
      )
      .documentation("The absolute minimum size to spawn the flock with.")
      .addValidator(Validators.nonNull())
      .addValidator(Validators.greaterThanOrEqual(0))
      .add()
      .<double[]>appendInherited(
         new KeyedCodec<>("SizeWeights", Codec.DOUBLE_ARRAY),
         (spawn, o) -> spawn.sizeWeights = o,
         spawn -> spawn.sizeWeights,
         (spawn, parent) -> spawn.sizeWeights = parent.sizeWeights
      )
      .documentation(
         "An array of weights which is used in conjunction with the **MinSize** to determine the weighted size of the flock. The first value in the array corresponds to the weight of the minimum size and each successive value to a flock larger by one. e.g. If **MinSize** is 2 and **SizeWeights** is [ 25, 75 ], there will be a 25% chance that the flock will spawn with a size of 2 and a 75% chance that the flock will spawn with a size of 3. As these are weights, they do not need to add up to 100 and their percentage is relative to the total sum."
      )
      .addValidator(Validators.nonNull())
      .addValidator(new DoubleArrayValidator(Validators.greaterThan(0.0)))
      .add()
      .build();
   protected int minSize;
   protected double[] sizeWeights;

   protected WeightedSizeFlockAsset() {
   }

   public int getMinSize() {
      return this.minSize;
   }

   public double[] getSizeWeights() {
      return this.sizeWeights;
   }

   @Override
   public int getMinFlockSize() {
      return this.minSize;
   }

   @Override
   public int pickFlockSize() {
      int index = RandomExtra.pickWeightedIndex(this.sizeWeights);
      return Math.max(this.minSize, 1) + index;
   }

   @Nonnull
   @Override
   public String toString() {
      return "WeightedSizeFlockAsset{minSize=" + this.minSize + ", sizeWeights=" + Arrays.toString(this.sizeWeights) + "} " + super.toString();
   }
}
