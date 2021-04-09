# AutoFillHole-Plugin
AutoFillHole is an intelligent Minecraft Bukkit Plugin that automatically fills holes as if there never was a hole.

<a href="http://www.youtube.com/watch?feature=player_embedded&v=3H9ZBvCc2uQ
" target="_blank"><img src="http://img.youtube.com/vi/3H9ZBvCc2uQ/0.jpg" 
alt="AutoFillHole tech-demo" width="480" height="360" border="10" /></a>

## Features
  - fills the holes intelligently with surrounding materials
  - generates (kinda) realistic looking terrain as if there never was a hole
  - automatically detects if there is a hole to fill or where the hole ends to prevent your whole world from getting filled
  - works in holes, caves, small lakes, etc.
  - tells you how many blocks were filled
  
## Installation & usage
  Installation is pretty simple, all you need is a bukkit server that at least ran once. Download the AutoFillHole.jar from main branch and put it in your plugins folder & you're ready to go!
  Usage is also quite simple, all you need to do is looking at a hole (max. 10 blocks away) and use the command "/fillhole". Note that you have to look at a block wich is in the hole you want to fill and that has air, water or lava etc. on top of it as this is the block where it will start to fill. The programm will now start filling and tells you when it's done.
  You have to be very carefull with what you fill, if the programm finds just one hole let's say in a cave for example, it will fill the entire cave system, which can take ages. If this is happening, you can force the programm to stop by using the command "/fillhole stop".
  
## Commands
  - /fillhole: the main command that start's the filling
  - /fillhole stop: forcefully stops the programm, failsafe in case it starts to fill unwanted areas
