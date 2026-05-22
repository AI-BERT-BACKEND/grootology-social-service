# AIBERT — Social Service

> Microservicio social de la plataforma AIBERT: conecta a los estudiantes entre si, coordina sesiones de estudio colaborativas y gestiona la interaccion social del ecosistema academico.

---

# Tabla de Contenido

- Descripcion General
- Equipo
- Objetivos
- Planteamiento del Problema
- Requerimientos
- Arquitectura
- Stack Tecnologico
- Diagramas
- Gestion del Proyecto
- Pruebas y Calidad
- Cobertura
- Demo
- Instalacion
- Estructura del Proyecto
- Referencias

---

# Descripcion General

## Resumen Ejecutivo

- **Problema a resolver.** Los estudiantes carecen de un espacio para encontrarse, coordinar horarios y estudiar en grupo dentro de la plataforma AIBERT.
- **Solucion propuesta.** El **Social Service** centraliza las funcionalidades sociales: solicitudes de conexion, lista de amigos, chat, presencia en linea, configuracion de visibilidad, busqueda de usuarios, sesiones de estudio compartidas y puntos de referido.
- **Usuarios objetivo.** Estudiantes de la plataforma AIBERT.
- **Impacto esperado.** Fortalecer el aprendizaje colaborativo y la interaccion entre estudiantes, complementando los modulos de planificacion, recomendaciones y gamificacion.

## Alcance

### Incluye
- Solicitudes de conexion y gestion de amistades.
- Invitaciones de amistad y canje de codigos de referido.
- Configuracion de disponibilidad y visibilidad del perfil.
- Sesiones de estudio colaborativas e invitaciones a sesiones.
- Chat entre amigos y presencia en linea (online/offline).
- Panel social y busqueda de usuarios.
- Publicacion de eventos hacia el notification-service mediante Kafka.

### No Incluye
- Autenticacion y emision de tokens (responsabilidad del auth-service).
- Gestion del perfil academico (responsabilidad del profile-service).
- Entrega de notificaciones al usuario final (responsabilidad del notification-service).
- Interfaz de usuario (frontend).

---

# Equipo

**Equipo Grootyology**

| Integrante | Rol | Responsabilidades |
|-----------|------|-------------------|
| _Por completar_ | Backend | Funcionalidades sociales y APIs REST |
| _Por completar_ | DevOps | CI/CD y despliegue en Azure |
| _Por completar_ | QA | Pruebas unitarias y calidad |

> Completar la tabla con los integrantes reales del equipo.

---

# Objetivos

## Objetivo General

Gestionar las funcionalidades sociales de la plataforma AIBERT, permitiendo a los estudiantes conectarse entre si, comunicarse y coordinar sesiones de estudio colaborativas.

## Objetivos Especificos

- Permitir el envio, aceptacion y rechazo de solicitudes de conexion entre estudiantes.
- Gestionar la lista de amigos, la presencia en linea y el chat entre usuarios conectados.
- Administrar la disponibilidad y la visibilidad del perfil (PUBLIC, PRIVATE, SPECIFIC).
- Coordinar la creacion y participacion en sesiones de estudio compartidas.
- Integrarse con el resto del ecosistema AIBERT a traves del API Gateway y de eventos asincronos.

---

# Planteamiento del Problema

## Contexto

AIBERT es una plataforma orientada al apoyo academico de los estudiantes. El aprendizaje colaborativo es un factor clave del rendimiento, pero requiere herramientas que faciliten la interaccion.

## Problema

Los estudiantes no cuentan con un mecanismo unificado para descubrir companeros, coordinar horarios y organizar sesiones de estudio dentro de la plataforma.

## Dificultades Actuales

- Falta de un canal para conectar estudiantes con intereses academicos comunes.
- Ausencia de coordinacion de disponibilidad y horarios de estudio.
- Interaccion social dispersa y desligada del resto de los modulos academicos.

## Solucion Propuesta

Un microservicio dedicado a las funcionalidades sociales, desacoplado mediante arquitectura hexagonal, que se integra con los demas servicios de AIBERT a traves del API Gateway y de un bus de eventos (Kafka / Azure Event Hub).

---

