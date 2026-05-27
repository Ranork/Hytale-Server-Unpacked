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

public class BuilderToolRandomizeClipboard implements Packet, ToServerPacket {
   public static final int PACKET_ID = 428;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 15;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 15;
   public static final int MAX_SIZE = 15;
   public int deltaX;
   public int deltaY;
   public int deltaZ;
   public boolean flipX;
   public boolean flipY;
   public boolean flipZ;

   @Override
   public int getId() {
      return 428;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public BuilderToolRandomizeClipboard() {
   }

   public BuilderToolRandomizeClipboard(int deltaX, int deltaY, int deltaZ, boolean flipX, boolean flipY, boolean flipZ) {
      this.deltaX = deltaX;
      this.deltaY = deltaY;
      this.deltaZ = deltaZ;
      this.flipX = flipX;
      this.flipY = flipY;
      this.flipZ = flipZ;
   }

   public BuilderToolRandomizeClipboard(@Nonnull BuilderToolRandomizeClipboard other) {
      this.deltaX = other.deltaX;
      this.deltaY = other.deltaY;
      this.deltaZ = other.deltaZ;
      this.flipX = other.flipX;
      this.flipY = other.flipY;
      this.flipZ = other.flipZ;
   }

   @Nonnull
   public static BuilderToolRandomizeClipboard deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 15) {
         throw ProtocolException.bufferTooSmall("BuilderToolRandomizeClipboard", 15, buf.readableBytes() - offset);
      } else {
         BuilderToolRandomizeClipboard obj = new BuilderToolRandomizeClipboard();
         obj.deltaX = buf.getIntLE(offset + 0);
         obj.deltaY = buf.getIntLE(offset + 4);
         obj.deltaZ = buf.getIntLE(offset + 8);
         obj.flipX = buf.getByte(offset + 12) != 0;
         obj.flipY = buf.getByte(offset + 13) != 0;
         obj.flipZ = buf.getByte(offset + 14) != 0;
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 15;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 15L;
   }

   public static int getDeltaX(MemorySegment mem) {
      return getDeltaX(mem, 0);
   }

   public static int getDeltaX(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static int getDeltaY(MemorySegment mem) {
      return getDeltaY(mem, 0);
   }

   public static int getDeltaY(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 4));
   }

   public static int getDeltaZ(MemorySegment mem) {
      return getDeltaZ(mem, 0);
   }

   public static int getDeltaZ(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 8));
   }

   public static boolean getFlipX(MemorySegment mem) {
      return getFlipX(mem, 0);
   }

   public static boolean getFlipX(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 12));
   }

   public static boolean getFlipY(MemorySegment mem) {
      return getFlipY(mem, 0);
   }

   public static boolean getFlipY(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 13));
   }

   public static boolean getFlipZ(MemorySegment mem) {
      return getFlipZ(mem, 0);
   }

   public static boolean getFlipZ(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 14));
   }

   public static BuilderToolRandomizeClipboard toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BuilderToolRandomizeClipboard toObject(MemorySegment mem, int offset) {
      if (offset + 15 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BuilderToolRandomizeClipboard", offset + 15, (int)mem.byteSize());
      } else {
         return new BuilderToolRandomizeClipboard(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 4)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 8)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 12)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 13)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 14))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.deltaX);
      buf.writeIntLE(this.deltaY);
      buf.writeIntLE(this.deltaZ);
      buf.writeByte(this.flipX ? 1 : 0);
      buf.writeByte(this.flipY ? 1 : 0);
      buf.writeByte(this.flipZ ? 1 : 0);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.deltaX);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 4), this.deltaY);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 8), this.deltaZ);
      mem.set(PacketIO.PROTO_BOOL, offset + 12, this.flipX);
      mem.set(PacketIO.PROTO_BOOL, offset + 13, this.flipY);
      mem.set(PacketIO.PROTO_BOOL, offset + 14, this.flipZ);
      return 15;
   }

   @Override
   public int computeSize() {
      return 15;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 15 ? ValidationResult.error("Buffer too small: expected at least 15 bytes") : ValidationResult.OK;
   }

   public BuilderToolRandomizeClipboard clone() {
      BuilderToolRandomizeClipboard copy = new BuilderToolRandomizeClipboard();
      copy.deltaX = this.deltaX;
      copy.deltaY = this.deltaY;
      copy.deltaZ = this.deltaZ;
      copy.flipX = this.flipX;
      copy.flipY = this.flipY;
      copy.flipZ = this.flipZ;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BuilderToolRandomizeClipboard other)
            ? false
            : this.deltaX == other.deltaX
               && this.deltaY == other.deltaY
               && this.deltaZ == other.deltaZ
               && this.flipX == other.flipX
               && this.flipY == other.flipY
               && this.flipZ == other.flipZ;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.deltaX, this.deltaY, this.deltaZ, this.flipX, this.flipY, this.flipZ);
   }
}
