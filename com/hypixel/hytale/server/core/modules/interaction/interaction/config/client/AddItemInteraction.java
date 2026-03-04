package com.hypixel.hytale.server.core.modules.interaction.interaction.config.client;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AddItemInteraction extends SimpleBlockInteraction {
   @Nonnull
   public static final BuilderCodec<AddItemInteraction> CODEC = BuilderCodec.builder(
         AddItemInteraction.class, AddItemInteraction::new, SimpleBlockInteraction.CODEC
      )
      .documentation("Adds an item to the users inventory.")
      .<String>append(new KeyedCodec<>("ItemId", Codec.STRING), (i, s) -> i.itemId = s, i -> i.itemId)
      .documentation("The id of the item to add.")
      .addValidator(Validators.nonNull())
      .addValidator(Item.VALIDATOR_CACHE.getValidator())
      .add()
      .<Integer>append(new KeyedCodec<>("Quantity", Codec.INTEGER), (o, v) -> o.quantity = v, o -> o.quantity)
      .documentation("The amount of the item to add.")
      .add()
      .build();
   protected String itemId;
   protected int quantity;

   @Override
   protected void interactWithBlock(
      @Nonnull World world,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull InteractionType type,
      @Nonnull InteractionContext context,
      @Nullable ItemStack itemInHand,
      @Nonnull Vector3i targetBlock,
      @Nonnull CooldownHandler cooldownHandler
   ) {
      if (this.quantity > 0 && this.itemId != null) {
         Ref<EntityStore> ref = context.getEntity();
         if (EntityUtils.getEntity(ref, commandBuffer) instanceof LivingEntity livingEntity) {
            livingEntity.getInventory().getCombinedHotbarFirst().addItemStack(new ItemStack(this.itemId, this.quantity));
         }
      }
   }

   @Override
   protected void simulateInteractWithBlock(
      @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull World world, @Nonnull Vector3i targetBlock
   ) {
   }
}
