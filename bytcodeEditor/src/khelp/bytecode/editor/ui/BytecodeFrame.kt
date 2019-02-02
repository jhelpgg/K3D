package khelp.bytecode.editor.ui

import khelp.bitcode.compiler.Compiler
import khelp.bitcode.compiler.CompilerException
import khelp.bitcode.compiler.StackInspectorException
import khelp.bitcode.decompiler.decompile
import khelp.bitcode.loader.ClassManager
import khelp.bytecode.editor.resources.BYTECODE_RESOURCES_TEXTS
import khelp.bytecode.editor.resources.INVALID_LINE
import khelp.bytecode.editor.resources.VALID_LINE
import khelp.debug.debug
import khelp.debug.exception
import khelp.debug.warning
import khelp.io.StringInputStream
import khelp.io.StringOutputStream
import khelp.io.computeRelativePath
import khelp.io.createFile
import khelp.io.homeDirectory
import khelp.io.obtainExternalFile
import khelp.io.outsideDirectory
import khelp.preference.Preferences
import khelp.thread.parallel
import khelp.ui.JHelpSeparator
import khelp.ui.action.GenericAction
import khelp.ui.resources.FONT_BUTTON
import khelp.ui.tabbedPane.JHelpTabbedPane
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Stack
import javax.swing.JButton
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane

private const val ACTION_NEW = "New"
private const val ACTION_OPEN = "Open"
private const val ACTION_SAVE = "Save"
private const val ACTION_IMPORT = "Import"
private const val ACTION_EXIT = "Exit"

private const val ACTION_NEW_CLASS = "NewClass"

private const val ACTION_COMPILE = "Compile"
private const val ACTION_LAUNCH = "Launch"

private const val PREFERENCE_CURRENT_DIRECTORY = "CurrentDirectory"
private const val PREFERENCE_LAST_CLASS_FILE = "LastClassFile"

class BytecodeFrame : JFrame("Bytecode editor")
{
    private val tabbedPane = JHelpTabbedPane()
    private var classManager = ClassManager()
    private val console = Console()
    private val doAction = this::playAction
    private val actionLaunch = this.newAction(ACTION_LAUNCH)
    private val preferences = Preferences(obtainExternalFile("BytecodeEditor.pref"))
    private var currentDirectory: File? = null
    private val directoryChooser = JFileChooser(outsideDirectory)
    private val classChooser = JFileChooser(homeDirectory)

    init
    {
        this.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        this.layout = BorderLayout()
        this.tabbedPane.addTab("REMOVE IT", JLabel("REMOVE IT"))
        val buttonNewClass = JButton(this.newAction(ACTION_NEW_CLASS))
        buttonNewClass.font = FONT_BUTTON.font
        this.tabbedPane.addAdditionalComponent(buttonNewClass)

        this.add(this.tabbedPane, BorderLayout.CENTER)

        this.jMenuBar = this.menuBar()
        val panelTop = JPanel(FlowLayout(FlowLayout.LEFT))
        panelTop.add(JButton(this.newAction(ACTION_COMPILE)))
        panelTop.add(JButton(this.actionLaunch))
        this.add(panelTop, BorderLayout.NORTH)
        this.add(JScrollPane(this.console), BorderLayout.SOUTH)

        this.directoryChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        this.directoryChooser.isMultiSelectionEnabled = false
        this.classChooser.fileFilter = ClassFileFilter
        this.classChooser.isMultiSelectionEnabled = false
        this::initialize.parallel(1024)
    }

    private fun initialize()
    {
        this.tabbedPane.removeTab(0)
        this.currentDirectory = this.preferences.getFileValue(PREFERENCE_CURRENT_DIRECTORY)
        this.classChooser.currentDirectory = this.preferences.getFileValue(
                PREFERENCE_LAST_CLASS_FILE)?.parentFile ?: homeDirectory
        this.loadCurrentProject()
    }

