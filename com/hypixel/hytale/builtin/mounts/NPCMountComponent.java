package com.hypixel.hytale.builtin.mounts;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NPCMountComponent implements Component<EntityStore> {
   public static final BuilderCodec<NPCMountComponent> CODEC = BuilderCodec.builder(NPCMountComponent.class, NPCMountComponent::new)
      .append(
         new KeyedCodec<>("OriginalRoleIndex", Codec.INTEGER),
         (mountComponent, integer) -> mountComponent.originalRoleIndex = integer,
         mountComponent -> mountComponent.originalRoleIndex
      )
      .add()
      .build();
   private int originalRoleIndex;
   @Nullable
   private PlayerRef ownerPlayerRef;
   private float anchorX;
   private float anchorY;
   private float anchorZ;

   public static ComponentType<EntityStore, NPCMountComponent> getComponentType() {
      return MountPlugin.getInstance().getMountComponentType();
   }

   public int getOriginalRoleIndex() {
      return this.originalRoleIndex;
   }

   public void setOriginalRoleIndex(int originalRoleIndex) {
      this.originalRoleIndex = originalRoleIndex;
   }

   @Nullable
   public PlayerRef getOwnerPlayerRef() {
      return this.ownerPlayerRef;
   }

   public void setOwnerPlayerRef(PlayerRef ownerPlayerRef) {
      this.ownerPlayerRef = ownerPlayerRef;
   }

   public float getAnchorX() {
      return this.anchorX;
   }

   public float getAnchorY() {
      return this.anchorY;
   }

   public float getAnchorZ() {
      return this.anchorZ;
   }

   public void setAnchor(float x, float y, float z) {
      this.anchorX = x;
      this.anchorY = y;
      this.anchorZ = z;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      NPCMountComponent component = new NPCMountComponent();
      component.originalRoleIndex = this.originalRoleIndex;
      component.ownerPlayerRef = this.ownerPlayerRef;
      component.anchorX = this.anchorX;
      component.anchorY = this.anchorY;
      component.anchorZ = this.anchorZ;
      return component;
   }
}
