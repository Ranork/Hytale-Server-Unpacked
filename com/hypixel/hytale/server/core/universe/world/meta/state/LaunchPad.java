package com.hypixel.hytale.server.core.universe.world.meta.state;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LaunchPad implements Component<ChunkStore> {
   private static final int MAX_VELOCITY = 50000;
   @Nonnull
   public static final BuilderCodec<LaunchPad> CODEC = BuilderCodec.builder(LaunchPad.class, LaunchPad::new)
      .append(new KeyedCodec<>("VelocityX", Codec.DOUBLE), (state, d) -> state.velocityX = d.floatValue(), state -> (double)state.velocityX)
      .documentation("The X velocity of the launch pad.")
      .add()
      .<Double>append(new KeyedCodec<>("VelocityY", Codec.DOUBLE), (state, d) -> state.velocityY = d.floatValue(), state -> (double)state.velocityY)
      .documentation("The Y velocity of the launch pad.")
      .add()
      .<Double>append(new KeyedCodec<>("VelocityZ", Codec.DOUBLE), (state, d) -> state.velocityZ = d.floatValue(), state -> (double)state.velocityZ)
      .documentation("The Z velocity of the launch pad.")
      .add()
      .<Boolean>append(new KeyedCodec<>("PlayersOnly", Codec.BOOLEAN), (state, b) -> state.playersOnly = b, state -> state.playersOnly)
      .documentation("Determines whether only players can use this launch pad.")
      .add()
      .build();
   private float velocityX;
   private float velocityY;
   private float velocityZ;
   private boolean playersOnly;

   @Nonnull
   public static ComponentType<ChunkStore, LaunchPad> getComponentType() {
      return BlockModule.get().getLaunchPadComponentType();
   }

   public LaunchPad() {
   }

   public LaunchPad(float velocityX, float velocityY, float velocityZ, boolean playersOnly) {
      this.velocityX = velocityX;
      this.velocityY = velocityY;
      this.velocityZ = velocityZ;
      this.playersOnly = playersOnly;
   }

   public float getVelocityX() {
      return this.velocityX;
   }

   public float getVelocityY() {
      return this.velocityY;
   }

   public float getVelocityZ() {
      return this.velocityZ;
   }

   public boolean isPlayersOnly() {
      return this.playersOnly;
   }

   private static float clampVelocity(float velocity) {
      return Math.max(Math.min(velocity, 50000.0F), -50000.0F);
   }

   @Nonnull
   @Override
   public String toString() {
      return "LaunchPadState{velocityX="
         + this.velocityX
         + ", velocityY="
         + this.velocityY
         + ", velocityZ="
         + this.velocityZ
         + ", playersOnly="
         + this.playersOnly
         + "}";
   }

   @Nullable
   @Override
   public Component<ChunkStore> clone() {
      return new LaunchPad(this.velocityX, this.velocityY, this.velocityZ, this.playersOnly);
   }

   public static class LaunchPadSettingsPage extends InteractiveCustomUIPage<LaunchPad.LaunchPadSettingsPage.LaunchPadSettingsPageEventData> {
      private final Ref<ChunkStore> ref;

      public LaunchPadSettingsPage(@Nonnull PlayerRef playerRef, Ref<ChunkStore> ref, @Nonnull CustomPageLifetime lifetime) {
         super(playerRef, lifetime, LaunchPad.LaunchPadSettingsPage.LaunchPadSettingsPageEventData.CODEC);
         this.ref = ref;
      }

      @Override
      public void build(
         @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
      ) {
         commandBuilder.append("Pages/LaunchPadSettingsPage.ui");
         ChunkStore chunkStore = store.getExternalData().getWorld().getChunkStore();
         LaunchPad launchPadComponent = chunkStore.getStore().getComponent(this.ref, LaunchPad.getComponentType());
         commandBuilder.set("#VelocityX.Value", launchPadComponent.velocityX);
         commandBuilder.set("#VelocityY.Value", launchPadComponent.velocityY);
         commandBuilder.set("#VelocityZ.Value", launchPadComponent.velocityZ);
         commandBuilder.set("#PlayersOnlyContainer #CheckBox.Value", launchPadComponent.playersOnly);
         eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#SaveButton",
            new EventData()
               .append("@X", "#VelocityX.Value")
               .append("@Y", "#VelocityY.Value")
               .append("@Z", "#VelocityZ.Value")
               .append("@PlayersOnly", "#PlayersOnlyContainer #CheckBox.Value")
         );
      }

      public void handleDataEvent(
         @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull LaunchPad.LaunchPadSettingsPage.LaunchPadSettingsPageEventData data
      ) {
         ChunkStore chunkStore = store.getExternalData().getWorld().getChunkStore();
         LaunchPad launchPadComponent = chunkStore.getStore().getComponent(this.ref, LaunchPad.getComponentType());

         assert launchPadComponent != null;

         BlockModule.BlockStateInfo blockStateInfoComponent = chunkStore.getStore().getComponent(this.ref, BlockModule.BlockStateInfo.getComponentType());

         assert blockStateInfoComponent != null;

         launchPadComponent.velocityX = LaunchPad.clampVelocity((float)data.x);
         launchPadComponent.velocityY = LaunchPad.clampVelocity((float)data.y);
         launchPadComponent.velocityZ = LaunchPad.clampVelocity((float)data.z);
         launchPadComponent.playersOnly = data.playersOnly;
         WorldChunk worldChunkComponent = chunkStore.getStore().getComponent(blockStateInfoComponent.getChunkRef(), WorldChunk.getComponentType());

         assert worldChunkComponent != null;

         worldChunkComponent.markNeedsSaving();
         Player playerComponent = store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         playerComponent.getPageManager().setPage(ref, store, Page.None);
      }

      public static class LaunchPadSettingsPageEventData {
         public static final String KEY_X = "@X";
         public static final String KEY_Y = "@Y";
         public static final String KEY_Z = "@Z";
         public static final String KEY_PLAYERS_ONLY = "@PlayersOnly";
         public static final BuilderCodec<LaunchPad.LaunchPadSettingsPage.LaunchPadSettingsPageEventData> CODEC = BuilderCodec.builder(
               LaunchPad.LaunchPadSettingsPage.LaunchPadSettingsPageEventData.class, LaunchPad.LaunchPadSettingsPage.LaunchPadSettingsPageEventData::new
            )
            .addField(new KeyedCodec<>("@X", Codec.DOUBLE), (entry, s) -> entry.x = s, entry -> entry.x)
            .addField(new KeyedCodec<>("@Y", Codec.DOUBLE), (entry, s) -> entry.y = s, entry -> entry.y)
            .addField(new KeyedCodec<>("@Z", Codec.DOUBLE), (entry, s) -> entry.z = s, entry -> entry.z)
            .addField(new KeyedCodec<>("@PlayersOnly", Codec.BOOLEAN), (entry, s) -> entry.playersOnly = s, entry -> entry.playersOnly)
            .build();
         private double x;
         private double y;
         private double z;
         private boolean playersOnly;
      }
   }
}
