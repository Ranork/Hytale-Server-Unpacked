package com.hypixel.hytale.builtin.buildertools.tooloperations;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolOnUseInteraction;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.util.ColorParseUtil;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class TintOperation extends ToolOperation {
   private final int tintColor;

   public TintOperation(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Player player,
      @Nonnull BuilderToolOnUseInteraction packet,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      super(ref, packet, componentAccessor);
      String colorText = (String)this.args.tool().get("TintColor");

      try {
         this.tintColor = ColorParseUtil.hexStringToRGBInt(colorText);
      } catch (NumberFormatException var7) {
         player.sendMessage(Message.translation("server.builderTools.tintOperation.colorParseError").param("value", colorText));
         throw var7;
      }
   }

   @Override
   public void execute(ComponentAccessor<EntityStore> componentAccessor) {
      this.builderState.tint(this.x, this.y, this.z, this.tintColor, this.shape, this.shapeRange, this.shapeHeight, componentAccessor);
   }

   @Override
   public void executeAt(int posX, int posY, int posZ, ComponentAccessor<EntityStore> componentAccessor) {
      this.builderState.tint(posX, posY, posZ, this.tintColor, this.shape, this.shapeRange, this.shapeHeight, componentAccessor);
   }

   @Override
   boolean execute0(int x, int y, int z) {
      return true;
   }
}
