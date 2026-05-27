package com.hypixel.hytale.builtin.buildertools.tooloperations;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.builtin.buildertools.utils.Material;
import com.hypixel.hytale.builtin.buildertools.utils.Sample;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.DrawType;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.Vector3i;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolOnUseInteraction;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.accessor.BlockAccessor;
import com.hypixel.hytale.server.core.universe.world.accessor.ChunkAccessor;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.ArrayDeque;
import javax.annotation.Nonnull;

public class SculptOperation extends ToolOperation {
   private static final BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();
   private Vector3i hit;
   private int bufferWidth;
   private int bufferHeight;
   private double erosionStrength;
   private boolean sampleNearby;
   private boolean heightMap = false;
   private final int[] KERNEL_WEIGHTS;
   private final double KERNEL_STABLE;
   private final double EROSION_RANGE;
   private final int KERNEL_TOTAL;
   private final Material nonSampleMat;
   private final boolean isPrimary;
   private final SculptOperation.FluidMode fluidMode;
   private static final Material AIR = Material.block(0);
   public static final double EROSION_STRENGTH_SCALE = 10.0;
   private static final double HEIGHTMAP_EROSION_BIAS = 0.1;
   public static final double HEIGHTMAP_RAISE_THRESHOLD = 0.35;
   public static final double HEIGHTMAP_LOWER_THRESHOLD = -0.75;
   private double erosionPreCalc;
   private double erosionThresholdLow;
   private double erosionThresholdHigh;
   private int halfBufferWidth;
   private int halfBufferHeight;
   public static final SculptOperation.Kernel NORMAL_KERNEL = new SculptOperation.Kernel(
      25.5, 12.0, new int[]{1, 2, 1, 2, 3, 2, 1, 2, 1, 2, 3, 2, 3, 1, 3, 2, 3, 2, 1, 2, 1, 2, 3, 2, 1, 2, 1}
   );
   public static final SculptOperation.Kernel UNIFORM_KERNEL = new SculptOperation.Kernel(
      12.0, 8.0, new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
   );
   public static final SculptOperation.Kernel GAUSS_KERNEL = new SculptOperation.Kernel(
      12.0, 5.5, new int[]{0, 1, 0, 1, 2, 1, 0, 1, 0, 1, 2, 1, 2, 2, 2, 1, 2, 1, 0, 1, 0, 1, 2, 1, 0, 1, 0}
   );
   public static final SculptOperation.Kernel NEIGHBOUR_KERNEL = new SculptOperation.Kernel(
      2.0, 1.5, new int[]{0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0}
   );
   public static final SculptOperation.Kernel SQUARE_KERNEL = new SculptOperation.Kernel(
      1.75, 2.0, new int[]{0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0}
   );
   public static final SculptOperation.Kernel NORMAL_HEIGHTMAP = new SculptOperation.Kernel(
      -1.0, 1.15, new int[]{0, 1, 2, 1, 0, 1, 3, 4, 3, 1, 2, 4, 2, 4, 2, 1, 3, 4, 3, 1, 0, 1, 2, 1, 0}
   );
   public static final SculptOperation.Kernel UNIFORM_HEIGHTMAP = new SculptOperation.Kernel(
      -1.0, 1.15, new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
   );
   public static final SculptOperation.Kernel GAUSS_HEIGHTMAP = new SculptOperation.Kernel(
      -1.0, 1.15, new int[]{0, 0, 1, 0, 0, 0, 1, 2, 1, 0, 1, 2, 0, 2, 1, 0, 1, 2, 1, 0, 0, 0, 1, 0, 0}
   );
   public static final SculptOperation.Kernel NEIGHBOUR_HEIGHTMAP = new SculptOperation.Kernel(
      -1.0, 1.15, new int[]{1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1}
   );
   public static final SculptOperation.Kernel SQUARE_HEIGHTMAP = new SculptOperation.Kernel(
      -1.0, 1.15, new int[]{1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 1, 1, 1, 1}
   );
   public static final SculptOperation.Kernel NORMAL_FLAT = new SculptOperation.Kernel(
      10.0, 5.0, new int[]{0, 0, 0, 2, 2, 2, 0, 0, 0, 0, 0, 0, 3, 1, 3, 0, 0, 0, 0, 0, 0, 2, 2, 2, 0, 0, 0}
   );
   public static final SculptOperation.Kernel UNIFORM_FLAT = new SculptOperation.Kernel(
      4.0, 3.0, new int[]{0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0}
   );
   public static final SculptOperation.Kernel GAUSS_FLAT = new SculptOperation.Kernel(
      6.0, 3.0, new int[]{0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 2, 2, 2, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0}
   );
   public static final SculptOperation.Kernel NEIGHBOUR_FLAT = new SculptOperation.Kernel(
      2.0, 1.5, new int[]{0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0}
   );
   public static final SculptOperation.Kernel SQUARE_FLAT = new SculptOperation.Kernel(
      1.75, 2.0, new int[]{0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0}
   );
   private static final int INCREASE_BUFFER = 2;
   public static final int HEIGHTMAP_EXTRA_WIDTH = 4;
   public static final int HEIGHTMAP_EXTRA_HEIGHT = 20;
   private int[][][] buffer;
   private int[][][] mutatedBuffer;
   private int[][][] fluidBuffer;
   private boolean hasAnyFluid;
   private int[][] heightMapBuffer;
   private final boolean isAltPlaySculptBrushModDown;

