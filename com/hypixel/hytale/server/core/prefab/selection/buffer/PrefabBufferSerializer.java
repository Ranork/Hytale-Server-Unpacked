package com.hypixel.hytale.server.core.prefab.selection.buffer;

import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.PrefabBuffer;

public interface PrefabBufferSerializer<T> {
   T serialize(PrefabBuffer var1);
}
