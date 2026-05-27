package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.TagPattern;
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

public class UpdateTagPatterns implements Packet, ToClientPacket {
   public static final int PACKET_ID = 84;
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
   public Map<Integer, TagPattern> patterns;

   @Override
   public int getId() {
      return 84;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateTagPatterns() {
   }

   public UpdateTagPatterns(@Nonnull UpdateType type, int maxId, @Nullable Map<Integer, TagPattern> patterns) {
      this.type = type;
      this.maxId = maxId;
      this.patterns = patterns;
   }

   public UpdateTagPatterns(@Nonnull UpdateTagPatterns other) {
      this.type = other.type;
      this.maxId = other.maxId;
      this.patterns = other.patterns;
   }

   @Nonnull
   public static UpdateTagPatterns deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 6) {
         throw ProtocolException.bufferTooSmall("UpdateTagPatterns", 6, buf.readableBytes() - offset);
      } else {
         UpdateTagPatterns obj = new UpdateTagPatterns();
         byte nullBits = buf.getByte(offset);
         obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
         obj.maxId = buf.getIntLE(offset + 2);
         int pos = offset + 6;
         if ((nullBits & 1) != 0) {
            int patternsCount = VarInt.peek(buf, pos);
            if (patternsCount < 0) {
               throw ProtocolException.invalidVarInt("Patterns");
            }

            int patternsVarLen = VarInt.size(patternsCount);
            if (patternsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Patterns", patternsCount, 4096000);
            }

            pos += patternsVarLen;
            obj.patterns = new HashMap<>(patternsCount);

            for (int i = 0; i < patternsCount; i++) {
               int key = buf.getIntLE(pos);
               pos += 4;
               TagPattern val = TagPattern.deserialize(buf, pos);
               pos += TagPattern.computeBytesConsumed(buf, pos);
               if (obj.patterns.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("patterns", key);
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
            pos += TagPattern.computeBytesConsumed(buf, pos);
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
   public static Map<Integer, TagPattern> getPatterns(MemorySegment mem) {
      return getPatterns(mem, 0);
   }

   @Nullable
   public static Map<Integer, TagPattern> getPatterns(MemorySegment mem, int offset) {
      if (!hasPatterns(mem, offset)) {
         return null;
      } else {
         int off = offset + 6;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Patterns", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Patterns", len, 4096000);
         } else {
            Map<Integer, TagPattern> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               TagPattern value = TagPattern.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Patterns", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean hasPatterns(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static UpdateTagPatterns toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateTagPatterns toObject(MemorySegment mem, int offset) {
      if (offset + 6 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateTagPatterns", offset + 6, (int)mem.byteSize());
      } else {
         Map<Integer, TagPattern> patterns = null;
         if (hasPatterns(mem, offset)) {
            int off = offset + 6;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Patterns", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Patterns", len, 4096000);
            }

            patterns = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               TagPattern value = TagPattern.toObject(mem, off);
               off += value.computeSize();
               if (patterns.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Patterns", key);
               }
            }
         }

         return new UpdateTagPatterns(
            UpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))), mem.get(PacketIO.PROTO_INT, (long)(offset + 2)), patterns
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.patterns != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.maxId);
      if (this.patterns != null) {
         if (this.patterns.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Patterns", this.patterns.size(), 4096000);
         }

         VarInt.write(buf, this.patterns.size());

         for (Entry<Integer, TagPattern> e : this.patterns.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.patterns != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      mem.set(PacketIO.PROTO_INT, (long)(offset + 2), this.maxId);
      int varOffset = offset + 6;
      if (this.patterns != null) {
         if (this.patterns.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Patterns", this.patterns.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.patterns.size());

         for (Entry<Integer, TagPattern> e : this.patterns.entrySet()) {
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
      if (this.patterns != null) {
         int patternsSize = 0;

         for (Entry<Integer, TagPattern> kvp : this.patterns.entrySet()) {
            patternsSize += 4 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.patterns.size()) + patternsSize;
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
               int patternsCount = VarInt.peek(buffer, v);
               if (patternsCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for Patterns");
               }

               if (patternsCount > 4096000) {
                  return ValidationResult.error("Patterns exceeds max length 4096000");
               }

               v += VarInt.size(patternsCount);

               for (int i = 0; i < patternsCount; i++) {
                  v += 4;
                  if (v > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading key");
                  }

                  v += TagPattern.computeBytesConsumed(buffer, v);
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public UpdateTagPatterns clone() {
      UpdateTagPatterns copy = new UpdateTagPatterns();
      copy.type = this.type;
      copy.maxId = this.maxId;
      if (this.patterns != null) {
         Map<Integer, TagPattern> m = new HashMap<>();

         for (Entry<Integer, TagPattern> e : this.patterns.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.patterns = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateTagPatterns other)
            ? false
            : Objects.equals(this.type, other.type) && this.maxId == other.maxId && Objects.equals(this.patterns, other.patterns);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.maxId, this.patterns);
   }
}