   public SculptOperation(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Player player,
      @Nonnull PlayerRef playerRef,
      @Nonnull BuilderToolOnUseInteraction packet,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      super(ref, packet, componentAccessor);
      String sampling = (String)this.args.tool().get("sampling");
      String mode = (String)this.args.tool().get("mode");
      if (mode == null) {
         mode = "Full";
      }

      SculptOperation.Kernel kernelPicked;
      if ("Heightmap".equals(mode)) {
         this.heightMap = true;

         kernelPicked = switch (sampling) {
            case "Uniform" -> UNIFORM_HEIGHTMAP;
            case "Gaussian" -> GAUSS_HEIGHTMAP;
            case "Neighbor" -> NEIGHBOUR_HEIGHTMAP;
            case "Square" -> SQUARE_HEIGHTMAP;
            default -> NORMAL_HEIGHTMAP;
         };
      } else if ("Flat".equals(mode)) {
         kernelPicked = switch (sampling) {
            case "Uniform" -> UNIFORM_FLAT;
            case "Gaussian" -> GAUSS_FLAT;
            case "Neighbor" -> NEIGHBOUR_FLAT;
            case "Square" -> SQUARE_FLAT;
            default -> NORMAL_FLAT;
         };
      } else {
         kernelPicked = switch (sampling) {
            case "Uniform" -> UNIFORM_KERNEL;
            case "Gaussian" -> GAUSS_KERNEL;
            case "Neighbor" -> NEIGHBOUR_KERNEL;
            case "Square" -> SQUARE_KERNEL;
            default -> NORMAL_KERNEL;
         };
      }

      this.KERNEL_WEIGHTS = kernelPicked.weights();
      this.KERNEL_STABLE = kernelPicked.stable();
      this.EROSION_RANGE = kernelPicked.range();
      this.KERNEL_TOTAL = kernelPicked.total();
      this.isAltPlaySculptBrushModDown = packet.isAltPlaySculptBrushModDown;
      this.sampleNearby = (Boolean)this.args.tool().get("useExistingBlocks");
      if (this.isAltPlaySculptBrushModDown) {
         this.erosionStrength = 0.0;
      } else {
         this.erosionStrength = ((Integer)this.args.tool().get("erosionStrength")).intValue() / 10.0;
      }

      String var9 = (String)this.args.tool().get("fluidMode");
      switch (var9) {
         case "Fix":
            this.fluidMode = SculptOperation.FluidMode.FIX;
            break;
         case "Remove":
            this.fluidMode = SculptOperation.FluidMode.REMOVE;
            break;
         default:
            this.fluidMode = SculptOperation.FluidMode.IGNORE;
      }

      this.isPrimary = this.interactionType == InteractionType.Primary;
      if (this.heightMap) {
         this.erosionStrength += 0.1;
      }

      if (this.isPrimary) {
         this.erosionStrength *= -1.0;
      }

      if (!this.sampleNearby) {
         this.nonSampleMat = this.pattern.equals(BlockPattern.EMPTY) ? Material.fromKey("Rock_Stone") : Material.fromPattern(this.pattern, this.random);
      } else {
         this.nonSampleMat = null;
      }

      this.erosionPreCalc = -this.EROSION_RANGE * this.erosionStrength;
      this.erosionThresholdLow = this.KERNEL_STABLE + this.erosionPreCalc;
      this.erosionThresholdHigh = this.KERNEL_STABLE + 1.0 + this.erosionPreCalc;
      this.bufferWidth = this.getBrushWidth() + 2;
      this.bufferHeight = this.getBrushHeight() + 2;
      if (this.bufferWidth % 2 == 0) {
         this.bufferWidth++;
      }

      if (this.bufferHeight % 2 == 0) {
         this.bufferHeight++;
      }

      if (this.heightMap) {
         this.bufferWidth += 4;
         this.bufferHeight += 20;
      }

      this.halfBufferWidth = this.bufferWidth / 2;
      this.halfBufferHeight = this.bufferHeight / 2;
   }

