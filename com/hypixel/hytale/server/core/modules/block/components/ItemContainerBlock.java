package com.hypixel.hytale.server.core.modules.block.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDropList;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerBlockWindow;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemContainerBlock implements Component<ChunkStore> {
   public static final BuilderCodec<ItemContainerBlock> CODEC = BuilderCodec.builder(ItemContainerBlock.class, ItemContainerBlock::new)
      .appendInherited(new KeyedCodec<>("Droplist", Codec.STRING), (state, o) -> state.droplist = o, state -> state.droplist, (o, p) -> o.droplist = p.droplist)
      .addValidator(ItemDropList.VALIDATOR_CACHE.getValidator())
      .add()
      .appendInherited(
         new KeyedCodec<>("ItemContainer", SimpleItemContainer.CODEC),
         (state, o) -> state.itemContainer = o,
         state -> state.itemContainer,
         (o, p) -> o.itemContainer = p.itemContainer.clone()
      )
      .add()
      .appendInherited(new KeyedCodec<>("Capacity", Codec.SHORT), (o, i) -> o.capacity = i, o -> o.capacity, (o, p) -> o.capacity = p.capacity)
      .add()
      .build();
   private final transient Map<UUID, ContainerBlockWindow> windows = new ConcurrentHashMap<>();
   @Nullable
   protected String droplist;
   protected SimpleItemContainer itemContainer;
   protected short capacity = 20;

   public static ComponentType<ChunkStore, ItemContainerBlock> getComponentType() {
      return BlockModule.get().getItemContainerBlockComponentType();
   }

   private ItemContainerBlock() {
   }

   public ItemContainerBlock(ItemContainerBlock itemContainerBlock) {
      this.droplist = itemContainerBlock.droplist;
      this.itemContainer = itemContainerBlock.itemContainer != null ? itemContainerBlock.itemContainer.clone() : null;
      this.capacity = itemContainerBlock.capacity;
   }

   public void setItemContainer(SimpleItemContainer itemContainer) {
      this.itemContainer = itemContainer;
   }

   @Nullable
   public String getDroplist() {
      return this.droplist;
   }

   public void setDroplist(@Nullable String droplist) {
      this.droplist = droplist;
   }

   @Nonnull
   public Map<UUID, ContainerBlockWindow> getWindows() {
      return this.windows;
   }

   public SimpleItemContainer getItemContainer() {
      if (this.itemContainer == null) {
         this.itemContainer = new SimpleItemContainer(this.capacity);
      }

      return this.itemContainer;
   }

   public short getCapacity() {
      return this.capacity;
   }

   @Nullable
   @Override
   public Component<ChunkStore> clone() {
      return new ItemContainerBlock(this);
   }
}
