FROM python:3.11-slim

WORKDIR /app

RUN apt-get update && apt-get install -y \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY chat_service/ ./chat_service/

ENV PYTHONPATH=/app

EXPOSE 8000

CMD ["uvicorn", "chat_service.main:app", "--host", "0.0.0.0", "--port", "8000"]
