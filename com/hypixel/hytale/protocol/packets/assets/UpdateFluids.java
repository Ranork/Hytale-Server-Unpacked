package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.Fluid;
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

public class UpdateFluids implements Packet, ToClientPacket {
   public static final int PACKET_ID = 83;
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
   public Map<Integer, Fluid> fluids;

   @Override
   public int getId() {
      return 83;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateFluids() {
   }

   public UpdateFluids(@Nonnull UpdateType type, int maxId, @Nullable Map<Integer, Fluid> fluids) {
      this.type = type;
      this.maxId = maxId;
      this.fluids = fluids;
   }

   public UpdateFluids(@Nonnull UpdateFluids other) {
      this.type = other.type;
      this.maxId = other.maxId;
      this.fluids = other.fluids;
   }

   @Nonnull
   public static UpdateFluids deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 6) {
         throw ProtocolException.bufferTooSmall("UpdateFluids", 6, buf.readableBytes() - offset);
      } else {
         UpdateFluids obj = new UpdateFluids();
         byte nullBits = buf.getByte(offset);
         obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
         obj.maxId = buf.getIntLE(offset + 2);
         int pos = offset + 6;
         if ((nullBits & 1) != 0) {
            int fluidsCount = VarInt.peek(buf, pos);
            if (fluidsCount < 0) {
               throw ProtocolException.invalidVarInt("Fluids");
            }

            int fluidsVarLen = VarInt.size(fluidsCount);
            if (fluidsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Fluids", fluidsCount, 4096000);
            }

            pos += fluidsVarLen;
            obj.fluids = new HashMap<>(fluidsCount);

            for (int i = 0; i < fluidsCount; i++) {
               int key = buf.getIntLE(pos);
               pos += 4;
               Fluid val = Fluid.deserialize(buf, pos);
               pos += Fluid.computeBytesConsumed(buf, pos);
               if (obj.fluids.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("fluids", key);
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
            pos += Fluid.computeBytesConsumed(buf, pos);
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
   public static Map<Integer, Fluid> getFluids(MemorySegment mem) {
      return getFluids(mem, 0);
   }

   @Nullable
   public static Map<Integer, Fluid> getFluids(MemorySegment mem, int offset) {
      if (!hasFluids(mem, offset)) {
         return null;
      } else {
         int off = offset + 6;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Fluids", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Fluids", len, 4096000);
         } else {
            Map<Integer, Fluid> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               Fluid value = Fluid.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Fluids", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean hasFluids(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static UpdateFluids toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateFluids toObject(MemorySegment mem, int offset) {
      if (offset + 6 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateFluids", offset + 6, (int)mem.byteSize());
      } else {
         Map<Integer, Fluid> fluids = null;
         if (hasFluids(mem, offset)) {
            int off = offset + 6;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Fluids", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Fluids", len, 4096000);
            }

            fluids = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               Fluid value = Fluid.toObject(mem, off);
               off += value.computeSize();
               if (fluids.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Fluids", key);
               }
            }
         }

         return new UpdateFluids(
            UpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))), mem.get(PacketIO.PROTO_INT, (long)(offset + 2)), fluids
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.fluids != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.maxId);
      if (this.fluids != null) {
         if (this.fluids.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Fluids", this.fluids.size(), 4096000);
         }

         VarInt.write(buf, this.fluids.size());

         for (Entry<Integer, Fluid> e : this.fluids.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.fluids != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      mem.set(PacketIO.PROTO_INT, (long)(offset + 2), this.maxId);
      int varOffset = offset + 6;
      if (this.fluids != null) {
         if (this.fluids.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Fluids", this.fluids.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.fluids.size());

         for (Entry<Integer, Fluid> e : this.fluids.entrySet()) {
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
      if (this.fluids != null) {
         int fluidsSize = 0;

         for (Entry<Integer, Fluid> kvp : this.fluids.entrySet()) {
            fluidsSize += 4 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.fluids.size()) + fluidsSize;
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
               int fluidsCount = VarInt.peek(buffer, v);
               if (fluidsCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for Fluids");
               }

               if (fluidsCount > 4096000) {
                  return ValidationResult.error("Fluids exceeds max length 4096000");
               }

               v += VarInt.size(fluidsCount);

               for (int i = 0; i < fluidsCount; i++) {
                  v += 4;
                  if (v > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading key");
                  }

                  v += Fluid.computeBytesConsumed(buffer, v);
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public UpdateFluids clone() {
      UpdateFluids copy = new UpdateFluids();
      copy.type = this.type;
      copy.maxId = this.maxId;
      if (this.fluids != null) {
         Map<Integer, Fluid> m = new HashMap<>();

         for (Entry<Integer, Fluid> e : this.fluids.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.fluids = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateFluids other)
            ? false
            : Objects.equals(this.type, other.type) && this.maxId == other.maxId && Objects.equals(this.fluids, other.fluids);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.maxId, this.fluids);
   }
}
