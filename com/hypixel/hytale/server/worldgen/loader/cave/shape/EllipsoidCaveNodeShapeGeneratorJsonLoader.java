package com.hypixel.hytale.server.worldgen.loader.cave.shape;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.json.DoubleRangeJsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.procedurallib.supplier.IDoubleRange;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.cave.shape.EllipsoidCaveNodeShape;
import java.nio.file.Path;
import java.util.Objects;
import javax.annotation.Nonnull;

public class EllipsoidCaveNodeShapeGeneratorJsonLoader extends CaveNodeShapeGeneratorJsonLoader {
   public EllipsoidCaveNodeShapeGeneratorJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json) {
      super(seed.append(".EllipsoidCaveNodeShapeGenerator"), dataFolder, json);
   }

   @Nonnull
   public EllipsoidCaveNodeShape.EllipsoidCaveNodeShapeGenerator load() {
      IDoubleRange radiusX = null;
      IDoubleRange radiusY = null;
      IDoubleRange radiusZ = null;
      if (this.has("Radius")) {
         radiusX = radiusY = radiusZ = new DoubleRangeJsonLoader<>(this.seed, this.dataFolder, this.get("Radius"), 5.0).load();
      }

      if (this.has("RadiusX")) {
         radiusX = new DoubleRangeJsonLoader<>(this.seed, this.dataFolder, this.get("RadiusX"), 5.0).load();
      }

      if (this.has("RadiusY")) {
         radiusY = new DoubleRangeJsonLoader<>(this.seed, this.dataFolder, this.get("RadiusY"), 5.0).load();
      }

      if (this.has("RadiusZ")) {
         radiusZ = new DoubleRangeJsonLoader<>(this.seed, this.dataFolder, this.get("RadiusZ"), 5.0).load();
      }

      Objects.requireNonNull(radiusX, "RadiusX");
      Objects.requireNonNull(radiusY, "RadiusY");
      Objects.requireNonNull(radiusZ, "RadiusZ");
      return new EllipsoidCaveNodeShape.EllipsoidCaveNodeShapeGenerator(radiusX, radiusY, radiusZ);
   }

   public interface Constants {
      String KEY_RADIUS = "Radius";
      String KEY_RADIUS_X = "RadiusX";
      String KEY_RADIUS_Y = "RadiusY";
      String KEY_RADIUS_Z = "RadiusZ";
      String ERROR_RADIUS_NOT_SET = "%s was not set for Ellipsoid!";
   }
}
