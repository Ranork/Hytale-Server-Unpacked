package com.hypixel.hytale.server.core.modules.interaction.interaction.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.LegacyValidator;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class InteractionCameraSettings implements NetworkSerializable<com.hypixel.hytale.protocol.InteractionCameraSettings> {
   public static final BuilderCodec<InteractionCameraSettings> CODEC = BuilderCodec.builder(InteractionCameraSettings.class, InteractionCameraSettings::new)
      .appendInherited(
         new KeyedCodec<>(
            "FirstPerson", new ArrayCodec<>(InteractionCameraSettings.InteractionCamera.CODEC, InteractionCameraSettings.InteractionCamera[]::new)
         ),
         (o, i) -> o.firstPerson = i,
         o -> o.firstPerson,
         (o, p) -> o.firstPerson = p.firstPerson
      )
      .addValidator(getInteractionCameraValidator())
      .add()
      .<InteractionCameraSettings.InteractionCamera[]>appendInherited(
         new KeyedCodec<>(
            "ThirdPerson", new ArrayCodec<>(InteractionCameraSettings.InteractionCamera.CODEC, InteractionCameraSettings.InteractionCamera[]::new)
         ),
         (o, i) -> o.thirdPerson = i,
         o -> o.thirdPerson,
         (o, p) -> o.thirdPerson = p.thirdPerson
      )
      .addValidator(getInteractionCameraValidator())
      .add()
      .build();
   private InteractionCameraSettings.InteractionCamera[] firstPerson;
   private InteractionCameraSettings.InteractionCamera[] thirdPerson;

   @Nonnull
   private static LegacyValidator<InteractionCameraSettings.InteractionCamera[]> getInteractionCameraValidator() {
      return (interactionCameras, results) -> {
         if (interactionCameras != null) {
            float lastTime = -1.0F;

            for (InteractionCameraSettings.InteractionCamera entry : interactionCameras) {
               if (entry.time <= lastTime) {
                  results.fail("Camera entry with time: " + entry.time + " conflicts with another entry");
               }

               lastTime = entry.time;
            }
         }
      };
   }

   @Nonnull
   public com.hypixel.hytale.protocol.InteractionCameraSettings toPacket() {
      com.hypixel.hytale.protocol.InteractionCameraSettings packet = new com.hypixel.hytale.protocol.InteractionCameraSettings();
      if (this.firstPerson != null) {
         packet.firstPerson = new com.hypixel.hytale.protocol.InteractionCamera[this.firstPerson.length];

         for (int i = 0; i < this.firstPerson.length; i++) {
            packet.firstPerson[i] = this.firstPerson[i].toPacket();
         }
      }

      if (this.thirdPerson != null) {
         packet.thirdPerson = new com.hypixel.hytale.protocol.InteractionCamera[this.thirdPerson.length];

         for (int i = 0; i < this.thirdPerson.length; i++) {
            packet.thirdPerson[i] = this.thirdPerson[i].toPacket();
         }
      }

      return packet;
   }

   @Nonnull
   @Override
   public String toString() {
      return "InteractionCameraSettings{firstPerson="
         + Arrays.toString((Object[])this.firstPerson)
         + ", thirdPerson="
         + Arrays.toString((Object[])this.thirdPerson)
         + "}";
   }

   public static class InteractionCamera implements NetworkSerializable<com.hypixel.hytale.protocol.InteractionCamera> {
      public static final BuilderCodec<InteractionCameraSettings.InteractionCamera> CODEC = BuilderCodec.builder(
            InteractionCameraSettings.InteractionCamera.class, InteractionCameraSettings.InteractionCamera::new
         )
         .appendInherited(new KeyedCodec<>("Time", Codec.FLOAT), (o, i) -> o.time = i, o -> o.time, (o, p) -> o.time = p.time)
         .addValidator(Validators.greaterThan(0.0F))
         .add()
         .<Vector3f>appendInherited(
            new KeyedCodec<>("Position", ProtocolCodecs.VECTOR3F), (o, i) -> o.position = i, o -> o.position, (o, p) -> o.position = p.position
         )
         .addValidator(Validators.nonNull())
         .add()
         .<Direction>appendInherited(
            new KeyedCodec<>("Rotation", ProtocolCodecs.DIRECTION),
            (o, i) -> {
               o.rotation = i;
               o.rotation.yaw *= (float) (Math.PI / 180.0);
               o.rotation.pitch *= (float) (Math.PI / 180.0);
               o.rotation.roll *= (float) (Math.PI / 180.0);
            },
            o -> new Direction(
               o.rotation.yaw * (180.0F / (float)Math.PI), o.rotation.pitch * (180.0F / (float)Math.PI), o.rotation.roll * (180.0F / (float)Math.PI)
            ),
            (o, p) -> o.rotation = p.rotation
         )
         .addValidator(Validators.nonNull())
         .add()
         .build();
      private float time = 0.1F;
      private Vector3f position = new Vector3f(0.0F, 0.0F, 0.0F);
      private Direction rotation = new Direction(0.0F, 0.0F, 0.0F);

      @Nonnull
      public com.hypixel.hytale.protocol.InteractionCamera toPacket() {
         com.hypixel.hytale.protocol.InteractionCamera packet = new com.hypixel.hytale.protocol.InteractionCamera();
         packet.time = this.time;
         packet.position = this.position;
         packet.rotation = this.rotation;
         return packet;
      }

      @Nonnull
      @Override
      public String toString() {
         return "InteractionCamera{time=" + this.time + ", position=" + this.position + ", rotation=" + this.rotation + "}";
      }
   }
}
