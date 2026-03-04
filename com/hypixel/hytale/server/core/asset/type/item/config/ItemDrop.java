package com.hypixel.hytale.server.core.asset.type.item.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.codec.validation.validator.RangeRefValidator;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;

public class ItemDrop {
   public static final BuilderCodec<ItemDrop> CODEC = BuilderCodec.builder(ItemDrop.class, ItemDrop::new)
      .append(new KeyedCodec<>("ItemId", Codec.STRING), (itemDrop, s) -> itemDrop.itemId = s, itemDrop -> itemDrop.itemId)
      .addValidatorLate(() -> Item.VALIDATOR_CACHE.getValidator().late())
      .add()
      .addField(new KeyedCodec<>("Metadata", Codec.BSON_DOCUMENT), (itemDrop, document) -> itemDrop.metadata = document, itemDrop -> itemDrop.metadata)
      .<Integer>append(new KeyedCodec<>("QuantityMin", Codec.INTEGER), (itemDrop, i) -> itemDrop.quantityMin = i, itemDrop -> itemDrop.quantityMin)
      .addValidator(new RangeRefValidator<>(null, "1/QuantityMax", true))
      .add()
      .<Integer>append(new KeyedCodec<>("QuantityMax", Codec.INTEGER), (itemDrop, i) -> itemDrop.quantityMax = i, itemDrop -> itemDrop.quantityMax)
      .addValidator(Validators.greaterThan(0))
      .add()
      .build();
   protected String itemId;
   protected BsonDocument metadata;
   protected int quantityMin = 1;
   protected int quantityMax = 1;

   public ItemDrop(String itemId, BsonDocument metadata, int quantityMin, int quantityMax) {
      this.itemId = itemId;
      this.metadata = metadata;
      this.quantityMin = quantityMin;
      this.quantityMax = quantityMax;
   }

   protected ItemDrop() {
   }

   public String getItemId() {
      return this.itemId;
   }

   @Nullable
   public BsonDocument getMetadata() {
      return this.metadata == null ? null : this.metadata.clone();
   }

   public int getQuantityMin() {
      return this.quantityMin;
   }

   public int getQuantityMax() {
      return this.quantityMax;
   }

   public int getRandomQuantity(@Nonnull Random random) {
      return random.nextInt(Math.max(this.quantityMax - this.quantityMin + 1, 1)) + this.quantityMin;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ItemDrop{itemId='"
         + this.itemId
         + "', metadata="
         + this.metadata
         + ", quantityMin="
         + this.quantityMin
         + ", quantityMax="
         + this.quantityMax
         + "}";
   }
}
