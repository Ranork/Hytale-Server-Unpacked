package com.hypixel.hytale.server.npc.util;

import javax.annotation.Nullable;

public interface IAnnotatedComponentCollection extends IAnnotatedComponent {
   int componentCount();

   @Nullable
   IAnnotatedComponent getComponent(int var1);
}
