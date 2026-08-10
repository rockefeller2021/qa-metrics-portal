# 📊 QA Metrics Portal v2.0.0

Plataforma empresarial de **Gobierno QA, Métricas de Calidad y Seguimiento de Entregas al Cliente**, diseñada para reemplazar la gestión basada en hojas de cálculo Excel por una arquitectura web moderna, reactiva, segura y escalable.

---

## 🏛️ Arquitectura del Sistema

El sistema implementa una **Arquitectura Hexagonal (Puertos y Adaptadores)** en el Backend y una arquitectura orientada a **Componentes Standalone y Signals** en el Frontend.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          QA METRICS PORTAL                                  │
├─────────────────────────────────────────┬───────────────────────────────────┤
│          FRONTEND (Angular 21)          │       BACKEND (Spring Boot 3)     │
├─────────────────────────────────────────┼───────────────────────────────────┤
│ • Standalone Components (No NgModules)  │ • Architecture: Hexagonal Pure    │
│ • State Management: Angular Signals     │ • Java Version: 21 LTS            │
│ • Styling: Tailwind CSS                 │ • Security: Spring Security 6 JWT │
│ • Charts: Apache ECharts 5              │ • Database: MySQL 8.0 + Flyway    │
│ • HTTP: RxJS + Interceptors             │ • Reports: OpenPDF / POI / JFree  │
└─────────────────────────────────────────┴───────────────────────────────────┘
```

---

## 🎯 Reglas de Negocio del Sistema (Business Rules)

### 1. Segregación de Proyectos (RF01)
- Todos los registros, métricas y dashboards están estrictamente divididos en dos líneas de proyecto:
  - **FÁBRICA** (`FABRICA`): Proyectos de desarrollo a gran escala.
  - **MINOR DEMAND** (`MINOR_DEMAND`): Requerimientos de demanda menor y soportes rápidos.

### 2. Ejecuciones de Prueba y Retests N-Iterativos (RF02)
- Modelo relacional `1 a N` entre la ejecución principal (`TestExecution`) y sus corridas de prueba (`TestExecutionRun`).
- Permite registrar retests N-iterativos acumulando historial de ejecuciones, estados (`SUCCESSFUL`, `FAILED`, `BLOCKED`, `UNEXECUTED`) y tiempos de ejecución.

### 3. BugTracker & Detección de Reinyecciones (RF03)
- Registra incidencias vinculadas a un requerimiento (`requirementId`) y Sprint/PI.
- **Detección Automática de Reinyecciones**: Si se reporta un nuevo bug para una Historia de Usuario / Requerimiento previamente fallado o reabierto, el sistema lo clasifica automáticamente como **REINYECCIÓN (RF03)**.

### 4. Trazabilidad SLA & Gobierno de Entregas (RF04)
- Control de fechas clave:
  - `Fecha Estimada Compromiso Cliente`
  - `Fecha Estimada QA`
  - `Fecha Real QA`
  - `Fecha Real Entrega Cliente`
- Cálculo dinámico de estatus SLA: **`ON_TIME`** (A tiempo), **`DELAYED`** (Retrasado), **`PENDING`** (Pendiente).
- Cálculo automático de días de atraso/desviación.

### 5. Target de Porcentaje de Calidad = 95% (RF05)
- **Fórmula de Calidad QA**:
  $$\text{Porcentaje Calidad QA} = \left(1 - \frac{\text{Bugs Totales}}{\text{Casos Exitosos (OK)}}\right) \times 100$$
- **Fórmula de Calidad Cliente (IBL)**:
  $$\text{Porcentaje Calidad Cliente} = \left(1 - \frac{\text{Devoluciones IBL (2ª+ Vez)}}{\text{Total Entregas (Evolutivos + Soportes + Standard Change)}}\right) \times 100$$
- Si el Total de Entregas es igual a `0`, la métrica marca **`N/A`** y el badge muestra **`Sin Entregas`** para no distorsionar las gráficas con falsos 100%.

### 6. Sobrescritura Automática por Periodo (RF06)
- Al registrar entregas del cliente (`ClientDeliveryMetric`) para la misma Línea de Proyecto, Año, Mes y Periodo/Semana (ej. *Semana 1*), el sistema **sobrescribe** los datos existentes realizando un `UPDATE` sin crear registros duplicados.

### 7. Exclusión Dinámica en Gráficos ECharts (RF07)
- En los gráficos de tendencia y consolidado, las categorías que no contengan entregas registradas en el periodo son **100% excluidas** de los ejes y leyendas de ECharts para garantizar visualizaciones limpias.

---

## 🔒 Seguridad y Control de Acceso (RBAC)

- **Autenticación**: JWT (JSON Web Token) Stateless.
- **Header de Autorización**: `Authorization: Bearer <token>`
- **Roles**:
  - `ROLE_ADMIN`: Acceso total (Crear, Editar, Eliminar, Importar, Generar Reportes, Gestionar Usuarios).
  - `ROLE_ANALYST`: Acceso de lectura, registro de ejecuciones, bugs y entregas.

---

## 🔌 Referencia Completa de Endpoints REST API

Base URL: `http://localhost:8080/api/v1`

