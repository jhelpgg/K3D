package khelp.fileManager.ui

import khelp.database.sqlite.DatabaseAccessSQLite
import khelp.debug.debug
import khelp.fileManager.database.FileDatabase
import khelp.fileManager.database.FileType
import khelp.images.JHelpImage
import khelp.images.dynamic.DynamicImage
import khelp.images.gif.DynamicAnimationGIF
import khelp.images.gif.GIF
import khelp.images.gif.isGIF
import khelp.io.outsideDirectory
import khelp.thread.SwingContext
import khelp.thread.parallel
import khelp.ui.JHelpImageComponent
import khelp.ui.centerOnScreen
import khelp.ui.layout.CenterLayout
import khelp.ui.packedSize
import khelp.util.startCoroutine
import java.awt.BorderLayout
import java.awt.GridLayout
import java.awt.event.WindowEvent
import java.io.File
import java.io.FileInputStream
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane

class FileManagerFrame : JFrame("File Manager")
{
    private val fileDatabase: FileDatabase
    private val fillUpdateDatabase: () -> Unit
    private val imagePreview = JHelpImageComponent(512, 512)
    private var dynamicImage: DynamicImage? = null
    private var dynamicAnimationGIF: DynamicAnimationGIF? = null
    private var imagesIterator: Iterator<File>? = null
    private val label = JLabel("...", JLabel.CENTER)
    private val buttonNext = JButton("NEXT IMAGE")

    init
    {
        this.defaultCloseOperation = JFrame.DO_NOTHING_ON_CLOSE
        this.layout = BorderLayout()
        this.add(this.label, BorderLayout.NORTH)
        val panelCenter = JPanel(CenterLayout())
        panelCenter.add(this.imagePreview)
        this.add(JScrollPane(panelCenter), BorderLayout.CENTER)
        this.buttonNext.addActionListener({ this::nextImage.parallel() })
        val buttonRefresh = JButton("REFRESH")
        val panelSouth = JPanel(GridLayout(1, 0))
        panelSouth.add(this.buttonNext)

        // panelSouth.add(buttonRefresh)

        this.add(panelSouth, BorderLayout.SOUTH)
        packedSize(this)
        centerOnScreen(this)

        this.fileDatabase = FileDatabase()
        this.fillUpdateDatabase = { this.fileDatabase.add(*File.listRoots()) }
        buttonRefresh.addActionListener { this.fillUpdateDatabase.parallel() }
    }

    private fun nextImage()
    {
        {
            this.buttonNext.isEnabled = false
            this.buttonNext.text = "LOADING ..."
        }.startCoroutine(SwingContext)


        if (!(this.imagesIterator?.hasNext() ?: false))
        {
            this.imagesIterator = this.fileDatabase.filesByType(FileType.IMAGE)
        }

        var ko = true

        while (this.imagesIterator?.hasNext() ?: false && ko)
        {
            try
            {
                val file = this.imagesIterator?.next()
                debug("file=", file?.absolutePath)

                if (file != null)
                {
                    if (isGIF(file))
                    {
                        if (this.dynamicAnimationGIF != null)
                        {
                            this.dynamicImage?.stopAnimation(this.dynamicAnimationGIF!!)
                        }

                        val gif = GIF(FileInputStream(file))
                        this.dynamicAnimationGIF = DynamicAnimationGIF(gif)
                        this.dynamicImage = DynamicImage(gif.width, gif.height)
                        this.dynamicImage?.playAnimation(this.dynamicAnimationGIF!!)
                        this.imagePreview.invalidate()
                        this.imagePreview.image(this.dynamicImage!!.image)
                    }
                    else
                    {
                        if (this.dynamicAnimationGIF != null)
                        {
                            this.dynamicImage?.stopAnimation(this.dynamicAnimationGIF!!)
                        }

                        this.dynamicAnimationGIF = null
                        this.dynamicImage = null
                        this.imagePreview.image(JHelpImage.loadImage(file))
                    }

                    this.label.text = file.absolutePath
                }

                ko = false
            }
            catch (ignored: Exception)
            {
                ko = true
            }
        }

        {
            this.imagePreview.revalidate()
            this.imagePreview.repaint()
            this.buttonNext.isEnabled = true
            this.buttonNext.text = "NEXT IMAGE"
            this.buttonNext.requestFocus()
        }.startCoroutine(SwingContext)
    }

    override fun processWindowEvent(windowEvent: WindowEvent)
    {
        when (windowEvent.id)
        {
            WindowEvent.WINDOW_CLOSING ->
            {
                this.fileDatabase.close()
                this.isVisible = false
                this.dispose()
                System.exit(0)
            }
        }

        super.processWindowEvent(windowEvent)
    }
}