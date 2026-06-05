from __future__ import annotations

import json
import logging
from typing import Any

import pika

from emiyaoj_agent.core.models import AgentTask
from emiyaoj_agent.core.registry import AgentRegistry
from emiyaoj_agent.core.settings import Settings

logger = logging.getLogger(__name__)


class RabbitAgentWorker:
    def __init__(self, settings: Settings, registry: AgentRegistry) -> None:
        self._settings = settings
        self._registry = registry

    def run(self) -> None:
        connection = pika.BlockingConnection(self._connection_parameters())
        channel = connection.channel()
        self._declare_topology(channel)
        channel.basic_qos(prefetch_count=self._settings.rabbitmq_prefetch_count)
        channel.basic_consume(
            queue=self._settings.judge_feedback_queue,
            on_message_callback=self._handle_message,
            auto_ack=False,
        )
        logger.info(
            "Agent worker started, queue=%s, registeredAgents=%s",
            self._settings.judge_feedback_queue,
            ",".join(self._registry.agent_types),
        )
        channel.start_consuming()

    def _handle_message(self, channel: Any, method: Any, properties: Any, body: bytes) -> None:
        delivery_tag = method.delivery_tag
        try:
            payload = json.loads(body.decode("utf-8"))
            task = AgentTask.model_validate(payload)
            result = self._registry.handle(task)
            channel.basic_ack(delivery_tag)
            logger.info(
                "Agent task finished, agentType=%s, submissionId=%s, status=%s, traceId=%s",
                task.agent_type,
                task.submission_id,
                result.status,
                task.trace_id,
            )
        except Exception:
            logger.exception("Agent task failed, body=%s", body.decode("utf-8", errors="replace"))
            channel.basic_reject(delivery_tag, requeue=False)

    def _connection_parameters(self) -> pika.ConnectionParameters:
        credentials = pika.PlainCredentials(
            self._settings.rabbitmq_username,
            self._settings.rabbitmq_password,
        )
        return pika.ConnectionParameters(
            host=self._settings.rabbitmq_host,
            port=self._settings.rabbitmq_port,
            credentials=credentials,
            heartbeat=60,
            blocked_connection_timeout=30,
        )

    def _declare_topology(self, channel: Any) -> None:
        channel.exchange_declare(
            exchange=self._settings.agent_exchange,
            exchange_type="topic",
            durable=True,
        )
        channel.exchange_declare(
            exchange=self._settings.agent_dead_letter_exchange,
            exchange_type="topic",
            durable=True,
        )
        channel.queue_declare(
            queue=self._settings.judge_feedback_queue,
            durable=True,
            arguments={
                "x-dead-letter-exchange": self._settings.agent_dead_letter_exchange,
                "x-dead-letter-routing-key": self._settings.judge_feedback_dlq_routing_key,
            },
        )
        channel.queue_declare(queue=self._settings.judge_feedback_dlq, durable=True)
        channel.queue_bind(
            queue=self._settings.judge_feedback_queue,
            exchange=self._settings.agent_exchange,
            routing_key=self._settings.judge_feedback_routing_key,
        )
        channel.queue_bind(
            queue=self._settings.judge_feedback_dlq,
            exchange=self._settings.agent_dead_letter_exchange,
            routing_key=self._settings.judge_feedback_dlq_routing_key,
        )
