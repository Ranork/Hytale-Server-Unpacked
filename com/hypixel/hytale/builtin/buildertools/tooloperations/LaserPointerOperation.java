package com.hypixel.hytale.builtin.buildertools.tooloperations;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolLaserPointer;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolOnUseInteraction;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.util.ColorParseUtil;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.world.PlayerUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import javax.annotation.Nonnull;

public class LaserPointerOperation extends ToolOperation {
   private static final double MAX_DISTANCE = 128.0;

   public LaserPointerOperation(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Player player,
      @Nonnull BuilderToolOnUseInteraction packet,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      super(ref, packet, componentAccessor);
      String colorText = (String)this.args.tool().get("LaserColor");

      int laserColor;
      try {
         laserColor = ColorParseUtil.hexStringToRGBInt(colorText);
      } catch (NumberFormatException var18) {
         player.sendMessage(Message.translation("server.builderTools.laserPointer.colorParseError").param("value", colorText));
         throw var18;
      }

      Object durationObj = this.args.tool().get("Duration");
      int duration;
      if (durationObj instanceof Integer) {
         duration = (Integer)durationObj;
      } else if (durationObj instanceof String) {
         try {
            duration = Integer.parseInt((String)durationObj);
         } catch (NumberFormatException var17) {
            player.sendMessage(Message.translation("server.builderTools.laserPointer.durationParseError").param("value", String.valueOf(durationObj)));
            throw var17;
         }
      } else {
         duration = 300;
      }

      NetworkId networkIdComponent = componentAccessor.getComponent(ref, NetworkId.getComponentType());

      assert networkIdComponent != null;

      int playerNetworkId = networkIdComponent.getId();
      Transform lookVec = TargetUtil.getLook(ref, componentAccessor);
      Vector3d lookVecPosition = lookVec.getPosition();
      Vector3d lookVecDirection = lookVec.getDirection();
      Vector3d hitLocation = TargetUtil.getTargetLocation(ref, blockId -> blockId != 0, 128.0, componentAccessor);
      Vector3d endLocation = hitLocation != null ? hitLocation : lookVecPosition.add(lookVecDirection.scale(128.0));
      BuilderToolLaserPointer laserPacket = new BuilderToolLaserPointer();
      laserPacket.playerNetworkId = playerNetworkId;
      laserPacket.startX = (float)lookVecPosition.x;
      laserPacket.startY = (float)lookVecPosition.y;
      laserPacket.startZ = (float)lookVecPosition.z;
      laserPacket.endX = (float)endLocation.x;
      laserPacket.endY = (float)endLocation.y;
      laserPacket.endZ = (float)endLocation.z;
      laserPacket.color = laserColor;
      laserPacket.durationMs = duration;
      PlayerUtil.broadcastPacketToPlayers(componentAccessor, laserPacket);
   }

   @Override
   public void execute(ComponentAccessor<EntityStore> componentAccessor) {
   }

   @Override
   boolean execute0(int x, int y, int z) {
      return false;
   }
}
