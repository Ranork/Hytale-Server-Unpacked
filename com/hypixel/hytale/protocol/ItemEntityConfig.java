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

public class ItemEntityConfig {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 16384010;
   @Nullable
   public String particleSystemId;
   @Nullable
   public Color particleColor;
   public boolean showItemParticles;

   public ItemEntityConfig() {
   }

   public ItemEntityConfig(@Nullable String particleSystemId, @Nullable Color particleColor, boolean showItemParticles) {
      this.particleSystemId = particleSystemId;
      this.particleColor = particleColor;
      this.showItemParticles = showItemParticles;
   }

   public ItemEntityConfig(@Nonnull ItemEntityConfig other) {
      this.particleSystemId = other.particleSystemId;
      this.particleColor = other.particleColor;
      this.showItemParticles = other.showItemParticles;
   }

   @Nonnull
   public static ItemEntityConfig deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 5) {
         throw ProtocolException.bufferTooSmall("ItemEntityConfig", 5, buf.readableBytes() - offset);
      } else {
         ItemEntityConfig obj = new ItemEntityConfig();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            obj.particleColor = Color.deserialize(buf, offset + 1);
         }

         obj.showItemParticles = buf.getByte(offset + 4) != 0;
         int pos = offset + 5;
         if ((nullBits & 2) != 0) {
            int particleSystemIdLen = VarInt.peek(buf, pos);
            if (particleSystemIdLen < 0) {
               throw ProtocolException.invalidVarInt("ParticleSystemId");
            }

            int particleSystemIdVarLen = VarInt.size(particleSystemIdLen);
            if (particleSystemIdLen > 4096000) {
               throw ProtocolException.stringTooLong("ParticleSystemId", particleSystemIdLen, 4096000);
            }

            if (pos + particleSystemIdVarLen + particleSystemIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ParticleSystemId", pos + particleSystemIdVarLen + particleSystemIdLen, buf.readableBytes());
            }

            obj.particleSystemId = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
            pos += particleSystemIdVarLen + particleSystemIdLen;
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 5;
      if ((nullBits & 2) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.size(sl) + sl;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 5L;
   }

   @Nullable
   public static String getParticleSystemId(MemorySegment mem) {
      return getParticleSystemId(mem, 0);
   }

   @Nullable
   public static String getParticleSystemId(MemorySegment mem, int offset) {
      return hasParticleSystemId(mem, offset) ? PacketIO.readVarString("ParticleSystemId", mem, offset + 5, 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static Color getParticleColor(MemorySegment mem) {
      return getParticleColor(mem, 0);
   }

   @Nullable
   public static Color getParticleColor(MemorySegment mem, int offset) {
      return hasParticleColor(mem, offset) ? Color.toObject(mem, offset + 1) : null;
   }

   public static boolean getShowItemParticles(MemorySegment mem) {
      return getShowItemParticles(mem, 0);
   }

   public static boolean getShowItemParticles(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 4));
   }

   public static boolean hasParticleColor(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasParticleSystemId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static ItemEntityConfig toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ItemEntityConfig toObject(MemorySegment mem, int offset) {
      if (offset + 5 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ItemEntityConfig", offset + 5, (int)mem.byteSize());
      } else {
         return new ItemEntityConfig(
            hasParticleSystemId(mem, offset) ? PacketIO.readVarString("ParticleSystemId", mem, offset + 5, 4096000, PacketIO.UTF8) : null,
            hasParticleColor(mem, offset) ? Color.toObject(mem, offset + 1) : null,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 4))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.particleColor != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.particleSystemId != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      if (this.particleColor != null) {
         this.particleColor.serialize(buf);
      } else {
         buf.writeZero(3);
      }

      buf.writeByte(this.showItemParticles ? 1 : 0);
      if (this.particleSystemId != null) {
         PacketIO.writeVarString(buf, this.particleSystemId, 4096000);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.particleColor != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.particleSystemId != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      if (this.particleColor != null) {
         this.particleColor.serialize(mem, offset + 1);
      } else {
         mem.asSlice(offset + 1, 3L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_BOOL, offset + 4, this.showItemParticles);
      int varOffset = offset + 5;
      if (this.particleSystemId != null) {
         varOffset += PacketIO.writeVarString(mem, varOffset, this.particleSystemId, 4096000);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 5;
      if (this.particleSystemId != null) {
         size += PacketIO.stringSize(this.particleSystemId);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 5) {
         return ValidationResult.error("Buffer too small: expected at least 5 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 5;
         if ((nullBits & 2) != 0) {
            int particleSystemIdLen = VarInt.peek(buffer, pos);
            if (particleSystemIdLen < 0) {
               return ValidationResult.error("Invalid string length for ParticleSystemId");
            }

            if (particleSystemIdLen > 4096000) {
               return ValidationResult.error("ParticleSystemId exceeds max length 4096000");
            }

            pos += VarInt.size(particleSystemIdLen);
            pos += particleSystemIdLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ParticleSystemId");
            }
         }

         return ValidationResult.OK;
      }
   }

   public ItemEntityConfig clone() {
      ItemEntityConfig copy = new ItemEntityConfig();
      copy.particleSystemId = this.particleSystemId;
      copy.particleColor = this.particleColor != null ? this.particleColor.clone() : null;
      copy.showItemParticles = this.showItemParticles;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ItemEntityConfig other)
            ? false
            : Objects.equals(this.particleSystemId, other.particleSystemId)
               && Objects.equals(this.particleColor, other.particleColor)
               && this.showItemParticles == other.showItemParticles;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.particleSystemId, this.particleColor, this.showItemParticles);
   }
}
