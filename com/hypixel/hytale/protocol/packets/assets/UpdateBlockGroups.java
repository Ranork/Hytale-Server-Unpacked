package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.BlockGroup;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.io.PacketIO;
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

public class UpdateBlockGroups implements Packet, ToClientPacket {
   public static final int PACKET_ID = 78;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 2;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public UpdateType type = UpdateType.Init;
   @Nullable
   public Map<String, BlockGroup> groups;

   @Override
   public int getId() {
      return 78;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateBlockGroups() {
   }

   public UpdateBlockGroups(@Nonnull UpdateType type, @Nullable Map<String, BlockGroup> groups) {
      this.type = type;
      this.groups = groups;
   }

   public UpdateBlockGroups(@Nonnull UpdateBlockGroups other) {
      this.type = other.type;
      this.groups = other.groups;
   }

   @Nonnull
   public static UpdateBlockGroups deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateBlockGroups obj = new UpdateBlockGroups();
      byte nullBits = buf.getByte(offset);
      obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         int groupsCount = VarInt.peek(buf, pos);
         if (groupsCount < 0) {
            throw ProtocolException.negativeLength("Groups", groupsCount);
         }

         if (groupsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Groups", groupsCount, 4096000);
         }

         pos += VarInt.size(groupsCount);
         obj.groups = new HashMap<>(groupsCount);

         for (int i = 0; i < groupsCount; i++) {
            int keyLen = VarInt.peek(buf, pos);
            if (keyLen < 0) {
               throw ProtocolException.negativeLength("key", keyLen);
            }

            if (keyLen > 4096000) {
               throw ProtocolException.stringTooLong("key", keyLen, 4096000);
            }

            int keyVarLen = VarInt.length(buf, pos);
            String key = PacketIO.readVarString(buf, pos);
            pos += keyVarLen + keyLen;
            BlockGroup val = BlockGroup.deserialize(buf, pos);
            pos += BlockGroup.computeBytesConsumed(buf, pos);
            if (obj.groups.put(key, val) != null) {
               throw ProtocolException.duplicateKey("groups", key);
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
            int sl = VarInt.peek(buf, pos);
            pos += VarInt.length(buf, pos) + sl;
            pos += BlockGroup.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.groups != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      if (this.groups != null) {
         if (this.groups.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Groups", this.groups.size(), 4096000);
         }

         VarInt.write(buf, this.groups.size());

         for (Entry<String, BlockGroup> e : this.groups.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int computeSize() {
      int size = 2;
      if (this.groups != null) {
         int groupsSize = 0;

         for (Entry<String, BlockGroup> kvp : this.groups.entrySet()) {
            groupsSize += PacketIO.stringSize(kvp.getKey()) + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.groups.size()) + groupsSize;
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
            int groupsCount = VarInt.peek(buffer, pos);
            if (groupsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Groups");
            }

            if (groupsCount > 4096000) {
               return ValidationResult.error("Groups exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < groupsCount; i++) {
               int keyLen = VarInt.peek(buffer, pos);
               if (keyLen < 0) {
                  return ValidationResult.error("Invalid string length for key");
               }

               if (keyLen > 4096000) {
                  return ValidationResult.error("key exceeds max length 4096000");
               }

               pos += VarInt.length(buffer, pos);
               pos += keyLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               pos += BlockGroup.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateBlockGroups clone() {
      UpdateBlockGroups copy = new UpdateBlockGroups();
      copy.type = this.type;
      if (this.groups != null) {
         Map<String, BlockGroup> m = new HashMap<>();

         for (Entry<String, BlockGroup> e : this.groups.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.groups = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateBlockGroups other) ? false : Objects.equals(this.type, other.type) && Objects.equals(this.groups, other.groups);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.groups);
   }
}
