# Scrum Poker

Fullstack проект аналога Scrum Poker Online.

## Backend

- Java 21
- Spring Boot 3
- Maven
- Spring Web
- Spring WebSocket/STOMP
- Spring Security
- Spring Data JPA
- H2

## Frontend

- React
- TypeScript
- Vite
- Zustand
- Axios
- Native WebSocket STOMP

## Запуск backend

```bash
cd backend
mvn spring-boot:run
```

Backend:

```text
http://localhost:8080
```

Swagger:

```text
http://localhost:8080/swagger-ui.html
```

H2:

```text
http://localhost:8080/h2-console
```

H2 JDBC URL:

```text
jdbc:h2:mem:scrumpoker
```

User:

```text
sa
```

Password пустой.

## Запуск frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend:

```text
http://localhost:5173
```

## Что реализовано

- создание комнаты;
- подключение по invite link;
- роли: Модератор, Участник, Наблюдатель;
- ограничение до 100 голосующих;
- голосование карточками;
- reveal/reset;
- WebSocket realtime;
- realtime обновление при входе нового участника;
- настройки комнаты;
- Fibonacci / T-Shirt / Custom deck;
- auto reveal;
- timer UI;
- статистика: среднее, минимум, максимум, консенсус;
- скролл списка участников;
- сброс выбранной карточки после нового раунда.
