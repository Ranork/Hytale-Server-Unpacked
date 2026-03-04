package com.hypixel.hytale.server.core.inventory;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.protocol.ItemQuantity;
import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;
import org.bson.BsonValue;

public class ItemStack implements NetworkSerializable<ItemWithAllMetadata> {
   @Nonnull
   public static final ItemStack[] EMPTY_ARRAY = new ItemStack[0];
   @Nonnull
   public static final BuilderCodec<ItemStack> CODEC = BuilderCodec.builder(ItemStack.class, ItemStack::new)
      .append(new KeyedCodec<>("Id", Codec.STRING), (itemStack, id) -> itemStack.itemId = id, itemStack -> itemStack.itemId)
      .addValidator(Validators.nonNull())
      .addValidator(Item.VALIDATOR_CACHE.getValidator().late())
      .add()
      .<Integer>append(new KeyedCodec<>("Quantity", Codec.INTEGER), (itemStack, quantity) -> itemStack.quantity = quantity, itemStack -> itemStack.quantity)
      .addValidator(Validators.greaterThan(0))
      .add()
      .<Double>append(
         new KeyedCodec<>("Durability", Codec.DOUBLE), (itemStack, durability) -> itemStack.durability = durability, itemStack -> itemStack.durability
      )
      .addValidator(Validators.greaterThanOrEqual(0.0))
      .add()
      .<Double>append(
         new KeyedCodec<>("MaxDurability", Codec.DOUBLE),
         (itemStack, maxDurability) -> itemStack.maxDurability = maxDurability,
         itemStack -> itemStack.maxDurability
      )
      .addValidator(Validators.greaterThanOrEqual(0.0))
      .add()
      .append(
         new KeyedCodec<>("Metadata", Codec.BSON_DOCUMENT), (itemStack, bsonDocument) -> itemStack.metadata = bsonDocument, itemStack -> itemStack.metadata
      )
      .add()
      .append(
         new KeyedCodec<>("OverrideDroppedItemAnimation", Codec.BOOLEAN),
         (itemStack, b) -> itemStack.overrideDroppedItemAnimation = b,
         itemStack -> itemStack.overrideDroppedItemAnimation
      )
      .add()
      .build();
   @Nonnull
   public static final ItemStack EMPTY = new ItemStack() {
      {
         this.itemId = "Empty";
      }
   };
   protected String itemId;
   protected int quantity = 1;
   protected double durability;
   protected double maxDurability;
   protected boolean overrideDroppedItemAnimation;
   @Nullable
   protected BsonDocument metadata;
   @Nullable
   private ItemWithAllMetadata cachedPacket;

   public ItemStack(@Nonnull String itemId, int quantity, @Nullable BsonDocument metadata) {
      if (quantity <= 0) {
         throw new IllegalArgumentException(String.format("quantity %s must be >0!", quantity));
      } else if (itemId == null) {
         throw new IllegalArgumentException("itemId cannot be null!");
      } else if (itemId.equals("Empty")) {
         throw new IllegalArgumentException("itemId cannot be BlockTypeKey.EMPTY!");
      } else {
         this.itemId = itemId;
         this.quantity = quantity;
         double maxDurability = this.getItem().getMaxDurability();
         this.durability = maxDurability;
         this.maxDurability = maxDurability;
         this.metadata = metadata;
      }
   }

   public ItemStack(@Nonnull String itemId, int quantity, double durability, double maxDurability, @Nullable BsonDocument metadata) {
      this(itemId, quantity, metadata);
      this.durability = durability;
      this.maxDurability = maxDurability;
   }

   public ItemStack(@Nonnull String itemId) {
      this(itemId, 1);
   }

   public ItemStack(@Nonnull String itemId, int quantity) {
      this(itemId, quantity, null);
   }

   protected ItemStack() {
   }

   @Nonnull
   public String getItemId() {
      return this.itemId;
   }

   public int getQuantity() {
      return this.quantity;
   }

   @Nullable
   @Deprecated
   public BsonDocument getMetadata() {
      return this.metadata == null ? null : this.metadata.clone();
   }

   public boolean isUnbreakable() {
      return this.maxDurability <= 0.0;
   }

   public boolean isBroken() {
      return this.isUnbreakable() ? false : this.durability == 0.0;
   }

   public double getMaxDurability() {
      return this.maxDurability;
   }

