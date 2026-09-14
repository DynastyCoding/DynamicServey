# dynamic-servey Agent Guide

## Frontend (Angular)
- Dev server: `npm start` or `ng serve` (http://localhost:4200)
- Build: `npm run build` or `ng build`
- Unit tests: `npm test` or `ng test`
- Generate component: `ng generate component <name>`
- E2E tests: `ng e2e` (requires separate e2e framework setup)

## Backend (Spring Boot/Gradle)
- Build: `./gradlew build`
- Run: `./gradlew bootRun`
- Unit tests: `./gradlew test`
- Requires MySQL: localhost:3306, schema 'dynamic_survey', user 'root', password 'a4678749'
- Java version: 17

## General
- Frontend and backend are independent; run separately.
- Communication: frontend -> backend REST APIs.