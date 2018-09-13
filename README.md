# evolution-AI
  I wanted to experiment with learning AI so I made this program to experiment with evolution/AI.

  Little circle begins without much intelligence and moves randomly. Upon colliding with an object, the circle dies but creates
a new circle that is smarter and remembers what caused the previous death. With more deaths and more generations, it becomes smarter
 and could prevent dying and reaching the end.

 At a certain level of intelligence, movement is upgraded and randomness is greatly reduced and instead movement is based on what gets
 the circle closer to the end location.

 Fusion is a mutation that can be attained and happens when 2 circles that can fuse collide, most traits are either the first or second's
 but some are added together

 After more time, now looks at tiles nearby instead of just coordinates. This greatly improves speed and prevents death and learns much quicker
 Also makes sure to get through every tile if there is a dead end instead of just getting stuck in loop

 Added random map generation with 100% chance of always being a way to get to end

   TODO:
* Improve random map generation and add starting with random movement again, however currently takes a while to get there
* Right now set to start at last evolution, will change later