### 1. Autenticación (`/auth`)
| Método | Endpoint | Descripción | Rol Mínimo |
| :--- | :--- | :--- | :--- |
| `POST` | `/auth/login` | Autenticación de usuario y obtención de JWT Token | Público |
| `POST` | `/auth/register` | Registro de nuevos usuarios en la plataforma | ADMIN |
| `GET` | `/auth/users` | Listado de usuarios registrados | ADMIN |

### 2. Ejecuciones de Prueba (`/test-executions`)
| Método | Endpoint | Parámetros Query | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/test-executions` | `projectType`, `sprintOrPi`, `year`, `month` | Filtrar ejecuciones de prueba |
| `POST` | `/test-executions` | Payload JSON `TestExecution` | Crear nueva ejecución |
| `PUT` | `/test-executions/{id}` | Payload JSON `TestExecution` | Actualizar ejecución existente |
| `DELETE` | `/test-executions/{id}` | — | Eliminar ejecución |
| `POST` | `/test-executions/{id}/runs` | Payload JSON `TestExecutionRun` | Registrar un Retest (Run N-iterativo) |

### 3. BugTracker Incidencias (`/bugs`)
| Método | Endpoint | Parámetros Query | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/bugs` | `projectType`, `sprintOrPi`, `year`, `month` | Listar incidencias con reinyecciones |
| `POST` | `/bugs` | Payload JSON `Bug` | Registrar nueva incidencia (aplica RF03) |
| `PUT` | `/bugs/{id}` | Payload JSON `Bug` | Actualizar estado o datos de incidencia |
| `DELETE` | `/bugs/{id}` | — | Eliminar incidencia |

### 4. Gobierno SLA (`/delivery-sla`)
| Método | Endpoint | Parámetros Query | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/delivery-sla` | `projectType`, `status`, `sprintOrPi`, `year`, `month` | Listar hitos SLA y desviaciones |
| `POST` | `/delivery-sla` | Payload JSON `DeliverySla` | Registrar hito SLA de entrega |
| `PUT` | `/delivery-sla/{id}` | Payload JSON `DeliverySla` | Actualizar fechas reales o comprometidas |
| `DELETE` | `/delivery-sla/{id}` | — | Eliminar hito SLA |

### 5. Seguimiento del Cliente & Calidad (`/client-tracking`)
| Método | Endpoint | Parámetros Query | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/client-tracking/metrics` | `projectType`, `year`, `month` | Listar entregas (Evolutivos, Soportes, SC) |
| `POST` | `/client-tracking/metrics` | Payload JSON `ClientDeliveryMetric` | Registrar/Sobrescribir entregas (RF06) |
| `DELETE` | `/client-tracking/metrics/{id}` | — | Eliminar registro de entregas |
| `GET` | `/client-tracking/returns` | `projectType`, `year`, `month` | Listar historial de devoluciones IBL |
| `POST` | `/client-tracking/returns` | Payload JSON `ClientReturn` | Registrar devolución del cliente |
| `DELETE` | `/client-tracking/returns/{id}` | — | Eliminar devolución IBL |
| `GET` | `/client-tracking/summary` | `projectType`, `year`, `month` | Obtener resumen de % Calidad y tendencias |

