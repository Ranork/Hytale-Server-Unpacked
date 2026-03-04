package com.hypixel.hytale.server.core.entity;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionSyncData;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChain;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ChainSyncStorage {
   InteractionState getClientState();

   void setClientState(InteractionState var1);

   @Nullable
   InteractionEntry getInteraction(int var1);

   void putInteractionSyncData(int var1, InteractionSyncData var2);

   void updateSyncPosition(int var1);

   boolean isSyncDataOutOfOrder(int var1);

   void syncFork(@Nonnull Ref<EntityStore> var1, @Nonnull InteractionManager var2, @Nonnull SyncInteractionChain var3);

   void clearInteractionSyncData(int var1);
}
