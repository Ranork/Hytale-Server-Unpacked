package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class Modifier {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 6;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 6;
   public static final int MAX_SIZE = 6;
   @Nonnull
   public ModifierTarget target = ModifierTarget.Min;
   @Nonnull
   public CalculationType calculationType = CalculationType.Additive;
   public float amount;

   public Modifier() {
   }

   public Modifier(@Nonnull ModifierTarget target, @Nonnull CalculationType calculationType, float amount) {
      this.target = target;
      this.calculationType = calculationType;
      this.amount = amount;
   }

   public Modifier(@Nonnull Modifier other) {
      this.target = other.target;
      this.calculationType = other.calculationType;
      this.amount = other.amount;
   }

   @Nonnull
   public static Modifier deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 6) {
         throw ProtocolException.bufferTooSmall("Modifier", 6, buf.readableBytes() - offset);
      } else {
         Modifier obj = new Modifier();
         obj.target = ModifierTarget.fromValue(buf.getByte(offset + 0));
         obj.calculationType = CalculationType.fromValue(buf.getByte(offset + 1));
         obj.amount = buf.getFloatLE(offset + 2);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 6;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 6L;
   }

   public static ModifierTarget getTarget(MemorySegment mem) {
      return getTarget(mem, 0);
   }

   public static ModifierTarget getTarget(MemorySegment mem, int offset) {
      return ModifierTarget.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0)));
   }

   public static CalculationType getCalculationType(MemorySegment mem) {
      return getCalculationType(mem, 0);
   }

   public static CalculationType getCalculationType(MemorySegment mem, int offset) {
      return CalculationType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   public static float getAmount(MemorySegment mem) {
      return getAmount(mem, 0);
   }

   public static float getAmount(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 2));
   }

   public static Modifier toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static Modifier toObject(MemorySegment mem, int offset) {
      if (offset + 6 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("Modifier", offset + 6, (int)mem.byteSize());
      } else {
         return new Modifier(
            ModifierTarget.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0))),
            CalculationType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 2))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.target.getValue());
      buf.writeByte(this.calculationType.getValue());
      buf.writeFloatLE(this.amount);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), (byte)this.target.getValue());
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.calculationType.getValue());
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 2), this.amount);
      return 6;
   }

   public int computeSize() {
      return 6;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 6) {
         return ValidationResult.error("Buffer too small: expected at least 6 bytes");
      } else {
         int v = buffer.getByte(offset + 0) & 255;
         if (v >= 2) {
            return ValidationResult.error("Invalid ModifierTarget value for Target");
         } else {
            v = buffer.getByte(offset + 1) & 255;
            return v >= 2 ? ValidationResult.error("Invalid CalculationType value for CalculationType") : ValidationResult.OK;
         }
      }
   }

   public Modifier clone() {
      Modifier copy = new Modifier();
      copy.target = this.target;
      copy.calculationType = this.calculationType;
      copy.amount = this.amount;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Modifier other)
            ? false
            : Objects.equals(this.target, other.target) && Objects.equals(this.calculationType, other.calculationType) && this.amount == other.amount;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.target, this.calculationType, this.amount);
   }
}
