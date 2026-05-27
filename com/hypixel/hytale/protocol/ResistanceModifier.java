package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class ResistanceModifier {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 5;
   @Nonnull
   public ResistanceCalculationType calculationType = ResistanceCalculationType.Flat;
   public float amount;

   public ResistanceModifier() {
   }

   public ResistanceModifier(@Nonnull ResistanceCalculationType calculationType, float amount) {
      this.calculationType = calculationType;
      this.amount = amount;
   }

   public ResistanceModifier(@Nonnull ResistanceModifier other) {
      this.calculationType = other.calculationType;
      this.amount = other.amount;
   }

   @Nonnull
   public static ResistanceModifier deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 5) {
         throw ProtocolException.bufferTooSmall("ResistanceModifier", 5, buf.readableBytes() - offset);
      } else {
         ResistanceModifier obj = new ResistanceModifier();
         obj.calculationType = ResistanceCalculationType.fromValue(buf.getByte(offset + 0));
         obj.amount = buf.getFloatLE(offset + 1);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 5;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 5L;
   }

   public static ResistanceCalculationType getCalculationType(MemorySegment mem) {
      return getCalculationType(mem, 0);
   }

   public static ResistanceCalculationType getCalculationType(MemorySegment mem, int offset) {
      return ResistanceCalculationType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0)));
   }

   public static float getAmount(MemorySegment mem) {
      return getAmount(mem, 0);
   }

   public static float getAmount(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1));
   }

   public static ResistanceModifier toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ResistanceModifier toObject(MemorySegment mem, int offset) {
      if (offset + 5 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ResistanceModifier", offset + 5, (int)mem.byteSize());
      } else {
         return new ResistanceModifier(
            ResistanceCalculationType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0))), mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.calculationType.getValue());
      buf.writeFloatLE(this.amount);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), (byte)this.calculationType.getValue());
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 1), this.amount);
      return 5;
   }

   public int computeSize() {
      return 5;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 5) {
         return ValidationResult.error("Buffer too small: expected at least 5 bytes");
      } else {
         int v = buffer.getByte(offset + 0) & 255;
         return v >= 2 ? ValidationResult.error("Invalid ResistanceCalculationType value for CalculationType") : ValidationResult.OK;
      }
   }

   public ResistanceModifier clone() {
      ResistanceModifier copy = new ResistanceModifier();
      copy.calculationType = this.calculationType;
      copy.amount = this.amount;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ResistanceModifier other) ? false : Objects.equals(this.calculationType, other.calculationType) && this.amount == other.amount;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.calculationType, this.amount);
   }
}
