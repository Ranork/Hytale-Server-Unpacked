package com.hypixel.hytale.server.worldgen.loader.cave.shape;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.cave.shape.CaveNodeShapeEnum;
import java.nio.file.Path;

public abstract class CaveNodeShapeGeneratorJsonLoader extends JsonLoader<SeedStringResource, CaveNodeShapeEnum.CaveNodeShapeGenerator> {
   public CaveNodeShapeGeneratorJsonLoader(SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json) {
      super(seed, dataFolder, json);
   }
}
