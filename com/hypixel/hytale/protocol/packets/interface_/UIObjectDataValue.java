package com.hypixel.hytale.protocol.packets.interface_;

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

public class UIObjectDataValue extends UIDataValue {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 0;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 8;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public String typeName = "";
   @Nonnull
   public Map<String, UIDataValue> properties = new HashMap<>();

   public UIObjectDataValue() {
   }

   public UIObjectDataValue(@Nonnull String typeName, @Nonnull Map<String, UIDataValue> properties) {
      this.typeName = typeName;
      this.properties = properties;
   }

   public UIObjectDataValue(@Nonnull UIObjectDataValue other) {
      this.typeName = other.typeName;
      this.properties = other.properties;
   }

   @Nonnull
   public static UIObjectDataValue deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 8) {
         throw ProtocolException.bufferTooSmall("UIObjectDataValue", 8, buf.readableBytes() - offset);
      } else {
         UIObjectDataValue obj = new UIObjectDataValue();
         int varPosBase0 = buf.getIntLE(offset + 0);
         if (varPosBase0 >= 0 && varPosBase0 <= buf.writerIndex() - offset - 8) {
            int varPos0 = offset + 8 + varPosBase0;
            int typeNameLen = VarInt.peek(buf, varPos0);
            if (typeNameLen < 0) {
               throw ProtocolException.invalidVarInt("TypeName");
            } else {
               int typeNameVarIntLen = VarInt.size(typeNameLen);
               if (typeNameLen > 4096000) {
                  throw ProtocolException.stringTooLong("TypeName", typeNameLen, 4096000);
               } else if (varPos0 + typeNameVarIntLen + typeNameLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("TypeName", varPos0 + typeNameVarIntLen + typeNameLen, buf.readableBytes());
               } else {
                  obj.typeName = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
                  varPosBase0 = buf.getIntLE(offset + 4);
                  if (varPosBase0 >= 0 && varPosBase0 <= buf.writerIndex() - offset - 8) {
                     varPos0 = offset + 8 + varPosBase0;
                     typeNameLen = VarInt.peek(buf, varPos0);
                     if (typeNameLen < 0) {
                        throw ProtocolException.invalidVarInt("Properties");
                     } else {
                        typeNameVarIntLen = VarInt.size(typeNameLen);
                        if (typeNameLen > 4096000) {
                           throw ProtocolException.dictionaryTooLarge("Properties", typeNameLen, 4096000);
                        } else {
                           obj.properties = new HashMap<>(typeNameLen);
                           int dictPos = varPos0 + typeNameVarIntLen;

                           for (int i = 0; i < typeNameLen; i++) {
                              int keyLen = VarInt.peek(buf, dictPos);
                              if (keyLen < 0) {
                                 throw ProtocolException.invalidVarInt("key");
                              }

                              int keyVarLen = VarInt.size(keyLen);
                              if (keyLen > 4096000) {
                                 throw ProtocolException.stringTooLong("key", keyLen, 4096000);
                              }

                              if (dictPos + keyVarLen + keyLen > buf.readableBytes()) {
                                 throw ProtocolException.bufferTooSmall("key", dictPos + keyVarLen + keyLen, buf.readableBytes());
                              }

                              String key = PacketIO.readVarString(buf, dictPos);
                              dictPos += keyVarLen + keyLen;
                              UIDataValue val = UIDataValue.deserialize(buf, dictPos);
                              dictPos += UIDataValue.computeBytesConsumed(buf, dictPos);
                              if (obj.properties.put(key, val) != null) {
                                 throw ProtocolException.duplicateKey("properties", key);
                              }
                           }

                           return obj;
                        }
                     }
                  } else {
                     throw ProtocolException.invalidOffset("Properties", varPosBase0, buf.readableBytes());
                  }
               }
            }
         } else {
            throw ProtocolException.invalidOffset("TypeName", varPosBase0, buf.readableBytes());
         }
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int maxEnd = 8;
      int fieldOffset0 = buf.getIntLE(offset + 0);
      if (fieldOffset0 >= 0 && fieldOffset0 <= buf.writerIndex() - offset - 8) {
         int pos0 = offset + 8 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }

         fieldOffset0 = buf.getIntLE(offset + 4);
         if (fieldOffset0 >= 0 && fieldOffset0 <= buf.writerIndex() - offset - 8) {
            pos0 = offset + 8 + fieldOffset0;
            sl = VarInt.peek(buf, pos0);
            pos0 += VarInt.size(sl);

            for (int i = 0; i < sl; i++) {
               int slx = VarInt.peek(buf, pos0);
               pos0 += VarInt.size(slx) + slx;
               pos0 += UIDataValue.computeBytesConsumed(buf, pos0);
            }

            if (pos0 - offset > maxEnd) {
               maxEnd = pos0 - offset;
            }

            return maxEnd;
         } else {
            throw ProtocolException.invalidOffset("Properties", fieldOffset0, maxEnd);
         }
      } else {
         throw ProtocolException.invalidOffset("TypeName", fieldOffset0, maxEnd);
      }
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 8L;
   }

   public static String getTypeName(MemorySegment mem) {
      return getTypeName(mem, 0);
   }

   public static String getTypeName(MemorySegment mem, int offset) {
      return PacketIO.readVarString("TypeName", mem, offset + getValidatedOffset(mem, offset, 0, 8, "TypeName"), 4096000, PacketIO.UTF8);
   }

   public static Map<String, UIDataValue> getProperties(MemorySegment mem) {
      return getProperties(mem, 0);
   }

   public static Map<String, UIDataValue> getProperties(MemorySegment mem, int offset) {
      int off = offset + getValidatedOffset(mem, offset, 4, 8, "Properties");
      long packed = VarInt.getWithLength(mem, off);
      int len = (int)packed;
      if (len < 0) {
         throw ProtocolException.negativeLength("Properties", len);
      } else if (len > 4096000) {
         throw ProtocolException.dictionaryTooLarge("Properties", len, 4096000);
      } else {
         Map<String, UIDataValue> data = new HashMap<>(len);
         off += (int)(packed >>> 32);

         for (int i = 0; i < len; i++) {
            long keyPacked = VarInt.getWithLength(mem, off);
            int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
            String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
            off += nkey;
            UIDataValue value = UIDataValue.toObject(mem, off);
            off += value.computeSizeWithTypeId();
            if (data.put(key, value) != null) {
               throw ProtocolException.duplicateKey("Properties", key);
            }
         }

         return data;
      }
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static UIObjectDataValue toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UIObjectDataValue toObject(MemorySegment mem, int offset) {
      if (offset + 8 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UIObjectDataValue", offset + 8, (int)mem.byteSize());
      } else {
         int off = offset + getValidatedOffset(mem, offset, 4, 8, "Properties");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Properties", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Properties", len, 4096000);
         } else {
            Map<String, UIDataValue> properties = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               UIDataValue value = UIDataValue.toObject(mem, off);
               off += value.computeSizeWithTypeId();
               if (properties.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Properties", key);
               }
            }

            return new UIObjectDataValue(
               PacketIO.readVarString("TypeName", mem, offset + getValidatedOffset(mem, offset, 0, 8, "TypeName"), 4096000, PacketIO.UTF8), properties
            );
         }
      }
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      int typeNameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int propertiesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      buf.setIntLE(typeNameOffsetSlot, buf.writerIndex() - varBlockStart);
      PacketIO.writeVarString(buf, this.typeName, 4096000);
      buf.setIntLE(propertiesOffsetSlot, buf.writerIndex() - varBlockStart);
      if (this.properties.size() > 4096000) {
         throw ProtocolException.dictionaryTooLarge("Properties", this.properties.size(), 4096000);
      } else {
         VarInt.write(buf, this.properties.size());

         for (Entry<String, UIDataValue> e : this.properties.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            e.getValue().serializeWithTypeId(buf);
         }

         return buf.writerIndex() - startPos;
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      int varOffset = offset + 8;
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), varOffset - offset - 8);
      varOffset += PacketIO.writeVarString(mem, varOffset, this.typeName, 4096000);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 4), varOffset - offset - 8);
      if (this.properties.size() > 4096000) {
         throw ProtocolException.dictionaryTooLarge("Properties", this.properties.size(), 4096000);
      } else {
         varOffset += VarInt.set(mem, varOffset, this.properties.size());

         for (Entry<String, UIDataValue> e : this.properties.entrySet()) {
            varOffset += PacketIO.writeVarString(mem, varOffset, e.getKey(), 16384000);
            varOffset += e.getValue().serializeWithTypeId(mem, varOffset);
         }

         return varOffset - offset;
      }
   }

   @Override
   public int computeSize() {
      int size = 8;
      size += PacketIO.stringSize(this.typeName);
      int propertiesSize = 0;

      for (Entry<String, UIDataValue> kvp : this.properties.entrySet()) {
         propertiesSize += PacketIO.stringSize(kvp.getKey()) + kvp.getValue().computeSizeWithTypeId();
      }

      return size + VarInt.size(this.properties.size()) + propertiesSize;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 8) {
         return ValidationResult.error("Buffer too small: expected at least 8 bytes");
      } else {
         int typeNameOffset = buffer.getIntLE(offset + 0);
         if (typeNameOffset >= 0 && typeNameOffset <= buffer.writerIndex() - offset - 8) {
            int pos = offset + 8 + typeNameOffset;
            int typeNameLen = VarInt.peek(buffer, pos);
            if (typeNameLen < 0) {
               return ValidationResult.error("Invalid string length for TypeName");
            } else if (typeNameLen > 4096000) {
               return ValidationResult.error("TypeName exceeds max length 4096000");
            } else {
               pos += VarInt.size(typeNameLen);
               pos += typeNameLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading TypeName");
               } else {
                  typeNameOffset = buffer.getIntLE(offset + 4);
                  if (typeNameOffset >= 0 && typeNameOffset <= buffer.writerIndex() - offset - 8) {
                     pos = offset + 8 + typeNameOffset;
                     typeNameLen = VarInt.peek(buffer, pos);
                     if (typeNameLen < 0) {
                        return ValidationResult.error("Invalid dictionary count for Properties");
                     } else if (typeNameLen > 4096000) {
                        return ValidationResult.error("Properties exceeds max length 4096000");
                     } else {
                        pos += VarInt.size(typeNameLen);

                        for (int i = 0; i < typeNameLen; i++) {
                           int keyLen = VarInt.peek(buffer, pos);
                           if (keyLen < 0) {
                              return ValidationResult.error("Invalid string length for key");
                           }

                           if (keyLen > 4096000) {
                              return ValidationResult.error("key exceeds max length 4096000");
                           }

                           pos += VarInt.size(keyLen);
                           pos += keyLen;
                           if (pos > buffer.writerIndex()) {
                              return ValidationResult.error("Buffer overflow reading key");
                           }

                           pos += UIDataValue.computeBytesConsumed(buffer, pos);
                        }

                        return ValidationResult.OK;
                     }
                  } else {
                     return ValidationResult.error("Invalid offset for Properties");
                  }
               }
            }
         } else {
            return ValidationResult.error("Invalid offset for TypeName");
         }
      }
   }

   public UIObjectDataValue clone() {
      UIObjectDataValue copy = new UIObjectDataValue();
      copy.typeName = this.typeName;
      copy.properties = new HashMap<>(this.properties);
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UIObjectDataValue other)
            ? false
            : Objects.equals(this.typeName, other.typeName) && Objects.equals(this.properties, other.properties);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.typeName, this.properties);
   }
}
