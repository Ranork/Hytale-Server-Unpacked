package com.hypixel.hytale.server.flock.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.codec.validation.validator.IntArrayValidator;
import com.hypixel.hytale.math.random.RandomExtra;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class RangeSizeFlockAsset extends FlockAsset {
   public static final BuilderCodec<RangeSizeFlockAsset> CODEC = BuilderCodec.builder(RangeSizeFlockAsset.class, RangeSizeFlockAsset::new, ABSTRACT_CODEC)
      .documentation("A flock definition in which the initial random size is picked from a range.")
      .<int[]>appendInherited(
         new KeyedCodec<>("Size", Codec.INT_ARRAY), (flock, o) -> flock.size = o, flock -> flock.size, (flock, parent) -> flock.size = parent.size
      )
      .documentation(
         "An array with two values specifying the random range from which to pick the size of the flock when it spawns. e.g. [ 2, 4 ] will randomly pick a size between two and four (inclusive)."
      )
      .addValidator(Validators.nonNull())
      .addValidator(Validators.intArraySize(2))
      .addValidator(new IntArrayValidator(Validators.greaterThan(0)))
      .add()
      .build();
   private static final int[] DEFAULT_SIZE = new int[]{1, 1};
   protected int[] size = DEFAULT_SIZE;

   protected RangeSizeFlockAsset(String id) {
      super(id);
   }

   protected RangeSizeFlockAsset() {
   }

   public int[] getSize() {
      return this.size;
   }

   @Override
   public int getMinFlockSize() {
      return this.size[0];
   }

   @Override
   public int pickFlockSize() {
      return RandomExtra.randomRange(Math.max(1, this.size[0]), this.size[1]);
   }

   @Nonnull
   public static RangeSizeFlockAsset getUnknownFor(String id) {
      return new RangeSizeFlockAsset(id);
   }

   @Nonnull
   @Override
   public String toString() {
      return "RangeSizeFlockAsset{size=" + Arrays.toString(this.size) + "} " + super.toString();
   }
}
