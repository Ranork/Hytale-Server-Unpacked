package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigEditStore;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.builtin.buildertools.tooloperations.SculptOperation;
import com.hypixel.hytale.builtin.buildertools.utils.Material;
import com.hypixel.hytale.builtin.buildertools.utils.Sample;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.DrawType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import javax.annotation.Nonnull;
import org.joml.Vector3i;

public class KernelErosionOperation extends SequenceBrushOperation {
   public static final BuilderCodec<KernelErosionOperation> CODEC = BuilderCodec.builder(KernelErosionOperation.class, KernelErosionOperation::new)
      .append(new KeyedCodec<>("Kernel", new EnumCodec<>(KernelErosionOperation.SmoothKernel.class)), (op, val) -> op.kernelArg = val, op -> op.kernelArg)
      .documentation("The smoothing kernel shape to use")
      .add()
      .<KernelErosionOperation.SmoothMode>append(
         new KeyedCodec<>("Mode", new EnumCodec<>(KernelErosionOperation.SmoothMode.class)), (op, val) -> op.modeArg = val, op -> op.modeArg
      )
      .documentation("Smoothing mode: Full (3D), Heightmap (2D heightmap), or Flat (XZ-plane only)")
      .add()
      .<Integer>append(new KeyedCodec<>("ErosionStrength", Codec.INTEGER), (op, val) -> op.erosionStrength = val, op -> op.erosionStrength)
      .documentation("Erosion strength from -10 to 10 (divided by 10 internally). Positive erodes, negative fills.")
      .add()
      .<Boolean>append(new KeyedCodec<>("SampleNearby", Codec.BOOLEAN), (op, val) -> op.sampleNearby = val, op -> op.sampleNearby)
      .documentation("When true, samples neighbor block types for fill material. When false, uses the brush pattern.")
      .add()
      .documentation("Smooths terrain using weighted kernels with erosion thresholds")
      .build();
   @Nonnull
   public KernelErosionOperation.SmoothKernel kernelArg = KernelErosionOperation.SmoothKernel.Normal;
   @Nonnull
   public KernelErosionOperation.SmoothMode modeArg = KernelErosionOperation.SmoothMode.Full;
   @Nonnull
   public Integer erosionStrength = 0;
   @Nonnull
   public Boolean sampleNearby = false;
   private static final BlockTypeAssetMap<String, BlockType> ASSET_MAP = BlockType.getAssetMap();
   private static final int BUFFER_MARGIN = 2;
   private static final int MIN_EROSION_STRENGTH = -10;
   private static final int MAX_EROSION_STRENGTH = 10;
   private int[][][] buffer;
   private int[][] heightMapBuffer;
   private int originX;
   private int originY;
   private int originZ;
   private int bufferWidth;
   private int bufferHeight;
   private int halfBufferWidth;
   private int halfBufferHeight;
   private int[] kernelWeights;
   private int kernelTotal;
   private double erosionPreCalc;
   private double erosionThresholdLow;
   private double erosionThresholdHigh;
   private boolean isHeightmap;

   public KernelErosionOperation() {
      super("Kernel Smooth", "Smooths terrain using weighted kernels with erosion thresholds", true);
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.isHeightmap = this.modeArg == KernelErosionOperation.SmoothMode.Heightmap;

      SculptOperation.Kernel kernel = switch (this.modeArg) {
         case Full -> this.kernelArg.full;
         case Heightmap -> this.kernelArg.heightmap;
         case Flat -> this.kernelArg.flat;
      };
      this.kernelWeights = kernel.weights();
      this.kernelTotal = kernel.total();
      int clampedErosionStrength = Math.max(-10, Math.min(10, this.erosionStrength));
      double strength = clampedErosionStrength / 10.0;
      this.erosionPreCalc = -kernel.range() * strength;
      this.erosionThresholdLow = kernel.stable() + this.erosionPreCalc;
      this.erosionThresholdHigh = kernel.stable() + 1.0 + this.erosionPreCalc;
      Vector3i origin = brushConfig.getOrigin();
      this.originX = origin.x;
      this.originY = origin.y;
      this.originZ = origin.z;
      this.bufferWidth = brushConfig.getShapeWidth() + 2;
      this.bufferHeight = brushConfig.getShapeHeight() + 2;
      if (this.bufferWidth % 2 == 0) {
         this.bufferWidth++;
      }

      if (this.bufferHeight % 2 == 0) {
         this.bufferHeight++;
      }

      if (this.isHeightmap) {
         this.bufferWidth += 4;
         this.bufferHeight += 20;
      }

      this.halfBufferWidth = this.bufferWidth / 2;
      this.halfBufferHeight = this.bufferHeight / 2;
      BrushConfigEditStore edit = brushConfigCommandExecutor.getEdit();
      this.buffer = buildBuffer(edit, this.originX, this.originY, this.originZ, this.bufferWidth, this.bufferHeight);
      if (this.isHeightmap) {
         this.heightMapBuffer = SculptOperation.preCalcBuffer(this.originY, this.buffer);
      }
   }

