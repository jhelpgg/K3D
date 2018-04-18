package khelp.images

import khelp.util.BLACK_ALPHA_MASK
import khelp.util.COLOR_MASK

/**
 * AND pixel algorithm : It apply AND with Red, Green and Blue of drawing image (over) and image where draw (source)
 */
val AND: (Int, Int) -> Int =
        { source, over -> (source and BLACK_ALPHA_MASK) or ((source and COLOR_MASK) and (over and COLOR_MASK)) }

/**
 * AND pixel algorithm with alpha : It apply AND with Alpha, Red, Green and Blue of drawing image (over) and image where draw
 * (source)
 */
val AND_ALPHA: (Int, Int) -> Int = { source, over -> source and over }

/**
 * OR pixel algorithm : It apply OR with Red, Green and Blue of drawing image (over) and image where draw (source)
 */
val OR: (Int, Int) -> Int =
        { source, over -> (source and BLACK_ALPHA_MASK) or ((source and COLOR_MASK) or (over and COLOR_MASK)) }

/**
 * OR pixel algorithm with alpha : It apply OR with Alpha, Red, Green and Blue of drawing image (over) and image where draw
 * (source)
 */
val OR_ALPHA: (Int, Int) -> Int = { source, over -> source or over }

/**
 * XOR pixel algorithm with alpha : It apply XOR with Alpha, Red, Green and Blue of drawing image (over) and image where draw
 * (source)
 */
val XOR: (Int, Int) -> Int =
        { source, over -> (source and BLACK_ALPHA_MASK) or ((source and COLOR_MASK) xor (over and COLOR_MASK)) }

/**
 * XOR pixel algorithm with alpha : It apply XOR with Alpha, Red, Green and Blue of drawing image (over) and image where draw
 * (source)
 */
val XOR_ALPHA: (Int, Int) -> Int = { source, over -> source xor over }

