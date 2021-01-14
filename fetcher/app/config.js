'use string';


/* RMQ's settings */
exports.RABBITMQ_HOST = process.env.RABBITMQ_HOST || "localhost";
exports.RABBITMQ_PORT = process.env.RABBITMQ_PORT || 5672;
exports.RABBITMQ_USER = process.env.RABBITMQ_USER || "rmq";
exports.RABBITMQ_PASSWORD = process.env.RABBITMQ_PASSWORD || "rmq123"
exports.RABBITMQ_VHOST = process.env.RABBITMQ_VHOST || "/";
exports.RABBITMQ_CONNECTION_URL = `amqp://${exports.RABBITMQ_USER}:${exports.RABBITMQ_PASSWORD}@${exports.RABBITMQ_HOST}:${exports.RABBITMQ_PORT}/${exports.RABBITMQ_VHOST}`;
exports.RABBITMQ_QUEUE_NAME = process.env.RABBITMQ_QUEUE_NAME || "frontier";

/* Minio's settings */
exports.MINIO_HOST = process.env.MINIO_HOST || "localhost";
exports.MINIO_PORT = process.env.MINIO_PORT || 9000;
exports.MINIO_USE_SSL = process.env.MINIO_USE_SSL || false;
exports.MINIO_ACCESS_KEY = process.env.MINIO_ACCESS_KEY || "minio";
exports.MINIO_SECRET_KEY = process.env.MINIO_SECRET_KEY || "minio123";
exports.MINIO_SCREENSHOT_BUCKET = process.env.MINIO_SCEENSHOT_BUCKET || "screenshots";
exports.MINIO_HTML_BUCKET = process.env.MINIO_HTML_BUCKET || "htmls";

/* Kafka's settings */
exports.KAFKA_BROKER_01 = process.env.KAFKA_BROKER_01 || "kafka:29092";
exports.KAFKA_BROKERS = [exports.KAFKA_BROKER_01]
exports.KAFKA_CLIENT_ID = process.env.KAFKA_CLIENT_ID || "fetcher";
exports.KAFKA_TOPIC = process.env.KAFKA_TOPIC || "htmls";

/* Playwrigh's settings */
exports.PLAYWRIGHT_BROWSER = process.env.PLAYWRIGHT_BROWSER || "firefox";
exports.PLAYWRIGHT_DEVICE_TYPE = process.env.PLAYWRIGHT_DEBIVE_TYPE || null;
exports.PLAYWRIGHT_SCREEN_RESOLUTION_WIDTH = process.env.PLAYWRIGHT_SCREEN_RESOLUTION_WIDTH || 1280;
exports.PLAYWRIGHT_SCREEN_RESOLUTION_HEIGHT = process.env.PLAYWRIGHT_SCREEN_RESOLUTION_HEIGHT || 720;
exports.PLAYWRIGHT_USER_AGENT = process.env.PLAYWRIGHT_USER_AGENT || null;
exports.PLAYWRIGHT_IS_MOBILE = process.env.PLAYWRIGHT_IS_MOBILE || false;
exports.PLAYWRIGHT_TAB_LIMIT = process.env.PLAYWRIGHT_TAB_LIMIT || 2;
exports.PLAYWRIGHT_CONTENT_ENABLED = process.env.PLAYWRIGHT_CONTENT_ENABLED || true;
exports.PLAYWRIGHT_METADATA_ENABLED = process.env.PLAYWRIGHT_METADATA_ENABLED || false;
exports.PLAYWRIGHT_SCEENSHOTS_ENABLED = process.env.PLAYWRIGHT_SCEENSHOTS_ENABLED || true;
exports.PLAYWRIGHT_PAGE_TIMEOUT = process.env.PLAYWRIGHT_PAGE_TIMEOUT || 30000;
