package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.ModelVFX;
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

public class UpdateModelvfxs implements Packet, ToClientPacket {
   public static final int PACKET_ID = 53;
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
   public Map<Integer, ModelVFX> modelVFXs;

   @Override
   public int getId() {
      return 53;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateModelvfxs() {
   }

   public UpdateModelvfxs(@Nonnull UpdateType type, int maxId, @Nullable Map<Integer, ModelVFX> modelVFXs) {
      this.type = type;
      this.maxId = maxId;
      this.modelVFXs = modelVFXs;
   }

   public UpdateModelvfxs(@Nonnull UpdateModelvfxs other) {
      this.type = other.type;
      this.maxId = other.maxId;
      this.modelVFXs = other.modelVFXs;
   }

   @Nonnull
   public static UpdateModelvfxs deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 6) {
         throw ProtocolException.bufferTooSmall("UpdateModelvfxs", 6, buf.readableBytes() - offset);
      } else {
         UpdateModelvfxs obj = new UpdateModelvfxs();
         byte nullBits = buf.getByte(offset);
         obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
         obj.maxId = buf.getIntLE(offset + 2);
         int pos = offset + 6;
         if ((nullBits & 1) != 0) {
            int modelVFXsCount = VarInt.peek(buf, pos);
            if (modelVFXsCount < 0) {
               throw ProtocolException.invalidVarInt("ModelVFXs");
            }

            int modelVFXsVarLen = VarInt.size(modelVFXsCount);
            if (modelVFXsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("ModelVFXs", modelVFXsCount, 4096000);
            }

            pos += modelVFXsVarLen;
            obj.modelVFXs = new HashMap<>(modelVFXsCount);

            for (int i = 0; i < modelVFXsCount; i++) {
               int key = buf.getIntLE(pos);
               pos += 4;
               ModelVFX val = ModelVFX.deserialize(buf, pos);
               pos += ModelVFX.computeBytesConsumed(buf, pos);
               if (obj.modelVFXs.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("modelVFXs", key);
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
            pos += ModelVFX.computeBytesConsumed(buf, pos);
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
   public static Map<Integer, ModelVFX> getModelVFXs(MemorySegment mem) {
      return getModelVFXs(mem, 0);
   }

   @Nullable
   public static Map<Integer, ModelVFX> getModelVFXs(MemorySegment mem, int offset) {
      if (!hasModelVFXs(mem, offset)) {
         return null;
      } else {
         int off = offset + 6;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("ModelVFXs", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ModelVFXs", len, 4096000);
         } else {
            Map<Integer, ModelVFX> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               ModelVFX value = ModelVFX.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("ModelVFXs", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean hasModelVFXs(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static UpdateModelvfxs toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateModelvfxs toObject(MemorySegment mem, int offset) {
      if (offset + 6 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateModelvfxs", offset + 6, (int)mem.byteSize());
      } else {
         Map<Integer, ModelVFX> modelVFXs = null;
         if (hasModelVFXs(mem, offset)) {
            int off = offset + 6;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("ModelVFXs", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("ModelVFXs", len, 4096000);
            }

            modelVFXs = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               ModelVFX value = ModelVFX.toObject(mem, off);
               off += value.computeSize();
               if (modelVFXs.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("ModelVFXs", key);
               }
            }
         }

         return new UpdateModelvfxs(
            UpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))), mem.get(PacketIO.PROTO_INT, (long)(offset + 2)), modelVFXs
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.modelVFXs != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.maxId);
      if (this.modelVFXs != null) {
         if (this.modelVFXs.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ModelVFXs", this.modelVFXs.size(), 4096000);
         }

         VarInt.write(buf, this.modelVFXs.size());

         for (Entry<Integer, ModelVFX> e : this.modelVFXs.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.modelVFXs != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      mem.set(PacketIO.PROTO_INT, (long)(offset + 2), this.maxId);
      int varOffset = offset + 6;
      if (this.modelVFXs != null) {
         if (this.modelVFXs.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ModelVFXs", this.modelVFXs.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.modelVFXs.size());

         for (Entry<Integer, ModelVFX> e : this.modelVFXs.entrySet()) {
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
      if (this.modelVFXs != null) {
         int modelVFXsSize = 0;

         for (Entry<Integer, ModelVFX> kvp : this.modelVFXs.entrySet()) {
            modelVFXsSize += 4 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.modelVFXs.size()) + modelVFXsSize;
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
               int modelVFXsCount = VarInt.peek(buffer, v);
               if (modelVFXsCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for ModelVFXs");
               }

               if (modelVFXsCount > 4096000) {
                  return ValidationResult.error("ModelVFXs exceeds max length 4096000");
               }

               v += VarInt.size(modelVFXsCount);

               for (int i = 0; i < modelVFXsCount; i++) {
                  v += 4;
                  if (v > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading key");
                  }

                  v += ModelVFX.computeBytesConsumed(buffer, v);
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public UpdateModelvfxs clone() {
      UpdateModelvfxs copy = new UpdateModelvfxs();
      copy.type = this.type;
      copy.maxId = this.maxId;
      if (this.modelVFXs != null) {
         Map<Integer, ModelVFX> m = new HashMap<>();

         for (Entry<Integer, ModelVFX> e : this.modelVFXs.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.modelVFXs = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateModelvfxs other)
            ? false
            : Objects.equals(this.type, other.type) && this.maxId == other.maxId && Objects.equals(this.modelVFXs, other.modelVFXs);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.maxId, this.modelVFXs);
   }
}
