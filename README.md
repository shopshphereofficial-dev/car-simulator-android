# Car Simulator (Android)

Ek top-down open-city car driving simulator, native Android (Kotlin) mein, koi external game engine nahi.
Saara rendering custom `View` + `Canvas` se hota hai.

## Features
- Open city: road grid, buildings, parks, fuel stations
- Arcade car physics: accelerate, brake, steering
- Traffic AI cars jo roads par chalti hain
- Collisions (buildings + traffic) se damage
- Fuel system + refuel stations
- Coins collect karo
- HUD: speedometer, fuel bar, damage bar, minimap, coins
- Ek driver character jo react karta hai (khush, crash pe shocked, low fuel pe worried)

## Controls (landscape)
- Bottom-left: `<` aur `>` steering
- Bottom-right: `BRAKE` aur `GAS`
- Multitouch: ek saath steer + gas daba sakte ho

## Build (GitHub Actions)
`main` branch par push karne se APK automatically build hoti hai (`.github/workflows/build.yml`).
Actions tab -> latest run -> `car-simulator-apk` artifact download karo.

## Install
APK ko phone par copy karke "Install from unknown sources" allow karke install karo.
