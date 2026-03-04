package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
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
      UpdateUnarmedInteractions obj = new UpdateUnarmedInteractions();
      byte nullBits = buf.getByte(offset);
      obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         int interactionsCount = VarInt.peek(buf, pos);
         if (interactionsCount < 0) {
            throw ProtocolException.negativeLength("Interactions", interactionsCount);
         }

         if (interactionsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Interactions", interactionsCount, 4096000);
         }

         pos += VarInt.size(interactionsCount);
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

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         int dictLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos);

         for (int i = 0; i < dictLen; i++) {
            pos = ++pos + 4;
         }
      }

      return pos - offset;
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
         int pos = offset + 2;
         if ((nullBits & 1) != 0) {
            int interactionsCount = VarInt.peek(buffer, pos);
            if (interactionsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Interactions");
            }

            if (interactionsCount > 4096000) {
               return ValidationResult.error("Interactions exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < interactionsCount; i++) {
               pos = ++pos + 4;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading value");
               }
            }
         }

         return ValidationResult.OK;
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
