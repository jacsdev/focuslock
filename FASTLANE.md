# FocusLock — Manual de Fastlane

Guía práctica para publicar releases de FocusLock en Google Play usando Fastlane.

---

## Prerequisitos

Antes de ejecutar cualquier lane, verificar que estés en la raíz del proyecto y que las dependencias estén instaladas:

```bash
cd ~/AndroidStudioProjects/FocusLock
bundle install
```

Verificar que la conexión con Google Play API funciona:

```bash
bundle exec fastlane run validate_play_store_json_key \
  json_key:/Users/jacsdev/android-keys/focuslock/focuslock-497323-7bd8c9a17190.json
```

Debe responder: `Successfully established connection to Google Play Store.`

---

## Build local (sin subir a Play Store)

Útil para probar el bundle firmado antes de publicar, o para subir manualmente.

```bash
bundle exec fastlane local
# o equivalente:
./build_release.sh
```

**Qué hace:**
1. Lee el `versionCode` actual en `app/build.gradle.kts` y lo incrementa en 1
2. Compila el AAB firmado con la upload key
3. Copia el AAB a `~/Downloads/focuslock-release-vN-TIMESTAMP.aab`

**Output esperado:**
```
versionCode: 7 → 8
Bundle copiado: /Users/jacsdev/Downloads/focuslock-release-v8-20260524_2130.aab
```

---

## Publicar en Internal Testing

El primer paso de toda release. Llega solo a los testers internos que hayas agregado en Play Console.

```bash
bundle exec fastlane internal
```

**Qué hace:**
1. Incrementa `versionCode` automáticamente
2. Compila el AAB firmado
3. Sube el AAB al track **Internal Testing** en estado **draft**

**Después de ejecutarlo:**
1. Ir a [play.google.com/console](https://play.google.com/console) → FocusLock → Testing → Internal testing
2. Verás el release en estado borrador — revisar y click **Review release**
3. Si hay advertencias, leerlas y confirmar
4. Click **Start rollout to Internal Testing**
5. Los testers internos recibirán la actualización

> El estado `draft` es intencional: te da la oportunidad de revisar en Play Console antes de que llegue a los testers.

---

## Promover a Alpha (Closed Testing)

Cuando el internal testing está estable y querés ampliar el grupo de testers.

```bash
bundle exec fastlane alpha
```

**Qué hace:** Mueve el release actual de Internal → Alpha. **No compila ni incrementa versión.**

**Después de ejecutarlo:**
1. Play Console → Testing → Closed testing (Alpha)
2. Revisar y publicar el release
3. Agregar el grupo de testers alpha si no existe

---

## Promover a Beta (Open Testing)

Testing abierto, cualquiera puede unirse desde la página de Play Store.

```bash
bundle exec fastlane beta
```

**Qué hace:** Mueve el release de Alpha → Beta. **No compila ni incrementa versión.**

**Después de ejecutarlo:**
1. Play Console → Testing → Open testing (Beta)
2. Revisar y publicar

---

## Publicar en Producción

Solo cuando el release pasó por Beta y está probado.

```bash
bundle exec fastlane deploy
```

**Qué hace:** Promueve el release de Beta → Production. **No compila ni incrementa versión.**

**Después de ejecutarlo:**
1. Play Console → Production
2. Elegir el porcentaje de rollout (recomendado: empezar con 10–20%)
3. Click **Start rollout to Production**

> Google Play puede tardar horas en aprobar y distribuir el release.

---

## Flujo completo de una release

```
1. bundle exec fastlane internal   →  subir a internal (draft)
        ↓
2. Play Console: revisar y publicar internal
        ↓
3. Probar con testers internos (días / semanas)
        ↓
4. bundle exec fastlane alpha      →  promover a alpha
        ↓
5. bundle exec fastlane beta       →  promover a beta
        ↓
6. bundle exec fastlane deploy     →  promover a producción
        ↓
7. Play Console: configurar % de rollout y publicar
```

---

## Referencia rápida

| Comando | Builds | Sube AAB | Track destino |
|---|:---:|:---:|---|
| `fastlane local` | ✅ | ❌ | — (copia local) |
| `fastlane internal` | ✅ | ✅ | Internal Testing |
| `fastlane alpha` | ❌ | ❌ | Alpha |
| `fastlane beta` | ❌ | ❌ | Beta |
| `fastlane deploy` | ❌ | ❌ | Production |

---

## Errores frecuentes

**`Google Api Error: forbidden`**
La service account no tiene permisos sobre la app. Verificar en Play Console → Users and permissions que `fastlane-supply@focuslock-497323.iam.gserviceaccount.com` tiene rol Release manager.

**`apk version code must be greater than X`**
El `versionCode` en `build.gradle.kts` es igual o menor al que ya está en Play Store. Corregir manualmente el valor en el archivo antes de volver a ejecutar.

**`No file found at path app/build/outputs/bundle/release/app-release.aab`**
Ejecutar `fastlane local` o `fastlane internal` en lugar de `alpha`/`beta`/`deploy` — esas lanes de promoción no compilan, asumen que el AAB ya fue subido previamente.

**`WARNING: Support for your Ruby version (3.2.2) is going away`**
Advertencia no bloqueante. Para resolverla: `rbenv install 3.3.0 && rbenv global 3.3.0 && gem install bundler fastlane`.
