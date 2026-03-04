package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;

public abstract class ParamValue {
   public static final int MAX_SIZE = 16384011;

   @Nonnull
   public static ParamValue deserialize(@Nonnull ByteBuf buf, int offset) {
      int typeId = VarInt.peek(buf, offset);
      int typeIdLen = VarInt.length(buf, offset);

      return (ParamValue)(switch (typeId) {
         case 0 -> StringParamValue.deserialize(buf, offset + typeIdLen);
         case 1 -> BoolParamValue.deserialize(buf, offset + typeIdLen);
         case 2 -> DoubleParamValue.deserialize(buf, offset + typeIdLen);
         case 3 -> IntParamValue.deserialize(buf, offset + typeIdLen);
         case 4 -> LongParamValue.deserialize(buf, offset + typeIdLen);
         default -> throw ProtocolException.unknownPolymorphicType("ParamValue", typeId);
      });
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int typeId = VarInt.peek(buf, offset);
      int typeIdLen = VarInt.length(buf, offset);

      return typeIdLen + switch (typeId) {
         case 0 -> StringParamValue.computeBytesConsumed(buf, offset + typeIdLen);
         case 1 -> BoolParamValue.computeBytesConsumed(buf, offset + typeIdLen);
         case 2 -> DoubleParamValue.computeBytesConsumed(buf, offset + typeIdLen);
         case 3 -> IntParamValue.computeBytesConsumed(buf, offset + typeIdLen);
         case 4 -> LongParamValue.computeBytesConsumed(buf, offset + typeIdLen);
         default -> throw ProtocolException.unknownPolymorphicType("ParamValue", typeId);
      };
   }

   public int getTypeId() {
      if (this instanceof StringParamValue sub) {
         return 0;
      } else if (this instanceof BoolParamValue sub) {
         return 1;
      } else if (this instanceof DoubleParamValue sub) {
         return 2;
      } else if (this instanceof IntParamValue sub) {
         return 3;
      } else if (this instanceof LongParamValue sub) {
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
         case 0 -> StringParamValue.validateStructure(buffer, offset + typeIdLen);
         case 1 -> BoolParamValue.validateStructure(buffer, offset + typeIdLen);
         case 2 -> DoubleParamValue.validateStructure(buffer, offset + typeIdLen);
         case 3 -> IntParamValue.validateStructure(buffer, offset + typeIdLen);
         case 4 -> LongParamValue.validateStructure(buffer, offset + typeIdLen);
         default -> ValidationResult.error("Unknown polymorphic type ID " + typeId + " for ParamValue");
      };
   }
}
