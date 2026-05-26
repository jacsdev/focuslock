# PRD — Ícono de condición climática

**Producto:** FocusLock
**Autor:** jacsdev
**Fecha:** 2026-05-26
**Estado:** Propuesto

---

## 1. Resumen

Hoy FocusLock muestra la **temperatura** de la ciudad seleccionada en el overlay del lock screen. Este PRD añade un **ícono que representa la condición climática actual** (soleado, nublado, lluvia, etc.) junto a esa temperatura, como mejora estética y de experiencia, sin cambiar de proveedor de datos ni alterar la arquitectura existente.

---

## 2. Contexto y estado actual

El flujo de clima ya existe y es minimalista:

| Capa | Archivo | Hoy hace |
|---|---|---|
| Data source | `data/datasource/WeatherDataSource.kt` | `geocode(city)` → lat/lon; `fetchTemperature(lat,lon)` pide solo `current=temperature_2m` |
| Modelo | `data/model/TemperatureResult.kt` | `temperature`, `city`, `timestamp`, `isStale` (>2h) |
| Repositorio | `data/repository/WeatherRepository.kt` | Cachea en DataStore (`weather_cache`); expone `observeTemperature()` |
| Worker | `worker/WeatherSyncWorker.kt` | Job periódico de WorkManager que llama `fetchAndCache(city)` |
| UI state | `ui/overlay/OverlayUiState.kt` | `temperature`, `weatherCity`, `temperatureIsStale`, `showTemperature` |
| UI | `ui/components/TemperatureDisplay.kt` | Muestra `XX °C` + nombre de ciudad |
| Servicio | `service/FocusLockAccessibilityService.kt` | `observeTemperature()` alimenta el `OverlayUiState` |

**Hallazgo clave (API):** Open-Meteo ya soporta la condición climática en el **mismo endpoint** que usamos. Basta con extender el parámetro `current`:

```
https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon
    &current=temperature_2m,weather_code,is_day
```

- `weather_code` → código WMO (0–99) que describe la condición.
- `is_day` → `1` de día, `0` de noche.

No requiere API key, ni nuevo proveedor, ni permisos adicionales.

---

## 3. Decisiones de producto (confirmadas)

| Decisión | Elección |
|---|---|
| **Estilo de ícono** | Vectores a medida (vector drawables minimalistas, alto contraste, alineados al design system) |
| **Granularidad** | Set simple: 6 condiciones |
| **Día / noche** | Sí — sol de día, luna de noche (usa `is_day`) |
| **Ubicación** | Overlay del lock screen **+** vista previa en pantalla de configuración |

---

## 4. Alcance

**Incluye:**
- Pedir y cachear `weather_code` e `is_day`.
- Mapear código WMO → 1 de 6 condiciones.
- 8 vector drawables (las 6 condiciones, con variante día/noche para "despejado" y "parcialmente nublado").
- Mostrar el ícono junto a la temperatura en el overlay y en la preview de configuración.
- Manejo de estados: stale (atenuado), datos ausentes, temperatura nula.

**No incluye (no-objetivos):**
- Animaciones del ícono.
- Pronóstico por horas o días.
- Múltiples ciudades.
- Alertas de clima severo.
- Cambiar el proveedor de datos.

---

## 5. Requisitos funcionales

- **RF01** — Al sincronizar el clima, el sistema obtiene y persiste `weather_code` e `is_day` junto a la temperatura.
- **RF02** — El sistema mapea `weather_code` (+ `is_day`) a una de 6 condiciones visuales.
- **RF03** — El overlay muestra el ícono de la condición junto a la temperatura cuando `showTemperature` está activo.
- **RF04** — Si la temperatura está **stale** (>2h), el ícono se atenúa igual que el texto (gris `0xFFAAAAAA`).
- **RF05** — Si no hay `weather_code` cacheado (cache viejo, fetch fallido), se muestra la temperatura **sin** ícono, sin romper la UI.
- **RF06** — La pantalla de configuración muestra una vista previa con el ícono y la temperatura cacheada.
- **RF07** — La variante día/noche aplica solo a "despejado" y "parcialmente nublado"; el resto usa un único ícono.

---

## 6. Mapeo WMO → condición

Set simple de 6 condiciones. Los ~28 códigos WMO se agrupan así:

| Condición | Códigos WMO | Ícono(s) |
|---|---|---|
| **Despejado** | 0 | `clear_day` (sol) / `clear_night` (luna) |
| **Parcialmente nublado** | 1, 2 | `partly_cloudy_day` (sol+nube) / `partly_cloudy_night` (luna+nube) |
| **Nublado** | 3, 45, 48 *(niebla se pliega aquí)* | `cloudy` |
| **Lluvia** | 51,53,55, 56,57, 61,63,65, 66,67, 80,81,82 | `rain` |
| **Nieve** | 71,73,75, 77, 85,86 | `snow` |
| **Tormenta** | 95, 96, 99 | `thunderstorm` |

**Total de assets:** 8 vector drawables.

Mapeo centralizado en un único punto (enum + función pura), fácil de testear:

```kotlin
enum class WeatherCondition {
    CLEAR_DAY, CLEAR_NIGHT,
    PARTLY_CLOUDY_DAY, PARTLY_CLOUDY_NIGHT,
    CLOUDY, RAIN, SNOW, THUNDERSTORM;

    companion object {
        fun from(code: Int, isDay: Boolean): WeatherCondition = when (code) {
            0        -> if (isDay) CLEAR_DAY else CLEAR_NIGHT
            1, 2     -> if (isDay) PARTLY_CLOUDY_DAY else PARTLY_CLOUDY_NIGHT
            3, 45, 48 -> CLOUDY
            in 51..67, in 80..82 -> RAIN
            71, 73, 75, 77, 85, 86 -> SNOW
            95, 96, 99 -> THUNDERSTORM
            else     -> CLOUDY   // fallback seguro
        }
    }
}
```

