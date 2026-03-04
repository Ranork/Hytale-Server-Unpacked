package com.hypixel.hytale.builtin.buildertools.tooloperations;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.builtin.buildertools.utils.Material;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.block.BlockUtil;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolOnUseInteraction;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import javax.annotation.Nonnull;

public class SculptOperation extends ToolOperation {
   private final int smoothVolume;
   private final int smoothRadius;
   private final boolean isAltPlaySculptBrushModDown;
   private final int brushDensity;
   private LongOpenHashSet packedPlacedBlockPositions;

   public SculptOperation(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Player player,
      @Nonnull BuilderToolOnUseInteraction packet,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      super(ref, packet, componentAccessor);
      UUIDComponent uuidComponent = componentAccessor.getComponent(ref, UUIDComponent.getComponentType());

      assert uuidComponent != null;

      this.isAltPlaySculptBrushModDown = packet.isAltPlaySculptBrushModDown;
      PrototypePlayerBuilderToolSettings prototypePlayerBuilderToolSettings = ToolOperation.PROTOTYPE_TOOL_SETTINGS.get(uuidComponent.getUuid());
      this.packedPlacedBlockPositions = prototypePlayerBuilderToolSettings.addIgnoredPaintOperation();
      int smoothStrength = (Integer)this.args.tool().get("SmoothStrength");
      this.smoothRadius = Math.min(smoothStrength, 4);
      int smoothRange = this.smoothRadius * 2 + 1;
      this.smoothVolume = smoothRange * smoothRange * smoothRange;
      this.brushDensity = this.getBrushDensity();
   }

   @Override
   boolean execute0(int x, int y, int z) {
      int currentBlock = this.edit.getBlock(x, y, z);
      if (this.isAltPlaySculptBrushModDown) {
         BuilderToolsPlugin.BuilderState.BlocksSampleData data = BuilderToolsPlugin.getState(this.player, this.player.getPlayerRef())
            .getBlocksSampleData(this.edit.getAccessor(), x, y, z, 2);
         if (currentBlock != data.mainBlock && data.mainBlockCount > this.smoothVolume * 0.5F) {
            this.edit.setMaterial(x, y, z, Material.block(data.mainBlock));
         }
      } else if (this.interactionType == InteractionType.Primary) {
         if (currentBlock > 0 && this.builderState.isAsideAir(this.edit.getAccessor(), x, y, z) && this.random.nextInt(100) <= this.brushDensity) {
            this.edit.setMaterial(x, y, z, Material.EMPTY);
         }
      } else if (this.interactionType == InteractionType.Secondary && currentBlock <= 0 && this.builderState.isAsideBlock(this.edit.getAccessor(), x, y, z)) {
         if (this.edit.getBlock(x, y, z) == 0) {
            this.packedPlacedBlockPositions.add(BlockUtil.pack(x, y, z));
         }

         if (this.random.nextInt(100) <= this.brushDensity) {
            Material material = Material.fromPattern(this.pattern, this.random);
            if (material.isEmpty()) {
               BuilderToolsPlugin.BuilderState.BlocksSampleData data = this.builderState.getBlocksSampleData(this.edit.getAccessor(), x, y, z, 1);
               material = Material.block(data.mainBlockNotAir);
            }

            this.edit.setMaterial(x, y, z, material);
         }
      }

      return true;
   }
}
