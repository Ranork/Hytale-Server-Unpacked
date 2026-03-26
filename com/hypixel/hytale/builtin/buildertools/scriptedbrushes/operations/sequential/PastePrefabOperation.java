package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigEditStore;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.builtin.buildertools.utils.Material;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.PrefabListAsset;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabBufferCall;
import com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabBufferUtil;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.PrefabBuffer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.nio.file.Path;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PastePrefabOperation extends SequenceBrushOperation {
   public static final BuilderCodec<PastePrefabOperation> CODEC = BuilderCodec.builder(PastePrefabOperation.class, PastePrefabOperation::new)
      .append(new KeyedCodec<>("PrefabListAssetName", Codec.STRING), (op, val) -> op.prefabListAssetId = val, op -> op.prefabListAssetId)
      .documentation("The name of a PrefabList asset")
      .add()
      .documentation("Paste a prefab at the origin+offset point")
      .build();
   @Nullable
   public String prefabListAssetId = null;
   private boolean hasBeenPlacedAlready = false;

   public PastePrefabOperation() {
      super("Paste Prefab", "Paste a prefab at the origin+offset point", true);
   }

   @Override
   public void resetInternalState() {
      this.hasBeenPlacedAlready = false;
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.hasBeenPlacedAlready = false;
   }

   @Override
   public boolean modifyBlocks(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      BrushConfigCommandExecutor brushConfigCommandExecutor,
      BrushConfigEditStore edit,
      int x,
      int y,
      int z,
      ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (this.hasBeenPlacedAlready) {
         return false;
      } else {
         PrefabListAsset prefabListAsset = this.prefabListAssetId != null ? PrefabListAsset.getAssetMap().getAsset(this.prefabListAssetId) : null;
         if (prefabListAsset == null) {
            brushConfig.setErrorFlag("PrefabList asset not found: " + this.prefabListAssetId);
            return false;
         } else {
            Path prefabPath = prefabListAsset.getRandomPrefab();
            if (prefabPath == null) {
               brushConfig.setErrorFlag("No prefab found in prefab list. Please double check your PrefabList asset.");
               return false;
            } else {
               PrefabBuffer.PrefabBufferAccessor accessor = PrefabBufferUtil.loadBuffer(prefabPath).newAccess();
               this.hasBeenPlacedAlready = true;
               accessor.forEach(
                  IPrefabBuffer.iterateAllColumns(),
                  (xi, yi, zi, blockId, holder, supportValue, rotation, filler, call, fluidId, fluidLevel) -> {
                     int bx = x + xi;
                     int by = y + yi;
                     int bz = z + zi;
                     if (filler != 0) {
                        if (fluidId != 0) {
                           edit.setMaterial(bx, by, bz, Material.fluid(fluidId, (byte)fluidLevel));
                        }
                     } else {
                        edit.setMaterial(bx, by, bz, Material.full(blockId, rotation, supportValue, 0, holder, fluidId, (byte)fluidLevel));
                     }
                  },
                  (xi, zi, entityWrappers, t) -> {},
                  (xi, yi, zi, path, fitHeightmap, inheritSeed, inheritHeightCondition, weights, rotation, t) -> {},
                  new PrefabBufferCall(new Random(), PrefabRotation.fromRotation(Rotation.None))
               );
               accessor.release();
               return false;
            }
         }
      }
   }
}
