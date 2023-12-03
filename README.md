https://github.com/nukmuk/mc-3d-animation-gyatt-particle/assets/10235825/b6bb5278-4c10-4f04-b4aa-6e9159921d18

# Custom Shiny (.shny) file format ✨☺️
Every frame starts with an int16 which defines how many particles the frame has. After that comes the particles which are 10 bytes each. After the last particle in the frame the next frame starts with an int16 again. The file uses little endian byte order.  

Particle format: 3\*2 bytes (position) float16, 1 byte (blue) uint8, 1 byte (green) uint8, 1 byte (red) uint8, 1 byte (pscale) uint8
total 10bytes per particle  

### Simple example animation with 3 frames and 2 particles per frame:  
```hex
02 00 00 3C 00 40 00 42 FF 00 FF FF 00 40 00 40 00 42 FF 00 FF FF 02 00 00 40 00 40 00 42 FF 00 FF FF 00 42 00 40 00 42 FF 00 FF FF 02 00 00 42 00 40 00 42 FF 00 FF FF 00 44 00 40 00 42 FF 00 FF FF
```

Explained:
```hex
02 00 (first frame length: 2 particles, uint16)

00 3C (x, float16)
00 40 (y, float16)
00 42 (z, float16)
FF 00 FF (color: purple, format is BGR, uint8)
FF (pscale: 255 uint8, can be interpreted as alpha channel of the color so the color would be stored as int32)

00 40 (start of the second particle)
00 40
00 42
FF 00 FF
FF


02 00 (second frame length with 2 particles too)

00 40
00 40
00 42
FF 00 FF
FF

00 42
00 40
00 42
FF 00 FF
FF


02 00 (3rd frame)

00 42
00 40
00 42
FF 00 FF
FF

00 44
00 40
00 42
FF 00 FF
FF
```

### Inspired by https://www.youtube.com/watch?v=iwhotujrJqE
