package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FluidParticle {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 8;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 8;
   public static final int MAX_SIZE = 16384013;
   @Nullable
   public String systemId;
   @Nullable
   public Color color;
   public float scale;

   public FluidParticle() {
   }

   public FluidParticle(@Nullable String systemId, @Nullable Color color, float scale) {
      this.systemId = systemId;
      this.color = color;
      this.scale = scale;
   }

   public FluidParticle(@Nonnull FluidParticle other) {
      this.systemId = other.systemId;
      this.color = other.color;
      this.scale = other.scale;
   }

   @Nonnull
   public static FluidParticle deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 8) {
         throw ProtocolException.bufferTooSmall("FluidParticle", 8, buf.readableBytes() - offset);
      } else {
         FluidParticle obj = new FluidParticle();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            obj.color = Color.deserialize(buf, offset + 1);
         }

         obj.scale = buf.getFloatLE(offset + 4);
         int pos = offset + 8;
         if ((nullBits & 2) != 0) {
            int systemIdLen = VarInt.peek(buf, pos);
            if (systemIdLen < 0) {
               throw ProtocolException.invalidVarInt("SystemId");
            }

            int systemIdVarLen = VarInt.size(systemIdLen);
            if (systemIdLen > 4096000) {
               throw ProtocolException.stringTooLong("SystemId", systemIdLen, 4096000);
            }

            if (pos + systemIdVarLen + systemIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("SystemId", pos + systemIdVarLen + systemIdLen, buf.readableBytes());
            }

            obj.systemId = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
            pos += systemIdVarLen + systemIdLen;
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 8;
      if ((nullBits & 2) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.size(sl) + sl;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 8L;
   }

   @Nullable
   public static String getSystemId(MemorySegment mem) {
      return getSystemId(mem, 0);
   }

   @Nullable
   public static String getSystemId(MemorySegment mem, int offset) {
      return hasSystemId(mem, offset) ? PacketIO.readVarString("SystemId", mem, offset + 8, 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static Color getColor(MemorySegment mem) {
      return getColor(mem, 0);
   }

   @Nullable
   public static Color getColor(MemorySegment mem, int offset) {
      return hasColor(mem, offset) ? Color.toObject(mem, offset + 1) : null;
   }

   public static float getScale(MemorySegment mem) {
      return getScale(mem, 0);
   }

   public static float getScale(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4));
   }

   public static boolean hasColor(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasSystemId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static FluidParticle toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static FluidParticle toObject(MemorySegment mem, int offset) {
      if (offset + 8 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("FluidParticle", offset + 8, (int)mem.byteSize());
      } else {
         return new FluidParticle(
            hasSystemId(mem, offset) ? PacketIO.readVarString("SystemId", mem, offset + 8, 4096000, PacketIO.UTF8) : null,
            hasColor(mem, offset) ? Color.toObject(mem, offset + 1) : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.color != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.systemId != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      if (this.color != null) {
         this.color.serialize(buf);
      } else {
         buf.writeZero(3);
      }

      buf.writeFloatLE(this.scale);
      if (this.systemId != null) {
         PacketIO.writeVarString(buf, this.systemId, 4096000);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.color != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.systemId != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      if (this.color != null) {
         this.color.serialize(mem, offset + 1);
      } else {
         mem.asSlice(offset + 1, 3L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 4), this.scale);
      int varOffset = offset + 8;
      if (this.systemId != null) {
         varOffset += PacketIO.writeVarString(mem, varOffset, this.systemId, 4096000);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 8;
      if (this.systemId != null) {
         size += PacketIO.stringSize(this.systemId);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 8) {
         return ValidationResult.error("Buffer too small: expected at least 8 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 8;
         if ((nullBits & 2) != 0) {
            int systemIdLen = VarInt.peek(buffer, pos);
            if (systemIdLen < 0) {
               return ValidationResult.error("Invalid string length for SystemId");
            }

            if (systemIdLen > 4096000) {
               return ValidationResult.error("SystemId exceeds max length 4096000");
            }

            pos += VarInt.size(systemIdLen);
            pos += systemIdLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading SystemId");
            }
         }

         return ValidationResult.OK;
      }
   }

   public FluidParticle clone() {
      FluidParticle copy = new FluidParticle();
      copy.systemId = this.systemId;
      copy.color = this.color != null ? this.color.clone() : null;
      copy.scale = this.scale;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof FluidParticle other)
            ? false
            : Objects.equals(this.systemId, other.systemId) && Objects.equals(this.color, other.color) && this.scale == other.scale;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.systemId, this.color, this.scale);
   }
}
