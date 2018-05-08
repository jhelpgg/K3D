package khelp.k3d.resources

import khelp.images.JHelpImage
import khelp.k3d.render.Texture
import khelp.resources.Resources

/**Dummy class for have resource reference*/
private class ResourcesK3D

/**Access to embedded resources*/
val resourcesK3D = Resources(ResourcesK3D::class.java)

/**
 * Possible eyes images.
 *
 * Can be used by [khelp.k3d.geometry.prebuilt.Head]
 */
enum class Eyes private constructor(val path: String)
{
    EyeBlue("eyes/eyeBlue.png"),
    EyeBlue2("eyes/eyeBlue2.png"),
    EyeBlue3("eyes/eyeBlue3.png"),
    EyeBrown("eyes/eyeBrown.png"),
    EyeBrown2("eyes/eyeBrown2.png"),
    EyeGreen("eyes/eyeGreen.png"),
    EyeGreen2("eyes/eyeGreen2.png"),
    EyeGreen3("eyes/eyeGreen3.png"),
    EyeGreenShine("eyes/eyeGreenShine.png"),
    EyeLightRed("eyes/eyeLightRed.png"),
    EyePink("eyes/eyePink.png"),
    EyePurple("eyes/eyePurple.png"),
    EyeRed("eyes/eyeRed.png"),
    EyeRed2("eyes/eyeRed2.png"),
    EyeRed3("eyes/eyeRed3.png"),
    EyeToneBlue("eyes/eyeToneBlue.png"),
    EyeToneRed("eyes/eyeToneRed.png")
    ;

    /**Eye image*/
    val image: JHelpImage by lazy { resourcesK3D.obtainResizedJHelpImage(this.path, 64, 64) }
}

/**
 * Possible mouth expressions
 *
 * Can be used by [khelp.k3d.geometry.prebuilt.Head]
 */
enum class Mouth private constructor(val path: String)
{
    Annoy1("mouth/annoy1.png"),
    Annoy2("mouth/annoy2.png"),
    Annoy3("mouth/annoy3.png"),
    AnnoyBig1("mouth/annoyBig1.png"),
    AnnoyBig2("mouth/annoyBig2.png"),
    AnnoyBig3("mouth/annoyBig3.png"),
    AnnoySemi1("mouth/annoySemi1.png"),
    AnnoySemi2("mouth/annoySemi2.png"),
    AnnoySemi3("mouth/annoySemi3.png"),
    Sad1("mouth/sad1.png"),
    Sad2("mouth/sad2.png"),
    Sad3("mouth/sad3.png"),
    Serious1("mouth/serious1.png"),
    Serious2("mouth/serious2.png"),
    Serious3("mouth/serious3.png"),
    Smile1("mouth/smile1.png"),
    Smile2("mouth/smile2.png"),
    Smile3("mouth/smile3.png"),
    SmileBig1("mouth/smileBig1.png"),
    SmileBig2("mouth/smileBig2.png"),
    SmileBig3("mouth/smileBig3.png"),
    SmileOther1("mouth/smileOther1.png"),
    SmileOther2("mouth/smileOther2.png"),
    SmileOther3("mouth/smileOther3.png"),
    Whisper1("mouth/whisper1.png"),
    Whisper2("mouth/whisper2.png"),
    Whisper3("mouth/whisper3.png")
    ;

    /**Mouth image*/
    val image: JHelpImage by lazy { resourcesK3D.obtainJHelpImage(this.path) }
}

/**
 * Obtain texture from resources
 * @param path Texture relative path
 */
fun obtainResourceTexture(path: String): Texture
{
    var texture = Texture.obtainTexture(path)

    if (texture != null)
    {
        return texture
    }

    val image = resourcesK3D.obtainJHelpImage(path)
    texture = Texture(path, image.width, image.height)
    texture.setImage(image)
    return texture
}