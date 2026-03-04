package com.hypixel.hytale.builtin.portals.ui;

import com.hypixel.hytale.builtin.portals.components.PortalDevice;
import com.hypixel.hytale.builtin.portals.components.PortalDeviceConfig;
import com.hypixel.hytale.builtin.portals.resources.PortalWorld;
import com.hypixel.hytale.builtin.portals.utils.BlockTypeUtils;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PortalDevicePageSupplier implements OpenCustomUIInteraction.CustomPageSupplier {
   @Nonnull
   public static final BuilderCodec<PortalDevicePageSupplier> CODEC = BuilderCodec.builder(PortalDevicePageSupplier.class, PortalDevicePageSupplier::new)
      .appendInherited(
         new KeyedCodec<>("Config", PortalDeviceConfig.CODEC),
         (supplier, o) -> supplier.config = o,
         supplier -> supplier.config,
         (supplier, parent) -> supplier.config = parent.config
      )
      .documentation("The portal device's config.")
      .add()
      .build();
   private PortalDeviceConfig config;

   @Override
   public CustomUIPage tryCreate(
      @Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> store, @Nonnull PlayerRef playerRef, @Nonnull InteractionContext context
   ) {
      BlockPosition targetBlock = context.getTargetBlock();
      if (targetBlock == null) {
         return null;
      } else {
         Player playerComponent = store.getComponent(ref, Player.getComponentType());
         if (playerComponent == null) {
            return null;
         } else {
            ItemStack inHand = playerComponent.getInventory().getItemInHand();
            World world = store.getExternalData().getWorld();
            BlockType blockType = world.getBlockType(targetBlock.x, targetBlock.y, targetBlock.z);
            if (blockType == null) {
               playerRef.sendMessage(
                  Message.translation("server.portals.device.blockTypeMisconfigured")
                     .param("x", targetBlock.x)
                     .param("y", targetBlock.y)
                     .param("z", targetBlock.z)
               );
               return null;
            } else {
               for (String blockStateKey : this.config.getBlockStates()) {
                  BlockType blockState = BlockTypeUtils.getBlockForState(blockType, blockStateKey);
                  if (blockState == null) {
                     playerRef.sendMessage(Message.translation("server.portals.device.blockStateMisconfigured").param("state", blockStateKey));
                     return null;
                  }
               }

               BlockType onBlockType = BlockTypeUtils.getBlockForState(blockType, this.config.getOnState());
               ChunkStore chunkStore = world.getChunkStore();
               Ref<ChunkStore> blockRef = BlockModule.getBlockEntity(world, targetBlock.x, targetBlock.y, targetBlock.z);
               if (blockRef == null) {
                  playerRef.sendMessage(Message.translation("server.portals.device.blockEntityMisconfigured"));
                  return null;
               } else {
                  PortalDevice existingDevice = chunkStore.getStore().getComponent(blockRef, PortalDevice.getComponentType());
                  World destinationWorld = existingDevice == null ? null : existingDevice.getDestinationWorld();
                  if (existingDevice != null && blockType == onBlockType && !isPortalWorldValid(destinationWorld)) {
                     world.setBlockInteractionState(new Vector3i(targetBlock.x, targetBlock.y, targetBlock.z), blockType, this.config.getOffState());
                     playerRef.sendMessage(Message.translation("server.portals.device.adjusted").color("#ff0000"));
                     return null;
                  } else if (existingDevice != null && destinationWorld != null) {
                     return new PortalDeviceActivePage(playerRef, this.config, blockRef);
                  } else {
                     chunkStore.getStore().putComponent(blockRef, PortalDevice.getComponentType(), new PortalDevice(this.config, blockType.getId()));
                     return new PortalDeviceSummonPage(playerRef, this.config, blockRef, inHand);
                  }
               }
            }
         }
      }
   }

   private static boolean isPortalWorldValid(@Nullable World world) {
      if (world == null) {
         return false;
      } else {
         Store<EntityStore> store = world.getEntityStore().getStore();
         PortalWorld portalWorld = store.getResource(PortalWorld.getResourceType());
         return portalWorld.exists();
      }
   }
}
