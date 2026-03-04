package com.hypixel.hytale.server.core.modules.interaction.interaction.config.none;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.protocol.CameraActionType;
import com.hypixel.hytale.protocol.CameraPerspectiveType;
import com.hypixel.hytale.protocol.Interaction;
import com.hypixel.hytale.protocol.InteractionSyncData;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import javax.annotation.Nonnull;

public class CameraInteraction extends SimpleInteraction {
   @Nonnull
   public static final BuilderCodec<CameraInteraction> CODEC = BuilderCodec.builder(CameraInteraction.class, CameraInteraction::new, SimpleInteraction.CODEC)
      .documentation("Adjusts the camera of the user.")
      .<Boolean>appendInherited(
         new KeyedCodec<>("PersistCameraState", Codec.BOOLEAN),
         (i, s) -> i.persistCameraState = s,
         i -> i.persistCameraState,
         (i, parent) -> i.persistCameraState = parent.persistCameraState
      )
      .documentation(
         "Should the camera state from this interaction persist to the next camera interaction. If the next interaction is null or not a camera interaction then this field does nothing."
      )
      .add()
      .<CameraActionType>appendInherited(
         new KeyedCodec<>("Action", new EnumCodec<>(CameraActionType.class)), (i, s) -> i.action = s, i -> i.action, (i, parent) -> i.action = parent.action
      )
      .documentation("What kind of camera action should we take")
      .add()
      .<CameraPerspectiveType>appendInherited(
         new KeyedCodec<>("Perspective", new EnumCodec<>(CameraPerspectiveType.class)),
         (i, s) -> i.perspective = s,
         i -> i.perspective,
         (i, parent) -> i.perspective = parent.perspective
      )
      .documentation("What camera perspective we want this interaction to take place in")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("CameraInteractionTime", Codec.FLOAT),
         (i, s) -> i.cameraInteractionTime = s,
         i -> i.cameraInteractionTime,
         (i, parent) -> i.cameraInteractionTime = parent.cameraInteractionTime
      )
      .documentation("How long this camera action lasts for")
      .add()
      .build();
   protected CameraActionType action;
   protected CameraPerspectiveType perspective;
   protected boolean persistCameraState;
   protected float cameraInteractionTime;

   @Nonnull
   @Override
   protected Interaction generatePacket() {
      return new com.hypixel.hytale.protocol.CameraInteraction();
   }

   @Override
   protected void configurePacket(Interaction packet) {
      super.configurePacket(packet);
      com.hypixel.hytale.protocol.CameraInteraction p = (com.hypixel.hytale.protocol.CameraInteraction)packet;
      p.cameraAction = this.action;
      p.cameraPerspective = this.perspective;
      p.cameraPersist = this.persistCameraState;
      p.cameraInteractionTime = this.cameraInteractionTime;
   }

   @Nonnull
   @Override
   public String toString() {
      return "CameraInteraction{action="
         + this.action
         + ", perspective='"
         + this.perspective
         + "', persistCameraState='"
         + this.persistCameraState
         + "', cameraInteractionTime='"
         + this.cameraInteractionTime
         + "'} "
         + super.toString();
   }

   @Override
   protected void tick0(
      boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler
   ) {
      super.tick0(firstRun, time, type, context, cooldownHandler);
      InteractionSyncData clientState = context.getClientState();

      assert clientState != null;

      context.getState().state = clientState.state;
   }

   @Nonnull
   @Override
   public WaitForDataFrom getWaitForDataFrom() {
      return WaitForDataFrom.Client;
   }

   @Override
   public boolean needsRemoteSync() {
      return true;
   }
}
