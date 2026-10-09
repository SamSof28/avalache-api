# Avalancha API

API REST en Java y Spring Boot para simular el pago de deudas personales con el
método **avalancha**, generar recomendaciones financieras y entregar un reporte
PDF descargable.

La simulación prioriza las obligaciones con mayor tasa de interés, conserva los
pagos mínimos de las demás deudas y reutiliza cada pago liberado para acelerar
la siguiente obligación. Gemini redacta una recomendación educativa a partir del
resultado calculado; no decide los meses ni reemplaza la lógica financiera.

> El proyecto es una herramienta educativa. Sus resultados dependen de los datos
> recibidos y de los supuestos de la simulación. No constituye asesoría
> financiera, legal ni crediticia.

## Estado actual

La aplicación está funcional para desarrollo local:

- API REST para simulación.
- Validación de solicitudes.
- Cálculo de meses, intereses estimados y saldos.
- Generación de reportes PDF.
- Integración opcional con Gemini 3.8 Flash.
- Recomendación local de respaldo cuando Gemini no está configurado o no responde.
- Descarga HTTP del PDF mediante `Content-Disposition`.
- Pruebas unitarias, de contexto y del controlador.

La persistencia todavía no está habilitada. Aunque el proyecto incluye
dependencias relacionadas con JPA y PostgreSQL, la configuración actual excluye la
autoconfiguración de base de datos para que el MVP funcione sin PostgreSQL.

## Stack tecnológico

- Java 21.
- Spring Boot 4.1.1.
- Spring Web MVC.
- Spring Validation.
- Maven Wrapper.
- Jackson Databind.
- OpenPDF 2.0.3.
- JUnit y pruebas Spring MVC.
- Gemini API mediante HTTP REST.

## Arquitectura

```text
HTTP
└── AvalanchaController
    ├── UsuarioRequestDTO + validación
    └── UsuarioMapper
        └── Usuario / DeudaUsuario

Aplicación
├── AvalanchaService
│   └── Simulación de la estrategia avalancha
├── ReporteFinancieroService
│   ├── ConsejoFinancieroPort
│   │   └── GeminiAdapter
│   └── PdfReportPort
│       └── OpenPdfReportAdapter
└── DTOs de resultado y reporte
```

La aplicación mantiene separadas las responsabilidades:

1. `AvalanchaService` calcula el resultado financiero.
2. `GeminiAdapter` solicita el texto de recomendación.
3. `OpenPdfReportAdapter` compone el documento.
4. `AvalanchaController` expone el contrato HTTP.

## Estructura relevante

```text
src/main/java/com/avalache_api/demo/
├── application/
│   ├── AvalanchaService.java
│   ├── ConsejoFinancieroPort.java
│   ├── PdfReportPort.java
│   ├── ReporteFinancieroService.java
│   └── dto/
├── domain/
│   ├── DeudaUsuario.java
│   └── Usuario.java
└── infrastructure/
    ├── AvalanchaController.java
    ├── DeudaRequestDTO.java
    ├── UsuarioMapper.java
    ├── UsuarioRequestDTO.java
    ├── gemini/
    │   └── GeminiAdapter.java
    └── pdf/
        └── OpenPdfReportAdapter.java

src/test/java/com/avalache_api/demo/
├── AvalanchaControllerTest.java
├── AvalanchaServiceTest.java
├── DemoApplicationTests.java
├── GeminiAdapterTest.java
└── ReporteFinancieroServiceTest.java
```

## Requisitos

- Java 21 o superior.
- Git.
- Maven no es obligatorio porque el repositorio incluye `mvnw`.
- Conexión a internet y una API key de Google AI Studio únicamente si se desea
  usar Gemini real.
- PostgreSQL no es necesario para ejecutar la versión actual.

## Configuración segura de Gemini

La aplicación utiliza el modelo **Gemini 3.8 Flash** por defecto:

```text
gemini-3.8-flash
```

La configuración se encuentra en
[`src/main/resources/application.properties`](src/main/resources/application.properties):

```properties
gemini.api.key=${GEMINI_API_KEY:}
gemini.api.url=${GEMINI_API_URL:https://generativelanguage.googleapis.com/v1beta}
gemini.api.model=${GEMINI_API_MODEL:gemini-3.8-flash}
```

