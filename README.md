# Infinite Chat Secondary Development Scaffold

这是一个用于即时通信系统。项目采用 Spring Boot、netty、websocket、mysqlk、fakfa
多模块结构，包含认证、联系人、消息、动态、离线存储、网关和实时通信等服务。



## Modules

| Module | Purpose |
| --- | --- |
| `AuthenticationService` | 用户认证与账户相关能力 |
| `ContactService` | 联系人和群组关系 |
| `MessagingService` | 消息处理 |
| `MomentService` | 动态与互动能力 |
| `OfflineDataStore` | 离线消息存储 |
| `RealTimeCommunicationService` | 实时通信 |
| `gateWay` | 服务网关 |
| `Common` | 公共模型与工具 |

## Requirements

- JDK 8
- Maven 3.8+
- MySQL 8+
- Redis
- Nacos
- Kafka
- MinIO（仅使用相关功能时需要）

## Configuration

配置文件位于各模块的 `src/main/resources/application.yml`。敏感配置已改为环境变量，
请在本地或部署平台设置实际值，不要把密码、Token 或公网服务凭据提交到仓库。

常用环境变量：

```text
NACOS_SERVER_ADDR
NACOS_USERNAME
NACOS_PASSWORD
MYSQL_URL
MYSQL_USERNAME
MYSQL_PASSWORD
REDIS_HOST
REDIS_PORT
REDIS_PASSWORD
KAFKA_BOOTSTRAP_SERVERS
MINIO_URL
MINIO_ACCESS_KEY
MINIO_SECRET_KEY
SENTRY_DSN
```

## Build

```bash
mvn clean package -DskipTests
```

构建或运行前，建议执行仓库检查：


