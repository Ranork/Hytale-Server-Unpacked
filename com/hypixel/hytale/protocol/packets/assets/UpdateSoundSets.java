package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.SoundSet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdateSoundSets implements Packet, ToClientPacket {
   public static final int PACKET_ID = 79;
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
   public Map<Integer, SoundSet> soundSets;

   @Override
   public int getId() {
      return 79;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateSoundSets() {
   }

   public UpdateSoundSets(@Nonnull UpdateType type, int maxId, @Nullable Map<Integer, SoundSet> soundSets) {
      this.type = type;
      this.maxId = maxId;
      this.soundSets = soundSets;
   }

   public UpdateSoundSets(@Nonnull UpdateSoundSets other) {
      this.type = other.type;
      this.maxId = other.maxId;
      this.soundSets = other.soundSets;
   }

   @Nonnull
   public static UpdateSoundSets deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateSoundSets obj = new UpdateSoundSets();
      byte nullBits = buf.getByte(offset);
      obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
      obj.maxId = buf.getIntLE(offset + 2);
      int pos = offset + 6;
      if ((nullBits & 1) != 0) {
         int soundSetsCount = VarInt.peek(buf, pos);
         if (soundSetsCount < 0) {
            throw ProtocolException.negativeLength("SoundSets", soundSetsCount);
         }

         if (soundSetsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SoundSets", soundSetsCount, 4096000);
         }

         pos += VarInt.size(soundSetsCount);
         obj.soundSets = new HashMap<>(soundSetsCount);

         for (int i = 0; i < soundSetsCount; i++) {
            int key = buf.getIntLE(pos);
            pos += 4;
            SoundSet val = SoundSet.deserialize(buf, pos);
            pos += SoundSet.computeBytesConsumed(buf, pos);
            if (obj.soundSets.put(key, val) != null) {
               throw ProtocolException.duplicateKey("soundSets", key);
            }
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 6;
      if ((nullBits & 1) != 0) {
         int dictLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos);

         for (int i = 0; i < dictLen; i++) {
            pos += 4;
            pos += SoundSet.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.soundSets != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.maxId);
      if (this.soundSets != null) {
         if (this.soundSets.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SoundSets", this.soundSets.size(), 4096000);
         }

         VarInt.write(buf, this.soundSets.size());

         for (Entry<Integer, SoundSet> e : this.soundSets.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int computeSize() {
      int size = 6;
      if (this.soundSets != null) {
         int soundSetsSize = 0;

         for (Entry<Integer, SoundSet> kvp : this.soundSets.entrySet()) {
            soundSetsSize += 4 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.soundSets.size()) + soundSetsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 6) {
         return ValidationResult.error("Buffer too small: expected at least 6 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 6;
         if ((nullBits & 1) != 0) {
            int soundSetsCount = VarInt.peek(buffer, pos);
            if (soundSetsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for SoundSets");
            }

            if (soundSetsCount > 4096000) {
               return ValidationResult.error("SoundSets exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < soundSetsCount; i++) {
               pos += 4;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               pos += SoundSet.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateSoundSets clone() {
      UpdateSoundSets copy = new UpdateSoundSets();
      copy.type = this.type;
      copy.maxId = this.maxId;
      if (this.soundSets != null) {
         Map<Integer, SoundSet> m = new HashMap<>();

         for (Entry<Integer, SoundSet> e : this.soundSets.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.soundSets = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateSoundSets other)
            ? false
            : Objects.equals(this.type, other.type) && this.maxId == other.maxId && Objects.equals(this.soundSets, other.soundSets);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.maxId, this.soundSets);
   }
}
