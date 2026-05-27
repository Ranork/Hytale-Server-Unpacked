package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AmbienceFXAmbientBed {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 6;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 14;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String track;
   public float volume;
   @Nonnull
   public AmbienceTransitionSpeed transitionSpeed = AmbienceTransitionSpeed.Default;
   @Nullable
   public StateBinding[] stateBindings;

   public AmbienceFXAmbientBed() {
   }

   public AmbienceFXAmbientBed(@Nullable String track, float volume, @Nonnull AmbienceTransitionSpeed transitionSpeed, @Nullable StateBinding[] stateBindings) {
      this.track = track;
      this.volume = volume;
      this.transitionSpeed = transitionSpeed;
      this.stateBindings = stateBindings;
   }

   public AmbienceFXAmbientBed(@Nonnull AmbienceFXAmbientBed other) {
      this.track = other.track;
      this.volume = other.volume;
      this.transitionSpeed = other.transitionSpeed;
      this.stateBindings = other.stateBindings;
   }

   @Nonnull
   public static AmbienceFXAmbientBed deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 14) {
         throw ProtocolException.bufferTooSmall("AmbienceFXAmbientBed", 14, buf.readableBytes() - offset);
      } else {
         AmbienceFXAmbientBed obj = new AmbienceFXAmbientBed();
         byte nullBits = buf.getByte(offset);
         obj.volume = buf.getFloatLE(offset + 1);
         obj.transitionSpeed = AmbienceTransitionSpeed.fromValue(buf.getByte(offset + 5));
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 6);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 14) {
               throw ProtocolException.invalidOffset("Track", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 14 + varPosBase0;
            int trackLen = VarInt.peek(buf, varPos0);
            if (trackLen < 0) {
               throw ProtocolException.invalidVarInt("Track");
            }

            int trackVarIntLen = VarInt.size(trackLen);
            if (trackLen > 4096000) {
               throw ProtocolException.stringTooLong("Track", trackLen, 4096000);
            }

            if (varPos0 + trackVarIntLen + trackLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Track", varPos0 + trackVarIntLen + trackLen, buf.readableBytes());
            }

            obj.track = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 10);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 14) {
               throw ProtocolException.invalidOffset("StateBindings", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 14 + varPosBase1;
            int stateBindingsCount = VarInt.peek(buf, varPos1);
            if (stateBindingsCount < 0) {
               throw ProtocolException.invalidVarInt("StateBindings");
            }

            int varIntLen = VarInt.size(stateBindingsCount);
            if (stateBindingsCount > 4096000) {
               throw ProtocolException.arrayTooLong("StateBindings", stateBindingsCount, 4096000);
            }

            if (varPos1 + varIntLen + stateBindingsCount * 5L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("StateBindings", varPos1 + varIntLen + stateBindingsCount * 5, buf.readableBytes());
            }

            obj.stateBindings = new StateBinding[stateBindingsCount];
            int elemPos = varPos1 + varIntLen;

            for (int i = 0; i < stateBindingsCount; i++) {
               obj.stateBindings[i] = StateBinding.deserialize(buf, elemPos);
               elemPos += StateBinding.computeBytesConsumed(buf, elemPos);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 14;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 6);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 14) {
            throw ProtocolException.invalidOffset("Track", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 14 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 10);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 14) {
            throw ProtocolException.invalidOffset("StateBindings", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 14 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos1 += StateBinding.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 14L;
   }

   @Nullable
   public static String getTrack(MemorySegment mem) {
      return getTrack(mem, 0);
   }

   @Nullable
   public static String getTrack(MemorySegment mem, int offset) {
      return hasTrack(mem, offset)
         ? PacketIO.readVarString("Track", mem, offset + getValidatedOffset(mem, offset, 6, 14, "Track"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static float getVolume(MemorySegment mem) {
      return getVolume(mem, 0);
   }

   public static float getVolume(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1));
   }

   public static AmbienceTransitionSpeed getTransitionSpeed(MemorySegment mem) {
      return getTransitionSpeed(mem, 0);
   }

   public static AmbienceTransitionSpeed getTransitionSpeed(MemorySegment mem, int offset) {
      return AmbienceTransitionSpeed.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 5)));
   }

   @Nullable
   public static StateBinding[] getStateBindings(MemorySegment mem) {
      return getStateBindings(mem, 0);
   }

   @Nullable
   public static StateBinding[] getStateBindings(MemorySegment mem, int offset) {
      if (!hasStateBindings(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 10, 14, "StateBindings");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("StateBindings", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("StateBindings", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("StateBindings", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               StateBinding[] data = new StateBinding[len];

               for (int i = 0; i < len; i++) {
                  data[i] = StateBinding.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   public static boolean hasTrack(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasStateBindings(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static AmbienceFXAmbientBed toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AmbienceFXAmbientBed toObject(MemorySegment mem, int offset) {
      if (offset + 14 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AmbienceFXAmbientBed", offset + 14, (int)mem.byteSize());
      } else {
         StateBinding[] stateBindings = null;
         if (hasStateBindings(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 10, 14, "StateBindings");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("StateBindings", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("StateBindings", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("StateBindings", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            stateBindings = new StateBinding[len];

            for (int i = 0; i < len; i++) {
               stateBindings[i] = StateBinding.toObject(mem, off);
               off += stateBindings[i].computeSize();
            }
         }

         return new AmbienceFXAmbientBed(
            hasTrack(mem, offset)
               ? PacketIO.readVarString("Track", mem, offset + getValidatedOffset(mem, offset, 6, 14, "Track"), 4096000, PacketIO.UTF8)
               : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1)),
            AmbienceTransitionSpeed.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 5))),
            stateBindings
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.track != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.stateBindings != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.volume);
      buf.writeByte(this.transitionSpeed.getValue());
      int trackOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int stateBindingsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.track != null) {
         buf.setIntLE(trackOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.track, 4096000);
      } else {
         buf.setIntLE(trackOffsetSlot, -1);
      }

      if (this.stateBindings != null) {
         buf.setIntLE(stateBindingsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.stateBindings.length > 4096000) {
            throw ProtocolException.arrayTooLong("StateBindings", this.stateBindings.length, 4096000);
         }

         VarInt.write(buf, this.stateBindings.length);

         for (StateBinding item : this.stateBindings) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(stateBindingsOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.track != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.stateBindings != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 1), this.volume);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 5), (byte)this.transitionSpeed.getValue());
      int varOffset = offset + 14;
      if (this.track != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), varOffset - offset - 14);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.track, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), -1);
      }

      if (this.stateBindings != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 10), varOffset - offset - 14);
         if (this.stateBindings.length > 4096000) {
            throw ProtocolException.arrayTooLong("StateBindings", this.stateBindings.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.stateBindings.length);
         int stateBindingsValueOffset = 0;

         for (int i = 0; i < this.stateBindings.length; i++) {
            stateBindingsValueOffset += this.stateBindings[i].serialize(mem, varOffset + stateBindingsValueOffset);
         }

         varOffset += stateBindingsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 10), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 14;
      if (this.track != null) {
         size += PacketIO.stringSize(this.track);
      }

      if (this.stateBindings != null) {
         int stateBindingsSize = 0;

         for (StateBinding elem : this.stateBindings) {
            stateBindingsSize += elem.computeSize();
         }

         size += VarInt.size(this.stateBindings.length) + stateBindingsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 14) {
         return ValidationResult.error("Buffer too small: expected at least 14 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 5) & 255;
         if (v >= 3) {
            return ValidationResult.error("Invalid AmbienceTransitionSpeed value for TransitionSpeed");
         } else {
            if ((nullBits & 1) != 0) {
               v = buffer.getIntLE(offset + 6);
               if (v < 0 || v > buffer.writerIndex() - offset - 14) {
                  return ValidationResult.error("Invalid offset for Track");
               }

               int pos = offset + 14 + v;
               int trackLen = VarInt.peek(buffer, pos);
               if (trackLen < 0) {
                  return ValidationResult.error("Invalid string length for Track");
               }

               if (trackLen > 4096000) {
                  return ValidationResult.error("Track exceeds max length 4096000");
               }

               pos += VarInt.size(trackLen);
               pos += trackLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Track");
               }
            }

            if ((nullBits & 2) != 0) {
               v = buffer.getIntLE(offset + 10);
               if (v < 0 || v > buffer.writerIndex() - offset - 14) {
                  return ValidationResult.error("Invalid offset for StateBindings");
               }

               int posx = offset + 14 + v;
               int stateBindingsCount = VarInt.peek(buffer, posx);
               if (stateBindingsCount < 0) {
                  return ValidationResult.error("Invalid array count for StateBindings");
               }

               if (stateBindingsCount > 4096000) {
                  return ValidationResult.error("StateBindings exceeds max length 4096000");
               }

               posx += VarInt.size(stateBindingsCount);

               for (int i = 0; i < stateBindingsCount; i++) {
                  ValidationResult structResult = StateBinding.validateStructure(buffer, posx);
                  if (!structResult.isValid()) {
                     return ValidationResult.error("Invalid StateBinding in StateBindings[" + i + "]: " + structResult.error());
                  }

                  posx += StateBinding.computeBytesConsumed(buffer, posx);
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public AmbienceFXAmbientBed clone() {
      AmbienceFXAmbientBed copy = new AmbienceFXAmbientBed();
      copy.track = this.track;
      copy.volume = this.volume;
      copy.transitionSpeed = this.transitionSpeed;
      copy.stateBindings = this.stateBindings != null ? Arrays.stream(this.stateBindings).map(e -> e.clone()).toArray(StateBinding[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AmbienceFXAmbientBed other)
            ? false
            : Objects.equals(this.track, other.track)
               && this.volume == other.volume
               && Objects.equals(this.transitionSpeed, other.transitionSpeed)
               && Arrays.equals((Object[])this.stateBindings, (Object[])other.stateBindings);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.track);
      result = 31 * result + Float.hashCode(this.volume);
      result = 31 * result + Objects.hashCode(this.transitionSpeed);
      return 31 * result + Arrays.hashCode((Object[])this.stateBindings);
   }
}
