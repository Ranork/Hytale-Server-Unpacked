package com.hypixel.hytale.server.core.modules.collision;

import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockData {
   protected int blockId;
   @Nullable
   protected BlockType blockType;
   @Nullable
   protected String blockTypeKey;
   protected int rotation;
   protected int filler;
   protected int fluidId;
   @Nullable
   protected Fluid fluid;
   @Nullable
   protected String fluidKey;
   protected double fillHeight;
   protected int collisionMaterials;
   @Nullable
   protected BlockBoundingBoxes blockBoundingBoxes;

   public void assign(@Nonnull BlockData other) {
      this.blockId = other.blockId;
      this.blockType = other.blockType;
      this.blockTypeKey = other.blockTypeKey;
      this.rotation = other.rotation;
      this.filler = other.filler;
      this.fluidId = other.fluidId;
      this.fluid = other.fluid;
      this.fluidKey = other.fluidKey;
      this.fillHeight = other.fillHeight;
      this.collisionMaterials = other.collisionMaterials;
      this.blockBoundingBoxes = other.blockBoundingBoxes;
   }

   public void clear() {
      this.blockId = Integer.MIN_VALUE;
      this.blockType = null;
      this.blockTypeKey = null;
      this.rotation = 0;
      this.filler = 0;
      this.fluidId = Integer.MIN_VALUE;
      this.fluid = null;
      this.fluidKey = null;
      this.blockBoundingBoxes = null;
   }

   public boolean isFiller() {
      return this.filler != 0;
   }

   public int originX(int x) {
      return x - FillerBlockUtil.unpackX(this.filler);
   }

   public int originY(int y) {
      return y - FillerBlockUtil.unpackY(this.filler);
   }

   public int originZ(int z) {
      return z - FillerBlockUtil.unpackZ(this.filler);
   }

   public double getFillHeight() {
      return this.fillHeight;
   }

   public boolean isTrigger() {
      return this.blockType.isTrigger();
   }

   public int getBlockDamage() {
      return Math.max(this.blockType.getDamageToEntities(), this.fluid.getDamageToEntities());
   }

   public int getSubmergeDamage() {
      Fluid fluid = this.getFluid();
      return fluid == null ? 0 : fluid.getDamageToEntities();
   }

   public int getCollisionMaterials() {
      return this.collisionMaterials;
   }

   @Nullable
   public BlockBoundingBoxes getBlockBoundingBoxes() {
      if (this.blockBoundingBoxes == null) {
         this.blockBoundingBoxes = BlockBoundingBoxes.getAssetMap().getAsset(this.blockType.getHitboxTypeIndex());
      }

      return this.blockBoundingBoxes;
   }

   @Nullable
   public BlockType getBlockType() {
      return this.blockType;
   }

   public int getFluidId() {
      return this.fluidId;
   }

   @Nullable
   public Fluid getFluid() {
      return this.fluid;
   }
}
