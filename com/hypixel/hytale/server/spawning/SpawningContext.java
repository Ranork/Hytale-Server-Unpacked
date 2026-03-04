package com.hypixel.hytale.server.spawning;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.modules.collision.CollisionModule;
import com.hypixel.hytale.server.core.modules.collision.CollisionResult;
import com.hypixel.hytale.server.core.modules.collision.WorldUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.environment.EnvironmentColumn;
import com.hypixel.hytale.server.npc.util.NPCPhysicsMath;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.spawning.suppression.SuppressionSpanHelper;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SpawningContext {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final BlockTypeAssetMap<String, BlockType> BLOCK_ASSET_MAP = BlockType.getAssetMap();
   @Nullable
   public World world;
   @Nullable
   public WorldChunk worldChunk;
   public int xBlock;
   public int zBlock;
   public double ySpawnHint;
   public int groundLevel;
   public int groundBlockId;
   public int groundRotation;
   @Nullable
   public BlockType groundBlockType;
   public int groundFluidId;
   @Nullable
   public Fluid groundFluid;
   public int ySpanMin;
   public int ySpanMax;
   public int yBlock;
   public int waterLevel;
   public int airHeight;
   public double ySpawnMin;
   public double xSpawn;
   public double zSpawn;
   public double ySpawn;
   private int environmentIndex = Integer.MIN_VALUE;
   private int minSpawnSpanHeight = Integer.MAX_VALUE;
   public double yaw;
   public double pitch;
   public double roll;
   @Nullable
   private ISpawnableWithModel spawnable;
   @Nullable
   private Model spawnModel;
   @Nullable
   private Scope modifierScope;
   private final CollisionResult collisionResult = new CollisionResult();
   private final Vector3d position = new Vector3d();
   private final ExecutionContext executionContext = new ExecutionContext();
   private SpawningContext.SpawnSpan[] spawnSpans = new SpawningContext.SpawnSpan[4];
   private int spawnSpansUsed;
   private int currentSpawnSpanIndex;
   private static final int SOLID_BLOCK = -1;
   private static final int EMPTY_BLOCK = 0;
   private static final int FLUID_BLOCK = 1;

   public SpawningContext() {
      for (int i = 0; i < this.spawnSpans.length; i++) {
         this.spawnSpans[i] = new SpawningContext.SpawnSpan();
      }

      this.spawnSpansUsed = 0;
   }

   public boolean setSpawnable(@Nonnull ISpawnableWithModel spawnable) {
      return this.setSpawnable(spawnable, false);
   }

   public boolean setSpawnable(@Nonnull ISpawnableWithModel spawnable, boolean maxScale) {
      if (spawnable == this.spawnable) {
         return true;
      } else {
         this.spawnable = spawnable;

         String modelName;
         try {
            this.executionContext.setScope(spawnable.createExecutionScope());
            this.modifierScope = this.spawnable.createModifierScope(this.executionContext);
            modelName = spawnable.getSpawnModelName(this.executionContext, this.modifierScope);
         } catch (Throwable var5) {
            LOGGER.at(Level.WARNING).log("Can't set role in spawning context %s: %s", spawnable.getIdentifier(), var5.getMessage());
            spawnable.markNeedsReload();
            this.spawnable = null;
            return false;
         }

         if (!this.setModel(modelName, maxScale)) {
            LOGGER.at(Level.WARNING).log("Can't set model in spawning context %s: %s", spawnable.getIdentifier(), modelName);
            spawnable.markNeedsReload();
            this.spawnable = null;
            return false;
         } else {
            return true;
         }
      }
   }

   private boolean setModel(@Nullable String modelName, boolean maxScale) {
      if (modelName == null) {
         this.clearModel();
         return false;
      } else {
         String currentModelName = this.spawnModel != null ? this.spawnModel.getModelAssetId() : null;
         if (modelName.equals(currentModelName)) {
            return true;
         } else {
            ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(modelName);
            Model model = null;
            if (modelAsset != null) {
               model = maxScale ? Model.createScaledModel(modelAsset, modelAsset.getMaxScale()) : Model.createRandomScaleModel(modelAsset);
            }

            if (model != null && model.getBoundingBox() != null) {
               this.spawnModel = model;
               this.minSpawnSpanHeight = MathUtil.ceil(model.getBoundingBox().height() + 0.2F);
               return true;
            } else {
               this.clearModel();
               return false;
            }
         }
      }
   }

   private void clearModel() {
      this.spawnModel = null;
      this.minSpawnSpanHeight = Integer.MAX_VALUE;
   }

   public void newModel() {
      if (this.spawnModel != null) {
         ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(this.spawnModel.getModelAssetId());
         this.spawnModel = Model.createRandomScaleModel(modelAsset);
      }
   }

   @Nullable
   public Model getModel() {
      return this.spawnModel;
   }

   public void setChunk(@Nonnull WorldChunk worldChunk, int environmentIndex) {
      this.worldChunk = worldChunk;
      this.world = worldChunk.getWorld();
      this.environmentIndex = environmentIndex;
      this.commonInit();
   }

   public boolean setColumn(int x, int z, int yHint, @Nonnull int[] yRange) {
      this.xBlock = x;
      this.zBlock = z;
      this.ySpawnHint = -1.0;
      this.spawnSpansUsed = 0;
      int min = Math.max(0, yHint + yRange[0]);
      int max = Math.min(319, yHint + yRange[1]);
      this.splitRangeToSpawnSpans(min, max);
      return this.spawnSpansUsed > 0;
   }

   public boolean setColumn(int x, int z, int yHint, @Nonnull int[] yRange, @Nonnull SuppressionSpanHelper suppressionHelper) {
      this.xBlock = x;
      this.zBlock = z;
      this.ySpawnHint = -1.0;
      this.spawnSpansUsed = 0;
      int y = Math.max(0, yHint + yRange[0]);
      int hintMax = Math.min(319, yHint + yRange[1]);

      while (y <= hintMax) {
         int min = suppressionHelper.adjustSpawnRangeMin(y);
         if (min >= hintMax) {
            break;
         }

         int max = Math.min(suppressionHelper.adjustSpawnRangeMax(min, hintMax), hintMax);
         y = max + 1;
         this.splitRangeToSpawnSpans(min, max);
      }

      return this.spawnSpansUsed > 0;
   }

   public void setColumn(int x, int z, @Nonnull SuppressionSpanHelper suppressionHelper) {
      this.xBlock = x;
      this.zBlock = z;
      this.ySpawnHint = -1.0;
      this.spawnSpansUsed = 0;
      EnvironmentColumn column = this.worldChunk.getBlockChunk().getEnvironmentColumn(this.xBlock, this.zBlock);

      for (int i = column.indexOf(0); i < column.size(); i++) {
         int envId = column.getValue(i);
         if (envId == this.environmentIndex) {
            int min = Math.max(0, column.getValueMin(i));
            if (min > 320) {
               break;
            }

            int adjustedMin = suppressionHelper.adjustSpawnRangeMin(min);
            int max = column.getValueMax(i);
            if (adjustedMin > max) {
               i = column.indexOf(adjustedMin) - 1;
            } else {
               int adjustedMax = suppressionHelper.adjustSpawnRangeMax(adjustedMin, max);
               this.splitRangeToSpawnSpans(adjustedMin, Math.min(adjustedMax, 319));
            }
         }
      }
   }

   @Nullable
   public Scope getModifierScope() {
      return this.modifierScope;
   }

   public boolean set(@Nonnull World world, double x, double y, double z) {
      if (this.minSpawnSpanHeight >= Integer.MAX_VALUE) {
         throw new IllegalStateException("minSpawnSpanHeight not set - forgot to set model or role?");
      } else {
         this.xBlock = MathUtil.floor(x);
         this.zBlock = MathUtil.floor(z);
         this.ySpawnHint = y;
         this.worldChunk = world.getChunkIfLoaded(ChunkUtil.indexChunkFromBlock(this.xBlock, this.zBlock));
         if (this.worldChunk == null) {
            return false;
         } else {
            this.xBlock = ChunkUtil.localCoordinate(this.xBlock);
            this.zBlock = ChunkUtil.localCoordinate(this.zBlock);
            this.environmentIndex = Integer.MIN_VALUE;
            this.world = world;
            this.commonInit();
            int yInt = MathUtil.floor(y);
            this.spawnSpansUsed = 0;
            EnvironmentColumn environmentColumn = this.worldChunk.getBlockChunk().getEnvironmentColumn(this.xBlock, this.zBlock);
            int rangeMin = Math.max(0, environmentColumn.getMin(yInt));
            int rangeMax = Math.min(environmentColumn.getMax(yInt), 319);
            this.splitRangeToSpawnSpans(rangeMin, rangeMax);
            if (this.spawnSpansUsed == 0) {
               return false;
            } else {
               int distance = Integer.MAX_VALUE;
               int chosenIndex = -1;

               for (int index = 0; index < this.spawnSpansUsed; index++) {
                  SpawningContext.SpawnSpan spawnSpan = this.spawnSpans[index];
                  int currentDistance;
                  if (spawnSpan.top < yInt) {
                     currentDistance = yInt - spawnSpan.top;
                  } else {
                     if (spawnSpan.bottom <= yInt) {
                        chosenIndex = index;
                        break;
                     }

                     currentDistance = spawnSpan.bottom - yInt;
                  }

                  if (currentDistance < distance) {
                     chosenIndex = index;
                     distance = currentDistance;
                  }
               }

               return this.selectSpawnSpan(chosenIndex);
            }
         }
      }
   }

   public void deleteCurrentSpawnSpan() {
      if (--this.spawnSpansUsed > this.currentSpawnSpanIndex) {
         SpawningContext.SpawnSpan temp = this.spawnSpans[this.currentSpawnSpanIndex];
         System.arraycopy(
            this.spawnSpans, this.currentSpawnSpanIndex + 1, this.spawnSpans, this.currentSpawnSpanIndex, this.spawnSpansUsed - this.currentSpawnSpanIndex
         );
         this.spawnSpans[this.spawnSpansUsed] = temp;
      }
   }

   public boolean selectRandomSpawnSpan() {
      return this.spawnSpansUsed > 0 && this.selectSpawnSpan(ThreadLocalRandom.current().nextInt(0, this.spawnSpansUsed));
   }

   private boolean selectSpawnSpan(int index) {
      if (index >= 0 && index < this.spawnSpansUsed) {
         this.currentSpawnSpanIndex = index;
         SpawningContext.SpawnSpan spawnSpan = this.spawnSpans[this.currentSpawnSpanIndex];
         this.ySpanMin = spawnSpan.bottom;
         this.ySpanMax = spawnSpan.top;
         this.waterLevel = spawnSpan.waterLevel;
         this.groundLevel = spawnSpan.groundLevel;
         if (this.waterLevel != -1) {
            this.airHeight = -1;
            if (this.waterLevel < 319) {
               int blockId = this.worldChunk.getBlockChunk().getBlock(this.xBlock, this.waterLevel + 1, this.zBlock);
               int fluidId = this.worldChunk.getFluidId(this.xBlock, this.waterLevel + 1, this.zBlock);
               if (blockId == 0 && fluidId == 0 || BLOCK_ASSET_MAP.getAsset(blockId).getMaterial() == BlockMaterial.Empty && fluidId == 0) {
                  this.airHeight = this.waterLevel + 1;
               }
            }
         } else {
            this.airHeight = this.groundLevel + 1;
         }

         this.yBlock = this.groundLevel;
         this.groundBlockId = this.worldChunk.getBlock(this.xBlock, this.groundLevel, this.zBlock);
         this.groundRotation = this.worldChunk.getRotationIndex(this.xBlock, this.groundLevel, this.zBlock);
         this.groundBlockType = BLOCK_ASSET_MAP.getAsset(this.groundBlockId);
         this.groundFluidId = this.worldChunk.getFluidId(this.xBlock, this.groundLevel, this.zBlock);
         this.groundFluid = Fluid.getAssetMap().getAsset(this.groundFluidId);
         this.ySpawnMin = this.yBlock + NPCPhysicsMath.blockHeight(this.groundBlockType, this.groundRotation);
         this.xSpawn = ChunkUtil.minBlock(this.worldChunk.getX()) + this.xBlock + 0.5;
         this.zSpawn = ChunkUtil.minBlock(this.worldChunk.getZ()) + this.zBlock + 0.5;
         this.ySpawn = this.ySpawnMin - this.spawnModel.getBoundingBox().min.y;
         return true;
      } else {
         return false;
      }
   }

   private void splitRangeToSpawnSpans(int min, int max) {
      int span = 0;
      int waterLevel = -1;
      int groundLevel = -1;

      while (min <= max) {
         int kind = this.isSpawnSpanBlock(this.xBlock, min, this.zBlock);
         if (kind != -1) {
            if (kind == 1) {
               waterLevel = min;
            }

            span++;
         } else {
            if (span > this.minSpawnSpanHeight) {
               this.addSpawnSpan(min, span, groundLevel, waterLevel);
            }

            span = 0;
            waterLevel = -1;
            groundLevel = min;
         }

         min++;
      }

      if (span > this.minSpawnSpanHeight) {
         this.addSpawnSpan(min, span, groundLevel, waterLevel);
      }
   }

   private void addSpawnSpan(int top, int span, int groundLevel, int waterLevel) {
      if (groundLevel == -1) {
         groundLevel = top - span;

         for (int blockType = this.isSpawnSpanBlock(this.xBlock, groundLevel, this.zBlock);
            groundLevel >= 0 && blockType != -1;
            blockType = this.isSpawnSpanBlock(this.xBlock, --groundLevel, this.zBlock)
         ) {
            if (waterLevel == -1 && blockType == 1) {
               waterLevel = groundLevel;
            }
         }
      }

      if (waterLevel == top - 1) {
         while (waterLevel < 319 && this.isSpawnSpanBlock(this.xBlock, waterLevel + 1, this.zBlock) == 1) {
            waterLevel++;
         }
      }

      if (this.spawnSpans.length <= this.spawnSpansUsed) {
         SpawningContext.SpawnSpan[] newSpans = new SpawningContext.SpawnSpan[this.spawnSpansUsed + 4];
         System.arraycopy(this.spawnSpans, 0, newSpans, 0, this.spawnSpansUsed);

         for (int i = this.spawnSpansUsed; i < newSpans.length; i++) {
            newSpans[i] = new SpawningContext.SpawnSpan();
         }

         this.spawnSpans = newSpans;
      }

      SpawningContext.SpawnSpan spawnSpan = this.spawnSpans[this.spawnSpansUsed++];
      spawnSpan.bottom = top - span;
      spawnSpan.top = top - 1;
      spawnSpan.waterLevel = waterLevel;
      spawnSpan.groundLevel = groundLevel;
   }

   private int isSpawnSpanBlock(int x, int y, int z) {
      int block = this.worldChunk.getBlock(x, y, z);
      if (block != 0 && BLOCK_ASSET_MAP.getAsset(block).getMaterial() != BlockMaterial.Empty) {
         return -1;
      } else {
         return this.worldChunk.getFluidId(x, y, z) == 0 ? 0 : 1;
      }
   }

   private void commonInit() {
      this.yaw = RandomExtra.randomRange(0.0F, (float) (Math.PI * 2));
      this.pitch = 0.0;
      this.roll = 0.0;
   }

   @Nonnull
   public SpawnTestResult canSpawn(boolean testOverlapBlocks, boolean testOverlapEntities) {
      SpawnTestResult spawnTestResult = SpawnTestResult.TEST_OK;
      if (testOverlapBlocks) {
         spawnTestResult = this.intersectsBlock();
         if (spawnTestResult != SpawnTestResult.TEST_OK) {
            return spawnTestResult;
         }
      }

      if (testOverlapEntities) {
         spawnTestResult = this.intersectsEntity();
      }

      return spawnTestResult;
   }

   @Nonnull
   public SpawnTestResult canSpawn() {
      return this.canSpawn(true, true);
   }

   @Nonnull
   private SpawnTestResult intersectsEntity() {
      return SpawnTestResult.TEST_OK;
   }

   @Nonnull
   private SpawnTestResult intersectsBlock() {
      if (this.worldChunk != null && this.spawnModel != null && this.spawnable != null) {
         return this.spawnable.canSpawn(this);
      } else {
         throw new IllegalStateException("SpawningContext initialized");
      }
   }

   public static boolean isWaterBlock(int fluidId) {
      return fluidId != 0;
   }

   public int getWaterLevel() {
      return this.waterLevel;
   }

   public int getAirHeight() {
      return this.airHeight;
   }

   public boolean isInsideSpan(double y) {
      return y >= this.ySpanMin && y <= this.ySpanMax;
   }

   public boolean isInWater(float minDepth) {
      int depth = this.waterLevel - this.groundLevel - 1;
      if (depth < 0) {
         return false;
      } else {
         int roundedDepth = MathUtil.fastCeil(minDepth);
         if (depth < roundedDepth) {
            return false;
         } else {
            double ySpawn = this.waterLevel - roundedDepth;
            if (!this.isInsideSpan(ySpawn)) {
               return false;
            } else {
               this.ySpawn = ySpawn - this.spawnModel.getBoundingBox().min.y;
               return true;
            }
         }
      }
   }

   public boolean isOnSolidGround() {
      if (isWaterBlock(this.groundFluidId)) {
         return false;
      } else {
         this.ySpawn = this.ySpawnMin - this.spawnModel.getBoundingBox().min.y;
         int ySpawnBlock = MathUtil.floor(this.ySpawnMin);
         if (ySpawnBlock != this.yBlock) {
            BlockType blockType = this.worldChunk.getBlockType(this.xBlock, ySpawnBlock, this.zBlock);
            int fluidId = this.worldChunk.getFluidId(this.xBlock, ySpawnBlock, this.zBlock);
            int rotation = this.worldChunk.getRotationIndex(this.xBlock, ySpawnBlock, this.zBlock);
            if (WorldUtil.isEmptyOnlyBlock(blockType, fluidId) || fluidId != 0) {
               return this.isInsideSpan(this.ySpawn);
            } else if (WorldUtil.isSolidOnlyBlock(blockType, fluidId)) {
               this.ySpawn = ySpawnBlock + NPCPhysicsMath.blockHeight(blockType, rotation) - this.spawnModel.getBoundingBox().min.y;
               return this.isInsideSpan(this.ySpawn);
            } else {
               return false;
            }
         } else {
            return this.ySpawn >= this.ySpanMin - 1 && this.ySpawn <= this.ySpanMax;
         }
      }
   }

   public boolean isInAir(double height) {
      this.ySpawn = this.getAirHeight() + height - this.spawnModel.getBoundingBox().min.y;
      return this.ySpawn < 320.0 && this.isInsideSpan(this.ySpawn);
   }

   public boolean validatePosition(int invalidMaterials) {
      if (this.spawnModel == null) {
         return false;
      } else {
         this.position.assign(this.xSpawn, this.ySpawn, this.zSpawn);
         return CollisionModule.get()
               .validatePosition(
                  this.world,
                  this.spawnModel.getBoundingBox(),
                  this.position,
                  invalidMaterials,
                  null,
                  (_this, collisionCode, collision, collisionConfig) -> collisionConfig.blockId != -1,
                  this.collisionResult
               )
            != -1;
      }
   }

   public boolean canBreathe(boolean breathesInAir, boolean breathesInWater) {
      if (this.spawnModel == null) {
         return false;
      } else {
         return !breathesInAir && this.ySpawn + this.spawnModel.getEyeHeight() >= this.airHeight
            ? false
            : breathesInWater || !(this.waterLevel + 1 - this.ySpawn >= this.spawnModel.getEyeHeight());
      }
   }

   public void release() {
      this.groundBlockType = null;
      this.world = null;
      this.worldChunk = null;
   }

   public void releaseFull() {
      this.release();
      this.spawnable = null;
      this.modifierScope = null;
      this.spawnModel = null;
      this.executionContext.setScope(null);
   }

   @Nonnull
   public ExecutionContext getExecutionContext() {
      return this.executionContext;
   }

   @Nonnull
   public Vector3d newPosition() {
      return new Vector3d(this.xSpawn, this.ySpawn, this.zSpawn);
   }

   @Nonnull
   public Vector3f newRotation() {
      return new Vector3f((float)this.pitch, (float)this.yaw, (float)this.roll);
   }

   @Nonnull
   @Override
   public String toString() {
      return "SpawningContext{xBlock="
         + this.xBlock
         + ", zBlock="
         + this.zBlock
         + ", yBlock="
         + this.yBlock
         + ", ySpawnMin="
         + this.ySpawnMin
         + ", xSpawn="
         + this.xSpawn
         + ", zSpawn="
         + this.zSpawn
         + ", ySpawn="
         + this.ySpawn
         + ", yaw="
         + this.yaw
         + ", pitch="
         + this.pitch
         + ", roll="
         + this.roll
         + ", groundBlockId="
         + this.groundBlockId
         + "}";
   }

   private static class SpawnSpan {
      int bottom;
      int top;
      int waterLevel;
      int groundLevel;
   }
}
