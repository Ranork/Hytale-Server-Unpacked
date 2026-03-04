package com.hypixel.hytale.protocol.packets.window;

import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;

public abstract class WindowAction {
   public static final int MAX_SIZE = 32768023;

   @Nonnull
   public static WindowAction deserialize(@Nonnull ByteBuf buf, int offset) {
      int typeId = VarInt.peek(buf, offset);
      int typeIdLen = VarInt.length(buf, offset);

      return (WindowAction)(switch (typeId) {
         case 0 -> CraftRecipeAction.deserialize(buf, offset + typeIdLen);
         case 1 -> TierUpgradeAction.deserialize(buf, offset + typeIdLen);
         case 2 -> SelectSlotAction.deserialize(buf, offset + typeIdLen);
         case 3 -> ChangeBlockAction.deserialize(buf, offset + typeIdLen);
         case 4 -> SetActiveAction.deserialize(buf, offset + typeIdLen);
         case 5 -> CraftItemAction.deserialize(buf, offset + typeIdLen);
         case 6 -> UpdateCategoryAction.deserialize(buf, offset + typeIdLen);
         case 7 -> CancelCraftingAction.deserialize(buf, offset + typeIdLen);
         case 8 -> SortItemsAction.deserialize(buf, offset + typeIdLen);
         default -> throw ProtocolException.unknownPolymorphicType("WindowAction", typeId);
      });
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int typeId = VarInt.peek(buf, offset);
      int typeIdLen = VarInt.length(buf, offset);

      return typeIdLen + switch (typeId) {
         case 0 -> CraftRecipeAction.computeBytesConsumed(buf, offset + typeIdLen);
         case 1 -> TierUpgradeAction.computeBytesConsumed(buf, offset + typeIdLen);
         case 2 -> SelectSlotAction.computeBytesConsumed(buf, offset + typeIdLen);
         case 3 -> ChangeBlockAction.computeBytesConsumed(buf, offset + typeIdLen);
         case 4 -> SetActiveAction.computeBytesConsumed(buf, offset + typeIdLen);
         case 5 -> CraftItemAction.computeBytesConsumed(buf, offset + typeIdLen);
         case 6 -> UpdateCategoryAction.computeBytesConsumed(buf, offset + typeIdLen);
         case 7 -> CancelCraftingAction.computeBytesConsumed(buf, offset + typeIdLen);
         case 8 -> SortItemsAction.computeBytesConsumed(buf, offset + typeIdLen);
         default -> throw ProtocolException.unknownPolymorphicType("WindowAction", typeId);
      };
   }

   public int getTypeId() {
      if (this instanceof CraftRecipeAction sub) {
         return 0;
      } else if (this instanceof TierUpgradeAction sub) {
         return 1;
      } else if (this instanceof SelectSlotAction sub) {
         return 2;
      } else if (this instanceof ChangeBlockAction sub) {
         return 3;
      } else if (this instanceof SetActiveAction sub) {
         return 4;
      } else if (this instanceof CraftItemAction sub) {
         return 5;
      } else if (this instanceof UpdateCategoryAction sub) {
         return 6;
      } else if (this instanceof CancelCraftingAction sub) {
         return 7;
      } else if (this instanceof SortItemsAction sub) {
         return 8;
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
         case 0 -> CraftRecipeAction.validateStructure(buffer, offset + typeIdLen);
         case 1 -> TierUpgradeAction.validateStructure(buffer, offset + typeIdLen);
         case 2 -> SelectSlotAction.validateStructure(buffer, offset + typeIdLen);
         case 3 -> ChangeBlockAction.validateStructure(buffer, offset + typeIdLen);
         case 4 -> SetActiveAction.validateStructure(buffer, offset + typeIdLen);
         case 5 -> CraftItemAction.validateStructure(buffer, offset + typeIdLen);
         case 6 -> UpdateCategoryAction.validateStructure(buffer, offset + typeIdLen);
         case 7 -> CancelCraftingAction.validateStructure(buffer, offset + typeIdLen);
         case 8 -> SortItemsAction.validateStructure(buffer, offset + typeIdLen);
         default -> ValidationResult.error("Unknown polymorphic type ID " + typeId + " for WindowAction");
      };
   }
}