---

## 7. Cambios técnicos por archivo

Todo se integra en la arquitectura actual (sin DI nuevo, sin librerías nuevas).

1. **`WeatherDataSource.kt`**
   - Cambiar `fetchTemperature` → `fetchCurrentWeather(lat, lon): CurrentWeather?`.
   - Nuevo URL con `current=temperature_2m,weather_code,is_day`.
   - Parsear `weather_code` (Int) e `is_day` (Int → Boolean) del bloque `current`.
   - Retornar un holder liviano `CurrentWeather(temperature, weatherCode, isDay)`.

2. **Nuevo: `data/model/WeatherCondition.kt`**
   - Enum + `from(code, isDay)` (sección 6).

3. **`data/model/TemperatureResult.kt`**
   - Agregar `weatherCode: Int?` e `isDay: Boolean`.
   - Propiedad derivada `condition: WeatherCondition?` (null si `weatherCode == null`).

4. **`WeatherRepository.kt`**
   - Nuevas keys DataStore: `CACHED_WEATHER_CODE` (int), `CACHED_IS_DAY` (int).
   - `fetchAndCache` persiste código e is_day.
   - `observeTemperature()` incluye los nuevos campos en `TemperatureResult`.

5. **`WeatherSyncWorker.kt`**
   - Sin cambios (sigue llamando `fetchAndCache`).

6. **`OverlayUiState.kt`**
   - Agregar `weatherCondition: WeatherCondition? = null`.

7. **`FocusLockAccessibilityService.kt`** y **`AdminViewModel.kt`**
   - En el `observeTemperature().collect`, mapear `result.condition` al UI state.

8. **`ui/components/WeatherIcon.kt`** (nuevo)
   - Composable que recibe `WeatherCondition` + `isStale` y pinta el vector drawable correcto.

9. **`TemperatureDisplay.kt`**
   - Recibir `condition: WeatherCondition?`; renderizar `WeatherIcon` arriba de la temperatura (ver sección 8).

10. **`res/drawable/`** — 8 vectores nuevos:
    `ic_weather_clear_day`, `ic_weather_clear_night`, `ic_weather_partly_cloudy_day`, `ic_weather_partly_cloudy_night`, `ic_weather_cloudy`, `ic_weather_rain`, `ic_weather_snow`, `ic_weather_thunderstorm`.

---

## 8. Diseño UI / UX

**Principios:** alto contraste, tamaño grande (audiencia de baja visión y adultos mayores), estética coherente con el reloj/anillo de batería (trazos gruesos, blanco/acento sobre fondo AMOLED).

**Overlay (lock screen):** layout vertical centrado, el ícono encima de la temperatura.

```
        ┌──────────┐
        │    ☀     │   ← WeatherIcon, ~96dp, alto contraste
        └──────────┘
          22 °C         ← temperatura (72sp, existente)
          Caracas       ← ciudad (22sp, existente)
```

- **Stale:** ícono y texto en gris `0xFFAAAAAA`.
- **Sin condición (RF05):** se omite el ícono; el bloque temperatura+ciudad se mantiene idéntico al actual.

**Configuración (preview):** fila compacta junto al control de temperatura existente, mostrando ícono + valor cacheado, para que el usuario confirme antes de bloquear el teléfono.

```
  Temperatura     [ ☁  18 °C ]   ⬤ (switch)
```

---

## 9. Estados y edge cases

| Estado | Comportamiento |
|---|---|
| Temp + código OK | Ícono + temperatura normales |
| Temp OK, código ausente (cache viejo) | Solo temperatura, sin ícono; se completa en el próximo sync |
| Temp nula | `-- °C`, sin ícono |
| Stale (>2h) | Ícono + texto atenuados |
| Código WMO desconocido | Fallback a `CLOUDY` (nunca crash) |
| Noche (`is_day=0`) | Luna en despejado / parcialmente nublado; resto sin cambio |

---

## 10. Compatibilidad y migración

- El cache actual (`weather_cache`) solo tiene temp/ciudad/timestamp. Tras el deploy, `weatherCode` será `null` hasta el primer sync → la UI muestra temperatura sin ícono (RF05). **No se requiere migración destructiva.**
- Sin cambios de permisos, sin nuevas dependencias Gradle, sin impacto en el fix reciente del AccessibilityService.

---

## 11. Métricas de éxito / verificación

- Las 6 condiciones (y variantes día/noche) renderizan el ícono correcto para sus códigos WMO representativos.
- El ícono se atenúa correctamente en estado stale.
- Con cache viejo (sin código), la UI no se rompe y muestra solo temperatura.
- Test unitario de `WeatherCondition.from()` cubriendo los rangos de la sección 6 + el fallback.
- Verificación visual en overlay real y en preview de configuración.
- (Opcional) Evento de analytics `weather_condition_shown` con la condición, para entender distribución de uso.

---

## 12. Orden de implementación

1. `WeatherDataSource` — extender request + parseo (`CurrentWeather`).
2. `WeatherCondition` (enum + mapping) + test unitario.
3. `TemperatureResult` + `WeatherRepository` (persistencia y observación).
4. `OverlayUiState` + wiring en servicio y `AdminViewModel`.
5. 8 vector drawables.
6. `WeatherIcon` + integración en `TemperatureDisplay`.
7. Preview en configuración.
8. Verificación (unit + visual).
