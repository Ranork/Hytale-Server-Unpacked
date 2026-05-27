package com.hypixel.hytale.builtin.audio;

import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.event.RemovedAssetsEvent;
import com.hypixel.hytale.builtin.audio.commands.AudioCommands;
import com.hypixel.hytale.builtin.audio.components.AudioStateComponent;
import com.hypixel.hytale.builtin.audio.components.ForcedMusicTracker;
import com.hypixel.hytale.builtin.audio.systems.AudioStateSystems;
import com.hypixel.hytale.builtin.audio.systems.ForcedMusicSystems;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.server.core.asset.type.ambiencefx.config.AmbienceFX;
import com.hypixel.hytale.server.core.asset.type.audiocategory.config.AudioCategory;
import com.hypixel.hytale.server.core.asset.type.audiostate.config.AudioState;
import com.hypixel.hytale.server.core.asset.type.musiccontainer.config.MusicContainer;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class AudioPlugin extends JavaPlugin {
   private static AudioPlugin instance;
   private ComponentType<EntityStore, AudioStateComponent> audioStateComponentType;
   private ComponentType<EntityStore, ForcedMusicTracker> forcedMusicTrackerComponentType;

   public static AudioPlugin get() {
      return instance;
   }

   public AudioPlugin(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   @Override
   protected void setup() {
      ComponentRegistryProxy<EntityStore> entityStoreRegistry = this.getEntityStoreRegistry();
      this.audioStateComponentType = entityStoreRegistry.registerComponent(AudioStateComponent.class, AudioStateComponent::new);
      this.forcedMusicTrackerComponentType = entityStoreRegistry.registerComponent(ForcedMusicTracker.class, ForcedMusicTracker::new);
      ComponentType<EntityStore, Player> playerComponentType = Player.getComponentType();
      ComponentType<EntityStore, PlayerRef> playerRefComponentType = PlayerRef.getComponentType();
      entityStoreRegistry.registerSystem(new ForcedMusicSystems.Tick(playerComponentType, playerRefComponentType, this.forcedMusicTrackerComponentType));
      entityStoreRegistry.registerSystem(new ForcedMusicSystems.PlayerAdded(playerRefComponentType, this.forcedMusicTrackerComponentType));
      entityStoreRegistry.registerSystem(new AudioStateSystems.Tick(playerComponentType, playerRefComponentType, this.audioStateComponentType));
      entityStoreRegistry.registerSystem(new AudioStateSystems.PlayerAdded(playerRefComponentType, this.audioStateComponentType));
      this.getCommandRegistry().registerCommand(new AudioCommands());
      EventRegistry eventRegistry = this.getEventRegistry();
      eventRegistry.register(LoadedAssetsEvent.class, AudioState.class, MusicContainer::onAudioStateLoaded);
      eventRegistry.register(LoadedAssetsEvent.class, AudioState.class, AmbienceFX::onAudioStateLoaded);
      eventRegistry.register(LoadedAssetsEvent.class, AudioState.class, SoundEvent::onAudioStateLoaded);
      eventRegistry.register(LoadedAssetsEvent.class, AudioState.class, AudioCategory::onAudioStateLoaded);
      eventRegistry.register(RemovedAssetsEvent.class, AudioState.class, MusicContainer::onAudioStateRemoved);
      eventRegistry.register(RemovedAssetsEvent.class, AudioState.class, AmbienceFX::onAudioStateRemoved);
      eventRegistry.register(RemovedAssetsEvent.class, AudioState.class, SoundEvent::onAudioStateRemoved);
      eventRegistry.register(RemovedAssetsEvent.class, AudioState.class, AudioCategory::onAudioStateRemoved);
   }

   public ComponentType<EntityStore, AudioStateComponent> getAudioStateComponentType() {
      return this.audioStateComponentType;
   }

   public ComponentType<EntityStore, ForcedMusicTracker> getForcedMusicTrackerComponentType() {
      return this.forcedMusicTrackerComponentType;
   }
}
