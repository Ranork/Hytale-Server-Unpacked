package com.hypixel.hytale.builtin.buildertools.tooloperations;

import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.builtin.buildertools.utils.Material;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.block.BlockUtil;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolOnUseInteraction;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import javax.annotation.Nonnull;

public class PaintOperation extends ToolOperation {
   private final int brushDensity;
   private LongOpenHashSet packedPlacedBlockPositions;

   public PaintOperation(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Player player,
      @Nonnull BuilderToolOnUseInteraction packet,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      super(ref, packet, componentAccessor);
      UUIDComponent uuidComponent = componentAccessor.getComponent(ref, UUIDComponent.getComponentType());

      assert uuidComponent != null;

      PrototypePlayerBuilderToolSettings prototypePlayerBuilderToolSettings = ToolOperation.PROTOTYPE_TOOL_SETTINGS.get(uuidComponent.getUuid());
      this.packedPlacedBlockPositions = prototypePlayerBuilderToolSettings.addIgnoredPaintOperation();
      this.brushDensity = this.getBrushDensity();
   }

   @Override
   boolean execute0(int x, int y, int z) {
      if (y >= 0 && y < 320) {
         if (this.edit.getBlock(x, y, z) == 0) {
            this.packedPlacedBlockPositions.add(BlockUtil.pack(x, y, z));
         }

         if (this.random.nextInt(100) <= this.brushDensity) {
            this.edit.setMaterial(x, y, z, Material.fromPattern(this.pattern, this.random));
         }

         return true;
      } else {
         return true;
      }
   }
}
