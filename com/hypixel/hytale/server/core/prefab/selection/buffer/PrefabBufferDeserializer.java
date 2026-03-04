package com.hypixel.hytale.server.core.prefab.selection.buffer;

import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.PrefabBuffer;
import java.nio.file.Path;

public interface PrefabBufferDeserializer<T> {
   PrefabBuffer deserialize(Path var1, T var2);
}
