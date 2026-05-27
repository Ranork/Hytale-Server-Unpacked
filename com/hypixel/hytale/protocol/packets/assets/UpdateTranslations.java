package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
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

public class UpdateTranslations implements Packet, ToClientPacket {
   public static final int PACKET_ID = 64;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 2;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public UpdateType type = UpdateType.Init;
   @Nullable
   public Map<String, String> translations;

   @Override
   public int getId() {
      return 64;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateTranslations() {
   }

   public UpdateTranslations(@Nonnull UpdateType type, @Nullable Map<String, String> translations) {
      this.type = type;
      this.translations = translations;
   }

   public UpdateTranslations(@Nonnull UpdateTranslations other) {
      this.type = other.type;
      this.translations = other.translations;
   }

   @Nonnull
   public static UpdateTranslations deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 2) {
         throw ProtocolException.bufferTooSmall("UpdateTranslations", 2, buf.readableBytes() - offset);
      } else {
         UpdateTranslations obj = new UpdateTranslations();
         byte nullBits = buf.getByte(offset);
         obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
         int pos = offset + 2;
         if ((nullBits & 1) != 0) {
            int translationsCount = VarInt.peek(buf, pos);
            if (translationsCount < 0) {
               throw ProtocolException.invalidVarInt("Translations");
            }

            int translationsVarLen = VarInt.size(translationsCount);
            if (translationsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Translations", translationsCount, 4096000);
            }

            pos += translationsVarLen;
            obj.translations = new HashMap<>(translationsCount);

            for (int i = 0; i < translationsCount; i++) {
               int keyLen = VarInt.peek(buf, pos);
               if (keyLen < 0) {
                  throw ProtocolException.invalidVarInt("key");
               }

               int keyVarLen = VarInt.size(keyLen);
               if (keyLen > 4096000) {
                  throw ProtocolException.stringTooLong("key", keyLen, 4096000);
               }

               if (pos + keyVarLen + keyLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("key", pos + keyVarLen + keyLen, buf.readableBytes());
               }

               String key = PacketIO.readVarString(buf, pos);
               pos += keyVarLen + keyLen;
               int valLen = VarInt.peek(buf, pos);
               if (valLen < 0) {
                  throw ProtocolException.invalidVarInt("val");
               }

               int valVarLen = VarInt.size(valLen);
               if (valLen > 4096000) {
                  throw ProtocolException.stringTooLong("val", valLen, 4096000);
               }

               if (pos + valVarLen + valLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("val", pos + valVarLen + valLen, buf.readableBytes());
               }

               String val = PacketIO.readVarString(buf, pos);
               pos += valVarLen + valLen;
               if (obj.translations.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("translations", key);
               }
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         int dictLen = VarInt.peek(buf, pos);
         pos += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos);
            pos += VarInt.size(sl) + sl;
            sl = VarInt.peek(buf, pos);
            pos += VarInt.size(sl) + sl;
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 2L;
   }

   public static UpdateType getType(MemorySegment mem) {
      return getType(mem, 0);
   }

   public static UpdateType getType(MemorySegment mem, int offset) {
      return UpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   @Nullable
   public static Map<String, String> getTranslations(MemorySegment mem) {
      return getTranslations(mem, 0);
   }

   @Nullable
   public static Map<String, String> getTranslations(MemorySegment mem, int offset) {
      if (!hasTranslations(mem, offset)) {
         return null;
      } else {
         int off = offset + 2;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Translations", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Translations", len, 4096000);
         } else {
            Map<String, String> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               long valuePacked = VarInt.getWithLength(mem, off);
               int nvalue = (int)valuePacked + (int)(valuePacked >>> 32);
               String value = PacketIO.readVarString("value", mem, off, 16384000, PacketIO.UTF8);
               off += nvalue;
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Translations", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean hasTranslations(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static UpdateTranslations toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateTranslations toObject(MemorySegment mem, int offset) {
      if (offset + 2 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateTranslations", offset + 2, (int)mem.byteSize());
      } else {
         Map<String, String> translations = null;
         if (hasTranslations(mem, offset)) {
            int off = offset + 2;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Translations", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Translations", len, 4096000);
            }

            translations = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               long valuePacked = VarInt.getWithLength(mem, off);
               int nvalue = (int)valuePacked + (int)(valuePacked >>> 32);
               String value = PacketIO.readVarString("value", mem, off, 16384000, PacketIO.UTF8);
               off += nvalue;
               if (translations.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Translations", key);
               }
            }
         }

         return new UpdateTranslations(UpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))), translations);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.translations != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      if (this.translations != null) {
         if (this.translations.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Translations", this.translations.size(), 4096000);
         }

         VarInt.write(buf, this.translations.size());

         for (Entry<String, String> e : this.translations.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            PacketIO.writeVarString(buf, e.getValue(), 4096000);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.translations != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      int varOffset = offset + 2;
      if (this.translations != null) {
         if (this.translations.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Translations", this.translations.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.translations.size());

         for (Entry<String, String> e : this.translations.entrySet()) {
            varOffset += PacketIO.writeVarString(mem, varOffset, e.getKey(), 16384000);
            varOffset += PacketIO.writeVarString(mem, varOffset, e.getValue(), 16384000);
         }
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 2;
      if (this.translations != null) {
         int translationsSize = 0;

         for (Entry<String, String> kvp : this.translations.entrySet()) {
            translationsSize += PacketIO.stringSize(kvp.getKey()) + PacketIO.stringSize(kvp.getValue());
         }

         size += VarInt.size(this.translations.size()) + translationsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 2) {
         return ValidationResult.error("Buffer too small: expected at least 2 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 3) {
            return ValidationResult.error("Invalid UpdateType value for Type");
         } else {
            v = offset + 2;
            if ((nullBits & 1) != 0) {
               int translationsCount = VarInt.peek(buffer, v);
               if (translationsCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for Translations");
               }

               if (translationsCount > 4096000) {
                  return ValidationResult.error("Translations exceeds max length 4096000");
               }

               v += VarInt.size(translationsCount);

               for (int i = 0; i < translationsCount; i++) {
                  int keyLen = VarInt.peek(buffer, v);
                  if (keyLen < 0) {
                     return ValidationResult.error("Invalid string length for key");
                  }

                  if (keyLen > 4096000) {
                     return ValidationResult.error("key exceeds max length 4096000");
                  }

                  v += VarInt.size(keyLen);
                  v += keyLen;
                  if (v > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading key");
                  }

                  int valueLen = VarInt.peek(buffer, v);
                  if (valueLen < 0) {
                     return ValidationResult.error("Invalid string length for value");
                  }

                  if (valueLen > 4096000) {
                     return ValidationResult.error("value exceeds max length 4096000");
                  }

                  v += VarInt.size(valueLen);
                  v += valueLen;
                  if (v > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading value");
                  }
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public UpdateTranslations clone() {
      UpdateTranslations copy = new UpdateTranslations();
      copy.type = this.type;
      copy.translations = this.translations != null ? new HashMap<>(this.translations) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateTranslations other)
            ? false
            : Objects.equals(this.type, other.type) && Objects.equals(this.translations, other.translations);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.translations);
   }
}
