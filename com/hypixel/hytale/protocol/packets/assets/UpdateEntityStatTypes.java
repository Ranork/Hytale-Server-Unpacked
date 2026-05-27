package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.EntityStatType;
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

public class UpdateEntityStatTypes implements Packet, ToClientPacket {
   public static final int PACKET_ID = 72;
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
   public Map<Integer, EntityStatType> types;

   @Override
   public int getId() {
      return 72;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateEntityStatTypes() {
   }

   public UpdateEntityStatTypes(@Nonnull UpdateType type, int maxId, @Nullable Map<Integer, EntityStatType> types) {
      this.type = type;
      this.maxId = maxId;
      this.types = types;
   }

   public UpdateEntityStatTypes(@Nonnull UpdateEntityStatTypes other) {
      this.type = other.type;
      this.maxId = other.maxId;
      this.types = other.types;
   }

   @Nonnull
   public static UpdateEntityStatTypes deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 6) {
         throw ProtocolException.bufferTooSmall("UpdateEntityStatTypes", 6, buf.readableBytes() - offset);
      } else {
         UpdateEntityStatTypes obj = new UpdateEntityStatTypes();
         byte nullBits = buf.getByte(offset);
         obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
         obj.maxId = buf.getIntLE(offset + 2);
         int pos = offset + 6;
         if ((nullBits & 1) != 0) {
            int typesCount = VarInt.peek(buf, pos);
            if (typesCount < 0) {
               throw ProtocolException.invalidVarInt("Types");
            }

            int typesVarLen = VarInt.size(typesCount);
            if (typesCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Types", typesCount, 4096000);
            }

            pos += typesVarLen;
            obj.types = new HashMap<>(typesCount);

            for (int i = 0; i < typesCount; i++) {
               int key = buf.getIntLE(pos);
               pos += 4;
               EntityStatType val = EntityStatType.deserialize(buf, pos);
               pos += EntityStatType.computeBytesConsumed(buf, pos);
               if (obj.types.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("types", key);
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
            pos += EntityStatType.computeBytesConsumed(buf, pos);
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
   public static Map<Integer, EntityStatType> getTypes(MemorySegment mem) {
      return getTypes(mem, 0);
   }

   @Nullable
   public static Map<Integer, EntityStatType> getTypes(MemorySegment mem, int offset) {
      if (!hasTypes(mem, offset)) {
         return null;
      } else {
         int off = offset + 6;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Types", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Types", len, 4096000);
         } else {
            Map<Integer, EntityStatType> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               EntityStatType value = EntityStatType.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Types", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean hasTypes(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static UpdateEntityStatTypes toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateEntityStatTypes toObject(MemorySegment mem, int offset) {
      if (offset + 6 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateEntityStatTypes", offset + 6, (int)mem.byteSize());
      } else {
         Map<Integer, EntityStatType> types = null;
         if (hasTypes(mem, offset)) {
            int off = offset + 6;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Types", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Types", len, 4096000);
            }

            types = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               EntityStatType value = EntityStatType.toObject(mem, off);
               off += value.computeSize();
               if (types.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Types", key);
               }
            }
         }

         return new UpdateEntityStatTypes(
            UpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))), mem.get(PacketIO.PROTO_INT, (long)(offset + 2)), types
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.types != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.maxId);
      if (this.types != null) {
         if (this.types.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Types", this.types.size(), 4096000);
         }

         VarInt.write(buf, this.types.size());

         for (Entry<Integer, EntityStatType> e : this.types.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.types != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      mem.set(PacketIO.PROTO_INT, (long)(offset + 2), this.maxId);
      int varOffset = offset + 6;
      if (this.types != null) {
         if (this.types.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Types", this.types.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.types.size());

         for (Entry<Integer, EntityStatType> e : this.types.entrySet()) {
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
      if (this.types != null) {
         int typesSize = 0;

         for (Entry<Integer, EntityStatType> kvp : this.types.entrySet()) {
            typesSize += 4 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.types.size()) + typesSize;
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
               int typesCount = VarInt.peek(buffer, v);
               if (typesCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for Types");
               }

               if (typesCount > 4096000) {
                  return ValidationResult.error("Types exceeds max length 4096000");
               }

               v += VarInt.size(typesCount);

               for (int i = 0; i < typesCount; i++) {
                  v += 4;
                  if (v > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading key");
                  }

                  v += EntityStatType.computeBytesConsumed(buffer, v);
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public UpdateEntityStatTypes clone() {
      UpdateEntityStatTypes copy = new UpdateEntityStatTypes();
      copy.type = this.type;
      copy.maxId = this.maxId;
      if (this.types != null) {
         Map<Integer, EntityStatType> m = new HashMap<>();

         for (Entry<Integer, EntityStatType> e : this.types.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.types = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateEntityStatTypes other)
            ? false
            : Objects.equals(this.type, other.type) && this.maxId == other.maxId && Objects.equals(this.types, other.types);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.maxId, this.types);
   }
}
