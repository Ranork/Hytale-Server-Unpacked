package com.hypixel.hytale.server.worldgen.loader.biome;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.condition.ConstantIntCondition;
import com.hypixel.hytale.procedurallib.condition.IIntCondition;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.loader.context.BiomeFileContext;
import com.hypixel.hytale.server.worldgen.loader.context.FileContext;
import com.hypixel.hytale.server.worldgen.loader.context.ZoneFileContext;
import com.hypixel.hytale.server.worldgen.loader.util.FileMaskCache;
import com.hypixel.hytale.server.worldgen.util.condition.IntConditionBuilder;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.nio.file.Path;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BiomeMaskJsonLoader extends JsonLoader<SeedStringResource, IIntCondition> {
   private final ZoneFileContext zoneContext;
   @Nullable
   private String fileName = null;

   public BiomeMaskJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, String maskName, ZoneFileContext zoneContext) {
      super(seed.append(".BiomeMask-" + maskName), dataFolder, json);
      this.zoneContext = zoneContext;
   }

   @Nullable
   public IIntCondition load() {
      FileMaskCache<IIntCondition> biomeMaskRegistry = this.seed.get().getBiomeMaskRegistry();
      if (this.fileName != null) {
         IIntCondition mask = biomeMaskRegistry.getIfPresentFileMask(this.fileName);
         if (mask != null) {
            return mask;
         }
      }

      IIntCondition mask = this.loadMask();
      if (this.fileName != null) {
         biomeMaskRegistry.putFileMask(this.fileName, mask);
      }

      return mask;
   }

   protected IIntCondition loadMask() {
      IIntCondition mask = ConstantIntCondition.DEFAULT_TRUE;
      if (this.json.isJsonArray()) {
         IntConditionBuilder builder = new IntConditionBuilder(IntOpenHashSet::new, -1);
         JsonArray array = this.json.getAsJsonArray();

         for (int i = 0; i < array.size(); i++) {
            JsonElement element = array.get(i);
            String rule = element.getAsString();
            this.parseRule(rule, builder);
         }

         mask = builder.buildOrDefault(ConstantIntCondition.DEFAULT_TRUE);
      }

      return mask;
   }

   protected void parseRule(@Nonnull String rule, @Nonnull IntConditionBuilder builder) {
      int zoneMarker = rule.indexOf(46);
      int typeMarker = rule.indexOf(35);
      ZoneFileContext zone = parseZone(rule, zoneMarker, this.zoneContext);
      String biomeName = parseBiomeName(rule, zoneMarker, typeMarker);
      BiomeFileContext.Type biomeType = parseBiomeType(rule, typeMarker + 1);
      boolean result;
      if (biomeType == null) {
         result = collectBiomes(zone.getTileBiomes(), biomeName, builder);
         result |= collectBiomes(zone.getCustomBiomes(), biomeName, builder);
      } else {
         result = collectBiomes(zone.getBiomes(biomeType), biomeName, builder);
      }

      if (!result) {
         throw new Error(
            String.format(
               "Failed to parse BiomeMask rule '%s'. Unable to find a %s called %s in %s", rule, getDisplayName(biomeType), biomeName, zone.getName()
            )
         );
      }
   }

   @Override
   protected JsonElement loadFileConstructor(String filePath) {
      this.fileName = filePath;
      return this.seed.get().getBiomeMaskRegistry().cachedFile(filePath, x$0 -> super.loadFileConstructor(x$0));
   }

   private static boolean collectBiomes(
      @Nonnull FileContext.Registry<BiomeFileContext> registry, @Nonnull String biomeName, @Nonnull IntConditionBuilder builder
   ) {
      if (!biomeName.equals("*")) {
         if (registry.contains(biomeName)) {
            BiomeFileContext biome = registry.get(biomeName);
            builder.add(biome.getId());
            return true;
         } else {
            return false;
         }
      } else {
         for (Entry<String, BiomeFileContext> biomeEntry : registry) {
            builder.add(biomeEntry.getValue().getId());
         }

         return true;
      }
   }

   @Nonnull
   private static ZoneFileContext parseZone(@Nonnull String rule, int marker, @Nonnull ZoneFileContext context) {
      if (marker <= 0) {
         return context;
      } else {
         String zoneName = rule.substring(0, marker);
         return context.getParentContext().getZones().get(zoneName);
      }
   }

   @Nullable
   private static BiomeFileContext.Type parseBiomeType(@Nonnull String rule, int marker) {
      if (marker <= 0) {
         return null;
      } else {
         String typeName = rule.substring(marker);
         return BiomeFileContext.Type.valueOf(typeName);
      }
   }

   @Nonnull
   private static String parseBiomeName(@Nonnull String rule, int zoneMarker, int typeMarker) {
      int nameStart = zoneMarker + 1;
      int nameEnd = typeMarker > zoneMarker ? typeMarker : rule.length();
      return nameStart == nameEnd ? "*" : rule.substring(nameStart, nameEnd);
   }

   @Nonnull
   private static String getDisplayName(@Nullable BiomeFileContext.Type type) {
      return type == null ? "Biome" : type.getDisplayName();
   }

   public interface Constants {
      char ZONE_MARKER = '.';
      char TYPE_MARKER = '#';
      int NULL_BIOME_ID = -1;
      String WILDCARD_BIOME_NAME = "*";
      String BIOME_TYPE_ANY_DISPLAY_NAME = "Biome";
      String ERROR_PARSE_RULE = "Failed to parse BiomeMask rule '%s'. Unable to find a %s called %s in %s";
   }
}
