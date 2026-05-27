package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import javax.annotation.Nonnull;

public abstract class UIDataValue {
   public static final int MAX_SIZE = 1677721605;

   @Nonnull
   public static UIDataValue deserialize(@Nonnull ByteBuf buf, int offset) {
      int typeId = VarInt.peek(buf, offset);
      int typeIdLen = VarInt.size(typeId);

      return (UIDataValue)(switch (typeId) {
         case 0 -> UIStringDataValue.deserialize(buf, offset + typeIdLen);
         case 1 -> UIFloatDataValue.deserialize(buf, offset + typeIdLen);
         case 2 -> UIIntDataValue.deserialize(buf, offset + typeIdLen);
         case 3 -> UIBoolDataValue.deserialize(buf, offset + typeIdLen);
         case 4 -> UICommandDataValue.deserialize(buf, offset + typeIdLen);
         case 5 -> UIObjectDataValue.deserialize(buf, offset + typeIdLen);
         case 6 -> UIEnumDataValue.deserialize(buf, offset + typeIdLen);
         case 7 -> UIListDataValue.deserialize(buf, offset + typeIdLen);
         case 8 -> UIByteDataValue.deserialize(buf, offset + typeIdLen);
         case 9 -> UISByteDataValue.deserialize(buf, offset + typeIdLen);
         case 10 -> UIShortDataValue.deserialize(buf, offset + typeIdLen);
         case 11 -> UIUShortDataValue.deserialize(buf, offset + typeIdLen);
         case 12 -> UIUIntDataValue.deserialize(buf, offset + typeIdLen);
         case 13 -> UILongDataValue.deserialize(buf, offset + typeIdLen);
         case 14 -> UIULongDataValue.deserialize(buf, offset + typeIdLen);
         case 15 -> UIDoubleDataValue.deserialize(buf, offset + typeIdLen);
         default -> throw ProtocolException.unknownPolymorphicType("UIDataValue", typeId);
      });
   }

   public static UIDataValue toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UIDataValue toObject(MemorySegment mem, int offset) {
      int typeId = VarInt.get(mem, offset);
      int typeIdLen = VarInt.size(typeId);

      return (UIDataValue)(switch (typeId) {
         case 0 -> UIStringDataValue.toObject(mem, offset + typeIdLen);
         case 1 -> UIFloatDataValue.toObject(mem, offset + typeIdLen);
         case 2 -> UIIntDataValue.toObject(mem, offset + typeIdLen);
         case 3 -> UIBoolDataValue.toObject(mem, offset + typeIdLen);
         case 4 -> UICommandDataValue.toObject(mem, offset + typeIdLen);
         case 5 -> UIObjectDataValue.toObject(mem, offset + typeIdLen);
         case 6 -> UIEnumDataValue.toObject(mem, offset + typeIdLen);
         case 7 -> UIListDataValue.toObject(mem, offset + typeIdLen);
         case 8 -> UIByteDataValue.toObject(mem, offset + typeIdLen);
         case 9 -> UISByteDataValue.toObject(mem, offset + typeIdLen);
         case 10 -> UIShortDataValue.toObject(mem, offset + typeIdLen);
         case 11 -> UIUShortDataValue.toObject(mem, offset + typeIdLen);
         case 12 -> UIUIntDataValue.toObject(mem, offset + typeIdLen);
         case 13 -> UILongDataValue.toObject(mem, offset + typeIdLen);
         case 14 -> UIULongDataValue.toObject(mem, offset + typeIdLen);
         case 15 -> UIDoubleDataValue.toObject(mem, offset + typeIdLen);
         default -> throw ProtocolException.unknownPolymorphicType("UIDataValue", typeId);
      });
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int typeId = VarInt.peek(buf, offset);
      int typeIdLen = VarInt.size(typeId);

      return typeIdLen + switch (typeId) {
         case 0 -> UIStringDataValue.computeBytesConsumed(buf, offset + typeIdLen);
         case 1 -> UIFloatDataValue.computeBytesConsumed(buf, offset + typeIdLen);
         case 2 -> UIIntDataValue.computeBytesConsumed(buf, offset + typeIdLen);
         case 3 -> UIBoolDataValue.computeBytesConsumed(buf, offset + typeIdLen);
         case 4 -> UICommandDataValue.computeBytesConsumed(buf, offset + typeIdLen);
         case 5 -> UIObjectDataValue.computeBytesConsumed(buf, offset + typeIdLen);
         case 6 -> UIEnumDataValue.computeBytesConsumed(buf, offset + typeIdLen);
         case 7 -> UIListDataValue.computeBytesConsumed(buf, offset + typeIdLen);
         case 8 -> UIByteDataValue.computeBytesConsumed(buf, offset + typeIdLen);
         case 9 -> UISByteDataValue.computeBytesConsumed(buf, offset + typeIdLen);
         case 10 -> UIShortDataValue.computeBytesConsumed(buf, offset + typeIdLen);
         case 11 -> UIUShortDataValue.computeBytesConsumed(buf, offset + typeIdLen);
         case 12 -> UIUIntDataValue.computeBytesConsumed(buf, offset + typeIdLen);
         case 13 -> UILongDataValue.computeBytesConsumed(buf, offset + typeIdLen);
         case 14 -> UIULongDataValue.computeBytesConsumed(buf, offset + typeIdLen);
         case 15 -> UIDoubleDataValue.computeBytesConsumed(buf, offset + typeIdLen);
         default -> throw ProtocolException.unknownPolymorphicType("UIDataValue", typeId);
      };
   }

