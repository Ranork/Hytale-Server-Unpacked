package com.hypixel.hytale.server.worldgen.loader.zone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.loader.util.ColorUtil;
import com.hypixel.hytale.server.worldgen.zone.Zone;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UniqueZoneEntryJsonLoader extends JsonLoader<SeedStringResource, Zone.UniqueEntry[]> {
   protected final Map<String, Zone> zoneLookup;

   public UniqueZoneEntryJsonLoader(SeedString<SeedStringResource> seed, Path dataFolder, @Nullable JsonElement json, Map<String, Zone> zoneLookup) {
      super(seed, dataFolder, json);
      this.zoneLookup = zoneLookup;
   }

   @Nonnull
   public Zone.UniqueEntry[] load() {
      if (this.json == null) {
         return Zone.UniqueEntry.EMPTY_ARRAY;
      } else if (!this.json.isJsonArray()) {
         throw new Error("Unexpected type for 'UniqueZones' field, expected array");
      } else {
         JsonArray arrayJson = this.json.getAsJsonArray();
         Zone.UniqueEntry[] entries = new Zone.UniqueEntry[arrayJson.size()];

         for (int i = 0; i < arrayJson.size(); i++) {
            JsonElement entry = arrayJson.get(i);
            if (!entry.isJsonObject()) {
               throw new Error("Unexpected type for unique zone entry: #" + i);
            }

            entries[i] = this.loadEntry(i, entry.getAsJsonObject());
         }

         return entries;
      }
   }

   protected Zone.UniqueEntry loadEntry(int index, JsonObject json) {
      JsonElement zoneJson = json.get("Zone");
      if (zoneJson == null) {
         throw new Error("Missing 'Zone' field in unique zone entry: #" + index);
      } else {
         JsonElement colorJson = json.get("Color");
         if (colorJson == null) {
            throw new Error("Missing 'Color' field in unique zone entry: #" + index);
         } else {
            JsonElement parentJson = json.get("Parent");
            if (parentJson == null) {
               throw new Error("Missing 'Parent' field in unique zone entry: #" + index);
            } else {
               JsonElement radiusJson = json.get("Radius");
               JsonElement paddingJson = json.get("Padding");
               Zone zone = this.zoneLookup.get(zoneJson.getAsString());
               if (zone == null) {
                  throw new Error("Unknown zone '" + zoneJson.getAsString() + "' in unique zone entry: #" + index);
               } else {
                  int color = ColorUtil.hexString(colorJson.getAsString());
                  int[] parent = loadParentColors(index, parentJson);
                  int radius = radiusJson != null ? radiusJson.getAsInt() : 1;
                  int padding = paddingJson != null ? paddingJson.getAsInt() : 0;
                  return new Zone.UniqueEntry(zone, color, parent, radius, padding);
               }
            }
         }
      }
   }

   protected static int[] loadParentColors(int index, JsonElement json) {
      if (!json.isJsonArray()) {
         if (json.isJsonPrimitive()) {
            return new int[]{ColorUtil.hexString(json.getAsString())};
         } else {
            throw new Error("Unexpected type for 'Parent' field in unique zone entry: #" + index);
         }
      } else {
         JsonArray arr = json.getAsJsonArray();
         int[] colors = new int[arr.size()];

         for (int i = 0; i < arr.size(); i++) {
            colors[i] = ColorUtil.hexString(arr.get(i).getAsString());
         }

         return colors;
      }
   }

   public static void collectZones(Set<String> zoneSet, @Nullable JsonElement json) {
      if (json != null) {
         if (!json.isJsonArray()) {
            throw new Error("Unexpected type for 'UniqueZones' field, expected array");
         } else {
            JsonArray arrayJson = json.getAsJsonArray();

            for (int i = 0; i < arrayJson.size(); i++) {
               JsonElement entry = arrayJson.get(i);
               if (!entry.isJsonObject()) {
                  throw new Error("Unexpected type for unique zone entry: #" + i);
               }

               JsonElement zone = entry.getAsJsonObject().get("Zone");
               if (zone == null) {
                  throw new Error("Missing 'Zone' field in unique zone entry: #" + i);
               }

               zoneSet.add(zone.getAsString());
            }
         }
      }
   }

   public interface Constants {
      String KEY_ZONE = "Zone";
      String KEY_COLOR = "Color";
      String KEY_PARENT = "Parent";
      String KEY_RADIUS = "Radius";
      String KEY_PADDING = "Padding";
      int DEFAULT_RADIUS = 1;
      int DEFAULT_PADDING = 0;
      String ERROR_ENTRIES_TYPE = "Unexpected type for 'UniqueZones' field, expected array";
      String ERROR_ENTRY_TYPE = "Unexpected type for unique zone entry: #";
      String ERROR_PARENT_TYPE = "Unexpected type for 'Parent' field in unique zone entry: #";
      String ERROR_MISSING_ZONE = "Missing 'Zone' field in unique zone entry: #";
      String ERROR_MISSING_COLOR = "Missing 'Color' field in unique zone entry: #";
      String ERROR_MISSING_PARENT = "Missing 'Parent' field in unique zone entry: #";
   }
}