   @Override
   public boolean modifyBlocks(
      Ref<EntityStore> ref,
      BrushConfig brushConfig,
      BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull BrushConfigEditStore edit,
      int x,
      int y,
      int z,
      ComponentAccessor<EntityStore> componentAccessor
   ) {
      int bx = x - this.originX + this.halfBufferWidth;
      int by = y - this.originY + this.halfBufferHeight;
      int bz = z - this.originZ + this.halfBufferWidth;
      int margin = this.isHeightmap ? 2 : 1;
      if (bx >= margin && by >= margin && bz >= margin && bx < this.bufferWidth - margin && by < this.bufferHeight - margin && bz < this.bufferWidth - margin) {
         if (this.buffer[bx][by][bz] == -1) {
            edit.setBlock(x, y, z, 0);
         }

         if (this.isHeightmap) {
            if (y != this.originY) {
               return true;
            }

            Sample.Result2D result = Sample.calcDeltaHeights(
               bx, by, bz, this.heightMapBuffer, this.buffer, this.kernelWeights, this.kernelTotal, this.originY, this.sampleNearby
            );
            this.smoothBlocksVertical(edit, brushConfig, x, z, result);
         } else if (this.sampleNearby) {
            Sample.Result3D result = Sample.calculate3D(bx, by, bz, this.buffer, this.kernelWeights);
            this.smoothBlocks(edit, brushConfig, x, y, z, result.airWeight(), result.sampledBlock(), result.originalBlock());
         } else {
            int airWeight = Sample.calculateAirWeight3D(bx, by, bz, this.buffer, this.kernelWeights);
            this.smoothBlocksFast(edit, brushConfig, x, y, z, airWeight, this.buffer[bx][by][bz]);
         }

         return true;
      } else {
         return true;
      }
   }

   private void smoothBlocks(BrushConfigEditStore edit, BrushConfig brushConfig, int x, int y, int z, int airWeight, int sampledBlock, int originalBlock) {
      Material mat = sampledBlock > 0 ? Material.block(sampledBlock) : brushConfig.getNextMaterial();
      if (airWeight <= this.erosionThresholdLow) {
         if (originalBlock <= 0) {
            edit.setMaterial(x, y, z, mat);
         }
      } else if (airWeight > this.erosionThresholdHigh && originalBlock != 0) {
         edit.setMaterial(x, y, z, Material.EMPTY);
      }
   }

   private void smoothBlocksFast(BrushConfigEditStore edit, BrushConfig brushConfig, int x, int y, int z, int airWeight, int originalBlock) {
      if (airWeight <= this.erosionThresholdLow) {
         if (originalBlock <= 0) {
            edit.setMaterial(x, y, z, brushConfig.getNextMaterial());
         } else {
            Material mat = brushConfig.getNextMaterial();
            if (mat.getBlockId() != originalBlock) {
               edit.setMaterial(x, y, z, mat);
            }
         }
      } else if (airWeight > this.erosionThresholdHigh) {
         if (originalBlock != 0) {
            edit.setMaterial(x, y, z, Material.EMPTY);
         }
      } else {
         Material mat = brushConfig.getNextMaterial();
         if (mat.getBlockId() != originalBlock) {
            edit.setMaterial(x, y, z, mat);
         }
      }
   }

