package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.AmbienceFX;
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

public class UpdateAmbienceFX implements Packet, ToClientPacket {
   public static final int PACKET_ID = 62;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 6;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 6;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public UpdateType type = UpdateType.Init;
   public int maxId;
   @Nullable
   public Map<Integer, AmbienceFX> ambienceFX;

   @Override
   public int getId() {
      return 62;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateAmbienceFX() {
   }

   public UpdateAmbienceFX(@Nonnull UpdateType type, int maxId, @Nullable Map<Integer, AmbienceFX> ambienceFX) {
      this.type = type;
      this.maxId = maxId;
      this.ambienceFX = ambienceFX;
   }

   public UpdateAmbienceFX(@Nonnull UpdateAmbienceFX other) {
      this.type = other.type;
      this.maxId = other.maxId;
      this.ambienceFX = other.ambienceFX;
   }

   @Nonnull
   public static UpdateAmbienceFX deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 6) {
         throw ProtocolException.bufferTooSmall("UpdateAmbienceFX", 6, buf.readableBytes() - offset);
      } else {
         UpdateAmbienceFX obj = new UpdateAmbienceFX();
         byte nullBits = buf.getByte(offset);
         obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
         obj.maxId = buf.getIntLE(offset + 2);
         int pos = offset + 6;
         if ((nullBits & 1) != 0) {
            int ambienceFXCount = VarInt.peek(buf, pos);
            if (ambienceFXCount < 0) {
               throw ProtocolException.invalidVarInt("AmbienceFX");
            }

            int ambienceFXVarLen = VarInt.size(ambienceFXCount);
            if (ambienceFXCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("AmbienceFX", ambienceFXCount, 4096000);
            }

            pos += ambienceFXVarLen;
            obj.ambienceFX = new HashMap<>(ambienceFXCount);

            for (int i = 0; i < ambienceFXCount; i++) {
               int key = buf.getIntLE(pos);
               pos += 4;
               AmbienceFX val = AmbienceFX.deserialize(buf, pos);
               pos += AmbienceFX.computeBytesConsumed(buf, pos);
               if (obj.ambienceFX.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("ambienceFX", key);
               }
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 6;
      if ((nullBits & 1) != 0) {
         int dictLen = VarInt.peek(buf, pos);
         pos += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos += 4;
            pos += AmbienceFX.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 6L;
   }

   public static UpdateType getType(MemorySegment mem) {
      return getType(mem, 0);
   }

   public static UpdateType getType(MemorySegment mem, int offset) {
      return UpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   public static int getMaxId(MemorySegment mem) {
      return getMaxId(mem, 0);
   }

   public static int getMaxId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 2));
   }

   @Nullable
   public static Map<Integer, AmbienceFX> getAmbienceFX(MemorySegment mem) {
      return getAmbienceFX(mem, 0);
   }

   @Nullable
   public static Map<Integer, AmbienceFX> getAmbienceFX(MemorySegment mem, int offset) {
      if (!hasAmbienceFX(mem, offset)) {
         return null;
      } else {
         int off = offset + 6;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("AmbienceFX", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("AmbienceFX", len, 4096000);
         } else {
            Map<Integer, AmbienceFX> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               AmbienceFX value = AmbienceFX.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("AmbienceFX", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean hasAmbienceFX(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static UpdateAmbienceFX toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateAmbienceFX toObject(MemorySegment mem, int offset) {
      if (offset + 6 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateAmbienceFX", offset + 6, (int)mem.byteSize());
      } else {
         Map<Integer, AmbienceFX> ambienceFX = null;
         if (hasAmbienceFX(mem, offset)) {
            int off = offset + 6;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("AmbienceFX", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("AmbienceFX", len, 4096000);
            }

            ambienceFX = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               AmbienceFX value = AmbienceFX.toObject(mem, off);
               off += value.computeSize();
               if (ambienceFX.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("AmbienceFX", key);
               }
            }
         }

         return new UpdateAmbienceFX(
            UpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))), mem.get(PacketIO.PROTO_INT, (long)(offset + 2)), ambienceFX
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.ambienceFX != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.maxId);
      if (this.ambienceFX != null) {
         if (this.ambienceFX.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("AmbienceFX", this.ambienceFX.size(), 4096000);
         }

         VarInt.write(buf, this.ambienceFX.size());

         for (Entry<Integer, AmbienceFX> e : this.ambienceFX.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.ambienceFX != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      mem.set(PacketIO.PROTO_INT, (long)(offset + 2), this.maxId);
      int varOffset = offset + 6;
      if (this.ambienceFX != null) {
         if (this.ambienceFX.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("AmbienceFX", this.ambienceFX.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.ambienceFX.size());

         for (Entry<Integer, AmbienceFX> e : this.ambienceFX.entrySet()) {
            mem.set(PacketIO.PROTO_INT, (long)varOffset, e.getKey());
            varOffset += 4;
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 6;
      if (this.ambienceFX != null) {
         int ambienceFXSize = 0;

         for (Entry<Integer, AmbienceFX> kvp : this.ambienceFX.entrySet()) {
            ambienceFXSize += 4 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.ambienceFX.size()) + ambienceFXSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 6) {
         return ValidationResult.error("Buffer too small: expected at least 6 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 3) {
            return ValidationResult.error("Invalid UpdateType value for Type");
         } else {
            v = offset + 6;
            if ((nullBits & 1) != 0) {
               int ambienceFXCount = VarInt.peek(buffer, v);
               if (ambienceFXCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for AmbienceFX");
               }

               if (ambienceFXCount > 4096000) {
                  return ValidationResult.error("AmbienceFX exceeds max length 4096000");
               }

               v += VarInt.size(ambienceFXCount);

               for (int i = 0; i < ambienceFXCount; i++) {
                  v += 4;
                  if (v > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading key");
                  }

                  v += AmbienceFX.computeBytesConsumed(buffer, v);
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public UpdateAmbienceFX clone() {
      UpdateAmbienceFX copy = new UpdateAmbienceFX();
      copy.type = this.type;
      copy.maxId = this.maxId;
      if (this.ambienceFX != null) {
         Map<Integer, AmbienceFX> m = new HashMap<>();

         for (Entry<Integer, AmbienceFX> e : this.ambienceFX.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.ambienceFX = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateAmbienceFX other)
            ? false
            : Objects.equals(this.type, other.type) && this.maxId == other.maxId && Objects.equals(this.ambienceFX, other.ambienceFX);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.maxId, this.ambienceFX);
   }
}
