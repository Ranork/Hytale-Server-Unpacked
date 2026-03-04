package com.hypixel.hytale.server.core.entity.entities.player.hud;

import com.hypixel.hytale.protocol.packets.interface_.CustomHud;
import com.hypixel.hytale.protocol.packets.interface_.HudComponent;
import com.hypixel.hytale.protocol.packets.interface_.ResetUserInterfaceState;
import com.hypixel.hytale.protocol.packets.interface_.UpdateVisibleHudComponents;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HudManager {
   private static final Set<HudComponent> DEFAULT_HUD_COMPONENTS = Set.of(
      HudComponent.UtilitySlotSelector,
      HudComponent.BlockVariantSelector,
      HudComponent.StatusIcons,
      HudComponent.Hotbar,
      HudComponent.Chat,
      HudComponent.Notifications,
      HudComponent.KillFeed,
      HudComponent.InputBindings,
      HudComponent.Reticle,
      HudComponent.Compass,
      HudComponent.Speedometer,
      HudComponent.ObjectivePanel,
      HudComponent.PortalPanel,
      HudComponent.EventTitle,
      HudComponent.Stamina,
      HudComponent.AmmoIndicator,
      HudComponent.Health,
      HudComponent.Mana,
      HudComponent.Oxygen,
      HudComponent.BuilderToolsLegend,
      HudComponent.Sleep
   );
   private final Set<HudComponent> visibleHudComponents = ConcurrentHashMap.newKeySet();
   private final Set<HudComponent> unmodifiableVisibleHudComponents = Collections.unmodifiableSet(this.visibleHudComponents);
   @Nullable
   private CustomUIHud customHud;

   public HudManager() {
      this.visibleHudComponents.addAll(DEFAULT_HUD_COMPONENTS);
   }

   public HudManager(@Nonnull HudManager other) {
      this.customHud = other.customHud;
   }

   @Nullable
   public CustomUIHud getCustomHud() {
      return this.customHud;
   }

   @Nonnull
   public Set<HudComponent> getVisibleHudComponents() {
      return this.unmodifiableVisibleHudComponents;
   }

   public void setVisibleHudComponents(@Nonnull PlayerRef ref, HudComponent... hudComponents) {
      this.visibleHudComponents.clear();
      Collections.addAll(this.visibleHudComponents, hudComponents);
      this.sendVisibleHudComponents(ref.getPacketHandler());
   }

   public void setVisibleHudComponents(@Nonnull PlayerRef ref, @Nonnull Set<HudComponent> hudComponents) {
      this.visibleHudComponents.clear();
      this.visibleHudComponents.addAll(hudComponents);
      this.sendVisibleHudComponents(ref.getPacketHandler());
   }

   public void showHudComponents(@Nonnull PlayerRef ref, HudComponent... hudComponents) {
      Collections.addAll(this.visibleHudComponents, hudComponents);
      this.sendVisibleHudComponents(ref.getPacketHandler());
   }

   public void showHudComponents(@Nonnull PlayerRef ref, @Nonnull Set<HudComponent> hudComponents) {
      this.visibleHudComponents.addAll(hudComponents);
      this.sendVisibleHudComponents(ref.getPacketHandler());
   }

   public void hideHudComponents(@Nonnull PlayerRef ref, @Nonnull HudComponent... hudComponents) {
      for (HudComponent hudComponent : hudComponents) {
         this.visibleHudComponents.remove(hudComponent);
      }

      this.sendVisibleHudComponents(ref.getPacketHandler());
   }

   public void setCustomHud(@Nonnull PlayerRef ref, @Nullable CustomUIHud hud) {
      CustomUIHud oldHud = this.getCustomHud();
      if (oldHud != hud) {
         this.customHud = hud;
         if (hud == null) {
            ref.getPacketHandler().writeNoCache(new CustomHud(true, null));
         } else {
            hud.show();
         }
      }
   }

   public void resetHud(@Nonnull PlayerRef ref) {
      this.setVisibleHudComponents(ref, DEFAULT_HUD_COMPONENTS);
      this.setCustomHud(ref, null);
   }

   public void resetUserInterface(@Nonnull PlayerRef ref) {
      ref.getPacketHandler().writeNoCache(new ResetUserInterfaceState());
   }

   public void sendVisibleHudComponents(@Nonnull PacketHandler packetHandler) {
      packetHandler.writeNoCache(new UpdateVisibleHudComponents(this.visibleHudComponents.toArray(HudComponent[]::new)));
   }

   @Nonnull
   @Override
   public String toString() {
      return "HudManager{visibleHudComponents="
         + this.visibleHudComponents
         + ", unmodifiableVisibleHudComponents="
         + this.unmodifiableVisibleHudComponents
         + ", customHud="
         + this.customHud
         + "}";
   }
}