# Requerimientos

## Requerimientos Funcionales

| ID | Requerimiento | Modulo |
|----|---------------|--------|
| RF-01 | Enviar, aceptar y rechazar solicitudes de conexion | Conexiones |
| RF-02 | Listar amigos con su estado de presencia y eliminar amistades | Amigos |
| RF-03 | Enviar invitaciones de amistad y canjear codigos de referido | Invitaciones |
| RF-04 | Configurar la disponibilidad y la visibilidad del perfil | Disponibilidad |
| RF-05 | Crear sesiones de estudio y responder a invitaciones | Sesiones de estudio |
| RF-06 | Enviar y consultar mensajes de chat entre amigos | Chat |
| RF-07 | Registrar y consultar la presencia en linea (heartbeat) | Presencia |
| RF-08 | Consultar el panel social y buscar usuarios | Panel / Busqueda |
| RF-09 | Publicar eventos de notificacion hacia el notification-service | Mensajeria |

## Requerimientos No Funcionales

| ID | Requerimiento | Metrica / Criterio |
|----|---------------|--------------------|
| RNF-01 | Seguridad | Validacion de token JWT en cada peticion |
| RNF-02 | Arquitectura | Hexagonal (puertos y adaptadores) |
| RNF-03 | Integracion asincrona | Eventos publicados en Kafka / Azure Event Hub |
| RNF-04 | Despliegue | Contenerizado en Azure Container Apps (QA / PROD) |
| RNF-05 | Calidad | Analisis estatico con SonarCloud y cobertura con JaCoCo |
| RNF-06 | Documentacion | Contrato de API expuesto con OpenAPI / Swagger |

## Analisis de Requerimientos

- [Guia de integracion con gateway y notification](docs/INTEGRACION.md)

---

# Arquitectura

## Arquitectura General

El Social Service es un **microservicio** construido con **arquitectura hexagonal (puertos y adaptadores)**, lo que mantiene la logica de negocio aislada de los detalles de infraestructura. Se organiza en cuatro capas:

- **Entrypoints.** Controladores REST y manejo global de errores.
- **Application.** Servicios de aplicacion y DTOs que orquestan los casos de uso.
- **Domain.** Modelo de dominio, excepciones y puertos (interfaces de entrada y salida).
- **Infrastructure.** Adaptadores de persistencia (JPA), servicios externos (Feign, correo) y mensajeria (Kafka).

## Integraciones

| Conexion | Direccion | Mecanismo |
|----------|-----------|-----------|
| API Gateway -> Social Service | entrante | Ruteo HTTP; todos los endpoints bajo `/api/social/**` |
| Social Service -> Notification Service | saliente | Kafka / Azure Event Hub (topico `social.events`) |
| Social Service -> Profile Service | saliente | Cliente Feign (validacion de perfil academico) |

- **API Gateway.** Todas las rutas del servicio cuelgan de `/api/social/**`, por lo que el gateway las enruta de forma uniforme. El servicio valida el token JWT en cada peticion.
- **Notification Service.** El Social Service publica eventos de dominio (invitaciones, solicitudes de conexion, mensajes) en el topico Kafka `social.events`, que el notification-service consume de forma asincrona.
- **Profile Service.** Se consulta el perfil academico mediante un cliente Feign para validar reglas de negocio.

El detalle completo de la integracion esta en [docs/INTEGRACION.md](docs/INTEGRACION.md).

---

# Stack Tecnologico

| Area | Tecnologias |
|------|-------------|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.4.3 |
| Persistencia | Spring Data JPA, PostgreSQL |
| Seguridad | Spring Security, JWT (jjwt) |
| Comunicacion entre servicios | Spring Cloud OpenFeign |
| Mensajeria | Spring Kafka, Azure Event Hub |
| Mapeo | MapStruct, Lombok |
| Documentacion API | springdoc-openapi (Swagger UI) |
| Build | Maven |
| Contenedores | Docker, Azure Container Apps |
| CI/CD | GitHub Actions, GitHub Container Registry |
| Calidad | SonarCloud, JaCoCo |

---

# Diagramas

> Los diagramas se mantienen como imagenes en `docs/uml/`.

