package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.WorldEnvironment;
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

public class UpdateEnvironments implements Packet, ToClientPacket {
   public static final int PACKET_ID = 61;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 7;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 7;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public UpdateType type = UpdateType.Init;
   public int maxId;
   @Nullable
   public Map<Integer, WorldEnvironment> environments;
   public boolean rebuildMapGeometry;

   @Override
   public int getId() {
      return 61;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateEnvironments() {
   }

   public UpdateEnvironments(@Nonnull UpdateType type, int maxId, @Nullable Map<Integer, WorldEnvironment> environments, boolean rebuildMapGeometry) {
      this.type = type;
      this.maxId = maxId;
      this.environments = environments;
      this.rebuildMapGeometry = rebuildMapGeometry;
   }

   public UpdateEnvironments(@Nonnull UpdateEnvironments other) {
      this.type = other.type;
      this.maxId = other.maxId;
      this.environments = other.environments;
      this.rebuildMapGeometry = other.rebuildMapGeometry;
   }

   @Nonnull
   public static UpdateEnvironments deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 7) {
         throw ProtocolException.bufferTooSmall("UpdateEnvironments", 7, buf.readableBytes() - offset);
      } else {
         UpdateEnvironments obj = new UpdateEnvironments();
         byte nullBits = buf.getByte(offset);
         obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
         obj.maxId = buf.getIntLE(offset + 2);
         obj.rebuildMapGeometry = buf.getByte(offset + 6) != 0;
         int pos = offset + 7;
         if ((nullBits & 1) != 0) {
            int environmentsCount = VarInt.peek(buf, pos);
            if (environmentsCount < 0) {
               throw ProtocolException.invalidVarInt("Environments");
            }

            int environmentsVarLen = VarInt.size(environmentsCount);
            if (environmentsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Environments", environmentsCount, 4096000);
            }

            pos += environmentsVarLen;
            obj.environments = new HashMap<>(environmentsCount);

            for (int i = 0; i < environmentsCount; i++) {
               int key = buf.getIntLE(pos);
               pos += 4;
               WorldEnvironment val = WorldEnvironment.deserialize(buf, pos);
               pos += WorldEnvironment.computeBytesConsumed(buf, pos);
               if (obj.environments.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("environments", key);
               }
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 7;
      if ((nullBits & 1) != 0) {
         int dictLen = VarInt.peek(buf, pos);
         pos += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos += 4;
            pos += WorldEnvironment.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 7L;
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
   public static Map<Integer, WorldEnvironment> getEnvironments(MemorySegment mem) {
      return getEnvironments(mem, 0);
   }

   @Nullable
   public static Map<Integer, WorldEnvironment> getEnvironments(MemorySegment mem, int offset) {
      if (!hasEnvironments(mem, offset)) {
         return null;
      } else {
         int off = offset + 7;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Environments", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Environments", len, 4096000);
         } else {
            Map<Integer, WorldEnvironment> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               WorldEnvironment value = WorldEnvironment.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Environments", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean getRebuildMapGeometry(MemorySegment mem) {
      return getRebuildMapGeometry(mem, 0);
   }

   public static boolean getRebuildMapGeometry(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 6));
   }

   public static boolean hasEnvironments(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static UpdateEnvironments toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateEnvironments toObject(MemorySegment mem, int offset) {
      if (offset + 7 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateEnvironments", offset + 7, (int)mem.byteSize());
      } else {
         Map<Integer, WorldEnvironment> environments = null;
         if (hasEnvironments(mem, offset)) {
            int off = offset + 7;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Environments", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Environments", len, 4096000);
            }

            environments = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               WorldEnvironment value = WorldEnvironment.toObject(mem, off);
               off += value.computeSize();
               if (environments.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Environments", key);
               }
            }
         }

         return new UpdateEnvironments(
            UpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 2)),
            environments,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 6))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.environments != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.maxId);
      buf.writeByte(this.rebuildMapGeometry ? 1 : 0);
      if (this.environments != null) {
         if (this.environments.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Environments", this.environments.size(), 4096000);
         }

         VarInt.write(buf, this.environments.size());

         for (Entry<Integer, WorldEnvironment> e : this.environments.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.environments != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      mem.set(PacketIO.PROTO_INT, (long)(offset + 2), this.maxId);
      mem.set(PacketIO.PROTO_BOOL, offset + 6, this.rebuildMapGeometry);
      int varOffset = offset + 7;
      if (this.environments != null) {
         if (this.environments.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Environments", this.environments.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.environments.size());

         for (Entry<Integer, WorldEnvironment> e : this.environments.entrySet()) {
            mem.set(PacketIO.PROTO_INT, (long)varOffset, e.getKey());
            varOffset += 4;
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 7;
      if (this.environments != null) {
         int environmentsSize = 0;

         for (Entry<Integer, WorldEnvironment> kvp : this.environments.entrySet()) {
            environmentsSize += 4 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.environments.size()) + environmentsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 7) {
         return ValidationResult.error("Buffer too small: expected at least 7 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 3) {
            return ValidationResult.error("Invalid UpdateType value for Type");
         } else {
            v = offset + 7;
            if ((nullBits & 1) != 0) {
               int environmentsCount = VarInt.peek(buffer, v);
               if (environmentsCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for Environments");
               }

               if (environmentsCount > 4096000) {
                  return ValidationResult.error("Environments exceeds max length 4096000");
               }

               v += VarInt.size(environmentsCount);

               for (int i = 0; i < environmentsCount; i++) {
                  v += 4;
                  if (v > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading key");
                  }

                  v += WorldEnvironment.computeBytesConsumed(buffer, v);
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public UpdateEnvironments clone() {
      UpdateEnvironments copy = new UpdateEnvironments();
      copy.type = this.type;
      copy.maxId = this.maxId;
      if (this.environments != null) {
         Map<Integer, WorldEnvironment> m = new HashMap<>();

         for (Entry<Integer, WorldEnvironment> e : this.environments.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.environments = m;
      }

      copy.rebuildMapGeometry = this.rebuildMapGeometry;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateEnvironments other)
            ? false
            : Objects.equals(this.type, other.type)
               && this.maxId == other.maxId
               && Objects.equals(this.environments, other.environments)
               && this.rebuildMapGeometry == other.rebuildMapGeometry;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.maxId, this.environments, this.rebuildMapGeometry);
   }
}
