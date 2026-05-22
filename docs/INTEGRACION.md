# Guia de Integracion - Social Service

Documento de handoff para conectar el **social-service** con el **API Gateway**
y con el **notification-service**.

- Rama: `develop`
- Servicio: `social-service` - Spring Boot 3.4.3 / Java 21 - puerto `1509`
- URL QA: `https://aibert-social-service-qa.yellowwave-cb2d91fc.centralus.azurecontainerapps.io`
- Prefijo de endpoints: `/api/social/**`

## 1. Resumen de las conexiones

| Conexion | Direccion | Mecanismo |
|---|---|---|
| Gateway -> Social | entrante | Ruteo HTTP (`/api/social/**`) |
| Social -> Notification | saliente | Kafka / Azure Event Hub (topico `social.events`) |

> La comunicacion entre social-service y notification-service es **exclusivamente
> por Kafka**. No se usa Feign entre ambos servicios.

## 2. API Gateway

La ruta del social-service ya existe en el `application.yaml` del gateway:

```
- id: social-service
  uri: https://aibert-social-service-qa.yellowwave-cb2d91fc.centralus.azurecontainerapps.io
  predicates:
    - Path=/api/social/**
```

Los 10 controladores del social-service estan bajo `/api/social/**`, asi que esa
ruta los cubre todos. No hay que cambiar nada en el gateway.

Unico requisito: `JWT_SECRET` debe ser identico en gateway, auth-service,
social-service y notification-service (en cada ambiente).

## 3. Notification (integracion por Kafka)

El social-service publica eventos en el topico **`social.events`**, que el
`SocialEventConsumer` de notification ya escucha.

Contrato del evento (JSON, sin type headers):

```
{
  "userId": "uuid",
  "type": "STUDY_SESSION_INVITE",
  "title": "...",
  "message": "...",
  "severity": "INFO",
  "relatedEntityId": "uuid"
}
```

- `key` del mensaje Kafka = `userId`.
- Tipos que envia social-service: `STUDY_SESSION_INVITE`, `CONNECTION_REQUEST_RECEIVED`,
  `CONNECTION_REQUEST_ACCEPTED`, `NEW_CHAT_MESSAGE`.
- Severidades: `HIGH`, `MEDIUM`, `LOW`, `INFO`.

Estado del lado notification (ya hecho): `SocialEvent.relatedEntityId` es `UUID`,
`NotificationType` incluye los `CONNECTION_REQUEST_*`, y el consumer mapea el tipo.

Pendiente en notification: el tipo `NEW_CHAT_MESSAGE` no esta en `NotificationType`
ni en el conjunto de tipos aceptados del `SocialEventConsumer`. Mientras no se
agregue, los eventos de mensajes de chat se descartan. Decision del equipo de
notification: agregarlo, o aceptar que esas notificaciones no se procesan.

## 4. DevOps / Infraestructura

- Topico `social.events` creado en `aibert-eventhubs-qa`. [hecho]
- En el Container App `aibert-social-service-qa`, configurar variables de entorno:
  - `KAFKA_BOOTSTRAP_SERVERS` = `aibert-eventhubs-qa.servicebus.windows.net:9093`
  - `KAFKA_CONNECTION_STRING` = (connection string del Event Hub)
  - `JWT_SECRET` = el mismo de los demas servicios
  - `SPRING_PROFILES_ACTIVE` = `qa`
  - `DB_URL`, `DB_USER`, `DB_PASSWORD`

## 5. API REST del social-service

Todos los endpoints estan bajo `/api/social/**` (availability, chat, connections,
friends, invitations, sessions, users). Contrato completo en Swagger:
`https://aibert-social-service-qa.yellowwave-cb2d91fc.centralus.azurecontainerapps.io/swagger-ui/index.html`

## 6. Checklist

- Social-service: listo (Kafka, rutas normalizadas, Dockerfile, variables).
- Gateway: confirmar `JWT_SECRET` compartido.
- Notification: decidir que hacer con `NEW_CHAT_MESSAGE`.
- DevOps: confirmar variables `KAFKA_*` y demas en el Container App de social-service.
