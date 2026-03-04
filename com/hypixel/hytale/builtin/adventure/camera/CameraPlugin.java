package com.hypixel.hytale.builtin.adventure.camera;

import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.IndexedAssetMap;
import com.hypixel.hytale.builtin.adventure.camera.asset.cameraeffect.CameraShakeEffect;
import com.hypixel.hytale.builtin.adventure.camera.asset.camerashake.CameraShake;
import com.hypixel.hytale.builtin.adventure.camera.asset.camerashake.CameraShakePacketGenerator;
import com.hypixel.hytale.builtin.adventure.camera.asset.viewbobbing.ViewBobbing;
import com.hypixel.hytale.builtin.adventure.camera.asset.viewbobbing.ViewBobbingPacketGenerator;
import com.hypixel.hytale.builtin.adventure.camera.command.CameraEffectCommand;
import com.hypixel.hytale.builtin.adventure.camera.interaction.CameraShakeInteraction;
import com.hypixel.hytale.builtin.adventure.camera.system.CameraEffectSystem;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.protocol.MovementType;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.asset.type.camera.CameraEffect;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.registry.AssetRegistry;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class CameraPlugin extends JavaPlugin {
   @Nonnull
   private static final String CODEC_CAMERA_SHAKE = "CameraShake";

   public CameraPlugin(@Nonnull JavaPluginInit init) {
      super(init);
   }

   @Override
   protected void setup() {
      AssetRegistry assetRegistry = this.getAssetRegistry();
      this.getCodecRegistry(CameraEffect.CODEC).register("CameraShake", CameraShakeEffect.class, CameraShakeEffect.CODEC);
      this.getCodecRegistry(Interaction.CODEC).register("CameraShake", CameraShakeInteraction.class, CameraShakeInteraction.CODEC);
      assetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                              String.class, CameraShake.class, new IndexedAssetMap()
                           )
                           .loadsBefore(CameraEffect.class))
                        .setCodec(CameraShake.CODEC))
                     .setPath("Camera/CameraShake"))
                  .setKeyFunction(CameraShake::getId))
               .setReplaceOnRemove(CameraShake::new))
            .setPacketGenerator(new CameraShakePacketGenerator())
            .build()
      );
      assetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                        MovementType.class, ViewBobbing.class, new DefaultAssetMap()
                     )
                     .setCodec(ViewBobbing.CODEC))
                  .setPath("Camera/ViewBobbing"))
               .setKeyFunction(ViewBobbing::getId))
            .setPacketGenerator(new ViewBobbingPacketGenerator())
            .build()
      );
      this.getCommandRegistry().registerCommand(new CameraEffectCommand());
      ComponentType<EntityStore, PlayerRef> playerRefComponentType = PlayerRef.getComponentType();
      ComponentType<EntityStore, EntityStatMap> entityStatMapComponentType = EntityStatMap.getComponentType();
      this.getEntityStoreRegistry().registerSystem(new CameraEffectSystem(playerRefComponentType, entityStatMapComponentType));
   }
}
