package com.hypixel.hytale.server.worldgen.loader.cave;

import com.google.gson.JsonObject;
import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.procedurallib.file.FileIO;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.cave.CaveNodeType;
import com.hypixel.hytale.server.worldgen.loader.context.ZoneFileContext;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

public class CaveNodeTypeStorage {
   protected final SeedString<SeedStringResource> seed;
   protected final Path dataFolder;
   protected final Path caveFolder;
   protected final ZoneFileContext zoneContext;
   @Nonnull
   protected final Map<String, CaveNodeType> caveNodeTypes;

   public CaveNodeTypeStorage(SeedString<SeedStringResource> seed, Path dataFolder, Path caveFolder, ZoneFileContext zoneContext) {
      this.seed = seed;
      this.dataFolder = dataFolder;
      this.caveFolder = caveFolder;
      this.zoneContext = zoneContext;
      this.caveNodeTypes = new HashMap<>();
   }

   public SeedString<SeedStringResource> getSeed() {
      return this.seed;
   }

   public void add(String name, CaveNodeType caveNodeType) {
      if (this.caveNodeTypes.containsKey(name)) {
         throw new Error(String.format("CaveNodeType (%s) has already been added to CaveNodeTypeStorage!", name));
      } else {
         this.caveNodeTypes.put(name, caveNodeType);
      }
   }

   @Nonnull
   public CaveNodeType getOrLoadCaveNodeType(@Nonnull String name) {
      CaveNodeType caveNodeType = this.getCaveNodeType(name);
      if (caveNodeType == null) {
         caveNodeType = this.loadCaveNodeType(name);
      }

      return caveNodeType;
   }

   public CaveNodeType getCaveNodeType(String name) {
      return this.caveNodeTypes.get(name);
   }

   @Nonnull
   public CaveNodeType loadCaveNodeType(@Nonnull String name) {
      String relativePath = String.format("%s.node.json", name.replace(".", File.separator));
      Path file = PathUtil.resolvePathWithinDir(this.caveFolder, relativePath);
      if (file == null) {
         throw new Error(String.format("Invalid cave node type name: %s", name));
      } else {
         try {
            JsonObject caveNodeJson = FileIO.load(file, JsonLoader.JSON_OBJ_LOADER);
            return this.loadCaveNodeType(name, caveNodeJson);
         } catch (Throwable var5) {
            throw new Error(String.format("Error while loading CaveNodeType %s for world generator from %s", name, file.toString()), var5);
         }
      }
   }

   @Nonnull
   public CaveNodeType loadCaveNodeType(@Nonnull String name, @Nonnull JsonObject json) {
      return new CaveNodeTypeJsonLoader(this.seed, this.dataFolder, json, name, this, this.zoneContext).load();
   }

   public interface Constants {
      String ERROR_ALREADY_ADDED = "CaveNodeType (%s) has already been added to CaveNodeTypeStorage!";
      String ERROR_LOADING_CAVE_NODE_TYPE = "Error while loading CaveNodeType %s for world generator from %s";
      String FILE_SUFFIX = "%s.node.json";
   }
}
