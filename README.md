# Mi-Tiempo

Manejo del tiempo personal, de trabajo y ocio.

App Android para marcar pautas de tiempo en tareas: gimnasio (tiempo por ejercicio, pausas entre series) o estudio (tiempos por temas y asignaturas), con alarmas para empezar y terminar cada bloque.

## Estado

- Pantalla principal con la lista de rutinas (crear con el botón +, ejecutar al tocar, eliminar con pulsación larga).
- Pantalla de edición: nombre de la rutina y bloques de tarea/pausa con duración editable en segundos, reordenables y eliminables.
- Pantalla de ejecución: temporizador grande con bloque actual, siguiente bloque, empezar/pausar/reanudar y reiniciar.
- Persistencia con Room (rutinas y bloques).

## Plan

1. Pantalla de ejecución con temporizador fijo. ✅
2. Rutinas guardadas con Room y pantalla de edición. ✅
3. Alarmas/notificaciones entre bloques.
4. Extras futuros: widgets, historial, estadísticas.

## Cómo abrir

1. Abre Android Studio → *File > Open* → selecciona esta carpeta.
2. Deja que Gradle sincronice (descarga dependencias automáticamente; incluye Room con procesado por kapt, puede tardar un poco la primera vez).
3. Ejecuta la configuración `app` en un emulador o dispositivo físico.

## Requisitos

- Android Studio (Koala o superior recomendado)
- Min SDK 24 (Android 7.0)

## Estructura

- `app/src/main/java/com/example/controltiempo/data/` — entidades, DAO y base de datos Room.
- `app/src/main/java/com/example/controltiempo/MainActivity.kt` — lista de rutinas.
- `app/src/main/java/com/example/controltiempo/EditRoutineActivity.kt` — creación/edición de rutinas.
- `app/src/main/java/com/example/controltiempo/RunActivity.kt` — ejecución de la rutina con temporizador.