### 6. Exportación de Reportes Ejecutivos (`/reports`)
| Método | Endpoint | Parámetros Query | Formato Respuesta |
| :--- | :--- | :--- | :--- |
| `GET` | `/reports/pdf` | `projectType`, `year`, `month`, `developerName`, `designerAnalyst` | Documento PDF (`OpenPDF`) |
| `GET` | `/reports/excel` | `projectType`, `year`, `month`, `developerName`, `designerAnalyst` | Libro Excel XLSX (`Apache POI` 5 Hojas) |
| `GET` | `/reports/pptx` | `projectType`, `year`, `month`, `developerName`, `designerAnalyst` | Presentación PPTX (`POI + JFreeChart`) |

### 7. Importación Masiva (`/import`)
| Método | Endpoint | Body | Descripción |
| :--- | :--- | :--- | :--- |
| `POST` | `/import/executions` | `multipart/form-data` (`file`) | Carga masiva de ejecuciones desde Excel |
| `POST` | `/import/bugs` | `multipart/form-data` (`file`) | Carga masiva de bugs e incidencias desde Excel |

---

## 📄 Estructura de los Reportes Exportables

### 🟢 Excel (.xlsx)
1. **Hoja 1 - Resumen Consolidado**: Métricas ejecutivas, KPI de ejecuciones, bugs, reinyecciones, SLA y Calidad del Cliente.
2. **Hoja 2 - Ejecuciones de Prueba**: Tabla con cobertura, casos OK, Fail, Block y Ratio %.
3. **Hoja 3 - BugTracker Incidencias**: Listado completo de incidencias y bandera de reinyección.
4. **Hoja 4 - Gobierno SLA Entregas**: Trazabilidad de fechas estimadas vs reales y días de retraso.
5. **Hoja 5 - Seguimiento Cliente & Calidad**: Registros de entregas por periodo y tabla detallada de devoluciones IBL con causa raíz.

### 🔴 PDF (.pdf)
- Documento formal A4 estilizado en paleta Indigo/Slate.
- Sección 1: Cobertura de Ejecuciones.
- Sección 2: BugTracker & Reinyecciones.
- Sección 3: Gobierno SLA.
- Sección 4: Entregas del Cliente & Devoluciones IBL (con % de Calidad Target 95%).

### 🟠 PowerPoint (.pptx)
- Presentación ejecutiva HD Flat Aesthetics con diapositivas:
  1. Portada Oficial.
  2. Métricas de Calidad y Cobertura (Gráfico de Barras JFreeChart).
  3. Clasificación de Incidencias & Reinyecciones (Gráfico Circular Donut).
  4. Gobierno SLA & Entregas (Gráfico de Estatus SLA).
  5. Seguimiento Cliente & Devoluciones IBL (Gráfico Comparativo HD).

---

## 🛠️ Requisitos e Instalación

### Prerrequisitos
- **Java JDK**: 21.0+
- **Maven**: 3.9+
- **Node.js**: 20.0+ (npm 10+)
- **Base de Datos**: MySQL 8.0+

### 1. Configuración de Base de Datos
Crear la base de datos en MySQL:
```sql
CREATE DATABASE qa_metrics_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Ejecutar el Backend (Spring Boot 3)
Navegar a la carpeta `backend`:
```bash
cd backend
mvn clean compile
mvn spring-boot:run
```
*Flyway ejecutará automáticamente las migraciones desde `V1` hasta `V12`.*

### 3. Ejecutar el Frontend (Angular 21)
Navegar a la carpeta `frontend`:
```bash
cd frontend
npm install
npm start
```
La aplicación estará disponible en `http://localhost:4200`.

---

## 🧪 Pruebas Automatizadas

### Unit Tests Backend (JUnit 5 + Mockito + JaCoCo)
```bash
cd backend
mvn test
```

### Pruebas E2E Frontend (Playwright POM Suite)
```bash
cd frontend
npx playwright test
```

---

## 👤 Autor & Licencia

Desarrollado para la gestión de calidad de Software en plataformas de pruebas empresariales.
**QA Metrics Portal Team — v2.0.0**
