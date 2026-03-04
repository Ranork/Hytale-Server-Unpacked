package com.hypixel.hytale.builtin.portals.components;

import com.hypixel.hytale.builtin.portals.PortalsPlugin;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PortalDevice implements Component<ChunkStore> {
   @Nonnull
   public static final BuilderCodec<PortalDevice> CODEC = BuilderCodec.builder(PortalDevice.class, PortalDevice::new)
      .append(new KeyedCodec<>("Config", PortalDeviceConfig.CODEC), (portal, o) -> portal.config = o, portal -> portal.config)
      .add()
      .append(new KeyedCodec<>("BaseBlockType", Codec.STRING), (portal, o) -> portal.baseBlockTypeKey = o, portal -> portal.baseBlockTypeKey)
      .add()
      .append(new KeyedCodec<>("DestinationWorld", Codec.UUID_BINARY), (portal, o) -> portal.destinationWorldUuid = o, portal -> portal.destinationWorldUuid)
      .add()
      .build();
   private PortalDeviceConfig config;
   private String baseBlockTypeKey;
   private UUID destinationWorldUuid;

   public static ComponentType<ChunkStore, PortalDevice> getComponentType() {
      return PortalsPlugin.getInstance().getPortalDeviceComponentType();
   }

   private PortalDevice() {
   }

   public PortalDevice(PortalDeviceConfig config, String baseBlockTypeKey) {
      this.config = config;
      this.baseBlockTypeKey = baseBlockTypeKey;
   }

   public PortalDeviceConfig getConfig() {
      return this.config;
   }

   public String getBaseBlockTypeKey() {
      return this.baseBlockTypeKey;
   }

   @Nullable
   public BlockType getBaseBlockType() {
      return BlockType.getAssetMap().getAsset(this.baseBlockTypeKey);
   }

   @Nullable
   public UUID getDestinationWorldUuid() {
      return this.destinationWorldUuid;
   }

   @Nullable
   public World getDestinationWorld() {
      if (this.destinationWorldUuid == null) {
         return null;
      } else {
         World world = Universe.get().getWorld(this.destinationWorldUuid);
         return world != null && world.isAlive() ? world : null;
      }
   }

   public void setDestinationWorld(@Nonnull World world) {
      this.destinationWorldUuid = world.getWorldConfig().getUuid();
   }

   @Override
   public Component<ChunkStore> clone() {
      PortalDevice portal = new PortalDevice();
      portal.config = this.config;
      portal.baseBlockTypeKey = this.baseBlockTypeKey;
      portal.destinationWorldUuid = this.destinationWorldUuid;
      return portal;
   }
}
