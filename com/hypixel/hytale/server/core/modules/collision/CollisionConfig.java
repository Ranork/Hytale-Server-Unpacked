package com.hypixel.hytale.server.core.modules.collision;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CollisionConfig {
   public static final int MATERIAL_EMPTY = 1;
   public static final int MATERIAL_FLUID = 2;
   public static final int MATERIAL_SOLID = 4;
   public static final int MATERIAL_SUBMERGED = 8;
   public static final int MATERIAL_DAMAGE = 16;
   public static final int MATERIAL_SET_NONE = 0;
   public static final int MATERIAL_SET_ANY = 15;
   private static final int INVALID_CHUNK_SECTION_INDEX = Integer.MIN_VALUE;
   public int blockId;
   @Nullable
   public BlockType blockType;
   @Nullable
   public BlockMaterial blockMaterial;
   public int rotation;
   public int blockX;
   public int blockY;
   public int blockZ;
   private int boundingBoxOffsetX;
   private int boundingBoxOffsetY;
   private int boundingBoxOffsetZ;
   private BlockBoundingBoxes.RotatedVariantBoxes boundingBoxes;
   @Nullable
   private WorldChunk chunk;
   private int chunkSectionIndex;
   @Nullable
   private BlockSection chunkSection;
   private int chunkX;
   private int chunkY;
   private int chunkZ;
   @Nullable
   private Ref<ChunkStore> chunkSectionRef;
   @Nullable
   public Fluid fluid;
   public int fluidId;
   public byte fluidLevel;
   @Nonnull
   private Box blockBox = new Box();
   private World world;
   private int blockMaterialCollisionMask;
   public int blockMaterialMask;
   public boolean blockCanCollide;
   public boolean blockCanTrigger;
   public boolean blockCanTriggerPartial;
   public boolean checkTriggerBlocks = true;
   public boolean checkDamageBlocks = true;
   public Predicate<CollisionConfig> canCollide;
   public boolean dumpInvalidBlocks;
   @Nullable
   public Object extraData1;
   @Nullable
   public Object extraData2;

   public int getDetailCount() {
      return this.boundingBoxes.getDetailBoxes().length;
   }

   @Nonnull
   public Box getBoundingBox() {
      this.blockBox.assign(this.boundingBoxes.getBoundingBox());
      if (this.blockId == 0 && this.fluidId != 0 && this.fluid != null) {
         this.blockBox.max.y -= 0.03125;
         this.blockBox.max.y = this.blockBox.max.y * ((double)this.fluidLevel / this.fluid.getMaxFluidLevel());
      }

      return this.blockBox;
   }

   @Nonnull
   public Box getBoundingBox(int i) {
      this.blockBox.assign(this.boundingBoxes.getDetailBoxes()[i]);
      return this.blockBox;
   }

   public int getBoundingBoxOffsetX() {
      return this.boundingBoxOffsetX;
   }

   public int getBoundingBoxOffsetY() {
      return this.boundingBoxOffsetY;
   }

   public int getBoundingBoxOffsetZ() {
      return this.boundingBoxOffsetZ;
   }

   public void setCollisionByMaterial(int collidingMaterials) {
      this.blockMaterialCollisionMask = this.blockMaterialCollisionMask & -16 | collidingMaterials & 15;
   }

   public int getCollisionByMaterial() {
      return this.blockMaterialCollisionMask & 15;
   }

   public boolean isCollidingWithDamageBlocks() {
      return (this.blockMaterialCollisionMask & 16) != 0;
   }

   public boolean setCollideWithDamageBlocks(boolean damageColliding) {
      boolean oldState = this.isCollidingWithDamageBlocks();
      if (damageColliding) {
         this.blockMaterialCollisionMask |= 16;
      } else {
         this.blockMaterialCollisionMask &= -17;
      }

      return oldState;
   }

   public Predicate<CollisionConfig> getBlockCollisionPredicate() {
      return this.canCollide;
   }

   public void setDefaultCollisionBehaviour() {
      this.setCollisionByMaterial(4);
      this.setCollideWithDamageBlocks(true);
      this.setDefaultBlockCollisionPredicate();
   }

   public void setDefaultBlockCollisionPredicate() {
      this.canCollide = collisionConfig -> (collisionConfig.blockMaterialMask & collisionConfig.blockMaterialCollisionMask) != 0;
   }

   public boolean isCheckTriggerBlocks() {
      return this.checkTriggerBlocks;
   }

   public void setCheckTriggerBlocks(boolean checkTriggerBlocks) {
      this.checkTriggerBlocks = checkTriggerBlocks;
   }

   public boolean isCheckDamageBlocks() {
      return this.checkDamageBlocks;
   }

   public void setCheckDamageBlocks(boolean checkDamageBlocks) {
      this.checkDamageBlocks = checkDamageBlocks;
   }

   public void setWorld(World world) {
      if (this.world != world) {
         this.chunk = null;
         this.chunkSectionRef = null;
         this.chunkSection = null;
         this.chunkSectionIndex = Integer.MIN_VALUE;
      }

      this.world = world;
      this.blockId = Integer.MIN_VALUE;
      this.blockX = Integer.MIN_VALUE;
      this.blockY = Integer.MIN_VALUE;
      this.blockZ = Integer.MIN_VALUE;
   }

   public boolean canCollide(int x, int y, int z) {
      this.blockX = x;
      this.blockY = y;
      this.blockZ = z;
      int chunkX = ChunkUtil.chunkCoordinate(x);
      int chunkY = ChunkUtil.chunkCoordinate(y);
      int chunkZ = ChunkUtil.chunkCoordinate(z);
      if (this.chunk == null || this.chunk.getX() != chunkX || chunkZ != this.chunk.getZ()) {
         this.chunk = this.world.getChunkIfInMemory(ChunkUtil.indexChunk(chunkX, chunkZ));
         this.chunkSectionIndex = Integer.MIN_VALUE;
         this.chunkSection = null;
      }

      if (this.chunkSectionRef == null || !this.chunkSectionRef.isValid() || this.chunkX != chunkX || this.chunkY != chunkY || this.chunkZ != chunkZ) {
         this.chunkSectionRef = this.world.getChunkStore().getChunkSectionReference(chunkX, chunkY, chunkZ);
         this.chunkX = chunkX;
         this.chunkY = chunkY;
         this.chunkZ = chunkZ;
      }

      this.blockCanTrigger = false;
      if (this.chunk != null && this.chunkSectionRef != null) {
         int sectionIndex = ChunkUtil.indexSection(y);
         if (this.chunkSection == null || this.chunkSectionIndex != sectionIndex) {
            this.chunkSectionIndex = sectionIndex;
            if (sectionIndex >= 0 && this.chunkSectionIndex < 10) {
               this.chunkSection = this.chunk.getBlockChunk().getSectionAtIndex(sectionIndex);
            } else {
               this.chunkSection = null;
            }
         }

         if (this.chunkSection == null) {
            this.blockType = BlockType.EMPTY;
            this.blockMaterial = BlockMaterial.Empty;
            this.fluid = null;
            this.fluidId = Integer.MIN_VALUE;
            this.boundingBoxes = BlockBoundingBoxes.UNIT_BOX.get(Rotation.None, Rotation.None, Rotation.None);
            this.blockMaterialMask = 1;
            this.blockCanCollide = (this.blockMaterialCollisionMask & this.blockMaterialMask) != 0;
            this.blockId = 0;
            return this.blockCanCollide;
         } else {
            int newBlockId = this.chunkSection.get(x, y, z);
            BlockType newBlockType = BlockType.getAssetMap().getAsset(newBlockId);
            FluidSection fluidSection = this.chunkSectionRef.getStore().getComponent(this.chunkSectionRef, FluidSection.getComponentType());
            byte newFluidLevel = 0;
            int newFluidId;
            Fluid newFluid;
            if (fluidSection != null) {
               newFluidId = fluidSection.getFluidId(this.blockX, this.blockY, this.blockZ);
               newFluid = Fluid.getAssetMap().getAsset(newFluidId);
               newFluidLevel = fluidSection.getFluidLevel(this.blockX, this.blockY, this.blockZ);
            } else {
               newFluidId = Integer.MIN_VALUE;
               newFluid = null;
            }

            int filler = this.chunkSection.getFiller(x, y, z);
            if (!newBlockType.isUnknown() && filler != 0) {
               this.boundingBoxOffsetX = -FillerBlockUtil.unpackX(filler);
               this.boundingBoxOffsetY = -FillerBlockUtil.unpackY(filler);
               this.boundingBoxOffsetZ = -FillerBlockUtil.unpackZ(filler);
            } else {
               this.boundingBoxOffsetX = 0;
               this.boundingBoxOffsetY = 0;
               this.boundingBoxOffsetZ = 0;
            }

            int newRotation = this.chunkSection.getRotationIndex(x, y, z);
            if (newBlockId == this.blockId && this.rotation == newRotation && this.fluidId == newFluidId && this.fluidLevel == newFluidLevel) {
               this.blockCanTrigger = this.blockCanTriggerPartial || this.checkTriggerBlocks && (newBlockType.isTrigger() || newFluid.isTrigger());
               return this.blockCanCollide || this.blockCanTrigger;
            } else {
               this.blockId = newBlockId;
               this.blockType = newBlockType;
               this.rotation = newRotation;
               this.fluidId = newFluidId;
               this.fluid = newFluid;
               this.fluidLevel = newFluidLevel;
               boolean blockWillDamage = this.checkDamageBlocks && (this.blockType.getDamageToEntities() > 0 || this.fluid.getDamageToEntities() > 0);
               this.blockCanTrigger = blockWillDamage || this.checkTriggerBlocks && (this.blockType.isTrigger() || newFluid.isTrigger());
               this.blockMaterialMask = blockWillDamage ? 16 : 0;
               if ((this.blockId == 0 || this.blockType == BlockType.EMPTY) && this.fluidId == 0) {
                  this.blockMaterial = BlockMaterial.Empty;
                  this.boundingBoxes = BlockBoundingBoxes.UNIT_BOX.get(Rotation.None, Rotation.None, Rotation.None);
                  this.blockMaterialMask |= 1;
                  this.blockCanCollide = (this.blockMaterialMask & this.blockMaterialCollisionMask) != 0;
                  return this.blockCanCollide || this.blockCanTrigger;
               } else {
                  this.blockMaterial = this.blockType.getMaterial();
                  this.boundingBoxes = BlockBoundingBoxes.getAssetMap().getAsset(newBlockType.getHitboxTypeIndex()).get(this.rotation);
                  if (this.blockMaterial == BlockMaterial.Empty) {
                     this.blockMaterialMask = this.blockMaterialMask | (this.fluidId != 0 ? 2 : 1);
                     this.blockCanCollide = (this.blockMaterialMask & this.blockMaterialCollisionMask) != 0;
                     return this.blockCanCollide || this.blockCanTrigger;
                  } else {
                     if (this.boundingBoxes == null) {
                        this.boundingBoxes = BlockBoundingBoxes.UNIT_BOX.get(Rotation.None, Rotation.None, Rotation.None);
                     }

                     this.blockMaterialMask = this.blockMaterialMask | (this.fluidId == 0 ? 4 : 14);
                     this.blockCanCollide = this.canCollide.test(this);
                     return this.blockCanCollide || this.blockCanTrigger;
                  }
               }
            }
         }
      } else {
         this.blockType = null;
         this.blockMaterial = null;
         this.fluid = null;
         this.fluidId = Integer.MIN_VALUE;
         this.boundingBoxes = BlockBoundingBoxes.UNIT_BOX.get(Rotation.None, Rotation.None, Rotation.None);
         this.blockMaterialMask = 0;
         this.blockCanCollide = true;
         this.blockId = Integer.MIN_VALUE;
         return true;
      }
   }

   public void clear() {
      this.chunk = null;
      this.chunkSectionIndex = Integer.MIN_VALUE;
      this.chunkSection = null;
      this.setWorld(null);
      this.dumpInvalidBlocks = false;
      this.extraData1 = null;
      this.extraData2 = null;
   }
}
