# Settings for the JDBC DataSource instrumentation

| System property                                   | Type    | Default | Description                                                                                               |
|---------------------------------------------------|---------|---------|-----------------------------------------------------------------------------------------------------------|
| `otel.instrumentation.jdbc-datasource.enabled`    | Boolean | `false` | Enables the JDBC DataSource instrumentation. Disabled by default due to high telemetry volume.           |

## Overview

This instrumentation creates spans for DataSource connection acquisition (`DataSource.getConnection()`).

**Note:** This instrumentation is disabled by default because it can produce a high volume of telemetry.
Enable it only when you need to monitor datasource connection behavior specifically.
