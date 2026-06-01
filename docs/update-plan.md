# Backend Database Migration Task

The database models has been updated to match the latest frontend architecture. However, the backend APIs are still using the old database model.

Your task is to analyze the project and update the backend so it fully supports the new database structure.

## Step 1: Analyze the System

* Scan the current codebase and database models.
* Understand the new database architecture.
* Understand the existing API flow, business logic, and data relationships.
* Identify all affected modules, entities, repositories, services, DTOs, and controllers.

## Step 2: Update the Backend

* Update repositories, DTOs, services, controllers, and API routes accordingly.
* Adjust validation, mapping logic, and business logic where necessary.
* Fix any inconsistencies or unreasonable designs found during implementation and document the changes.
* Ensure API responses remain consistent and usable by the frontend.

## Requirements

* The application must build and run successfully after the changes.
* Avoid introducing breaking changes or runtime crashes.
* If a task depends heavily on a third-party library and becomes too complex or risky, skip it and report the issue instead of repeatedly attempting workarounds.
* Follow the existing project structure and coding conventions.
* Update only what is necessary to support the new database model.

## Deliverables

1. `docs/update.md`

   * Summary of all changes made.
   * Database-related migration notes.
   * Any design improvements or issues discovered.

2. `docs/system-architecture.md`

   * Overview of the current backend architecture.
   * Main modules and responsibilities.
   * Request flow and data flow.
   * Database structure and relationships.
