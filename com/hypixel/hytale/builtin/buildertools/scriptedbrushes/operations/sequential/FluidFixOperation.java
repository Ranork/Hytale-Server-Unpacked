package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigEditStore;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.builtin.buildertools.tooloperations.SculptOperation;
import com.hypixel.hytale.builtin.buildertools.utils.Material;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.ArrayDeque;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import org.joml.Vector3i;

public class FluidFixOperation extends SequenceBrushOperation {
   public static final BuilderCodec<FluidFixOperation> CODEC = BuilderCodec.builder(FluidFixOperation.class, FluidFixOperation::new)
      .append(new KeyedCodec<>("FluidAction", new EnumCodec<>(FluidFixOperation.FluidAction.class)), (op, val) -> op.fluidAction = val, op -> op.fluidAction)
      .documentation("Fill flood-fills fluids into air pockets. Clear removes all fluids in the brush area.")
      .add()
      .documentation("Fixes or clears fluids in the brush area after terrain modifications")
      .build();
   @Nonnull
   private FluidFixOperation.FluidAction fluidAction = FluidFixOperation.FluidAction.Fill;
   private static final int BUFFER_MARGIN = 1;
   private static final long MAX_BUFFER_VOLUME = 1000000L;
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private int originX;
   private int originY;
   private int originZ;
   private int bufferWidth;
   private int bufferHeight;
   private int halfBufferWidth;
   private int halfBufferHeight;

