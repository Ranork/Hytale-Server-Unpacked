package com.hypixel.hytale.protocol.packets.buildertools;

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

public class BuilderToolExtrudeAction implements Packet, ToServerPacket {
   public static final int PACKET_ID = 403;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 30;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 30;
   public static final int MAX_SIZE = 30;
   public int x;
   public int y;
   public int z;
   public int xNormal;
   public int yNormal;
   public int zNormal;
   @Nonnull
   public ExtrudeMode mode = ExtrudeMode.Extrude;
   public boolean isHoldDownInteraction;
   public int undoGroupSize;

   @Override
   public int getId() {
      return 403;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public BuilderToolExtrudeAction() {
   }

   public BuilderToolExtrudeAction(
      int x, int y, int z, int xNormal, int yNormal, int zNormal, @Nonnull ExtrudeMode mode, boolean isHoldDownInteraction, int undoGroupSize
   ) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.xNormal = xNormal;
      this.yNormal = yNormal;
      this.zNormal = zNormal;
      this.mode = mode;
      this.isHoldDownInteraction = isHoldDownInteraction;
      this.undoGroupSize = undoGroupSize;
   }

   public BuilderToolExtrudeAction(@Nonnull BuilderToolExtrudeAction other) {
      this.x = other.x;
      this.y = other.y;
      this.z = other.z;
      this.xNormal = other.xNormal;
      this.yNormal = other.yNormal;
      this.zNormal = other.zNormal;
      this.mode = other.mode;
      this.isHoldDownInteraction = other.isHoldDownInteraction;
      this.undoGroupSize = other.undoGroupSize;
   }

   @Nonnull
   public static BuilderToolExtrudeAction deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 30) {
         throw ProtocolException.bufferTooSmall("BuilderToolExtrudeAction", 30, buf.readableBytes() - offset);
      } else {
         BuilderToolExtrudeAction obj = new BuilderToolExtrudeAction();
         obj.x = buf.getIntLE(offset + 0);
         obj.y = buf.getIntLE(offset + 4);
         obj.z = buf.getIntLE(offset + 8);
         obj.xNormal = buf.getIntLE(offset + 12);
         obj.yNormal = buf.getIntLE(offset + 16);
         obj.zNormal = buf.getIntLE(offset + 20);
         obj.mode = ExtrudeMode.fromValue(buf.getByte(offset + 24));
         obj.isHoldDownInteraction = buf.getByte(offset + 25) != 0;
         obj.undoGroupSize = buf.getIntLE(offset + 26);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 30;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 30L;
   }

   public static int getX(MemorySegment mem) {
      return getX(mem, 0);
   }

   public static int getX(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static int getY(MemorySegment mem) {
      return getY(mem, 0);
   }

   public static int getY(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 4));
   }

   public static int getZ(MemorySegment mem) {
      return getZ(mem, 0);
   }

   public static int getZ(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 8));
   }

   public static int getXNormal(MemorySegment mem) {
      return getXNormal(mem, 0);
   }

   public static int getXNormal(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 12));
   }

   public static int getYNormal(MemorySegment mem) {
      return getYNormal(mem, 0);
   }

   public static int getYNormal(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 16));
   }

   public static int getZNormal(MemorySegment mem) {
      return getZNormal(mem, 0);
   }

   public static int getZNormal(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 20));
   }

   public static ExtrudeMode getMode(MemorySegment mem) {
      return getMode(mem, 0);
   }

   public static ExtrudeMode getMode(MemorySegment mem, int offset) {
      return ExtrudeMode.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 24)));
   }

   public static boolean getIsHoldDownInteraction(MemorySegment mem) {
      return getIsHoldDownInteraction(mem, 0);
   }

   public static boolean getIsHoldDownInteraction(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 25));
   }

   public static int getUndoGroupSize(MemorySegment mem) {
      return getUndoGroupSize(mem, 0);
   }

   public static int getUndoGroupSize(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 26));
   }

   public static BuilderToolExtrudeAction toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BuilderToolExtrudeAction toObject(MemorySegment mem, int offset) {
      if (offset + 30 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BuilderToolExtrudeAction", offset + 30, (int)mem.byteSize());
      } else {
         return new BuilderToolExtrudeAction(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 4)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 8)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 12)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 16)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 20)),
            ExtrudeMode.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 24))),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 25)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 26))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.x);
      buf.writeIntLE(this.y);
      buf.writeIntLE(this.z);
      buf.writeIntLE(this.xNormal);
      buf.writeIntLE(this.yNormal);
      buf.writeIntLE(this.zNormal);
      buf.writeByte(this.mode.getValue());
      buf.writeByte(this.isHoldDownInteraction ? 1 : 0);
      buf.writeIntLE(this.undoGroupSize);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.x);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 4), this.y);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 8), this.z);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 12), this.xNormal);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 16), this.yNormal);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 20), this.zNormal);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 24), (byte)this.mode.getValue());
      mem.set(PacketIO.PROTO_BOOL, offset + 25, this.isHoldDownInteraction);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 26), this.undoGroupSize);
      return 30;
   }

   @Override
   public int computeSize() {
      return 30;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 30) {
         return ValidationResult.error("Buffer too small: expected at least 30 bytes");
      } else {
         int v = buffer.getByte(offset + 24) & 255;
         return v >= 3 ? ValidationResult.error("Invalid ExtrudeMode value for Mode") : ValidationResult.OK;
      }
   }

   public BuilderToolExtrudeAction clone() {
      BuilderToolExtrudeAction copy = new BuilderToolExtrudeAction();
      copy.x = this.x;
      copy.y = this.y;
      copy.z = this.z;
      copy.xNormal = this.xNormal;
      copy.yNormal = this.yNormal;
      copy.zNormal = this.zNormal;
      copy.mode = this.mode;
      copy.isHoldDownInteraction = this.isHoldDownInteraction;
      copy.undoGroupSize = this.undoGroupSize;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BuilderToolExtrudeAction other)
            ? false
            : this.x == other.x
               && this.y == other.y
               && this.z == other.z
               && this.xNormal == other.xNormal
               && this.yNormal == other.yNormal
               && this.zNormal == other.zNormal
               && Objects.equals(this.mode, other.mode)
               && this.isHoldDownInteraction == other.isHoldDownInteraction
               && this.undoGroupSize == other.undoGroupSize;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.x, this.y, this.z, this.xNormal, this.yNormal, this.zNormal, this.mode, this.isHoldDownInteraction, this.undoGroupSize);
   }
}
