package com.hypixel.hytale.builtin.portals.ui;

import com.hypixel.hytale.builtin.portals.components.PortalDevice;
import com.hypixel.hytale.builtin.portals.components.PortalDeviceConfig;
import com.hypixel.hytale.builtin.portals.resources.PortalWorld;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.portalworld.PortalType;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nonnull;

public class PortalDeviceActivePage extends InteractiveCustomUIPage<PortalDeviceActivePage.Data> {
   @Nonnull
   private final PortalDeviceConfig config;
   @Nonnull
   private final Ref<ChunkStore> blockRef;

   public PortalDeviceActivePage(@Nonnull PlayerRef playerRef, @Nonnull PortalDeviceConfig config, @Nonnull Ref<ChunkStore> blockRef) {
      super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, PortalDeviceActivePage.Data.CODEC);
      this.config = config;
      this.blockRef = blockRef;
   }

   @Override
   public void build(
      @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
   ) {
      PortalDeviceActivePage.State state = this.computeState(ref, store);
      if (state != PortalDeviceActivePage.Error.INVALID_BLOCK) {
         commandBuilder.append("Pages/PortalDeviceActive.ui");
         if (state instanceof PortalDeviceActivePage.PortalIsOpen(World playerCountMsg, PortalWorld var19, boolean var26)) {
            boolean var20 = var26;
            if (true) {
               PortalType var18 = var19.getPortalType();
               commandBuilder.set("#PortalPanel.Visible", true);
               if (var20) {
                  commandBuilder.set("#Died.Visible", true);
               }

               commandBuilder.set(
                  "#PortalTitle.TextSpans", Message.translation("server.customUI.portalDevice.portalTitle").param("name", var18.getDisplayName())
               );
               commandBuilder.set("#PortalDescription.TextSpans", PortalDeviceSummonPage.createDescription(var18, var19.getTimeLimitSeconds()));
               Message playerCountMsgx = createPlayerCountMsg(playerCountMsg);
               commandBuilder.set("#PlayersInside.TextSpans", playerCountMsgx);
               double remainingSeconds = var19.getRemainingSeconds(playerCountMsg);
               if (remainingSeconds < var19.getTimeLimitSeconds()) {
                  int remainingMinutes = (int)Math.round(remainingSeconds / 60.0);
                  Message remainingTimeMsg = remainingMinutes <= 1
                     ? Message.translation("server.customUI.portalDevice.lessThanAMinute")
                     : Message.translation("server.customUI.portalDevice.remainingMinutes").param("time", remainingMinutes);
                  commandBuilder.set(
                     "#RemainingDuration.TextSpans",
                     Message.translation("server.customUI.portalDevice.remainingDuration").param("remaining", remainingTimeMsg.color("#ea4fa46b"))
                  );
               } else {
                  commandBuilder.set("#PortalIsOpen.Visible", true);
                  commandBuilder.set("#RemainingDuration.TextSpans", Message.translation("server.customUI.portalDevice.beTheFirst").color("#ea4fa46b"));
               }

               return;
            }
         }

         commandBuilder.set("#Error.Visible", true);
         commandBuilder.set("#ErrorLabel.Text", Message.translation("server.customUI.portalDevice.unknownError").param("state", state.toString()));
      }
   }

   @Nonnull
   private static Message createPlayerCountMsg(@Nonnull World world) {
      int playerCount = world.getPlayerCount();
      String pinkEnoughColor = "#ea4fa46b";
      if (playerCount == 0) {
         return Message.translation("server.customUI.portalDevice.playersInside")
            .param("count", Message.translation("server.customUI.portalDevice.playersInsideNone").color(pinkEnoughColor));
      } else if (playerCount > 4) {
         return Message.translation("server.customUI.portalDevice.playersInside").param("count", Message.raw(playerCount + "!").color(pinkEnoughColor));
      } else {
         Message msg = Message.translation("server.customUI.portalDevice.playersInside").param("count", Message.raw(playerCount + "!").color(pinkEnoughColor));

         for (PlayerRef ref : world.getPlayerRefs()) {
            msg.insert(Message.raw("- ").color("#6b6b6b6b")).insert(ref.getUsername());
         }

         return msg;
      }
   }

   @Nonnull
   private PortalDeviceActivePage.State computeState(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (!this.blockRef.isValid()) {
         return PortalDeviceActivePage.Error.INVALID_BLOCK;
      } else {
         Store<ChunkStore> chunkStore = this.blockRef.getStore();
         PortalDevice portalDevice = chunkStore.getComponent(this.blockRef, PortalDevice.getComponentType());
         if (portalDevice == null) {
            return PortalDeviceActivePage.Error.INVALID_BLOCK;
         } else {
            World destinationWorld = portalDevice.getDestinationWorld();
            if (destinationWorld == null) {
               return PortalDeviceActivePage.Error.INVALID_WORLD;
            } else {
               Store<EntityStore> destinationStore = destinationWorld.getEntityStore().getStore();
               PortalWorld portalWorld = destinationStore.getResource(PortalWorld.getResourceType());
               if (!portalWorld.exists()) {
                  return PortalDeviceActivePage.Error.DESTINATION_NOT_FRAGMENT;
               } else {
                  UUIDComponent uuidComponent = componentAccessor.getComponent(ref, UUIDComponent.getComponentType());
                  if (uuidComponent == null) {
                     return PortalDeviceActivePage.Error.INACTIVE_PORTAL;
                  } else {
                     UUID playerUUID = uuidComponent.getUuid();
                     boolean diedInside = portalWorld.getDiedInWorld().contains(playerUUID);
                     return new PortalDeviceActivePage.PortalIsOpen(destinationWorld, portalWorld, diedInside);
                  }
               }
            }
         }
      }
   }

   protected static class Data {
      @Nonnull
      public static final BuilderCodec<PortalDeviceActivePage.Data> CODEC = BuilderCodec.builder(
            PortalDeviceActivePage.Data.class, PortalDeviceActivePage.Data::new
         )
         .build();
   }

   private static enum Error implements PortalDeviceActivePage.State {
      INVALID_BLOCK,
      INVALID_WORLD,
      DESTINATION_NOT_FRAGMENT,
      INACTIVE_PORTAL;
   }

   private record PortalIsOpen(World world, PortalWorld portalWorld, boolean diedInside) implements PortalDeviceActivePage.State {
   }

   private sealed interface State permits PortalDeviceActivePage.PortalIsOpen, PortalDeviceActivePage.Error {
   }
}
