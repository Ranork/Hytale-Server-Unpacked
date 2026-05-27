package com.hypixel.hytale.protocol.packets.player;

import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.BlockRotation;
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
import javax.annotation.Nullable;

public class ClientPlaceBlock implements Packet, ToServerPacket {
   public static final int PACKET_ID = 117;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 23;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 23;
   public static final int MAX_SIZE = 23;
   @Nullable
   public BlockPosition position;
   @Nullable
   public BlockRotation rotation;
   public int placedBlockId;
   public boolean quickReplace;
   public boolean quickRetype;
   public boolean noPhysics;

   @Override
   public int getId() {
      return 117;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public ClientPlaceBlock() {
   }

   public ClientPlaceBlock(
      @Nullable BlockPosition position, @Nullable BlockRotation rotation, int placedBlockId, boolean quickReplace, boolean quickRetype, boolean noPhysics
   ) {
      this.position = position;
      this.rotation = rotation;
      this.placedBlockId = placedBlockId;
      this.quickReplace = quickReplace;
      this.quickRetype = quickRetype;
      this.noPhysics = noPhysics;
   }

   public ClientPlaceBlock(@Nonnull ClientPlaceBlock other) {
      this.position = other.position;
      this.rotation = other.rotation;
      this.placedBlockId = other.placedBlockId;
      this.quickReplace = other.quickReplace;
      this.quickRetype = other.quickRetype;
      this.noPhysics = other.noPhysics;
   }

   @Nonnull
   public static ClientPlaceBlock deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 23) {
         throw ProtocolException.bufferTooSmall("ClientPlaceBlock", 23, buf.readableBytes() - offset);
      } else {
         ClientPlaceBlock obj = new ClientPlaceBlock();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            obj.position = BlockPosition.deserialize(buf, offset + 1);
         }

         if ((nullBits & 2) != 0) {
            obj.rotation = BlockRotation.deserialize(buf, offset + 13);
         }

         obj.placedBlockId = buf.getIntLE(offset + 16);
         obj.quickReplace = buf.getByte(offset + 20) != 0;
         obj.quickRetype = buf.getByte(offset + 21) != 0;
         obj.noPhysics = buf.getByte(offset + 22) != 0;
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 23;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 23L;
   }

   @Nullable
   public static BlockPosition getPosition(MemorySegment mem) {
      return getPosition(mem, 0);
   }

   @Nullable
   public static BlockPosition getPosition(MemorySegment mem, int offset) {
      return hasPosition(mem, offset) ? BlockPosition.toObject(mem, offset + 1) : null;
   }

   @Nullable
   public static BlockRotation getRotation(MemorySegment mem) {
      return getRotation(mem, 0);
   }

   @Nullable
   public static BlockRotation getRotation(MemorySegment mem, int offset) {
      return hasRotation(mem, offset) ? BlockRotation.toObject(mem, offset + 13) : null;
   }

   public static int getPlacedBlockId(MemorySegment mem) {
      return getPlacedBlockId(mem, 0);
   }

   public static int getPlacedBlockId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 16));
   }

   public static boolean getQuickReplace(MemorySegment mem) {
      return getQuickReplace(mem, 0);
   }

   public static boolean getQuickReplace(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 20));
   }

   public static boolean getQuickRetype(MemorySegment mem) {
      return getQuickRetype(mem, 0);
   }

   public static boolean getQuickRetype(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 21));
   }

   public static boolean getNoPhysics(MemorySegment mem) {
      return getNoPhysics(mem, 0);
   }

   public static boolean getNoPhysics(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 22));
   }

   public static boolean hasPosition(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasRotation(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static ClientPlaceBlock toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ClientPlaceBlock toObject(MemorySegment mem, int offset) {
      if (offset + 23 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ClientPlaceBlock", offset + 23, (int)mem.byteSize());
      } else {
         return new ClientPlaceBlock(
            hasPosition(mem, offset) ? BlockPosition.toObject(mem, offset + 1) : null,
            hasRotation(mem, offset) ? BlockRotation.toObject(mem, offset + 13) : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 16)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 20)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 21)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 22))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.position != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.rotation != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      if (this.position != null) {
         this.position.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.rotation != null) {
         this.rotation.serialize(buf);
      } else {
         buf.writeZero(3);
      }

      buf.writeIntLE(this.placedBlockId);
      buf.writeByte(this.quickReplace ? 1 : 0);
      buf.writeByte(this.quickRetype ? 1 : 0);
      buf.writeByte(this.noPhysics ? 1 : 0);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.position != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.rotation != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      if (this.position != null) {
         this.position.serialize(mem, offset + 1);
      } else {
         mem.asSlice(offset + 1, 12L).fill((byte)0);
      }

      if (this.rotation != null) {
         this.rotation.serialize(mem, offset + 13);
      } else {
         mem.asSlice(offset + 13, 3L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_INT, (long)(offset + 16), this.placedBlockId);
      mem.set(PacketIO.PROTO_BOOL, offset + 20, this.quickReplace);
      mem.set(PacketIO.PROTO_BOOL, offset + 21, this.quickRetype);
      mem.set(PacketIO.PROTO_BOOL, offset + 22, this.noPhysics);
      return 23;
   }

   @Override
   public int computeSize() {
      return 23;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 23) {
         return ValidationResult.error("Buffer too small: expected at least 23 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         return ValidationResult.OK;
      }
   }

   public ClientPlaceBlock clone() {
      ClientPlaceBlock copy = new ClientPlaceBlock();
      copy.position = this.position != null ? this.position.clone() : null;
      copy.rotation = this.rotation != null ? this.rotation.clone() : null;
      copy.placedBlockId = this.placedBlockId;
      copy.quickReplace = this.quickReplace;
      copy.quickRetype = this.quickRetype;
      copy.noPhysics = this.noPhysics;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ClientPlaceBlock other)
            ? false
            : Objects.equals(this.position, other.position)
               && Objects.equals(this.rotation, other.rotation)
               && this.placedBlockId == other.placedBlockId
               && this.quickReplace == other.quickReplace
               && this.quickRetype == other.quickRetype
               && this.noPhysics == other.noPhysics;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.position, this.rotation, this.placedBlockId, this.quickReplace, this.quickRetype, this.noPhysics);
   }
}
