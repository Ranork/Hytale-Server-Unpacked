package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InteractableUpdate extends ComponentUpdate {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 16384006;
   @Nullable
   public String interactionHint;

   public InteractableUpdate() {
   }

   public InteractableUpdate(@Nullable String interactionHint) {
      this.interactionHint = interactionHint;
   }

   public InteractableUpdate(@Nonnull InteractableUpdate other) {
      this.interactionHint = other.interactionHint;
   }

   @Nonnull
   public static InteractableUpdate deserialize(@Nonnull ByteBuf buf, int offset) {
      InteractableUpdate obj = new InteractableUpdate();
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int interactionHintLen = VarInt.peek(buf, pos);
         if (interactionHintLen < 0) {
            throw ProtocolException.negativeLength("InteractionHint", interactionHintLen);
         }

         if (interactionHintLen > 4096000) {
            throw ProtocolException.stringTooLong("InteractionHint", interactionHintLen, 4096000);
         }

         int interactionHintVarLen = VarInt.length(buf, pos);
         obj.interactionHint = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
         pos += interactionHintVarLen + interactionHintLen;
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos) + sl;
      }

      return pos - offset;
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.interactionHint != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.interactionHint != null) {
         PacketIO.writeVarString(buf, this.interactionHint, 4096000);
      }

      return buf.writerIndex() - startPos;
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.interactionHint != null) {
         size += PacketIO.stringSize(this.interactionHint);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 1) {
         return ValidationResult.error("Buffer too small: expected at least 1 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int interactionHintLen = VarInt.peek(buffer, pos);
            if (interactionHintLen < 0) {
               return ValidationResult.error("Invalid string length for InteractionHint");
            }

            if (interactionHintLen > 4096000) {
               return ValidationResult.error("InteractionHint exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += interactionHintLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading InteractionHint");
            }
         }

         return ValidationResult.OK;
      }
   }

   public InteractableUpdate clone() {
      InteractableUpdate copy = new InteractableUpdate();
      copy.interactionHint = this.interactionHint;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof InteractableUpdate other ? Objects.equals(this.interactionHint, other.interactionHint) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.interactionHint);
   }
}
