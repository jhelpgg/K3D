package khelp.ui.resources

import khelp.images.JHelpFont.Type
import khelp.images.JHelpFont.Value
import khelp.resources.Resources
import khelp.util.ColorInt

class UIResources

val UI_RESOURCES = Resources(UIResources::class.java)
val UI_TEXTS = UI_RESOURCES.obtainResourceText("texts/texts")

fun obtainUIFont(fontName: String, fontSize: Int) =
        UI_RESOURCES.obtainJHelpFont(Type.TRUE_TYPE, fontName, fontSize, Value.FREE, Value.FREE, false)

// *************
// *** Texts ***
// *************
const val TEXT_KEY_OK = "OK"
const val TEXT_KEY_YES = "YES"
const val TEXT_KEY_NO = "NO"
const val TEXT_KEY_CANCEL = "CANCEL"

fun okText() = UI_TEXTS.text(TEXT_KEY_OK)
fun yestText() = UI_TEXTS.text(TEXT_KEY_YES)
fun noText() = UI_TEXTS.text(TEXT_KEY_NO)
fun cancelText() = UI_TEXTS.text(TEXT_KEY_CANCEL)

// **************
// *** Alphas ***
// **************

/** Alpha suggested for hint */
const val COLOR_ALPHA_HINT: ColorInt = 0x66000000.toInt()
/** Alpha suggested for lower */
const val COLOR_ALPHA_LOWER: ColorInt = 0x89000000.toInt()
/** Alpha suggested for main */
const val COLOR_ALPHA_MAIN: ColorInt = 0xDD000000.toInt()
/** Alpha suggested for opaque */
const val COLOR_ALPHA_OPAQUE: ColorInt = 0xFF000000.toInt()
/** Alpha suggested for totaly transparent */
const val COLOR_ALPHA_TRANSPARENT: ColorInt = 0x00000000.toInt()

/** Alphas list */
val COLOR_ALPHAS = intArrayOf(
        COLOR_ALPHA_OPAQUE, COLOR_ALPHA_MAIN, COLOR_ALPHA_LOWER,
        COLOR_ALPHA_HINT, COLOR_ALPHA_TRANSPARENT
)

// **************
// *** COLORS ***
// **************

/** Mask for extract color part (RGB) from complete color (ARGB) */
const val MASK_COLOR = 0x00FFFFFF.toInt()

/** Black */
const val COLOR_BLACK: ColorInt = 0xFF000000.toInt()
/** White */
const val COLOR_WHITE: ColorInt = 0xFFFFFFFF.toInt()

/** Amber 50 */
const val COLOR_AMBER_0050: ColorInt = 0xFFFFF8E1.toInt()
/** Amber 100 */
const val COLOR_AMBER_0100: ColorInt = 0xFFFFECB3.toInt()
/** Amber 200 */
const val COLOR_AMBER_0200: ColorInt = 0xFFFFE082.toInt()
/** Amber 300 */
const val COLOR_AMBER_0300: ColorInt = 0xFFFFD54F.toInt()
/** Amber 400 */
const val COLOR_AMBER_0400: ColorInt = 0xFFFFCA28.toInt()
/** Amber 500 : Reference */
const val COLOR_AMBER_0500: ColorInt = 0xFFFFC107.toInt()
/** Amber 600 */
const val COLOR_AMBER_0600: ColorInt = 0xFFFFB300.toInt()
/** Amber 700 */
const val COLOR_AMBER_0700: ColorInt = 0xFFFFA000.toInt()
/** Amber 800 */
const val COLOR_AMBER_0800: ColorInt = 0xFFFF8F00.toInt()
/** Amber 900 */
const val COLOR_AMBER_0900: ColorInt = 0xFFFF6F00.toInt()
/** Amber A100 */
const val COLOR_AMBER_A100: ColorInt = 0xFFFFE57F.toInt()
/** Amber A200 */
const val COLOR_AMBER_A200: ColorInt = 0xFFFFD740.toInt()
/** Amber A400 */
const val COLOR_AMBER_A400: ColorInt = 0xFFFFC400.toInt()
/** Amber A700 */
const val COLOR_AMBER_A700: ColorInt = 0xFFFFAB00.toInt()

/** Amber list : light to dark */
val COLOR_AMBERS = intArrayOf(
        COLOR_AMBER_0050, COLOR_AMBER_0100, COLOR_AMBER_0200,
        COLOR_AMBER_0300, COLOR_AMBER_0400, COLOR_AMBER_0500,
        COLOR_AMBER_0600, COLOR_AMBER_0700, COLOR_AMBER_0800,
        COLOR_AMBER_0900, COLOR_AMBER_A100, COLOR_AMBER_A200,
        COLOR_AMBER_A400, COLOR_AMBER_A700
)

/** Blue 50 */
const val COLOR_BLUE_0050: ColorInt = 0xFFE7E9FD.toInt()
/** Blue 100 */
const val COLOR_BLUE_0100: ColorInt = 0xFFD0D9FF.toInt()
/** Blue 200 */
const val COLOR_BLUE_0200: ColorInt = 0xFFAFBFFF.toInt()
/** Blue 300 */
const val COLOR_BLUE_0300: ColorInt = 0xFF91A7FF.toInt()
/** Blue 400 */
const val COLOR_BLUE_0400: ColorInt = 0xFF738FFE.toInt()
/** Blue 500 */
const val COLOR_BLUE_0500: ColorInt = 0xFF5677FC.toInt()
/** Blue 600 */
const val COLOR_BLUE_0600: ColorInt = 0xFF4E6CEF.toInt()
/** Blue 700 */
const val COLOR_BLUE_0700: ColorInt = 0xFF455EDE.toInt()
/** Blue 800 */
const val COLOR_BLUE_0800: ColorInt = 0xFF3B50CE.toInt()
/** Blue 900 */
const val COLOR_BLUE_0900: ColorInt = 0xFF2A36B1.toInt()
/** Blue A100 */
const val COLOR_BLUE_A100: ColorInt = 0xFFA6BAFF.toInt()
/** Blue A200 */
const val COLOR_BLUE_A200: ColorInt = 0xFF6889FF.toInt()
/** Blue A400 */
const val COLOR_BLUE_A400: ColorInt = 0xFF4D73FF.toInt()
/** Blue A700 */
const val COLOR_BLUE_A700: ColorInt = 0xFF4D69FF.toInt()

