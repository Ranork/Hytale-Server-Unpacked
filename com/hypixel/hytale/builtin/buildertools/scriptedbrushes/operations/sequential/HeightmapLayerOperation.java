package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigEditStore;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.BuilderTool;
import com.hypixel.hytale.server.core.codec.LayerEntryCodec;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HeightmapLayerOperation extends SequenceBrushOperation {
   public static final BuilderCodec<HeightmapLayerOperation> CODEC = BuilderCodec.builder(HeightmapLayerOperation.class, HeightmapLayerOperation::new)
      .append(
         new KeyedCodec<>("Layers", new ArrayCodec<>(LayerEntryCodec.CODEC, LayerEntryCodec[]::new)),
         (op, val) -> op.layerArgs = (List<LayerEntryCodec>)(val != null ? new ArrayList<>(Arrays.asList(val)) : List.of()),
         op -> op.layerArgs != null ? op.layerArgs.toArray(new LayerEntryCodec[0]) : new LayerEntryCodec[0]
      )
      .documentation("The layers to set")
      .add()
      .documentation("Replace blocks according to the specified layers in terms of their depth from the tallest block in its column")
      .build();
   private List<LayerEntryCodec> layerArgs = new ArrayList<>();

   public HeightmapLayerOperation() {
      super("Heightmap Layer", "Replace blocks according to the specified layers in terms of their depth from the tallest block in its column", true);
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
      WorldChunk chunk = edit.getAccessor().getChunk(ChunkUtil.indexChunkFromBlock(x, z));
      int depth = chunk.getHeight(x, z) - y;
      if (depth >= 0 && edit.getBlock(x, y, z) > 0) {
         Map<String, Object> toolArgs = this.getToolArgs(ref, componentAccessor);
         int depthTestingAt = 0;

         for (LayerEntryCodec entry : this.layerArgs) {
            depthTestingAt += entry.getDepth();
            if (depth < depthTestingAt) {
               if (entry.isSkip()) {
                  return true;
               }

               int blockId = this.resolveBlockId(entry, toolArgs, brushConfig);
               if (blockId >= 0) {
                  edit.setBlock(x, y, z, blockId);
               }

               return true;
            }
         }

         return true;
      } else {
         return true;
      }
   }

   private int resolveBlockId(LayerEntryCodec entry, @Nullable Map<String, Object> toolArgs, BrushConfig brushConfig) {
      if (entry.isUseToolArg()) {
         if (toolArgs != null && toolArgs.containsKey(entry.getMaterial())) {
            if (toolArgs.get(entry.getMaterial()) instanceof BlockPattern blockPattern) {
               return blockPattern.nextBlock(brushConfig.getRandom());
            } else {
               brushConfig.setErrorFlag("HeightmapLayer: Tool arg '" + entry.getMaterial() + "' is not a Block type");
               return -1;
            }
         } else {
            brushConfig.setErrorFlag("HeightmapLayer: Tool arg '" + entry.getMaterial() + "' not found");
            return -1;
         }
      } else {
         return BlockType.getAssetMap().getIndex(entry.getMaterial());
      }
   }

   @Nullable
   private Map<String, Object> getToolArgs(Ref<EntityStore> ref, ComponentAccessor<EntityStore> componentAccessor) {
      Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());
      if (playerComponent == null) {
         return null;
      } else {
         BuilderTool builderTool = BuilderTool.getActiveBuilderTool(playerComponent);
         if (builderTool == null) {
            return null;
         } else {
            ItemStack itemStack = playerComponent.getInventory().getItemInHand();
            if (itemStack == null) {
               return null;
            } else {
               BuilderTool.ArgData argData = builderTool.getItemArgData(itemStack);
               return argData.tool();
            }
         }
      }
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
   }
}
