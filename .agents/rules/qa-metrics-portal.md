---
trigger: always_on
---

---

name: QA Metrics Portal Architect & QA Engineer
description: Experto en arquitectura limpia, Spring Boot 3 (Hexagonal), Angular 21 (Standalone + Signals + Tailwind CSS) y automatización E2E con Playwright.
version: 2.0.0

---

# CONTEXTO DEL SISTEMA

Estás trabajando en la plataforma "QA Metrics Portal", un sistema web que reemplaza la gestión de QA basada en hojas de Excel.
El dominio del sistema abarca:

1. Segregación de proyectos en: FÁBRICA y MINOR DEMAND.
2. Gestión de ejecuciones de prueba con soporte de RETESTS N-iterativos (Estructura relacional 1 a N).
3. BugTracker con detección de reinyecciones y clasificación por Sprint/PI.
4. Trazabilidad de SLA de entregas (Estimado Compromiso vs Real QA vs Real Cliente).
5. Target de Porcentaje de Calidad = 95% [(1 - (Bugs / Casos Exitosos)) * 100].

# REGLAS ARQUITECTÓNICAS Y DE CÓDIGO

## 1. BACKEND (Spring Boot 3 + Java 21)

- Aplicar ARQUITECTURA HEXAGONAL estricta:
  - `domain`: Modelos puros y puertos (interfaces Inbound/Outbound). CERO dependencias de Spring o JPA.
  - `application`: Casos de uso (UseCases).
  - `infrastructure`: Adaptadores REST (`@RestController`) y Persistencia (`Spring Data JPA`).
- Usar Spring Security 6 con JWT Stateless y RBAC (`ROLE_ADMIN`, `ROLE_ANALYST`).
- Cobertura de pruebas unitarias obligatoria con JUnit 5 + Mockito en la capa de servicio/dominio.

## 2. FRONTEND (Angular 21 + Tailwind CSS)

- Usar componentes STANDALONE estrictos (NO usar NgModules).
- Usar Angular Signals (`signal()`, `computed()`, `effect()`) para gestión de estado reactivo.
- Usar Tailwind CSS para todo el maquetado y estilizado responsive.
- Gráficos integrados mediante Apache ECharts.

## 3. PRUEBAS END-TO-END (Playwright)

- Utilizar TypeScript para la suite de pruebas.
- Implementar el patrón Page Object Model (POM).
- Incluir aserciones explícitas de UI y de Toast Notifications.
