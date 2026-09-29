# Copilot Instructions for SISDNIT

## Project Overview

SISDNIT is a legacy Java web application used by DNIT. It is packaged as a WAR and deployed to Apache Tomcat.

The project uses:

- Java 8
- Apache Struts 1.3.8
- JSP, JSTL, and custom tag libraries
- Jersey 2.26 for REST APIs
- Jabsorb for JSON-RPC/AJAX integration
- JDBC with Oracle and IBM DB2 support
- JasperReports for report generation
- Log4j 2 for logging
- Maven for builds
- JUnit 4 and Mockito for tests

Do not assume that the application can be migrated to newer Java, Jakarta EE, Struts, Jersey, or servlet APIs without an explicit compatibility analysis.

## Repository Structure

- `pom.xml`: Maven build configuration and dependency declarations.
- `src/main/java/`: Java application source code.
- `src/main/resources/`: Configuration files, messages, reports, templates, fonts, images, and schemas.
- `src/main/webapp/`: JSP pages and static web assets.
- `src/main/webapp/WEB-INF/`: Struts configuration, validation files, tag libraries, and `web.xml`.
- `src/test/java/`: Unit and integration tests.
- `src/test/resources/`: Test configuration and test resources.
- `target/`: Generated build output. Never edit or commit generated files.
- `Servers/Tomcat v9.0 Server at localhost-config/`: Local Eclipse/Tomcat configuration. Treat it as environment-specific infrastructure.

## Build and Runtime Constraints

- Compile for Java 8. Do not use language features or APIs unavailable in Java 8.
- The application is a WAR deployed to Tomcat 9.
- Servlet, JSP, and EL APIs are provided by Tomcat and must not be packaged into the WAR.
- Preserve the `javax.*` namespace. Do not replace it with `jakarta.*` without an explicit migration request.
- Keep dependencies compatible with the versions declared in `pom.xml`.
- Do not upgrade Struts, Jersey, JasperReports, logging libraries, database drivers, or servlet APIs casually. Check transitive dependencies and runtime compatibility first.
- Maven resource filtering is enabled only for `versoes.properties`. Do not enable filtering for other resources without checking the consequences.

## Application Architecture

The application contains several legacy integration styles:

- Struts actions are configured through XML files under `src/main/webapp/WEB-INF/acao/`.
- Struts requests generally use the `*.do` URL pattern.
- REST endpoints are exposed under `/api/*` through Jersey.
- JSON-RPC requests use the `/JSON-RPC` endpoint.
- JSP pages and JavaScript assets are under `src/main/webapp/`.
- Business rules are commonly implemented in packages such as `negocio`.
- Database and infrastructure code is located under `infraestrutura`.
- Reports use JasperReports and resources under `src/main/resources/`.
- Authentication, authorization, filters, listeners, and request processing are configured in `src/main/webapp/WEB-INF/web.xml`.

Before changing a request flow, inspect the relevant servlet/filter mappings, Struts action and form configuration, action or REST resource, business rules, validation, persistence code, and JSP/report resources.

## Coding Guidelines

- Follow the existing package structure and naming conventions.
- Preserve public APIs and XML configuration contracts unless a breaking change is explicitly requested.
- Prefer small, localized changes in this legacy codebase.
- Avoid broad refactoring when implementing a bug fix or feature.
- Reuse existing transaction, logging, validation, database-access, and utility abstractions.
- Do not duplicate authentication, authorization, transaction, or database-management logic.
- Preserve established Portuguese domain terminology in existing Java identifiers.
- Use UTF-8 for source files and resources.
- Avoid introducing new frameworks or libraries when existing dependencies are sufficient.

## Database and Transactions

- The application supports Oracle and IBM DB2. Do not assume SQL portability between them.
- Preserve database-specific behavior unless the change explicitly targets both platforms.
- Use existing transaction and connection-management mechanisms.
- Close JDBC resources safely, preferably with try-with-resources where compatible with surrounding code.
- Never hard-code credentials, connection strings, tokens, or environment-specific values.
- Treat `db.properties`, `app.properties`, and Tomcat configuration as potentially environment-specific. Do not expose or commit secrets.
- Be especially careful when changing SQL, result-set handling, numeric conversions, date handling, or transaction boundaries.

