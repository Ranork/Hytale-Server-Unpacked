package com.hypixel.hytale.builtin.mounts;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.BlockMountType;
import com.hypixel.hytale.protocol.MountController;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MountedComponent implements Component<EntityStore> {
   private Ref<EntityStore> mountedToEntity;
   private Ref<ChunkStore> mountedToBlock;
   private MountController controller;
   private BlockMountType blockMountType;
   private Vector3f attachmentOffset = new Vector3f(0.0F, 0.0F, 0.0F);
   private long mountStartMs;
   private boolean isNetworkOutdated = true;

   public static ComponentType<EntityStore, MountedComponent> getComponentType() {
      return MountPlugin.getInstance().getMountedComponentType();
   }

   public MountedComponent(Ref<EntityStore> mountedToEntity, Vector3f attachmentOffset, MountController controller) {
      this.mountedToEntity = mountedToEntity;
      this.attachmentOffset = attachmentOffset;
      this.controller = controller;
      this.mountStartMs = System.currentTimeMillis();
   }

   public MountedComponent(Ref<ChunkStore> mountedToBlock, Vector3f attachmentOffset, BlockMountType blockMountType) {
      this.mountedToBlock = mountedToBlock;
      this.attachmentOffset = attachmentOffset;
      this.controller = MountController.BlockMount;
      this.blockMountType = blockMountType;
      this.mountStartMs = System.currentTimeMillis();
   }

   @Nullable
   public Ref<EntityStore> getMountedToEntity() {
      return this.mountedToEntity;
   }

   @Nullable
   public Ref<ChunkStore> getMountedToBlock() {
      return this.mountedToBlock;
   }

   public Vector3f getAttachmentOffset() {
      return this.attachmentOffset;
   }

   public MountController getControllerType() {
      return this.controller;
   }

   public BlockMountType getBlockMountType() {
      return this.blockMountType;
   }

   public long getMountedDurationMs() {
      return System.currentTimeMillis() - this.mountStartMs;
   }

   public boolean consumeNetworkOutdated() {
      boolean tmp = this.isNetworkOutdated;
      this.isNetworkOutdated = false;
      return tmp;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new MountedComponent(this.mountedToEntity, this.attachmentOffset, this.controller);
   }
}
