package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.Trail;
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

public class UpdateTrails implements Packet, ToClientPacket {
   public static final int PACKET_ID = 48;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 2;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public UpdateType type = UpdateType.Init;
   @Nullable
   public Map<String, Trail> trails;

   @Override
   public int getId() {
      return 48;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateTrails() {
   }

   public UpdateTrails(@Nonnull UpdateType type, @Nullable Map<String, Trail> trails) {
      this.type = type;
      this.trails = trails;
   }

   public UpdateTrails(@Nonnull UpdateTrails other) {
      this.type = other.type;
      this.trails = other.trails;
   }

   @Nonnull
   public static UpdateTrails deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 2) {
         throw ProtocolException.bufferTooSmall("UpdateTrails", 2, buf.readableBytes() - offset);
      } else {
         UpdateTrails obj = new UpdateTrails();
         byte nullBits = buf.getByte(offset);
         obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
         int pos = offset + 2;
         if ((nullBits & 1) != 0) {
            int trailsCount = VarInt.peek(buf, pos);
            if (trailsCount < 0) {
               throw ProtocolException.invalidVarInt("Trails");
            }

            int trailsVarLen = VarInt.size(trailsCount);
            if (trailsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Trails", trailsCount, 4096000);
            }

            pos += trailsVarLen;
            obj.trails = new HashMap<>(trailsCount);

            for (int i = 0; i < trailsCount; i++) {
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
               Trail val = Trail.deserialize(buf, pos);
               pos += Trail.computeBytesConsumed(buf, pos);
               if (obj.trails.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("trails", key);
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
            pos += Trail.computeBytesConsumed(buf, pos);
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
   public static Map<String, Trail> getTrails(MemorySegment mem) {
      return getTrails(mem, 0);
   }

   @Nullable
   public static Map<String, Trail> getTrails(MemorySegment mem, int offset) {
      if (!hasTrails(mem, offset)) {
         return null;
      } else {
         int off = offset + 2;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Trails", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Trails", len, 4096000);
         } else {
            Map<String, Trail> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               Trail value = Trail.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Trails", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean hasTrails(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static UpdateTrails toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateTrails toObject(MemorySegment mem, int offset) {
      if (offset + 2 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateTrails", offset + 2, (int)mem.byteSize());
      } else {
         Map<String, Trail> trails = null;
         if (hasTrails(mem, offset)) {
            int off = offset + 2;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Trails", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Trails", len, 4096000);
            }

            trails = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               Trail value = Trail.toObject(mem, off);
               off += value.computeSize();
               if (trails.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Trails", key);
               }
            }
         }

         return new UpdateTrails(UpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))), trails);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.trails != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      if (this.trails != null) {
         if (this.trails.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Trails", this.trails.size(), 4096000);
         }

         VarInt.write(buf, this.trails.size());

         for (Entry<String, Trail> e : this.trails.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.trails != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      int varOffset = offset + 2;
      if (this.trails != null) {
         if (this.trails.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Trails", this.trails.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.trails.size());

         for (Entry<String, Trail> e : this.trails.entrySet()) {
            varOffset += PacketIO.writeVarString(mem, varOffset, e.getKey(), 16384000);
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 2;
      if (this.trails != null) {
         int trailsSize = 0;

         for (Entry<String, Trail> kvp : this.trails.entrySet()) {
            trailsSize += PacketIO.stringSize(kvp.getKey()) + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.trails.size()) + trailsSize;
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
               int trailsCount = VarInt.peek(buffer, v);
               if (trailsCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for Trails");
               }

               if (trailsCount > 4096000) {
                  return ValidationResult.error("Trails exceeds max length 4096000");
               }

               v += VarInt.size(trailsCount);

               for (int i = 0; i < trailsCount; i++) {
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

                  v += Trail.computeBytesConsumed(buffer, v);
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public UpdateTrails clone() {
      UpdateTrails copy = new UpdateTrails();
      copy.type = this.type;
      if (this.trails != null) {
         Map<String, Trail> m = new HashMap<>();

         for (Entry<String, Trail> e : this.trails.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.trails = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateTrails other) ? false : Objects.equals(this.type, other.type) && Objects.equals(this.trails, other.trails);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.trails);
   }
}
