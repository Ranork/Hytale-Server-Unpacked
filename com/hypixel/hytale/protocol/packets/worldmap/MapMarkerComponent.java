package com.hypixel.hytale.protocol.packets.worldmap;

import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;

public abstract class MapMarkerComponent {
   public static final int MAX_SIZE = 1677721605;

   @Nonnull
   public static MapMarkerComponent deserialize(@Nonnull ByteBuf buf, int offset) {
      int typeId = VarInt.peek(buf, offset);
      int typeIdLen = VarInt.length(buf, offset);

      return (MapMarkerComponent)(switch (typeId) {
         case 0 -> PlayerMarkerComponent.deserialize(buf, offset + typeIdLen);
         case 1 -> PlacedByMarkerComponent.deserialize(buf, offset + typeIdLen);
         case 2 -> HeightDeltaIconComponent.deserialize(buf, offset + typeIdLen);
         case 3 -> TintComponent.deserialize(buf, offset + typeIdLen);
         default -> throw ProtocolException.unknownPolymorphicType("MapMarkerComponent", typeId);
      });
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int typeId = VarInt.peek(buf, offset);
      int typeIdLen = VarInt.length(buf, offset);

      return typeIdLen + switch (typeId) {
         case 0 -> PlayerMarkerComponent.computeBytesConsumed(buf, offset + typeIdLen);
         case 1 -> PlacedByMarkerComponent.computeBytesConsumed(buf, offset + typeIdLen);
         case 2 -> HeightDeltaIconComponent.computeBytesConsumed(buf, offset + typeIdLen);
         case 3 -> TintComponent.computeBytesConsumed(buf, offset + typeIdLen);
         default -> throw ProtocolException.unknownPolymorphicType("MapMarkerComponent", typeId);
      };
   }

   public int getTypeId() {
      if (this instanceof PlayerMarkerComponent sub) {
         return 0;
      } else if (this instanceof PlacedByMarkerComponent sub) {
         return 1;
      } else if (this instanceof HeightDeltaIconComponent sub) {
         return 2;
      } else if (this instanceof TintComponent sub) {
         return 3;
      } else {
         throw new IllegalStateException("Unknown subtype: " + this.getClass().getName());
      }
   }

   public abstract int serialize(@Nonnull ByteBuf var1);

   public abstract int computeSize();

   public int serializeWithTypeId(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      VarInt.write(buf, this.getTypeId());
      this.serialize(buf);
      return buf.writerIndex() - startPos;
   }

   public int computeSizeWithTypeId() {
      return VarInt.size(this.getTypeId()) + this.computeSize();
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      int typeId = VarInt.peek(buffer, offset);
      int typeIdLen = VarInt.length(buffer, offset);

      return switch (typeId) {
         case 0 -> PlayerMarkerComponent.validateStructure(buffer, offset + typeIdLen);
         case 1 -> PlacedByMarkerComponent.validateStructure(buffer, offset + typeIdLen);
         case 2 -> HeightDeltaIconComponent.validateStructure(buffer, offset + typeIdLen);
         case 3 -> TintComponent.validateStructure(buffer, offset + typeIdLen);
         default -> ValidationResult.error("Unknown polymorphic type ID " + typeId + " for MapMarkerComponent");
      };
   }
}
