# 🔌 Guía de Integración — Social Service

Documento de *handoff* para conectar el **social-service** con el **API Gateway**
y con el **notification-service**.

- **Rama de trabajo:** `develop`
- **Servicio:** `social-service` — Spring Boot 3.4.3 / Java 21
- **Puerto:** `1509`
- **URL en QA:** `https://aibert-social-service-qa.yellowwave-cb2d91fc.centralus.azurecontainerapps.io`
- **Prefijo de todos los endpoints:** `/api/social/**`

---

## 1. Resumen de las conexiones

| Conexión | Dirección | Mecanismo | Estado del lado social-service |
|---|---|---|---|
| Gateway → Social | entrante | Ruteo HTTP (`/api/social/**`) | ✅ Listo |
| Notification → Social | entrante | Feign (REST directo) | ✅ Listo |
| Social → Notification | saliente | Kafka (Azure Event Hub) | ✅ Listo |

El social-service **no requiere más cambios** para estas integraciones. Lo que cada
equipo debe hacer en su propio repositorio está descrito abajo.

---

## 2. Para el equipo de API Gateway

### 2.1. La ruta ya existe

El `application.yaml` del gateway ya enruta correctamente al social-service:

```yaml
- id: social-service
  uri: https://aibert-social-service-qa.yellowwave-cb2d91fc.centralus.azurecontainerapps.io
  predicates:
    - Path=/api/social/**
```

**No hay que cambiar nada en el gateway.** Antes, el controlador `StudyInviteController`
estaba en `/api/v1/social/user/**` y NO quedaba cubierto por esa ruta. Ya fue
**normalizado a `/api/social/**`**, por lo que ahora los 10 controladores del
social-service quedan cubiertos por la ruta existente.

### 2.2. Único requisito: JWT secreto compartido

El gateway valida el JWT y el social-service lo **vuelve a validar** por su cuenta.
Para que esto funcione, la variable de entorno `JWT_SECRET` debe ser **idéntica**
en `gateway`, `auth-service`, `social-service` y `notification-service` en cada
ambiente (QA / PROD).

El gateway además inyecta los headers `X-User-Id`, `X-User-Email`, `X-User-Role`.
El social-service no los necesita (revalida el token), así que no hay acción extra.

---

## 3. Para el equipo de Notification

Hay **dos** integraciones con notification: una entrante (Feign) y una saliente (Kafka).

### 3.1. Feign: notification → social-service

El social-service expone un endpoint pensado específicamente para el notification-service:

```
GET /api/social/users/{userId}/study-invites/pending
```

Devuelve las sesiones de estudio en estado `PENDING` donde el usuario es participante.

**Pasos en el repo de notification:**

1. Añadir la URL del social-service en `application.yml`:

   ```yaml
   services:
     social-service:
       url: ${SOCIAL_SERVICE_URL:https://aibert-social-service-qa.yellowwave-cb2d91fc.centralus.azurecontainerapps.io}
   ```

2. Crear el cliente Feign (mismo patrón que el `ProfileServiceClient` ya existente):

   ```java
   @FeignClient(name = "social-service", url = "${services.social-service.url}")
   public interface SocialServiceClient {

       @GetMapping("/api/social/users/{userId}/study-invites/pending")
       List<StudyInviteDto> getPendingStudyInvites(@PathVariable("userId") UUID userId);
   }
   ```

3. El DTO de respuesta (`StudyInviteDto`) debe mapear este JSON:

   ```json
   {
     "id": "0b5f...-uuid",
     "topic": "Cálculo II",
     "scheduledAt": "2026-06-01T15:00:00",
     "durationHours": 1.5,
     "participantIds": ["uuid", "uuid"],
     "status": "PENDING",
     "notes": "Traer ejercicios"
   }
   ```

4. **Autenticación:** el endpoint exige un JWT válido (`Authorization: Bearer ...`).
   El `FeignInterceptorConfig` que ya tiene notification reenvía el header
   `Authorization` de la petición entrante — por eso la llamada Feign debe hacerse
   **dentro del contexto de una petición HTTP** (por ejemplo, cuando el usuario
   consulta sus notificaciones). Si se llamara desde un consumidor de Kafka o un
   job programado no habría token; en ese caso notification necesitaría un token
   de servicio (fuera del alcance del social-service).

### 3.2. Kafka: social-service → notification

El social-service **publica** eventos de notificación en el tópico **`social.events`**,
que es exactamente el que el `SocialEventConsumer` de notification ya escucha. ✅

**Contrato del evento publicado** (clase `NotificationEvent` del social-service):

```json
{
  "userId": "uuid",
  "type": "STUDY_SESSION_INVITE",
  "title": "Nueva invitación a sesión de estudio",
  "message": "Te han invitado a una sesión de estudio sobre: ...",
  "severity": "INFO",
  "relatedEntityId": "uuid"
}
```

