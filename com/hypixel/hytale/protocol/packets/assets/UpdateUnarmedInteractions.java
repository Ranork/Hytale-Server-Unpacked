package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.InteractionType;
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

public class UpdateUnarmedInteractions implements Packet, ToClientPacket {
   public static final int PACKET_ID = 68;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 2;
   public static final int MAX_SIZE = 20480007;
   @Nonnull
   public UpdateType type = UpdateType.Init;
   @Nullable
   public Map<InteractionType, Integer> interactions;

   @Override
   public int getId() {
      return 68;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateUnarmedInteractions() {
   }

   public UpdateUnarmedInteractions(@Nonnull UpdateType type, @Nullable Map<InteractionType, Integer> interactions) {
      this.type = type;
      this.interactions = interactions;
   }

   public UpdateUnarmedInteractions(@Nonnull UpdateUnarmedInteractions other) {
      this.type = other.type;
      this.interactions = other.interactions;
   }

   @Nonnull
   public static UpdateUnarmedInteractions deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 2) {
         throw ProtocolException.bufferTooSmall("UpdateUnarmedInteractions", 2, buf.readableBytes() - offset);
      } else {
         UpdateUnarmedInteractions obj = new UpdateUnarmedInteractions();
         byte nullBits = buf.getByte(offset);
         obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
         int pos = offset + 2;
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
               InteractionType key = InteractionType.fromValue(buf.getByte(pos));
               int val = buf.getIntLE(++pos);
               pos += 4;
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
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         int dictLen = VarInt.peek(buf, pos);
         pos += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos = ++pos + 4;
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
   public static Map<InteractionType, Integer> getInteractions(MemorySegment mem) {
      return getInteractions(mem, 0);
   }

   @Nullable
   public static Map<InteractionType, Integer> getInteractions(MemorySegment mem, int offset) {
      if (!hasInteractions(mem, offset)) {
         return null;
      } else {
         int off = offset + 2;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Interactions", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Interactions", len, 4096000);
         } else {
            Map<InteractionType, Integer> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               InteractionType key = InteractionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)off));
               int value = mem.get(PacketIO.PROTO_INT, (long)(++off));
               off += 4;
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

   public static UpdateUnarmedInteractions toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateUnarmedInteractions toObject(MemorySegment mem, int offset) {
      if (offset + 2 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateUnarmedInteractions", offset + 2, (int)mem.byteSize());
      } else {
         Map<InteractionType, Integer> interactions = null;
         if (hasInteractions(mem, offset)) {
            int off = offset + 2;
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
               InteractionType key = InteractionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)off));
               int value = mem.get(PacketIO.PROTO_INT, (long)(++off));
               off += 4;
               if (interactions.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Interactions", key);
               }
            }
         }

         return new UpdateUnarmedInteractions(UpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))), interactions);
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
      if (this.interactions != null) {
         if (this.interactions.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Interactions", this.interactions.size(), 4096000);
         }

         VarInt.write(buf, this.interactions.size());

         for (Entry<InteractionType, Integer> e : this.interactions.entrySet()) {
            buf.writeByte(e.getKey().getValue());
            buf.writeIntLE(e.getValue());
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
      int varOffset = offset + 2;
      if (this.interactions != null) {
         if (this.interactions.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Interactions", this.interactions.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.interactions.size());

         for (Entry<InteractionType, Integer> e : this.interactions.entrySet()) {
            mem.set(PacketIO.PROTO_BYTE, (long)varOffset, (byte)e.getKey().getValue());
            mem.set(PacketIO.PROTO_INT, (long)(++varOffset), e.getValue());
            varOffset += 4;
         }
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 2;
      if (this.interactions != null) {
         size += VarInt.size(this.interactions.size()) + this.interactions.size() * 5;
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
               int interactionsCount = VarInt.peek(buffer, v);
               if (interactionsCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for Interactions");
               }

               if (interactionsCount > 4096000) {
                  return ValidationResult.error("Interactions exceeds max length 4096000");
               }

               v += VarInt.size(interactionsCount);

               for (int i = 0; i < interactionsCount; i++) {
                  int vx = buffer.getByte(v) & 255;
                  if (vx >= 25) {
                     return ValidationResult.error("Invalid InteractionType value for key");
                  }

                  v = ++v + 4;
                  if (v > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading value");
                  }
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public UpdateUnarmedInteractions clone() {
      UpdateUnarmedInteractions copy = new UpdateUnarmedInteractions();
      copy.type = this.type;
      copy.interactions = this.interactions != null ? new HashMap<>(this.interactions) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateUnarmedInteractions other)
            ? false
            : Objects.equals(this.type, other.type) && Objects.equals(this.interactions, other.interactions);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.interactions);
   }
}
