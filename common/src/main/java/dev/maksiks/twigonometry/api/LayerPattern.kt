package dev.maksiks.twigonometry.api

import kotlin.math.abs

enum class LayerPattern {
    CORNERS,        // just the 4 corners
    CARDINALS,      // just N, E, S, W
    DIAGONALS,      // just NE, SE, SW, NW
    STRIPE_NS,      // north-south stripe
    STRIPE_EW,      // east-west stripe
    CROSS,          // + shape
    X_SHAPE,        // x shape
    RING,           // outer edge only
    INNER,          // everything except the outer edge

    // inverse
    NOT_CORNERS,
    NOT_CARDINALS,
    NOT_DIAGONALS,
    NOT_STRIPE_NS,
    NOT_STRIPE_EW,
    NOT_CROSS,
    NOT_X_SHAPE,
    NOT_INNER, // same as RING
    NOT_RING; // same as INNER

    // RING and INNER can be achieved w hollow layers, they're more of a shorthand.

    companion object {
        @JvmStatic
        fun matchesPattern(pattern: LayerPattern, x: Int, z: Int, dist: Int, maxDist: Int): Boolean {
            return when (pattern) {
                CORNERS -> abs(x) == abs(z) && abs(x) != 0
                CARDINALS -> (x == 0 || z == 0) && !(x == 0 && z == 0)
                DIAGONALS -> abs(x) == abs(z) && x != 0
                STRIPE_NS -> x == 0
                STRIPE_EW -> z == 0
                CROSS -> x == 0 || z == 0
                X_SHAPE -> abs(x) == abs(z)
                RING -> dist == maxDist
                INNER -> dist < maxDist

                // inverse
                NOT_CORNERS -> abs(x) != abs(z) || abs(x) == 0
                NOT_CARDINALS -> x != 0 && z != 0
                NOT_DIAGONALS -> abs(x) != abs(z) || x == 0
                NOT_STRIPE_NS -> x != 0
                NOT_STRIPE_EW -> z != 0
                NOT_CROSS -> x != 0 && z != 0
                NOT_X_SHAPE -> abs(x) != abs(z)
                NOT_RING -> dist < maxDist
                NOT_INNER -> dist == maxDist
            }
        }
    }
}