   @Override
   public void execute(ComponentAccessor<EntityStore> componentAccessor) {
      this.executeAt(this.x, this.y, this.z, componentAccessor);
   }

   @Override
   public void executeAt(int posX, int posY, int posZ, ComponentAccessor<EntityStore> componentAccessor) {
      this.prepareBuffersForCenter(posX, posY + this.originOffsetY, posZ);
      super.executeAt(posX, posY, posZ, componentAccessor);
      if (this.fluidMode == SculptOperation.FluidMode.FIX) {
         this.floodFillFluids();
      }
   }

   private void prepareBuffersForCenter(int centerX, int centerY, int centerZ) {
      this.hit = new Vector3i(centerX, centerY, centerZ);
      boolean needFluids = this.fluidMode != SculptOperation.FluidMode.IGNORE;
      this.fluidBuffer = needFluids ? new int[this.bufferWidth][this.bufferHeight][this.bufferWidth] : null;
      this.buffer = bufferRegion(this.edit.getAccessor(), this.hit, this.bufferWidth, this.bufferHeight, this.bufferWidth, this.fluidBuffer);
      this.hasAnyFluid = needFluids && hasNonEmptyFluid(this.fluidBuffer, this.bufferWidth, this.bufferHeight);
      this.mutatedBuffer = this.fluidMode == SculptOperation.FluidMode.FIX ? copyBuffer(this.buffer) : null;
      this.heightMapBuffer = this.heightMap ? preCalcBuffer(this.hit.y, this.buffer) : null;
   }