    private fun loadCurrentProject()
    {
        this.currentDirectory?.let { directory ->
            if (directory.exists())
            {
                val stack = Stack<File>()
                stack.push(directory)

                while (stack.isNotEmpty())
                {
                    val file = stack.pop()

                    if (file.isDirectory)
                    {
                        for (child in file.listFiles(BytecodeFileFilter))
                        {
                            stack.push(child)
                        }
                    }
                    else
                    {
                        val path = computeRelativePath(directory, file)
                        val index = path.lastIndexOf('.')
                        val name = path.substring(0, index).replace('/', '.')
                        val editor = BytecodeEditor()
                        editor.text = file.readText()
                        this.tabbedPane.addTab(name, JScrollPane(editor))
                    }
                }

                this.directoryChooser.currentDirectory = directory
                this.preferences[PREFERENCE_CURRENT_DIRECTORY] = directory
            }
        }
    }

    private fun playAction(actionKey: String)
    {
        when (actionKey)
        {
            ACTION_NEW       -> this.newProject()
            ACTION_OPEN      -> this.open()
            ACTION_SAVE      -> this.save()
            ACTION_IMPORT    -> this.importClass()
            ACTION_EXIT      -> this.exit()
            ACTION_NEW_CLASS -> this.newClass()
            ACTION_COMPILE   -> this.compile()
            ACTION_LAUNCH    -> this.launch()
        }
    }

    private fun newAction(name: String) = GenericAction(name, this.doAction, resourceText = BYTECODE_RESOURCES_TEXTS)

    private fun addItem(menu: JMenu, name: String) = menu.add(JMenuItem(this.newAction(name)))

    private fun menuBar(): JMenuBar
    {
        val menuBar = JMenuBar()
        menuBar.add(menuFile())
        return menuBar
    }

    private fun menuFile(): JMenu
    {
        val menuFile = JMenu("File")
        this.addItem(menuFile, ACTION_NEW)
        menuFile.add(JHelpSeparator())
        this.addItem(menuFile, ACTION_OPEN)
        this.addItem(menuFile, ACTION_SAVE)
        menuFile.add(JHelpSeparator())
        this.addItem(menuFile, ACTION_IMPORT)
        menuFile.add(JHelpSeparator())
        this.addItem(menuFile, ACTION_EXIT)
        return menuFile
    }

    private fun compile(): List<String>
    {
        this.classManager = ClassManager()
        this.console.clear()
        var valid = true
        val classList = ArrayList<String>()

        for (tab in 0 until this.tabbedPane.tabCount)
        {
            val tabComponent = this.tabbedPane[tab]
            debug("Compiling : ", tabComponent.text)
            val editor = (tabComponent.component as JScrollPane).viewport.view as BytecodeEditor
            val className = this.compile(editor)

            if (className.isEmpty())
            {
                valid = false
            }
            else
            {
                classList += className
            }
        }

        if (valid)
        {
            return classList
        }

        return emptyList()
    }

    private fun compile(bytecodeEditor: BytecodeEditor): String
    {
        bytecodeEditor.removeAllTemporaryModification()

        try
        {
            val className = this.classManager.addASM(bytecodeEditor.text)

            this.currentDirectory?.let { directory ->
                val file = File(directory,
                                "out" + File.separator + className.replace('.', File.separatorChar) + ".class")
                createFile(file)
                val compiler = Compiler()
                compiler.compile(StringInputStream(bytecodeEditor.text), FileOutputStream(file))
            }

            debug(className, " compiled!")
            return className
        }
        catch (compilerException: CompilerException)
        {
            exception(compilerException)

            if (compilerException is StackInspectorException)
            {
                compilerException.path.forEach { stackInfo ->
                    bytecodeEditor.changeTemporaryLineNumberBackground(stackInfo.lineNumber, VALID_LINE)
                    bytecodeEditor.addTemporaryTextInformation(stackInfo.lineNumber, stackInfo.information())
                }

                bytecodeEditor.changeTemporaryLineNumberBackground(compilerException.path.last().lineNumber,
                                                                   INVALID_LINE)
            }

            return ""
        }
    }

