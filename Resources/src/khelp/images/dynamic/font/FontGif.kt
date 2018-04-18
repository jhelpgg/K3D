package khelp.images.dynamic.font

import khelp.images.gif.GIF
import khelp.io.readLines
import khelp.resources.ROOT
import khelp.resources.ResourceDirectory
import khelp.resources.Resources
import khelp.text.StringCutter
import java.io.IOException
import java.util.Collections

/**Gif with position*/
data class GifPosition(val x: Int, val y: Int, val gif: GIF)

/**
 * Gif text
 */
class GifText(val width: Int, val height: Int, gifPositions: List<GifPosition>)
{
    private val gifPositions = gifPositions
    fun gifPositions() = Collections.unmodifiableList(this.gifPositions)
}

/**
 * File that describe a font
 */
private val CHARACTERS = "characters.txt"
/**
 * Resources to access GIF images
 */
private val RESOURCES = Resources(FontGif::class.java)

/**Available fonts based of gif names*/
val FONT_GIF_NAMES: List<String> by lazy {
    val names = ArrayList<String>()
    val resourcesSystem = RESOURCES.obtainResourcesSystem()

    for (resourceElement in resourcesSystem.obtainList(ROOT))
    {
        if (resourceElement.directory() &&
                resourcesSystem.obtainList(resourceElement as ResourceDirectory)
                        .any { !it.directory() && CHARACTERS == it.name() })
        {
            names.add(resourceElement.name())
        }
    }

    Collections.sort(names)
    Collections.unmodifiableList(names)
}

/**
 * Font based on Gifs
 * @param font Font name
 */
class FontGif(font: String)
{
    /**Gifs map*/
    private val gifs = ArrayList<Pair<String, GIF>>()
    /**Gif element height*/
    private var height = 0
    /**Space size*/
    private var space = 0

    init
    {
        this.parseFont(font)
    }

    /**
     * Parse font description
     *
     * @param font Font folder
     * @throws IOException On parsing issue
     */
    @Throws(IOException::class)
    private fun parseFont(font: String)
    {
        val header = "$font/"
        this.space = Integer.MAX_VALUE
        this.height = 0
        var exception: IOException? = null

        readLines({ RESOURCES.obtainResourceStream(header + CHARACTERS) },
                  { line ->
                      if (line.length > 0 && line[0] != '#')
                      {
                          var index = line.indexOf('\t')

                          if (index > 0)
                          {
                              val key = line.substring(0, index)

                              index = line.lastIndexOf('\t')

                              if (index > 0)
                              {
                                  val image = line.substring(index + 1)
                                  val gif: GIF

                                  try
                                  {
                                      val inputStream = RESOURCES.obtainResourceStream(
                                              header + image)
                                      gif = GIF(inputStream)
                                      inputStream.close()
                                  }
                                  catch (io: IOException)
                                  {
                                      exception = io
                                      return@readLines
                                  }

                                  this.gifs.add(Pair(key, gif))
                                  this.space = Math.min(this.space, gif.width)
                                  this.height = Math.max(this.height, gif.height)
                              }
                          }
                      }
                  },
                  ({ exception = it }))

        if (exception != null)
        {
            throw exception!!
        }
    }

    /**
     * Compute text description from a String
     *
     * @param text String to get text description
     * @return Text description
     */
    fun computeGifText(text: String): GifText
    {
        var width = 0
        var y = 0
        var x: Int
        val gifPositions = ArrayList<GifPosition>()
        var gif: GIF?

        val stringCutter = StringCutter(text, '\n')
        var line = stringCutter.next()

        while (line != null)
        {
            x = 0

            for (character in line.toCharArray())
            {
                gif = this.gifs.firstOrNull { it.first.indexOf(character) >= 0 }?.second

                if (gif != null)
                {
                    gifPositions.add(GifPosition(x, y, gif))
                    x += gif.width
                }
                else
                {
                    x += this.space
                }
            }

            width = Math.max(x, width)
            y += this.height
            line = stringCutter.next()
        }

        return GifText(width, y, gifPositions)
    }
}