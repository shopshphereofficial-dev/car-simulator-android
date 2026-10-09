# Car Simulator 3D (Android)

Ek 3D open-city car driving simulator, native Android (Kotlin) + OpenGL ES 2.0.
Koi external game engine nahi — renderer aur physics sab custom code.

## Features
- Real 3D scene: third-person chase camera car ke peeche
- Open city — road grid, 3D buildings, fuel stations
- Low-poly 3D car (body, cabin, wheels) + directional lighting
- Traffic AI cars jo roads par chalti hain
- Collisions (buildings + traffic) se damage
- Fuel system + refuel stations
- Coins collect karo (ghoomte hue 3D coins)
- HUD: speedometer, fuel bar, damage bar, minimap, coins
- Driver character jo react karta hai (khush, crash pe shocked, low fuel pe worried)

## Controls (landscape)
- Bottom-left: `<` aur `>` steering
- Bottom-right: `BRAKE` aur `GAS`
- Multitouch: ek saath steer + gas

## Build (GitHub Actions)
`main` branch par push karne se APK automatically build hoti hai (`.github/workflows/build.yml`).
Actions tab -> latest run -> `car-simulator-apk` artifact download karo.

## Install
APK ko phone par copy karke "Install from unknown sources" allow karke install karo.
