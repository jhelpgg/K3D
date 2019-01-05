package khelp.ui.action

import khelp.debug.verbose
import khelp.images.JHelpImage
import khelp.resources.ResourceText
import khelp.resources.ResourceTextListener
import khelp.thread.parallel
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.Action
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.KeyStroke

class GenericAction(keyName: String, private val action: (String) -> Unit,
                    smallIcon: JHelpImage? = null, largeIcon: JHelpImage? = null,
                    keyTooltip: String? = null,
                    resourceText: ResourceText? = null) : AbstractAction()
{
    private inner class EventManager() : ResourceTextListener
    {
        override fun resourceTextLanguageChanged(resourceText: ResourceText)
        {
            this@GenericAction.doResourceTextLanguageChanged(resourceText)
        }
    }

    var keyName = keyName
        private set
    var keyTooltip = keyTooltip
        private set
    var resourceText = resourceText
        private set

    init
    {
        this.resourceText?.register(EventManager())
        this.putValue(Action.NAME, this.keyName)
        val small = smallIcon ?: largeIcon
        val large = largeIcon ?: smallIcon

        if (small != null)
        {
            this.putValue(Action.SMALL_ICON, small)
        }

        if (large != null)
        {
            this.putValue(Action.LARGE_ICON_KEY, large)
        }

        this.keyTooltip?.let { this.putValue(Action.SHORT_DESCRIPTION, it) }
    }

    override final fun putValue(key: String, newValue: Any?)
    {
        var value = newValue

        when
        {
            Action.NAME == key                                       ->
                if (value == null)
                {
                    throw NullPointerException("value for name mustn't be null")
                }
                else
                {
                    val name = value.toString()
                    this.keyName = name
                    value = this.resourceText?.let { it.text(name) } ?: name
                }
            Action.SHORT_DESCRIPTION == key                          ->
                if (value == null)
                {
                    this.keyTooltip = null
                }
                else
                {
                    val name = value.toString()
                    this.keyTooltip = name
                    value = this.resourceText?.let { it.text(name) } ?: name
                }
            Action.SMALL_ICON == key || Action.LARGE_ICON_KEY == key ->
                if (value != null)
                {
                    if (value !is Icon)
                    {
                        throw IllegalArgumentException(
                                "A javax.swing.Icon or a jhelp.util.gui.JHelpImage should be associate to the key $key")
                    }

                    value = JHelpImage.toJHelpImage(value)
                }
            Action.ACCELERATOR_KEY == key                            -> Unit
            "enabled" != key                                         ->
            {
                verbose("The key $key is not managed!")
                return
            }
        }

        super.putValue(key, value)
    }

    internal final fun doResourceTextLanguageChanged(resourceText: ResourceText)
    {
        this.putValue(Action.NAME, resourceText.text(this.keyName));
        this.keyTooltip?.let { this.putValue(Action.SHORT_DESCRIPTION, resourceText.text(it)); }
    }

    override final fun actionPerformed(actionEvent: ActionEvent)
    {
        this.action.parallel(this.keyName)
    }

    final fun largeIcon() = this.getValue(Action.LARGE_ICON_KEY)?.let { it as JHelpImage }

    final fun largeIcon(largeIcon: JHelpImage?) = this.putValue(Action.LARGE_ICON_KEY, largeIcon)

    final fun smallIcon() = this.getValue(Action.SMALL_ICON)?.let { it as JHelpImage }

    final fun smallIcon(smallIcon: JHelpImage?) = this.putValue(Action.SMALL_ICON, smallIcon)

    final fun name() = this.keyName

    final fun name(name: String) = this.putValue(Action.NAME, name)

    final fun toolTip() = this.keyTooltip

    final fun toolTip(toolTip: String?) = this.putValue(Action.SHORT_DESCRIPTION, toolTip)

    final fun printName() = this.getValue(Action.NAME) as String

    final fun printToolTip() = this.getValue(Action.SHORT_DESCRIPTION)?.let { it as String }

    final fun shortcut() = this.getValue(Action.ACCELERATOR_KEY)?.let { it as KeyStroke }

    final fun shortcut(keyStroke: KeyStroke?) = this.putValue(Action.ACCELERATOR_KEY, keyStroke)

    final fun setIcons(icon: JHelpImage?)
    {
        this.smallIcon(icon)
        this.largeIcon(icon)
    }
}

fun JComponent.associateShortcut(keyStroke: KeyStroke, genericAction: GenericAction)
{
    val actionMap = this.actionMap
    val inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
    genericAction.shortcut(keyStroke)
    actionMap.put(genericAction.name(), genericAction)
    inputMap.put(keyStroke, genericAction.name())
}

fun JFrame.associateShortcut(keyStroke: KeyStroke, genericAction: GenericAction) =
        this.rootPane.associateShortcut(keyStroke, genericAction)

fun JComponent.dissociateShortcut(genericAction: GenericAction)
{
    val actionMap = this.actionMap
    val inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
    val shortcut = genericAction.shortcut()
    genericAction.shortcut(null)
    actionMap.remove(genericAction.name())
    inputMap.remove(shortcut)
}

fun JFrame.dissociateShortcut(genericAction: GenericAction) =
        this.rootPane.dissociateShortcut(genericAction)