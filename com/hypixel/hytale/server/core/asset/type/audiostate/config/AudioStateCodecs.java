package com.hypixel.hytale.server.core.asset.type.audiostate.config;

import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.protocol.AudioStateAuthority;
import com.hypixel.hytale.protocol.FadeCurve;
import com.hypixel.hytale.protocol.SyncPoint;

public final class AudioStateCodecs {
   public static final EnumCodec<FadeCurve> FADE_CURVE = new EnumCodec<>(FadeCurve.class);
   public static final EnumCodec<SyncPoint> SYNC_POINT = new EnumCodec<>(SyncPoint.class);
   public static final EnumCodec<AudioStateAuthority> AUTHORITY = new EnumCodec<>(AudioStateAuthority.class);

   private AudioStateCodecs() {
   }
}
