package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class CombatTextUpdate extends ComponentUpdate {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 4;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 4;
   public static final int MAX_SIZE = 16384009;
   public float hitAngleDeg;
   @Nonnull
   public String text = "";

   public CombatTextUpdate() {
   }

   public CombatTextUpdate(float hitAngleDeg, @Nonnull String text) {
      this.hitAngleDeg = hitAngleDeg;
      this.text = text;
   }

   public CombatTextUpdate(@Nonnull CombatTextUpdate other) {
      this.hitAngleDeg = other.hitAngleDeg;
      this.text = other.text;
   }

   @Nonnull
   public static CombatTextUpdate deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 4) {
         throw ProtocolException.bufferTooSmall("CombatTextUpdate", 4, buf.readableBytes() - offset);
      } else {
         CombatTextUpdate obj = new CombatTextUpdate();
         obj.hitAngleDeg = buf.getFloatLE(offset + 0);
         int pos = offset + 4;
         int textLen = VarInt.peek(buf, pos);
         if (textLen < 0) {
            throw ProtocolException.invalidVarInt("Text");
         } else {
            int textVarLen = VarInt.size(textLen);
            if (textLen > 4096000) {
               throw ProtocolException.stringTooLong("Text", textLen, 4096000);
            } else if (pos + textVarLen + textLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Text", pos + textVarLen + textLen, buf.readableBytes());
            } else {
               obj.text = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
               pos += textVarLen + textLen;
               return obj;
            }
         }
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int pos = offset + 4;
      int sl = VarInt.peek(buf, pos);
      pos += VarInt.size(sl) + sl;
      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 4L;
   }

   public static float getHitAngleDeg(MemorySegment mem) {
      return getHitAngleDeg(mem, 0);
   }

   public static float getHitAngleDeg(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0));
   }

   public static String getText(MemorySegment mem) {
      return getText(mem, 0);
   }

   public static String getText(MemorySegment mem, int offset) {
      return PacketIO.readVarString("Text", mem, offset + 4, 4096000, PacketIO.UTF8);
   }

   public static CombatTextUpdate toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static CombatTextUpdate toObject(MemorySegment mem, int offset) {
      if (offset + 4 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("CombatTextUpdate", offset + 4, (int)mem.byteSize());
      } else {
         return new CombatTextUpdate(mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0)), PacketIO.readVarString("Text", mem, offset + 4, 4096000, PacketIO.UTF8));
      }
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      buf.writeFloatLE(this.hitAngleDeg);
      PacketIO.writeVarString(buf, this.text, 4096000);
      return buf.writerIndex() - startPos;
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 0), this.hitAngleDeg);
      int varOffset = offset + 4;
      varOffset += PacketIO.writeVarString(mem, varOffset, this.text, 4096000);
      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 4;
      return size + PacketIO.stringSize(this.text);
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 4) {
         return ValidationResult.error("Buffer too small: expected at least 4 bytes");
      } else {
         int pos = offset + 4;
         int textLen = VarInt.peek(buffer, pos);
         if (textLen < 0) {
            return ValidationResult.error("Invalid string length for Text");
         } else if (textLen > 4096000) {
            return ValidationResult.error("Text exceeds max length 4096000");
         } else {
            pos += VarInt.size(textLen);
            pos += textLen;
            return pos > buffer.writerIndex() ? ValidationResult.error("Buffer overflow reading Text") : ValidationResult.OK;
         }
      }
   }

   public CombatTextUpdate clone() {
      CombatTextUpdate copy = new CombatTextUpdate();
      copy.hitAngleDeg = this.hitAngleDeg;
      copy.text = this.text;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof CombatTextUpdate other) ? false : this.hitAngleDeg == other.hitAngleDeg && Objects.equals(this.text, other.text);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.hitAngleDeg, this.text);
   }
}
