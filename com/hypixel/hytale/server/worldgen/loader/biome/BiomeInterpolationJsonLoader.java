package com.hypixel.hytale.server.worldgen.loader.biome;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hypixel.hytale.procedurallib.condition.IIntCondition;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.biome.BiomeInterpolation;
import com.hypixel.hytale.server.worldgen.loader.context.ZoneFileContext;
import com.hypixel.hytale.server.worldgen.util.condition.HashSetIntCondition;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BiomeInterpolationJsonLoader extends JsonLoader<SeedStringResource, BiomeInterpolation> {
   protected final ZoneFileContext zoneFileContext;

   public BiomeInterpolationJsonLoader(SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, ZoneFileContext zoneFileContext) {
      super(seed, dataFolder, json);
      this.zoneFileContext = zoneFileContext;
   }

   public BiomeInterpolation load() {
      int defaultRadius = this.loadDefaultRadius();
      Int2IntMap biomeRadii = this.loadBiomeRadii(defaultRadius);
      return BiomeInterpolation.create(defaultRadius, biomeRadii);
   }

   protected int loadDefaultRadius() {
      if (!this.has("DefaultRadius")) {
         return 5;
      } else {
         int radius = this.get("DefaultRadius").getAsInt();
         if (radius >= 0 && radius <= 5) {
            return radius;
         } else {
            throw new Error(String.format("Default biome interpolation radius %s lies outside the range 0-5", radius));
         }
      }
   }

   @Nonnull
   protected Int2IntMap loadBiomeRadii(int maxRadius) {
      if (!this.has("Biomes")) {
         return BiomeInterpolation.EMPTY_MAP;
      } else {
         JsonElement biomes = this.get("Biomes");
         if (!biomes.isJsonArray()) {
            throw new Error("Invalid json-type for Biomes property. Must be an array!");
         } else {
            Int2IntOpenHashMap biomeRadii = new Int2IntOpenHashMap();

            for (JsonElement entry : biomes.getAsJsonArray()) {
               this.loadBiomeEntry(entry, maxRadius, biomeRadii);
            }

            return biomeRadii;
         }
      }
   }

   protected void loadBiomeEntry(@Nonnull JsonElement entry, int defaultRadius, @Nonnull Int2IntMap biomeRadii) {
      if (!entry.isJsonObject()) {
         throw new Error("Invalid json-type for biome entry. Must be an object!");
      } else {
         int radius = loadBiomeRadius(entry.getAsJsonObject(), defaultRadius);
         if (radius != defaultRadius) {
            IIntCondition mask = this.loadBiomeMask(entry.getAsJsonObject());
            addBiomes(mask, radius, biomeRadii);
         }
      }
   }

   @Nullable
   protected IIntCondition loadBiomeMask(@Nonnull JsonObject entry) {
      if (!entry.has("Mask")) {
         throw new Error(String.format("Missing property %s", "Mask"));
      } else {
         return new BiomeMaskJsonLoader(this.seed, this.dataFolder, entry.get("Mask"), "InterpolationMask", this.zoneFileContext).load();
      }
   }

   protected static int loadBiomeRadius(@Nonnull JsonObject entry, int maxRadius) {
      if (!entry.has("Radius")) {
         throw new Error(String.format("Missing property %s", "Radius"));
      } else {
         int radius = entry.get("Radius").getAsInt();
         if (radius >= 0 && radius <= maxRadius) {
            return radius;
         } else {
            throw new Error(String.format("Biome interpolation radius %s is outside the range 0-%s", radius, maxRadius));
         }
      }
   }

   protected static void addBiomes(IIntCondition mask, int radius, @Nonnull Int2IntMap biomeRadii) {
      if (mask instanceof HashSetIntCondition) {
         int radius2 = radius * radius;
         IntSet biomes = ((HashSetIntCondition)mask).getSet();
         IntIterator var5 = biomes.iterator();

         while (var5.hasNext()) {
            int biome = (Integer)var5.next();
            if (biomeRadii.containsKey(biome)) {
               throw new Error("Duplicate biome detected in interpolation rules");
            }

            biomeRadii.put(biome, radius2);
         }
      }
   }

   public interface Constants {
      String KEY_DEFAULT_RADIUS = "DefaultRadius";
      String KEY_RADIUS = "Radius";
      String KEY_BIOMES = "Biomes";
      String KEY_MASK = "Mask";
      String SEED_OFFSET_MASK = "InterpolationMask";
      String ERROR_MISSING_PROPERTY = "Missing property %s";
      String ERROR_INVALID_BIOME_LIST = "Invalid json-type for Biomes property. Must be an array!";
      String ERROR_INVALID_BIOME_ENTRY = "Invalid json-type for biome entry. Must be an object!";
      String ERROR_DUPLICATE_BIOME = "Duplicate biome detected in interpolation rules";
      String ERROR_BIOME_RADIUS = "Biome interpolation radius %s is outside the range 0-%s";
      String ERROR_DEFAULT_RADIUS = "Default biome interpolation radius %s lies outside the range 0-5";
   }
}
