# Gilded Rose starting position in Java

## Spring Profiles

The application ships with three environment profiles. The **default is `dev`** — it
activates automatically when no profile is specified.

| Profile | Purpose | Logging | Swagger UI |
|---|---|---|---|
| `local` *(default)* | Developer's own machine | DEBUG (app) + DEBUG (Spring Web) | Enabled |
| `dev` | Shared dev / CI server | DEBUG (app) + INFO (framework) | Enabled |
| `prod` | Production server | WARN only | **Disabled** |

### How to activate each profile

**Local** — default; most verbose, full debug visibility:

```bash
# Just run — local activates automatically
./mvnw spring-boot:run

# Or explicitly:
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
java -jar target/gilded-rose-kata-*.jar --spring.profiles.active=local

# IDE — add one of:
#   VM option:   -Dspring.profiles.active=local
#   Env var:     SPRING_PROFILES_ACTIVE=local
```

**Dev** — must be activated explicitly:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
java -jar target/gilded-rose-kata-*.jar --spring.profiles.active=dev
```

**Prod** — always supply explicitly at deployment time; never set as the default:

```bash
# Built JAR
java -jar target/gilded-rose-kata-*.jar --spring.profiles.active=prod

# Environment variable (recommended for servers / containers)
SPRING_PROFILES_ACTIVE=prod java -jar target/gilded-rose-kata-*.jar

# Docker
ENV SPRING_PROFILES_ACTIVE=prod

# Kubernetes (Deployment env or ConfigMap)
# - name: SPRING_PROFILES_ACTIVE
#   value: "prod"
```

> **Note:** Swagger UI is available at `http://localhost:8080/swagger-ui.html` when
> running with the `local` or `dev` profile. It is intentionally **disabled** in `prod`
> to prevent API enumeration and information disclosure.

---

## Run the TextTest Fixture from Command-Line

```
./mvnw test-compile exec:java -Dexec.mainClass=com.vinods.gildedrose.TexttestFixture -Dexec.classpathScope=test
```

### Specify Number of Days

For e.g. 10 days:

```
./mvnw test-compile exec:java -Dexec.mainClass=com.vinods.gildedrose.TexttestFixture -Dexec.classpathScope=test -Dexec.args=10
```

You should make sure the Maven commands shown above work when you execute them in a terminal before trying to use TextTest (see below).


## Run the TextTest approval test that comes with this project

There are instructions in the [TextTest Readme](../texttests/README.md) for setting up TextTest. What's unusual for the Java version is there are two executables listed in [config.gr](../texttests/config.gr) for Java. The first uses Gradle wrapped in a python script. Uncomment these lines to use it:

    executable:${TEXTTEST_HOME}/Java/texttest_rig.py
    interpreter:python

The other relies on your CLASSPATH being set correctly in [environment.gr](../texttests/environment.gr). Uncomment these lines to use it instead:

    executable:com.vinods.gildedrose.TexttestFixture
    interpreter:java
