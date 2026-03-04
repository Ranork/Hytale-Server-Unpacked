package com.hypixel.hytale.server.worldgen.loader.cave;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.cave.CaveType;
import com.hypixel.hytale.server.worldgen.util.BlockFluidEntry;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class FluidLevelJsonLoader extends JsonLoader<SeedStringResource, CaveType.FluidLevel> {
   public FluidLevelJsonLoader(SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json) {
      super(seed, dataFolder, json);
   }

   @Nonnull
   public CaveType.FluidLevel load() {
      int blockId = 0;
      int rotation = 0;
      int fluidId = 0;
      if (this.has("Block")) {
         String blockString = this.get("Block").getAsString();
         BlockPattern.BlockEntry blockTypeKey = BlockPattern.BlockEntry.decode(blockString);
         blockId = BlockType.getAssetMap().getIndex(blockTypeKey.blockTypeKey());
         if (blockId == Integer.MIN_VALUE) {
            throw new Error(String.format("Could not resolve block \"%s\" from BlockTypes.", blockString));
         }

         rotation = blockTypeKey.rotation();
      }

      if (this.has("Fluid")) {
         String fluidKey = this.get("Fluid").getAsString();
         fluidId = Fluid.getAssetMap().getIndex(fluidKey);
         if (fluidId == Integer.MIN_VALUE) {
            throw new Error(String.format("Could not resolve fluid \"%s\" from Fluids.", fluidKey));
         }
      }

      if (!this.has("Block") && !this.has("Fluid")) {
         throw new IllegalArgumentException("Could not find block to use in FluidLevel container. Keyword: Block");
      } else {
         return new CaveType.FluidLevel(new BlockFluidEntry(blockId, rotation, fluidId), this.loadHeight());
      }
   }

   protected int loadHeight() {
      return this.get("Height").getAsInt();
   }

   public interface Constants {
      String KEY_HEIGHT = "Height";
      String KEY_BLOCK = "Block";
      String KEY_FLUID = "Fluid";
      String ERROR_NO_BLOCK = "Could not find block to use in FluidLevel container. Keyword: Block";
      String ERROR_UNKOWN_BLOCK = "Could not resolve block \"%s\" from BlockTypes.";
      String ERROR_UNKOWN_FLUID = "Could not resolve fluid \"%s\" from Fluids.";
   }
}
