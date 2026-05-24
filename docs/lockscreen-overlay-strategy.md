# Estrategia exitosa: Overlay en Lock Screen

## El problema

FocusLock necesita mostrar un widget (hora + batería) encima de la pantalla de bloqueo de Android. La app debe quedar visible desde el momento en que el teléfono enciende la pantalla, antes de que el usuario ingrese su PIN o patrón.

Esto es significativamente más difícil de lo que parece porque Android restringe activamente qué apps pueden dibujar sobre el keyguard.

---

## Estrategias que NO funcionaron

### 1. Activity con `setShowWhenLocked(true)`

La primera aproximación fue lanzar una `Activity` con los flags de lock screen:

```kotlin
setShowWhenLocked(true)
setTurnScreenOn(true)
window.addFlags(FLAG_SHOW_WHEN_LOCKED or FLAG_TURN_SCREEN_ON)
```

**Por qué falló:** A partir de Android 10, las apps no pueden iniciar Activities desde el background. El `BroadcastReceiver` que escucha `ACTION_SCREEN_ON` corre en background, por lo que `startActivity()` es silenciosamente bloqueado por el sistema. El overlay se mostraba, pero recién **después de desbloquear** — visible apenas un instante antes de que la Activity quedara en background.

### 2. Full-Screen Intent via Notificación

El segundo intento usó una notificación con `setFullScreenIntent()` para burlar la restricción de background:

```kotlin
NotificationCompat.Builder(context, channelId)
    .setFullScreenIntent(pendingIntent, true)
    .build()
```

**Por qué falló:** El `FullScreenIntent` sí logra lanzar la Activity sobre el lock screen en Android 10-13. Pero en Android 14+ (`UPSIDE_DOWN_CAKE`) Google introdujo un permiso adicional (`USE_FULL_SCREEN_INTENT`) que los fabricantes OEM como Xiaomi, Samsung y OPPO restringen por defecto. Además, incluso cuando funciona, hay una condición de carrera entre el lanzamiento de la Activity y el keyguard que en algunos dispositivos hace que el overlay quede detrás del keyguard.

### 3. `TYPE_APPLICATION_OVERLAY` (SYSTEM_ALERT_WINDOW)

El tercer intento migró a un `WindowManager.addView()` con tipo `TYPE_APPLICATION_OVERLAY` dentro de un `ForegroundService`:

```kotlin
val params = WindowManager.LayoutParams(
    MATCH_PARENT, MATCH_PARENT,
    TYPE_APPLICATION_OVERLAY,
    FLAG_SHOW_WHEN_LOCKED or FLAG_TURN_SCREEN_ON or ...,
    PixelFormat.TRANSLUCENT,
)
windowManager.addView(overlayView, params)
```

**Por qué falló:** `TYPE_APPLICATION_OVERLAY` tiene un z-order que en la mayoría de los dispositivos queda **debajo del keyguard**. El flag `FLAG_SHOW_WHEN_LOCKED` en este tipo de ventana es ignorado o inconsistente según el OEM y la versión de Android. Xiaomi MIUI, Samsung One UI y otros agresivamente restringen este comportamiento.

---

## La estrategia exitosa: `TYPE_ACCESSIBILITY_OVERLAY`

### Concepto clave

Android tiene un tipo de ventana especial, `TYPE_ACCESSIBILITY_OVERLAY`, disponible únicamente cuando hay un `AccessibilityService` activo. Este tipo fue introducido en API 22 (Android 5.1) específicamente para que los servicios de accesibilidad puedan dibujar sobre **cualquier ventana del sistema**, incluyendo el keyguard.

```
z-order (de menor a mayor):
  Apps normales
  TYPE_APPLICATION_OVERLAY  ← no supera el keyguard consistentemente
  Keyguard / Lock Screen
  TYPE_ACCESSIBILITY_OVERLAY  ← siempre encima del keyguard ✓
```

### Arquitectura implementada

