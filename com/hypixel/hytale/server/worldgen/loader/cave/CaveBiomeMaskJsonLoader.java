package com.hypixel.hytale.server.worldgen.loader.cave;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.condition.ConstantIntCondition;
import com.hypixel.hytale.procedurallib.condition.IIntCondition;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.cave.CaveBiomeMaskFlags;
import com.hypixel.hytale.server.worldgen.loader.biome.BiomeMaskJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.context.ZoneFileContext;
import com.hypixel.hytale.server.worldgen.util.condition.flag.CompositeInt2Flags;
import com.hypixel.hytale.server.worldgen.util.condition.flag.FlagOperator;
import com.hypixel.hytale.server.worldgen.util.condition.flag.Int2FlagsCondition;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CaveBiomeMaskJsonLoader extends JsonLoader<SeedStringResource, Int2FlagsCondition> {
   private final ZoneFileContext zoneContext;

   public CaveBiomeMaskJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, ZoneFileContext zoneContext) {
      super(seed.append(".CaveBiomeMask"), dataFolder, json);
      this.zoneContext = zoneContext;
   }

   @Nonnull
   public Int2FlagsCondition load() {
      IIntCondition generate = this.loadGenerationMask();
      if (generate == ConstantIntCondition.DEFAULT_FALSE) {
         return CaveBiomeMaskFlags.DEFAULT_DENY;
      } else {
         IIntCondition populate = this.loadPopulationMask();
         if (generate == ConstantIntCondition.DEFAULT_TRUE && populate == ConstantIntCondition.DEFAULT_TRUE) {
            return CaveBiomeMaskFlags.DEFAULT_ALLOW;
         } else {
            int defaultResult = this.loadDefaultResult();
            CompositeInt2Flags.FlagCondition[] flagConditions = this.loadFlagConditions(generate, populate);
            return new CompositeInt2Flags(defaultResult, flagConditions);
         }
      }
   }

   @Nullable
   protected IIntCondition loadGenerationMask() {
      return this.loadBiomeMask("Generate");
   }

   @Nullable
   protected IIntCondition loadPopulationMask() {
      return this.loadBiomeMask("Populate");
   }

   @Nonnull
   protected CompositeInt2Flags.FlagCondition[] loadFlagConditions(IIntCondition generate, IIntCondition populate) {
      return new CompositeInt2Flags.FlagCondition[]{
         new CompositeInt2Flags.FlagCondition(generate, FlagOperator.Or, 1), new CompositeInt2Flags.FlagCondition(populate, FlagOperator.Or, 2)
      };
   }

   protected int loadDefaultResult() {
      int result = 4;
      if (this.loadFlagSetting("Terminate", CaveBiomeMaskJsonLoader.Constants.DEFAULT_TERMINATE_SETTING)) {
         result ^= 4;
      }

      return result;
   }

   @Nullable
   protected IIntCondition loadBiomeMask(String maskName) {
      IIntCondition mask = ConstantIntCondition.DEFAULT_TRUE;
      if (this.has(maskName)) {
         mask = new BiomeMaskJsonLoader(this.seed, this.dataFolder, this.getRaw(maskName), maskName, this.zoneContext).load();
      }

      return mask;
   }

   protected boolean loadFlagSetting(String key, boolean defaultValue) {
      boolean result = defaultValue;
      if (this.has(key)) {
         result = this.getRaw(key).getAsBoolean();
      }

      return result;
   }

   public interface Constants {
      String KEY_GENERATION = "Generate";
      String KEY_POPULATION = "Populate";
      String KEY_TERMINATE = "Terminate";
      boolean DEFAULT_TERMINATE_SETTING = !CaveBiomeMaskFlags.canContinue(4);
   }
}