La API key nunca debe escribirse directamente en `application.properties`, Java,
README, pruebas, commits o logs.

### Usar un archivo `.env` local

El repositorio incluye `.env.example` como plantilla. Crea un archivo `.env` en
la raíz del proyecto:

```dotenv
GEMINI_API_KEY=tu-clave-real
GEMINI_API_MODEL=gemini-3.8-flash
```

`.env` está excluido por `.gitignore`. Para cargarlo en Linux o macOS antes de
iniciar Spring Boot:

```bash
set -a
source .env
set +a
```

También puedes definir las variables directamente en la terminal o en el sistema
operativo. Spring Boot no carga `.env` automáticamente; las variables deben estar
exportadas antes de iniciar la aplicación.

Si una clave se expone, revócala inmediatamente desde
[Google AI Studio](https://aistudio.google.com/app/apikey) y genera una nueva.

### Selección del modelo

El modelo se puede cambiar sin modificar Java mediante `GEMINI_API_MODEL`.
La aplicación usa `gemini-3.8-flash` como valor predeterminado, siempre que el
modelo esté disponible para la API key y la cuenta utilizada. Los nombres,
cuotas y disponibilidad de modelos pueden cambiar; deben confirmarse en la
documentación oficial de Google:

- [Modelos de Gemini](https://ai.google.dev/gemini-api/docs/models).
- [Gemini 3.8 Flash](https://ai.google.dev/gemini-api/docs/models/gemini-3.8-flash).
- [Referencia de `generateContent`](https://ai.google.dev/api/generate-content).

Si Gemini no está configurado, falla la conexión, la clave es inválida o el
modelo no está disponible, el reporte continúa mediante una recomendación local
determinista. En ese caso la simulación y el PDF siguen funcionando, pero el
texto no proviene de Gemini.

## Ejecución

Clona el repositorio y entra en la carpeta:

```bash
git clone <URL_DEL_REPOSITORIO>
cd avalache-api
```

Ejecuta todas las pruebas:

```bash
./mvnw test
```

Inicia la aplicación sin Gemini:

```bash
./mvnw spring-boot:run
```

Inicia la aplicación usando `.env`:

```bash
set -a
source .env
set +a
./mvnw spring-boot:run
```

La aplicación inicia en:

```text
http://localhost:8080
```

En Windows utiliza `mvnw.cmd` y configura las variables con PowerShell.

## Endpoints

### Simulación simple

```http
POST /api/v1/avalancha/simular
Content-Type: application/json
```

Devuelve un número entero con los meses estimados. Si la deuda no puede
liquidarse dentro del límite interno de 600 meses, devuelve `-1`.

### Reporte financiero PDF

```http
POST /api/v1/avalancha/reporte
Content-Type: application/json
Accept: application/pdf
```

Devuelve:

```http
200 OK
Content-Type: application/pdf
Content-Disposition: attachment; filename="reporte-financiero.pdf"
```

El PDF incluye:

- Nombre del usuario.
- Tiempo estimado para salir de deudas.
- Intereses estimados.
- Recomendación financiera.
- Resumen de saldos.
- Advertencia de uso educativo.

## Contrato de entrada

El cuerpo de ambos endpoints utiliza esta estructura:

```json
{
  "nombreUsuario": "cliente-demo",
  "montoExtra": 500000,
  "deudas": [
    {
      "nombreDeuda": "Tarjeta de credito",
      "saldo": 2500000,
      "numCuotas": 24,
      "pagoMinimo": 150000,
      "tasaInteres": 0.025
    }
  ]
}
```

Reglas de validación actuales:

- `nombreUsuario` es obligatorio y no puede estar vacío.
- `deudas` debe contener al menos una deuda.
- Cada nombre de deuda es obligatorio.
- `saldo` debe ser positivo.
- `numCuotas` debe ser positivo.
- `pagoMinimo` debe ser positivo.
- `tasaInteres` debe ser cero o positiva.
- `montoExtra` debe ser cero o positivo.

Las tasas se interpretan como proporciones mensuales. Por ejemplo, `0.025`
representa una tasa mensual del 2.5 %. La API todavía no convierte tasas
efectivas anuales ni realiza reglas bancarias de redondeo.

## Ejemplos de uso

### Generar y descargar un PDF

```bash
curl --fail-with-body -sS \
  -X POST http://localhost:8080/api/v1/avalancha/reporte \
  -H "Content-Type: application/json" \
  -D headers.txt \
  -o reporte-financiero.pdf \
  -d '{
    "nombreUsuario": "cliente-demo",
    "montoExtra": 500000,
    "deudas": [
      {
        "nombreDeuda": "Tarjeta de credito",
        "saldo": 2500000,
        "numCuotas": 24,
        "pagoMinimo": 150000,
        "tasaInteres": 0.025
      },
      {
        "nombreDeuda": "Credito personal",
        "saldo": 6000000,
        "numCuotas": 36,
        "pagoMinimo": 300000,
        "tasaInteres": 0.015
      }
    ]
  }'
```

Verifica los headers y el archivo:

```bash
cat headers.txt
file reporte-financiero.pdf
head -c 4 reporte-financiero.pdf
```

El archivo debe indicar que es PDF y comenzar con:

```text
%PDF
```

Si tienes Poppler instalado, puedes inspeccionar el contenido:

```bash
pdfinfo reporte-financiero.pdf
pdftotext reporte-financiero.pdf -
```

### Solicitud inválida

```bash
curl -i \
  -X POST http://localhost:8080/api/v1/avalancha/reporte \
  -H "Content-Type: application/json" \
  -d '{
    "nombreUsuario": "",
    "montoExtra": -1,
    "deudas": []
  }'
```

La respuesta esperada es `400 Bad Request` y no debe generarse un PDF.

## Flujo de procesamiento

1. El controlador recibe y valida el JSON.
2. `UsuarioMapper` transforma el DTO HTTP al dominio.
3. `AvalanchaService` prioriza deudas y simula los pagos mensuales.
4. `ReporteFinancieroService` combina el resultado con una recomendación.
5. `GeminiAdapter` solicita el texto a Gemini 3.8 Flash o activa el fallback.
6. `OpenPdfReportAdapter` genera el documento en memoria.
7. El controlador devuelve el PDF como descarga HTTP.

Gemini no calcula el número de meses ni modifica los saldos. La lógica financiera
permanece dentro de la aplicación para que el resultado sea reproducible y
auditable.

## Pruebas

Ejecuta:

```bash
./mvnw test
```

La suite cubre:

- Reglas principales del algoritmo avalancha.
- Liquidación de saldos y liberación de pagos mínimos.
- No mutación de las deudas originales al generar reportes.
- Generación de un PDF válido.
- Headers y contenido de la respuesta HTTP.
- Rechazo de solicitudes inválidas.
- Fallback local cuando Gemini no tiene API key.
- Carga del contexto Spring.

Las pruebas automáticas no consumen la API real de Gemini ni requieren una clave.
La integración real se valida manualmente mediante `GEMINI_API_KEY`, revisando
los logs y descargando el PDF.

## Seguridad y limitaciones actuales

- No se almacenan API keys en el repositorio.
- `.env` está excluido por `.gitignore`.
- El adaptador no registra la clave ni el prompt completo.
- Gemini es un proveedor opcional y tiene fallback local.
- No hay autenticación ni autorización.
- No hay persistencia de usuarios o reportes.
- No hay rate limiting ni observabilidad avanzada.
- Los datos financieros enviados al endpoint deben considerarse sensibles.
- Antes de un uso productivo se requiere revisión legal, financiera y de
  protección de datos.

## Próximos pasos

1. Formalizar el contrato OpenAPI.
2. Definir moneda, redondeos, fechas de corte y reglas financieras con expertos.
3. Añadir autenticación, autorización y rate limiting.
4. Incorporar manejo uniforme de errores.
5. Añadir persistencia con migraciones versionadas.
6. Añadir métricas, trazas y auditoría.
7. Separar configuración de desarrollo, pruebas y producción.
8. Añadir pruebas de integración con un servidor HTTP de Gemini simulado.
9. Revisar el uso de datos personales y el texto de las recomendaciones.

## Licencia

La licencia del proyecto todavía no ha sido definida.
