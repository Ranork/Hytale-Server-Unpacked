package com.hypixel.hytale.builtin.buildertools.tooloperations;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolOnUseInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class SmoothOperation extends ToolOperation {
   private final int smoothVolume;

   public SmoothOperation(@Nonnull Ref<EntityStore> ref, @Nonnull BuilderToolOnUseInteraction packet, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      super(ref, packet, componentAccessor);
      int smoothStrength = (Integer)this.args.tool().get("SmoothStrength");
      int smoothRange = Math.min(smoothStrength, 4) * 2 + 1;
      this.smoothVolume = smoothRange * smoothRange * smoothRange;
   }

   @Override
   boolean execute0(int x, int y, int z) {
      int currentBlock = this.edit.getBlock(x, y, z);
      BuilderToolsPlugin.BuilderState.BlocksSampleData data = BuilderToolsPlugin.getState(this.player, this.player.getPlayerRef())
         .getBlocksSampleData(this.edit.getAccessor(), x, y, z, 2);
      if (currentBlock != data.mainBlock && data.mainBlockCount > this.smoothVolume * 0.5F) {
         this.edit.setBlock(x, y, z, data.mainBlock);
      }

      return true;
   }
}