   @Override
   protected boolean executeBlock(int x, int y, int z) {
      if (this.hit == null) {
         return false;
      } else {
         int bufferX = x - this.hit.x + this.halfBufferWidth;
         int bufferY = y - this.hit.y + this.halfBufferHeight;
         int bufferZ = z - this.hit.z + this.halfBufferWidth;
         int margin = this.heightMap ? 2 : 1;
         if (bufferX >= margin
            && bufferY >= margin
            && bufferZ >= margin
            && bufferX < this.bufferWidth - margin
            && bufferY < this.bufferHeight - margin
            && bufferZ < this.bufferWidth - margin) {
            if (this.buffer[bufferX][bufferY][bufferZ] == -1) {
               this.setAir(x, y, z);
            }

            if (this.fluidMode == SculptOperation.FluidMode.REMOVE && this.hasAnyFluid && this.fluidBuffer[bufferX][bufferY][bufferZ] != 0) {
               this.edit.clearFluid(x, y, z);
            }

            if (this.heightMap) {
               if (y != this.hit.y) {
                  return true;
               }

               Sample.Result2D result = Sample.calcDeltaHeights(
                  bufferX, bufferY, bufferZ, this.heightMapBuffer, this.buffer, this.KERNEL_WEIGHTS, this.KERNEL_TOTAL, this.hit.y, this.sampleNearby
               );
               this.smoothBlocks(x, z, result);
            } else if (this.sampleNearby) {
               Sample.Result3D result = Sample.calculate3D(bufferX, bufferY, bufferZ, this.buffer, this.KERNEL_WEIGHTS);
               this.smoothBlocks(x, y, z, result);
            } else {
               int airWeight = Sample.calculateAirWeight3D(bufferX, bufferY, bufferZ, this.buffer, this.KERNEL_WEIGHTS);
               this.smoothBlocksFast(x, y, z, airWeight, this.buffer[bufferX][bufferY][bufferZ]);
            }

            return true;
         } else {
            return true;
         }
      }
   }

   private void smoothBlocks(int x, int y, int z, Sample.Result3D result) {
      int airWeight = result.airWeight();
      int sampled = result.sampledBlock();
      Material mat = sampled > 0 ? Material.block(sampled) : Material.fromPattern(this.pattern, this.random);
      if (airWeight <= this.erosionThresholdLow) {
         if (result.originalBlock() <= 0) {
            this.setSolid(x, y, z, mat);
         }
      } else if (airWeight > this.erosionThresholdHigh && result.originalBlock() != 0) {
         this.setAir(x, y, z);
      }
   }

   private Material getNonSampleMat() {
      return this.nonSampleMat != null ? this.nonSampleMat : Material.fromPattern(this.pattern, this.random);
   }

   private void setSolid(int x, int y, int z, Material mat) {
      if (this.hasAnyFluid) {
         int bx = x - this.hit.x + this.halfBufferWidth;
         int by = y - this.hit.y + this.halfBufferHeight;
         int bz = z - this.hit.z + this.halfBufferWidth;
         if (bx >= 0 && by >= 0 && bz >= 0 && bx < this.bufferWidth && by < this.bufferHeight && bz < this.bufferWidth && this.fluidBuffer[bx][by][bz] != 0) {
            this.edit.clearFluid(x, y, z);
         }
      }

      this.edit.setMaterial(x, y, z, mat);
      this.updateMutatedBuffer(x, y, z, mat.getBlockId());
   }

   private void setAir(int x, int y, int z) {
      this.edit.setMaterial(x, y, z, AIR);
      this.updateMutatedBuffer(x, y, z, 0);
   }

   private void updateMutatedBuffer(int x, int y, int z, int blockId) {
      if (this.mutatedBuffer != null) {
         int bx = x - this.hit.x + this.halfBufferWidth;
         int by = y - this.hit.y + this.halfBufferHeight;
         int bz = z - this.hit.z + this.halfBufferWidth;
         if (bx >= 0 && by >= 0 && bz >= 0 && bx < this.bufferWidth && by < this.bufferHeight && bz < this.bufferWidth) {
            this.mutatedBuffer[bx][by][bz] = blockId;
         }
      }
   }

   private void smoothBlocksFast(int x, int y, int z, int airWeight, int originalBlock) {
      if (airWeight <= this.erosionThresholdLow) {
         if (originalBlock <= 0) {
            this.setSolid(x, y, z, this.getNonSampleMat());
         } else {
            Material mat = this.getNonSampleMat();
            if (mat.getBlockId() != originalBlock) {
               this.setSolid(x, y, z, mat);
            }
         }
      } else if (airWeight > this.erosionThresholdHigh) {
         if (originalBlock != 0) {
            this.setAir(x, y, z);
         }
      } else {
         Material mat = this.getNonSampleMat();
         if (mat.getBlockId() != originalBlock) {
            this.setSolid(x, y, z, mat);
         }
      }
   }

