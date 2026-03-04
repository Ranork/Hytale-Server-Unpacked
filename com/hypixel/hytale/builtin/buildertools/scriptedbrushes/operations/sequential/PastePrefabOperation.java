package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigEditStore;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.PrefabListAsset;
import com.hypixel.hytale.server.core.blocktype.component.BlockPhysics;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabBufferCall;
import com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabBufferUtil;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.PrefabBuffer;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.accessor.LocalCachedChunkAccessor;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
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
               World world = componentAccessor.getExternalData().getWorld();
               PrefabBuffer.PrefabBufferAccessor accessor = PrefabBufferUtil.loadBuffer(prefabPath).newAccess();
               this.hasBeenPlacedAlready = true;
               double xLength = accessor.getMaxX() - accessor.getMinX();
               double zLength = accessor.getMaxZ() - accessor.getMinZ();
               int prefabRadius = (int)MathUtil.fastFloor(0.5 * Math.sqrt(xLength * xLength + zLength * zLength));
               LocalCachedChunkAccessor chunkAccessor = LocalCachedChunkAccessor.atWorldCoords(world, x, z, prefabRadius);
               BlockTypeAssetMap<String, BlockType> blockTypeMap = BlockType.getAssetMap();
               accessor.forEach(
                  IPrefabBuffer.iterateAllColumns(),
                  (xi, yi, zi, blockId, holder, supportValue, rotation, filler, call, fluidId, fluidLevel) -> {
                     int bx = x + xi;
                     int by = y + yi;
                     int bz = z + zi;
                     WorldChunk chunk = chunkAccessor.getNonTickingChunk(ChunkUtil.indexChunkFromBlock(bx, bz));
                     Store<ChunkStore> store = chunk.getWorld().getChunkStore().getStore();
                     ChunkColumn column = store.getComponent(chunk.getReference(), ChunkColumn.getComponentType());
                     Ref<ChunkStore> section = column.getSection(ChunkUtil.chunkCoordinate(by));
                     FluidSection fluidSection = store.ensureAndGetComponent(section, FluidSection.getComponentType());
                     fluidSection.setFluid(bx, by, bz, fluidId, (byte)fluidLevel);
                     BlockType block = blockTypeMap.getAsset(blockId);
                     String blockKey = block.getId();
                     if (filler == 0) {
                        RotationTuple rot = RotationTuple.get(rotation);
                        chunk.placeBlock(bx, by, bz, blockKey, rot.yaw(), rot.pitch(), rot.roll(), 0);
                        if (supportValue != 0) {
                           Ref<ChunkStore> chunkRef = chunk.getReference();
                           store = chunkRef.getStore();
                           column = store.getComponent(chunkRef, ChunkColumn.getComponentType());
                           BlockPhysics.setSupportValue(store, column.getSection(ChunkUtil.chunkCoordinate(by)), bx, by, bz, supportValue);
                        }

                        if (holder != null) {
                           chunk.setState(bx, by, bz, holder.clone());
                        }
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
