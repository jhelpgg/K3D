# Play sounds

For play sounds we use **OpenAL**. 
**OpenAL** permits to have 3D sound effect.
3D sound effect means that a sound have a source position in space.
In other words:
* If the position is at user right (And device can play stereo sounds), the sound is emit at user right.
* If the position is at user left (And device can play stereo sounds), the sound is emit at user left.
* More the position is far for the user, more the level is low
* More the position is near the user, more the level is high

> Constraints:
> * **OpenAL** can only play mono or stereo sounds
> * The 3D sound effect is only possible with mono sound. 
    **OpenAL** will ignore the position for stereo sounds.
> * For now we able to load only **MP3** and some **WAV** files. 
    For **WAV** only internal format support by **JVM** can work.

### Background sound

It is possible to play sounds in background. 
The sound level can be changed only for mono sounds.
The stereo sounds will ignore the level parameter and be played as they are.

Example:

[Code source](../../samples/khelp/samples/k3d/SoundBackground.kt)

Need a 3D window:

````Kotlin
    // 1) Create the 3D scene
    val window3D = Window3D.createSizedWindow(800, 600, "Background sound", true)
````

Get the sound manager:

````Kotlin
    //2) Obtain the sound manager
    val soundManager = window3D.soundManager()
````

Load the sounds in memory:

````Kotlin
    //3) Load the sounds
    val soundKuma = SoundMP3(SoundBackground::class.java.getResourceAsStream("Kuma.mp3"))
    val soundSucceed = SoundWav.create(SoundBackground::class.java.getResourceAsStream("succeed.wav"))
````

Lines above shows how load **MP3** and **WAV** sounds.

Play sounds in background:

````Kotlin
    //4) Play sounds in background
    soundManager.playBackground(soundKuma)
    soundManager.enqueueBackground(soundSucceed)
    soundManager.enqueueBackground(soundKuma)
```` 

Here play the **MP3**, then the **WAV**, and finally the **MP3** again.

> Note:
> * `playBackground` will stop the current sound (if there one) and play the sound immediately
> * `enqueueBackground` if the sound queue is empty and there no current sound, it played immediately. Else the sound is enqueue and wait its turn.

**"Et voilà" :)**

### Sound source

To benefit the 3D effect, it is possible to create sound source and then move it later.

A sound source have a position in space and a queue of sounds to play. It is possible at any time to change these properties.

If a sound source is no more necessary, think about destroy it. 
But instead of destroy one and create an other just after, think about reuse the sound source (Better for memory and performance impact).

Example:

[Code source](../../samples/khelp/samples/k3d/SoundSourceAlone.kt)

Create the window and load the sound:

````Kotlin
    // 1) Create the 3D scene
    val window3D = Window3D.createSizedWindow(800, 600, "Background sound", true)

    // 2) Obtain the sound manager
    val soundManager = window3D.soundManager()

    // 3) Load the sound
    val soundAlya = SoundMP3(SoundSourceAlone::class.java.getResourceAsStream("Alya.mp3"))
````

Create a sound source

````Kotlin
    // 4) Create sound source
    val soundSource = soundManager.createSource()
````

Enqueue the sound several times to able hear the 3D effect.

````Kotlin
    // 5) Play sounds on sound source
    val duration = soundAlya.duration()
    val numberFrame = NUMBER_REPETITION * window3D.timeAnimationToFrameAnimation(duration.toInt())
    (0..NUMBER_REPETITION * 5).forEach { soundSource.enqueueSound(soundAlya) }
````

Move the sound source to have the sound goes left, the right, the center, then go far and finally comes back.

````Kotlin
    // 6) Move the sound source to hear the 3D effect
    window3D.playAnimation {
        when
        {
            it < numberFrame      ->
            {
                // (0, 0, 0) -> (-5, 0, 0)
                soundSource.position((-5f * it) / numberFrame, 0f, 0f)
                true
            }
            it < numberFrame * 2f ->
            {
                // (-5, 0, 0) -> (5, 0, 0)
                soundSource.position((10 * (it - numberFrame)) / numberFrame - 5f, 0f, 0f)
                true
            }
            it < numberFrame * 3f ->
            {
                // (5, 0, 0) -> (0, 0, 0)
                soundSource.position((-5 * (it - 2f * numberFrame)) / numberFrame + 5f, 0f, 0f)
                true
            }
            it < numberFrame * 4f ->
            {
                // (0, 0, 0) -> (0, 0, -10)
                soundSource.position(0f, 0f, (-10f * (it - 3f * numberFrame)) / numberFrame)
                true
            }
            it < numberFrame * 5f ->
            {
                // (0, 0, -10) -> (0, 0, 0)
                soundSource.position(0f, 0f, -10f + (10f * (it - 4f * numberFrame)) / numberFrame)
                true
            }
            else                  -> false
        }
    }
````

More about animations: [Animations](../Animations/Animations.md)

**"Et voilà" :)**

### Link sound source to a node

It is possible to link a sound source to a node. The aim is to give illusion, that is the node that emit the sound.

Example:

[Code source](../../samples/khelp/samples/k3d/SoundSourceFollow.kt)

Create the 3D scene:

````Kotlin
    // 1) Create the 3D scene
    val window3D = Window3D.createSizedWindow(800, 600, "Background sound", true)
    val scene = window3D.scene()
    scene.background(0f,0f,0f)
    val node = Box()
    node.position(0f, 0f, -5f)
    scene.add(node)
````

Prepare the sound source:

````Kotlin
    // 2) Obtain the sound manager
    val soundManager = window3D.soundManager()

    // 3) Load the sound
    val soundAlya = SoundMP3(SoundSourceFollow::class.java.getResourceAsStream("Alya.mp3"))

    // 4) Create sound source
    val soundSource = soundManager.createSource()

    // 5) Play sounds on sound source
    val duration = soundAlya.duration()
    val numberFrame = NUMBER_REPETITION_2 * window3D.timeAnimationToFrameAnimation(duration.toInt())
    (0..NUMBER_REPETITION_2 * 5).forEach { soundSource.enqueueSound(soundAlya) }
````

Link the sound source to the node. After that the sound will follow the node every where it goes.

````Kotlin
    // 6) Link sound source to node
    soundSource.link(node)
````

For hear the 3D effect, move the node:

````Kotlin
    // 7) Move the node to hear the 3D effect
    val animationPositionNode = AnimationPositionNode(node)
    animationPositionNode.addFrame(numberFrame, PositionNode(-8f, 0f, -5f))
    animationPositionNode.addFrame(2 * numberFrame, PositionNode(8f, 0f, -5f))
    animationPositionNode.addFrame(3 * numberFrame, PositionNode(0f, 0f, -5f))
    animationPositionNode.addFrame(4 * numberFrame, PositionNode(0f, 0f, -15f))
    animationPositionNode.addFrame(5 * numberFrame, PositionNode(0f, 0f, -5f))
    window3D.playAnimation(animationPositionNode)
````

More about animations: [Animations](../Animations/Animations.md)
 
**"Et voilà" :)**

[Menu](../Menu.md)