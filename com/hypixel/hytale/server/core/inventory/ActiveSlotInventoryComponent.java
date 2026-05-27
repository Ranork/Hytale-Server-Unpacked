package com.hypixel.hytale.server.core.inventory;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.event.events.ecs.InventorySetActiveSlotEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ActiveSlotInventoryComponent extends InventoryComponent {
   @Nonnull
   public static final BuilderCodec<ActiveSlotInventoryComponent> CODEC = BuilderCodec.abstractBuilder(
         ActiveSlotInventoryComponent.class, InventoryComponent.CODEC
      )
      .append(new KeyedCodec<>("ActiveSlot", Codec.BYTE), (o, i) -> o.activeSlot = i, o -> o.activeSlot)
      .add()
      .afterDecode(ActiveSlotInventoryComponent::clampActiveSlot)
      .build();
   protected byte activeSlot = -1;

   protected ActiveSlotInventoryComponent() {
   }

   protected ActiveSlotInventoryComponent(short capacity) {
      super(capacity);
      this.clampActiveSlot();
   }

   public abstract int getSectionId();

   protected void clampActiveSlot() {
      if (this.activeSlot >= this.inventory.getCapacity()) {
         this.activeSlot = -1;
      }
   }

   @Override
   public void ensureCapacity(short capacity, @Nonnull List<ItemStack> remainder) {
      super.ensureCapacity(capacity, remainder);
      this.clampActiveSlot();
   }

   public byte getActiveSlot() {
      return this.activeSlot;
   }

   public void setActiveSlot(byte slot, @Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> accessor) {
      if (this.activeSlot != slot) {
         byte previous = this.activeSlot;
         this.activeSlot = slot;
         accessor.invoke(ref, new InventorySetActiveSlotEvent(this.getSectionId(), previous, slot));
      }
   }

   public void setActiveSlot(byte slot, @Nonnull Holder<EntityStore> holder, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (this.activeSlot != slot) {
         byte previous = this.activeSlot;
         this.activeSlot = slot;
         componentAccessor.invoke(holder, new InventorySetActiveSlotEvent(this.getSectionId(), previous, slot));
      }
   }

   @Nullable
   public ItemStack getActiveItem() {
      return this.activeSlot != -1 && this.activeSlot < this.inventory.getCapacity() ? this.inventory.getItemStack(this.activeSlot) : null;
   }
}
