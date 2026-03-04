package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigEditStore;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.builtin.buildertools.utils.Material;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import javax.annotation.Nonnull;

public class ErodeOperation extends SequenceBrushOperation {
   public static final BuilderCodec<ErodeOperation> CODEC = BuilderCodec.builder(ErodeOperation.class, ErodeOperation::new)
      .append(new KeyedCodec<>("ErodePreset", new EnumCodec<>(ErodeOperation.ErodePreset.class)), (op, val) -> op.erodePresetArg = val, op -> op.erodePresetArg)
      .documentation("An erosion preset to use with the operation")
      .add()
      .documentation("Erodes blocks following a preset")
      .build();
   private ErodeOperation.ErodePreset erodePresetArg = ErodeOperation.ErodePreset.Default;
   private static final Vector3i[] FACES_TO_CHECK = new Vector3i[]{
      new Vector3i(0, -1, 0), new Vector3i(0, 1, 0), new Vector3i(0, 0, 1), new Vector3i(0, 0, -1), new Vector3i(1, 0, 0), new Vector3i(-1, 0, 0)
   };
   int iterationIndex;

   public ErodeOperation() {
      super("erode", "Erodes blocks following a preset", true);
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
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
      if (this.iterationIndex < this.erodePresetArg.erosionIterations) {
         this.iterateErosion(edit, x, y, z);
      } else {
         this.iterateFill(edit, x, y, z);
      }

      return true;
   }

   @Override
   public void beginIterationIndex(int iterationIndex) {
      this.iterationIndex = iterationIndex;
   }

   @Override
   public int getNumModifyBlockIterations() {
      return this.erodePresetArg.erosionIterations + this.erodePresetArg.fillIterations;
   }

   private void iterateFill(@Nonnull BrushConfigEditStore edit, int x, int y, int z) {
      int block = edit.getBlock(x, y, z);
      if (block == 0) {
         int numFacesFound = 0;
         Int2IntMap blockCount = new Int2IntOpenHashMap();

         for (Vector3i direction : FACES_TO_CHECK) {
            int blockAtRelativePosition = edit.getBlock(x + direction.x, y + direction.y, z + direction.z);
            if (blockAtRelativePosition != 0) {
               numFacesFound++;
               blockCount.put(blockAtRelativePosition, blockCount.getOrDefault(blockAtRelativePosition, 0) + 1);
            }
         }

         if (numFacesFound >= this.erodePresetArg.fillFaces) {
            int blockIdWithHighestQuantity = block;
            int blockIdWithHighestQuantityAmount = 0;
            IntIterator var15 = blockCount.keySet().iterator();

            while (var15.hasNext()) {
               int blockId = (Integer)var15.next();
               int countOfType = blockCount.get(blockId);
               if (countOfType > blockIdWithHighestQuantityAmount) {
                  blockIdWithHighestQuantity = blockId;
                  blockIdWithHighestQuantityAmount = countOfType;
               }
            }

            edit.setMaterial(x, y, z, Material.block(blockIdWithHighestQuantity));
         }
      }
   }

   private void iterateErosion(@Nonnull BrushConfigEditStore edit, int x, int y, int z) {
      int block = edit.getBlock(x, y, z);
      if (block != 0) {
         int numFacesFound = 0;

         for (Vector3i direction : FACES_TO_CHECK) {
            int blockAtRelativePosition = edit.getBlock(x + direction.x, y + direction.y, z + direction.z);
            if (blockAtRelativePosition == 0) {
               numFacesFound++;
            }
         }

         if (numFacesFound >= this.erodePresetArg.erosionFaces) {
            edit.setMaterial(x, y, z, Material.EMPTY);
         }
      }
   }

   public static enum ErodePreset {
      Default(0, 1, 0, 1),
      Melt(2, 1, 5, 1),
      Fill(5, 1, 2, 1),
      Smooth(3, 1, 3, 1),
      Lift(6, 0, 1, 1),
      FloatClean(6, 1, 6, 1);

      public final int erosionFaces;
      public final int erosionIterations;
      public final int fillFaces;
      public final int fillIterations;

      private ErodePreset(int erosionFaces, int erosionIterations, int fillFaces, int fillIterations) {
         this.erosionFaces = erosionFaces;
         this.erosionIterations = erosionIterations;
         this.fillFaces = fillFaces;
         this.fillIterations = fillIterations;
      }
   }
}
