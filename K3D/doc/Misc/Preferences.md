# Preferences

With [Window3D](../../src/khelp/k3d/render/Window3D.kt) is associate a [Preferences](../../../Utilities/src/khelp/preference/Preferences.kt)
file.

Preferences is a map with a key and a value associated. It is stored on system to be able persist data after close and 
relaunch application.
They are automatically save, so need to care about it.

To get window's preferences:

````Kotlin
val preferences = window3D.preferences()
````

* For read a value use one of **`get`** method
* For write a value use one of **`set`** method 
 
**"Et voilà" :)**

[Menu](../Menu.md)