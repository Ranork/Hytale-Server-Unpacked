package com.hypixel.hytale.server.worldgen.loader.cave.shape;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.json.DoubleRangeJsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.procedurallib.supplier.IDoubleRange;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.cave.shape.EmptyLineCaveNodeShape;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EmptyLineCaveNodeShapeGeneratorJsonLoader extends CaveNodeShapeGeneratorJsonLoader {
   public EmptyLineCaveNodeShapeGeneratorJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json) {
      super(seed.append(".EmptyLineCaveNodeShapeGenerator"), dataFolder, json);
   }

   @Nonnull
   public EmptyLineCaveNodeShape.EmptyLineCaveNodeShapeGenerator load() {
      return new EmptyLineCaveNodeShape.EmptyLineCaveNodeShapeGenerator(this.loadLength());
   }

   @Nullable
   protected IDoubleRange loadLength() {
      return new DoubleRangeJsonLoader<>(this.seed, this.dataFolder, this.get("Length"), 0.0).load();
   }

   public interface Constants {
      String KEY_LENGTH = "Length";
   }
}