- Se publica como JSON, **sin** headers de tipo (`spring.json.add.type.headers: false`),
  para que notification pueda deserializarlo con su propia clase.
- `key` del mensaje Kafka = `userId`.
- Valores de `type` que envía el social-service: `STUDY_SESSION_INVITE`,
  `CONNECTION_REQUEST_RECEIVED`, `CONNECTION_REQUEST_ACCEPTED`, `NEW_CHAT_MESSAGE`.
- Valores de `severity`: `HIGH`, `MEDIUM`, `LOW`, `INFO` (actualmente siempre `INFO`).

#### ⚠️ Cambio necesario en notification (`SocialEvent`)

Hay un **desajuste de tipo** en el campo `relatedEntityId`:

| Campo | Social-service envía | `SocialEvent` de notification | ¿Compatible? |
|---|---|---|---|
| `relatedEntityId` | `UUID` (string JSON) | `Long` | ❌ **No** |

El social-service usa identificadores `UUID`, por lo que `relatedEntityId` viaja
como un string tipo `"0b5f8c...-uuid"`. La clase `SocialEvent` de notification lo
declara como `Long`, y Jackson **no puede convertir** un UUID a `Long` → la
deserialización del evento completo falla y la notificación se pierde.

**Solución (en el repo de notification):** cambiar el tipo del campo en
`infrastructure/messaging/event/SocialEvent.java`:

```java
// Antes
private Long relatedEntityId;
// Después
private String relatedEntityId;   // o java.util.UUID
```

#### Nota opcional sobre `type`

El `SocialEventConsumer` de notification hoy fija siempre
`NotificationType.STUDY_SESSION_INVITE` e ignora `event.getType()`. Si se quiere
distinguir los otros tipos (`CONNECTION_REQUEST_*`, `NEW_CHAT_MESSAGE`), notification
debería mapear `event.getType()`. No es bloqueante para la conexión.

---

## 4. Para DevOps / Infraestructura

### 4.1. Crear el Event Hub

Según lo indicado por DevOps, hay que crear en el Event Hub
`aibert-eventhubs-qa` un evento (tópico) con el nombre **exacto** que usa el código:

```
social.events
```

### 4.2. Variables de entorno del contenedor `social-service` (QA)

Configurar en el Azure Container App `aibert-social-service-qa`:

| Variable | Valor |
|---|---|
| `KAFKA_BOOTSTRAP_SERVERS` | `aibert-eventhubs-qa.servicebus.windows.net:9093` |
| `KAFKA_CONNECTION_STRING` | *(connection string del Event Hub — proporcionado por DevOps)* |
| `JWT_SECRET` | *(el mismo que usan gateway / auth / notification)* |

> ⚠️ El connection string es un secreto: **no** se commitea al repositorio. Se
> configura solo como variable de entorno en Azure.

El perfil `qa` del `application.yml` ya está preparado para consumir estas variables
con la configuración `SASL_SSL` requerida por el Event Hub.

---

## 5. Superficie de API REST del social-service

Todos los endpoints cuelgan de `/api/social/**` (enrutables por el gateway):

| Controlador | Ruta base |
|---|---|
| Disponibilidad | `/api/social/availability` |
| Chat | `/api/social/chat` |
| Solicitudes de conexión | `/api/social/connections` |
| Amigos | `/api/social/friends` |
| Invitaciones de amistad | `/api/social/invitations` |
| Sesiones de estudio | `/api/social/sessions` |
| Usuarios (presencia, panel, búsqueda, invitaciones pendientes) | `/api/social/users` |

Contrato completo y probable en Swagger UI:
`https://aibert-social-service-qa.yellowwave-cb2d91fc.centralus.azurecontainerapps.io/swagger-ui.html`

---

## 6. Checklist de integración

**Social-service (este repo) — hecho:**

- [x] Todos los endpoints normalizados bajo `/api/social/**`.
- [x] `application.yml`: bloque `spring:` duplicado corregido + `SASL_SSL` arreglado.
- [x] Productor Kafka aplica las propiedades `SASL_SSL` del Event Hub.
- [x] Variables `KAFKA_*` añadidas a `docker-compose.yml` y `.env.example`.
- [x] Puerto del `Dockerfile` corregido a `1509`.

**API Gateway — pendiente:**

- [ ] Confirmar que `JWT_SECRET` es el mismo que en social-service (QA y PROD).

**Notification-service — pendiente:**

- [ ] Crear `SocialServiceClient` (Feign) y añadir `services.social-service.url`.
- [ ] Cambiar `SocialEvent.relatedEntityId` de `Long` a `String`/`UUID`.

**DevOps — pendiente:**

- [ ] Crear el tópico `social.events` en `aibert-eventhubs-qa`.
- [ ] Configurar `KAFKA_BOOTSTRAP_SERVERS` y `KAFKA_CONNECTION_STRING` en el
      contenedor del social-service.