   public double getDurability() {
      return this.durability;
   }

   public boolean isEmpty() {
      return this.itemId.equals("Empty");
   }

   public boolean getOverrideDroppedItemAnimation() {
      return this.overrideDroppedItemAnimation;
   }

   public void setOverrideDroppedItemAnimation(boolean b) {
      this.overrideDroppedItemAnimation = b;
   }

   @Nullable
   public String getBlockKey() {
      if (this.isEmpty()) {
         return "Empty";
      } else {
         Item item = this.getItem();
         if (item == null) {
            return null;
         } else {
            return item.hasBlockType() ? item.getBlockId() : null;
         }
      }
   }

   @Nonnull
   public Item getItem() {
      Item item = Item.getAssetMap().getAsset(this.itemId);
      return item != null ? item : Item.UNKNOWN;
   }

   public boolean isValid() {
      return this.isEmpty() || this.getItem() != null;
   }

   @Nonnull
   public ItemStack withDurability(double durability) {
      return new ItemStack(this.itemId, this.quantity, MathUtil.clamp(durability, 0.0, this.maxDurability), this.maxDurability, this.metadata);
   }

   @Nonnull
   public ItemStack withMaxDurability(double maxDurability) {
      return new ItemStack(this.itemId, this.quantity, Math.min(this.durability, maxDurability), maxDurability, this.metadata);
   }

   @Nonnull
   public ItemStack withIncreasedDurability(double inc) {
      return this.withDurability(this.durability + inc);
   }

   @Nonnull
   public ItemStack withRestoredDurability(double maxDurability) {
      return new ItemStack(this.itemId, this.quantity, maxDurability, maxDurability, this.metadata);
   }

   @Nonnull
   public ItemStack withState(@Nonnull String state) {
      String newItemId = this.getItem().getItemIdForState(state);
      if (newItemId == null) {
         throw new IllegalArgumentException("Invalid state: " + state);
      } else {
         return new ItemStack(newItemId, this.quantity, this.durability, this.maxDurability, this.metadata);
      }
   }

   @Nullable
   public ItemStack withQuantity(int quantity) {
      if (quantity == 0) {
         return null;
      } else {
         return quantity == this.quantity ? this : new ItemStack(this.itemId, quantity, this.durability, this.maxDurability, this.metadata);
      }
   }

   @Nonnull
   public ItemStack withMetadata(@Nullable BsonDocument metadata) {
      return new ItemStack(this.itemId, this.quantity, this.durability, this.maxDurability, metadata);
   }

   @Nonnull
   public <T> ItemStack withMetadata(@Nonnull KeyedCodec<T> keyedCodec, @Nullable T data) {
      return this.withMetadata(keyedCodec.getKey(), keyedCodec.getChildCodec(), data);
   }

   @Nonnull
   public <T> ItemStack withMetadata(@Nonnull String key, @Nonnull Codec<T> codec, @Nullable T data) {
      BsonDocument clonedMeta = this.metadata == null ? new BsonDocument() : this.metadata.clone();
      if (data == null) {
         clonedMeta.remove(key);
      } else {
         BsonValue bsonValue = codec.encode(data);
         boolean empty = bsonValue.isNull() || bsonValue instanceof BsonDocument doc && doc.isEmpty();
         if (empty) {
            clonedMeta.remove(key);
         } else {
            clonedMeta.put(key, bsonValue);
         }
      }

      if (clonedMeta.isEmpty()) {
         clonedMeta = null;
      }

      return new ItemStack(this.itemId, this.quantity, this.durability, this.maxDurability, clonedMeta);
   }

   @Nonnull
   public ItemStack withMetadata(@Nonnull String key, @Nullable BsonValue bsonValue) {
      BsonDocument clonedMeta = this.metadata == null ? new BsonDocument() : this.metadata.clone();
      if (bsonValue != null && !bsonValue.isNull()) {
         clonedMeta.put(key, bsonValue);
      } else {
         clonedMeta.remove(key);
      }

      return new ItemStack(this.itemId, this.quantity, this.durability, this.maxDurability, clonedMeta);
   }

