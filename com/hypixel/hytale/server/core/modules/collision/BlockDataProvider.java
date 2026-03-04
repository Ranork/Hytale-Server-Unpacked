package com.hypixel.hytale.server.core.modules.collision;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockDataProvider extends BlockData {
   protected static int FULL_LEVEL = 8;
   protected final int INVALID_CHUNK_SECTION_INDEX = Integer.MIN_VALUE;
   @Nullable
   protected World world;
   @Nullable
   protected WorldChunk chunk;
   protected int chunkSectionIndex;
   @Nullable
   protected BlockSection chunkSection;
   protected int chunkX;
   protected int chunkY;
   protected int chunkZ;
   @Nullable
   protected Ref<ChunkStore> chunkSectionRef;

   public void initialize(World world) {
      this.world = world;
      this.blockId = Integer.MIN_VALUE;
      this.cleanup0();
   }

   public void cleanup() {
      this.world = null;
      this.cleanup0();
   }

   public void read(int x, int y, int z) {
      int newBlockId = this.readBlockId(x, y, z);
      int fluidId = this.readFluidId(x, y, z);
      if (this.blockId != newBlockId || this.fluidId != fluidId) {
         if (newBlockId == 0 && fluidId == 0) {
            this.setBlock(0, BlockType.EMPTY, 0, 1);
            this.fluidId = 0;
            this.fluid = Fluid.EMPTY;
            this.fluidKey = "Empty";
            this.fillHeight = 0.0;
         } else if (newBlockId == 1) {
            this.setBlock(1, BlockType.UNKNOWN, 0, 4);
            this.fluidId = 0;
            this.fluid = Fluid.EMPTY;
            this.fluidKey = "Empty";
            this.fillHeight = 0.0;
         } else {
            this.blockId = newBlockId;
            this.blockType = BlockType.getAssetMap().getAsset(newBlockId);
            if (this.blockType.isUnknown()) {
               this.setBlock(newBlockId, this.blockType, 0, 4);
               this.fluidId = 0;
               this.fluid = Fluid.EMPTY;
               this.fluidKey = "Empty";
               this.fillHeight = 0.0;
            } else {
               Fluid fluid = Fluid.getAssetMap().getAsset(fluidId);
               byte fluidLevel = this.readFluidLevel(x, y, z);
               this.blockTypeKey = this.blockType.getId();
               this.filler = this.readFiller(x, y, z);
               this.rotation = this.readRotation(x, y, z);
               if (this.blockType.getMaterial() == BlockMaterial.Solid) {
                  this.collisionMaterials = 4;
                  if (this.blockType.getHitboxTypeIndex() != 0) {
                     this.collisionMaterials = this.collisionMaterials + materialFromFillLevel(fluid, fluidLevel);
                  }
               } else {
                  this.collisionMaterials = materialFromFillLevel(fluid, fluidLevel);
               }

               this.fluidId = fluidId;
               this.fluid = fluid;
               this.fluidKey = fluid.getId();
               this.fillHeight = fluidId != 0 ? (double)fluidLevel / fluid.getMaxFluidLevel() : 0.0;
               this.blockBoundingBoxes = null;
            }
         }
      }
   }

   protected int readBlockId(int x, int y, int z) {
      int chunkX = ChunkUtil.chunkCoordinate(x);
      int chunkZ = ChunkUtil.chunkCoordinate(z);
      if (this.chunk == null || this.chunk.getX() != chunkX || chunkZ != this.chunk.getZ()) {
         this.chunk = this.world.getChunkIfInMemory(ChunkUtil.indexChunk(chunkX, chunkZ));
         this.chunkSectionIndex = Integer.MIN_VALUE;
         this.chunkSection = null;
      }

      if (this.chunk == null) {
         return 1;
      } else {
         int sectionIndex = ChunkUtil.indexSection(y);
         if (this.chunkSection == null || this.chunkSectionIndex != sectionIndex) {
            this.chunkSectionIndex = sectionIndex;
            this.chunkSection = sectionIndex >= 0 && this.chunkSectionIndex < 10 ? this.chunk.getBlockChunk().getSectionAtIndex(sectionIndex) : null;
         }

         return this.chunkSection == null ? 0 : this.chunkSection.get(x, y, z);
      }
   }

   protected int readRotation(int x, int y, int z) {
      int chunkX = ChunkUtil.chunkCoordinate(x);
      int chunkZ = ChunkUtil.chunkCoordinate(z);
      if (this.chunk == null || this.chunk.getX() != chunkX || chunkZ != this.chunk.getZ()) {
         this.chunk = this.world.getChunkIfInMemory(ChunkUtil.indexChunk(chunkX, chunkZ));
         this.chunkSectionIndex = Integer.MIN_VALUE;
         this.chunkSection = null;
      }

      if (this.chunk == null) {
         return 0;
      } else {
         int sectionIndex = ChunkUtil.indexSection(y);
         if (this.chunkSection == null || this.chunkSectionIndex != sectionIndex) {
            this.chunkSectionIndex = sectionIndex;
            this.chunkSection = sectionIndex >= 0 && this.chunkSectionIndex < 10 ? this.chunk.getBlockChunk().getSectionAtIndex(sectionIndex) : null;
         }

         return this.chunkSection == null ? 0 : this.chunkSection.getRotationIndex(x, y, z);
      }
   }

   protected int readFiller(int x, int y, int z) {
      int chunkX = ChunkUtil.chunkCoordinate(x);
      int chunkZ = ChunkUtil.chunkCoordinate(z);
      if (this.chunk == null || this.chunk.getX() != chunkX || chunkZ != this.chunk.getZ()) {
         this.chunk = this.world.getChunkIfInMemory(ChunkUtil.indexChunk(chunkX, chunkZ));
         this.chunkSectionIndex = Integer.MIN_VALUE;
         this.chunkSection = null;
      }

      if (this.chunk == null) {
         return 0;
      } else {
         int sectionIndex = ChunkUtil.indexSection(y);
         if (this.chunkSection == null || this.chunkSectionIndex != sectionIndex) {
            this.chunkSectionIndex = sectionIndex;
            this.chunkSection = sectionIndex >= 0 && this.chunkSectionIndex < 10 ? this.chunk.getBlockChunk().getSectionAtIndex(sectionIndex) : null;
         }

         return this.chunkSection == null ? 0 : this.chunkSection.getFiller(x, y, z);
      }
   }

   protected int readFluidId(int x, int y, int z) {
      int chunkX = ChunkUtil.chunkCoordinate(x);
      int chunkY = ChunkUtil.chunkCoordinate(y);
      int chunkZ = ChunkUtil.chunkCoordinate(z);
      if (this.chunkSectionRef == null || !this.chunkSectionRef.isValid() || this.chunkX != chunkX || this.chunkY != chunkY || this.chunkZ != chunkZ) {
         this.chunkSectionRef = this.world.getChunkStore().getChunkSectionReference(chunkX, chunkY, chunkZ);
         this.chunkX = chunkX;
         this.chunkY = chunkY;
         this.chunkZ = chunkZ;
      }

      if (this.chunkSectionRef == null) {
         return 1;
      } else {
         FluidSection fluidSection = this.world.getChunkStore().getStore().getComponent(this.chunkSectionRef, FluidSection.getComponentType());
         return fluidSection == null ? 1 : fluidSection.getFluidId(x, y, z);
      }
   }

   protected byte readFluidLevel(int x, int y, int z) {
      int chunkX = ChunkUtil.chunkCoordinate(x);
      int chunkY = ChunkUtil.chunkCoordinate(y);
      int chunkZ = ChunkUtil.chunkCoordinate(z);
      if (this.chunkSectionRef == null || !this.chunkSectionRef.isValid() || this.chunkX != chunkX || this.chunkY != chunkY || this.chunkZ != chunkZ) {
         this.chunkSectionRef = this.world.getChunkStore().getChunkSectionReference(chunkX, chunkY, chunkZ);
         this.chunkX = chunkX;
         this.chunkY = chunkY;
         this.chunkZ = chunkZ;
      }

      if (this.chunkSectionRef == null) {
         return 0;
      } else {
         FluidSection fluidSection = this.world.getChunkStore().getStore().getComponent(this.chunkSectionRef, FluidSection.getComponentType());
         return fluidSection == null ? 0 : fluidSection.getFluidLevel(x, y, z);
      }
   }

   protected void setBlock(int id, @Nonnull BlockType type, int rotation, int material, BlockBoundingBoxes box) {
      this.blockId = id;
      this.rotation = rotation;
      this.blockType = type;
      this.blockTypeKey = type.getId();
      this.collisionMaterials = material;
      this.blockBoundingBoxes = box;
      this.fillHeight = 0.0;
   }

   protected void setBlock(int id, @Nonnull BlockType type, int rotation, int material) {
      this.setBlock(id, type, rotation, material, BlockBoundingBoxes.UNIT_BOX);
   }

   protected void cleanup0() {
      this.chunk = null;
      this.chunkSectionIndex = Integer.MIN_VALUE;
      this.chunkSection = null;
      this.blockType = null;
      this.blockTypeKey = null;
      this.blockBoundingBoxes = null;
      this.fluid = null;
      this.fluidKey = null;
   }

   protected static int materialFromFillLevel(@Nonnull Fluid fluid, byte level) {
      if (level == 0) {
         return 1;
      } else {
         return level == fluid.getMaxFluidLevel() ? 2 : 3;
      }
   }
}
