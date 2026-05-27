package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
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

public class AssetEditorFetchJsonAssetWithParentsReply implements Packet, ToClientPacket {
   public static final int PACKET_ID = 313;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 1677721600;
   public int token;
   @Nullable
   public Map<AssetPath, String> assets;

   @Override
   public int getId() {
      return 313;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorFetchJsonAssetWithParentsReply() {
   }

   public AssetEditorFetchJsonAssetWithParentsReply(int token, @Nullable Map<AssetPath, String> assets) {
      this.token = token;
      this.assets = assets;
   }

   public AssetEditorFetchJsonAssetWithParentsReply(@Nonnull AssetEditorFetchJsonAssetWithParentsReply other) {
      this.token = other.token;
      this.assets = other.assets;
   }

   @Nonnull
   public static AssetEditorFetchJsonAssetWithParentsReply deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 5) {
         throw ProtocolException.bufferTooSmall("AssetEditorFetchJsonAssetWithParentsReply", 5, buf.readableBytes() - offset);
      } else {
         AssetEditorFetchJsonAssetWithParentsReply obj = new AssetEditorFetchJsonAssetWithParentsReply();
         byte nullBits = buf.getByte(offset);
         obj.token = buf.getIntLE(offset + 1);
         int pos = offset + 5;
         if ((nullBits & 1) != 0) {
            int assetsCount = VarInt.peek(buf, pos);
            if (assetsCount < 0) {
               throw ProtocolException.invalidVarInt("Assets");
            }

            int assetsVarLen = VarInt.size(assetsCount);
            if (assetsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Assets", assetsCount, 4096000);
            }

            pos += assetsVarLen;
            obj.assets = new HashMap<>(assetsCount);

            for (int i = 0; i < assetsCount; i++) {
               AssetPath key = AssetPath.deserialize(buf, pos);
               pos += AssetPath.computeBytesConsumed(buf, pos);
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
               if (obj.assets.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("assets", key);
               }
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         int dictLen = VarInt.peek(buf, pos);
         pos += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos += AssetPath.computeBytesConsumed(buf, pos);
            int sl = VarInt.peek(buf, pos);
            pos += VarInt.size(sl) + sl;
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 5L;
   }

   public static int getToken(MemorySegment mem) {
      return getToken(mem, 0);
   }

   public static int getToken(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   @Nullable
   public static Map<AssetPath, String> getAssets(MemorySegment mem) {
      return getAssets(mem, 0);
   }

   @Nullable
   public static Map<AssetPath, String> getAssets(MemorySegment mem, int offset) {
      if (!hasAssets(mem, offset)) {
         return null;
      } else {
         int off = offset + 5;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Assets", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Assets", len, 4096000);
         } else {
            Map<AssetPath, String> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               AssetPath key = AssetPath.toObject(mem, off);
               off += key.computeSize();
               long valuePacked = VarInt.getWithLength(mem, off);
               int nvalue = (int)valuePacked + (int)(valuePacked >>> 32);
               String value = PacketIO.readVarString("value", mem, off, 16384000, PacketIO.UTF8);
               off += nvalue;
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Assets", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean hasAssets(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static AssetEditorFetchJsonAssetWithParentsReply toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetEditorFetchJsonAssetWithParentsReply toObject(MemorySegment mem, int offset) {
      if (offset + 5 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetEditorFetchJsonAssetWithParentsReply", offset + 5, (int)mem.byteSize());
      } else {
         Map<AssetPath, String> assets = null;
         if (hasAssets(mem, offset)) {
            int off = offset + 5;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Assets", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Assets", len, 4096000);
            }

            assets = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               AssetPath key = AssetPath.toObject(mem, off);
               off += key.computeSize();
               long valuePacked = VarInt.getWithLength(mem, off);
               int nvalue = (int)valuePacked + (int)(valuePacked >>> 32);
               String value = PacketIO.readVarString("value", mem, off, 16384000, PacketIO.UTF8);
               off += nvalue;
               if (assets.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Assets", key);
               }
            }
         }

         return new AssetEditorFetchJsonAssetWithParentsReply(mem.get(PacketIO.PROTO_INT, (long)(offset + 1)), assets);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.assets != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.token);
      if (this.assets != null) {
         if (this.assets.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Assets", this.assets.size(), 4096000);
         }

         VarInt.write(buf, this.assets.size());

         for (Entry<AssetPath, String> e : this.assets.entrySet()) {
            e.getKey().serialize(buf);
            PacketIO.writeVarString(buf, e.getValue(), 4096000);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.assets != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.token);
      int varOffset = offset + 5;
      if (this.assets != null) {
         if (this.assets.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Assets", this.assets.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.assets.size());

         for (Entry<AssetPath, String> e : this.assets.entrySet()) {
            varOffset += e.getKey().serialize(mem, varOffset);
            varOffset += PacketIO.writeVarString(mem, varOffset, e.getValue(), 16384000);
         }
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 5;
      if (this.assets != null) {
         int assetsSize = 0;

         for (Entry<AssetPath, String> kvp : this.assets.entrySet()) {
            assetsSize += kvp.getKey().computeSize() + PacketIO.stringSize(kvp.getValue());
         }

         size += VarInt.size(this.assets.size()) + assetsSize;
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
            int assetsCount = VarInt.peek(buffer, pos);
            if (assetsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Assets");
            }

            if (assetsCount > 4096000) {
               return ValidationResult.error("Assets exceeds max length 4096000");
            }

            pos += VarInt.size(assetsCount);

            for (int i = 0; i < assetsCount; i++) {
               pos += AssetPath.computeBytesConsumed(buffer, pos);
               int valueLen = VarInt.peek(buffer, pos);
               if (valueLen < 0) {
                  return ValidationResult.error("Invalid string length for value");
               }

               if (valueLen > 4096000) {
                  return ValidationResult.error("value exceeds max length 4096000");
               }

               pos += VarInt.size(valueLen);
               pos += valueLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading value");
               }
            }
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorFetchJsonAssetWithParentsReply clone() {
      AssetEditorFetchJsonAssetWithParentsReply copy = new AssetEditorFetchJsonAssetWithParentsReply();
      copy.token = this.token;
      copy.assets = this.assets != null ? new HashMap<>(this.assets) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetEditorFetchJsonAssetWithParentsReply other)
            ? false
            : this.token == other.token && Objects.equals(this.assets, other.assets);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.token, this.assets);
   }
}
