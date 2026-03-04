package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class NameplateUpdate extends ComponentUpdate {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 0;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 0;
   public static final int MAX_SIZE = 16384005;
   @Nonnull
   public String text = "";

   public NameplateUpdate() {
   }

   public NameplateUpdate(@Nonnull String text) {
      this.text = text;
   }

   public NameplateUpdate(@Nonnull NameplateUpdate other) {
      this.text = other.text;
   }

   @Nonnull
   public static NameplateUpdate deserialize(@Nonnull ByteBuf buf, int offset) {
      NameplateUpdate obj = new NameplateUpdate();
      int pos = offset + 0;
      int textLen = VarInt.peek(buf, pos);
      if (textLen < 0) {
         throw ProtocolException.negativeLength("Text", textLen);
      } else if (textLen > 4096000) {
         throw ProtocolException.stringTooLong("Text", textLen, 4096000);
      } else {
         int textVarLen = VarInt.length(buf, pos);
         obj.text = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
         pos += textVarLen + textLen;
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int pos = offset + 0;
      int sl = VarInt.peek(buf, pos);
      pos += VarInt.length(buf, pos) + sl;
      return pos - offset;
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      PacketIO.writeVarString(buf, this.text, 4096000);
      return buf.writerIndex() - startPos;
   }

   @Override
   public int computeSize() {
      int size = 0;
      return size + PacketIO.stringSize(this.text);
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 0) {
         return ValidationResult.error("Buffer too small: expected at least 0 bytes");
      } else {
         int pos = offset + 0;
         int textLen = VarInt.peek(buffer, pos);
         if (textLen < 0) {
            return ValidationResult.error("Invalid string length for Text");
         } else if (textLen > 4096000) {
            return ValidationResult.error("Text exceeds max length 4096000");
         } else {
            pos += VarInt.length(buffer, pos);
            pos += textLen;
            return pos > buffer.writerIndex() ? ValidationResult.error("Buffer overflow reading Text") : ValidationResult.OK;
         }
      }
   }

   public NameplateUpdate clone() {
      NameplateUpdate copy = new NameplateUpdate();
      copy.text = this.text;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof NameplateUpdate other ? Objects.equals(this.text, other.text) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.text);
   }
}
