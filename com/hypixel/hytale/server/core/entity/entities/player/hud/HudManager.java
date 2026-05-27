package com.hypixel.hytale.server.core.entity.entities.player.hud;

import com.hypixel.hytale.protocol.packets.interface_.CustomHud;
import com.hypixel.hytale.protocol.packets.interface_.HudComponent;
import com.hypixel.hytale.protocol.packets.interface_.ResetUserInterfaceState;
import com.hypixel.hytale.protocol.packets.interface_.UpdateVisibleHudComponents;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
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
   private final Map<String, CustomUIHud> customHuds = new LinkedHashMap<>();

   public HudManager() {
      this.visibleHudComponents.addAll(DEFAULT_HUD_COMPONENTS);
   }

   @Nullable
   public CustomUIHud getCustomHud(@Nonnull String key) {
      return this.customHuds.get(key);
   }

   @Nonnull
   public Map<String, CustomUIHud> getCustomHuds() {
      return Collections.unmodifiableMap(this.customHuds);
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

   public void addCustomHud(@Nonnull PlayerRef ref, @Nonnull CustomUIHud hud) {
      String key = hud.getKey();
      CustomUIHud oldHud = this.customHuds.get(key);
      if (oldHud != hud) {
         if (oldHud != null) {
            oldHud.onRemove();
            ref.getPacketHandler().writeNoCache(new CustomHud(key, 0, true, null));
         }

         this.customHuds.put(key, hud);
         hud.show();
      }
   }

   public void removeCustomHud(@Nonnull PlayerRef ref, @Nonnull String key) {
      CustomUIHud oldHud = this.customHuds.remove(key);
      if (oldHud != null) {
         oldHud.onRemove();
         ref.getPacketHandler().writeNoCache(new CustomHud(key, 0, true, null));
      }
   }

   public void resetHud(@Nonnull PlayerRef ref) {
      this.setVisibleHudComponents(ref, DEFAULT_HUD_COMPONENTS);

      for (Entry<String, CustomUIHud> entry : this.customHuds.entrySet()) {
         entry.getValue().onRemove();
         ref.getPacketHandler().writeNoCache(new CustomHud(entry.getKey(), 0, true, null));
      }

      this.customHuds.clear();
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
      return "HudManager{visibleHudComponents=" + this.visibleHudComponents + ", customHuds=" + this.customHuds + "}";
   }
}
