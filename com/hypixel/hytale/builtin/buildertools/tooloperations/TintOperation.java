package com.hypixel.hytale.builtin.buildertools.tooloperations;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.block.BlockUtil;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolOnUseInteraction;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.BuilderTool;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.args.ToolArgException;
import com.hypixel.hytale.server.core.asset.util.ColorParseUtil;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import javax.annotation.Nonnull;

public class TintOperation extends ToolOperation {
   private int tintColor;
   private double opacity;
   private boolean blendMode = false;
   private int bufferOriginX;
   private int bufferOriginZ;
   private int[][] colorBuffer;
   private final boolean isHoldingAltModeDown;
   private LongOpenHashSet packedPlacedTinsPositions;
   private static final int SAMPLE_DISTANCE = 4;

   public TintOperation(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Player player,
      @Nonnull BuilderToolOnUseInteraction packet,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      super(ref, packet, componentAccessor);
      this.isHoldingAltModeDown = packet.isAltPlaySculptBrushModDown;
      if (this.interactionType == InteractionType.Primary) {
         this.blendMode = true;
      }

      if (this.blendMode && this.isHoldingAltModeDown) {
         int sampledTint = this.edit.getTint(this.x, this.z);
         String hexColor = ColorParseUtil.toHexString(sampledTint & 16777215);
         InventoryComponent.Hotbar hotbar = componentAccessor.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
         ItemStack itemStack = hotbar.getActiveItem();
         BuilderTool builderTool = BuilderTool.getActiveBuilderTool(player);

         try {
            ItemStack newItemStack = builderTool.updateArgMetadata(itemStack, "bTintColor", hexColor);
            hotbar.getInventory().setItemStackForSlot(hotbar.getActiveSlot(), newItemStack);
            BuilderToolsPlugin.sendFeedback(
               Message.translation("server.builderTools.pickedColor").param("color", hexColor), player, NotificationStyle.Success, componentAccessor
            );
         } catch (ToolArgException var11) {
            player.sendMessage(Message.translation("server.builderTools.tintOperation.colorParseError").param("value", hexColor));
         }
      } else {
         String colorText = (String)this.args.tool().getOrDefault("bTintColor", "ffffff");

         try {
            this.tintColor = ColorParseUtil.hexStringToRGBInt(colorText);
         } catch (NumberFormatException var12) {
            player.sendMessage(Message.translation("server.builderTools.tintOperation.colorParseError").param("value", colorText));
            throw var12;
         }

         this.opacity = ((Integer)this.args.tool().getOrDefault("cOpacity", 0)).intValue() / 100.0;
         UUIDComponent uuidComponent = ref.getStore().getComponent(ref, UUIDComponent.getComponentType());
         PrototypePlayerBuilderToolSettings prototypeSettings = PROTOTYPE_TOOL_SETTINGS.get(uuidComponent.getUuid());
         if (!packet.isHoldDownInteraction) {
            prototypeSettings.getIgnoredPaintOperations().clear();
         }

         this.packedPlacedTinsPositions = prototypeSettings.addIgnoredPaintOperation();

         for (LongOpenHashSet previousSet : prototypeSettings.getIgnoredPaintOperations()) {
            if (previousSet != this.packedPlacedTinsPositions) {
               this.packedPlacedTinsPositions.addAll(previousSet);
            }
         }

         if (this.blendMode) {
            int bufferSize = (this.shapeRange + 4) * 2 + 1;
            this.bufferOriginX = this.x - this.shapeRange - 4;
            this.bufferOriginZ = this.z - this.shapeRange - 4;
            this.colorBuffer = new int[bufferSize][bufferSize];

            for (int bufferX = 0; bufferX < bufferSize; bufferX++) {
               for (int bufferZ = 0; bufferZ < bufferSize; bufferZ++) {
                  this.colorBuffer[bufferX][bufferZ] = this.edit.getTint(this.bufferOriginX + bufferX, this.bufferOriginZ + bufferZ);
               }
            }
         }
      }
   }

   @Override
   boolean execute0(int x, int y, int z) {
      if (this.isHoldingAltModeDown && this.blendMode) {
         return true;
      } else {
         long packed = BlockUtil.pack(x, 0, z);
         if (this.packedPlacedTinsPositions.contains(packed)) {
            return true;
         } else {
            this.packedPlacedTinsPositions.add(packed);
            if (this.blendMode) {
               int targetColor = this.sampleKernelBlend(x, z);
               this.edit.setTint(x, z, targetColor, 0.0);
            } else {
               this.edit.setTint(x, z, this.tintColor, this.opacity);
            }

            return true;
         }
      }
   }

   private int sampleKernelBlend(int x, int z) {
      double totalWeight = 0.0;
      double r = 0.0;
      double g = 0.0;
      double b = 0.0;

      for (int deltaX = -4; deltaX <= 4; deltaX++) {
         for (int deltaZ = -4; deltaZ <= 4; deltaZ++) {
            double dist = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
            if (!(dist > 4.0)) {
               int bufferX = x + deltaX - this.bufferOriginX;
               int bufferZ = z + deltaZ - this.bufferOriginZ;
               if (bufferX >= 0 && bufferZ >= 0 && bufferX < this.colorBuffer.length && bufferZ < this.colorBuffer[0].length) {
                  double sigma = 2.0;
                  double weight = Math.exp(-(dist * dist) / (2.0 * sigma * sigma));
                  int color = this.colorBuffer[bufferX][bufferZ];
                  r += (color >> 16 & 0xFF) * weight;
                  g += (color >> 8 & 0xFF) * weight;
                  b += (color & 0xFF) * weight;
                  totalWeight += weight;
               }
            }
         }
      }

      return totalWeight == 0.0
         ? this.tintColor
         : (int)Math.round(r / totalWeight) << 16 | (int)Math.round(g / totalWeight) << 8 | (int)Math.round(b / totalWeight);
   }
}