/** Blue list : light to dark */
val COLOR_BLUES = intArrayOf(
        COLOR_BLUE_0050, COLOR_BLUE_0100, COLOR_BLUE_0200,
        COLOR_BLUE_0300, COLOR_BLUE_0400, COLOR_BLUE_0500,
        COLOR_BLUE_0600, COLOR_BLUE_0700, COLOR_BLUE_0800,
        COLOR_BLUE_0900, COLOR_BLUE_A100, COLOR_BLUE_A200,
        COLOR_BLUE_A400, COLOR_BLUE_A700
)

/** Blue grey 50 */
const val COLOR_BLUE_GREY_0050: ColorInt = 0xFFECEFF1.toInt()
/** Blue grey 100 */
const val COLOR_BLUE_GREY_0100: ColorInt = 0xFFCFD8DC.toInt()
/** Blue grey 200 */
const val COLOR_BLUE_GREY_0200: ColorInt = 0xFFB0BEC5.toInt()
/** Blue grey 300 */
const val COLOR_BLUE_GREY_0300: ColorInt = 0xFF90A4AE.toInt()
/** Blue grey 400 */
const val COLOR_BLUE_GREY_0400: ColorInt = 0xFF78909C.toInt()
/** Blue grey 500 */
const val COLOR_BLUE_GREY_0500: ColorInt = 0xFF607D8B.toInt()
/** Blue grey 600 */
const val COLOR_BLUE_GREY_0600: ColorInt = 0xFF546E7A.toInt()
/** Blue grey 700 */
const val COLOR_BLUE_GREY_0700: ColorInt = 0xFF455A64.toInt()
/** Blue grey 800 */
const val COLOR_BLUE_GREY_0800: ColorInt = 0xFF37474F.toInt()
/** Blue grey 900 */
const val COLOR_BLUE_GREY_0900: ColorInt = 0xFF263238.toInt()

/** Blue grey list : light to dark */
val COLOR_BLUE_GREYS = intArrayOf(
        COLOR_BLUE_GREY_0050, COLOR_BLUE_GREY_0100, COLOR_BLUE_GREY_0200,
        COLOR_BLUE_GREY_0300, COLOR_BLUE_GREY_0400, COLOR_BLUE_GREY_0500,
        COLOR_BLUE_GREY_0600, COLOR_BLUE_GREY_0700, COLOR_BLUE_GREY_0800,
        COLOR_BLUE_GREY_0900
)

/** Brown 50 */
const val COLOR_BROWN_0050: ColorInt = 0xFFEFEBE9.toInt()
/** Brown 100 */
const val COLOR_BROWN_0100: ColorInt = 0xFFD7CCC8.toInt()
/** Brown 200 */
const val COLOR_BROWN_0200: ColorInt = 0xFFBCAAA4.toInt()
/** Brown 300 */
const val COLOR_BROWN_0300: ColorInt = 0xFFA1887F.toInt()
/** Brown 400 */
const val COLOR_BROWN_0400: ColorInt = 0xFF8D6E63.toInt()
/** Brown 500 */
const val COLOR_BROWN_0500: ColorInt = 0xFF795548.toInt()
/** Brown 600 */
const val COLOR_BROWN_0600: ColorInt = 0xFF6D4C41.toInt()
/** Brown 700 */
const val COLOR_BROWN_0700: ColorInt = 0xFF5D4037.toInt()
/** Brown 800 */
const val COLOR_BROWN_0800: ColorInt = 0xFF4E342E.toInt()
/** Brown 900 */
const val COLOR_BROWN_0900: ColorInt = 0xFF3E2723.toInt()

/** Brown list : light to dark */
val COLOR_BROWNS = intArrayOf(
        COLOR_BROWN_0050, COLOR_BROWN_0100, COLOR_BROWN_0200,
        COLOR_BROWN_0300, COLOR_BROWN_0400, COLOR_BROWN_0500,
        COLOR_BROWN_0600, COLOR_BROWN_0700, COLOR_BROWN_0800,
        COLOR_BROWN_0900
)

/** Cyan 50 */
const val COLOR_CYAN_0050: ColorInt = 0xFFE0F7FA.toInt()
/** Cyan 100 */
const val COLOR_CYAN_0100: ColorInt = 0xFFB2EBF2.toInt()
/** Cyan 200 */
const val COLOR_CYAN_0200: ColorInt = 0xFF80DEEA.toInt()
/** Cyan 300 */
const val COLOR_CYAN_0300: ColorInt = 0xFF4DD0E1.toInt()
/** Cyan 400 */
const val COLOR_CYAN_0400: ColorInt = 0xFF26C6DA.toInt()
/** Cyan 500 */
const val COLOR_CYAN_0500: ColorInt = 0xFF00BCD4.toInt()
/** Cyan 600 */
const val COLOR_CYAN_0600: ColorInt = 0xFF00ACC1.toInt()
/** Cyan 700 */
const val COLOR_CYAN_0700: ColorInt = 0xFF0097A7.toInt()
/** Cyan 800 */
const val COLOR_CYAN_0800: ColorInt = 0xFF00838F.toInt()
/** Cyan 900 */
const val COLOR_CYAN_0900: ColorInt = 0xFF006064.toInt()
/** Cyan A100 */
const val COLOR_CYAN_A100: ColorInt = 0xFF84FFFF.toInt()
/** Cyan A200 */
const val COLOR_CYAN_A200: ColorInt = 0xFF18FFFF.toInt()
/** Cyan A400 */
const val COLOR_CYAN_A400: ColorInt = 0xFF00E5FF.toInt()
/** Cyan A700 */
const val COLOR_CYAN_A700: ColorInt = 0xFF00B8D4.toInt()

