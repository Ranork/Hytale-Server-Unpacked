package com.hypixel.hytale.server.core.asset.type.blocktype.config;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.VariantRotation.1;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.annotation.Nonnull;

public enum VariantRotation implements NetworkSerializable<com.hypixel.hytale.protocol.VariantRotation> {
    None(
        com.hypixel.hytale.protocol.VariantRotation.None,
        RotationTuple.EMPTY_ARRAY,
        pair -> RotationTuple.NONE,
        (pair, rotation) -> RotationTuple.NONE,
        (pair, rotation) -> RotationTuple.NONE
    ),
    Wall(
        com.hypixel.hytale.protocol.VariantRotation.Wall,
        new RotationTuple[]{RotationTuple.of(Rotation.Ninety, Rotation.None)},
        pair -> pair.yaw() != Rotation.Ninety && pair.yaw() != Rotation.TwoSeventy
                ? RotationTuple.of(Rotation.None, Rotation.None)
                : RotationTuple.of(Rotation.Ninety, Rotation.None),
        (pair, rotation) -> pair,
        (pair, rotation) -> pair
    ),
    UpDown(
        com.hypixel.hytale.protocol.VariantRotation.UpDown,
        new RotationTuple[]{RotationTuple.of(Rotation.None, Rotation.OneEighty)},
        pair -> pair.pitch() == Rotation.OneEighty ? RotationTuple.of(Rotation.None, Rotation.OneEighty) : RotationTuple.of(Rotation.None, Rotation.None),
        (pair, rotation) -> RotationTuple.of(pair.yaw(), pair.pitch().add(rotation)),
        (pair, rotation) -> pair.pitch().add(rotation) == Rotation.OneEighty
                ? RotationTuple.of(pair.yaw(), Rotation.OneEighty)
                : RotationTuple.of(pair.yaw(), Rotation.None)
    ),
    Pipe(
        com.hypixel.hytale.protocol.VariantRotation.Pipe,
        new RotationTuple[]{RotationTuple.of(Rotation.None, Rotation.Ninety), RotationTuple.of(Rotation.Ninety, Rotation.Ninety)},
        pair -> pair.pitch() != Rotation.Ninety && pair.pitch() != Rotation.TwoSeventy
                ? RotationTuple.of(Rotation.None, validatePipe(pair.pitch()))
                : RotationTuple.of(validatePipe(pair.yaw()), validatePipe(pair.pitch())),
        (pair, rotation) -> RotationTuple.of(pair.yaw(), pair.pitch().add(rotation)),
        (pair, rotation) -> {
            if (pair.yaw() == Rotation.None && pair.pitch() == Rotation.Ninety) {
                return pair;
            } else {
                return switch (1.$SwitchMap$com$hypixel$hytale$server$core$asset$type$blocktype$config$Rotation[pair.yaw().add(rotation).ordinal()]) {
                    case 1, 2 -> RotationTuple.of(Rotation.None, Rotation.None);
                    case 3, 4 -> RotationTuple.of(Rotation.Ninety, Rotation.Ninety);
                    default -> throw new MatchException(null, null);
                };
            }
        }
    ),
    DoublePipe(
        com.hypixel.hytale.protocol.VariantRotation.DoublePipe,
        new RotationTuple[]{
            RotationTuple.of(Rotation.None, Rotation.Ninety),
            RotationTuple.of(Rotation.Ninety, Rotation.Ninety),
            RotationTuple.of(Rotation.OneEighty, Rotation.Ninety),
            RotationTuple.of(Rotation.TwoSeventy, Rotation.Ninety),
            RotationTuple.of(Rotation.None, Rotation.OneEighty)
        },
        pair -> {
            return switch (1.$SwitchMap$com$hypixel$hytale$server$core$asset$type$blocktype$config$Rotation[pair.pitch().ordinal()]) {
                case 2 -> RotationTuple.of(Rotation.None, Rotation.OneEighty);
                case 3 -> pair;
                case 4 -> RotationTuple.of(pair.yaw().flip(), Rotation.Ninety);
                default -> RotationTuple.NONE;
            };
        },
        (pair, rotation) -> (pair.yaw() == Rotation.Ninety || pair.yaw() == Rotation.TwoSeventy) && pair.pitch() == Rotation.Ninety
                ? pair
                : RotationTuple.getRotation(
                    new RotationTuple[]{
                        RotationTuple.NONE,
                        RotationTuple.of(Rotation.None, Rotation.Ninety),
                        RotationTuple.of(Rotation.None, Rotation.OneEighty),
                        RotationTuple.of(Rotation.OneEighty, Rotation.Ninety)
                    },
                    pair,
                    rotation
                ),
        (pair, rotation) -> pair.yaw() != Rotation.None || pair.pitch() != Rotation.Ninety && pair.pitch() != Rotation.TwoSeventy
                ? RotationTuple.getRotation(
                    new RotationTuple[]{
                        RotationTuple.NONE,
                        RotationTuple.of(Rotation.Ninety, Rotation.Ninety),
                        RotationTuple.of(Rotation.None, Rotation.OneEighty),
                        RotationTuple.of(Rotation.TwoSeventy, Rotation.Ninety)
                    },
                    pair,
                    rotation
                )
                : pair
    ),
    NESW(
        com.hypixel.hytale.protocol.VariantRotation.NESW,
        new RotationTuple[]{
            RotationTuple.of(Rotation.Ninety, Rotation.None),
            RotationTuple.of(Rotation.OneEighty, Rotation.None),
            RotationTuple.of(Rotation.TwoSeventy, Rotation.None)
        },
        pair -> RotationTuple.of(pair.yaw(), Rotation.None),
        (pair, rotation) -> pair,
        (pair, rotation) -> pair
    ),
    UpDownNESW(
        com.hypixel.hytale.protocol.VariantRotation.UpDownNESW,
        new RotationTuple[]{
            RotationTuple.of(Rotation.Ninety, Rotation.None),
            RotationTuple.of(Rotation.OneEighty, Rotation.None),
            RotationTuple.of(Rotation.TwoSeventy, Rotation.None),
            RotationTuple.of(Rotation.None, Rotation.OneEighty),
            RotationTuple.of(Rotation.Ninety, Rotation.OneEighty),
            RotationTuple.of(Rotation.OneEighty, Rotation.OneEighty),
            RotationTuple.of(Rotation.TwoSeventy, Rotation.OneEighty)
        },
        pair -> pair.pitch() == Rotation.OneEighty ? RotationTuple.of(pair.yaw(), Rotation.OneEighty) : RotationTuple.of(pair.yaw(), Rotation.None),
        (pair, rotation) -> RotationTuple.of(pair.yaw(), pair.pitch().add(rotation)),
        (pair, rotation) -> pair.pitch().add(rotation) == Rotation.OneEighty ? RotationTuple.of(pair.yaw(), Rotation.OneEighty) : pair
    ),
    Debug(
        com.hypixel.hytale.protocol.VariantRotation.UpDownNESW,
        new RotationTuple[]{
            RotationTuple.of(Rotation.Ninety, Rotation.None),
            RotationTuple.of(Rotation.OneEighty, Rotation.None),
            RotationTuple.of(Rotation.TwoSeventy, Rotation.None),
            RotationTuple.of(Rotation.None, Rotation.Ninety),
            RotationTuple.of(Rotation.Ninety, Rotation.Ninety),
            RotationTuple.of(Rotation.OneEighty, Rotation.Ninety),
            RotationTuple.of(Rotation.TwoSeventy, Rotation.Ninety),
            RotationTuple.of(Rotation.None, Rotation.OneEighty),
            RotationTuple.of(Rotation.Ninety, Rotation.OneEighty),
            RotationTuple.of(Rotation.OneEighty, Rotation.OneEighty),
            RotationTuple.of(Rotation.TwoSeventy, Rotation.OneEighty),
            RotationTuple.of(Rotation.None, Rotation.TwoSeventy),
            RotationTuple.of(Rotation.Ninety, Rotation.TwoSeventy),
            RotationTuple.of(Rotation.OneEighty, Rotation.TwoSeventy),
            RotationTuple.of(Rotation.TwoSeventy, Rotation.TwoSeventy)
        },
        Function.identity(),
        (pair, rotation) -> RotationTuple.of(pair.yaw(), pair.pitch().add(rotation)),
        (pair, rotation) -> pair
    ),
    All(
        com.hypixel.hytale.protocol.VariantRotation.All,
        new RotationTuple[]{
            RotationTuple.of(Rotation.None, Rotation.None, Rotation.Ninety),
            RotationTuple.of(Rotation.None, Rotation.None, Rotation.OneEighty),
            RotationTuple.of(Rotation.None, Rotation.None, Rotation.TwoSeventy),
            RotationTuple.of(Rotation.None, Rotation.Ninety, Rotation.None),
            RotationTuple.of(Rotation.None, Rotation.Ninety, Rotation.Ninety),
            RotationTuple.of(Rotation.None, Rotation.Ninety, Rotation.OneEighty),
            RotationTuple.of(Rotation.None, Rotation.Ninety, Rotation.TwoSeventy),
            RotationTuple.of(Rotation.None, Rotation.OneEighty, Rotation.None),
            RotationTuple.of(Rotation.None, Rotation.OneEighty, Rotation.Ninety),
            RotationTuple.of(Rotation.None, Rotation.OneEighty, Rotation.OneEighty),
            RotationTuple.of(Rotation.None, Rotation.OneEighty, Rotation.TwoSeventy),
            RotationTuple.of(Rotation.None, Rotation.TwoSeventy, Rotation.None),
            RotationTuple.of(Rotation.None, Rotation.TwoSeventy, Rotation.Ninety),
            RotationTuple.of(Rotation.None, Rotation.TwoSeventy, Rotation.OneEighty),
            RotationTuple.of(Rotation.None, Rotation.TwoSeventy, Rotation.TwoSeventy),
            RotationTuple.of(Rotation.Ninety, Rotation.None, Rotation.None),
            RotationTuple.of(Rotation.Ninety, Rotation.None, Rotation.Ninety),
            RotationTuple.of(Rotation.Ninety, Rotation.None, Rotation.OneEighty),
            RotationTuple.of(Rotation.Ninety, Rotation.None, Rotation.TwoSeventy),
            RotationTuple.of(Rotation.Ninety, Rotation.Ninety, Rotation.None),
            RotationTuple.of(Rotation.Ninety, Rotation.Ninety, Rotation.Ninety),
            RotationTuple.of(Rotation.Ninety, Rotation.Ninety, Rotation.OneEighty),
            RotationTuple.of(Rotation.Ninety, Rotation.Ninety, Rotation.TwoSeventy),
            RotationTuple.of(Rotation.Ninety, Rotation.OneEighty, Rotation.None),
            RotationTuple.of(Rotation.Ninety, Rotation.OneEighty, Rotation.Ninety),
            RotationTuple.of(Rotation.Ninety, Rotation.OneEighty, Rotation.OneEighty),
            RotationTuple.of(Rotation.Ninety, Rotation.OneEighty, Rotation.TwoSeventy),
            RotationTuple.of(Rotation.Ninety, Rotation.TwoSeventy, Rotation.None),
            RotationTuple.of(Rotation.Ninety, Rotation.TwoSeventy, Rotation.Ninety),
            RotationTuple.of(Rotation.Ninety, Rotation.TwoSeventy, Rotation.OneEighty),
            RotationTuple.of(Rotation.Ninety, Rotation.TwoSeventy, Rotation.TwoSeventy),
            RotationTuple.of(Rotation.OneEighty, Rotation.None, Rotation.None),
            RotationTuple.of(Rotation.OneEighty, Rotation.None, Rotation.Ninety),
            RotationTuple.of(Rotation.OneEighty, Rotation.None, Rotation.OneEighty),
            RotationTuple.of(Rotation.OneEighty, Rotation.None, Rotation.TwoSeventy),
            RotationTuple.of(Rotation.OneEighty, Rotation.Ninety, Rotation.None),
            RotationTuple.of(Rotation.OneEighty, Rotation.Ninety, Rotation.Ninety),
            RotationTuple.of(Rotation.OneEighty, Rotation.Ninety, Rotation.OneEighty),
            RotationTuple.of(Rotation.OneEighty, Rotation.Ninety, Rotation.TwoSeventy),
            RotationTuple.of(Rotation.OneEighty, Rotation.OneEighty, Rotation.None),
            RotationTuple.of(Rotation.OneEighty, Rotation.OneEighty, Rotation.Ninety),
            RotationTuple.of(Rotation.OneEighty, Rotation.OneEighty, Rotation.OneEighty),
            RotationTuple.of(Rotation.OneEighty, Rotation.OneEighty, Rotation.TwoSeventy),
            RotationTuple.of(Rotation.OneEighty, Rotation.TwoSeventy, Rotation.None),
            RotationTuple.of(Rotation.OneEighty, Rotation.TwoSeventy, Rotation.Ninety),
            RotationTuple.of(Rotation.OneEighty, Rotation.TwoSeventy, Rotation.OneEighty),
            RotationTuple.of(Rotation.OneEighty, Rotation.TwoSeventy, Rotation.TwoSeventy),
            RotationTuple.of(Rotation.TwoSeventy, Rotation.None, Rotation.None),
            RotationTuple.of(Rotation.TwoSeventy, Rotation.None, Rotation.Ninety),
            RotationTuple.of(Rotation.TwoSeventy, Rotation.None, Rotation.OneEighty),
            RotationTuple.of(Rotation.TwoSeventy, Rotation.None, Rotation.TwoSeventy),
            RotationTuple.of(Rotation.TwoSeventy, Rotation.Ninety, Rotation.None),
            RotationTuple.of(Rotation.TwoSeventy, Rotation.Ninety, Rotation.Ninety),
            RotationTuple.of(Rotation.TwoSeventy, Rotation.Ninety, Rotation.OneEighty),
            RotationTuple.of(Rotation.TwoSeventy, Rotation.Ninety, Rotation.TwoSeventy),
            RotationTuple.of(Rotation.TwoSeventy, Rotation.OneEighty, Rotation.None),
            RotationTuple.of(Rotation.TwoSeventy, Rotation.OneEighty, Rotation.Ninety),
            RotationTuple.of(Rotation.TwoSeventy, Rotation.OneEighty, Rotation.OneEighty),
            RotationTuple.of(Rotation.TwoSeventy, Rotation.OneEighty, Rotation.TwoSeventy),
            RotationTuple.of(Rotation.TwoSeventy, Rotation.TwoSeventy, Rotation.None),
            RotationTuple.of(Rotation.TwoSeventy, Rotation.TwoSeventy, Rotation.Ninety),
            RotationTuple.of(Rotation.TwoSeventy, Rotation.TwoSeventy, Rotation.OneEighty),
            RotationTuple.of(Rotation.TwoSeventy, Rotation.TwoSeventy, Rotation.TwoSeventy)
        },
        Function.identity(),
        (pair, rotation) -> RotationTuple.of(pair.yaw(), pair.pitch().add(rotation)),
        (pair, rotation) -> pair
    );

