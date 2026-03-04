package com.hypixel.hytale.server.core.modules.interaction;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public interface IInteractionSimulationHandler {
   void setState(InteractionType var1, boolean var2);

   boolean isCharging(boolean var1, float var2, InteractionType var3, InteractionContext var4, Ref<EntityStore> var5, CooldownHandler var6);

   boolean shouldCancelCharging(boolean var1, float var2, InteractionType var3, InteractionContext var4, Ref<EntityStore> var5, CooldownHandler var6);

   float getChargeValue(boolean var1, float var2, InteractionType var3, InteractionContext var4, Ref<EntityStore> var5, CooldownHandler var6);
}
