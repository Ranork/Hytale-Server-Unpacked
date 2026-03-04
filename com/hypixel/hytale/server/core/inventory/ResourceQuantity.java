package com.hypixel.hytale.server.core.inventory;

import com.hypixel.hytale.protocol.ItemResourceType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ResourceQuantity {
   protected String resourceId;
   protected int quantity;

   public ResourceQuantity(String resourceId, int quantity) {
      Objects.requireNonNull(resourceId, "resourceId cannot be null!");
      if (quantity <= 0) {
         throw new IllegalArgumentException("quantity " + quantity + " must be >0!");
      } else {
         this.resourceId = resourceId;
         this.quantity = quantity;
      }
   }

   protected ResourceQuantity() {
   }

   public String getResourceId() {
      return this.resourceId;
   }

   public int getQuantity() {
      return this.quantity;
   }

   @Nonnull
   public ResourceQuantity clone(int quantity) {
      return new ResourceQuantity(this.resourceId, quantity);
   }

   @Nullable
   public ItemResourceType getResourceType(@Nonnull Item item) {
      return ItemContainer.getMatchingResourceType(item, this.resourceId);
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ResourceQuantity itemStack = (ResourceQuantity)o;
         if (this.quantity != itemStack.quantity) {
            return false;
         } else {
            return this.resourceId != null ? this.resourceId.equals(itemStack.resourceId) : itemStack.resourceId == null;
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.resourceId != null ? this.resourceId.hashCode() : 0;
      return 31 * result + this.quantity;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ResourceQuantity{resourceId='" + this.resourceId + "', quantity=" + this.quantity + "}";
   }
}
