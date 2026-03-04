package com.hypixel.hytale.server.core.universe.world;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec.Builder;
import com.hypixel.hytale.math.shape.Box2D;
import com.hypixel.hytale.math.vector.Vector2d;
import javax.annotation.Nullable;

public class WorldConfig$ChunkConfig {
    public static final BuilderCodec<WorldConfig$ChunkConfig> CODEC = ((Builder)((Builder)((Builder)BuilderCodec.builder(
                        WorldConfig$ChunkConfig.class, WorldConfig$ChunkConfig::new
                    )
                    .appendInherited(
                        new KeyedCodec("PregenerateRegion", Box2D.CODEC),
                        (o, i) -> o.pregenerateRegion = i,
                        o -> o.pregenerateRegion,
                        (o, p) -> o.pregenerateRegion = p.pregenerateRegion
                    )
                    .documentation(
                        "Sets the region that will be pregenerated for the world.\n\nIf set, the specified region will be pregenerated when the world starts."
                    )
                    .add())
                .appendInherited(
                    new KeyedCodec("KeepLoadedRegion", Box2D.CODEC),
                    (o, i) -> o.keepLoadedRegion = i,
                    o -> o.keepLoadedRegion,
                    (o, p) -> o.keepLoadedRegion = p.keepLoadedRegion
                )
                .documentation("Sets a region of chunks that will never be unloaded.")
                .add())
            .afterDecode(o -> {
                if (o.pregenerateRegion != null) {
                    o.pregenerateRegion.normalize();
                }
        
                if (o.keepLoadedRegion != null) {
                    o.keepLoadedRegion.normalize();
                }
            }))
        .build();
    private static final Box2D DEFAULT_PREGENERATE_REGION = new Box2D(new Vector2d(-512.0, -512.0), new Vector2d(512.0, 512.0));
    @Nullable
    private Box2D pregenerateRegion;
    @Nullable
    private Box2D keepLoadedRegion;

    @Nullable
    public Box2D getPregenerateRegion() {
        return this.pregenerateRegion;
    }

    public void setPregenerateRegion(@Nullable Box2D pregenerateRegion) {
        if (pregenerateRegion != null) {
            pregenerateRegion.normalize();
        }

        this.pregenerateRegion = pregenerateRegion;
    }

    @Nullable
    public Box2D getKeepLoadedRegion() {
        return this.keepLoadedRegion;
    }

    public void setKeepLoadedRegion(@Nullable Box2D keepLoadedRegion) {
        if (keepLoadedRegion != null) {
            keepLoadedRegion.normalize();
        }

        this.keepLoadedRegion = keepLoadedRegion;
    }
}