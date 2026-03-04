package com.hypixel.hytale.builtin.adventure.teleporter.page;

import com.hypixel.hytale.builtin.adventure.teleporter.component.Teleporter;
import com.hypixel.hytale.builtin.adventure.teleporter.system.CreateWarpWhenTeleporterPlacedSystem;
import com.hypixel.hytale.builtin.adventure.teleporter.system.TurnOffTeleportersSystem;
import com.hypixel.hytale.builtin.adventure.teleporter.util.CannedWarpNames;
import com.hypixel.hytale.builtin.teleport.TeleportPlugin;
import com.hypixel.hytale.builtin.teleport.Warp;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class TeleporterSettingsPage extends InteractiveCustomUIPage<TeleporterSettingsPage.PageEventData> {
   @Nonnull
   private final Ref<ChunkStore> blockRef;
   private final TeleporterSettingsPage.Mode mode;

   public TeleporterSettingsPage(@Nonnull PlayerRef playerRef, @Nonnull Ref<ChunkStore> blockRef, TeleporterSettingsPage.Mode mode) {
      super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, TeleporterSettingsPage.PageEventData.CODEC);
      this.blockRef = blockRef;
      this.mode = mode;
   }

   @Override
   public void build(
      @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
   ) {
      String language = this.playerRef.getLanguage();
      Teleporter teleporter = this.blockRef.getStore().getComponent(this.blockRef, Teleporter.getComponentType());
      commandBuilder.append("Pages/Teleporter.ui");
      if (teleporter == null) {
         commandBuilder.set("#ErrorScreen.Visible", true);
         commandBuilder.set("#FullSettings.Visible", false);
         commandBuilder.set("#WarpSettings.Visible", false);
         commandBuilder.set("#Buttons.Visible", false);
      } else {
         commandBuilder.set("#ErrorScreen.Visible", false);
         commandBuilder.set("#FullSettings.Visible", this.mode == TeleporterSettingsPage.Mode.FULL);
         switch (this.mode) {
            case FULL:
               byte relativeMask = teleporter.getRelativeMask();
               commandBuilder.set("#BlockRelative #CheckBox.Value", (relativeMask & 64) != 0);
               Transform transform = teleporter.getTransform();
               if (transform != null) {
                  commandBuilder.set("#X #Input.Value", transform.getPosition().getX());
                  commandBuilder.set("#Y #Input.Value", transform.getPosition().getY());
                  commandBuilder.set("#Z #Input.Value", transform.getPosition().getZ());
               }

               commandBuilder.set("#X #CheckBox.Value", (relativeMask & 1) != 0);
               commandBuilder.set("#Y #CheckBox.Value", (relativeMask & 2) != 0);
               commandBuilder.set("#Z #CheckBox.Value", (relativeMask & 4) != 0);
               if (transform != null) {
                  commandBuilder.set("#Yaw #Input.Value", transform.getRotation().getYaw());
                  commandBuilder.set("#Pitch #Input.Value", transform.getRotation().getPitch());
                  commandBuilder.set("#Roll #Input.Value", transform.getRotation().getRoll());
               }

               commandBuilder.set("#Yaw #CheckBox.Value", (relativeMask & 8) != 0);
               commandBuilder.set("#Pitch #CheckBox.Value", (relativeMask & 16) != 0);
               commandBuilder.set("#Roll #CheckBox.Value", (relativeMask & 32) != 0);
               ObjectArrayList<DropdownEntryInfo> worlds = new ObjectArrayList();
               worlds.add(new DropdownEntryInfo(LocalizableString.fromMessageId("server.customUI.teleporter.noWorld"), ""));

               for (World world : Universe.get().getWorlds().values()) {
                  worlds.add(new DropdownEntryInfo(LocalizableString.fromString(world.getName()), world.getWorldConfig().getUuid().toString()));
               }

               commandBuilder.set("#WorldDropdown.Entries", worlds);
               UUID worldUuid = teleporter.getWorldUuid();
               commandBuilder.set("#WorldDropdown.Value", worldUuid != null ? worldUuid.toString() : "");
               commandBuilder.set("#WarpDropdown.Entries", getWarpsSortedById(teleporter.getOwnedWarp(), null));
               commandBuilder.set("#WarpDropdown.Value", teleporter.getWarp() != null ? teleporter.getWarp() : "");
               commandBuilder.set("#NewWarp.Value", teleporter.getOwnedWarp() != null ? teleporter.getOwnedWarp() : "");
               eventBuilder.addEventBinding(
                  CustomUIEventBindingType.Activating,
                  "#SaveButton",
                  new EventData()
                     .append("@BlockRelative", "#BlockRelative #CheckBox.Value")
                     .append("@X", "#X #Input.Value")
                     .append("@Y", "#Y #Input.Value")
                     .append("@Z", "#Z #Input.Value")
                     .append("@XIsRelative", "#X #CheckBox.Value")
                     .append("@YIsRelative", "#Y #CheckBox.Value")
                     .append("@ZIsRelative", "#Z #CheckBox.Value")
                     .append("@Yaw", "#Yaw #Input.Value")
                     .append("@Pitch", "#Pitch #Input.Value")
                     .append("@Roll", "#Roll #Input.Value")
                     .append("@YawIsRelative", "#Yaw #CheckBox.Value")
                     .append("@PitchIsRelative", "#Pitch #CheckBox.Value")
                     .append("@RollIsRelative", "#Roll #CheckBox.Value")
                     .append("@World", "#WorldDropdown.Value")
                     .append("@Warp", "#WarpDropdown.Value")
                     .append("@NewWarp", "#NewWarp.Value")
               );
               break;
            case WARP:
               commandBuilder.set("#WarpDropdown.Entries", getWarpsSortedById(teleporter.getOwnedWarp(), store.getExternalData().getWorld().getName()));
               commandBuilder.set("#WarpDropdown.Value", teleporter.getWarp() != null ? teleporter.getWarp() : "");
               Message placeholder;
               if (teleporter.hasOwnedWarp() && !teleporter.isCustomName()) {
                  placeholder = Message.translation(teleporter.getOwnedWarp());
               } else {
                  String cannedName = CannedWarpNames.generateCannedWarpNameKey(this.blockRef, language);
                  placeholder = cannedName == null ? Message.translation("server.customUI.teleporter.warpName") : Message.translation(cannedName);
               }

               commandBuilder.set("#NewWarp.PlaceholderText", placeholder);
               String value = teleporter.isCustomName() && teleporter.getOwnedWarp() != null ? teleporter.getOwnedWarp() : "";
               commandBuilder.set("#NewWarp.Value", value);
               eventBuilder.addEventBinding(
                  CustomUIEventBindingType.Activating,
                  "#SaveButton",
                  new EventData().append("@Warp", "#WarpDropdown.Value").append("@NewWarp", "#NewWarp.Value")
               );
         }
      }
   }

   static List<DropdownEntryInfo> getWarpsSortedById(@NullableDecl String ownedWarpId, @NullableDecl String worldNameToFilter) {
      List<DropdownEntryInfo> warps = new ObjectArrayList();
      warps.add(new DropdownEntryInfo(LocalizableString.fromMessageId("server.customUI.teleporter.noWarp"), ""));
      ObjectArrayList<Warp> sortedWarps = new ObjectArrayList(TeleportPlugin.get().getWarps().values());
      sortedWarps.sort((a, b) -> a.getId().compareToIgnoreCase(b.getId()));
      ObjectListIterator var4 = sortedWarps.iterator();

      while (var4.hasNext()) {
         Warp warp = (Warp)var4.next();
         if ((worldNameToFilter == null || warp.getWorld().equals(worldNameToFilter)) && !warp.getId().equalsIgnoreCase(ownedWarpId)) {
            warps.add(new DropdownEntryInfo(LocalizableString.fromString(warp.getId()), warp.getId().toLowerCase()));
         }
      }

      return warps;
   }

   public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull TeleporterSettingsPage.PageEventData data) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         String language = this.playerRef.getLanguage();
         BlockModule.BlockStateInfo blockStateInfo = this.blockRef.getStore().getComponent(this.blockRef, BlockModule.BlockStateInfo.getComponentType());
         if (blockStateInfo == null) {
            playerComponent.getPageManager().setPage(ref, store, Page.None);
         } else {
            Ref<ChunkStore> chunkRef = blockStateInfo.getChunkRef();
            if (!chunkRef.isValid()) {
               playerComponent.getPageManager().setPage(ref, store, Page.None);
            } else {
               WorldChunk worldChunkComponent = chunkRef.getStore().getComponent(chunkRef, WorldChunk.getComponentType());

               assert worldChunkComponent != null;

               Teleporter teleporterComponent = this.blockRef.getStore().getComponent(this.blockRef, Teleporter.getComponentType());
               if (teleporterComponent == null) {
                  playerComponent.getPageManager().setPage(ref, store, Page.None);
               } else {
                  String oldOwnedWarp = teleporterComponent.getOwnedWarp();
                  boolean customName = true;
                  if (data.warpName == null || data.warpName.isEmpty()) {
                     if (oldOwnedWarp == null) {
                        data.warpName = CannedWarpNames.generateCannedWarpName(this.blockRef, language);
                        customName = false;
                     } else {
                        data.warpName = oldOwnedWarp;
                        customName = teleporterComponent.isCustomName();
                     }

                     if (data.warpName == null) {
                        UICommandBuilder commandBuilder = new UICommandBuilder();
                        commandBuilder.set("#NewWarp.PlaceholderText", Message.translation("server.customUI.teleporter.warpNameRightHereHint"));
                        commandBuilder.set("#ErrorLabel.Text", Message.translation("server.customUI.teleporter.errorMissingWarpName"));
                        commandBuilder.set("#ErrorLabel.Visible", true);
                        this.sendUpdate(commandBuilder);
                        return;
                     }
                  }

                  if (!data.warpName.equalsIgnoreCase(oldOwnedWarp)) {
                     boolean alreadyExists = TeleportPlugin.get().getWarps().containsKey(data.warpName.toLowerCase());
                     if (alreadyExists) {
                        UICommandBuilder commandBuilder = new UICommandBuilder();
                        commandBuilder.set("#ErrorLabel.Text", Message.translation("server.customUI.teleporter.errorWarpAlreadyExists"));
                        commandBuilder.set("#ErrorLabel.Visible", true);
                        this.sendUpdate(commandBuilder);
                        return;
                     }
                  }

                  if (oldOwnedWarp != null && !oldOwnedWarp.isEmpty()) {
                     TeleportPlugin.get().getWarps().remove(oldOwnedWarp.toLowerCase());
                  }

                  playerComponent.getPageManager().setPage(ref, store, Page.None);
                  String ownedWarpBefore = teleporterComponent.getOwnedWarp();
                  String destinationWarpBefore = teleporterComponent.getWarp();
                  CreateWarpWhenTeleporterPlacedSystem.createWarp(worldChunkComponent, blockStateInfo, data.warpName);
                  teleporterComponent.setOwnedWarp(data.warpName);
                  teleporterComponent.setIsCustomName(customName);
                  switch (this.mode) {
                     case FULL:
                        teleporterComponent.setWorldUuid(data.world != null && !data.world.isEmpty() ? UUID.fromString(data.world) : null);
                        Transform transform = new Transform();
                        transform.getPosition().setX(data.x);
                        transform.getPosition().setY(data.y);
                        transform.getPosition().setZ(data.z);
                        transform.getRotation().setYaw(data.yaw);
                        transform.getRotation().setPitch(data.pitch);
                        transform.getRotation().setRoll(data.roll);
                        teleporterComponent.setTransform(transform);
                        teleporterComponent.setRelativeMask(
                           (byte)(
                              (data.xIsRelative ? 1 : 0)
                                 | (data.yIsRelative ? 2 : 0)
                                 | (data.zIsRelative ? 4 : 0)
                                 | (data.yawIsRelative ? 8 : 0)
                                 | (data.pitchIsRelative ? 16 : 0)
                                 | (data.rollIsRelative ? 32 : 0)
                                 | (data.isBlockRelative ? 64 : 0)
                           )
                        );
                        teleporterComponent.setWarp(data.destinationWarp != null && !data.destinationWarp.isEmpty() ? data.destinationWarp : null);
                        break;
                     case WARP:
                        teleporterComponent.setWorldUuid(null);
                        teleporterComponent.setTransform(null);
                        teleporterComponent.setWarp(data.destinationWarp != null && !data.destinationWarp.isEmpty() ? data.destinationWarp : null);
                  }

                  boolean ownChanged = !Objects.equals(ownedWarpBefore, teleporterComponent.getOwnedWarp());
                  boolean destinationChanged = !Objects.equals(destinationWarpBefore, teleporterComponent.getWarp());
                  if (ownChanged || destinationChanged) {
                     World world = store.getExternalData().getWorld();
                     TurnOffTeleportersSystem.updatePortalBlocksInWorld(world);
                  }
               }
            }
         }
      }
   }

   public static enum Mode {
      FULL,
      WARP;

      @Nonnull
      public static final Codec<TeleporterSettingsPage.Mode> CODEC = new EnumCodec<>(TeleporterSettingsPage.Mode.class);
   }

   public static class PageEventData {
      @Nonnull
      public static final String KEY_BLOCK_RELATIVE = "@BlockRelative";
      @Nonnull
      public static final String KEY_X = "@X";
      @Nonnull
      public static final String KEY_Y = "@Y";
      @Nonnull
      public static final String KEY_Z = "@Z";
      @Nonnull
      public static final String KEY_X_IS_RELATIVE = "@XIsRelative";
      @Nonnull
      public static final String KEY_Y_IS_RELATIVE = "@YIsRelative";
      @Nonnull
      public static final String KEY_Z_IS_RELATIVE = "@ZIsRelative";
      @Nonnull
      public static final String KEY_YAW = "@Yaw";
      @Nonnull
      public static final String KEY_PITCH = "@Pitch";
      @Nonnull
      public static final String KEY_ROLL = "@Roll";
      @Nonnull
      public static final String KEY_YAW_IS_RELATIVE = "@YawIsRelative";
      @Nonnull
      public static final String KEY_PITCH_IS_RELATIVE = "@PitchIsRelative";
      @Nonnull
      public static final String KEY_ROLL_IS_RELATIVE = "@RollIsRelative";
      @Nonnull
      public static final String KEY_WORLD = "@World";
      @Nonnull
      public static final String KEY_WARP = "@Warp";
      @Nonnull
      public static final String KEY_NEW_WARP = "@NewWarp";
      @Nonnull
      public static final BuilderCodec<TeleporterSettingsPage.PageEventData> CODEC = BuilderCodec.builder(
            TeleporterSettingsPage.PageEventData.class, TeleporterSettingsPage.PageEventData::new
         )
         .append(
            new KeyedCodec<>("@BlockRelative", Codec.BOOLEAN),
            (pageEventData, o) -> pageEventData.isBlockRelative = o,
            pageEventData -> pageEventData.isBlockRelative
         )
         .add()
         .append(new KeyedCodec<>("@X", Codec.DOUBLE), (pageEventData, o) -> pageEventData.x = o, pageEventData -> pageEventData.x)
         .add()
         .append(new KeyedCodec<>("@Y", Codec.DOUBLE), (pageEventData, o) -> pageEventData.y = o, pageEventData -> pageEventData.y)
         .add()
         .append(new KeyedCodec<>("@Z", Codec.DOUBLE), (pageEventData, o) -> pageEventData.z = o, pageEventData -> pageEventData.z)
         .add()
         .append(
            new KeyedCodec<>("@XIsRelative", Codec.BOOLEAN), (pageEventData, o) -> pageEventData.xIsRelative = o, pageEventData -> pageEventData.xIsRelative
         )
         .add()
         .append(
            new KeyedCodec<>("@YIsRelative", Codec.BOOLEAN), (pageEventData, o) -> pageEventData.yIsRelative = o, pageEventData -> pageEventData.yIsRelative
         )
         .add()
         .append(
            new KeyedCodec<>("@ZIsRelative", Codec.BOOLEAN), (pageEventData, o) -> pageEventData.zIsRelative = o, pageEventData -> pageEventData.zIsRelative
         )
         .add()
         .append(new KeyedCodec<>("@Yaw", Codec.FLOAT), (pageEventData, o) -> pageEventData.yaw = o, pageEventData -> pageEventData.yaw)
         .add()
         .append(new KeyedCodec<>("@Pitch", Codec.FLOAT), (pageEventData, o) -> pageEventData.pitch = o, pageEventData -> pageEventData.pitch)
         .add()
         .append(new KeyedCodec<>("@Roll", Codec.FLOAT), (pageEventData, o) -> pageEventData.roll = o, pageEventData -> pageEventData.roll)
         .add()
         .append(
            new KeyedCodec<>("@YawIsRelative", Codec.BOOLEAN),
            (pageEventData, o) -> pageEventData.yawIsRelative = o,
            pageEventData -> pageEventData.yawIsRelative
         )
         .add()
         .append(
            new KeyedCodec<>("@PitchIsRelative", Codec.BOOLEAN),
            (pageEventData, o) -> pageEventData.pitchIsRelative = o,
            pageEventData -> pageEventData.pitchIsRelative
         )
         .add()
         .append(
            new KeyedCodec<>("@RollIsRelative", Codec.BOOLEAN),
            (pageEventData, o) -> pageEventData.rollIsRelative = o,
            pageEventData -> pageEventData.pitchIsRelative
         )
         .add()
         .append(new KeyedCodec<>("@World", Codec.STRING), (pageEventData, o) -> pageEventData.world = o, pageEventData -> pageEventData.world)
         .add()
         .append(
            new KeyedCodec<>("@Warp", Codec.STRING), (pageEventData, o) -> pageEventData.destinationWarp = o, pageEventData -> pageEventData.destinationWarp
         )
         .add()
         .append(new KeyedCodec<>("@NewWarp", Codec.STRING), (pageEventData, o) -> pageEventData.warpName = o, pageEventData -> pageEventData.warpName)
         .add()
         .build();
      public boolean isBlockRelative;
      public double x;
      public double y;
      public double z;
      public boolean xIsRelative;
      public boolean yIsRelative;
      public boolean zIsRelative;
      public float yaw;
      public float pitch;
      public float roll;
      public boolean yawIsRelative;
      public boolean pitchIsRelative;
      public boolean rollIsRelative;
      public String world;
      public String destinationWarp;
      @Nullable
      public String warpName;
   }
}
