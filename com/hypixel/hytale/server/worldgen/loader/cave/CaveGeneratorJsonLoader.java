package com.hypixel.hytale.server.worldgen.loader.cave;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hypixel.hytale.procedurallib.file.AssetPath;
import com.hypixel.hytale.procedurallib.file.FileIO;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.cave.CaveGenerator;
import com.hypixel.hytale.server.worldgen.cave.CaveType;
import com.hypixel.hytale.server.worldgen.loader.context.ZoneFileContext;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CaveGeneratorJsonLoader extends JsonLoader<SeedStringResource, CaveGenerator> {
   protected final Path caveFolder;
   protected final ZoneFileContext zoneContext;

   public CaveGeneratorJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, Path caveFolder, ZoneFileContext zoneContext) {
      super(seed.append(".CaveGenerator"), dataFolder, json);
      this.caveFolder = caveFolder;
      this.zoneContext = zoneContext;
   }

   @Nullable
   public CaveGenerator load() {
      CaveGenerator caveGenerator = null;
      if (this.caveFolder != null) {
         AssetPath assetPath = FileIO.resolve(this.caveFolder.resolve("Caves.json"));
         if (!FileIO.exists(assetPath)) {
            return null;
         }

         try {
            JsonObject cavesJson = FileIO.load(assetPath, JsonLoader.JSON_OBJ_LOADER);
            caveGenerator = new CaveGenerator(this.loadCaveTypes(cavesJson));
         } catch (Throwable var4) {
            throw new Error(String.format("Error while loading caves for world generator from %s", assetPath.toString()), var4);
         }
      }

      return caveGenerator;
   }

   @Nonnull
   protected CaveType[] loadCaveTypes(@Nonnull JsonObject jsonObject) {
      return new CaveTypesJsonLoader(this.seed, this.dataFolder, jsonObject.get("Types"), this.caveFolder, this.zoneContext).load();
   }

   public interface Constants {
      String FILE_CAVES_JSON = "Caves.json";
      String KEY_TYPES = "Types";
      String ERROR_LOADING_CAVES = "Error while loading caves for world generator from %s";
   }
}