    public static final VariantRotation[] EMPTY_ARRAY = new VariantRotation[0];
    private final com.hypixel.hytale.protocol.VariantRotation protocolType;
    private final RotationTuple[] rotations;
    private final Function<RotationTuple, RotationTuple> verify;
    private final BiFunction<RotationTuple, Rotation, RotationTuple> rotateX;
    private final BiFunction<RotationTuple, Rotation, RotationTuple> rotateZ;

    // $VF: Unable to simplify switch on enum
    // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
    @Nonnull
    private static Rotation validatePipe(@Nonnull Rotation yaw) {
        return switch (1.$SwitchMap$com$hypixel$hytale$server$core$asset$type$blocktype$config$Rotation[yaw.ordinal()]) {
            case 1, 3 -> yaw;
            case 2 -> Rotation.None;
            case 4 -> Rotation.Ninety;
            default -> throw new MatchException(null, null);
        };
    }

    private VariantRotation(
        com.hypixel.hytale.protocol.VariantRotation param3,
        RotationTuple[] param4,
        Function<RotationTuple, RotationTuple> param5,
        BiFunction<RotationTuple, Rotation, RotationTuple> param6,
        BiFunction<RotationTuple, Rotation, RotationTuple> param7
    ) {
        this.protocolType = protocolType;
        this.rotations = rotations;
        this.verify = verify;
        this.rotateX = rotateX;
        this.rotateZ = rotateZ;
    }

    public RotationTuple[] getRotations() {
        return this.rotations;
    }

    public RotationTuple rotateX(RotationTuple pair, Rotation rotation) {
        return (RotationTuple)this.rotateX.apply(pair, rotation);
    }

    public RotationTuple rotateZ(RotationTuple pair, Rotation rotation) {
        return (RotationTuple)this.rotateZ.apply(pair, rotation);
    }

    public RotationTuple verify(RotationTuple pair) {
        return (RotationTuple)this.verify.apply(pair);
    }

    public com.hypixel.hytale.protocol.VariantRotation toPacket() {
        return this.protocolType;
    }
}