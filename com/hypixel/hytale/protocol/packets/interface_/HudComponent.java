package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum HudComponent {
   Hotbar(0),
   StatusIcons(1),
   Reticle(2),
   Chat(3),
   Requests(4),
   Notifications(5),
   KillFeed(6),
   InputBindings(7),
   PlayerList(8),
   EventTitle(9),
   Compass(10),
   ObjectivePanel(11),
   PortalPanel(12),
   BuilderToolsLegend(13),
   Speedometer(14),
   UtilitySlotSelector(15),
   BlockVariantSelector(16),
   BuilderToolsMaterialSlotSelector(17),
   Stamina(18),
   AmmoIndicator(19),
   Health(20),
   Mana(21),
   Oxygen(22),
   Sleep(23);

   public static final HudComponent[] VALUES = values();
   private final int value;

   private HudComponent(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static HudComponent fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("HudComponent", value);
      }
   }
}
