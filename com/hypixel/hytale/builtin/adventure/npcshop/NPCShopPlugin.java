package com.hypixel.hytale.builtin.adventure.npcshop;

import com.hypixel.hytale.builtin.adventure.npcshop.npc.builders.BuilderActionOpenBarterShop;
import com.hypixel.hytale.builtin.adventure.npcshop.npc.builders.BuilderActionOpenShop;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.npc.NPCPlugin;
import javax.annotation.Nonnull;

public class NPCShopPlugin extends JavaPlugin {
   public NPCShopPlugin(@Nonnull JavaPluginInit init) {
      super(init);
   }

   @Override
   protected void setup() {
      NPCPlugin.get().registerCoreComponentType("OpenShop", BuilderActionOpenShop::new);
      NPCPlugin.get().registerCoreComponentType("OpenBarterShop", BuilderActionOpenBarterShop::new);
   }
}
