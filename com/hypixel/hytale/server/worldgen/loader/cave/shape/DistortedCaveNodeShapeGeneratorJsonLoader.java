package com.hypixel.hytale.server.worldgen.loader.cave.shape;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.json.DoubleRangeJsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.procedurallib.logic.GeneralNoise;
import com.hypixel.hytale.procedurallib.supplier.IDoubleRange;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.cave.shape.CaveNodeShapeEnum;
import com.hypixel.hytale.server.worldgen.cave.shape.DistortedCaveNodeShape;
import com.hypixel.hytale.server.worldgen.cave.shape.distorted.DistortedShape;
import com.hypixel.hytale.server.worldgen.cave.shape.distorted.DistortedShapes;
import com.hypixel.hytale.server.worldgen.cave.shape.distorted.ShapeDistortion;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DistortedCaveNodeShapeGeneratorJsonLoader extends CaveNodeShapeGeneratorJsonLoader {
   public DistortedCaveNodeShapeGeneratorJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json) {
      super(seed.append(".DistortedCaveNodeShape"), dataFolder, json);
   }

   @Nonnull
   public CaveNodeShapeEnum.CaveNodeShapeGenerator load() {
      return new DistortedCaveNodeShape.DistortedCaveNodeShapeGenerator(
         this.loadShape(),
         this.loadWidth(),
         this.loadHeight(),
         this.loadMidWidth(),
         this.loadMidHeight(),
         this.loadLength(),
         this.loadInheritParentRadius(),
         this.loadShapeDistortion(),
         this.loadInterpolation()
      );
   }

   @Nonnull
   private DistortedShape.Factory loadShape() {
      if (this.has("Shape")) {
         DistortedShape.Factory shape = DistortedShapes.getByName(this.get("Shape").getAsString());
         if (shape != null) {
            return shape;
         }
      }

      return DistortedCaveNodeShapeGeneratorJsonLoader.Constants.DEFAULT_SHAPE;
   }

   @Nullable
   private IDoubleRange loadWidth() {
      return this.has("RadiusX")
         ? new DoubleRangeJsonLoader<>(this.seed, this.dataFolder, this.get("RadiusX"), 3.0).load()
         : new DoubleRangeJsonLoader<>(this.seed, this.dataFolder, this.get("Width"), 3.0).load();
   }

   @Nullable
   private IDoubleRange loadHeight() {
      return this.has("RadiusY")
         ? new DoubleRangeJsonLoader<>(this.seed, this.dataFolder, this.get("RadiusY"), 3.0).load()
         : new DoubleRangeJsonLoader<>(this.seed, this.dataFolder, this.get("Height"), 3.0).load();
   }

   @Nullable
   private IDoubleRange loadMidWidth() {
      IDoubleRange midWidth = null;
      if (this.has("MiddleWidth")) {
         midWidth = new DoubleRangeJsonLoader<>(this.seed, this.dataFolder, this.get("MiddleWidth"), 0.0).load();
      }

      return midWidth;
   }

   @Nullable
   private IDoubleRange loadMidHeight() {
      IDoubleRange midHeight = null;
      if (this.has("MiddleHeight")) {
         midHeight = new DoubleRangeJsonLoader<>(this.seed, this.dataFolder, this.get("MiddleHeight"), 0.0).load();
      }

      return midHeight;
   }

   @Nullable
   private IDoubleRange loadLength() {
      if (this.has("RadiusZ")) {
         return new DoubleRangeJsonLoader<>(this.seed, this.dataFolder, this.get("RadiusZ"), 3.0).load();
      } else {
         IDoubleRange length = null;
         if (this.has("Length")) {
            length = new DoubleRangeJsonLoader<>(this.seed, this.dataFolder, this.get("Length"), 5.0, 15.0).load();
         }

         return length;
      }
   }

   private boolean loadInheritParentRadius() {
      boolean inherit = true;
      if (this.has("InheritParentRadius")) {
         inherit = this.get("InheritParentRadius").getAsBoolean();
      }

      return inherit;
   }

   @Nullable
   private ShapeDistortion loadShapeDistortion() {
      ShapeDistortion distortion = ShapeDistortion.DEFAULT;
      if (this.has("Distortion")) {
         distortion = new ShapeDistortionJsonLoader<>(this.seed, this.dataFolder, this.get("Distortion")).load();
      }

      return distortion;
   }

   private GeneralNoise.InterpolationFunction loadInterpolation() {
      GeneralNoise.InterpolationFunction interpolation = DistortedCaveNodeShapeGeneratorJsonLoader.Constants.DEFAULT_INTERPOLATION;
      if (this.has("Interpolation")) {
         interpolation = GeneralNoise.InterpolationMode.valueOf(this.get("Interpolation").getAsString()).function;
      }

      return interpolation;
   }

   public interface Constants {
      String KEY_SHAPE = "Shape";
      String KEY_WIDTH = "Width";
      String KEY_HEIGHT = "Height";
      String KEY_MID_WIDTH = "MiddleWidth";
      String KEY_MID_HEIGHT = "MiddleHeight";
      String KEY_LENGTH = "Length";
      String KEY_RADIUS_X = "RadiusX";
      String KEY_RADIUS_Y = "RadiusY";
      String KEY_RADIUS_Z = "RadiusZ";
      String KEY_INHERIT_PARENT_RADIUS = "InheritParentRadius";
      String KEY_DISTORTION = "Distortion";
      String KEY_INTERPOLATION = "Interpolation";
      DistortedShape.Factory DEFAULT_SHAPE = DistortedShapes.getDefault();
      GeneralNoise.InterpolationFunction DEFAULT_INTERPOLATION = GeneralNoise.InterpolationMode.LINEAR.function;
   }
}
