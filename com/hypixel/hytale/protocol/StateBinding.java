package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StateBinding {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 53248010;
   public int audioStateIndex;
   @Nullable
   public StateDelta[] deltas;

   public StateBinding() {
   }

   public StateBinding(int audioStateIndex, @Nullable StateDelta[] deltas) {
      this.audioStateIndex = audioStateIndex;
      this.deltas = deltas;
   }

   public StateBinding(@Nonnull StateBinding other) {
      this.audioStateIndex = other.audioStateIndex;
      this.deltas = other.deltas;
   }

   @Nonnull
   public static StateBinding deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 5) {
         throw ProtocolException.bufferTooSmall("StateBinding", 5, buf.readableBytes() - offset);
      } else {
         StateBinding obj = new StateBinding();
         byte nullBits = buf.getByte(offset);
         obj.audioStateIndex = buf.getIntLE(offset + 1);
         int pos = offset + 5;
         if ((nullBits & 1) != 0) {
            int deltasCount = VarInt.peek(buf, pos);
            if (deltasCount < 0) {
               throw ProtocolException.invalidVarInt("Deltas");
            }

            int deltasVarLen = VarInt.size(deltasCount);
            if (deltasCount > 4096000) {
               throw ProtocolException.arrayTooLong("Deltas", deltasCount, 4096000);
            }

            if (pos + deltasVarLen + deltasCount * 13L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Deltas", pos + deltasVarLen + deltasCount * 13, buf.readableBytes());
            }

            pos += deltasVarLen;
            obj.deltas = new StateDelta[deltasCount];

            for (int i = 0; i < deltasCount; i++) {
               obj.deltas[i] = StateDelta.deserialize(buf, pos);
               pos += StateDelta.computeBytesConsumed(buf, pos);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos += StateDelta.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 5L;
   }

   public static int getAudioStateIndex(MemorySegment mem) {
      return getAudioStateIndex(mem, 0);
   }

   public static int getAudioStateIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   @Nullable
   public static StateDelta[] getDeltas(MemorySegment mem) {
      return getDeltas(mem, 0);
   }

   @Nullable
   public static StateDelta[] getDeltas(MemorySegment mem, int offset) {
      if (!hasDeltas(mem, offset)) {
         return null;
      } else {
         int off = offset + 5;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Deltas", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Deltas", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 13L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Deltas", off + lenOffset + len * 13, (int)mem.byteSize());
            } else {
               off += lenOffset;
               StateDelta[] data = new StateDelta[len];

               for (int i = 0; i < len; i++) {
                  data[i] = StateDelta.toObject(mem, off + i * 13);
               }

               return data;
            }
         }
      }
   }

   public static boolean hasDeltas(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static StateBinding toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static StateBinding toObject(MemorySegment mem, int offset) {
      if (offset + 5 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("StateBinding", offset + 5, (int)mem.byteSize());
      } else {
         StateDelta[] deltas = null;
         if (hasDeltas(mem, offset)) {
            int off = offset + 5;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Deltas", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Deltas", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 13L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Deltas", off + lenOffset + len * 13, (int)mem.byteSize());
            }

            off += lenOffset;
            deltas = new StateDelta[len];

            for (int i = 0; i < len; i++) {
               deltas[i] = StateDelta.toObject(mem, off + i * 13);
            }
         }

         return new StateBinding(mem.get(PacketIO.PROTO_INT, (long)(offset + 1)), deltas);
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.deltas != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.audioStateIndex);
      if (this.deltas != null) {
         if (this.deltas.length > 4096000) {
            throw ProtocolException.arrayTooLong("Deltas", this.deltas.length, 4096000);
         }

         VarInt.write(buf, this.deltas.length);

         for (StateDelta item : this.deltas) {
            item.serialize(buf);
         }
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.deltas != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.audioStateIndex);
      int varOffset = offset + 5;
      if (this.deltas != null) {
         if (this.deltas.length > 4096000) {
            throw ProtocolException.arrayTooLong("Deltas", this.deltas.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.deltas.length);
         int deltasValueOffset = 0;

         for (int i = 0; i < this.deltas.length; i++) {
            deltasValueOffset += this.deltas[i].serialize(mem, varOffset + deltasValueOffset);
         }

         varOffset += deltasValueOffset;
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 5;
      if (this.deltas != null) {
         size += VarInt.size(this.deltas.length) + this.deltas.length * 13;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 5) {
         return ValidationResult.error("Buffer too small: expected at least 5 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 5;
         if ((nullBits & 1) != 0) {
            int deltasCount = VarInt.peek(buffer, pos);
            if (deltasCount < 0) {
               return ValidationResult.error("Invalid array count for Deltas");
            }

            if (deltasCount > 4096000) {
               return ValidationResult.error("Deltas exceeds max length 4096000");
            }

            pos += VarInt.size(deltasCount);
            pos += deltasCount * 13;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Deltas");
            }
         }

         return ValidationResult.OK;
      }
   }

   public StateBinding clone() {
      StateBinding copy = new StateBinding();
      copy.audioStateIndex = this.audioStateIndex;
      copy.deltas = this.deltas != null ? Arrays.stream(this.deltas).map(e -> e.clone()).toArray(StateDelta[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof StateBinding other)
            ? false
            : this.audioStateIndex == other.audioStateIndex && Arrays.equals((Object[])this.deltas, (Object[])other.deltas);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Integer.hashCode(this.audioStateIndex);
      return 31 * result + Arrays.hashCode((Object[])this.deltas);
   }
}