## Diagrama de Clases

El diagrama de clases muestra la estructura interna del microservicio y como se modelan las interacciones sociales entre usuarios siguiendo una arquitectura hexagonal dividida en capas de Entrypoints, Application, Domain e Infrastructure. Se observa como los controladores `InvitationController`, `AvailabilityController` y `StudySessionController` delegan la logica a los casos de uso correspondientes, el manejo de entidades de dominio como `Invitation`, `AvailabilityConfig` y `StudySession`, asi como la persistencia y servicios externos a traves de puertos y adaptadores desacoplados de la logica de negocio.

<div align="center">

![Diagrama de Clases](docs/uml/Diagrama_de_Clases1.png)

</div>

## Diagrama de Componentes

El diagrama de componentes muestra la interaccion entre los principales componentes del Social Service durante la gestion de funcionalidades sociales. Se observa como los controladores delegan la logica a los casos de uso, los cuales interactuan con los repositorios a traves de puertos y adaptadores para persistir invitaciones, configuraciones de disponibilidad y sesiones de estudio, manteniendo desacoplada la logica de negocio.

<div align="center">

![Diagrama de Componentes](docs/uml/Diagrama_de_componentes.png)

</div>

## Diagramas de Secuencia

### Envio de Invitaciones

Representa el flujo completo de envio de invitaciones entre usuarios. El proceso inicia cuando el usuario solicita enviar una invitacion, el `InvitationController` delega la operacion al `InvitationUseCase`, se valida y persiste la invitacion y, cuando corresponde, se notifica al usuario invitado antes de retornar la confirmacion.

<div align="center">

![Diagrama Secuencia Envio de Invitaciones](docs/uml/Diagrama_secuencia_sendInvitations.png)

</div>

### Guardar Configuracion de Disponibilidad

Describe el proceso de registro y actualizacion de la disponibilidad horaria del usuario. El `AvailabilityController` recibe la configuracion enviada, el caso de uso construye el objeto de dominio `AvailabilityConfig` y la informacion es persistida mediante el repositorio antes de finalizar la operacion.

<div align="center">

![Diagrama Secuencia Guardar Configuracion](docs/uml/Diagrama_secuencia_saveConfig.png)

</div>

### Creacion de Sesion de Estudio

Muestra el flujo de creacion de una sesion de estudio compartida. El proceso inicia cuando el usuario solicita crear la sesion, el `StudySessionController` delega la logica al `StudySessionUseCase`, se construye la entidad `StudySession`, se persiste la informacion y se retorna la confirmacion de la sesion creada.

<div align="center">

![Diagrama Secuencia Crear Sesion](docs/uml/Diagrama_secuencia_createSession.png)

</div>

---

# Gestion del Proyecto

## Metodologia

Se utiliza **Scrum** con iteraciones cortas, asegurando entregas continuas. El control de versiones sigue una estrategia **Git Flow**:

- `main` — version estable del microservicio; solo recibe cambios validados desde `develop`.
- `develop` — rama de integracion de funcionalidades.
- `feature/*` — ramas de trabajo por funcionalidad, creadas a partir de `develop` e integradas mediante Pull Requests.

Las ramas principales estan protegidas y todo PR debe pasar la validacion estatica (SonarCloud) y el pipeline de CI.

## Sprints

| Sprint | Objetivo | Estado |
|--------|----------|--------|
| Sprint 1 | Configuracion del proyecto y contenerizacion | Completado |
| Sprint 2 | Funcionalidades sociales (amigos, disponibilidad, sesiones) | Completado |
| Sprint 3 | Integracion con gateway, notification (Kafka) y profile | Completado |

## Riesgos

| Riesgo | Impacto | Mitigacion |
|--------|---------|------------|
| Desajustes de contrato entre microservicios | Alto | Documento de integracion y revision conjunta |
| Fallos de despliegue en la nube | Medio | Despliegue por imagen versionada (SHA) y revisiones de Azure |
| Regresiones de codigo | Medio | Pruebas unitarias y Quality Gate en cada PR |

---

# Pruebas y Calidad

## Estrategia

