package khelp.images.gif

/**
 * This block is a single-field block indicating the end of the GIF Data Stream
 */
const val BLOCK_END_GIF = 0x3B
/**
 * Indicates that is an extended bock
 */
const val BLOCK_EXTENSION = 0x21
/**
 * The Application Extension contains application-specific information
 */
const val BLOCK_EXTENSION_APPLICATION = 0xFF
/**
 * The Comment Extension contains textual information which is not part of the actual graphics in the GIF Data
 * Stream. It is
 * suitable for including comments about the graphics, credits, descriptions or any other type of non-control and
 * non-graphic
 * data.
 */
const val BLOCK_EXTENSION_COMMENT = 0xFE
/**
 * The Graphic Control Extension contains parameters used when processing a graphic rendering block
 */
const val BLOCK_EXTENSION_GRAPHIC_CONTROL = 0xF9
/**
 * The Plain Text Extension contains textual data and the parameters necessary to render that data as a graphic, in
 * a simple
 * form.
 */
const val BLOCK_EXTENSION_PLAIN_TEXT = 0x01
/**
 * The Image Descriptor contains the parameters necessary to process a table based image.
 */
const val BLOCK_IMAGE_DESCRIPTOR = 0x2C
/**
 * Default image duration in milliseconds
 */
const val DEFAULT_TIME: Long = 100
/**
 * Do not dispose. The graphic is to be left in place.
 */
const val DISPOSAL_METHOD_NOT_DISPOSE = 1
/**
 * Restore to background color. The area used by the graphic must be restored to the background color.
 */
const val DISPOSAL_METHOD_RESTORE_BACKGROUND_COLOR = 2
/**
 * Restore to previous. The decoder is required to restore the area overwritten by the graphic with what was there
 * prior to
 * rendering the graphic.
 */
const val DISPOSAL_METHOD_RESTORE_PREVIOUS = 3
/**
 * No disposal specified. The decoder is not required to take any action.
 */
const val DISPOSAL_METHOD_UNSPECIFIED = 0
/**
 * GIF header
 */
const val HEADER_GIF = "GIF"
/**
 * Mask used for extract color resolution from flags : b01110000
 */
const val MASK_COLOR_RESOLUTION = 0x70
/**
 * Mask used for extract information about a following color table from flags : b10000000
 */
const val MASK_COLOR_TABLE_FOLLOW = 0x80
/**
 * Mask used for extract information about ordered table from table : b00100000
 */
const val MASK_COLOR_TABLE_ORDERED = 0x20
/**
 * Mask used for extract disposal method from flags : b00011100
 */
const val MASK_DISPOSAL_METHOD = 0x1C
/**
 * Mask used for extract information about global color table ordered from flags : b00001000
 */
const val MASK_GLOBAL_COLOR_TABLE_ORDERED = 0x08
/**
 * Mask used for extract global color table size from flags : b00000111
 */
const val MASK_GLOBAL_COLOR_TABLE_SIZE = 0x07
/**
 * Mask used for extract information about interlaced image from flags : b01000000
 */
const val MASK_IMAGE_INTERLACED = 0x40
/**
 * Mask used for extract information about transparency given from flags : b00000001
 */
const val MASK_TRANSPARENCY_GIVEN = 0x01
/**
 * Shift of bits to use to get the right value of color resolution
 */
const val SHIFT_COLOR_RESOLUTION = 4
/**
 * Shift of bits to use to have right value of disposal method
 */
const val SHIFT_DISPOSAL_METHOD = 2
/**
 * GIF version 87a : May 1987
 */
const val VERSION_87_A = "87a"
/**
 * GIF version 89a : July 1989
 */
const val VERSION_89_A = "89a"