   private void smoothBlocks(int x, int z, Sample.Result2D result) {
      if (result.currentHeight() != -1) {
         Material mat;
         if (!this.sampleNearby) {
            mat = this.getNonSampleMat();
         } else if (result.sampledBlock() == 0) {
            mat = Material.fromPattern(this.pattern, this.random);
         } else {
            mat = Material.block(result.sampledBlock());
         }

         double delta = result.deltaHeight() + this.erosionPreCalc;
         int currentY = result.currentHeight();
         if (delta > 0.35) {
            int topDy = Math.max((int)delta, 1);

            for (int dy = 1; dy <= topDy; dy++) {
               this.setSolid(x, currentY + dy, z, mat);
            }
         } else if (delta < -0.75) {
            int targetDy = Math.min((int)delta, -1);

            for (int dy = 0; dy > targetDy; dy--) {
               this.setAir(x, currentY + dy, z);
            }

            int capY = currentY + targetDy;
            this.setSolid(x, capY, z, mat);
            int bufferXIdx = x - this.hit.x + this.halfBufferWidth;
            int bufferZIdx = z - this.hit.z + this.halfBufferWidth;
            int fillY = capY - 1;

            while (true) {
               int bufY = fillY - this.hit.y + this.halfBufferHeight;
               if (bufY < 0 || bufY >= this.bufferHeight) {
                  break;
               }

               if (this.buffer[bufferXIdx][bufY][bufferZIdx] <= 0) {
                  this.setSolid(x, fillY, z, mat);
               }

               fillY--;
            }
         } else if (!this.sampleNearby && mat.getBlockId() != result.originalBlock()) {
            this.setSolid(x, currentY, z, mat);
         }
      }
   }

   private void floodFillFluids() {
      boolean[][] propagateDown = new boolean[this.bufferWidth][this.bufferWidth];
      int[][] resolvedFluidId = new int[this.bufferWidth][this.bufferWidth];
      boolean[][] visited = new boolean[this.bufferWidth][this.bufferWidth];
      ArrayDeque<SculptOperation.FluidFillEntry> queue = new ArrayDeque<>(this.bufferWidth * this.bufferWidth);

      for (int bufY = this.bufferHeight - 1; bufY >= 0; bufY--) {
         int worldY = this.hit.y - this.halfBufferHeight + bufY;

         for (int bx = 0; bx < this.bufferWidth; bx++) {
            for (int bz = 0; bz < this.bufferWidth; bz++) {
               visited[bx][bz] = false;
               int existingFluid = this.fluidBuffer[bx][bufY][bz];
               boolean fromAbove = propagateDown[bx][bz];
               propagateDown[bx][bz] = false;
               int seedFluid = existingFluid != 0 ? existingFluid : resolvedFluidId[bx][bz];
               resolvedFluidId[bx][bz] = 0;
               if (seedFluid != 0 || fromAbove) {
                  if (this.mutatedBuffer[bx][bufY][bz] <= 0) {
                     queue.add(new SculptOperation.FluidFillEntry(bx, bz, seedFluid));
                     visited[bx][bz] = true;
                  } else {
                     propagateDown[bx][bz] = true;
                     if (seedFluid != 0) {
                        resolvedFluidId[bx][bz] = seedFluid;
                     }
                  }
               }
            }
         }

         while (!queue.isEmpty()) {
            SculptOperation.FluidFillEntry entry = queue.poll();
            int bx = entry.bx();
            int bzx = entry.bz();
            int carriedFluid = entry.fluidId();
            propagateDown[bx][bzx] = true;
            int existingFluidHere = this.fluidBuffer[bx][bufY][bzx];
            if (existingFluidHere != 0) {
               carriedFluid = existingFluidHere;
            } else if (carriedFluid == 0) {
               carriedFluid = this.findNearbyFluidIdFromBuffer(bx, bufY, bzx);
            }

            if (carriedFluid != 0) {
               int wx = this.hit.x - this.halfBufferWidth + bx;
               int wz = this.hit.z - this.halfBufferWidth + bzx;
               Fluid fluid = Fluid.getAssetMap().getAsset(carriedFluid);
               byte maxLevel = (byte)(fluid != null ? fluid.getMaxFluidLevel() : 8);
               this.edit.setMaterial(wx, worldY, wz, Material.fluid(carriedFluid, maxLevel));
               this.fluidBuffer[bx][bufY][bzx] = carriedFluid;
               resolvedFluidId[bx][bzx] = carriedFluid;
            }

            if (bx > 0 && !visited[bx - 1][bzx] && this.mutatedBuffer[bx - 1][bufY][bzx] <= 0) {
               visited[bx - 1][bzx] = true;
               queue.add(new SculptOperation.FluidFillEntry(bx - 1, bzx, carriedFluid));
            }

            if (bx < this.bufferWidth - 1 && !visited[bx + 1][bzx] && this.mutatedBuffer[bx + 1][bufY][bzx] <= 0) {
               visited[bx + 1][bzx] = true;
               queue.add(new SculptOperation.FluidFillEntry(bx + 1, bzx, carriedFluid));
            }

            if (bzx > 0 && !visited[bx][bzx - 1] && this.mutatedBuffer[bx][bufY][bzx - 1] <= 0) {
               visited[bx][bzx - 1] = true;
               queue.add(new SculptOperation.FluidFillEntry(bx, bzx - 1, carriedFluid));
            }

            if (bzx < this.bufferWidth - 1 && !visited[bx][bzx + 1] && this.mutatedBuffer[bx][bufY][bzx + 1] <= 0) {
               visited[bx][bzx + 1] = true;
               queue.add(new SculptOperation.FluidFillEntry(bx, bzx + 1, carriedFluid));
            }
         }
      }
   }

