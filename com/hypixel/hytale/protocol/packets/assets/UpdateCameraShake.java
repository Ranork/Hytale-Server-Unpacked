package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.CameraShake;
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

public class UpdateCameraShake implements Packet, ToClientPacket {
   public static final int PACKET_ID = 77;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 2;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public UpdateType type = UpdateType.Init;
   @Nullable
   public Map<Integer, CameraShake> profiles;

   @Override
   public int getId() {
      return 77;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateCameraShake() {
   }

   public UpdateCameraShake(@Nonnull UpdateType type, @Nullable Map<Integer, CameraShake> profiles) {
      this.type = type;
      this.profiles = profiles;
   }

   public UpdateCameraShake(@Nonnull UpdateCameraShake other) {
      this.type = other.type;
      this.profiles = other.profiles;
   }

   @Nonnull
   public static UpdateCameraShake deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 2) {
         throw ProtocolException.bufferTooSmall("UpdateCameraShake", 2, buf.readableBytes() - offset);
      } else {
         UpdateCameraShake obj = new UpdateCameraShake();
         byte nullBits = buf.getByte(offset);
         obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
         int pos = offset + 2;
         if ((nullBits & 1) != 0) {
            int profilesCount = VarInt.peek(buf, pos);
            if (profilesCount < 0) {
               throw ProtocolException.invalidVarInt("Profiles");
            }

            int profilesVarLen = VarInt.size(profilesCount);
            if (profilesCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Profiles", profilesCount, 4096000);
            }

            pos += profilesVarLen;
            obj.profiles = new HashMap<>(profilesCount);

            for (int i = 0; i < profilesCount; i++) {
               int key = buf.getIntLE(pos);
               pos += 4;
               CameraShake val = CameraShake.deserialize(buf, pos);
               pos += CameraShake.computeBytesConsumed(buf, pos);
               if (obj.profiles.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("profiles", key);
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
            pos += 4;
            pos += CameraShake.computeBytesConsumed(buf, pos);
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
   public static Map<Integer, CameraShake> getProfiles(MemorySegment mem) {
      return getProfiles(mem, 0);
   }

   @Nullable
   public static Map<Integer, CameraShake> getProfiles(MemorySegment mem, int offset) {
      if (!hasProfiles(mem, offset)) {
         return null;
      } else {
         int off = offset + 2;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Profiles", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Profiles", len, 4096000);
         } else {
            Map<Integer, CameraShake> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               CameraShake value = CameraShake.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Profiles", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean hasProfiles(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static UpdateCameraShake toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateCameraShake toObject(MemorySegment mem, int offset) {
      if (offset + 2 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateCameraShake", offset + 2, (int)mem.byteSize());
      } else {
         Map<Integer, CameraShake> profiles = null;
         if (hasProfiles(mem, offset)) {
            int off = offset + 2;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Profiles", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Profiles", len, 4096000);
            }

            profiles = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               CameraShake value = CameraShake.toObject(mem, off);
               off += value.computeSize();
               if (profiles.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Profiles", key);
               }
            }
         }

         return new UpdateCameraShake(UpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))), profiles);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.profiles != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      if (this.profiles != null) {
         if (this.profiles.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Profiles", this.profiles.size(), 4096000);
         }

         VarInt.write(buf, this.profiles.size());

         for (Entry<Integer, CameraShake> e : this.profiles.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.profiles != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      int varOffset = offset + 2;
      if (this.profiles != null) {
         if (this.profiles.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Profiles", this.profiles.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.profiles.size());

         for (Entry<Integer, CameraShake> e : this.profiles.entrySet()) {
            mem.set(PacketIO.PROTO_INT, (long)varOffset, e.getKey());
            varOffset += 4;
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 2;
      if (this.profiles != null) {
         int profilesSize = 0;

         for (Entry<Integer, CameraShake> kvp : this.profiles.entrySet()) {
            profilesSize += 4 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.profiles.size()) + profilesSize;
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
               int profilesCount = VarInt.peek(buffer, v);
               if (profilesCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for Profiles");
               }

               if (profilesCount > 4096000) {
                  return ValidationResult.error("Profiles exceeds max length 4096000");
               }

               v += VarInt.size(profilesCount);

               for (int i = 0; i < profilesCount; i++) {
                  v += 4;
                  if (v > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading key");
                  }

                  v += CameraShake.computeBytesConsumed(buffer, v);
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public UpdateCameraShake clone() {
      UpdateCameraShake copy = new UpdateCameraShake();
      copy.type = this.type;
      if (this.profiles != null) {
         Map<Integer, CameraShake> m = new HashMap<>();

         for (Entry<Integer, CameraShake> e : this.profiles.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.profiles = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateCameraShake other) ? false : Objects.equals(this.type, other.type) && Objects.equals(this.profiles, other.profiles);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.profiles);
   }
}
