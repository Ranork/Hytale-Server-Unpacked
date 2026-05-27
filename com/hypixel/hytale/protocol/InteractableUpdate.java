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
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("InteractableUpdate", 1, buf.readableBytes() - offset);
      } else {
         InteractableUpdate obj = new InteractableUpdate();
         byte nullBits = buf.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int interactionHintLen = VarInt.peek(buf, pos);
            if (interactionHintLen < 0) {
               throw ProtocolException.invalidVarInt("InteractionHint");
            }

            int interactionHintVarLen = VarInt.size(interactionHintLen);
            if (interactionHintLen > 4096000) {
               throw ProtocolException.stringTooLong("InteractionHint", interactionHintLen, 4096000);
            }

            if (pos + interactionHintVarLen + interactionHintLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("InteractionHint", pos + interactionHintVarLen + interactionHintLen, buf.readableBytes());
            }

            obj.interactionHint = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
            pos += interactionHintVarLen + interactionHintLen;
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.size(sl) + sl;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   @Nullable
   public static String getInteractionHint(MemorySegment mem) {
      return getInteractionHint(mem, 0);
   }

   @Nullable
   public static String getInteractionHint(MemorySegment mem, int offset) {
      return hasInteractionHint(mem, offset) ? PacketIO.readVarString("InteractionHint", mem, offset + 1, 4096000, PacketIO.UTF8) : null;
   }

   public static boolean hasInteractionHint(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static InteractableUpdate toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static InteractableUpdate toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("InteractableUpdate", offset + 1, (int)mem.byteSize());
      } else {
         return new InteractableUpdate(
            hasInteractionHint(mem, offset) ? PacketIO.readVarString("InteractionHint", mem, offset + 1, 4096000, PacketIO.UTF8) : null
         );
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
      if (this.interactionHint != null) {
         PacketIO.writeVarString(buf, this.interactionHint, 4096000);
      }

      return buf.writerIndex() - startPos;
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.interactionHint != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 1;
      if (this.interactionHint != null) {
         varOffset += PacketIO.writeVarString(mem, varOffset, this.interactionHint, 4096000);
      }

      return varOffset - offset;
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

            pos += VarInt.size(interactionHintLen);
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
