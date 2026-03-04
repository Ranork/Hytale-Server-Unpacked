package com.hypixel.hytale.server.core.prefab.selection.buffer;

import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import java.util.Random;

public class PrefabBufferCall {
   public Random random;
   public PrefabRotation rotation;

   public PrefabBufferCall() {
   }

   public PrefabBufferCall(Random random, PrefabRotation rotation) {
      this.random = random;
      this.rotation = rotation;
   }
}
