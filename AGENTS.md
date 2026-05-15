# AGENTS.md

## Project Overview

This repository contains a database course project: a small e-commerce mall platform built with SpringBoot3, Vue3, and MySQL.

## Core Requirements

- Keep the project runnable locally.
- Prioritize database design quality and transaction correctness.
- Do not store passwords in plaintext.
- Do not rely only on frontend permission checks.
- Use JWT authentication.
- Use MySQL scripts for schema and seed data.
- Document all major design decisions in `docs/`.

## Backend Conventions

- Java 17 and Spring Boot 3.
- Controllers handle request/response only.
- Services contain business logic.
- Mappers handle database access.
- Use DTO/VO objects instead of exposing entities directly.
- Use `@Transactional` for order creation.
- Use global exception handling and unified API responses.

## Frontend Conventions

- Vue 3 + Vite + Element Plus.
- Use Pinia for auth and cart state.
- Use Axios interceptors for Token handling.
- Use route guards for protected routes and admin routes.

## Testing and Documentation

- Keep `api-test.http` updated.
- Keep `README.md` runnable.
- Keep `docs/` suitable for experiment report writing.

