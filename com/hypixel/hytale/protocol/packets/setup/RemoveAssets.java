package com.hypixel.hytale.protocol.packets.setup;

import com.hypixel.hytale.protocol.Asset;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RemoveAssets implements Packet, ToClientPacket {
   public static final int PACKET_ID = 27;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public Asset[] asset;

   @Override
   public int getId() {
      return 27;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public RemoveAssets() {
   }

   public RemoveAssets(@Nullable Asset[] asset) {
      this.asset = asset;
   }

   public RemoveAssets(@Nonnull RemoveAssets other) {
      this.asset = other.asset;
   }

   @Nonnull
   public static RemoveAssets deserialize(@Nonnull ByteBuf buf, int offset) {
      RemoveAssets obj = new RemoveAssets();
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int assetCount = VarInt.peek(buf, pos);
         if (assetCount < 0) {
            throw ProtocolException.negativeLength("Asset", assetCount);
         }

         if (assetCount > 4096000) {
            throw ProtocolException.arrayTooLong("Asset", assetCount, 4096000);
         }

         int assetVarLen = VarInt.size(assetCount);
         if (pos + assetVarLen + assetCount * 64L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Asset", pos + assetVarLen + assetCount * 64, buf.readableBytes());
         }

         pos += assetVarLen;
         obj.asset = new Asset[assetCount];

         for (int i = 0; i < assetCount; i++) {
            obj.asset[i] = Asset.deserialize(buf, pos);
            pos += Asset.computeBytesConsumed(buf, pos);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos);

         for (int i = 0; i < arrLen; i++) {
            pos += Asset.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.asset != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.asset != null) {
         if (this.asset.length > 4096000) {
            throw ProtocolException.arrayTooLong("Asset", this.asset.length, 4096000);
         }

         VarInt.write(buf, this.asset.length);

         for (Asset item : this.asset) {
            item.serialize(buf);
         }
      }
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.asset != null) {
         int assetSize = 0;

         for (Asset elem : this.asset) {
            assetSize += elem.computeSize();
         }

         size += VarInt.size(this.asset.length) + assetSize;
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
            int assetCount = VarInt.peek(buffer, pos);
            if (assetCount < 0) {
               return ValidationResult.error("Invalid array count for Asset");
            }

            if (assetCount > 4096000) {
               return ValidationResult.error("Asset exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < assetCount; i++) {
               ValidationResult structResult = Asset.validateStructure(buffer, pos);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid Asset in Asset[" + i + "]: " + structResult.error());
               }

               pos += Asset.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public RemoveAssets clone() {
      RemoveAssets copy = new RemoveAssets();
      copy.asset = this.asset != null ? Arrays.stream(this.asset).map(e -> e.clone()).toArray(Asset[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof RemoveAssets other ? Arrays.equals((Object[])this.asset, (Object[])other.asset) : false;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + Arrays.hashCode((Object[])this.asset);
   }
}
