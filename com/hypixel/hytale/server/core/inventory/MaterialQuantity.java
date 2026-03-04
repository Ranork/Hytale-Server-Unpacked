package com.hypixel.hytale.server.core.inventory;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;

public class MaterialQuantity implements NetworkSerializable<com.hypixel.hytale.protocol.MaterialQuantity> {
   public static final MaterialQuantity[] EMPTY_ARRAY = new MaterialQuantity[0];
   public static final BuilderCodec<MaterialQuantity> CODEC = BuilderCodec.builder(MaterialQuantity.class, MaterialQuantity::new)
      .addField(
         new KeyedCodec<>("ItemId", Codec.STRING),
         (craftingMaterial, blockTypeKey) -> craftingMaterial.itemId = blockTypeKey,
         craftingMaterial -> craftingMaterial.itemId
      )
      .addField(
         new KeyedCodec<>("ResourceTypeId", Codec.STRING),
         (craftingMaterial, s) -> craftingMaterial.resourceTypeId = s,
         craftingMaterial -> craftingMaterial.resourceTypeId
      )
      .addField(new KeyedCodec<>("ItemTag", Codec.STRING), (materialQuantity, s) -> materialQuantity.tag = s, materialQuantity -> materialQuantity.tag)
      .<Integer>append(
         new KeyedCodec<>("Quantity", Codec.INTEGER), (craftingMaterial, s) -> craftingMaterial.quantity = s, craftingMaterial -> craftingMaterial.quantity
      )
      .addValidator(Validators.greaterThan(0))
      .add()
      .addField(
         new KeyedCodec<>("Metadata", Codec.BSON_DOCUMENT),
         (craftingMaterial, s) -> craftingMaterial.metadata = s,
         craftingMaterial -> craftingMaterial.metadata
      )
      .afterDecode((materialQuantity, extraInfo) -> {
         if (materialQuantity.tag != null) {
            materialQuantity.tagIndex = AssetRegistry.getOrCreateTagIndex(materialQuantity.tag);
         }
      })
      .build();
   @Nullable
   protected String itemId;
   @Nullable
   protected String resourceTypeId;
   protected String tag;
   protected int tagIndex = Integer.MIN_VALUE;
   protected int quantity = 1;
   @Nullable
   protected BsonDocument metadata;

   public MaterialQuantity(@Nullable String itemId, @Nullable String resourceTypeId, @Nullable String tag, int quantity, BsonDocument metadata) {
      if (itemId == null && resourceTypeId == null && tag == null) {
         throw new IllegalArgumentException("itemId, resourceTypeId and tag cannot all be null!");
      } else if (quantity <= 0) {
         throw new IllegalArgumentException("quantity " + quantity + " must be >0!");
      } else {
         this.itemId = itemId;
         this.resourceTypeId = resourceTypeId;
         this.tag = tag;
         this.quantity = quantity;
         this.metadata = metadata;
      }
   }

   protected MaterialQuantity() {
   }

   @Nullable
   public String getItemId() {
      return this.itemId;
   }

   @Nullable
   public String getResourceTypeId() {
      return this.resourceTypeId;
   }

   public int getTagIndex() {
      return this.tagIndex;
   }

   public int getQuantity() {
      return this.quantity;
   }

   public BsonDocument getMetadata() {
      return this.metadata;
   }

   @Nonnull
   public MaterialQuantity clone(int quantity) {
      return new MaterialQuantity(this.itemId, this.resourceTypeId, this.tag, quantity, this.metadata);
   }

   @Nullable
   public ItemStack toItemStack() {
      if (this.itemId == null) {
         return null;
      } else {
         return this.itemId.equals("Empty") ? ItemStack.EMPTY : new ItemStack(this.itemId, this.quantity, this.metadata);
      }
   }

   @Nullable
   public ResourceQuantity toResource() {
      return this.resourceTypeId == null ? null : new ResourceQuantity(this.resourceTypeId, this.quantity);
   }

   @Nonnull
   public com.hypixel.hytale.protocol.MaterialQuantity toPacket() {
      com.hypixel.hytale.protocol.MaterialQuantity packet = new com.hypixel.hytale.protocol.MaterialQuantity();
      if (this.itemId != null) {
         packet.itemId = this.itemId.toString();
      }

      packet.itemTag = this.tagIndex;
      packet.resourceTypeId = this.resourceTypeId;
      packet.quantity = this.quantity;
      return packet;
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         MaterialQuantity that = (MaterialQuantity)o;
         if (this.quantity != that.quantity) {
            return false;
         } else if (this.itemId != null ? this.itemId.equals(that.itemId) : that.itemId == null) {
            if (this.resourceTypeId != null ? this.resourceTypeId.equals(that.resourceTypeId) : that.resourceTypeId == null) {
               if (this.tag != null ? this.tag.equals(that.tag) : that.tag == null) {
                  return this.metadata != null ? this.metadata.equals(that.metadata) : that.metadata == null;
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.itemId != null ? this.itemId.hashCode() : 0;
      result = 31 * result + (this.resourceTypeId != null ? this.resourceTypeId.hashCode() : 0);
      result = 31 * result + (this.tag != null ? this.tag.hashCode() : 0);
      result = 31 * result + this.quantity;
      return 31 * result + (this.metadata != null ? this.metadata.hashCode() : 0);
   }

   @Nonnull
   @Override
   public String toString() {
      return "MaterialQuantity{itemId="
         + this.itemId
         + ", resourceTypeId='"
         + this.resourceTypeId
         + "', tag='"
         + this.tag
         + "', quantity="
         + this.quantity
         + ", metadata="
         + this.metadata
         + "}";
   }
}
