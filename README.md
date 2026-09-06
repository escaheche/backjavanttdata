# backjavanttdata

Backend Java - Proyecto NTT DATA con Spring Boot + MySQL.

API REST básica conectada a MySQL que expone datos de personas (`usuario`) para pruebas con Postman.

---

## Stack

- **Java 21** + **Spring Boot 4.1.1** (parent)
- **Spring WebMVC**, **Spring Data JPA**, **Hibernate 7**
- **MySQL 8.0** + `mysql-connector-j`
- **Lombok**, **Maven Wrapper**, **Tomcat 11**

## Estructura

```
BACKNTTDA/
├── nttdata/                          # proyecto Spring Boot
│   ├── pom.xml                       # dependencias: webmvc, data-jpa, mysql-connector-j, lombok
│   ├── src/main/java/cl/miappparanttdata/nttdata/
│   │   ├── NttdataApplication.java   # @SpringBootApplication
│   │   ├── entity/Usuario.java       # @Entity @Table(name="usuario")
│   │   ├── repository/UsuarioRepository.java  # JpaRepository<Usuario,Integer>
│   │   └── controller/UsuarioController.java  # REST /api/usuarios
│   └── src/main/resources/
│       ├── application.properties           # config BD (usa variables de entorno, sin credenciales hardcodeadas)
│       └── application-example.properties   # plantilla para copiar
└── README.md
```

### Código relevante

- `nttdata/src/main/java/cl/miappparanttdata/nttdata/entity/Usuario.java:12` — Entidad mapeada a tabla `usuario` (`user_id, nombre, correo_electronico, contrasena, saldo, fecha_creacion`). `contrasena` con `@JsonIgnore`.
- `nttdata/src/main/java/cl/miappparanttdata/nttdata/repository/UsuarioRepository.java:9` — `JpaRepository` + `findByCorreoElectronico`.
- `nttdata/src/main/java/cl/miappparanttdata/nttdata/controller/UsuarioController.java:14` — Endpoints GET.

## Base de Datos

### Creación BD nueva `nttdata` (no usa `alkewallet` existente)

BD creada local en MySQL Workbench `Local instance MySQL80` (`127.0.0.1:3306`).

```sql
CREATE DATABASE IF NOT EXISTS nttdata CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE nttdata;

CREATE TABLE usuario (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    correo_electronico VARCHAR(150) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    saldo DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE moneda (
    currency_id INT AUTO_INCREMENT PRIMARY KEY,
    currency_name VARCHAR(50) NOT NULL,
    currency_symbol VARCHAR(10) NOT NULL
);

CREATE TABLE transaccion (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    sender_user_id INT NOT NULL,
    receiver_user_id INT NOT NULL,
    currency_id INT NOT NULL,
    importe DECIMAL(12,2) NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_user_id) REFERENCES usuario(user_id),
    FOREIGN KEY (receiver_user_id) REFERENCES usuario(user_id),
    FOREIGN KEY (currency_id) REFERENCES moneda(currency_id)
);

-- Datos de prueba
INSERT INTO usuario (nombre, correo_electronico, contrasena, saldo) VALUES
('Ana Torres','ana@nttdata.cl','***',150000),
('Luis Soto','luis@nttdata.cl','***',80000),
('Carla Diaz','carla@nttdata.cl','***',30000),
('Pedro Rojas','pedro@nttdata.cl','***',120000),
('Maria Gonzalez','maria@nttdata.cl','***',95000);
```

> Se usa `contrasena` sin `ñ` para evitar problemas de encoding (`utf8mb4`). Si migras desde `alkewallet` donde la columna es `contraseña`, renombra o ajusta `@Column(name="contrasena")`.

## Configuración — Sin exponer credenciales

`application.properties` **no contiene passwords en texto plano**. Usa variables de entorno:

```properties
# nttdata/src/main/resources/application.properties
spring.datasource.url=jdbc:mysql://${DB_HOST:127.0.0.1}:${DB_PORT:3306}/${DB_NAME:nttdata}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Santiago&characterEncoding=UTF-8&useUnicode=true
spring.datasource.username=${DB_USER:root}
spring.datasource.password=${DB_PASSWORD:}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
```

### Plantilla
Copia `application-example.properties` a `application.properties` local (no se commitea con credenciales reales):

```bash
cp nttdata/src/main/resources/application-example.properties nttdata/src/main/resources/application.properties
# editar con tus valores locales
```

O exporta variables en tu shell:

```powershell
# PowerShell (Windows)
$env:DB_HOST="127.0.0.1"
$env:DB_PORT="3306"
$env:DB_NAME="nttdata"
$env:DB_USER="root"
$env:DB_PASSWORD="tu_password_seguro"

# bash / zsh
export DB_HOST=127.0.0.1
export DB_PORT=3306
export DB_NAME=nttdata
export DB_USER=root
export DB_PASSWORD=tu_password_seguro
```

Alternativa `.env` + plugin si usas Docker/IntelliJ.

> **Nunca** commitees `application.properties` con `password=123456` real. El `.gitignore` ignora `application-local.properties` y `.env`.

## Ejecutar

```bash
cd nttdata
./mvnw clean package -DskipTests
java -jar target/nttdata-0.0.1-SNAPSHOT.jar
# o
./mvnw spring-boot:run
```
App en `http://localhost:8080`.

## API — Endpoints GET

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/api/usuarios` | Lista todas las personas |
| GET | `/api/usuarios/{id}` | Persona por `user_id` |
| GET | `/api/usuarios/health` | Health check sin BD |

### Postman

1. Importa o crea request `GET`.
2. URL: `http://localhost:8080/api/usuarios`
3. Send — Respuesta:
```json
[
  {"userId":1,"nombre":"Ana Torres","correoElectronico":"ana@nttdata.cl","saldo":150000.00,"fechaCreacion":"2026-09-06T20:26:58"},
  {"userId":2,"nombre":"Luis Soto","correoElectronico":"luis@nttdata.cl","saldo":80000.00,"fechaCreacion":"2026-09-06T20:26:58"}
]
```
`GET http://localhost:8080/api/usuarios/1` → una persona.

Headers: ninguno requerido. `@CrossOrigin(origins="*")` habilitado.

## Seguridad y buenas prácticas

- Credenciales vía variables de entorno, no en git.
- `spring.jpa.hibernate.ddl-auto=validate` — no altera la BD en prod.
- `contrasena` marcada `@JsonIgnore` — no se expone en JSON.
- Validar que `application.properties` con password real esté en `.gitignore` si usas overrides locales.

## Git

Repo: `https://github.com/escaheche/backjavanttdata.git` — rama `master`.