   private int findNearbyFluidIdFromBuffer(int bx, int by, int bz) {
      if (by + 1 < this.bufferHeight) {
         int id = this.fluidBuffer[bx][by + 1][bz];
         if (id != 0) {
            return id;
         }
      }

      if (bx > 0) {
         int id = this.fluidBuffer[bx - 1][by][bz];
         if (id != 0) {
            return id;
         }
      }

      if (bx < this.bufferWidth - 1) {
         int id = this.fluidBuffer[bx + 1][by][bz];
         if (id != 0) {
            return id;
         }
      }

      if (bz > 0) {
         int id = this.fluidBuffer[bx][by][bz - 1];
         if (id != 0) {
            return id;
         }
      }

      if (bz < this.bufferWidth - 1) {
         int id = this.fluidBuffer[bx][by][bz + 1];
         if (id != 0) {
            return id;
         }
      }

      if (by > 0) {
         int id = this.fluidBuffer[bx][by - 1][bz];
         if (id != 0) {
            return id;
         }
      }

      return 0;
   }

   public static int[][] preCalcBuffer(int hitY, int[][][] buffer) {
      int bufferHeight = buffer[0].length;
      int bufferSize = buffer.length;
      int[][] heightMapBuffer = new int[bufferSize][bufferSize];

      for (int x = 0; x < bufferSize; x++) {
         for (int z = 0; z < bufferSize; z++) {
            if (buffer[x][bufferHeight - 1][z] > 0) {
               heightMapBuffer[x][z] = -1;
            } else {
               int y = bufferHeight - 2;

               for (int block = 0; block <= 0 && y >= 0; y--) {
                  block = buffer[x][y][z];
               }

               if (y == -1) {
                  heightMapBuffer[x][z] = -1;
               } else {
                  heightMapBuffer[x][z] = y + 1 + hitY - bufferHeight / 2;
               }
            }
         }
      }

      return heightMapBuffer;
   }