## Web and Security Guidelines

- Treat request parameters, uploaded files, headers, and external responses as untrusted input.
- Preserve and extend existing validation and authorization checks.
- Never bypass authentication filters or security constraints for convenience.
- Do not expose stack traces, database errors, credentials, or internal paths to users.
- Use the existing ESAPI and validation mechanisms where applicable.
- Preserve UTF-8 request and response handling.
- Validate file names, paths, MIME types, file sizes, and uploaded content.
- Avoid XSS, SQL injection, path traversal, SSRF, insecure deserialization, and authorization-bypass vulnerabilities.
- Do not enable development-only endpoints or SQL execution pages in production configuration.

## Struts, JSP, REST, and JSON-RPC

- When adding or changing a Struts action, update all required XML configuration and validation files consistently.
- Check `struts-global.xml`, action XML files, validation XML files, and `web.xml` before changing request behavior.
- Do not expose JSP files directly when the security configuration prevents direct access.
- Preserve custom tag libraries defined in `WEB-INF/tld/`.
- Escape user-controlled values in HTML output.
- Keep business logic out of JSP pages.
- Preserve existing JSON response formats and HTTP status behavior.
- Use the existing Jackson/Jersey configuration rather than adding another JSON implementation.
- Treat JSON-RPC methods as externally callable interfaces and apply authorization and input validation.
- Preserve Jersey multipart support when changing file-upload endpoints.

## Logging and Error Handling

- Use the project's Log4j 2 setup instead of `System.out` or `System.err`.
- Never log passwords, tokens, complete authentication data, or sensitive personal information.
- Preserve existing request and user context in logs.
- Use the application's existing exception-handling mechanisms.
- Do not catch exceptions silently.
- Add useful diagnostic context without exposing confidential data.
- Handle client-aborted requests and streaming responses carefully.

## Reports and Resources

- Preserve JasperReports parameters, field names, resource paths, subreports, fonts, and exported formats.
- Do not modify generated `.jasper` files manually.
- When changing a report, inspect its `.jrxml`, images, fonts, subreports, and Java parameter code.
- Do not change locale, date, number, or currency formatting without checking existing requirements.

## Testing

- Add or update tests for changed business rules, security behavior, database mapping, and utility logic.
- Use JUnit 4 and Mockito 4 as already configured in `pom.xml`.
- Prefer isolated unit tests over tests requiring a live database or external service.
- Existing tests may require Oracle, DB2, files, certificates, or other environment-specific resources.
- New unit tests should follow the Surefire configuration and use the `*Test.java` naming pattern.
- Do not make tests depend on execution order, local paths, developer credentials, or production services.
- Do not weaken assertions or disable tests merely to make the build pass.

## Maven Commands

Run commands from the project root:

```sh
mvn test
mvn package
mvn dependency:list
mvn enforcer:enforce
```

Before considering a change complete:

1. Compile the project.
2. Run the relevant tests.
3. Check dependency convergence when dependencies change.
4. Inspect the generated WAR when packaging or resource behavior changes.
5. Confirm that no credentials, local configuration, or generated files were added.

## Change Review Checklist

- [ ] Java 8 compatibility is preserved.
- [ ] Struts, Jersey, JSP, and Tomcat contracts still work.
- [ ] Related XML configuration files were updated consistently.
- [ ] Authentication and authorization were not weakened.
- [ ] Inputs are validated and outputs are safely encoded.
- [ ] Database resources and transactions are handled correctly.
- [ ] Sensitive data is not logged or committed.
- [ ] Tests were added or updated where appropriate.
- [ ] No files under `target/` were edited.
- [ ] No unnecessary dependency or framework upgrade was introduced.
- [ ] The change is limited to the requested scope.
