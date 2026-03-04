package com.hypixel.hytale.server.worldgen.loader.cave.shape;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.json.DoubleRangeJsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.procedurallib.supplier.IDoubleRange;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.cave.shape.PipeCaveNodeShape;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PipeCaveNodeShapeGeneratorJsonLoader extends CaveNodeShapeGeneratorJsonLoader {
   public PipeCaveNodeShapeGeneratorJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json) {
      super(seed.append(".PipeCaveNodeShapeGenerator"), dataFolder, json);
   }

   @Nonnull
   public PipeCaveNodeShape.PipeCaveNodeShapeGenerator load() {
      return new PipeCaveNodeShape.PipeCaveNodeShapeGenerator(this.loadRadius(), this.loadMiddleRadius(), this.loadLength(), this.loadInheritParentRadius());
   }

   @Nullable
   protected IDoubleRange loadRadius() {
      return new DoubleRangeJsonLoader<>(this.seed, this.dataFolder, this.get("Radius"), 3.0).load();
   }

   @Nullable
   protected IDoubleRange loadMiddleRadius() {
      IDoubleRange middleRadius = null;
      if (this.has("MiddleRadius")) {
         middleRadius = new DoubleRangeJsonLoader<>(this.seed, this.dataFolder, this.get("MiddleRadius"), 0.0).load();
      }

      return middleRadius;
   }

   @Nullable
   protected IDoubleRange loadLength() {
      return new DoubleRangeJsonLoader<>(this.seed, this.dataFolder, this.get("Length"), 5.0, 15.0).load();
   }

   protected boolean loadInheritParentRadius() {
      boolean inherit = true;
      if (this.has("InheritParentRadius")) {
         inherit = this.get("InheritParentRadius").getAsBoolean();
      }

      return inherit;
   }

   public interface Constants {
      String KEY_RADIUS = "Radius";
      String KEY_MIDDLE_RADIUS = "MiddleRadius";
      String KEY_LENGTH = "Length";
      String KEY_INHERIT_PARENT_RADIUS = "InheritParentRadius";
   }
}
