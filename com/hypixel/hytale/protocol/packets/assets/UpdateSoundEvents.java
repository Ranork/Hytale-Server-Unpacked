package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.SoundEvent;
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

public class UpdateSoundEvents implements Packet, ToClientPacket {
   public static final int PACKET_ID = 65;
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
   public Map<Integer, SoundEvent> soundEvents;

   @Override
   public int getId() {
      return 65;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateSoundEvents() {
   }

   public UpdateSoundEvents(@Nonnull UpdateType type, int maxId, @Nullable Map<Integer, SoundEvent> soundEvents) {
      this.type = type;
      this.maxId = maxId;
      this.soundEvents = soundEvents;
   }

   public UpdateSoundEvents(@Nonnull UpdateSoundEvents other) {
      this.type = other.type;
      this.maxId = other.maxId;
      this.soundEvents = other.soundEvents;
   }

   @Nonnull
   public static UpdateSoundEvents deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateSoundEvents obj = new UpdateSoundEvents();
      byte nullBits = buf.getByte(offset);
      obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
      obj.maxId = buf.getIntLE(offset + 2);
      int pos = offset + 6;
      if ((nullBits & 1) != 0) {
         int soundEventsCount = VarInt.peek(buf, pos);
         if (soundEventsCount < 0) {
            throw ProtocolException.negativeLength("SoundEvents", soundEventsCount);
         }

         if (soundEventsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SoundEvents", soundEventsCount, 4096000);
         }

         pos += VarInt.size(soundEventsCount);
         obj.soundEvents = new HashMap<>(soundEventsCount);

         for (int i = 0; i < soundEventsCount; i++) {
            int key = buf.getIntLE(pos);
            pos += 4;
            SoundEvent val = SoundEvent.deserialize(buf, pos);
            pos += SoundEvent.computeBytesConsumed(buf, pos);
            if (obj.soundEvents.put(key, val) != null) {
               throw ProtocolException.duplicateKey("soundEvents", key);
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
            pos += SoundEvent.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.soundEvents != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.maxId);
      if (this.soundEvents != null) {
         if (this.soundEvents.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SoundEvents", this.soundEvents.size(), 4096000);
         }

         VarInt.write(buf, this.soundEvents.size());

         for (Entry<Integer, SoundEvent> e : this.soundEvents.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int computeSize() {
      int size = 6;
      if (this.soundEvents != null) {
         int soundEventsSize = 0;

         for (Entry<Integer, SoundEvent> kvp : this.soundEvents.entrySet()) {
            soundEventsSize += 4 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.soundEvents.size()) + soundEventsSize;
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
            int soundEventsCount = VarInt.peek(buffer, pos);
            if (soundEventsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for SoundEvents");
            }

            if (soundEventsCount > 4096000) {
               return ValidationResult.error("SoundEvents exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < soundEventsCount; i++) {
               pos += 4;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               pos += SoundEvent.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateSoundEvents clone() {
      UpdateSoundEvents copy = new UpdateSoundEvents();
      copy.type = this.type;
      copy.maxId = this.maxId;
      if (this.soundEvents != null) {
         Map<Integer, SoundEvent> m = new HashMap<>();

         for (Entry<Integer, SoundEvent> e : this.soundEvents.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.soundEvents = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateSoundEvents other)
            ? false
            : Objects.equals(this.type, other.type) && this.maxId == other.maxId && Objects.equals(this.soundEvents, other.soundEvents);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.maxId, this.soundEvents);
   }
}
