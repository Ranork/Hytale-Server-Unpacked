package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.BlockSoundSet;
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

public class UpdateBlockSoundSets implements Packet, ToClientPacket {
   public static final int PACKET_ID = 42;
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
   public Map<Integer, BlockSoundSet> blockSoundSets;

   @Override
   public int getId() {
      return 42;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateBlockSoundSets() {
   }

   public UpdateBlockSoundSets(@Nonnull UpdateType type, int maxId, @Nullable Map<Integer, BlockSoundSet> blockSoundSets) {
      this.type = type;
      this.maxId = maxId;
      this.blockSoundSets = blockSoundSets;
   }

   public UpdateBlockSoundSets(@Nonnull UpdateBlockSoundSets other) {
      this.type = other.type;
      this.maxId = other.maxId;
      this.blockSoundSets = other.blockSoundSets;
   }

   @Nonnull
   public static UpdateBlockSoundSets deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateBlockSoundSets obj = new UpdateBlockSoundSets();
      byte nullBits = buf.getByte(offset);
      obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
      obj.maxId = buf.getIntLE(offset + 2);
      int pos = offset + 6;
      if ((nullBits & 1) != 0) {
         int blockSoundSetsCount = VarInt.peek(buf, pos);
         if (blockSoundSetsCount < 0) {
            throw ProtocolException.negativeLength("BlockSoundSets", blockSoundSetsCount);
         }

         if (blockSoundSetsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("BlockSoundSets", blockSoundSetsCount, 4096000);
         }

         pos += VarInt.size(blockSoundSetsCount);
         obj.blockSoundSets = new HashMap<>(blockSoundSetsCount);

         for (int i = 0; i < blockSoundSetsCount; i++) {
            int key = buf.getIntLE(pos);
            pos += 4;
            BlockSoundSet val = BlockSoundSet.deserialize(buf, pos);
            pos += BlockSoundSet.computeBytesConsumed(buf, pos);
            if (obj.blockSoundSets.put(key, val) != null) {
               throw ProtocolException.duplicateKey("blockSoundSets", key);
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
            pos += BlockSoundSet.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.blockSoundSets != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.maxId);
      if (this.blockSoundSets != null) {
         if (this.blockSoundSets.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("BlockSoundSets", this.blockSoundSets.size(), 4096000);
         }

         VarInt.write(buf, this.blockSoundSets.size());

         for (Entry<Integer, BlockSoundSet> e : this.blockSoundSets.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int computeSize() {
      int size = 6;
      if (this.blockSoundSets != null) {
         int blockSoundSetsSize = 0;

         for (Entry<Integer, BlockSoundSet> kvp : this.blockSoundSets.entrySet()) {
            blockSoundSetsSize += 4 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.blockSoundSets.size()) + blockSoundSetsSize;
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
            int blockSoundSetsCount = VarInt.peek(buffer, pos);
            if (blockSoundSetsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for BlockSoundSets");
            }

            if (blockSoundSetsCount > 4096000) {
               return ValidationResult.error("BlockSoundSets exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < blockSoundSetsCount; i++) {
               pos += 4;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               pos += BlockSoundSet.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateBlockSoundSets clone() {
      UpdateBlockSoundSets copy = new UpdateBlockSoundSets();
      copy.type = this.type;
      copy.maxId = this.maxId;
      if (this.blockSoundSets != null) {
         Map<Integer, BlockSoundSet> m = new HashMap<>();

         for (Entry<Integer, BlockSoundSet> e : this.blockSoundSets.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.blockSoundSets = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateBlockSoundSets other)
            ? false
            : Objects.equals(this.type, other.type) && this.maxId == other.maxId && Objects.equals(this.blockSoundSets, other.blockSoundSets);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.maxId, this.blockSoundSets);
   }
}
