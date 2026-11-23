# Actividades Ahora (Android)

Mapa interactivo de actividades que la gente está haciendo **en este momento**.
Escrito en Kotlin con Jetpack Compose y Material 3. El mapa usa OpenStreetMap (osmdroid), sin clave de API.

## Qué hace (versión 1, datos de demostración)

- **Mapa:** muestra las actividades activas cerca de ti, con un ícono por tipo
  (💬 conversación, ⚽ deportes, 🎲 juegos de mesa, ❤️ citas, 🤝 amistad).
  Se puede filtrar por tipo. Cada actividad desaparece sola cuando se acaba su tiempo.
- **Crear actividad:** título, tipo, tema, lugar, descripción, duración (1–4 h), requisitos
  (ej. "trae cerveza"), cupo y filtro de entrada. Aparece en tu ubicación actual.
- **Detalle de actividad:** quién está dentro, requisitos, filtro de entrada y botón *Unirme*.
- **Filtro de entrada:** el anfitrión puede pedir una calificación mínima y excluir a personas
  con reportes de conflicto. Si no cumples, la app te dice por qué no puedes entrar.
- **Perfil:** calificación promedio, reportes de conflicto, historial de actividades y opiniones recibidas.
- **Calificar:** cuando una actividad termina, los participantes pueden calificarse (1–5 ★),
  dejar un comentario y reportar un conflicto.

Todo vive en memoria del teléfono, con usuarios y actividades de ejemplo. Al cerrar la app se
reinicia. Para que distintas personas vean las mismas actividades hace falta un servidor
(ver "Siguientes pasos").

## Abrir y ejecutar

1. Instala [Android Studio](https://developer.android.com/studio).
2. **File → Open** y elige esta carpeta `android-app`.
3. Pulsa **Run ▶** con un emulador o un teléfono conectado por USB.

Desde la terminal (requiere Android SDK y JDK 17+):

```sh
./gradlew testDebugUnitTest   # pruebas de las reglas de entrada
./gradlew assembleDebug       # APK en app/build/outputs/apk/debug/
```

Cada push que cambie `android-app/` compila la app en GitHub (pestaña **Actions**),
donde se puede descargar el APK `app-debug`.

## Estructura

| Archivo | Contenido |
|---------|-----------|
| `model/Models.kt` | Actividad, usuario, reseña y la regla `checkJoin` (filtro de entrada) |
| `data/ActivityRepository.kt` | Almacén en memoria: crear, unirse, salir, calificar |
| `data/DemoData.kt` | Usuarios, actividades y reseñas de ejemplo |
| `ui/map/` | Pantalla del mapa, marcadores y ubicación |
| `ui/detail/` | Detalle de una actividad |
| `ui/create/` | Formulario para crear una actividad |
| `ui/profile/` | Perfil con historial y opiniones |

## Siguientes pasos

- Servidor y cuentas reales (por ejemplo Firebase o Supabase) para compartir actividades entre teléfonos.
- Privacidad: para actividades en casas, mostrar una ubicación aproximada y dar la dirección
  exacta solo a quien fue aceptado.
- Aprobación del anfitrión antes de entrar, chat del grupo, verificación de edad para citas.
