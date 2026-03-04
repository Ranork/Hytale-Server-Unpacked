package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InteractionsUpdate extends ComponentUpdate {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 36864019;
   @Nonnull
   public Map<InteractionType, Integer> interactions = new HashMap<>();
   @Nullable
   public String interactionHint;

   public InteractionsUpdate() {
   }

   public InteractionsUpdate(@Nonnull Map<InteractionType, Integer> interactions, @Nullable String interactionHint) {
      this.interactions = interactions;
      this.interactionHint = interactionHint;
   }

   public InteractionsUpdate(@Nonnull InteractionsUpdate other) {
      this.interactions = other.interactions;
      this.interactionHint = other.interactionHint;
   }

   @Nonnull
   public static InteractionsUpdate deserialize(@Nonnull ByteBuf buf, int offset) {
      InteractionsUpdate obj = new InteractionsUpdate();
      byte nullBits = buf.getByte(offset);
      int varPos0 = offset + 9 + buf.getIntLE(offset + 1);
      int interactionsCount = VarInt.peek(buf, varPos0);
      if (interactionsCount < 0) {
         throw ProtocolException.negativeLength("Interactions", interactionsCount);
      } else if (interactionsCount > 4096000) {
         throw ProtocolException.dictionaryTooLarge("Interactions", interactionsCount, 4096000);
      } else {
         int varIntLen = VarInt.length(buf, varPos0);
         obj.interactions = new HashMap<>(interactionsCount);
         int dictPos = varPos0 + varIntLen;

         for (int i = 0; i < interactionsCount; i++) {
            InteractionType key = InteractionType.fromValue(buf.getByte(dictPos));
            int val = buf.getIntLE(++dictPos);
            dictPos += 4;
            if (obj.interactions.put(key, val) != null) {
               throw ProtocolException.duplicateKey("interactions", key);
            }
         }

         if ((nullBits & 1) != 0) {
            varPos0 = offset + 9 + buf.getIntLE(offset + 5);
            interactionsCount = VarInt.peek(buf, varPos0);
            if (interactionsCount < 0) {
               throw ProtocolException.negativeLength("InteractionHint", interactionsCount);
            }

            if (interactionsCount > 4096000) {
               throw ProtocolException.stringTooLong("InteractionHint", interactionsCount, 4096000);
            }

            obj.interactionHint = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 9;
      int fieldOffset0 = buf.getIntLE(offset + 1);
      int pos0 = offset + 9 + fieldOffset0;
      int dictLen = VarInt.peek(buf, pos0);
      pos0 += VarInt.length(buf, pos0);

      for (int i = 0; i < dictLen; i++) {
         pos0 = ++pos0 + 4;
      }

      if (pos0 - offset > maxEnd) {
         maxEnd = pos0 - offset;
      }

      if ((nullBits & 1) != 0) {
         fieldOffset0 = buf.getIntLE(offset + 5);
         pos0 = offset + 9 + fieldOffset0;
         dictLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + dictLen;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      return maxEnd;
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.interactionHint != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      int interactionsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int interactionHintOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      buf.setIntLE(interactionsOffsetSlot, buf.writerIndex() - varBlockStart);
      if (this.interactions.size() > 4096000) {
         throw ProtocolException.dictionaryTooLarge("Interactions", this.interactions.size(), 4096000);
      } else {
         VarInt.write(buf, this.interactions.size());

         for (Entry<InteractionType, Integer> e : this.interactions.entrySet()) {
            buf.writeByte(e.getKey().getValue());
            buf.writeIntLE(e.getValue());
         }

         if (this.interactionHint != null) {
            buf.setIntLE(interactionHintOffsetSlot, buf.writerIndex() - varBlockStart);
            PacketIO.writeVarString(buf, this.interactionHint, 4096000);
         } else {
            buf.setIntLE(interactionHintOffsetSlot, -1);
         }

         return buf.writerIndex() - startPos;
      }
   }

   @Override
   public int computeSize() {
      int size = 9;
      size += VarInt.size(this.interactions.size()) + this.interactions.size() * 5;
      if (this.interactionHint != null) {
         size += PacketIO.stringSize(this.interactionHint);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int interactionsOffset = buffer.getIntLE(offset + 1);
         if (interactionsOffset < 0) {
            return ValidationResult.error("Invalid offset for Interactions");
         } else {
            int pos = offset + 9 + interactionsOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Interactions");
            } else {
               int interactionsCount = VarInt.peek(buffer, pos);
               if (interactionsCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for Interactions");
               } else if (interactionsCount > 4096000) {
                  return ValidationResult.error("Interactions exceeds max length 4096000");
               } else {
                  pos += VarInt.length(buffer, pos);

                  for (int i = 0; i < interactionsCount; i++) {
                     pos = ++pos + 4;
                     if (pos > buffer.writerIndex()) {
                        return ValidationResult.error("Buffer overflow reading value");
                     }
                  }

                  if ((nullBits & 1) != 0) {
                     interactionsOffset = buffer.getIntLE(offset + 5);
                     if (interactionsOffset < 0) {
                        return ValidationResult.error("Invalid offset for InteractionHint");
                     }

                     pos = offset + 9 + interactionsOffset;
                     if (pos >= buffer.writerIndex()) {
                        return ValidationResult.error("Offset out of bounds for InteractionHint");
                     }

                     interactionsCount = VarInt.peek(buffer, pos);
                     if (interactionsCount < 0) {
                        return ValidationResult.error("Invalid string length for InteractionHint");
                     }

                     if (interactionsCount > 4096000) {
                        return ValidationResult.error("InteractionHint exceeds max length 4096000");
                     }

                     pos += VarInt.length(buffer, pos);
                     pos += interactionsCount;
                     if (pos > buffer.writerIndex()) {
                        return ValidationResult.error("Buffer overflow reading InteractionHint");
                     }
                  }

                  return ValidationResult.OK;
               }
            }
         }
      }
   }

   public InteractionsUpdate clone() {
      InteractionsUpdate copy = new InteractionsUpdate();
      copy.interactions = new HashMap<>(this.interactions);
      copy.interactionHint = this.interactionHint;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof InteractionsUpdate other)
            ? false
            : Objects.equals(this.interactions, other.interactions) && Objects.equals(this.interactionHint, other.interactionHint);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.interactions, this.interactionHint);
   }
}