En lugar de un `ForegroundService` (que requería permiso `SYSTEM_ALERT_WINDOW` y mostraba una notificación persistente), FocusLock usa un `AccessibilityService` como componente principal:

```
FocusLockAccessibilityService (AccessibilityService)
├── WindowManager.addView(ComposeView, TYPE_ACCESSIBILITY_OVERLAY)
├── ServiceLifecycleOwner  ← permite que Compose funcione fuera de una Activity
├── ScreenReceiver (registrado en runtime)
│   ├── ACTION_SCREEN_ON  → showOverlay()
│   ├── ACTION_SCREEN_OFF → (no-op)
│   └── ACTION_USER_PRESENT → hideOverlay()
├── BatteryRepository  ← observa nivel/estado de carga
└── ThemeRepository    ← observa el tema de color seleccionado
```

### Código del overlay

```kotlin
@Suppress("DEPRECATION")
private fun showOverlay() {
    if (isOverlayShowing) return
    val keyguard = getSystemService(KeyguardManager::class.java)
    if (!keyguard.isKeyguardLocked) return

    // Dimensiones reales del display (no el área usable)
    // MIUI y otros OEMs recortan MATCH_PARENT al área sin system bars
    val bounds: Rect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        windowManager.currentWindowMetrics.bounds
    } else {
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(dm)
        Rect(0, 0, dm.widthPixels, dm.heightPixels)
    }

    val params = WindowManager.LayoutParams(
        bounds.width(),
        bounds.height(),
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
        PixelFormat.TRANSLUCENT,
    ).apply {
        gravity = Gravity.TOP or Gravity.START
        x = 0
        y = 0
    }

    windowManager.addView(overlayView, params)
}
```

### Por qué `bounds.width/height()` en lugar de `MATCH_PARENT`

En Xiaomi MIUI y HyperOS, el `WindowManager` restringe `MATCH_PARENT` para overlays al área visible (excluyendo status bar y barra de navegación), dejando franjas negras o de color de sistema en los bordes. Al obtener las dimensiones reales del display con `currentWindowMetrics.bounds` (API 30+) o `Display.getRealMetrics()` (API < 30), el overlay ocupa el 100% de la pantalla física independientemente de las restricciones OEM.

### Declaración en el Manifest

```xml
<service
    android:name=".service.FocusLockAccessibilityService"
    android:exported="true"
    android:label="@string/app_name"
    android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
    <intent-filter>
        <action android:name="android.accessibilityservice.AccessibilityService" />
    </intent-filter>
    <meta-data
        android:name="android.accessibilityservice"
        android:resource="@xml/accessibility_service_config" />
</service>
```

### Configuración mínima del servicio

FocusLock no necesita inspeccionar la interfaz de otras apps. La configuración es la mínima posible para declarar un `AccessibilityService` válido:

```xml
<!-- res/xml/accessibility_service_config.xml -->
<accessibility-service
    android:accessibilityEventTypes="typeWindowStateChanged"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:accessibilityFlags="flagDefault"
    android:canRetrieveWindowContent="false"
    android:description="@string/accessibility_service_description"
    android:notificationTimeout="100"
    android:settingsActivity="com.developermind.focuslock.MainActivity" />
```

---

## Ventajas de esta estrategia

| Aspecto | Estrategia anterior | Estrategia actual |
|---|---|---|
| Permiso requerido | SYSTEM_ALERT_WINDOW (manual) | Accesibilidad (guiado) |
| Notificación persistente | Sí (foreground service) | No |
| Funciona en MIUI | No confiable | Sí |
| Funciona en One UI | No confiable | Sí |
| Reinicio tras reboot | Necesita BootReceiver | Android lo restaura solo |
| Condición de carrera con keyguard | Sí | No |
| Requiere activación por el usuario | Permiso en Settings > Apps | Accesibilidad > FocusLock |

## Referencia

La estrategia fue confirmada estudiando el código fuente del proyecto open-source **LockscreenWidgets** (`tk.zwander.lockscreenwidgets`), que usa exactamente este mecanismo en producción con millones de instalaciones en la Play Store.
