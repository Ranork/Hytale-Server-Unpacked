package com.hypixel.hytale.protocol.packets.asseteditor;

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

public class AssetEditorSetupAssetTypes implements Packet, ToClientPacket {
   public static final int PACKET_ID = 306;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public AssetEditorAssetType[] assetTypes;

   @Override
   public int getId() {
      return 306;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorSetupAssetTypes() {
   }

   public AssetEditorSetupAssetTypes(@Nullable AssetEditorAssetType[] assetTypes) {
      this.assetTypes = assetTypes;
   }

   public AssetEditorSetupAssetTypes(@Nonnull AssetEditorSetupAssetTypes other) {
      this.assetTypes = other.assetTypes;
   }

   @Nonnull
   public static AssetEditorSetupAssetTypes deserialize(@Nonnull ByteBuf buf, int offset) {
      AssetEditorSetupAssetTypes obj = new AssetEditorSetupAssetTypes();
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int assetTypesCount = VarInt.peek(buf, pos);
         if (assetTypesCount < 0) {
            throw ProtocolException.negativeLength("AssetTypes", assetTypesCount);
         }

         if (assetTypesCount > 4096000) {
            throw ProtocolException.arrayTooLong("AssetTypes", assetTypesCount, 4096000);
         }

         int assetTypesVarLen = VarInt.size(assetTypesCount);
         if (pos + assetTypesVarLen + assetTypesCount * 3L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("AssetTypes", pos + assetTypesVarLen + assetTypesCount * 3, buf.readableBytes());
         }

         pos += assetTypesVarLen;
         obj.assetTypes = new AssetEditorAssetType[assetTypesCount];

         for (int i = 0; i < assetTypesCount; i++) {
            obj.assetTypes[i] = AssetEditorAssetType.deserialize(buf, pos);
            pos += AssetEditorAssetType.computeBytesConsumed(buf, pos);
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
            pos += AssetEditorAssetType.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.assetTypes != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.assetTypes != null) {
         if (this.assetTypes.length > 4096000) {
            throw ProtocolException.arrayTooLong("AssetTypes", this.assetTypes.length, 4096000);
         }

         VarInt.write(buf, this.assetTypes.length);

         for (AssetEditorAssetType item : this.assetTypes) {
            item.serialize(buf);
         }
      }
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.assetTypes != null) {
         int assetTypesSize = 0;

         for (AssetEditorAssetType elem : this.assetTypes) {
            assetTypesSize += elem.computeSize();
         }

         size += VarInt.size(this.assetTypes.length) + assetTypesSize;
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
            int assetTypesCount = VarInt.peek(buffer, pos);
            if (assetTypesCount < 0) {
               return ValidationResult.error("Invalid array count for AssetTypes");
            }

            if (assetTypesCount > 4096000) {
               return ValidationResult.error("AssetTypes exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < assetTypesCount; i++) {
               ValidationResult structResult = AssetEditorAssetType.validateStructure(buffer, pos);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid AssetEditorAssetType in AssetTypes[" + i + "]: " + structResult.error());
               }

               pos += AssetEditorAssetType.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorSetupAssetTypes clone() {
      AssetEditorSetupAssetTypes copy = new AssetEditorSetupAssetTypes();
      copy.assetTypes = this.assetTypes != null ? Arrays.stream(this.assetTypes).map(e -> e.clone()).toArray(AssetEditorAssetType[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof AssetEditorSetupAssetTypes other ? Arrays.equals((Object[])this.assetTypes, (Object[])other.assetTypes) : false;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + Arrays.hashCode((Object[])this.assetTypes);
   }
}
