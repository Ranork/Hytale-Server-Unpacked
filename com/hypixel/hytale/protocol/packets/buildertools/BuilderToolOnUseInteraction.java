package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class BuilderToolOnUseInteraction implements Packet, ToServerPacket {
   public static final int PACKET_ID = 413;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 57;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 57;
   public static final int MAX_SIZE = 57;
   @Nonnull
   public InteractionType type = InteractionType.Primary;
   public int x;
   public int y;
   public int z;
   public int offsetForPaintModeX;
   public int offsetForPaintModeY;
   public int offsetForPaintModeZ;
   public boolean isAltPlaySculptBrushModDown;
   public boolean isHoldDownInteraction;
   public boolean isDoServerRaytraceForPosition;
   public boolean isShowEditNotifications;
   public int maxLengthToolIgnoreHistory;
   public float raycastOriginX;
   public float raycastOriginY;
   public float raycastOriginZ;
   public float raycastDirectionX;
   public float raycastDirectionY;
   public float raycastDirectionZ;

   @Override
   public int getId() {
      return 413;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public BuilderToolOnUseInteraction() {
   }

   public BuilderToolOnUseInteraction(
      @Nonnull InteractionType type,
      int x,
      int y,
      int z,
      int offsetForPaintModeX,
      int offsetForPaintModeY,
      int offsetForPaintModeZ,
      boolean isAltPlaySculptBrushModDown,
      boolean isHoldDownInteraction,
      boolean isDoServerRaytraceForPosition,
      boolean isShowEditNotifications,
      int maxLengthToolIgnoreHistory,
      float raycastOriginX,
      float raycastOriginY,
      float raycastOriginZ,
      float raycastDirectionX,
      float raycastDirectionY,
      float raycastDirectionZ
   ) {
      this.type = type;
      this.x = x;
      this.y = y;
      this.z = z;
      this.offsetForPaintModeX = offsetForPaintModeX;
      this.offsetForPaintModeY = offsetForPaintModeY;
      this.offsetForPaintModeZ = offsetForPaintModeZ;
      this.isAltPlaySculptBrushModDown = isAltPlaySculptBrushModDown;
      this.isHoldDownInteraction = isHoldDownInteraction;
      this.isDoServerRaytraceForPosition = isDoServerRaytraceForPosition;
      this.isShowEditNotifications = isShowEditNotifications;
      this.maxLengthToolIgnoreHistory = maxLengthToolIgnoreHistory;
      this.raycastOriginX = raycastOriginX;
      this.raycastOriginY = raycastOriginY;
      this.raycastOriginZ = raycastOriginZ;
      this.raycastDirectionX = raycastDirectionX;
      this.raycastDirectionY = raycastDirectionY;
      this.raycastDirectionZ = raycastDirectionZ;
   }

   public BuilderToolOnUseInteraction(@Nonnull BuilderToolOnUseInteraction other) {
      this.type = other.type;
      this.x = other.x;
      this.y = other.y;
      this.z = other.z;
      this.offsetForPaintModeX = other.offsetForPaintModeX;
      this.offsetForPaintModeY = other.offsetForPaintModeY;
      this.offsetForPaintModeZ = other.offsetForPaintModeZ;
      this.isAltPlaySculptBrushModDown = other.isAltPlaySculptBrushModDown;
      this.isHoldDownInteraction = other.isHoldDownInteraction;
      this.isDoServerRaytraceForPosition = other.isDoServerRaytraceForPosition;
      this.isShowEditNotifications = other.isShowEditNotifications;
      this.maxLengthToolIgnoreHistory = other.maxLengthToolIgnoreHistory;
      this.raycastOriginX = other.raycastOriginX;
      this.raycastOriginY = other.raycastOriginY;
      this.raycastOriginZ = other.raycastOriginZ;
      this.raycastDirectionX = other.raycastDirectionX;
      this.raycastDirectionY = other.raycastDirectionY;
      this.raycastDirectionZ = other.raycastDirectionZ;
   }

   @Nonnull
   public static BuilderToolOnUseInteraction deserialize(@Nonnull ByteBuf buf, int offset) {
      BuilderToolOnUseInteraction obj = new BuilderToolOnUseInteraction();
      obj.type = InteractionType.fromValue(buf.getByte(offset + 0));
      obj.x = buf.getIntLE(offset + 1);
      obj.y = buf.getIntLE(offset + 5);
      obj.z = buf.getIntLE(offset + 9);
      obj.offsetForPaintModeX = buf.getIntLE(offset + 13);
      obj.offsetForPaintModeY = buf.getIntLE(offset + 17);
      obj.offsetForPaintModeZ = buf.getIntLE(offset + 21);
      obj.isAltPlaySculptBrushModDown = buf.getByte(offset + 25) != 0;
      obj.isHoldDownInteraction = buf.getByte(offset + 26) != 0;
      obj.isDoServerRaytraceForPosition = buf.getByte(offset + 27) != 0;
      obj.isShowEditNotifications = buf.getByte(offset + 28) != 0;
      obj.maxLengthToolIgnoreHistory = buf.getIntLE(offset + 29);
      obj.raycastOriginX = buf.getFloatLE(offset + 33);
      obj.raycastOriginY = buf.getFloatLE(offset + 37);
      obj.raycastOriginZ = buf.getFloatLE(offset + 41);
      obj.raycastDirectionX = buf.getFloatLE(offset + 45);
      obj.raycastDirectionY = buf.getFloatLE(offset + 49);
      obj.raycastDirectionZ = buf.getFloatLE(offset + 53);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 57;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.x);
      buf.writeIntLE(this.y);
      buf.writeIntLE(this.z);
      buf.writeIntLE(this.offsetForPaintModeX);
      buf.writeIntLE(this.offsetForPaintModeY);
      buf.writeIntLE(this.offsetForPaintModeZ);
      buf.writeByte(this.isAltPlaySculptBrushModDown ? 1 : 0);
      buf.writeByte(this.isHoldDownInteraction ? 1 : 0);
      buf.writeByte(this.isDoServerRaytraceForPosition ? 1 : 0);
      buf.writeByte(this.isShowEditNotifications ? 1 : 0);
      buf.writeIntLE(this.maxLengthToolIgnoreHistory);
      buf.writeFloatLE(this.raycastOriginX);
      buf.writeFloatLE(this.raycastOriginY);
      buf.writeFloatLE(this.raycastOriginZ);
      buf.writeFloatLE(this.raycastDirectionX);
      buf.writeFloatLE(this.raycastDirectionY);
      buf.writeFloatLE(this.raycastDirectionZ);
   }

   @Override
   public int computeSize() {
      return 57;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 57 ? ValidationResult.error("Buffer too small: expected at least 57 bytes") : ValidationResult.OK;
   }

   public BuilderToolOnUseInteraction clone() {
      BuilderToolOnUseInteraction copy = new BuilderToolOnUseInteraction();
      copy.type = this.type;
      copy.x = this.x;
      copy.y = this.y;
      copy.z = this.z;
      copy.offsetForPaintModeX = this.offsetForPaintModeX;
      copy.offsetForPaintModeY = this.offsetForPaintModeY;
      copy.offsetForPaintModeZ = this.offsetForPaintModeZ;
      copy.isAltPlaySculptBrushModDown = this.isAltPlaySculptBrushModDown;
      copy.isHoldDownInteraction = this.isHoldDownInteraction;
      copy.isDoServerRaytraceForPosition = this.isDoServerRaytraceForPosition;
      copy.isShowEditNotifications = this.isShowEditNotifications;
      copy.maxLengthToolIgnoreHistory = this.maxLengthToolIgnoreHistory;
      copy.raycastOriginX = this.raycastOriginX;
      copy.raycastOriginY = this.raycastOriginY;
      copy.raycastOriginZ = this.raycastOriginZ;
      copy.raycastDirectionX = this.raycastDirectionX;
      copy.raycastDirectionY = this.raycastDirectionY;
      copy.raycastDirectionZ = this.raycastDirectionZ;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BuilderToolOnUseInteraction other)
            ? false
            : Objects.equals(this.type, other.type)
               && this.x == other.x
               && this.y == other.y
               && this.z == other.z
               && this.offsetForPaintModeX == other.offsetForPaintModeX
               && this.offsetForPaintModeY == other.offsetForPaintModeY
               && this.offsetForPaintModeZ == other.offsetForPaintModeZ
               && this.isAltPlaySculptBrushModDown == other.isAltPlaySculptBrushModDown
               && this.isHoldDownInteraction == other.isHoldDownInteraction
               && this.isDoServerRaytraceForPosition == other.isDoServerRaytraceForPosition
               && this.isShowEditNotifications == other.isShowEditNotifications
               && this.maxLengthToolIgnoreHistory == other.maxLengthToolIgnoreHistory
               && this.raycastOriginX == other.raycastOriginX
               && this.raycastOriginY == other.raycastOriginY
               && this.raycastOriginZ == other.raycastOriginZ
               && this.raycastDirectionX == other.raycastDirectionX
               && this.raycastDirectionY == other.raycastDirectionY
               && this.raycastDirectionZ == other.raycastDirectionZ;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.type,
         this.x,
         this.y,
         this.z,
         this.offsetForPaintModeX,
         this.offsetForPaintModeY,
         this.offsetForPaintModeZ,
         this.isAltPlaySculptBrushModDown,
         this.isHoldDownInteraction,
         this.isDoServerRaytraceForPosition,
         this.isShowEditNotifications,
         this.maxLengthToolIgnoreHistory,
         this.raycastOriginX,
         this.raycastOriginY,
         this.raycastOriginZ,
         this.raycastDirectionX,
         this.raycastDirectionY,
         this.raycastDirectionZ
      );
   }
}
