# Overview

Project Moonglade is a 3D sandbox game written in java that has an emphasis on procedural generation.
It uses OpenGL for rendering via LWJGL2, and uses its own engine.

# Required Libraries
- [LWJGL2] https://www.lwjgl.org/
- [JOML] https://github.com/JOML-CI/JOML
- [Slick2D] http://slick.ninjacave.com/
- [JOrbis] http://www.jcraft.com/jorbis/
- [Ini4j] http://ini4j.sourceforge.net/
- [JSquish] https://github.com/memo33/jsquish

# Screenshots
![nature1](https://i.imgur.com/8ZpGrsY.png)
![nature2](https://i.imgur.com/15lcR5w.png)
![nature3](https://i.imgur.com/wHwKcxB.png)

# Compilation
Include the required libraies in your project's build path, as well as the source code to this project.

# Running the Project
Check the releases for compiled builds, unzip the contents into a folder, and run the jar file. Should any issues arise (such as the program terminating immediately on startup, you can trace the cause of the problem by running this batch file in the same directory as the jar instead of running the jar directly:

<code>
java -jar (JARNAME).jar
@pause
</code>
