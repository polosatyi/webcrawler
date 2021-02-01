# -*- coding: utf-8 -*-
import os
import sys
import pika
import docker


RABBITMQ_HOST = os.environ.get("RABBITMQ_HOST", "rmq")
RABBITMQ_PORT = int(os.environ.get("RABBITMQ_PORT", 5672))
RABBITMQ_USER = os.environ.get("RABBITMQ_USER", "rmq")
RABBITMQ_PASSWORD = os.environ.get("RABBITMQ_PASSWORD", "rmq123")
RABBITMQ_VHOST = os.environ.get("RABBITMQ_VHOST", "/")
RABBITMQ_QUEUE_NAME = os.environ.get("RABBITMQ_QUEUE_NAME", "frontier")
KAFKA_TOPIC_NAME = os.environ.get("KAFKA_TOPIC_NAME", "htmls")

FLINK_JOB_CLASS_NAME = "Extractor"


def create_rmq_connection() -> pika.BlockingConnection:
    """ Creates a blocking RabbitMQ connection. """
    credentials = pika.PlainCredentials(
        username=RABBITMQ_USER,
        password=RABBITMQ_PASSWORD)
    return pika.BlockingConnection(
        pika.ConnectionParameters(
            host=RABBITMQ_HOST,
            port=RABBITMQ_PORT,
            virtual_host=RABBITMQ_VHOST,
            credentials=credentials))


def main(domain: str):
    rmq_connection = create_rmq_connection()
    channel = rmq_connection.channel()
    client = docker.from_env()
    container = client.containers.list(filters={"name": "jobmanager"})[0]
    container.exec_run(cmd=f"flink run -d -c {FLINK_JOB_CLASS_NAME} jars/extractor-assembly-0.1.jar htmls")
    channel.basic_publish(
        exchange='', routing_key=RABBITMQ_QUEUE_NAME, body=domain)
    rmq_connection.close()


if __name__=="__main__":
    if len(sys.argv) < 2:
        print("You have to pass a domain as the first argument!")
        sys.exit(1)
    main(domain=sys.argv[1])
