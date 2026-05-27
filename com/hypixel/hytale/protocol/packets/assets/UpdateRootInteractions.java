package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.RootInteraction;
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

public class UpdateRootInteractions implements Packet, ToClientPacket {
   public static final int PACKET_ID = 67;
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
   public Map<Integer, RootInteraction> interactions;

   @Override
   public int getId() {
      return 67;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateRootInteractions() {
   }

   public UpdateRootInteractions(@Nonnull UpdateType type, int maxId, @Nullable Map<Integer, RootInteraction> interactions) {
      this.type = type;
      this.maxId = maxId;
      this.interactions = interactions;
   }

   public UpdateRootInteractions(@Nonnull UpdateRootInteractions other) {
      this.type = other.type;
      this.maxId = other.maxId;
      this.interactions = other.interactions;
   }

   @Nonnull
   public static UpdateRootInteractions deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 6) {
         throw ProtocolException.bufferTooSmall("UpdateRootInteractions", 6, buf.readableBytes() - offset);
      } else {
         UpdateRootInteractions obj = new UpdateRootInteractions();
         byte nullBits = buf.getByte(offset);
         obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
         obj.maxId = buf.getIntLE(offset + 2);
         int pos = offset + 6;
         if ((nullBits & 1) != 0) {
            int interactionsCount = VarInt.peek(buf, pos);
            if (interactionsCount < 0) {
               throw ProtocolException.invalidVarInt("Interactions");
            }

            int interactionsVarLen = VarInt.size(interactionsCount);
            if (interactionsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Interactions", interactionsCount, 4096000);
            }

            pos += interactionsVarLen;
            obj.interactions = new HashMap<>(interactionsCount);

            for (int i = 0; i < interactionsCount; i++) {
               int key = buf.getIntLE(pos);
               pos += 4;
               RootInteraction val = RootInteraction.deserialize(buf, pos);
               pos += RootInteraction.computeBytesConsumed(buf, pos);
               if (obj.interactions.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("interactions", key);
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
            pos += RootInteraction.computeBytesConsumed(buf, pos);
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
   public static Map<Integer, RootInteraction> getInteractions(MemorySegment mem) {
      return getInteractions(mem, 0);
   }

   @Nullable
   public static Map<Integer, RootInteraction> getInteractions(MemorySegment mem, int offset) {
      if (!hasInteractions(mem, offset)) {
         return null;
      } else {
         int off = offset + 6;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Interactions", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Interactions", len, 4096000);
         } else {
            Map<Integer, RootInteraction> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               RootInteraction value = RootInteraction.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Interactions", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean hasInteractions(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static UpdateRootInteractions toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateRootInteractions toObject(MemorySegment mem, int offset) {
      if (offset + 6 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateRootInteractions", offset + 6, (int)mem.byteSize());
      } else {
         Map<Integer, RootInteraction> interactions = null;
         if (hasInteractions(mem, offset)) {
            int off = offset + 6;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Interactions", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Interactions", len, 4096000);
            }

            interactions = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               RootInteraction value = RootInteraction.toObject(mem, off);
               off += value.computeSize();
               if (interactions.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Interactions", key);
               }
            }
         }

         return new UpdateRootInteractions(
            UpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))), mem.get(PacketIO.PROTO_INT, (long)(offset + 2)), interactions
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.interactions != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.maxId);
      if (this.interactions != null) {
         if (this.interactions.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Interactions", this.interactions.size(), 4096000);
         }

         VarInt.write(buf, this.interactions.size());

         for (Entry<Integer, RootInteraction> e : this.interactions.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.interactions != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      mem.set(PacketIO.PROTO_INT, (long)(offset + 2), this.maxId);
      int varOffset = offset + 6;
      if (this.interactions != null) {
         if (this.interactions.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Interactions", this.interactions.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.interactions.size());

         for (Entry<Integer, RootInteraction> e : this.interactions.entrySet()) {
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
      if (this.interactions != null) {
         int interactionsSize = 0;

         for (Entry<Integer, RootInteraction> kvp : this.interactions.entrySet()) {
            interactionsSize += 4 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.interactions.size()) + interactionsSize;
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
               int interactionsCount = VarInt.peek(buffer, v);
               if (interactionsCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for Interactions");
               }

               if (interactionsCount > 4096000) {
                  return ValidationResult.error("Interactions exceeds max length 4096000");
               }

               v += VarInt.size(interactionsCount);

               for (int i = 0; i < interactionsCount; i++) {
                  v += 4;
                  if (v > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading key");
                  }

                  v += RootInteraction.computeBytesConsumed(buffer, v);
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public UpdateRootInteractions clone() {
      UpdateRootInteractions copy = new UpdateRootInteractions();
      copy.type = this.type;
      copy.maxId = this.maxId;
      if (this.interactions != null) {
         Map<Integer, RootInteraction> m = new HashMap<>();

         for (Entry<Integer, RootInteraction> e : this.interactions.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.interactions = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateRootInteractions other)
            ? false
            : Objects.equals(this.type, other.type) && this.maxId == other.maxId && Objects.equals(this.interactions, other.interactions);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.maxId, this.interactions);
   }
}