   public ItemWithAllMetadata toPacket() {
      if (this.cachedPacket != null) {
         return this.cachedPacket;
      } else {
         ItemWithAllMetadata packet = new ItemWithAllMetadata();
         packet.itemId = this.itemId.toString();
         packet.quantity = this.quantity;
         packet.durability = this.durability;
         packet.maxDurability = this.maxDurability;
         packet.overrideDroppedItemAnimation = this.overrideDroppedItemAnimation;
         packet.metadata = this.metadata != null ? this.metadata.toJson() : null;
         this.cachedPacket = packet;
         return this.cachedPacket;
      }
   }

   public boolean isStackableWith(@Nullable ItemStack itemStack) {
      if (itemStack == null) {
         return false;
      } else if (Double.compare(itemStack.durability, this.durability) != 0) {
         return false;
      } else if (Double.compare(itemStack.maxDurability, this.maxDurability) != 0) {
         return false;
      } else if (!this.itemId.equals(itemStack.itemId)) {
         return false;
      } else {
         return this.metadata != null ? this.metadata.equals(itemStack.metadata) : itemStack.metadata == null;
      }
   }

   public boolean isEquivalentType(@Nullable ItemStack itemStack) {
      if (itemStack == null) {
         return false;
      } else if (!this.itemId.equals(itemStack.itemId)) {
         return false;
      } else {
         return this.metadata != null ? this.metadata.equals(itemStack.metadata) : itemStack.metadata == null;
      }
   }

   @Nullable
   public <T> T getFromMetadataOrNull(@Nonnull KeyedCodec<T> keyedCodec) {
      return keyedCodec.getOrNull(this.metadata);
   }

   @Nullable
   public <T> T getFromMetadataOrNull(@Nonnull String key, @Nonnull Codec<T> codec) {
      BsonValue bsonValue = this.metadata == null ? null : this.metadata.get(key);
      return bsonValue == null ? null : codec.decode(bsonValue);
   }

   public <T> T getFromMetadataOrDefault(@Nonnull String key, @Nonnull BuilderCodec<T> codec) {
      BsonDocument clonedMeta = this.metadata == null ? new BsonDocument() : this.metadata.clone();
      if (clonedMeta == null) {
         return codec.getDefaultValue();
      } else {
         BsonValue bsonValue = clonedMeta.get(key);
         return bsonValue == null ? codec.getDefaultValue() : codec.decode(bsonValue);
      }
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ItemStack itemStack = (ItemStack)o;
         if (this.quantity != itemStack.quantity) {
            return false;
         } else if (Double.compare(itemStack.durability, this.durability) != 0) {
            return false;
         } else if (Double.compare(itemStack.maxDurability, this.maxDurability) != 0) {
            return false;
         } else if (!this.itemId.equals(itemStack.itemId)) {
            return false;
         } else {
            return this.metadata != null ? this.metadata.equals(itemStack.metadata) : itemStack.metadata == null;
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.itemId.hashCode();
      result = 31 * result + this.quantity;
      long temp = Double.doubleToLongBits(this.durability);
      result = 31 * result + (int)(temp ^ temp >>> 32);
      temp = Double.doubleToLongBits(this.maxDurability);
      result = 31 * result + (int)(temp ^ temp >>> 32);
      return 31 * result + (this.metadata != null ? this.metadata.hashCode() : 0);
   }

   @Nonnull
   @Override
   public String toString() {
      return "ItemStack{itemId="
         + this.itemId
         + ", quantity="
         + this.quantity
         + ", maxDurability="
         + this.maxDurability
         + ", durability="
         + this.durability
         + ", metadata="
         + this.metadata
         + "}";
   }

   public static boolean isEmpty(@Nullable ItemStack itemFrom) {
      return itemFrom == null || itemFrom.isEmpty();
   }

   public static boolean isStackableWith(@Nullable ItemStack a, ItemStack b) {
      return a == b || a != null && a.isStackableWith(b);
   }

   public static boolean isEquivalentType(@Nullable ItemStack a, ItemStack b) {
      return a == b || a != null && a.isEquivalentType(b);
   }

   public static boolean isSameItemType(@Nullable ItemStack a, @Nullable ItemStack b) {
      return a == b || a != null && b != null && a.itemId.equals(b.itemId);
   }

   @Nullable
   public static ItemStack fromPacket(@Nullable ItemQuantity packet) {
      if (packet == null) {
         return null;
      } else {
         int quantity = packet.quantity;
         return quantity <= 0 ? null : new ItemStack(packet.itemId, quantity, null);
      }
   }

   public static class Metadata {
      public static final String BLOCK_STATE = "BlockState";
   }
}
