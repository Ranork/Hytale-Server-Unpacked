package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.WorldEnvironment;
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
      UpdateEnvironments obj = new UpdateEnvironments();
      byte nullBits = buf.getByte(offset);
      obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
      obj.maxId = buf.getIntLE(offset + 2);
      obj.rebuildMapGeometry = buf.getByte(offset + 6) != 0;
      int pos = offset + 7;
      if ((nullBits & 1) != 0) {
         int environmentsCount = VarInt.peek(buf, pos);
         if (environmentsCount < 0) {
            throw ProtocolException.negativeLength("Environments", environmentsCount);
         }

         if (environmentsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Environments", environmentsCount, 4096000);
         }

         pos += VarInt.size(environmentsCount);
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

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 7;
      if ((nullBits & 1) != 0) {
         int dictLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos);

         for (int i = 0; i < dictLen; i++) {
            pos += 4;
            pos += WorldEnvironment.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
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
         int pos = offset + 7;
         if ((nullBits & 1) != 0) {
            int environmentsCount = VarInt.peek(buffer, pos);
            if (environmentsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Environments");
            }

            if (environmentsCount > 4096000) {
               return ValidationResult.error("Environments exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < environmentsCount; i++) {
               pos += 4;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               pos += WorldEnvironment.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
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