/** Cyan list : light to dark */
val COLOR_CYANS = intArrayOf(
        COLOR_CYAN_0050, COLOR_CYAN_0100, COLOR_CYAN_0200,
        COLOR_CYAN_0300, COLOR_CYAN_0400, COLOR_CYAN_0500,
        COLOR_CYAN_0600, COLOR_CYAN_0700, COLOR_CYAN_0800,
        COLOR_CYAN_0900, COLOR_CYAN_A100, COLOR_CYAN_A200,
        COLOR_CYAN_A400, COLOR_CYAN_A700
)

/** Deep orange 50 */
const val COLOR_DEEP_ORANGE_0050: ColorInt = 0xFFFBE9E7.toInt()
/** Deep orange 100 */
const val COLOR_DEEP_ORANGE_0100: ColorInt = 0xFFFFCCBC.toInt()
/** Deep orange 200 */
const val COLOR_DEEP_ORANGE_0200: ColorInt = 0xFFFFAB91.toInt()
/** Deep orange 300 */
const val COLOR_DEEP_ORANGE_0300: ColorInt = 0xFFFF8A65.toInt()
/** Deep orange 400 */
const val COLOR_DEEP_ORANGE_0400: ColorInt = 0xFFFF7043.toInt()
/** Deep orange 500 */
const val COLOR_DEEP_ORANGE_0500: ColorInt = 0xFFFF5722.toInt()
/** Deep orange 600 */
const val COLOR_DEEP_ORANGE_0600: ColorInt = 0xFFF4511E.toInt()
/** Deep orange 700 */
const val COLOR_DEEP_ORANGE_0700: ColorInt = 0xFFE64A19.toInt()
/** Deep orange 800 */
const val COLOR_DEEP_ORANGE_0800: ColorInt = 0xFFD84315.toInt()
/** Deep orange 900 */
const val COLOR_DEEP_ORANGE_0900: ColorInt = 0xFFBF360C.toInt()
/** Deep orange A100 */
const val COLOR_DEEP_ORANGE_A100: ColorInt = 0xFFFF9E80.toInt()
/** Deep orange A200 */
const val COLOR_DEEP_ORANGE_A200: ColorInt = 0xFFFF6E40.toInt()
/** Deep orange A400 */
const val COLOR_DEEP_ORANGE_A400: ColorInt = 0xFFFF3D00.toInt()
/** Deep orange A700 */
const val COLOR_DEEP_ORANGE_A700: ColorInt = 0xFFDD2C00.toInt()

/** Deep orange list : light to dark */
val COLOR_DEEP_ORANGES = intArrayOf(
        COLOR_DEEP_ORANGE_0050, COLOR_DEEP_ORANGE_0100, COLOR_DEEP_ORANGE_0200,
        COLOR_DEEP_ORANGE_0300, COLOR_DEEP_ORANGE_0400, COLOR_DEEP_ORANGE_0500,
        COLOR_DEEP_ORANGE_0600, COLOR_DEEP_ORANGE_0700, COLOR_DEEP_ORANGE_0800,
        COLOR_DEEP_ORANGE_0900, COLOR_DEEP_ORANGE_A100, COLOR_DEEP_ORANGE_A200,
        COLOR_DEEP_ORANGE_A400, COLOR_DEEP_ORANGE_A700
)

/** Deep purple 50 */
const val COLOR_DEEP_PURPLE_0050: ColorInt = 0xFFEDE7F6.toInt()
/** Deep purple 100 */
const val COLOR_DEEP_PURPLE_0100: ColorInt = 0xFFD1C4E9.toInt()
/** Deep purple 200 */
const val COLOR_DEEP_PURPLE_0200: ColorInt = 0xFFB39DDB.toInt()
/** Deep purple 300 */
const val COLOR_DEEP_PURPLE_0300: ColorInt = 0xFF9575CD.toInt()
/** Deep purple 400 */
const val COLOR_DEEP_PURPLE_0400: ColorInt = 0xFF7E57C2.toInt()
/** Deep purple 500 */
const val COLOR_DEEP_PURPLE_0500: ColorInt = 0xFF673AB7.toInt()
/** Deep purple 600 */
const val COLOR_DEEP_PURPLE_0600: ColorInt = 0xFF5E35B1.toInt()
/** Deep purple 700 */
const val COLOR_DEEP_PURPLE_0700: ColorInt = 0xFF512DA8.toInt()
/** Deep purple 800 */
const val COLOR_DEEP_PURPLE_0800: ColorInt = 0xFF4527A0.toInt()
/** Deep purple 900 */
const val COLOR_DEEP_PURPLE_0900: ColorInt = 0xFF311B92.toInt()
/** Deep purple A100 */
const val COLOR_DEEP_PURPLE_A100: ColorInt = 0xFFB388FF.toInt()
/** Deep purple A200 */
const val COLOR_DEEP_PURPLE_A200: ColorInt = 0xFF7C4DFF.toInt()
/** Deep purple A400 */
const val COLOR_DEEP_PURPLE_A400: ColorInt = 0xFF651FFF.toInt()
/** Deep purple A700 */
const val COLOR_DEEP_PURPLE_A700: ColorInt = 0xFF6200EA.toInt()

