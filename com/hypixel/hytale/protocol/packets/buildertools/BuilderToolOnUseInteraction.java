package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class BuilderToolOnUseInteraction implements Packet, ToServerPacket {
   public static final int PACKET_ID = 413;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 60;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 60;
   public static final int MAX_SIZE = 60;
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
   public int maxLengthToolIgnoreHistory;
   public float raycastOriginX;
   public float raycastOriginY;
   public float raycastOriginZ;
   public float raycastDirectionX;
   public float raycastDirectionY;
   public float raycastDirectionZ;
   public int undoGroupSize;

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
      int maxLengthToolIgnoreHistory,
      float raycastOriginX,
      float raycastOriginY,
      float raycastOriginZ,
      float raycastDirectionX,
      float raycastDirectionY,
      float raycastDirectionZ,
      int undoGroupSize
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
      this.maxLengthToolIgnoreHistory = maxLengthToolIgnoreHistory;
      this.raycastOriginX = raycastOriginX;
      this.raycastOriginY = raycastOriginY;
      this.raycastOriginZ = raycastOriginZ;
      this.raycastDirectionX = raycastDirectionX;
      this.raycastDirectionY = raycastDirectionY;
      this.raycastDirectionZ = raycastDirectionZ;
      this.undoGroupSize = undoGroupSize;
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
      this.maxLengthToolIgnoreHistory = other.maxLengthToolIgnoreHistory;
      this.raycastOriginX = other.raycastOriginX;
      this.raycastOriginY = other.raycastOriginY;
      this.raycastOriginZ = other.raycastOriginZ;
      this.raycastDirectionX = other.raycastDirectionX;
      this.raycastDirectionY = other.raycastDirectionY;
      this.raycastDirectionZ = other.raycastDirectionZ;
      this.undoGroupSize = other.undoGroupSize;
   }

   @Nonnull
   public static BuilderToolOnUseInteraction deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 60) {
         throw ProtocolException.bufferTooSmall("BuilderToolOnUseInteraction", 60, buf.readableBytes() - offset);
      } else {
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
         obj.maxLengthToolIgnoreHistory = buf.getIntLE(offset + 28);
         obj.raycastOriginX = buf.getFloatLE(offset + 32);
         obj.raycastOriginY = buf.getFloatLE(offset + 36);
         obj.raycastOriginZ = buf.getFloatLE(offset + 40);
         obj.raycastDirectionX = buf.getFloatLE(offset + 44);
         obj.raycastDirectionY = buf.getFloatLE(offset + 48);
         obj.raycastDirectionZ = buf.getFloatLE(offset + 52);
         obj.undoGroupSize = buf.getIntLE(offset + 56);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 60;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 60L;
   }

   public static InteractionType getType(MemorySegment mem) {
      return getType(mem, 0);
   }

   public static InteractionType getType(MemorySegment mem, int offset) {
      return InteractionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0)));
   }

   public static int getX(MemorySegment mem) {
      return getX(mem, 0);
   }

   public static int getX(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static int getY(MemorySegment mem) {
      return getY(mem, 0);
   }

   public static int getY(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 5));
   }

   public static int getZ(MemorySegment mem) {
      return getZ(mem, 0);
   }

   public static int getZ(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 9));
   }

   public static int getOffsetForPaintModeX(MemorySegment mem) {
      return getOffsetForPaintModeX(mem, 0);
   }

   public static int getOffsetForPaintModeX(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 13));
   }

   public static int getOffsetForPaintModeY(MemorySegment mem) {
      return getOffsetForPaintModeY(mem, 0);
   }

   public static int getOffsetForPaintModeY(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 17));
   }

   public static int getOffsetForPaintModeZ(MemorySegment mem) {
      return getOffsetForPaintModeZ(mem, 0);
   }

   public static int getOffsetForPaintModeZ(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 21));
   }

   public static boolean getIsAltPlaySculptBrushModDown(MemorySegment mem) {
      return getIsAltPlaySculptBrushModDown(mem, 0);
   }

   public static boolean getIsAltPlaySculptBrushModDown(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 25));
   }

   public static boolean getIsHoldDownInteraction(MemorySegment mem) {
      return getIsHoldDownInteraction(mem, 0);
   }

   public static boolean getIsHoldDownInteraction(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 26));
   }

   public static boolean getIsDoServerRaytraceForPosition(MemorySegment mem) {
      return getIsDoServerRaytraceForPosition(mem, 0);
   }

   public static boolean getIsDoServerRaytraceForPosition(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 27));
   }

   public static int getMaxLengthToolIgnoreHistory(MemorySegment mem) {
      return getMaxLengthToolIgnoreHistory(mem, 0);
   }

   public static int getMaxLengthToolIgnoreHistory(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 28));
   }

   public static float getRaycastOriginX(MemorySegment mem) {
      return getRaycastOriginX(mem, 0);
   }

   public static float getRaycastOriginX(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 32));
   }

   public static float getRaycastOriginY(MemorySegment mem) {
      return getRaycastOriginY(mem, 0);
   }

   public static float getRaycastOriginY(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 36));
   }

   public static float getRaycastOriginZ(MemorySegment mem) {
      return getRaycastOriginZ(mem, 0);
   }

   public static float getRaycastOriginZ(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 40));
   }

   public static float getRaycastDirectionX(MemorySegment mem) {
      return getRaycastDirectionX(mem, 0);
   }

   public static float getRaycastDirectionX(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 44));
   }

   public static float getRaycastDirectionY(MemorySegment mem) {
      return getRaycastDirectionY(mem, 0);
   }

   public static float getRaycastDirectionY(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 48));
   }

   public static float getRaycastDirectionZ(MemorySegment mem) {
      return getRaycastDirectionZ(mem, 0);
   }

   public static float getRaycastDirectionZ(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 52));
   }

   public static int getUndoGroupSize(MemorySegment mem) {
      return getUndoGroupSize(mem, 0);
   }

   public static int getUndoGroupSize(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 56));
   }

   public static BuilderToolOnUseInteraction toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BuilderToolOnUseInteraction toObject(MemorySegment mem, int offset) {
      if (offset + 60 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BuilderToolOnUseInteraction", offset + 60, (int)mem.byteSize());
      } else {
         return new BuilderToolOnUseInteraction(
            InteractionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0))),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 5)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 9)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 13)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 17)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 21)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 25)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 26)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 27)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 28)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 32)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 36)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 40)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 44)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 48)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 52)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 56))
         );
      }
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
      buf.writeIntLE(this.maxLengthToolIgnoreHistory);
      buf.writeFloatLE(this.raycastOriginX);
      buf.writeFloatLE(this.raycastOriginY);
      buf.writeFloatLE(this.raycastOriginZ);
      buf.writeFloatLE(this.raycastDirectionX);
      buf.writeFloatLE(this.raycastDirectionY);
      buf.writeFloatLE(this.raycastDirectionZ);
      buf.writeIntLE(this.undoGroupSize);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), (byte)this.type.getValue());
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.x);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 5), this.y);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 9), this.z);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 13), this.offsetForPaintModeX);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 17), this.offsetForPaintModeY);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 21), this.offsetForPaintModeZ);
      mem.set(PacketIO.PROTO_BOOL, offset + 25, this.isAltPlaySculptBrushModDown);
      mem.set(PacketIO.PROTO_BOOL, offset + 26, this.isHoldDownInteraction);
      mem.set(PacketIO.PROTO_BOOL, offset + 27, this.isDoServerRaytraceForPosition);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 28), this.maxLengthToolIgnoreHistory);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 32), this.raycastOriginX);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 36), this.raycastOriginY);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 40), this.raycastOriginZ);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 44), this.raycastDirectionX);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 48), this.raycastDirectionY);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 52), this.raycastDirectionZ);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 56), this.undoGroupSize);
      return 60;
   }

   @Override
   public int computeSize() {
      return 60;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 60) {
         return ValidationResult.error("Buffer too small: expected at least 60 bytes");
      } else {
         int v = buffer.getByte(offset + 0) & 255;
         return v >= 25 ? ValidationResult.error("Invalid InteractionType value for Type") : ValidationResult.OK;
      }
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
      copy.maxLengthToolIgnoreHistory = this.maxLengthToolIgnoreHistory;
      copy.raycastOriginX = this.raycastOriginX;
      copy.raycastOriginY = this.raycastOriginY;
      copy.raycastOriginZ = this.raycastOriginZ;
      copy.raycastDirectionX = this.raycastDirectionX;
      copy.raycastDirectionY = this.raycastDirectionY;
      copy.raycastDirectionZ = this.raycastDirectionZ;
      copy.undoGroupSize = this.undoGroupSize;
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
               && this.maxLengthToolIgnoreHistory == other.maxLengthToolIgnoreHistory
               && this.raycastOriginX == other.raycastOriginX
               && this.raycastOriginY == other.raycastOriginY
               && this.raycastOriginZ == other.raycastOriginZ
               && this.raycastDirectionX == other.raycastDirectionX
               && this.raycastDirectionY == other.raycastDirectionY
               && this.raycastDirectionZ == other.raycastDirectionZ
               && this.undoGroupSize == other.undoGroupSize;
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
         this.maxLengthToolIgnoreHistory,
         this.raycastOriginX,
         this.raycastOriginY,
         this.raycastOriginZ,
         this.raycastDirectionX,
         this.raycastDirectionY,
         this.raycastDirectionZ,
         this.undoGroupSize
      );
   }
}
