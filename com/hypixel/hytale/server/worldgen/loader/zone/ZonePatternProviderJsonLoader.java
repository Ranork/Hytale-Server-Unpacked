package com.hypixel.hytale.server.worldgen.loader.zone;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.PointGeneratorJsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.procedurallib.logic.point.IPointGenerator;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.chunk.MaskProvider;
import com.hypixel.hytale.server.worldgen.climate.ClimateColor;
import com.hypixel.hytale.server.worldgen.climate.ClimateMaskProvider;
import com.hypixel.hytale.server.worldgen.climate.ClimateType;
import com.hypixel.hytale.server.worldgen.zone.Zone;
import com.hypixel.hytale.server.worldgen.zone.ZoneColorMapping;
import com.hypixel.hytale.server.worldgen.zone.ZonePatternProvider;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ZonePatternProviderJsonLoader extends JsonLoader<SeedStringResource, ZonePatternProvider> {
   protected final MaskProvider maskProvider;
   protected Zone[] zones;
   protected Map<String, Zone> zoneLookup = Map.of();

   public ZonePatternProviderJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, MaskProvider maskProvider) {
      super(seed.append(".ZonePatternGenerator"), dataFolder, json);
      this.maskProvider = maskProvider;
   }

   public void setZones(Zone[] zones) {
      this.zones = zones;
      this.zoneLookup = new HashMap<>();

      for (Zone zone : zones) {
         this.zoneLookup.put(zone.name(), zone);
      }
   }

   @Nonnull
   public ZonePatternProvider load() {
      return new ZonePatternProvider(this.loadGridGenerator(), this.zones, this.loadUniqueZoneCandidates(), this.maskProvider, this.loadColorMapping());
   }

   @Nullable
   protected IPointGenerator loadGridGenerator() {
      return new PointGeneratorJsonLoader<>(this.seed, this.dataFolder, this.get("GridGenerator")).load();
   }

   @Nonnull
   protected ZoneColorMapping loadColorMapping() {
      if (!this.has("MaskMapping")) {
         throw new IllegalArgumentException("Could not find mappings for colors in mask file. Keyword: MaskMapping");
      } else {
         ZoneColorMapping colorMapping = new ZoneColorMappingJsonLoader(this.seed, this.dataFolder, this.get("MaskMapping"), this.zoneLookup).load();
         this.ensureMaskIntegrity(colorMapping);
         return colorMapping;
      }
   }

   @Nonnull
   public Set<String> loadZoneRequirement() {
      return new ZoneRequirementJsonLoader(this.seed, this.dataFolder, this.json).load();
   }

   protected void ensureMaskIntegrity(@Nonnull ZoneColorMapping zoneColorMapping) {
      if (this.maskProvider instanceof ClimateMaskProvider climateMask) {
         for (ClimateType parent : climateMask.getGraph().getParents()) {
            if (parent.children.length == 0) {
               validateMapping(parent, parent, parent.color, zoneColorMapping, "");
               validateMapping(parent, parent, parent.island, zoneColorMapping, "Island");
            } else {
               for (ClimateType child : parent.children) {
                  validateMapping(parent, child, child.color, zoneColorMapping, "");
                  validateMapping(parent, child, child.island, zoneColorMapping, "Island.");
               }
            }
         }
      } else {
         this.maskProvider.getFuzzyZoom().getExactZoom().getDistanceProvider().getColors().forEach(rgb -> {
            if (zoneColorMapping.get(rgb) == null) {
               throw new NullPointerException(Integer.toHexString(rgb));
            }
         });
      }
   }

   protected Zone.UniqueCandidate[] loadUniqueZoneCandidates() {
      if (this.maskProvider instanceof ClimateMaskProvider climateMask) {
         return climateMask.getUniqueZoneCandidates(this.zoneLookup);
      } else {
         Zone.UniqueEntry[] uniqueZones = new UniqueZoneEntryJsonLoader(this.seed, this.dataFolder, this.get("UniqueZones"), this.zoneLookup).load();
         return this.maskProvider.generateUniqueZoneCandidates(uniqueZones, 100);
      }
   }

   protected static void validateMapping(
      @Nullable ClimateType parent, @Nonnull ClimateType type, @Nonnull ClimateColor color, ZoneColorMapping mapping, String prefix
   ) {
      if (mapping.get(color.land) == null) {
         throw new Error(prefix + "Color is not mapped in climate type: " + ClimateType.name(parent, type));
      } else if (mapping.get(color.shore) == null) {
         throw new Error(prefix + "Shore is not mapped in climate type: " + ClimateType.name(parent, type));
      } else if (mapping.get(color.ocean) == null) {
         throw new Error(prefix + "Ocean is not mapped in climate type: " + ClimateType.name(parent, type));
      } else if (mapping.get(color.shallowOcean) == null) {
         throw new Error(prefix + "ShallowOcean is not mapped in climate type: " + ClimateType.name(parent, type));
      }
   }

   public interface Constants {
      String KEY_GRID_GENERATOR = "GridGenerator";
      String KEY_UNIQUE_ZONES = "UniqueZones";
      String KEY_MASK_MAPPING = "MaskMapping";
      String ERROR_UNMAPPED_COLOR = "Mask image contains unmapped color! #%s";
      String ERROR_NO_MAPPING = "Could not find mappings for colors in mask file. Keyword: MaskMapping";
      int UNIQUE_ZONE_CANDIDATE_POS_LIMIT = 100;
   }
}
