package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PortalDef {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 16384014;
   @Nullable
   public String nameKey;
   public int explorationSeconds;
   public int breachSeconds;

   public PortalDef() {
   }

   public PortalDef(@Nullable String nameKey, int explorationSeconds, int breachSeconds) {
      this.nameKey = nameKey;
      this.explorationSeconds = explorationSeconds;
      this.breachSeconds = breachSeconds;
   }

   public PortalDef(@Nonnull PortalDef other) {
      this.nameKey = other.nameKey;
      this.explorationSeconds = other.explorationSeconds;
      this.breachSeconds = other.breachSeconds;
   }

   @Nonnull
   public static PortalDef deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("PortalDef", 9, buf.readableBytes() - offset);
      } else {
         PortalDef obj = new PortalDef();
         byte nullBits = buf.getByte(offset);
         obj.explorationSeconds = buf.getIntLE(offset + 1);
         obj.breachSeconds = buf.getIntLE(offset + 5);
         int pos = offset + 9;
         if ((nullBits & 1) != 0) {
            int nameKeyLen = VarInt.peek(buf, pos);
            if (nameKeyLen < 0) {
               throw ProtocolException.invalidVarInt("NameKey");
            }

            int nameKeyVarLen = VarInt.size(nameKeyLen);
            if (nameKeyLen > 4096000) {
               throw ProtocolException.stringTooLong("NameKey", nameKeyLen, 4096000);
            }

            if (pos + nameKeyVarLen + nameKeyLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("NameKey", pos + nameKeyVarLen + nameKeyLen, buf.readableBytes());
            }

            obj.nameKey = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
            pos += nameKeyVarLen + nameKeyLen;
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 9;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.size(sl) + sl;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 9L;
   }

   @Nullable
   public static String getNameKey(MemorySegment mem) {
      return getNameKey(mem, 0);
   }

   @Nullable
   public static String getNameKey(MemorySegment mem, int offset) {
      return hasNameKey(mem, offset) ? PacketIO.readVarString("NameKey", mem, offset + 9, 4096000, PacketIO.UTF8) : null;
   }

   public static int getExplorationSeconds(MemorySegment mem) {
      return getExplorationSeconds(mem, 0);
   }

   public static int getExplorationSeconds(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static int getBreachSeconds(MemorySegment mem) {
      return getBreachSeconds(mem, 0);
   }

   public static int getBreachSeconds(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 5));
   }

   public static boolean hasNameKey(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static PortalDef toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static PortalDef toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("PortalDef", offset + 9, (int)mem.byteSize());
      } else {
         return new PortalDef(
            hasNameKey(mem, offset) ? PacketIO.readVarString("NameKey", mem, offset + 9, 4096000, PacketIO.UTF8) : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 5))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.nameKey != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.explorationSeconds);
      buf.writeIntLE(this.breachSeconds);
      if (this.nameKey != null) {
         PacketIO.writeVarString(buf, this.nameKey, 4096000);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.nameKey != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.explorationSeconds);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 5), this.breachSeconds);
      int varOffset = offset + 9;
      if (this.nameKey != null) {
         varOffset += PacketIO.writeVarString(mem, varOffset, this.nameKey, 4096000);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 9;
      if (this.nameKey != null) {
         size += PacketIO.stringSize(this.nameKey);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 9;
         if ((nullBits & 1) != 0) {
            int nameKeyLen = VarInt.peek(buffer, pos);
            if (nameKeyLen < 0) {
               return ValidationResult.error("Invalid string length for NameKey");
            }

            if (nameKeyLen > 4096000) {
               return ValidationResult.error("NameKey exceeds max length 4096000");
            }

            pos += VarInt.size(nameKeyLen);
            pos += nameKeyLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading NameKey");
            }
         }

         return ValidationResult.OK;
      }
   }

   public PortalDef clone() {
      PortalDef copy = new PortalDef();
      copy.nameKey = this.nameKey;
      copy.explorationSeconds = this.explorationSeconds;
      copy.breachSeconds = this.breachSeconds;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof PortalDef other)
            ? false
            : Objects.equals(this.nameKey, other.nameKey) && this.explorationSeconds == other.explorationSeconds && this.breachSeconds == other.breachSeconds;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.nameKey, this.explorationSeconds, this.breachSeconds);
   }
}
