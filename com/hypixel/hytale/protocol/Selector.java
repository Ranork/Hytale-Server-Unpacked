package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;

public abstract class Selector {
   public static final int MAX_SIZE = 42;

   @Nonnull
   public static Selector deserialize(@Nonnull ByteBuf buf, int offset) {
      int typeId = VarInt.peek(buf, offset);
      int typeIdLen = VarInt.length(buf, offset);

      return (Selector)(switch (typeId) {
         case 0 -> AOECircleSelector.deserialize(buf, offset + typeIdLen);
         case 1 -> AOECylinderSelector.deserialize(buf, offset + typeIdLen);
         case 2 -> RaycastSelector.deserialize(buf, offset + typeIdLen);
         case 3 -> HorizontalSelector.deserialize(buf, offset + typeIdLen);
         case 4 -> StabSelector.deserialize(buf, offset + typeIdLen);
         default -> throw ProtocolException.unknownPolymorphicType("Selector", typeId);
      });
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int typeId = VarInt.peek(buf, offset);
      int typeIdLen = VarInt.length(buf, offset);

      return typeIdLen + switch (typeId) {
         case 0 -> AOECircleSelector.computeBytesConsumed(buf, offset + typeIdLen);
         case 1 -> AOECylinderSelector.computeBytesConsumed(buf, offset + typeIdLen);
         case 2 -> RaycastSelector.computeBytesConsumed(buf, offset + typeIdLen);
         case 3 -> HorizontalSelector.computeBytesConsumed(buf, offset + typeIdLen);
         case 4 -> StabSelector.computeBytesConsumed(buf, offset + typeIdLen);
         default -> throw ProtocolException.unknownPolymorphicType("Selector", typeId);
      };
   }

   public int getTypeId() {
      if (this instanceof AOECircleSelector sub) {
         return 0;
      } else if (this instanceof AOECylinderSelector sub) {
         return 1;
      } else if (this instanceof RaycastSelector sub) {
         return 2;
      } else if (this instanceof HorizontalSelector sub) {
         return 3;
      } else if (this instanceof StabSelector sub) {
         return 4;
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
         case 0 -> AOECircleSelector.validateStructure(buffer, offset + typeIdLen);
         case 1 -> AOECylinderSelector.validateStructure(buffer, offset + typeIdLen);
         case 2 -> RaycastSelector.validateStructure(buffer, offset + typeIdLen);
         case 3 -> HorizontalSelector.validateStructure(buffer, offset + typeIdLen);
         case 4 -> StabSelector.validateStructure(buffer, offset + typeIdLen);
         default -> ValidationResult.error("Unknown polymorphic type ID " + typeId + " for Selector");
      };
   }
}
