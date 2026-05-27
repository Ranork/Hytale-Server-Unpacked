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

public class BenchRequirement {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 6;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 14;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public BenchType type = BenchType.Crafting;
   @Nonnull
   public String id = "";
   @Nullable
   public String[] categories;
   public int requiredTierLevel;

   public BenchRequirement() {
   }

   public BenchRequirement(@Nonnull BenchType type, @Nonnull String id, @Nullable String[] categories, int requiredTierLevel) {
      this.type = type;
      this.id = id;
      this.categories = categories;
      this.requiredTierLevel = requiredTierLevel;
   }

   public BenchRequirement(@Nonnull BenchRequirement other) {
      this.type = other.type;
      this.id = other.id;
      this.categories = other.categories;
      this.requiredTierLevel = other.requiredTierLevel;
   }

   @Nonnull
   public static BenchRequirement deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 14) {
         throw ProtocolException.bufferTooSmall("BenchRequirement", 14, buf.readableBytes() - offset);
      } else {
         BenchRequirement obj = new BenchRequirement();
         byte nullBits = buf.getByte(offset);
         obj.type = BenchType.fromValue(buf.getByte(offset + 1));
         obj.requiredTierLevel = buf.getIntLE(offset + 2);
         int varPosBase0 = buf.getIntLE(offset + 6);
         if (varPosBase0 >= 0 && varPosBase0 <= buf.writerIndex() - offset - 14) {
            int varPos0 = offset + 14 + varPosBase0;
            int idLen = VarInt.peek(buf, varPos0);
            if (idLen < 0) {
               throw ProtocolException.invalidVarInt("Id");
            } else {
               int idVarIntLen = VarInt.size(idLen);
               if (idLen > 4096000) {
                  throw ProtocolException.stringTooLong("Id", idLen, 4096000);
               } else if (varPos0 + idVarIntLen + idLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("Id", varPos0 + idVarIntLen + idLen, buf.readableBytes());
               } else {
                  obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
                  if ((nullBits & 1) != 0) {
                     varPosBase0 = buf.getIntLE(offset + 10);
                     if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 14) {
                        throw ProtocolException.invalidOffset("Categories", varPosBase0, buf.readableBytes());
                     }

                     varPos0 = offset + 14 + varPosBase0;
                     idLen = VarInt.peek(buf, varPos0);
                     if (idLen < 0) {
                        throw ProtocolException.invalidVarInt("Categories");
                     }

                     idVarIntLen = VarInt.size(idLen);
                     if (idLen > 4096000) {
                        throw ProtocolException.arrayTooLong("Categories", idLen, 4096000);
                     }

                     if (varPos0 + idVarIntLen + idLen * 1L > buf.readableBytes()) {
                        throw ProtocolException.bufferTooSmall("Categories", varPos0 + idVarIntLen + idLen * 1, buf.readableBytes());
                     }

                     obj.categories = new String[idLen];
                     int elemPos = varPos0 + idVarIntLen;

                     for (int i = 0; i < idLen; i++) {
                        int strLen = VarInt.peek(buf, elemPos);
                        if (strLen < 0) {
                           throw ProtocolException.invalidVarInt("categories[" + i + "]");
                        }

                        int strVarLen = VarInt.size(strLen);
                        if (strLen > 4096000) {
                           throw ProtocolException.stringTooLong("categories[" + i + "]", strLen, 4096000);
                        }

                        if (elemPos + strVarLen + strLen > buf.readableBytes()) {
                           throw ProtocolException.bufferTooSmall("categories[" + i + "]", elemPos + strVarLen + strLen, buf.readableBytes());
                        }

                        obj.categories[i] = PacketIO.readVarString(buf, elemPos);
                        elemPos += strVarLen + strLen;
                     }
                  }

                  return obj;
               }
            }
         } else {
            throw ProtocolException.invalidOffset("Id", varPosBase0, buf.readableBytes());
         }
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 14;
      int fieldOffset0 = buf.getIntLE(offset + 6);
      if (fieldOffset0 >= 0 && fieldOffset0 <= buf.writerIndex() - offset - 14) {
         int pos0 = offset + 14 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }

         if ((nullBits & 1) != 0) {
            fieldOffset0 = buf.getIntLE(offset + 10);
            if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 14) {
               throw ProtocolException.invalidOffset("Categories", fieldOffset0, maxEnd);
            }

            pos0 = offset + 14 + fieldOffset0;
            sl = VarInt.peek(buf, pos0);
            pos0 += VarInt.size(sl);

            for (int i = 0; i < sl; i++) {
               int slx = VarInt.peek(buf, pos0);
               pos0 += VarInt.size(slx) + slx;
            }

            if (pos0 - offset > maxEnd) {
               maxEnd = pos0 - offset;
            }
         }

         return maxEnd;
      } else {
         throw ProtocolException.invalidOffset("Id", fieldOffset0, maxEnd);
      }
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 14L;
   }

   public static BenchType getType(MemorySegment mem) {
      return getType(mem, 0);
   }

   public static BenchType getType(MemorySegment mem, int offset) {
      return BenchType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   public static String getId(MemorySegment mem) {
      return getId(mem, 0);
   }

   public static String getId(MemorySegment mem, int offset) {
      return PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 6, 14, "Id"), 4096000, PacketIO.UTF8);
   }

   @Nullable
   public static String[] getCategories(MemorySegment mem) {
      return getCategories(mem, 0);
   }

   @Nullable
   public static String[] getCategories(MemorySegment mem, int offset) {
      if (!hasCategories(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 10, 14, "Categories");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Categories", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Categories", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Categories", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               String[] data = new String[len];

               for (int i = 0; i < len; i++) {
                  long sp = VarInt.getWithLength(mem, off);
                  int n = (int)sp + (int)(sp >>> 32);
                  data[i] = PacketIO.readVarString("Categories", mem, off, 16384000, PacketIO.UTF8);
                  off += n;
               }

               return data;
            }
         }
      }
   }

   public static int getRequiredTierLevel(MemorySegment mem) {
      return getRequiredTierLevel(mem, 0);
   }

   public static int getRequiredTierLevel(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 2));
   }

   public static boolean hasCategories(MemorySegment mem, int offset) {
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

   public static BenchRequirement toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BenchRequirement toObject(MemorySegment mem, int offset) {
      if (offset + 14 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BenchRequirement", offset + 14, (int)mem.byteSize());
      } else {
         String[] categories = null;
         if (hasCategories(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 10, 14, "Categories");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Categories", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Categories", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Categories", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            categories = new String[len];

            for (int i = 0; i < len; i++) {
               long sp = VarInt.getWithLength(mem, off);
               int n = (int)sp + (int)(sp >>> 32);
               categories[i] = PacketIO.readVarString("Categories", mem, off, 16384000, PacketIO.UTF8);
               off += n;
            }
         }

         return new BenchRequirement(
            BenchType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))),
            PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 6, 14, "Id"), 4096000, PacketIO.UTF8),
            categories,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 2))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.categories != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.requiredTierLevel);
      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int categoriesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
      PacketIO.writeVarString(buf, this.id, 4096000);
      if (this.categories != null) {
         buf.setIntLE(categoriesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.categories.length > 4096000) {
            throw ProtocolException.arrayTooLong("Categories", this.categories.length, 4096000);
         }

         VarInt.write(buf, this.categories.length);

         for (String item : this.categories) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      } else {
         buf.setIntLE(categoriesOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.categories != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      mem.set(PacketIO.PROTO_INT, (long)(offset + 2), this.requiredTierLevel);
      int varOffset = offset + 14;
      mem.set(PacketIO.PROTO_INT, (long)(offset + 6), varOffset - offset - 14);
      varOffset += PacketIO.writeVarString(mem, varOffset, this.id, 4096000);
      if (this.categories != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 10), varOffset - offset - 14);
         if (this.categories.length > 4096000) {
            throw ProtocolException.arrayTooLong("Categories", this.categories.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.categories.length);
         int categoriesValueOffset = 0;

         for (int i = 0; i < this.categories.length; i++) {
            categoriesValueOffset += PacketIO.writeVarString(mem, varOffset + categoriesValueOffset, this.categories[i], 16384000);
         }

         varOffset += categoriesValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 10), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 14;
      size += PacketIO.stringSize(this.id);
      if (this.categories != null) {
         int categoriesSize = 0;

         for (String elem : this.categories) {
            categoriesSize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.categories.length) + categoriesSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 14) {
         return ValidationResult.error("Buffer too small: expected at least 14 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 4) {
            return ValidationResult.error("Invalid BenchType value for Type");
         } else {
            v = buffer.getIntLE(offset + 6);
            if (v >= 0 && v <= buffer.writerIndex() - offset - 14) {
               int pos = offset + 14 + v;
               int idLen = VarInt.peek(buffer, pos);
               if (idLen < 0) {
                  return ValidationResult.error("Invalid string length for Id");
               } else if (idLen > 4096000) {
                  return ValidationResult.error("Id exceeds max length 4096000");
               } else {
                  pos += VarInt.size(idLen);
                  pos += idLen;
                  if (pos > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading Id");
                  } else {
                     if ((nullBits & 1) != 0) {
                        v = buffer.getIntLE(offset + 10);
                        if (v < 0 || v > buffer.writerIndex() - offset - 14) {
                           return ValidationResult.error("Invalid offset for Categories");
                        }

                        pos = offset + 14 + v;
                        idLen = VarInt.peek(buffer, pos);
                        if (idLen < 0) {
                           return ValidationResult.error("Invalid array count for Categories");
                        }

                        if (idLen > 4096000) {
                           return ValidationResult.error("Categories exceeds max length 4096000");
                        }

                        pos += VarInt.size(idLen);

                        for (int i = 0; i < idLen; i++) {
                           int strLen = VarInt.peek(buffer, pos);
                           if (strLen < 0) {
                              return ValidationResult.error("Invalid string length in Categories");
                           }

                           pos += VarInt.size(strLen);
                           pos += strLen;
                           if (pos > buffer.writerIndex()) {
                              return ValidationResult.error("Buffer overflow reading string in Categories");
                           }
                        }
                     }

                     return ValidationResult.OK;
                  }
               }
            } else {
               return ValidationResult.error("Invalid offset for Id");
            }
         }
      }
   }

   public BenchRequirement clone() {
      BenchRequirement copy = new BenchRequirement();
      copy.type = this.type;
      copy.id = this.id;
      copy.categories = this.categories != null ? Arrays.copyOf(this.categories, this.categories.length) : null;
      copy.requiredTierLevel = this.requiredTierLevel;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BenchRequirement other)
            ? false
            : Objects.equals(this.type, other.type)
               && Objects.equals(this.id, other.id)
               && Arrays.equals((Object[])this.categories, (Object[])other.categories)
               && this.requiredTierLevel == other.requiredTierLevel;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.type);
      result = 31 * result + Objects.hashCode(this.id);
      result = 31 * result + Arrays.hashCode((Object[])this.categories);
      return 31 * result + Integer.hashCode(this.requiredTierLevel);
   }
}