   private void smoothBlocksVertical(BrushConfigEditStore edit, BrushConfig brushConfig, int x, int z, Sample.Result2D result) {
      if (result.currentHeight() != -1) {
         Material mat = result.sampledBlock() > 0 ? Material.block(result.sampledBlock()) : brushConfig.getNextMaterial();
         double delta = result.deltaHeight() + this.erosionPreCalc;
         int currentY = result.currentHeight();
         if (delta > 0.35) {
            int topDy = Math.max((int)delta, 1);

            for (int dy = 1; dy <= topDy; dy++) {
               edit.setMaterial(x, currentY + dy, z, mat);
            }
         } else if (delta < -0.75) {
            int targetDy = Math.min((int)delta, -1);

            for (int dy = 0; dy > targetDy; dy--) {
               edit.setMaterial(x, currentY + dy, z, Material.EMPTY);
            }

            int capY = currentY + targetDy;
            edit.setMaterial(x, capY, z, mat);
            int bufferXIdx = x - this.originX + this.halfBufferWidth;
            int bufferZIdx = z - this.originZ + this.halfBufferWidth;
            int fillY = capY - 1;

            while (true) {
               int bufY = fillY - this.originY + this.halfBufferHeight;
               if (bufY < 0 || bufY >= this.bufferHeight) {
                  break;
               }

               if (this.buffer[bufferXIdx][bufY][bufferZIdx] <= 0) {
                  edit.setMaterial(x, fillY, z, mat);
               }

               fillY--;
            }
         } else if (!this.sampleNearby && mat.getBlockId() != result.originalBlock()) {
            edit.setMaterial(x, currentY, z, mat);
         }
      }
   }

   private static int[][][] buildBuffer(BrushConfigEditStore edit, int cx, int cy, int cz, int sizeXZ, int sizeY) {
      int[][][] buf = new int[sizeXZ][sizeY][sizeXZ];
      Int2IntOpenHashMap fullBlockCache = new Int2IntOpenHashMap();
      fullBlockCache.defaultReturnValue(Integer.MIN_VALUE);
      int halfXZ = sizeXZ / 2;
      int halfY = sizeY / 2;

      for (int dx = 0; dx < sizeXZ; dx++) {
         int wx = cx - halfXZ + dx;

         for (int dz = 0; dz < sizeXZ; dz++) {
            int wz = cz - halfXZ + dz;

            for (int dy = 0; dy < sizeY; dy++) {
               int wy = cy - halfY + dy;
               int block = edit.getBlock(wx, wy, wz);
               if (block != 0) {
                  int cached = fullBlockCache.get(block);
                  if (cached == Integer.MIN_VALUE) {
                     BlockType blockType = ASSET_MAP.getAsset(block);
                     if (blockType == null) {
                        cached = -1;
                     } else {
                        DrawType drawType = blockType.getDrawType();
                        cached = drawType != DrawType.Cube && drawType != DrawType.CubeWithModel ? -1 : block;
                     }

                     fullBlockCache.put(block, cached);
                  }

                  buf[dx][dy][dz] = cached;
               }
            }
         }
      }

      return buf;
   }

   public static enum SmoothKernel {
      Normal(SculptOperation.NORMAL_KERNEL, SculptOperation.NORMAL_HEIGHTMAP, SculptOperation.NORMAL_FLAT),
      Uniform(SculptOperation.UNIFORM_KERNEL, SculptOperation.UNIFORM_HEIGHTMAP, SculptOperation.UNIFORM_FLAT),
      Gaussian(SculptOperation.GAUSS_KERNEL, SculptOperation.GAUSS_HEIGHTMAP, SculptOperation.GAUSS_FLAT),
      Neighbor(SculptOperation.NEIGHBOUR_KERNEL, SculptOperation.NEIGHBOUR_HEIGHTMAP, SculptOperation.NEIGHBOUR_FLAT),
      Square(SculptOperation.SQUARE_KERNEL, SculptOperation.SQUARE_HEIGHTMAP, SculptOperation.SQUARE_FLAT);

      final SculptOperation.Kernel full;
      final SculptOperation.Kernel heightmap;
      final SculptOperation.Kernel flat;

      private SmoothKernel(SculptOperation.Kernel full, SculptOperation.Kernel heightmap, SculptOperation.Kernel flat) {
         this.full = full;
         this.heightmap = heightmap;
         this.flat = flat;
      }
   }

   public static enum SmoothMode {
      Full,
      Heightmap,
      Flat;
   }
}
