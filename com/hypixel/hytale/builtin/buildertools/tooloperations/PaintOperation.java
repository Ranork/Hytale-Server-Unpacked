package com.hypixel.hytale.builtin.buildertools.tooloperations;

import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.builtin.buildertools.tooloperations.transform.Transform;
import com.hypixel.hytale.builtin.buildertools.utils.Material;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.block.BlockUtil;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolOnUseInteraction;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import javax.annotation.Nonnull;
import org.joml.Vector3i;

public class PaintOperation extends ToolOperation {
   private final Transform brushRotation;
   private final BlockPattern effectivePattern;
   private LongOpenHashSet packedPlacedBlockPositions;

   public PaintOperation(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Player player,
      @Nonnull PlayerRef playerRef,
      @Nonnull BuilderToolOnUseInteraction packet,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      super(ref, packet, componentAccessor);
      if (this.interactionType == InteractionType.Primary) {
         this.effectivePattern = BlockPattern.EMPTY;
      } else if (this.pattern.equals(BlockPattern.EMPTY)) {
         this.effectivePattern = BlockPattern.parse("Rock_Stone");
      } else {
         this.effectivePattern = this.pattern;
      }

      UUIDComponent uuidComponent = componentAccessor.getComponent(ref, UUIDComponent.getComponentType());

      assert uuidComponent != null;

      PrototypePlayerBuilderToolSettings prototypePlayerBuilderToolSettings = ToolOperation.PROTOTYPE_TOOL_SETTINGS.get(uuidComponent.getUuid());
      this.packedPlacedBlockPositions = prototypePlayerBuilderToolSettings.addIgnoredPaintOperation();
      this.brushRotation = this.getBrushRotation(componentAccessor);
   }

   @Override
   protected boolean executeBlock(int x, int y, int z) {
      Vector3i vector = new Vector3i(x - this.currentCenterX, y - this.currentCenterY, z - this.currentCenterZ);
      this.brushRotation.apply(vector);
      x = this.currentCenterX + vector.x;
      y = this.currentCenterY + vector.y;
      z = this.currentCenterZ + vector.z;
      if (y >= 0 && y < 320) {
         if (this.edit.getBlock(x, y, z) == 0) {
            this.packedPlacedBlockPositions.add(BlockUtil.pack(x, y, z));
         }

         if (this.random.nextInt(100) <= this.density) {
            this.edit.setMaterial(x, y, z, Material.fromPattern(this.effectivePattern, this.random));
         }

         return true;
      } else {
         return true;
      }
   }
}
