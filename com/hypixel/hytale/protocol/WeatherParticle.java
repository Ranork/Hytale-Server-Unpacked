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

public class WeatherParticle {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 13;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 13;
   public static final int MAX_SIZE = 16384018;
   @Nullable
   public String systemId;
   @Nullable
   public Color color;
   public float scale;
   public boolean isOvergroundOnly;
   public float positionOffsetMultiplier;

   public WeatherParticle() {
   }

   public WeatherParticle(@Nullable String systemId, @Nullable Color color, float scale, boolean isOvergroundOnly, float positionOffsetMultiplier) {
      this.systemId = systemId;
      this.color = color;
      this.scale = scale;
      this.isOvergroundOnly = isOvergroundOnly;
      this.positionOffsetMultiplier = positionOffsetMultiplier;
   }

   public WeatherParticle(@Nonnull WeatherParticle other) {
      this.systemId = other.systemId;
      this.color = other.color;
      this.scale = other.scale;
      this.isOvergroundOnly = other.isOvergroundOnly;
      this.positionOffsetMultiplier = other.positionOffsetMultiplier;
   }

   @Nonnull
   public static WeatherParticle deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 13) {
         throw ProtocolException.bufferTooSmall("WeatherParticle", 13, buf.readableBytes() - offset);
      } else {
         WeatherParticle obj = new WeatherParticle();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            obj.color = Color.deserialize(buf, offset + 1);
         }

         obj.scale = buf.getFloatLE(offset + 4);
         obj.isOvergroundOnly = buf.getByte(offset + 8) != 0;
         obj.positionOffsetMultiplier = buf.getFloatLE(offset + 9);
         int pos = offset + 13;
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
      int pos = offset + 13;
      if ((nullBits & 2) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.size(sl) + sl;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 13L;
   }

   @Nullable
   public static String getSystemId(MemorySegment mem) {
      return getSystemId(mem, 0);
   }

   @Nullable
   public static String getSystemId(MemorySegment mem, int offset) {
      return hasSystemId(mem, offset) ? PacketIO.readVarString("SystemId", mem, offset + 13, 4096000, PacketIO.UTF8) : null;
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

   public static boolean getIsOvergroundOnly(MemorySegment mem) {
      return getIsOvergroundOnly(mem, 0);
   }

   public static boolean getIsOvergroundOnly(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 8));
   }

   public static float getPositionOffsetMultiplier(MemorySegment mem) {
      return getPositionOffsetMultiplier(mem, 0);
   }

   public static float getPositionOffsetMultiplier(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9));
   }

   public static boolean hasColor(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasSystemId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static WeatherParticle toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static WeatherParticle toObject(MemorySegment mem, int offset) {
      if (offset + 13 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("WeatherParticle", offset + 13, (int)mem.byteSize());
      } else {
         return new WeatherParticle(
            hasSystemId(mem, offset) ? PacketIO.readVarString("SystemId", mem, offset + 13, 4096000, PacketIO.UTF8) : null,
            hasColor(mem, offset) ? Color.toObject(mem, offset + 1) : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 8)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9))
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
      buf.writeByte(this.isOvergroundOnly ? 1 : 0);
      buf.writeFloatLE(this.positionOffsetMultiplier);
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
      mem.set(PacketIO.PROTO_BOOL, offset + 8, this.isOvergroundOnly);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 9), this.positionOffsetMultiplier);
      int varOffset = offset + 13;
      if (this.systemId != null) {
         varOffset += PacketIO.writeVarString(mem, varOffset, this.systemId, 4096000);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 13;
      if (this.systemId != null) {
         size += PacketIO.stringSize(this.systemId);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 13) {
         return ValidationResult.error("Buffer too small: expected at least 13 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 13;
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

   public WeatherParticle clone() {
      WeatherParticle copy = new WeatherParticle();
      copy.systemId = this.systemId;
      copy.color = this.color != null ? this.color.clone() : null;
      copy.scale = this.scale;
      copy.isOvergroundOnly = this.isOvergroundOnly;
      copy.positionOffsetMultiplier = this.positionOffsetMultiplier;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof WeatherParticle other)
            ? false
            : Objects.equals(this.systemId, other.systemId)
               && Objects.equals(this.color, other.color)
               && this.scale == other.scale
               && this.isOvergroundOnly == other.isOvergroundOnly
               && this.positionOffsetMultiplier == other.positionOffsetMultiplier;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.systemId, this.color, this.scale, this.isOvergroundOnly, this.positionOffsetMultiplier);
   }
}
