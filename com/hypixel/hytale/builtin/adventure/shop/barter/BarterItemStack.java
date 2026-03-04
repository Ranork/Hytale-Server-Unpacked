package com.hypixel.hytale.builtin.adventure.shop.barter;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;

public class BarterItemStack {
   @Nonnull
   public static final BuilderCodec<BarterItemStack> CODEC = BuilderCodec.builder(BarterItemStack.class, BarterItemStack::new)
      .append(new KeyedCodec<>("ItemId", Codec.STRING), (stack, s) -> stack.itemId = s, stack -> stack.itemId)
      .addValidator(Validators.nonNull())
      .add()
      .<Integer>append(new KeyedCodec<>("Quantity", Codec.INTEGER), (stack, i) -> stack.quantity = i, stack -> stack.quantity)
      .addValidator(Validators.greaterThanOrEqual(1))
      .add()
      .build();
   protected String itemId;
   protected int quantity = 1;

   public BarterItemStack(String itemId, int quantity) {
      this.itemId = itemId;
      this.quantity = quantity;
   }

   protected BarterItemStack() {
   }

   public String getItemId() {
      return this.itemId;
   }

   public int getQuantity() {
      return this.quantity;
   }

   @Nonnull
   @Override
   public String toString() {
      return "BarterItemStack{itemId='" + this.itemId + "', quantity=" + this.quantity + "}";
   }
}
