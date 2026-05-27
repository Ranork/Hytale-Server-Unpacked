package com.hypixel.hytale.protocol.packets.setup;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
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

public class UpdateFeatures implements Packet, ToClientPacket {
   public static final int PACKET_ID = 31;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 8192006;
   @Nullable
   public Map<ClientFeature, Boolean> features;

   @Override
   public int getId() {
      return 31;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateFeatures() {
   }

   public UpdateFeatures(@Nullable Map<ClientFeature, Boolean> features) {
      this.features = features;
   }

   public UpdateFeatures(@Nonnull UpdateFeatures other) {
      this.features = other.features;
   }

   @Nonnull
   public static UpdateFeatures deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("UpdateFeatures", 1, buf.readableBytes() - offset);
      } else {
         UpdateFeatures obj = new UpdateFeatures();
         byte nullBits = buf.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int featuresCount = VarInt.peek(buf, pos);
            if (featuresCount < 0) {
               throw ProtocolException.invalidVarInt("Features");
            }

            int featuresVarLen = VarInt.size(featuresCount);
            if (featuresCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Features", featuresCount, 4096000);
            }

            pos += featuresVarLen;
            obj.features = new HashMap<>(featuresCount);

            for (int i = 0; i < featuresCount; i++) {
               ClientFeature key = ClientFeature.fromValue(buf.getByte(pos));
               pos++;
               boolean val = buf.getByte(pos) != 0;
               pos++;
               if (obj.features.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("features", key);
               }
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int dictLen = VarInt.peek(buf, pos);
         pos += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos++;
            pos++;
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   @Nullable
   public static Map<ClientFeature, Boolean> getFeatures(MemorySegment mem) {
      return getFeatures(mem, 0);
   }

   @Nullable
   public static Map<ClientFeature, Boolean> getFeatures(MemorySegment mem, int offset) {
      if (!hasFeatures(mem, offset)) {
         return null;
      } else {
         int off = offset + 1;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Features", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Features", len, 4096000);
         } else {
            Map<ClientFeature, Boolean> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               ClientFeature key = ClientFeature.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)off));
               boolean value = mem.get(PacketIO.PROTO_BOOL, (long)(++off));
               off++;
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Features", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean hasFeatures(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static UpdateFeatures toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateFeatures toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateFeatures", offset + 1, (int)mem.byteSize());
      } else {
         Map<ClientFeature, Boolean> features = null;
         if (hasFeatures(mem, offset)) {
            int off = offset + 1;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Features", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Features", len, 4096000);
            }

            features = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               ClientFeature key = ClientFeature.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)off));
               boolean value = mem.get(PacketIO.PROTO_BOOL, (long)(++off));
               off++;
               if (features.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Features", key);
               }
            }
         }

         return new UpdateFeatures(features);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.features != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.features != null) {
         if (this.features.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Features", this.features.size(), 4096000);
         }

         VarInt.write(buf, this.features.size());

         for (Entry<ClientFeature, Boolean> e : this.features.entrySet()) {
            buf.writeByte(e.getKey().getValue());
            buf.writeByte(e.getValue() ? 1 : 0);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.features != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 1;
      if (this.features != null) {
         if (this.features.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Features", this.features.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.features.size());

         for (Entry<ClientFeature, Boolean> e : this.features.entrySet()) {
            mem.set(PacketIO.PROTO_BYTE, (long)varOffset, (byte)e.getKey().getValue());
            mem.set(PacketIO.PROTO_BOOL, ++varOffset, e.getValue());
            varOffset++;
         }
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.features != null) {
         size += VarInt.size(this.features.size()) + this.features.size() * 2;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 1) {
         return ValidationResult.error("Buffer too small: expected at least 1 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int featuresCount = VarInt.peek(buffer, pos);
            if (featuresCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Features");
            }

            if (featuresCount > 4096000) {
               return ValidationResult.error("Features exceeds max length 4096000");
            }

            pos += VarInt.size(featuresCount);

            for (int i = 0; i < featuresCount; i++) {
               int v = buffer.getByte(pos) & 255;
               if (v >= 11) {
                  return ValidationResult.error("Invalid ClientFeature value for key");
               }

               pos++;
               if (++pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading value");
               }
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateFeatures clone() {
      UpdateFeatures copy = new UpdateFeatures();
      copy.features = this.features != null ? new HashMap<>(this.features) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof UpdateFeatures other ? Objects.equals(this.features, other.features) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.features);
   }
}
