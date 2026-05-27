package com.hypixel.hytale.builtin.worldgen.modifier.content.common;

import com.hypixel.hytale.builtin.worldgen.modifier.content.Codecs;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.range.FloatRange;
import com.hypixel.hytale.procedurallib.condition.DoubleThreshold;
import com.hypixel.hytale.procedurallib.condition.DoubleThresholdCondition;
import com.hypixel.hytale.procedurallib.json.SeedResourcePointGenerator;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.procedurallib.logic.cell.DistanceCalculationMode;
import com.hypixel.hytale.procedurallib.logic.cell.GridCellDistanceFunction;
import com.hypixel.hytale.procedurallib.logic.cell.evaluator.PointEvaluator;
import com.hypixel.hytale.procedurallib.logic.cell.jitter.ConstantCellJitter;
import com.hypixel.hytale.procedurallib.logic.point.IPointGenerator;
import com.hypixel.hytale.procedurallib.logic.point.ScaledPointGenerator;
import com.hypixel.hytale.procedurallib.supplier.DoubleRange;
import com.hypixel.hytale.server.worldgen.SeedStringResource;

public class PointGrid {
   public static final BuilderCodec<PointGrid> CODEC = BuilderCodec.builder(PointGrid.class, PointGrid::new)
      .documentation(
         "Defines a point grid that distributes placement positions across the world using a seeded cell-based layout with configurable scale, jitter, and density"
      )
      .<String>append(new KeyedCodec<>("Seed", Codec.STRING), (t, v) -> t.seed = v, t -> t.seed)
      .documentation("Seed for the point grid")
      .add()
      .<Double>append(new KeyedCodec<>("Scale", Codec.DOUBLE), (t, v) -> t.scale = v, t -> t.scale)
      .documentation("Scale of the point grid")
      .add()
      .<Double>append(new KeyedCodec<>("Jitter", Codec.DOUBLE), (t, v) -> t.jitter = v, t -> t.jitter)
      .documentation("Jitter of the points in the grid")
      .add()
      .<FloatRange>append(new KeyedCodec<>("Density", Codecs.DENSITY_RANGE), (t, v) -> t.density = v, t -> t.density)
      .documentation("Density threshold for the point grid")
      .add()
      .build();
   public static final PointGrid DEFAULT = new PointGrid();
   protected String seed = "Default_Grid";
   protected double scale = 16.0;
   protected double jitter = 0.75;
   protected FloatRange density = new FloatRange(0.0F, 1.0F);

   public double scale() {
      return this.scale;
   }

   public double jitter() {
      return this.jitter;
   }

   public FloatRange density() {
      return this.density;
   }

   public IPointGenerator build(SeedString<SeedStringResource> seed) {
      return new ScaledPointGenerator(
         new SeedResourcePointGenerator(
            seed.appendToOriginal(this.seed).hashCode(),
            GridCellDistanceFunction.DISTANCE_FUNCTION,
            PointEvaluator.of(
               DistanceCalculationMode.EUCLIDEAN.getFunction(),
               new DoubleThresholdCondition(new DoubleThreshold.Single(this.density.getInclusiveMin(), this.density.getInclusiveMax())),
               new DoubleRange.Constant(1.0),
               new ConstantCellJitter(this.jitter, this.jitter, this.jitter)
            ),
            seed.get()
         ),
         this.scale == 0.0 ? 0.0 : 1.0 / this.scale
      );
   }
}
