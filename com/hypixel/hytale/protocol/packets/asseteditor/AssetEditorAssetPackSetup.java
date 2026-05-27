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

public class AssetEditorAssetPackSetup implements Packet, ToClientPacket {
   public static final int PACKET_ID = 314;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public Map<String, AssetPackManifest> packs;

   @Override
   public int getId() {
      return 314;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorAssetPackSetup() {
   }

   public AssetEditorAssetPackSetup(@Nullable Map<String, AssetPackManifest> packs) {
      this.packs = packs;
   }

   public AssetEditorAssetPackSetup(@Nonnull AssetEditorAssetPackSetup other) {
      this.packs = other.packs;
   }

   @Nonnull
   public static AssetEditorAssetPackSetup deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("AssetEditorAssetPackSetup", 1, buf.readableBytes() - offset);
      } else {
         AssetEditorAssetPackSetup obj = new AssetEditorAssetPackSetup();
         byte nullBits = buf.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int packsCount = VarInt.peek(buf, pos);
            if (packsCount < 0) {
               throw ProtocolException.invalidVarInt("Packs");
            }

            int packsVarLen = VarInt.size(packsCount);
            if (packsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Packs", packsCount, 4096000);
            }

            pos += packsVarLen;
            obj.packs = new HashMap<>(packsCount);

            for (int i = 0; i < packsCount; i++) {
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
               AssetPackManifest val = AssetPackManifest.deserialize(buf, pos);
               pos += AssetPackManifest.computeBytesConsumed(buf, pos);
               if (obj.packs.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("packs", key);
               }
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int dictLen = VarInt.peek(buf, pos);
         pos += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos);
            pos += VarInt.size(sl) + sl;
            pos += AssetPackManifest.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   @Nullable
   public static Map<String, AssetPackManifest> getPacks(MemorySegment mem) {
      return getPacks(mem, 0);
   }

   @Nullable
   public static Map<String, AssetPackManifest> getPacks(MemorySegment mem, int offset) {
      if (!hasPacks(mem, offset)) {
         return null;
      } else {
         int off = offset + 1;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Packs", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Packs", len, 4096000);
         } else {
            Map<String, AssetPackManifest> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               AssetPackManifest value = AssetPackManifest.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Packs", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean hasPacks(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static AssetEditorAssetPackSetup toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetEditorAssetPackSetup toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetEditorAssetPackSetup", offset + 1, (int)mem.byteSize());
      } else {
         Map<String, AssetPackManifest> packs = null;
         if (hasPacks(mem, offset)) {
            int off = offset + 1;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Packs", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Packs", len, 4096000);
            }

            packs = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               AssetPackManifest value = AssetPackManifest.toObject(mem, off);
               off += value.computeSize();
               if (packs.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Packs", key);
               }
            }
         }

         return new AssetEditorAssetPackSetup(packs);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.packs != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.packs != null) {
         if (this.packs.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Packs", this.packs.size(), 4096000);
         }

         VarInt.write(buf, this.packs.size());

         for (Entry<String, AssetPackManifest> e : this.packs.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.packs != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 1;
      if (this.packs != null) {
         if (this.packs.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Packs", this.packs.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.packs.size());

         for (Entry<String, AssetPackManifest> e : this.packs.entrySet()) {
            varOffset += PacketIO.writeVarString(mem, varOffset, e.getKey(), 16384000);
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.packs != null) {
         int packsSize = 0;

         for (Entry<String, AssetPackManifest> kvp : this.packs.entrySet()) {
            packsSize += PacketIO.stringSize(kvp.getKey()) + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.packs.size()) + packsSize;
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
            int packsCount = VarInt.peek(buffer, pos);
            if (packsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Packs");
            }

            if (packsCount > 4096000) {
               return ValidationResult.error("Packs exceeds max length 4096000");
            }

            pos += VarInt.size(packsCount);

            for (int i = 0; i < packsCount; i++) {
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

               pos += AssetPackManifest.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorAssetPackSetup clone() {
      AssetEditorAssetPackSetup copy = new AssetEditorAssetPackSetup();
      if (this.packs != null) {
         Map<String, AssetPackManifest> m = new HashMap<>();

         for (Entry<String, AssetPackManifest> e : this.packs.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.packs = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof AssetEditorAssetPackSetup other ? Objects.equals(this.packs, other.packs) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.packs);
   }
}
