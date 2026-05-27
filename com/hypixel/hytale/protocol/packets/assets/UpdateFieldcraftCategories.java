package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.ItemCategory;
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
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdateFieldcraftCategories implements Packet, ToClientPacket {
   public static final int PACKET_ID = 58;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 2;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public UpdateType type = UpdateType.Init;
   @Nullable
   public ItemCategory[] itemCategories;

   @Override
   public int getId() {
      return 58;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateFieldcraftCategories() {
   }

   public UpdateFieldcraftCategories(@Nonnull UpdateType type, @Nullable ItemCategory[] itemCategories) {
      this.type = type;
      this.itemCategories = itemCategories;
   }

   public UpdateFieldcraftCategories(@Nonnull UpdateFieldcraftCategories other) {
      this.type = other.type;
      this.itemCategories = other.itemCategories;
   }

   @Nonnull
   public static UpdateFieldcraftCategories deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 2) {
         throw ProtocolException.bufferTooSmall("UpdateFieldcraftCategories", 2, buf.readableBytes() - offset);
      } else {
         UpdateFieldcraftCategories obj = new UpdateFieldcraftCategories();
         byte nullBits = buf.getByte(offset);
         obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
         int pos = offset + 2;
         if ((nullBits & 1) != 0) {
            int itemCategoriesCount = VarInt.peek(buf, pos);
            if (itemCategoriesCount < 0) {
               throw ProtocolException.invalidVarInt("ItemCategories");
            }

            int itemCategoriesVarLen = VarInt.size(itemCategoriesCount);
            if (itemCategoriesCount > 4096000) {
               throw ProtocolException.arrayTooLong("ItemCategories", itemCategoriesCount, 4096000);
            }

            if (pos + itemCategoriesVarLen + itemCategoriesCount * 6L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ItemCategories", pos + itemCategoriesVarLen + itemCategoriesCount * 6, buf.readableBytes());
            }

            pos += itemCategoriesVarLen;
            obj.itemCategories = new ItemCategory[itemCategoriesCount];

            for (int i = 0; i < itemCategoriesCount; i++) {
               obj.itemCategories[i] = ItemCategory.deserialize(buf, pos);
               pos += ItemCategory.computeBytesConsumed(buf, pos);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos += ItemCategory.computeBytesConsumed(buf, pos);
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
   public static ItemCategory[] getItemCategories(MemorySegment mem) {
      return getItemCategories(mem, 0);
   }

   @Nullable
   public static ItemCategory[] getItemCategories(MemorySegment mem, int offset) {
      if (!hasItemCategories(mem, offset)) {
         return null;
      } else {
         int off = offset + 2;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("ItemCategories", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("ItemCategories", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("ItemCategories", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               ItemCategory[] data = new ItemCategory[len];

               for (int i = 0; i < len; i++) {
                  data[i] = ItemCategory.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   public static boolean hasItemCategories(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static UpdateFieldcraftCategories toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateFieldcraftCategories toObject(MemorySegment mem, int offset) {
      if (offset + 2 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateFieldcraftCategories", offset + 2, (int)mem.byteSize());
      } else {
         ItemCategory[] itemCategories = null;
         if (hasItemCategories(mem, offset)) {
            int off = offset + 2;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("ItemCategories", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("ItemCategories", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("ItemCategories", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            itemCategories = new ItemCategory[len];

            for (int i = 0; i < len; i++) {
               itemCategories[i] = ItemCategory.toObject(mem, off);
               off += itemCategories[i].computeSize();
            }
         }

         return new UpdateFieldcraftCategories(UpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))), itemCategories);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.itemCategories != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      if (this.itemCategories != null) {
         if (this.itemCategories.length > 4096000) {
            throw ProtocolException.arrayTooLong("ItemCategories", this.itemCategories.length, 4096000);
         }

         VarInt.write(buf, this.itemCategories.length);

         for (ItemCategory item : this.itemCategories) {
            item.serialize(buf);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.itemCategories != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      int varOffset = offset + 2;
      if (this.itemCategories != null) {
         if (this.itemCategories.length > 4096000) {
            throw ProtocolException.arrayTooLong("ItemCategories", this.itemCategories.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.itemCategories.length);
         int itemCategoriesValueOffset = 0;

         for (int i = 0; i < this.itemCategories.length; i++) {
            itemCategoriesValueOffset += this.itemCategories[i].serialize(mem, varOffset + itemCategoriesValueOffset);
         }

         varOffset += itemCategoriesValueOffset;
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 2;
      if (this.itemCategories != null) {
         int itemCategoriesSize = 0;

         for (ItemCategory elem : this.itemCategories) {
            itemCategoriesSize += elem.computeSize();
         }

         size += VarInt.size(this.itemCategories.length) + itemCategoriesSize;
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
               int itemCategoriesCount = VarInt.peek(buffer, v);
               if (itemCategoriesCount < 0) {
                  return ValidationResult.error("Invalid array count for ItemCategories");
               }

               if (itemCategoriesCount > 4096000) {
                  return ValidationResult.error("ItemCategories exceeds max length 4096000");
               }

               v += VarInt.size(itemCategoriesCount);

               for (int i = 0; i < itemCategoriesCount; i++) {
                  ValidationResult structResult = ItemCategory.validateStructure(buffer, v);
                  if (!structResult.isValid()) {
                     return ValidationResult.error("Invalid ItemCategory in ItemCategories[" + i + "]: " + structResult.error());
                  }

                  v += ItemCategory.computeBytesConsumed(buffer, v);
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public UpdateFieldcraftCategories clone() {
      UpdateFieldcraftCategories copy = new UpdateFieldcraftCategories();
      copy.type = this.type;
      copy.itemCategories = this.itemCategories != null ? Arrays.stream(this.itemCategories).map(e -> e.clone()).toArray(ItemCategory[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateFieldcraftCategories other)
            ? false
            : Objects.equals(this.type, other.type) && Arrays.equals((Object[])this.itemCategories, (Object[])other.itemCategories);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.type);
      return 31 * result + Arrays.hashCode((Object[])this.itemCategories);
   }
}