/** Deep purple list : light to dark */
val COLOR_DEEP_PURPLES = intArrayOf(
        COLOR_DEEP_PURPLE_0050, COLOR_DEEP_PURPLE_0100, COLOR_DEEP_PURPLE_0200,
        COLOR_DEEP_PURPLE_0300, COLOR_DEEP_PURPLE_0400, COLOR_DEEP_PURPLE_0500,
        COLOR_DEEP_PURPLE_0600, COLOR_DEEP_PURPLE_0700, COLOR_DEEP_PURPLE_0800,
        COLOR_DEEP_PURPLE_0900, COLOR_DEEP_PURPLE_A100, COLOR_DEEP_PURPLE_A200,
        COLOR_DEEP_PURPLE_A400, COLOR_DEEP_PURPLE_A700
)

/** Green 50 */
const val COLOR_GREEN_0050: ColorInt = 0xFFD0F8CE.toInt()
/** Green 100 */
const val COLOR_GREEN_0100: ColorInt = 0xFFA3E9A4.toInt()
/** Green 200 */
const val COLOR_GREEN_0200: ColorInt = 0xFF72D572.toInt()
/** Green 300 */
const val COLOR_GREEN_0300: ColorInt = 0xFF42BD41.toInt()
/** Green 400 */
const val COLOR_GREEN_0400: ColorInt = 0xFF2BAF2B.toInt()
/** Green 500 */
const val COLOR_GREEN_0500: ColorInt = 0xFF259B24.toInt()
/** Green 600 */
const val COLOR_GREEN_0600: ColorInt = 0xFF0A8F08.toInt()
/** Green 700 */
const val COLOR_GREEN_0700: ColorInt = 0xFF0A7E07.toInt()
/** Green 800 */
const val COLOR_GREEN_0800: ColorInt = 0xFF056F00.toInt()
/** Green 900 */
const val COLOR_GREEN_0900: ColorInt = 0xFF0D5302.toInt()
/** Green A100 */
const val COLOR_GREEN_A100: ColorInt = 0xFFA2F78D.toInt()
/** Green A200 */
const val COLOR_GREEN_A200: ColorInt = 0xFF5AF158.toInt()
/** Green A400 */
const val COLOR_GREEN_A400: ColorInt = 0xFF14E715.toInt()
/** Green A700 */
const val COLOR_GREEN_A700: ColorInt = 0xFF12C700.toInt()

/** Green list : light to dark */
val COLOR_GREENS = intArrayOf(
        COLOR_GREEN_0050, COLOR_GREEN_0100, COLOR_GREEN_0200,
        COLOR_GREEN_0300, COLOR_GREEN_0400, COLOR_GREEN_0500,
        COLOR_GREEN_0600, COLOR_GREEN_0700, COLOR_GREEN_0800,
        COLOR_GREEN_0900, COLOR_GREEN_A100, COLOR_GREEN_A200,
        COLOR_GREEN_A400, COLOR_GREEN_A700
)

/** Grey 50 */
const val COLOR_GREY_0050: ColorInt = 0xFFFAFAFA.toInt()
/** Grey 100 */
const val COLOR_GREY_0100: ColorInt = 0xFFF5F5F5.toInt()
/** Grey 200 */
const val COLOR_GREY_0200: ColorInt = 0xFFEEEEEE.toInt()
/** Grey 300 */
const val COLOR_GREY_0300: ColorInt = 0xFFE0E0E0.toInt()
/** Grey 400 */
const val COLOR_GREY_0400: ColorInt = 0xFFBDBDBD.toInt()
/** Grey 500 */
const val COLOR_GREY_0500: ColorInt = 0xFF9E9E9E.toInt()
/** Grey 600 */
const val COLOR_GREY_0600: ColorInt = 0xFF757575.toInt()
/** Grey 700 */
const val COLOR_GREY_0700: ColorInt = 0xFF616161.toInt()
/** Grey 800 */
const val COLOR_GREY_0800: ColorInt = 0xFF424242.toInt()
/** Grey 900 */
const val COLOR_GREY_0900: ColorInt = 0xFF212121.toInt()

/** Grey list : light to dark (white to black) */
val COLOR_GREYS = intArrayOf(
        COLOR_WHITE, COLOR_GREY_0050, COLOR_GREY_0100, COLOR_GREY_0200,
        COLOR_GREY_0300, COLOR_GREY_0400, COLOR_GREY_0500,
        COLOR_GREY_0600, COLOR_GREY_0700, COLOR_GREY_0800,
        COLOR_GREY_0900, COLOR_BLACK
)

/** Indigo 50 */
const val COLOR_INDIGO_0050: ColorInt = 0xFFE8EAF6.toInt()
/** Indigo 100 */
const val COLOR_INDIGO_0100: ColorInt = 0xFFC5CAE9.toInt()
/** Indigo 200 */
const val COLOR_INDIGO_0200: ColorInt = 0xFF9FA8DA.toInt()
/** Indigo 300 */
const val COLOR_INDIGO_0300: ColorInt = 0xFF7986CB.toInt()
/** Indigo 400 */
const val COLOR_INDIGO_0400: ColorInt = 0xFF5C6BC0.toInt()
/** Indigo 500 */
const val COLOR_INDIGO_0500: ColorInt = 0xFF3F51B5.toInt()
/** Indigo 600 */
const val COLOR_INDIGO_0600: ColorInt = 0xFF3949AB.toInt()
/** Indigo 700 */
const val COLOR_INDIGO_0700: ColorInt = 0xFF303F9F.toInt()
/** Indigo 800 */
const val COLOR_INDIGO_0800: ColorInt = 0xFF283593.toInt()
/** Indigo 900 */
const val COLOR_INDIGO_0900: ColorInt = 0xFF1A237E.toInt()
/** Indigo A100 */
const val COLOR_INDIGO_A100: ColorInt = 0xFF8C9EFF.toInt()
/** Indigo A200 */
const val COLOR_INDIGO_A200: ColorInt = 0xFF536DFE.toInt()
/** Indigo A400 */
const val COLOR_INDIGO_A400: ColorInt = 0xFF3D5AFE.toInt()
/** Indigo A700 */
const val COLOR_INDIGO_A700: ColorInt = 0xFF304FFE.toInt()

