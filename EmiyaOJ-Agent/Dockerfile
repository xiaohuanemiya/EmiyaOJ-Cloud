FROM ghcr.io/astral-sh/uv:python3.12-bookworm-slim

WORKDIR /app

COPY pyproject.toml ./
RUN uv sync --no-dev

COPY src ./src

ENV PYTHONPATH=/app/src

CMD ["uv", "run", "python", "-m", "emiyaoj_agent.main"]
