package com.hypixel.hytale.builtin.hytalegenerator.material;

import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MaterialCache {
   @Nonnull
   private final ConcurrentHashMap<Integer, SolidMaterial> hashToSolidMap = new ConcurrentHashMap<>();
   @Nonnull
   private final ConcurrentHashMap<Integer, FluidMaterial> hashToFluidMap = new ConcurrentHashMap<>();
   @Nonnull
   private final ConcurrentHashMap<Integer, Material> hashToMaterialMap = new ConcurrentHashMap<>();
   @Nullable
   public final SolidMaterial EMPTY_AIR = this.getSolidMaterial("Empty_Air");
   @Nullable
   public final SolidMaterial ROCK_STONE = this.getSolidMaterial("Rock_Stone");
   @Nullable
   public final SolidMaterial SOIL_GRASS = this.getSolidMaterial("Soil_Grass");
   @Nullable
   public final SolidMaterial SOIL_DIRT = this.getSolidMaterial("Soil_Dirt");
   @Nullable
   public final SolidMaterial SOIL_MUD = this.getSolidMaterial("Soil_Mud");
   @Nullable
   public final SolidMaterial SOIL_NEEDLES = this.getSolidMaterial("Soil_Needles");
   @Nullable
   public final SolidMaterial SOIL_GRAVEL = this.getSolidMaterial("Soil_Gravel");
   @Nullable
   public final SolidMaterial ROCK_QUARTZITE = this.getSolidMaterial("Rock_Quartzite");
   @Nullable
   public final SolidMaterial ROCK_MARBLE = this.getSolidMaterial("Rock_Marble");
   @Nullable
   public final SolidMaterial ROCK_SHALE = this.getSolidMaterial("Rock_Shale");
   @Nullable
   public final SolidMaterial FLUID_WATER = this.getSolidMaterial("Fluid_Water");
   @Nullable
   public final SolidMaterial BEDROCK = this.getSolidMaterial("Rock_Volcanic");
   @Nullable
   public final FluidMaterial UNKNOWN_FLUID = this.getFluidMaterial(Fluid.UNKNOWN.getId());
   @Nullable
   public final FluidMaterial EMPTY_FLUID = this.getFluidMaterial(Fluid.EMPTY.getId());
   @Nonnull
   public final Material EMPTY = this.getMaterial(this.EMPTY_AIR, this.EMPTY_FLUID);

   @Nonnull
   public Material getMaterial(@Nonnull SolidMaterial solidMaterial, @Nonnull FluidMaterial fluidMaterial) {
      int hash = Material.hashCode(solidMaterial, fluidMaterial);
      Material material = this.hashToMaterialMap.get(hash);
      if (material == null) {
         material = new Material(solidMaterial, fluidMaterial);
         this.hashToMaterialMap.put(hash, material);
         return material;
      } else {
         return material;
      }
   }

   @Nullable
   public FluidMaterial getFluidMaterial(@Nonnull String fluidString) {
      int fluidId = 0;
      Fluid key = Fluid.getAssetMap().getAsset(fluidString);
      if (key != null) {
         fluidId = Fluid.getAssetMap().getIndex(fluidString);
         byte level = fluidId == 0 ? 0 : (byte)key.getMaxFluidLevel();
         return this.getOrRegisterFluid(fluidId, level);
      } else {
         LoggerUtil.getLogger().warning("Attempted to register an invalid Fluid " + fluidString + ", using Unknown instead.");
         return this.UNKNOWN_FLUID;
      }
   }

   @Nullable
   public FluidMaterial getFluidMaterial(int fluidId, byte level) {
      Fluid key = Fluid.getAssetMap().getAsset(fluidId);
      if (key == null) {
         LoggerUtil.getLogger().warning("Attempted to register an invalid Fluid " + fluidId + ", using Unknown instead.");
         return this.UNKNOWN_FLUID;
      } else {
         return this.getOrRegisterFluid(fluidId, level);
      }
   }

   @Nonnull
   private FluidMaterial getOrRegisterFluid(int fluidId, byte level) {
      int hash = FluidMaterial.contentHash(fluidId, level);
      FluidMaterial fluidMaterial = this.hashToFluidMap.get(hash);
      if (fluidMaterial != null) {
         return fluidMaterial;
      } else {
         fluidMaterial = new FluidMaterial(this, fluidId, level);
         this.hashToFluidMap.put(hash, fluidMaterial);
         return fluidMaterial;
      }
   }

   @Nullable
   public SolidMaterial getSolidMaterial(@Nonnull String solidString, @Nonnull RotationTuple rotation) {
      int blockId = 0;
      BlockType key = BlockType.fromString(solidString);
      if (key != null) {
         blockId = BlockType.getAssetMap().getIndex(key.getId());
      }

      if (BlockType.getAssetMap().getAsset(blockId) == null) {
         System.out.println("Attempted to register an invalid block ID " + blockId + ": using Empty_Air instead.");
         return this.EMPTY_AIR;
      } else {
         int hash = SolidMaterial.contentHash(blockId, 0, rotation.index(), 0, null);
         SolidMaterial solidMaterial = this.hashToSolidMap.get(hash);
         if (solidMaterial != null) {
            return solidMaterial;
         } else {
            solidMaterial = new SolidMaterial(this, blockId, 0, rotation.index(), 0, null);
            this.hashToSolidMap.put(blockId, solidMaterial);
            return solidMaterial;
         }
      }
   }

   @Nullable
   public SolidMaterial getSolidMaterial(@Nonnull String solidString) {
      return this.getSolidMaterial(solidString, RotationTuple.NONE);
   }

   @Nonnull
   public SolidMaterial getSolidMaterialRotatedY(@Nonnull SolidMaterial solidMaterial, @Nonnull Rotation rotation) {
      PrefabRotation prefabRotation = PrefabRotation.fromRotation(rotation);
      int rotatedRotation = prefabRotation.getRotation(solidMaterial.rotation);
      int rotatedFiller = prefabRotation.getFiller(solidMaterial.filler);
      int hash = SolidMaterial.contentHash(solidMaterial.blockId, solidMaterial.support, rotatedRotation, rotatedFiller, solidMaterial.holder);
      SolidMaterial rotatedSolidMaterial = this.hashToSolidMap.get(hash);
      if (rotatedSolidMaterial != null) {
         return rotatedSolidMaterial;
      } else {
         rotatedSolidMaterial = new SolidMaterial(this, solidMaterial.blockId, solidMaterial.support, rotatedRotation, rotatedFiller, solidMaterial.holder);
         this.hashToSolidMap.put(hash, rotatedSolidMaterial);
         return rotatedSolidMaterial;
      }
   }

   @Nullable
   public SolidMaterial getSolidMaterial(int blockId, int support, int rotation, int filler, @Nullable Holder<ChunkStore> holder) {
      if (BlockType.getAssetMap().getAsset(blockId) == null) {
         System.out.println("Attempted to register an invalid block ID " + blockId + ": using Empty_Air instead.");
         return this.EMPTY_AIR;
      } else {
         int hash = SolidMaterial.contentHash(blockId, support, rotation, filler, holder);
         SolidMaterial solidMaterial = this.hashToSolidMap.get(hash);
         if (solidMaterial != null) {
            return solidMaterial;
         } else {
            solidMaterial = new SolidMaterial(this, blockId, support, rotation, filler, holder);
            this.hashToSolidMap.put(hash, solidMaterial);
            return solidMaterial;
         }
      }
   }
}
