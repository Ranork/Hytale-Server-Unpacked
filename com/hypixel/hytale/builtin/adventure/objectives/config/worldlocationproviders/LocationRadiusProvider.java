package com.hypixel.hytale.builtin.adventure.objectives.config.worldlocationproviders;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.util.TrigMathUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LocationRadiusProvider extends WorldLocationProvider {
   @Nonnull
   public static final BuilderCodec<LocationRadiusProvider> CODEC = BuilderCodec.builder(LocationRadiusProvider.class, LocationRadiusProvider::new, BASE_CODEC)
      .append(
         new KeyedCodec<>("MinRadius", Codec.INTEGER),
         (locationRadiusCondition, integer) -> locationRadiusCondition.minRadius = integer,
         locationRadiusCondition -> locationRadiusCondition.minRadius
      )
      .addValidator(Validators.greaterThan(0))
      .add()
      .<Integer>append(
         new KeyedCodec<>("MaxRadius", Codec.INTEGER),
         (locationRadiusCondition, integer) -> locationRadiusCondition.maxRadius = integer,
         locationRadiusCondition -> locationRadiusCondition.maxRadius
      )
      .addValidator(Validators.greaterThanOrEqual(1))
      .add()
      .afterDecode(
         locationRadiusCondition -> {
            if (locationRadiusCondition.minRadius > locationRadiusCondition.maxRadius) {
               throw new IllegalArgumentException(
                  "LocationRadiusCondition.MinRadius ("
                     + locationRadiusCondition.minRadius
                     + ") needs to be greater than LocationRadiusCondition.MaxRadius ("
                     + locationRadiusCondition.maxRadius
                     + ")"
               );
            }
         }
      )
      .build();
   protected int minRadius = 10;
   protected int maxRadius = 50;

   @Nullable
   @Override
   public Vector3i runCondition(@Nonnull World world, @Nonnull Vector3i position) {
      double angle = Math.random() * (float) (Math.PI * 2);
      int radius = MathUtil.randomInt(this.minRadius, this.maxRadius);
      Vector3i newPosition = position.clone();
      newPosition.add((int)(radius * TrigMathUtil.cos(angle)), 0, (int)(radius * TrigMathUtil.sin(angle)));
      long chunkIndex = ChunkUtil.indexChunkFromBlock(newPosition.x, newPosition.z);
      WorldChunk worldChunkComponent = world.getChunk(chunkIndex);
      if (worldChunkComponent == null) {
         return null;
      } else {
         newPosition.y = worldChunkComponent.getHeight(newPosition.x, newPosition.z);
         return newPosition;
      }
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         LocationRadiusProvider that = (LocationRadiusProvider)o;
         return this.minRadius != that.minRadius ? false : this.maxRadius == that.maxRadius;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.minRadius;
      return 31 * result + this.maxRadius;
   }

   @Nonnull
   @Override
   public String toString() {
      return "LocationRadiusProvider{minRadius=" + this.minRadius + ", maxRadius=" + this.maxRadius + "} " + super.toString();
   }
}