   private static int[][][] copyBuffer(int[][][] src) {
      int sx = src.length;
      int sy = src[0].length;
      int sz = src[0][0].length;
      int[][][] dst = new int[sx][sy][sz];

      for (int x = 0; x < sx; x++) {
         for (int y = 0; y < sy; y++) {
            System.arraycopy(src[x][y], 0, dst[x][y], 0, sz);
         }
      }

      return dst;
   }

   private static boolean hasNonEmptyFluid(int[][][] fluidBuf, int sizeXZ, int sizeY) {
      for (int bx = 0; bx < sizeXZ; bx++) {
         for (int by = 0; by < sizeY; by++) {
            for (int bz = 0; bz < sizeXZ; bz++) {
               if (fluidBuf[bx][by][bz] != 0) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public static int filterFullBlockForBuffer(int block, Int2IntOpenHashMap fullBlockCache) {
      if (block == 0) {
         return 0;
      } else {
         int cached = fullBlockCache.get(block);
         if (cached == Integer.MIN_VALUE) {
            BlockType blockType = assetMap.getAsset(block);
            if (blockType == null) {
               cached = -1;
            } else {
               DrawType drawType = blockType.getDrawType();
               cached = drawType != DrawType.Cube && drawType != DrawType.CubeWithModel ? -1 : block;
            }

            fullBlockCache.put(block, cached);
         }

         return cached;
      }
   }

   public static int[][][] bufferRegion(ChunkAccessor accessor, Vector3i center, int sizeX, int sizeY, int sizeZ, int[][][] fluidOut) {
      int[][][] blockBuf = new int[sizeX][sizeY][sizeZ];
      Int2IntOpenHashMap fullBlockCache = new Int2IntOpenHashMap();
      fullBlockCache.defaultReturnValue(Integer.MIN_VALUE);
      int worldXMin = center.x - sizeX / 2;
      int worldXMax = worldXMin + sizeX - 1;
      int worldZMin = center.z - sizeZ / 2;
      int worldZMax = worldZMin + sizeZ - 1;
      int worldYMin = center.y - sizeY / 2;
      int chunkXMin = worldXMin >> 5;
      int chunkXMax = worldXMax >> 5;
      int chunkZMin = worldZMin >> 5;
      int chunkZMax = worldZMax >> 5;

      for (int cx = chunkXMin; cx <= chunkXMax; cx++) {
         for (int cz = chunkZMin; cz <= chunkZMax; cz++) {
            BlockAccessor chunk = accessor.getChunk(ChunkUtil.indexChunk(cx, cz));
            if (chunk != null) {
               int localXMin = Math.max(worldXMin, cx << 5);
               int localXMax = Math.min(worldXMax, (cx << 5) + 31);
               int localZMin = Math.max(worldZMin, cz << 5);
               int localZMax = Math.min(worldZMax, (cz << 5) + 31);

               for (int wx = localXMin; wx <= localXMax; wx++) {
                  int bx = wx - worldXMin;

                  for (int wz = localZMin; wz <= localZMax; wz++) {
                     int bz = wz - worldZMin;

                     for (int by = 0; by < sizeY; by++) {
                        int wy = worldYMin + by;
                        int block = chunk.getBlock(wx, wy, wz);
                        blockBuf[bx][by][bz] = filterFullBlockForBuffer(block, fullBlockCache);
                        if (fluidOut != null) {
                           fluidOut[bx][by][bz] = chunk.getFluidId(wx, wy, wz);
                        }
                     }
                  }
               }
            }
         }
      }

      return blockBuf;
   }

   private record FluidFillEntry(int bx, int bz, int fluidId) {
   }

   private static enum FluidMode {
      IGNORE,
      FIX,
      REMOVE;
   }

   public record Kernel(double stable, double range, int[] weights, int total) {
      public Kernel(double stable, double range, int[] weights) {
         this(stable, range, weights, sumWeights(weights));
      }

      private static int sumWeights(int[] w) {
         int sum = 0;

         for (int v : w) {
            sum += v;
         }

         return sum;
      }
   }
}