   public int getTypeId() {
      if (this instanceof UIStringDataValue sub) {
         return 0;
      } else if (this instanceof UIFloatDataValue sub) {
         return 1;
      } else if (this instanceof UIIntDataValue sub) {
         return 2;
      } else if (this instanceof UIBoolDataValue sub) {
         return 3;
      } else if (this instanceof UICommandDataValue sub) {
         return 4;
      } else if (this instanceof UIObjectDataValue sub) {
         return 5;
      } else if (this instanceof UIEnumDataValue sub) {
         return 6;
      } else if (this instanceof UIListDataValue sub) {
         return 7;
      } else if (this instanceof UIByteDataValue sub) {
         return 8;
      } else if (this instanceof UISByteDataValue sub) {
         return 9;
      } else if (this instanceof UIShortDataValue sub) {
         return 10;
      } else if (this instanceof UIUShortDataValue sub) {
         return 11;
      } else if (this instanceof UIUIntDataValue sub) {
         return 12;
      } else if (this instanceof UILongDataValue sub) {
         return 13;
      } else if (this instanceof UIULongDataValue sub) {
         return 14;
      } else if (this instanceof UIDoubleDataValue sub) {
         return 15;
      } else {
         throw new IllegalStateException("Unknown subtype: " + this.getClass().getName());
      }
   }

   public abstract int serialize(@Nonnull ByteBuf var1);

   public abstract int serialize(@Nonnull MemorySegment var1, int var2);

   public abstract int computeSize();

   public int serializeWithTypeId(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      VarInt.write(buf, this.getTypeId());
      this.serialize(buf);
      return buf.writerIndex() - startPos;
   }

   public int serializeWithTypeId(@Nonnull MemorySegment mem, int offset) {
      int len = VarInt.set(mem, offset, this.getTypeId());
      return len + this.serialize(mem, offset + len);
   }

   public int computeSizeWithTypeId() {
      return VarInt.size(this.getTypeId()) + this.computeSize();
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      int typeId = VarInt.peek(buffer, offset);
      int typeIdLen = VarInt.size(typeId);

      return switch (typeId) {
         case 0 -> UIStringDataValue.validateStructure(buffer, offset + typeIdLen);
         case 1 -> UIFloatDataValue.validateStructure(buffer, offset + typeIdLen);
         case 2 -> UIIntDataValue.validateStructure(buffer, offset + typeIdLen);
         case 3 -> UIBoolDataValue.validateStructure(buffer, offset + typeIdLen);
         case 4 -> UICommandDataValue.validateStructure(buffer, offset + typeIdLen);
         case 5 -> UIObjectDataValue.validateStructure(buffer, offset + typeIdLen);
         case 6 -> UIEnumDataValue.validateStructure(buffer, offset + typeIdLen);
         case 7 -> UIListDataValue.validateStructure(buffer, offset + typeIdLen);
         case 8 -> UIByteDataValue.validateStructure(buffer, offset + typeIdLen);
         case 9 -> UISByteDataValue.validateStructure(buffer, offset + typeIdLen);
         case 10 -> UIShortDataValue.validateStructure(buffer, offset + typeIdLen);
         case 11 -> UIUShortDataValue.validateStructure(buffer, offset + typeIdLen);
         case 12 -> UIUIntDataValue.validateStructure(buffer, offset + typeIdLen);
         case 13 -> UILongDataValue.validateStructure(buffer, offset + typeIdLen);
         case 14 -> UIULongDataValue.validateStructure(buffer, offset + typeIdLen);
         case 15 -> UIDoubleDataValue.validateStructure(buffer, offset + typeIdLen);
         default -> ValidationResult.error("Unknown polymorphic type ID " + typeId + " for UIDataValue");
      };
   }
}