- **Pruebas unitarias** de servicios de aplicacion y controladores REST con JUnit 5 y Mockito.
- Validacion estatica del codigo con **SonarCloud** en cada Pull Request.
- Ejecucion automatica de la suite en el pipeline de CI.

## Ejecucion

```bash
./mvnw clean test
```

El reporte de pruebas se publica como artefacto en cada ejecucion de GitHub Actions.

---

# Cobertura

La cobertura se genera con **JaCoCo** y se analiza en **SonarCloud** (proyecto `AI-BERT-BACKEND_grootology-social-service`).

| Metrica | Cobertura |
|---------|-----------|
| Lineas | _Ver ultimo reporte de CI_ |
| Ramas | _Ver ultimo reporte de CI_ |
| Metodos | _Ver ultimo reporte de CI_ |

El reporte JaCoCo (`index.html`) se publica como artefacto en cada ejecucion del pipeline. El Quality Gate de SonarCloud debe estar en estado **Passed** para integrar cambios.

## Calidad

- **Quality Gate (SonarCloud):** debe estar en estado Passed para integrar cambios.
- **Bugs y vulnerabilidades:** revisados en cada Pull Request mediante el analisis estatico.
- **Code Smells y deuda tecnica:** monitoreados en SonarCloud.
- **Pruebas:** la suite unitaria debe pasar en el pipeline de CI.

---

# Demo

## Video Demo
- _Por agregar_

## Capturas
- _Por agregar_

Documentacion interactiva de la API (Swagger UI) en el ambiente de QA:
`https://aibert-social-service-qa.yellowwave-cb2d91fc.centralus.azurecontainerapps.io/swagger-ui/index.html`

---

# Instalacion

## Requisitos

- Java 21
- Maven 3.8+
- Docker (opcional)
- PostgreSQL

## Clonar repositorio

```bash
git clone https://github.com/AI-BERT-BACKEND/grootology-social-service.git
cd grootology-social-service
```

## Variables de entorno

Copiar `.env.example` a `.env` y completar los valores (base de datos, JWT, correo, CORS, Kafka). El servicio usa los perfiles `local`, `qa` y `prod`.

## Ejecucion local (Maven)

```bash
./mvnw spring-boot:run
```

URL local: `http://localhost:1509` — Swagger UI: `http://localhost:1509/swagger-ui/index.html`

## Ejecucion con Docker

```bash
docker-compose up --build -d
```

---

# Estructura del Proyecto

El microservicio sigue una arquitectura hexagonal (puertos y adaptadores):

```
grootology-social-service/
├── src/
│   ├── main/
│   │   ├── java/com/aibert/dosw/
│   │   │   ├── application/        # Capa de aplicacion
│   │   │   │   ├── dto/            # DTOs de request y response
│   │   │   │   └── service/        # Servicios de aplicacion (casos de uso)
│   │   │   ├── config/             # Configuracion (seguridad, JWT, Kafka, Feign)
│   │   │   ├── domain/             # Capa de dominio
│   │   │   │   ├── exceptions/     # Excepciones de dominio
│   │   │   │   ├── model/          # Entidades y value objects
│   │   │   │   └── ports/          # Puertos in / out
│   │   │   ├── entrypoints/        # Capa de entrada
│   │   │   │   ├── advice/         # Manejo global de errores
│   │   │   │   └── rest/controller/# Controladores REST
│   │   │   └── infrastructure/     # Capa de infraestructura
│   │   │       ├── adapters/       # Adaptadores y persistencia JPA
│   │   │       ├── external/       # Integraciones (Feign, correo)
│   │   │       └── messaging/      # Publicacion de eventos Kafka
│   │   └── resources/              # application.yml
│   └── test/                       # Pruebas unitarias
├── docs/                           # Documentacion y diagramas (uml/)
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

---

# Referencias

- [Documentacion de Spring Boot](https://docs.spring.io/spring-boot/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/reference/)
- [Spring for Apache Kafka](https://docs.spring.io/spring-kafka/reference/)
- [Azure Container Apps](https://learn.microsoft.com/azure/container-apps/)
- [OpenAPI Specification](https://www.openapis.org/)
- [Guia de integracion del Social Service](docs/INTEGRACION.md)

---
