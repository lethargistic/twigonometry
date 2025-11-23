package dev.maksiks.twigonometry.api

import net.minecraft.core.Direction


enum class Sector(val bit: Int) {
    N(0), NE(1), E(2), SE(3),
    S(4), SW(5), W(6), NW(7);

    val skip: Int get() = 1 shl bit

    companion object {
        @JvmOverloads
        @JvmStatic
        fun getSectorByDirection(direction1: Direction, direction2: Direction? = null): Sector {
            require(direction1.axis.isHorizontal) {
                "Twigonometry: direction must be horizontal, got: $direction1"
            }

            if (direction2 == null) {
                return when (direction1) {
                    Direction.NORTH -> N
                    Direction.EAST -> E
                    Direction.SOUTH -> S
                    Direction.WEST -> W
                    else -> throw IllegalArgumentException("Twigonometry: unexpected direction: $direction1")
                }
            }

            require(direction2.axis.isHorizontal) {
                "Twigonometry: direction must be horizontal, got: $direction2"
            }
            require(direction1 != direction2) {
                "Twigonometry: directions cannot be the same: $direction1"
            }
            require(direction1 != direction2.opposite) {
                "Twigonometry: directions cannot be opposite: $direction1 and $direction2"
            }

            return when {
                (direction1 == Direction.NORTH && direction2 == Direction.EAST) ||
                        (direction1 == Direction.EAST && direction2 == Direction.NORTH) -> NE

                (direction1 == Direction.SOUTH && direction2 == Direction.EAST) ||
                        (direction1 == Direction.EAST && direction2 == Direction.SOUTH) -> SE

                (direction1 == Direction.SOUTH && direction2 == Direction.WEST) ||
                        (direction1 == Direction.WEST && direction2 == Direction.SOUTH) -> SW

                (direction1 == Direction.NORTH && direction2 == Direction.WEST) ||
                        (direction1 == Direction.WEST && direction2 == Direction.NORTH) -> NW

                else -> throw IllegalArgumentException("Twigonometry: invalid direction combination: $direction1, $direction2")
            }
        }
    }
}