    private fun launch()
    {
        val classList = this.compile()
        var mainLaunched = false
        var error = false

        for (className in classList)
        {
            try
            {
                this.classManager.invokeMethodStatic<Any, Unit>(className, "main", *emptyArray<String>())
                mainLaunched = true
                this.currentDirectory?.let { directory ->
                    var file = File(directory, "out/launch.sh")
                    createFile(file)
                    file.writeText("cd ${directory.absolutePath}/out\njava $className")
                    file = File(directory, "out/launch.bat")
                    createFile(file)
                    file.writeText("cd ${directory.absolutePath}\\out\njava $className")
                }
                break
            }
            catch (ignored: IllegalArgumentException)
            {
                // Ignored
            }
            catch (exception: Exception)
            {
                exception(exception)
                error = true
            }
        }

        if (!mainLaunched && !error)
        {
            warning("No main found!")
        }
    }

    private fun newProject()
    {
        this.tabbedPane.clear()
        this.currentDirectory = null
    }

    private fun open()
    {
        val result = this.directoryChooser.showOpenDialog(this.tabbedPane)

        if (result == JFileChooser.APPROVE_OPTION)
        {
            if (this.directoryChooser.selectedFile.name == "." || this.directoryChooser.selectedFile.name == "..")
            {
                this.currentDirectory = this.directoryChooser.selectedFile.parentFile
            }
            else
            {
                this.currentDirectory = this.directoryChooser.selectedFile
            }

            this.tabbedPane.clear()
            this.loadCurrentProject()
        }
    }

    private fun save()
    {
        if (this.currentDirectory == null)
        {
            val result = this.directoryChooser.showSaveDialog(this.tabbedPane)

            if (result == JFileChooser.APPROVE_OPTION)
            {
                if (this.directoryChooser.selectedFile.name == "." || this.directoryChooser.selectedFile.name == "..")
                {
                    this.currentDirectory = this.directoryChooser.selectedFile.parentFile
                }
                else
                {
                    this.currentDirectory = this.directoryChooser.selectedFile
                }
            }
        }

        this.currentDirectory?.let { directory ->
            this.console.clear()
            debug("Saving in ", directory.absolutePath, " ...")
            this.preferences[PREFERENCE_CURRENT_DIRECTORY] = directory

            for (tab in 0 until this.tabbedPane.tabCount)
            {
                val tabComponent = this.tabbedPane[tab]
                val editor = (tabComponent.component as JScrollPane).viewport.view as BytecodeEditor
                val file = File(directory,
                                tabComponent.text.replace('.', File.separatorChar) + "." + BYTECODE_EXTENSION)
                createFile(file)
                file.writeText(editor.text)
                debug("${tabComponent.text} saved!")
            }

            debug("Save done!")
        }
    }

    private fun importClass()
    {
        if (this.classChooser.showOpenDialog(this.tabbedPane) == JFileChooser.APPROVE_OPTION)
        {
            val file = this.classChooser.selectedFile
            val inputStream = FileInputStream(file)
            val outputStream = StringOutputStream()
            val className = decompile(inputStream, outputStream, file.name)
            val editor = BytecodeEditor()
            editor.text = outputStream.string
            this.tabbedPane.addTab(className, JScrollPane(editor))
            this.preferences[PREFERENCE_LAST_CLASS_FILE] = file
        }
    }

    private fun exit()
    {
        this.isVisible = false
        this.dispose()
        System.exit(0)
    }

    private fun newClass()
    {
        val completeName = JOptionPane.showInputDialog("Class complete name")

        if (completeName != null && completeName.isNotEmpty())
        {
            val editor = BytecodeEditor()
            editor.text = "class $completeName\n\n"
            this.tabbedPane.addTab(completeName, JScrollPane(editor))
        }
    }
}