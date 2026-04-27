# BookOS Backend

Spring Boot 3.5, Java 21, Spring Security, Spring Data JPA, and MySQL.

Milestone 1 backend scope:

- Authentication
- Current user endpoint
- Book CRUD
- Personal library CRUD-like operations
- Seed data

## Package structure

- `common`
- `config`
- `security`
- `user`
- `book`
- `note`
- `capture`
- `knowledge`
- `link`
- `daily`
- `project`
- `forum`
- `ai`
- `media`
- `admin`

The non-library domains exist only as placeholders in Milestone 1.

## Environment

Copy [backend/.env.example](</D:/【指挥中心】/节操都市/项目/BookOS/backend/.env.example>) or the root `.env.example` values into your shell or `.env`.

## Run

```powershell
$repo = (Resolve-Path ..).Path
$env:JAVA_HOME = Join-Path $repo 'tools\jdk21-extracted\jdk-21.0.10+7'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd spring-boot:run
```

## Test

```powershell
$repo = (Resolve-Path ..).Path
$env:JAVA_HOME = Join-Path $repo 'tools\jdk21-extracted\jdk-21.0.10+7'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd test
```

## Seed accounts

- `designer@bookos.local` / `Password123!`
- `admin@bookos.local` / `Admin123!`
