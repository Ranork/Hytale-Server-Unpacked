package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
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
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.BuilderTool;
import com.hypixel.hytale.server.core.codec.LayerEntryCodec;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.accessor.BlockAccessor;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LayerOperation extends SequenceBrushOperation {
   public static final BuilderCodec<LayerOperation> CODEC = BuilderCodec.builder(LayerOperation.class, LayerOperation::new)
      .append(
         new KeyedCodec<>("Layers", new ArrayCodec<>(LayerEntryCodec.CODEC, LayerEntryCodec[]::new)),
         (op, val) -> op.layerArgs = (List<LayerEntryCodec>)(val != null ? new ArrayList<>(Arrays.asList(val)) : List.of()),
         op -> op.layerArgs != null ? op.layerArgs.toArray(new LayerEntryCodec[0]) : new LayerEntryCodec[0]
      )
      .documentation("The layers to set")
      .add()
      .documentation("Replace blocks according to the specified layers in terms of their depth from the nearest air block")
      .build();
   private List<LayerEntryCodec> layerArgs = new ArrayList<>();

   public LayerOperation() {
      super("Layer", "Replace blocks according to the specified layers in terms of their depth from the nearest air block", true);
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
      Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      PlayerRef playerRefComponent = componentAccessor.getComponent(ref, PlayerRef.getComponentType());

      assert playerRefComponent != null;

      BuilderToolsPlugin.BuilderState builderState = BuilderToolsPlugin.getState(playerComponent, playerRefComponent);
      if (edit.getBlock(x, y, z) <= 0) {
         return true;
      } else {
         Map<String, Object> toolArgs = this.getToolArgs(ref, componentAccessor);
         List<Pair<Integer, String>> layers = new ArrayList<>();
         int maxDepth = 0;

         for (LayerEntryCodec layer : this.layerArgs) {
            if (!layer.isSkip()) {
               maxDepth += layer.getDepth();
               layers.add(Pair.of(layer.getDepth(), this.resolveBlockPattern(layer, toolArgs, brushConfig)));
            }
         }

         BlockAccessor chunk = edit.getAccessor().getChunk(ChunkUtil.indexChunkFromBlock(x, z));
         builderState.layer(x, y, z, layers, maxDepth, Vector3i.DOWN, (WorldChunk)chunk, edit.getBefore(), edit.getAfter());
         return true;
      }
   }

   private String resolveBlockPattern(LayerEntryCodec entry, @Nullable Map<String, Object> toolArgs, BrushConfig brushConfig) {
      if (entry.isUseToolArg()) {
         if (toolArgs != null && toolArgs.containsKey(entry.getMaterial())) {
            if (toolArgs.get(entry.getMaterial()) instanceof BlockPattern blockPattern) {
               return blockPattern.toString();
            } else {
               brushConfig.setErrorFlag("Layer: Tool arg '" + entry.getMaterial() + "' is not a Block type");
               return "";
            }
         } else {
            brushConfig.setErrorFlag("Layer: Tool arg '" + entry.getMaterial() + "' not found");
            return "";
         }
      } else {
         return entry.getMaterial();
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
