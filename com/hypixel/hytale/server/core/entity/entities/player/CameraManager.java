package com.hypixel.hytale.server.core.entity.entities.player;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.ClientCameraView;
import com.hypixel.hytale.protocol.MouseButtonState;
import com.hypixel.hytale.protocol.MouseButtonType;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.EnumMap;
import java.util.Map;
import javax.annotation.Nonnull;

public class CameraManager implements Component<EntityStore> {
   private final Map<MouseButtonType, MouseButtonState> mouseStates = new EnumMap<>(MouseButtonType.class);
   private final Map<MouseButtonType, Vector3i> mousePressedPosition = new EnumMap<>(MouseButtonType.class);
   private final Map<MouseButtonType, Vector3i> mouseReleasedPosition = new EnumMap<>(MouseButtonType.class);
   private Vector2d lastScreenPoint = Vector2d.ZERO;
   private Vector3i lastTargetBlock;

   public static ComponentType<EntityStore, CameraManager> getComponentType() {
      return EntityModule.get().getCameraManagerComponentType();
   }

   public CameraManager() {
   }

   public CameraManager(@Nonnull CameraManager other) {
      this();
      this.lastScreenPoint = other.lastScreenPoint;
      this.lastTargetBlock = other.lastTargetBlock;
   }

   public void resetCamera(@Nonnull PlayerRef ref) {
      ref.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.Custom, false, null));
      this.mouseStates.clear();
   }

   public void handleMouseButtonState(MouseButtonType mouseButtonType, MouseButtonState state, Vector3i targetBlock) {
      this.mouseStates.put(mouseButtonType, state);
      if (state == MouseButtonState.Pressed) {
         this.mousePressedPosition.put(mouseButtonType, targetBlock);
      }

      if (state == MouseButtonState.Released) {
         this.mouseReleasedPosition.put(mouseButtonType, targetBlock);
      }
   }

   public MouseButtonState getMouseButtonState(MouseButtonType mouseButtonType) {
      return this.mouseStates.getOrDefault(mouseButtonType, MouseButtonState.Released);
   }

   public Vector3i getLastMouseButtonPressedPosition(MouseButtonType mouseButtonType) {
      return this.mousePressedPosition.get(mouseButtonType);
   }

   public Vector3i getLastMouseButtonReleasedPosition(MouseButtonType mouseButtonType) {
      return this.mouseReleasedPosition.get(mouseButtonType);
   }

   public void setLastScreenPoint(Vector2d lastScreenPoint) {
      this.lastScreenPoint = lastScreenPoint;
   }

   public Vector2d getLastScreenPoint() {
      return this.lastScreenPoint;
   }

   public void setLastBlockPosition(Vector3i targetBlock) {
      this.lastTargetBlock = targetBlock;
   }

   public Vector3i getLastTargetBlock() {
      return this.lastTargetBlock;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new CameraManager(this);
   }

   @Nonnull
   @Override
   public String toString() {
      return "CameraManager{mouseStates="
         + this.mouseStates
         + ", mousePressedPosition="
         + this.mousePressedPosition
         + ", mouseReleasedPosition="
         + this.mouseReleasedPosition
         + ", lastScreenPoint="
         + this.lastScreenPoint
         + ", lastTargetBlock="
         + this.lastTargetBlock
         + "}";
   }
}