/** Indigo list : light to dark */
val COLOR_INDIGOS = intArrayOf(
        COLOR_INDIGO_0050, COLOR_INDIGO_0100, COLOR_INDIGO_0200,
        COLOR_INDIGO_0300, COLOR_INDIGO_0400, COLOR_INDIGO_0500,
        COLOR_INDIGO_0600, COLOR_INDIGO_0700, COLOR_INDIGO_0800,
        COLOR_INDIGO_0900, COLOR_INDIGO_A100, COLOR_INDIGO_A200,
        COLOR_INDIGO_A400, COLOR_INDIGO_A700
)

/** Light blue 50 */
const val COLOR_LIGHT_BLUE_0050: ColorInt = 0xFFE1F5FE.toInt()
/** Light blue 100 */
const val COLOR_LIGHT_BLUE_0100: ColorInt = 0xFFB3E5FC.toInt()
/** Light blue 200 */
const val COLOR_LIGHT_BLUE_0200: ColorInt = 0xFF81D4FA.toInt()
/** Light blue 300 */
const val COLOR_LIGHT_BLUE_0300: ColorInt = 0xFF4FC3F7.toInt()
/** Light blue 400 */
const val COLOR_LIGHT_BLUE_0400: ColorInt = 0xFF29B6F6.toInt()
/** Light blue 500 */
const val COLOR_LIGHT_BLUE_0500: ColorInt = 0xFF03A9F4.toInt()
/** Light blue 600 */
const val COLOR_LIGHT_BLUE_0600: ColorInt = 0xFF039BE5.toInt()
/** Light blue 700 */
const val COLOR_LIGHT_BLUE_0700: ColorInt = 0xFF0288D1.toInt()
/** Light blue 800 */
const val COLOR_LIGHT_BLUE_0800: ColorInt = 0xFF0277BD.toInt()
/** Light blue 900 */
const val COLOR_LIGHT_BLUE_0900: ColorInt = 0xFF01579B.toInt()
/** Light blue A100 */
const val COLOR_LIGHT_BLUE_A100: ColorInt = 0xFF80D8FF.toInt()
/** Light blue A200 */
const val COLOR_LIGHT_BLUE_A200: ColorInt = 0xFF40C4FF.toInt()
/** Light blue A400 */
const val COLOR_LIGHT_BLUE_A400: ColorInt = 0xFF00B0FF.toInt()
/** Light blue A700 */
const val COLOR_LIGHT_BLUE_A700: ColorInt = 0xFF0091EA.toInt()

/** Light blue list : light to dark */
val COLOR_LIGHT_BLUES = intArrayOf(
        COLOR_LIGHT_BLUE_0050, COLOR_LIGHT_BLUE_0100, COLOR_LIGHT_BLUE_0200,
        COLOR_LIGHT_BLUE_0300, COLOR_LIGHT_BLUE_0400, COLOR_LIGHT_BLUE_0500,
        COLOR_LIGHT_BLUE_0600, COLOR_LIGHT_BLUE_0700, COLOR_LIGHT_BLUE_0800,
        COLOR_LIGHT_BLUE_0900, COLOR_LIGHT_BLUE_A100, COLOR_LIGHT_BLUE_A200,
        COLOR_LIGHT_BLUE_A400, COLOR_LIGHT_BLUE_A700
)

/** Light green 50 */
const val COLOR_LIGHT_GREEN_0050: ColorInt = 0xFFF1F8E9.toInt()
/** Light green 100 */
const val COLOR_LIGHT_GREEN_0100: ColorInt = 0xFFDCEDC8.toInt()
/** Light green 200 */
const val COLOR_LIGHT_GREEN_0200: ColorInt = 0xFFC5E1A5.toInt()
/** Light green 300 */
const val COLOR_LIGHT_GREEN_0300: ColorInt = 0xFFAED581.toInt()
/** Light green 400 */
const val COLOR_LIGHT_GREEN_0400: ColorInt = 0xFF9CCC65.toInt()
/** Light green 500 */
const val COLOR_LIGHT_GREEN_0500: ColorInt = 0xFF8BC34A.toInt()
/** Light green 600 */
const val COLOR_LIGHT_GREEN_0600: ColorInt = 0xFF7CB342.toInt()
/** Light green 700 */
const val COLOR_LIGHT_GREEN_0700: ColorInt = 0xFF689F38.toInt()
/** Light green 800 */
const val COLOR_LIGHT_GREEN_0800: ColorInt = 0xFF558B2F.toInt()
/** Light green 900 */
const val COLOR_LIGHT_GREEN_0900: ColorInt = 0xFF33691E.toInt()
/** Light green A100 */
const val COLOR_LIGHT_GREEN_A100: ColorInt = 0xFFCCFF90.toInt()
/** Light green A200 */
const val COLOR_LIGHT_GREEN_A200: ColorInt = 0xFFB2FF59.toInt()
/** Light green A400 */
const val COLOR_LIGHT_GREEN_A400: ColorInt = 0xFF76FF03.toInt()
/** Light green A700 */
const val COLOR_LIGHT_GREEN_A700: ColorInt = 0xFF64DD17.toInt()

/** Light green list : light to dark */
val COLOR_LIGHT_GREENS = intArrayOf(
        COLOR_LIGHT_GREEN_0050, COLOR_LIGHT_GREEN_0100, COLOR_LIGHT_GREEN_0200,
        COLOR_LIGHT_GREEN_0300, COLOR_LIGHT_GREEN_0400, COLOR_LIGHT_GREEN_0500,
        COLOR_LIGHT_GREEN_0600, COLOR_LIGHT_GREEN_0700, COLOR_LIGHT_GREEN_0800,
        COLOR_LIGHT_GREEN_0900, COLOR_LIGHT_GREEN_A100, COLOR_LIGHT_GREEN_A200,
        COLOR_LIGHT_GREEN_A400, COLOR_LIGHT_GREEN_A700
)

