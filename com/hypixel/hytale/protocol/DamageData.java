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

public class DamageData {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public DamageEntry[] entries;

   public DamageData() {
   }

   public DamageData(@Nullable DamageEntry[] entries) {
      this.entries = entries;
   }

   public DamageData(@Nonnull DamageData other) {
      this.entries = other.entries;
   }

   @Nonnull
   public static DamageData deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("DamageData", 1, buf.readableBytes() - offset);
      } else {
         DamageData obj = new DamageData();
         byte nullBits = buf.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int entriesCount = VarInt.peek(buf, pos);
            if (entriesCount < 0) {
               throw ProtocolException.invalidVarInt("Entries");
            }

            int entriesVarLen = VarInt.size(entriesCount);
            if (entriesCount > 4096000) {
               throw ProtocolException.arrayTooLong("Entries", entriesCount, 4096000);
            }

            if (pos + entriesVarLen + entriesCount * 9L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Entries", pos + entriesVarLen + entriesCount * 9, buf.readableBytes());
            }

            pos += entriesVarLen;
            obj.entries = new DamageEntry[entriesCount];

            for (int i = 0; i < entriesCount; i++) {
               obj.entries[i] = DamageEntry.deserialize(buf, pos);
               pos += DamageEntry.computeBytesConsumed(buf, pos);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos += DamageEntry.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   @Nullable
   public static DamageEntry[] getEntries(MemorySegment mem) {
      return getEntries(mem, 0);
   }

   @Nullable
   public static DamageEntry[] getEntries(MemorySegment mem, int offset) {
      if (!hasEntries(mem, offset)) {
         return null;
      } else {
         int off = offset + 1;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Entries", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Entries", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Entries", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               DamageEntry[] data = new DamageEntry[len];

               for (int i = 0; i < len; i++) {
                  data[i] = DamageEntry.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   public static boolean hasEntries(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static DamageData toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static DamageData toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("DamageData", offset + 1, (int)mem.byteSize());
      } else {
         DamageEntry[] entries = null;
         if (hasEntries(mem, offset)) {
            int off = offset + 1;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Entries", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Entries", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Entries", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            entries = new DamageEntry[len];

            for (int i = 0; i < len; i++) {
               entries[i] = DamageEntry.toObject(mem, off);
               off += entries[i].computeSize();
            }
         }

         return new DamageData(entries);
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.entries != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.entries != null) {
         if (this.entries.length > 4096000) {
            throw ProtocolException.arrayTooLong("Entries", this.entries.length, 4096000);
         }

         VarInt.write(buf, this.entries.length);

         for (DamageEntry item : this.entries) {
            item.serialize(buf);
         }
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.entries != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 1;
      if (this.entries != null) {
         if (this.entries.length > 4096000) {
            throw ProtocolException.arrayTooLong("Entries", this.entries.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.entries.length);
         int entriesValueOffset = 0;

         for (int i = 0; i < this.entries.length; i++) {
            entriesValueOffset += this.entries[i].serialize(mem, varOffset + entriesValueOffset);
         }

         varOffset += entriesValueOffset;
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 1;
      if (this.entries != null) {
         int entriesSize = 0;

         for (DamageEntry elem : this.entries) {
            entriesSize += elem.computeSize();
         }

         size += VarInt.size(this.entries.length) + entriesSize;
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
            int entriesCount = VarInt.peek(buffer, pos);
            if (entriesCount < 0) {
               return ValidationResult.error("Invalid array count for Entries");
            }

            if (entriesCount > 4096000) {
               return ValidationResult.error("Entries exceeds max length 4096000");
            }

            pos += VarInt.size(entriesCount);

            for (int i = 0; i < entriesCount; i++) {
               ValidationResult structResult = DamageEntry.validateStructure(buffer, pos);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid DamageEntry in Entries[" + i + "]: " + structResult.error());
               }

               pos += DamageEntry.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public DamageData clone() {
      DamageData copy = new DamageData();
      copy.entries = this.entries != null ? Arrays.stream(this.entries).map(e -> e.clone()).toArray(DamageEntry[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof DamageData other ? Arrays.equals((Object[])this.entries, (Object[])other.entries) : false;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + Arrays.hashCode((Object[])this.entries);
   }
}
