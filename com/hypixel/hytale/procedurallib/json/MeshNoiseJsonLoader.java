package com.hypixel.hytale.procedurallib.json;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.NoiseFunction;
import com.hypixel.hytale.procedurallib.condition.DoubleThresholdCondition;
import com.hypixel.hytale.procedurallib.condition.IIntCondition;
import com.hypixel.hytale.procedurallib.logic.HexMeshNoise;
import com.hypixel.hytale.procedurallib.logic.MeshNoise;
import com.hypixel.hytale.procedurallib.logic.cell.CellType;
import com.hypixel.hytale.procedurallib.logic.cell.evaluator.DensityPointEvaluator;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class MeshNoiseJsonLoader<K extends SeedResource> extends AbstractCellJitterJsonLoader<K, NoiseFunction> {
   public MeshNoiseJsonLoader(@Nonnull SeedString<K> seed, Path dataFolder, JsonElement json) {
      super(seed.append(".MeshNoise"), dataFolder, json);
   }

   public NoiseFunction load() {
      return (NoiseFunction)(switch (this.loadCellType()) {
         case SQUARE -> this.loadGridMeshNoise();
         case HEX -> this.loadHexMeshNoise();
      });
   }

   @Nonnull
   protected MeshNoise loadGridMeshNoise() {
      double defaultJitter = this.loadDefaultJitter();
      return new MeshNoise(this.loadDensity(), this.loadThickness(), this.loadJitterX(defaultJitter), this.loadJitterY(defaultJitter));
   }

   @Nonnull
   protected HexMeshNoise loadHexMeshNoise() {
      return new HexMeshNoise(this.loadDensity(), this.loadThickness(), this.loadJitter(), this.loadLinesX(), this.loadLinesY(), this.loadLinesZ());
   }

   @Nonnull
   protected CellType loadCellType() {
      CellType cellType = CellNoiseJsonLoader.Constants.DEFAULT_CELL_TYPE;
      if (this.has("CellType")) {
         cellType = CellType.valueOf(this.get("CellType").getAsString());
      }

      return cellType;
   }

   protected double loadThickness() {
      if (!this.has("Thickness")) {
         throw new IllegalStateException("Could not find thickness. Keyword: Thickness");
      } else {
         return this.get("Thickness").getAsDouble();
      }
   }

   @Nonnull
   protected IIntCondition loadDensity() {
      return DensityPointEvaluator.getDensityCondition(
         this.has("Density") ? new DoubleThresholdCondition(new DoubleThresholdJsonLoader<>(this.seed, this.dataFolder, this.get("Density")).load()) : null
      );
   }

   protected boolean loadLinesX() {
      return this.loadLinesFlag("LinesX", true);
   }

   protected boolean loadLinesY() {
      return this.loadLinesFlag("LinesY", true);
   }

   protected boolean loadLinesZ() {
      return this.loadLinesFlag("LinesZ", true);
   }

   protected boolean loadLinesFlag(String key, boolean defaulValue) {
      return !this.has(key) ? defaulValue : this.get(key).getAsBoolean();
   }

   public interface Constants {
      String KEY_THICKNESS = "Thickness";
      String KEY_LINES_X = "LinesX";
      String KEY_LINES_Y = "LinesY";
      String KEY_LINES_Z = "LinesZ";
      String ERROR_NO_THICKNESS = "Could not find thickness. Keyword: Thickness";
      boolean DEFAULT_LINES_X = true;
      boolean DEFAULT_LINES_Y = true;
      boolean DEFAULT_LINES_Z = true;
   }
}