/** Lime 50 */
const val COLOR_LIME_0050: ColorInt = 0xFFF9FBE7.toInt()
/** Lime 100 */
const val COLOR_LIME_0100: ColorInt = 0xFFF0F4C3.toInt()
/** Lime 200 */
const val COLOR_LIME_0200: ColorInt = 0xFFE6EE9C.toInt()
/** Lime 300 */
const val COLOR_LIME_0300: ColorInt = 0xFFDCE775.toInt()
/** Lime 400 */
const val COLOR_LIME_0400: ColorInt = 0xFFD4E157.toInt()
/** Lime 500 */
const val COLOR_LIME_0500: ColorInt = 0xFFCDDC39.toInt()
/** Lime 600 */
const val COLOR_LIME_0600: ColorInt = 0xFFC0CA33.toInt()
/** Lime 700 */
const val COLOR_LIME_0700: ColorInt = 0xFFAFB42B.toInt()
/** Lime 800 */
const val COLOR_LIME_0800: ColorInt = 0xFF9E9D24.toInt()
/** Lime 900 */
const val COLOR_LIME_0900: ColorInt = 0xFF827717.toInt()
/** Lime A100 */
const val COLOR_LIME_A100: ColorInt = 0xFFF4FF81.toInt()
/** Lime A200 */
const val COLOR_LIME_A200: ColorInt = 0xFFEEFF41.toInt()
/** Lime A400 */
const val COLOR_LIME_A400: ColorInt = 0xFFC6FF00.toInt()
/** Lime A700 */
const val COLOR_LIME_A700: ColorInt = 0xFFAEEA00.toInt()

/** Lime list : light to dark */
val COLOR_LIMES = intArrayOf(
        COLOR_LIME_0050, COLOR_LIME_0100, COLOR_LIME_0200,
        COLOR_LIME_0300, COLOR_LIME_0400, COLOR_LIME_0500,
        COLOR_LIME_0600, COLOR_LIME_0700, COLOR_LIME_0800,
        COLOR_LIME_0900, COLOR_LIME_A100, COLOR_LIME_A200,
        COLOR_LIME_A400, COLOR_LIME_A700
)

/** Orange 00 */
const val COLOR_ORANGE_0050: ColorInt = 0xFFFFF3E0.toInt()
/** Orange 100 */
const val COLOR_ORANGE_0100: ColorInt = 0xFFFFE0B2.toInt()
/** Orange 200 */
const val COLOR_ORANGE_0200: ColorInt = 0xFFFFCC80.toInt()
/** Orange 300 */
const val COLOR_ORANGE_0300: ColorInt = 0xFFFFB74D.toInt()
/** Orange 400 */
const val COLOR_ORANGE_0400: ColorInt = 0xFFFFA726.toInt()
/** Orange 500 */
const val COLOR_ORANGE_0500: ColorInt = 0xFFFF9800.toInt()
/** Orange 600 */
const val COLOR_ORANGE_0600: ColorInt = 0xFFFB8C00.toInt()
/** Orange 700 */
const val COLOR_ORANGE_0700: ColorInt = 0xFFF57C00.toInt()
/** Orange 800 */
const val COLOR_ORANGE_0800: ColorInt = 0xFFEF6C00.toInt()
/** Orange 900 */
const val COLOR_ORANGE_0900: ColorInt = 0xFFE65100.toInt()
/** Orange A100 */
const val COLOR_ORANGE_A100: ColorInt = 0xFFFFD180.toInt()
/** Orange A200 */
const val COLOR_ORANGE_A200: ColorInt = 0xFFFFAB40.toInt()
/** Orange A400 */
const val COLOR_ORANGE_A400: ColorInt = 0xFFFF9100.toInt()
/** Orange A700 */
const val COLOR_ORANGE_A700: ColorInt = 0xFFFF6D00.toInt()

/** Orange list : light to dark */
val COLOR_ORANGES = intArrayOf(
        COLOR_ORANGE_0050, COLOR_ORANGE_0100, COLOR_ORANGE_0200,
        COLOR_ORANGE_0300, COLOR_ORANGE_0400, COLOR_ORANGE_0500,
        COLOR_ORANGE_0600, COLOR_ORANGE_0700, COLOR_ORANGE_0800,
        COLOR_ORANGE_0900, COLOR_ORANGE_A100, COLOR_ORANGE_A200,
        COLOR_ORANGE_A400, COLOR_ORANGE_A700
)

/** Pink 50 */
const val COLOR_PINK_0050: ColorInt = 0xFFFCE4EC.toInt()
/** Pink 100 */
const val COLOR_PINK_0100: ColorInt = 0xFFF8BBD0.toInt()
/** Pink 200 */
const val COLOR_PINK_0200: ColorInt = 0xFFF48FB1.toInt()
/** Pink 300 */
const val COLOR_PINK_0300: ColorInt = 0xFFF06292.toInt()
/** Pink 400 */
const val COLOR_PINK_0400: ColorInt = 0xFFEC407A.toInt()
/** Pink 500 */
const val COLOR_PINK_0500: ColorInt = 0xFFE91E63.toInt()
/** Pink 600 */
const val COLOR_PINK_0600: ColorInt = 0xFFD81B60.toInt()
/** Pink 700 */
const val COLOR_PINK_0700: ColorInt = 0xFFC2185B.toInt()
/** Pink 800 */
const val COLOR_PINK_0800: ColorInt = 0xFFAD1457.toInt()
/** Pink 900 */
const val COLOR_PINK_0900: ColorInt = 0xFF880E4F.toInt()
/** Pink A100 */
const val COLOR_PINK_A100: ColorInt = 0xFFFF80AB.toInt()
/** Pink A200 */
const val COLOR_PINK_A200: ColorInt = 0xFFFF4081.toInt()
/** Pink A400 */
const val COLOR_PINK_A400: ColorInt = 0xFFF50057.toInt()
/** Pink A700 */
const val COLOR_PINK_A700: ColorInt = 0xFFC51162.toInt()

