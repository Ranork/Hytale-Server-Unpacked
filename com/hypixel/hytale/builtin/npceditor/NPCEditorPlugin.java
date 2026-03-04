package com.hypixel.hytale.builtin.npceditor;

import com.hypixel.hytale.builtin.asseteditor.AssetEditorPlugin;
import com.hypixel.hytale.builtin.asseteditor.event.AssetEditorSelectAssetEvent;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorPreviewCameraSettings;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorUpdateModelPreview;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.command.system.exceptions.GeneralCommandException;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderInfo;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.spawning.ISpawnableWithModel;
import com.hypixel.hytale.server.spawning.SpawningContext;
import javax.annotation.Nonnull;

public class NPCEditorPlugin extends JavaPlugin {
   private static final AssetEditorPreviewCameraSettings DEFAULT_PREVIEW_CAMERA_SETTINGS = new AssetEditorPreviewCameraSettings(
      0.25F, new Vector3f(0.0F, 75.0F, 0.0F), new Vector3f(0.0F, 0.7853F, 0.0F)
   );

   public NPCEditorPlugin(@Nonnull JavaPluginInit init) {
      super(init);
   }

   @Override
   protected void setup() {
      AssetEditorPlugin.get().getAssetTypeRegistry().registerAssetType(new NPCRoleAssetTypeHandler());
      this.getEventRegistry().register(AssetEditorSelectAssetEvent.class, NPCEditorPlugin::onSelectAsset);
   }

   private static void onSelectAsset(@Nonnull AssetEditorSelectAssetEvent event) {
      String assetType = event.getAssetType();
      if ("NPCRole".equals(assetType)) {
         NPCPlugin npcPlugin = NPCPlugin.get();
         String key = ModelAsset.getAssetStore().decodeFilePathKey(event.getAssetFilePath().path());
         int roleIndex = npcPlugin.getIndex(key);
         npcPlugin.forceValidation(roleIndex);
         BuilderInfo roleBuilderInfo = npcPlugin.getRoleBuilderInfo(roleIndex);
         if (roleBuilderInfo == null) {
            throw new IllegalStateException("Can't find a matching role builder");
         }

         if (!npcPlugin.testAndValidateRole(roleBuilderInfo)) {
            throw new GeneralCommandException(Message.translation("server.commands.npc.spawn.validation_failed"));
         }

         Builder<Role> roleBuilder = npcPlugin.tryGetCachedValidRole(roleIndex);
         if (roleBuilder == null) {
            throw new IllegalArgumentException("Can't find a matching role builder");
         }

         if (!(roleBuilder instanceof ISpawnableWithModel spawnable)) {
            return;
         }

         if (!roleBuilder.isSpawnable()) {
            return;
         }

         SpawningContext spawningContext = new SpawningContext();
         if (!spawningContext.setSpawnable(spawnable)) {
            return;
         }

         Model model = spawningContext.getModel();
         if (model == null) {
            return;
         }

         com.hypixel.hytale.protocol.Model modelPacket = model.toPacket();
         event.getEditorClient()
            .getPacketHandler()
            .write(new AssetEditorUpdateModelPreview(event.getAssetFilePath().toPacket(), modelPacket, null, DEFAULT_PREVIEW_CAMERA_SETTINGS));
      }
   }
}
