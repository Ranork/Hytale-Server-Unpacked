package com.hypixel.hytale.builtin.buildertools.tooloperations;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolOnUseInteraction;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class SmootherOperation extends ToolOperation {
   private final float strength;

   public SmootherOperation(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Player player,
      @Nonnull BuilderToolOnUseInteraction packet,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      super(ref, packet, componentAccessor);
      boolean removing = packet.type == InteractionType.Primary;
      int baseStrength = removing ? (Integer)this.args.tool().get("RemoveStrength") : (Integer)this.args.tool().get("AddStrength");
      this.strength = 1.0F + (removing ? baseStrength : -baseStrength) * 0.01F;
   }

   @Override
   boolean execute0(int x, int y, int z) {
      int currentBlock = this.edit.getBlock(x, y, z);
      BuilderToolsPlugin.BuilderState.SmoothSampleData data = this.builderState.getBlocksSmoothData(this.edit.getAccessor(), x, y, z);
      if (data.solidStrength > this.strength) {
         if (currentBlock != data.solidBlock) {
            this.edit.setBlock(x, y, z, data.solidBlock);
         }
      } else if (currentBlock != data.fillerBlock) {
         this.edit.setBlock(x, y, z, data.fillerBlock);
      }

      return true;
   }
}