/** Pink list : light to dark */
val COLOR_PINKS = intArrayOf(
        COLOR_PINK_0050, COLOR_PINK_0100, COLOR_PINK_0200,
        COLOR_PINK_0300, COLOR_PINK_0400, COLOR_PINK_0500,
        COLOR_PINK_0600, COLOR_PINK_0700, COLOR_PINK_0800,
        COLOR_PINK_0900, COLOR_PINK_A100, COLOR_PINK_A200,
        COLOR_PINK_A400, COLOR_PINK_A700
)

/** Purple 50 */
const val COLOR_PURPLE_0050: ColorInt = 0xFFF3E5F5.toInt()
/** Purple 100 */
const val COLOR_PURPLE_0100: ColorInt = 0xFFE1BEE7.toInt()
/** Purple 200 */
const val COLOR_PURPLE_0200: ColorInt = 0xFFCE93D8.toInt()
/** Purple 300 */
const val COLOR_PURPLE_0300: ColorInt = 0xFFBA68C8.toInt()
/** Purple 400 */
const val COLOR_PURPLE_0400: ColorInt = 0xFFAB47BC.toInt()
/** Purple 500 */
const val COLOR_PURPLE_0500: ColorInt = 0xFF9C27B0.toInt()
/** Purple 600 */
const val COLOR_PURPLE_0600: ColorInt = 0xFF8E24AA.toInt()
/** Purple 700 */
const val COLOR_PURPLE_0700: ColorInt = 0xFF7B1FA2.toInt()
/** Purple 800 */
const val COLOR_PURPLE_0800: ColorInt = 0xFF6A1B9A.toInt()
/** Purple 900 */
const val COLOR_PURPLE_0900: ColorInt = 0xFF4A148C.toInt()
/** Purple A100 */
const val COLOR_PURPLE_A100: ColorInt = 0xFFEA80FC.toInt()
/** Purple A200 */
const val COLOR_PURPLE_A200: ColorInt = 0xFFE040FB.toInt()
/** Purple A400 */
const val COLOR_PURPLE_A400: ColorInt = 0xFFD500F9.toInt()
/** Purple A700 */
const val COLOR_PURPLE_A700: ColorInt = 0xFFAA00FF.toInt()

/** Purple list : light to dark */
val COLOR_PURPLES = intArrayOf(
        COLOR_PURPLE_0050, COLOR_PURPLE_0100, COLOR_PURPLE_0200,
        COLOR_PURPLE_0300, COLOR_PURPLE_0400, COLOR_PURPLE_0500,
        COLOR_PURPLE_0600, COLOR_PURPLE_0700, COLOR_PURPLE_0800,
        COLOR_PURPLE_0900, COLOR_PURPLE_A100, COLOR_PURPLE_A200,
        COLOR_PURPLE_A400, COLOR_PURPLE_A700
)

/** Red 50 */
const val COLOR_RED_0050: ColorInt = 0xFFFDE0DC.toInt()
/** Red 100 */
const val COLOR_RED_0100: ColorInt = 0xFFF9BDBB.toInt()
/** Red 200 */
const val COLOR_RED_0200: ColorInt = 0xFFF69988.toInt()
/** Red 300 */
const val COLOR_RED_0300: ColorInt = 0xFFF36C60.toInt()
/** Red 400 */
const val COLOR_RED_0400: ColorInt = 0xFFE84E40.toInt()
/** Red 500 */
const val COLOR_RED_0500: ColorInt = 0xFFE51C23.toInt()
/** Red 600 */
const val COLOR_RED_0600: ColorInt = 0xFFDD191D.toInt()
/** Red 700 */
const val COLOR_RED_0700: ColorInt = 0xFFD01716.toInt()
/** Red 800 */
const val COLOR_RED_0800: ColorInt = 0xFFC41411.toInt()
/** Red 900 */
const val COLOR_RED_0900: ColorInt = 0xFFB0120A.toInt()
/** Red A100 */
const val COLOR_RED_A100: ColorInt = 0xFFFF7997.toInt()
/** Red A200 */
const val COLOR_RED_A200: ColorInt = 0xFFFF5177.toInt()
/** Red A400 */
const val COLOR_RED_A400: ColorInt = 0xFFFF2D6F.toInt()
/** Red A700 */
const val COLOR_RED_A700: ColorInt = 0xFFE00032.toInt()

/** Red list : light to dark */
val COLOR_REDS = intArrayOf(
        COLOR_RED_0050, COLOR_RED_0100, COLOR_RED_0200, COLOR_RED_0300,
        COLOR_RED_0400, COLOR_RED_0500, COLOR_RED_0600, COLOR_RED_0700,
        COLOR_RED_0800, COLOR_RED_0900, COLOR_RED_A100, COLOR_RED_A200,
        COLOR_RED_A400, COLOR_RED_A700
)

/** Teal 50 */
const val COLOR_TEAL_0050: ColorInt = 0xFFE0F2F1.toInt()
/** Teal 100 */
const val COLOR_TEAL_0100: ColorInt = 0xFFB2DFDB.toInt()
/** Teal 200 */
const val COLOR_TEAL_0200: ColorInt = 0xFF80CBC4.toInt()
/** Teal 300 */
const val COLOR_TEAL_0300: ColorInt = 0xFF4DB6AC.toInt()
/** Teal 400 */
const val COLOR_TEAL_0400: ColorInt = 0xFF26A69A.toInt()
/** Teal 500 */
const val COLOR_TEAL_0500: ColorInt = 0xFF009688.toInt()
/** Teal 600 */
const val COLOR_TEAL_0600: ColorInt = 0xFF00897B.toInt()
/** Teal 700 */
const val COLOR_TEAL_0700: ColorInt = 0xFF00796B.toInt()
/** Teal 800 */
const val COLOR_TEAL_0800: ColorInt = 0xFF00695C.toInt()
/** Teal 900 */
const val COLOR_TEAL_0900: ColorInt = 0xFF004D40.toInt()
/** Teal A100 */
const val COLOR_TEAL_A100: ColorInt = 0xFFA7FFEB.toInt()
/** Teal A200 */
const val COLOR_TEAL_A200: ColorInt = 0xFF64FFDA.toInt()
/** Teal A400 */
const val COLOR_TEAL_A400: ColorInt = 0xFF1DE9B6.toInt()
/** Teal A700 */
const val COLOR_TEAL_A700: ColorInt = 0xFF00BFA5.toInt()

