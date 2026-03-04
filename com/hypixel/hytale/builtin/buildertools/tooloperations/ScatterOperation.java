package com.hypixel.hytale.builtin.buildertools.tooloperations;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolOnUseInteraction;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ScatterOperation extends ToolOperation {
   private final int brushDensity = this.getBrushDensity();
   private final BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();

   public ScatterOperation(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Player player,
      @Nonnull BuilderToolOnUseInteraction packet,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      super(ref, packet, componentAccessor);
   }

   @Override
   boolean execute0(int x, int y, int z) {
      int currentBlock = this.edit.getBlock(x, y, z);
      if (currentBlock <= 0
         && this.builderState.isAsideBlock(this.edit.getAccessor(), x, y, z)
         && this.assetMap.getAsset(this.edit.getBlock(x, y - 1, z)).getFlags().isStackable
         && this.random.nextInt(100) <= this.brushDensity) {
         this.edit.setBlock(x, y, z, this.pattern.nextBlock(this.random));
      }

      return true;
   }
}