   public FluidFixOperation() {
      super("Fluid Fix", "Fixes or clears fluids in the brush area after terrain modifications", true);
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      Vector3i origin = brushConfig.getOrigin();
      this.originX = origin.x;
      this.originY = origin.y;
      this.originZ = origin.z;
      this.bufferWidth = brushConfig.getShapeWidth() + 2;
      this.bufferHeight = brushConfig.getShapeHeight() + 2;
      this.halfBufferWidth = this.bufferWidth / 2;
      this.halfBufferHeight = this.bufferHeight / 2;
      if (this.fluidAction == FluidFixOperation.FluidAction.Fill) {
         long bufferVolume = (long)this.bufferWidth * this.bufferHeight * this.bufferWidth;
         if (bufferVolume > 1000000L) {
            LOGGER.at(Level.WARNING)
               .log("Skipping fluid fill for oversized brush buffer %dx%dx%d (%d cells)", this.bufferWidth, this.bufferHeight, this.bufferWidth, bufferVolume);
            return;
         }

         BrushConfigEditStore edit = brushConfigCommandExecutor.getEdit();
         int[][][] blockBuffer = new int[this.bufferWidth][this.bufferHeight][this.bufferWidth];
         int[][][] fluidBuffer = new int[this.bufferWidth][this.bufferHeight][this.bufferWidth];
         this.buildBuffers(edit, blockBuffer, fluidBuffer);
         this.floodFillFluids(edit, blockBuffer, fluidBuffer);
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
      if (this.fluidAction == FluidFixOperation.FluidAction.Clear) {
         int fluidId = edit.getFluid(x, y, z);
         if (fluidId != 0) {
            edit.setMaterial(x, y, z, Material.fluid(0, (byte)0));
         }
      }

      return true;
   }

   private void buildBuffers(BrushConfigEditStore edit, int[][][] blockOut, int[][][] fluidOut) {
      Int2IntOpenHashMap fullBlockCache = new Int2IntOpenHashMap();
      fullBlockCache.defaultReturnValue(Integer.MIN_VALUE);

      for (int dx = 0; dx < this.bufferWidth; dx++) {
         int wx = this.originX - this.halfBufferWidth + dx;

         for (int dz = 0; dz < this.bufferWidth; dz++) {
            int wz = this.originZ - this.halfBufferWidth + dz;

            for (int dy = 0; dy < this.bufferHeight; dy++) {
               int wy = this.originY - this.halfBufferHeight + dy;
               blockOut[dx][dy][dz] = SculptOperation.filterFullBlockForBuffer(edit.getBlock(wx, wy, wz), fullBlockCache);
               fluidOut[dx][dy][dz] = edit.getFluid(wx, wy, wz);
            }
         }
      }
   }

   private void floodFillFluids(BrushConfigEditStore edit, int[][][] blockBuffer, int[][][] fluidBuffer) {
      boolean[][] propagateDown = new boolean[this.bufferWidth][this.bufferWidth];
      int[][] resolvedFluidId = new int[this.bufferWidth][this.bufferWidth];
      boolean[][] visited = new boolean[this.bufferWidth][this.bufferWidth];
      ArrayDeque<FluidFixOperation.FluidFillEntry> queue = new ArrayDeque<>(this.bufferWidth * this.bufferWidth);

      for (int bufY = this.bufferHeight - 1; bufY >= 0; bufY--) {
         int worldY = this.originY - this.halfBufferHeight + bufY;

         for (int bx = 0; bx < this.bufferWidth; bx++) {
            for (int bz = 0; bz < this.bufferWidth; bz++) {
               visited[bx][bz] = false;
               int existingFluid = fluidBuffer[bx][bufY][bz];
               boolean fromAbove = propagateDown[bx][bz];
               propagateDown[bx][bz] = false;
               int seedFluid = existingFluid != 0 ? existingFluid : resolvedFluidId[bx][bz];
               resolvedFluidId[bx][bz] = 0;
               if (seedFluid != 0 || fromAbove) {
                  if (blockBuffer[bx][bufY][bz] <= 0) {
                     queue.add(new FluidFixOperation.FluidFillEntry(bx, bz, seedFluid));
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
            FluidFixOperation.FluidFillEntry entry = queue.poll();
            int bx = entry.bx();
            int bzx = entry.bz();
            int carriedFluid = entry.fluidId();
            propagateDown[bx][bzx] = true;
            int existingFluidHere = fluidBuffer[bx][bufY][bzx];
            if (existingFluidHere != 0) {
               carriedFluid = existingFluidHere;
            } else if (carriedFluid == 0) {
               carriedFluid = this.findNearbyFluid(fluidBuffer, bx, bufY, bzx);
            }

            if (carriedFluid != 0) {
               int wx = this.originX - this.halfBufferWidth + bx;
               int wz = this.originZ - this.halfBufferWidth + bzx;
               Fluid fluid = Fluid.getAssetMap().getAsset(carriedFluid);
               byte maxLevel = (byte)(fluid != null ? fluid.getMaxFluidLevel() : 8);
               edit.setMaterial(wx, worldY, wz, Material.fluid(carriedFluid, maxLevel));
               fluidBuffer[bx][bufY][bzx] = carriedFluid;
               resolvedFluidId[bx][bzx] = carriedFluid;
            }

            if (bx > 0 && !visited[bx - 1][bzx] && blockBuffer[bx - 1][bufY][bzx] <= 0) {
               visited[bx - 1][bzx] = true;
               queue.add(new FluidFixOperation.FluidFillEntry(bx - 1, bzx, carriedFluid));
            }

            if (bx < this.bufferWidth - 1 && !visited[bx + 1][bzx] && blockBuffer[bx + 1][bufY][bzx] <= 0) {
               visited[bx + 1][bzx] = true;
               queue.add(new FluidFixOperation.FluidFillEntry(bx + 1, bzx, carriedFluid));
            }

            if (bzx > 0 && !visited[bx][bzx - 1] && blockBuffer[bx][bufY][bzx - 1] <= 0) {
               visited[bx][bzx - 1] = true;
               queue.add(new FluidFixOperation.FluidFillEntry(bx, bzx - 1, carriedFluid));
            }

            if (bzx < this.bufferWidth - 1 && !visited[bx][bzx + 1] && blockBuffer[bx][bufY][bzx + 1] <= 0) {
               visited[bx][bzx + 1] = true;
               queue.add(new FluidFixOperation.FluidFillEntry(bx, bzx + 1, carriedFluid));
            }
         }
      }
   }

   private int findNearbyFluid(int[][][] fluidBuffer, int bx, int by, int bz) {
      if (by + 1 < this.bufferHeight) {
         int id = fluidBuffer[bx][by + 1][bz];
         if (id != 0) {
            return id;
         }
      }

      if (bx > 0) {
         int id = fluidBuffer[bx - 1][by][bz];
         if (id != 0) {
            return id;
         }
      }

      if (bx < this.bufferWidth - 1) {
         int id = fluidBuffer[bx + 1][by][bz];
         if (id != 0) {
            return id;
         }
      }

      if (bz > 0) {
         int id = fluidBuffer[bx][by][bz - 1];
         if (id != 0) {
            return id;
         }
      }

      if (bz < this.bufferWidth - 1) {
         int id = fluidBuffer[bx][by][bz + 1];
         if (id != 0) {
            return id;
         }
      }

      if (by > 0) {
         int id = fluidBuffer[bx][by - 1][bz];
         if (id != 0) {
            return id;
         }
      }

      return 0;
   }

   public static enum FluidAction {
      Fill,
      Clear;
   }

   private record FluidFillEntry(int bx, int bz, int fluidId) {
   }
}