/** Teal list : light to dark */
val COLOR_TEALS = intArrayOf(
        COLOR_TEAL_0050, COLOR_TEAL_0100, COLOR_TEAL_0200,
        COLOR_TEAL_0300, COLOR_TEAL_0400, COLOR_TEAL_0500,
        COLOR_TEAL_0600, COLOR_TEAL_0700, COLOR_TEAL_0800,
        COLOR_TEAL_0900, COLOR_TEAL_A100, COLOR_TEAL_A200,
        COLOR_TEAL_A400, COLOR_TEAL_A700
)

/** Yellow 50 */
const val COLOR_YELLOW_0050: ColorInt = 0xFFFFFDE7.toInt()
/** Yellow 100 */
const val COLOR_YELLOW_0100: ColorInt = 0xFFFFF9C4.toInt()
/** Yellow 200 */
const val COLOR_YELLOW_0200: ColorInt = 0xFFFFF59D.toInt()
/** Yellow 300 */
const val COLOR_YELLOW_0300: ColorInt = 0xFFFFF176.toInt()
/** Yellow 400 */
const val COLOR_YELLOW_0400: ColorInt = 0xFFFFEE58.toInt()
/** Yellow 500 */
const val COLOR_YELLOW_0500: ColorInt = 0xFFFFEB3B.toInt()
/** Yellow 600 */
const val COLOR_YELLOW_0600: ColorInt = 0xFFFDD835.toInt()
/** Yellow 700 */
const val COLOR_YELLOW_0700: ColorInt = 0xFFFBC02D.toInt()
/** Yellow 800 */
const val COLOR_YELLOW_0800: ColorInt = 0xFFF9A825.toInt()
/** Yellow 900 */
const val COLOR_YELLOW_0900: ColorInt = 0xFFF57F17.toInt()
/** Yellow A100 */
const val COLOR_YELLOW_A100: ColorInt = 0xFFFFFF8D.toInt()
/** Yellow A200 */
const val COLOR_YELLOW_A200: ColorInt = 0xFFFFFF00.toInt()
/** Yellow A400 */
const val COLOR_YELLOW_A400: ColorInt = 0xFFFFEA00.toInt()
/** Yellow A700 */
const val COLOR_YELLOW_A700: ColorInt = 0xFFFFD600.toInt()

/** Yellow list : light to dark */
val COLOR_YELLOWS = intArrayOf(
        COLOR_YELLOW_0050, COLOR_YELLOW_0100, COLOR_YELLOW_0200,
        COLOR_YELLOW_0300, COLOR_YELLOW_0400, COLOR_YELLOW_0500,
        COLOR_YELLOW_0600, COLOR_YELLOW_0700, COLOR_YELLOW_0800,
        COLOR_YELLOW_0900, COLOR_YELLOW_A100, COLOR_YELLOW_A200,
        COLOR_YELLOW_A400, COLOR_YELLOW_A700
)

/** Body font regular */
val FONT_BODY_1 by lazy { obtainUIFont("fonts/Roboto-Regular.ttf", 24) }
/** Body font medium */
val FONT_BODY_2 by lazy { obtainUIFont("fonts/Roboto-Medium.ttf", 24) }
/** Button font */
val FONT_BUTTON by lazy { obtainUIFont("fonts/Roboto-Medium.ttf", 32) }
/** Caption font */
val FONT_CAPTION by lazy { obtainUIFont("fonts/Roboto-Regular.ttf", 20) }
/** Display font small */
val FONT_DISPLAY_1 by lazy { obtainUIFont("fonts/Roboto-Regular.ttf", 34) }
/** Display font normal */
val FONT_DISPLAY_2 by lazy { obtainUIFont("fonts/Roboto-Regular.ttf", 45) }
/** Display font big */
val FONT_DISPLAY_3 by lazy { obtainUIFont("fonts/Roboto-Regular.ttf", 56) }
/** Display font very big */
val FONT_DISPLAY_4 by lazy { obtainUIFont("fonts/Roboto-Light.ttf", 112) }
/** Headline font */
val FONT_HEADLINE by lazy { obtainUIFont("fonts/Roboto-Regular.ttf", 24) }
/** Menu font */
val FONT_MENU by lazy { obtainUIFont("fonts/Roboto-Medium.ttf", 24) }
/** Operation font */
val FONT_OPERATION by lazy { obtainUIFont("fonts/Roboto-Thin.ttf", 24) }
/** Sub-head font */
val FONT_SUB_HEAD by lazy { obtainUIFont("fonts/Roboto-Regular.ttf", 26) }
/** Title font */
val FONT_TITLE by lazy { obtainUIFont("fonts/Roboto-Medium.ttf", 28) }

/** Error icon */
val ICON_ERROR by lazy { UI_RESOURCES.obtainJHelpImage("images/errorIcon.png") }
/** Information icon */
val ICON_INFORMATION by lazy { UI_RESOURCES.obtainJHelpImage("images/informationIcon.png") }
/** Question icon */
val ICON_QUESTION by lazy { UI_RESOURCES.obtainJHelpImage("images/questionIcon.png") }
/** Warning icon */
val ICON_WARNING by lazy { UI_RESOURCES.obtainJHelpImage("images/warningIcon.png") }
