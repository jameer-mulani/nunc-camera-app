### Camara Application made with **CameraX** & **Exoplayer**, designed by MVVM + Clean architecture.

**``App Version:1.0``**
**``Min SDK:21``**
**``Compile sdk:34``**
**``Target Sdk:34``**

#### Functional Features:
1. Showing camera preview.
2. Start and stop recording.
4. Handling flash light.
5. Switching camera between Front and Rear.
6. Pausing recording session.
7. Resuming recording sessio.
8. Displaying recording session Timer.
9. Save recorded files based on OS version.
10. Loading and showing recorded files into the UI
11. Playing recorded videos with inbuilt ExoPlayer.

#### Screens(UI) Elements:
##### Screen1 (Video list screen):
1. Displays list of recorded videos from the app which stores onto the device storage.
2. Loads the videos based on OS version.
3. Allows user to navigate to record a new a video.
4. If no recorded videos available then shows empty view.
5. Allows user to navigate to video player screen when recorded video list item is selected.

##### Screen2 (Video capture screen):
1. Displays camera preview.
2. Allows user to start and stop recording session.
3. Allows user to pause and resume on-going recording session.
4. Displays timer when recording is on-going.
5. Blinks the timer when recording is in paused state.
6. Allows user to switch the camera preview.
7. Handles and create appropriate output options based on version, for OS P and below devices uses ```FileOutputOptions``` and for OS Q above devices uses ```MediaStoreOutputOptions```.

##### Screen3 (Video player screen):
1. Integrated Exoplayer.
2. Plays the one of selected recorded file from video list screen.

#### Tech stack:
1. Navigation component: uses navgraph and navhost fragment for in app destination navigation
2. Hilt : uses to provide dependency injection.
3. Glide : uses to load video thumbnails.
4. Lifecycle component : uses ViewModel and Livedata.
5. Coroutine : uses to load the data asynchronously.
6. CameraX: use to achieve camera releated functionalities.
7. Exoplayer : uses to play in app video playing experience.
8. Databinding : to bind xml and data.


