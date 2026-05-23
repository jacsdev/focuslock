Necesito desarrollar una aplicación Android en Kotlin enfocada en accesibilidad visual para personas con baja visión.

El objetivo es crear una experiencia visual accesible que aparezca inmediatamente al activar la pantalla del teléfono, permitiendo visualizar claramente:
- la hora
- el nivel de batería
- el estado de carga

La aplicación NO debe ser un launcher.
NO debe reemplazar el sistema Android.
NO debe modificar SystemUI.
Debe funcionar como un overlay o pantalla accesible mostrada encima de la lock screen del sistema.

Objetivo principal:
Cuando el usuario presione el botón de encendido o active la pantalla, debe aparecer una interfaz minimalista y altamente legible mostrando:
- Hora grande.
- Indicador circular de batería.
- Porcentaje de batería centrado dentro del anillo.
- Estado de carga.
- Fondo negro AMOLED.
- Alto contraste.
- UI moderna y minimalista.

Tecnologías requeridas:
- Kotlin
- Jetpack Compose
- Material 3
- BatteryManager
- BroadcastReceiver
- Foreground Service
- Compose Canvas
- APIs modernas Android

Arquitectura requerida:

1. Overlay Activity
   Crear una Activity fullscreen capaz de mostrarse sobre la pantalla de bloqueo usando:
- setShowWhenLocked(true)
- setTurnScreenOn(true)

La Activity debe:
- abrirse al encender la pantalla
- ser visualmente minimalista
- cerrarse automáticamente al desbloquear el dispositivo
- ocultar barras del sistema cuando sea posible

2. Monitor de batería
   Crear una clase encargada de obtener:
- porcentaje de batería
- estado de carga
- conexión a corriente
- batería baja
- batería completa

Usar:
- Intent.ACTION_BATTERY_CHANGED
- BatteryManager.EXTRA_LEVEL
- BatteryManager.EXTRA_SCALE
- BatteryManager.EXTRA_STATUS
- BatteryManager.EXTRA_PLUGGED

3. Screen Receiver
   Implementar un BroadcastReceiver para detectar:
- ACTION_SCREEN_ON
- ACTION_SCREEN_OFF
- ACTION_USER_PRESENT

Cuando ocurra ACTION_SCREEN_ON:
- lanzar la Overlay Activity

Cuando ocurra ACTION_USER_PRESENT:
- cerrar la Overlay Activity

4. Foreground Service
   Crear un Foreground Service responsable de:
- mantener activos los receivers
- escuchar cambios de batería
- escuchar eventos de pantalla
- mantener actualizada la UI

Debe mostrar una notificación persistente discreta.

Diseño UI:

Fondo:
- negro puro AMOLED (#000000)

Hora:
- extremadamente grande
- entre 88sp y 110sp
- bold
- color blanco

Indicador circular de batería:
- círculo grueso
- stroke entre 22dp y 30dp
- bordes redondeados
- animación suave
- porcentaje centrado dentro del círculo

Texto batería:
- entre 56sp y 72sp
- bold
- alto contraste

Estados:
- “Cargando”
- “Batería baja”
- “Completa”
- “Desconectado”

Indicador circular:
Debe disminuir visualmente conforme baja la batería.

Ejemplo:
- 100% → círculo completo
- 50% → medio círculo
- 10% → pequeño arco restante

Componente BatteryRing:
Implementar usando Compose Canvas:
- drawArc()
- animaciones suaves
- progresión dinámica

Colores:
- Verde → batería alta
- Amarillo/Naranja → batería media
- Rojo → batería baja
- Blanco → texto principal
- Gris claro → texto secundario

Accesibilidad:
La aplicación está dirigida específicamente a usuarios con baja visión.

Por lo tanto:
- tipografía muy grande
- contraste extremo
- lectura instantánea
- mínima carga visual
- diseño limpio
- evitar elementos pequeños
- evitar exceso de información
- evitar detalles innecesarios

La interfaz debe poder entenderse rápidamente incluso con visión borrosa.

Flujo esperado:
1. Usuario activa la pantalla.
2. El sistema detecta ACTION_SCREEN_ON.
3. La aplicación muestra la Overlay Activity.
4. Se muestra:
    - hora grande
    - batería circular
    - porcentaje
    - estado de carga
5. Usuario obtiene información inmediata.
6. Al desbloquear el dispositivo, el overlay desaparece.

Restricciones:
- No usar root.
- No usar APIs privadas.
- No modificar el lockscreen real del sistema.
- No reemplazar seguridad Android.
- Compatible con Android moderno.

Resultado esperado:
Una aplicación Android moderna en Kotlin que funcione como overlay accesible sobre la pantalla de bloqueo mostrando hora y batería de forma extremadamente legible y minimalista para usuarios con baja visión.