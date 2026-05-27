package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
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
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("InteractionsUpdate", 9, buf.readableBytes() - offset);
      } else {
         InteractionsUpdate obj = new InteractionsUpdate();
         byte nullBits = buf.getByte(offset);
         int varPosBase0 = buf.getIntLE(offset + 1);
         if (varPosBase0 >= 0 && varPosBase0 <= buf.writerIndex() - offset - 9) {
            int varPos0 = offset + 9 + varPosBase0;
            int interactionsCount = VarInt.peek(buf, varPos0);
            if (interactionsCount < 0) {
               throw ProtocolException.invalidVarInt("Interactions");
            } else {
               int varIntLen = VarInt.size(interactionsCount);
               if (interactionsCount > 4096000) {
                  throw ProtocolException.dictionaryTooLarge("Interactions", interactionsCount, 4096000);
               } else {
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
                     varPosBase0 = buf.getIntLE(offset + 5);
                     if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 9) {
                        throw ProtocolException.invalidOffset("InteractionHint", varPosBase0, buf.readableBytes());
                     }

                     varPos0 = offset + 9 + varPosBase0;
                     interactionsCount = VarInt.peek(buf, varPos0);
                     if (interactionsCount < 0) {
                        throw ProtocolException.invalidVarInt("InteractionHint");
                     }

                     varIntLen = VarInt.size(interactionsCount);
                     if (interactionsCount > 4096000) {
                        throw ProtocolException.stringTooLong("InteractionHint", interactionsCount, 4096000);
                     }

                     if (varPos0 + varIntLen + interactionsCount > buf.readableBytes()) {
                        throw ProtocolException.bufferTooSmall("InteractionHint", varPos0 + varIntLen + interactionsCount, buf.readableBytes());
                     }

                     obj.interactionHint = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
                  }

                  return obj;
               }
            }
         } else {
            throw ProtocolException.invalidOffset("Interactions", varPosBase0, buf.readableBytes());
         }
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 9;
      int fieldOffset0 = buf.getIntLE(offset + 1);
      if (fieldOffset0 >= 0 && fieldOffset0 <= buf.writerIndex() - offset - 9) {
         int pos0 = offset + 9 + fieldOffset0;
         int dictLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos0 = ++pos0 + 4;
         }

         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }

         if ((nullBits & 1) != 0) {
            fieldOffset0 = buf.getIntLE(offset + 5);
            if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("InteractionHint", fieldOffset0, maxEnd);
            }

            pos0 = offset + 9 + fieldOffset0;
            dictLen = VarInt.peek(buf, pos0);
            pos0 += VarInt.size(dictLen) + dictLen;
            if (pos0 - offset > maxEnd) {
               maxEnd = pos0 - offset;
            }
         }

         return maxEnd;
      } else {
         throw ProtocolException.invalidOffset("Interactions", fieldOffset0, maxEnd);
      }
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 9L;
   }

   public static Map<InteractionType, Integer> getInteractions(MemorySegment mem) {
      return getInteractions(mem, 0);
   }

   public static Map<InteractionType, Integer> getInteractions(MemorySegment mem, int offset) {
      int off = offset + getValidatedOffset(mem, offset, 1, 9, "Interactions");
      long packed = VarInt.getWithLength(mem, off);
      int len = (int)packed;
      if (len < 0) {
         throw ProtocolException.negativeLength("Interactions", len);
      } else if (len > 4096000) {
         throw ProtocolException.dictionaryTooLarge("Interactions", len, 4096000);
      } else {
         Map<InteractionType, Integer> data = new HashMap<>(len);
         off += (int)(packed >>> 32);

         for (int i = 0; i < len; i++) {
            InteractionType key = InteractionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)off));
            int value = mem.get(PacketIO.PROTO_INT, (long)(++off));
            off += 4;
            if (data.put(key, value) != null) {
               throw ProtocolException.duplicateKey("Interactions", key);
            }
         }

         return data;
      }
   }

   @Nullable
   public static String getInteractionHint(MemorySegment mem) {
      return getInteractionHint(mem, 0);
   }

   @Nullable
   public static String getInteractionHint(MemorySegment mem, int offset) {
      return hasInteractionHint(mem, offset)
         ? PacketIO.readVarString("InteractionHint", mem, offset + getValidatedOffset(mem, offset, 5, 9, "InteractionHint"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static boolean hasInteractionHint(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static InteractionsUpdate toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static InteractionsUpdate toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("InteractionsUpdate", offset + 9, (int)mem.byteSize());
      } else {
         int off = offset + getValidatedOffset(mem, offset, 1, 9, "Interactions");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Interactions", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Interactions", len, 4096000);
         } else {
            Map<InteractionType, Integer> interactions = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               InteractionType key = InteractionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)off));
               int value = mem.get(PacketIO.PROTO_INT, (long)(++off));
               off += 4;
               if (interactions.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Interactions", key);
               }
            }

            return new InteractionsUpdate(
               interactions,
               hasInteractionHint(mem, offset)
                  ? PacketIO.readVarString("InteractionHint", mem, offset + getValidatedOffset(mem, offset, 5, 9, "InteractionHint"), 4096000, PacketIO.UTF8)
                  : null
            );
         }
      }
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
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.interactionHint != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 9;
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 9);
      if (this.interactions.size() > 4096000) {
         throw ProtocolException.dictionaryTooLarge("Interactions", this.interactions.size(), 4096000);
      } else {
         varOffset += VarInt.set(mem, varOffset, this.interactions.size());

         for (Entry<InteractionType, Integer> e : this.interactions.entrySet()) {
            mem.set(PacketIO.PROTO_BYTE, (long)varOffset, (byte)e.getKey().getValue());
            mem.set(PacketIO.PROTO_INT, (long)(++varOffset), e.getValue());
            varOffset += 4;
         }

         if (this.interactionHint != null) {
            mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 9);
            varOffset += PacketIO.writeVarString(mem, varOffset, this.interactionHint, 4096000);
         } else {
            mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
         }

         return varOffset - offset;
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
         if (interactionsOffset >= 0 && interactionsOffset <= buffer.writerIndex() - offset - 9) {
            int pos = offset + 9 + interactionsOffset;
            int interactionsCount = VarInt.peek(buffer, pos);
            if (interactionsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Interactions");
            } else if (interactionsCount > 4096000) {
               return ValidationResult.error("Interactions exceeds max length 4096000");
            } else {
               pos += VarInt.size(interactionsCount);

               for (int i = 0; i < interactionsCount; i++) {
                  int v = buffer.getByte(pos) & 255;
                  if (v >= 25) {
                     return ValidationResult.error("Invalid InteractionType value for key");
                  }

                  pos = ++pos + 4;
                  if (pos > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading value");
                  }
               }

               if ((nullBits & 1) != 0) {
                  interactionsOffset = buffer.getIntLE(offset + 5);
                  if (interactionsOffset < 0 || interactionsOffset > buffer.writerIndex() - offset - 9) {
                     return ValidationResult.error("Invalid offset for InteractionHint");
                  }

                  pos = offset + 9 + interactionsOffset;
                  interactionsCount = VarInt.peek(buffer, pos);
                  if (interactionsCount < 0) {
                     return ValidationResult.error("Invalid string length for InteractionHint");
                  }

                  if (interactionsCount > 4096000) {
                     return ValidationResult.error("InteractionHint exceeds max length 4096000");
                  }

                  pos += VarInt.size(interactionsCount);
                  pos += interactionsCount;
                  if (pos > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading InteractionHint");
                  }
               }

               return ValidationResult.OK;
            }
         } else {
            return ValidationResult.error("Invalid offset for Interactions");
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
