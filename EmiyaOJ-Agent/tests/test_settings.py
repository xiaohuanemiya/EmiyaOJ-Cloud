from emiyaoj_agent.core.settings import Settings


def test_llm_api_key_prefers_judge_feedback_key():
    settings = Settings(
        DEEPSEEK_API_KEY="deepseek-key",
        JUDGE_FEEDBACK_API_KEY="judge-key",
        CHAT_API_KEY="chat-key",
    )

    assert settings.llm_api_key == "deepseek-key"


def test_llm_api_key_falls_back_to_judge_feedback_and_chat_keys():
    judge_settings = Settings(JUDGE_FEEDBACK_API_KEY="judge-key", CHAT_API_KEY="chat-key")
    chat_settings = Settings(CHAT_API_KEY="chat-key")

    assert judge_settings.llm_api_key == "judge-key"
    assert chat_settings.llm_api_key == "chat-key"


def test_llm_api_key_ignores_blank_values():
    settings = Settings(DEEPSEEK_API_KEY=" ", JUDGE_FEEDBACK_API_KEY="", CHAT_API_KEY="chat-key")

    assert settings.llm_api_key == "chat-key"


def test_deepseek_v4_pro_is_the_default_model():
    settings = Settings()

    assert settings.judge_feedback_model == "deepseek-v4-pro"
    assert settings.judge_feedback_api_url == "https://api.deepseek.com/chat/completions"
    assert settings.judge_feedback_timeout_seconds == 120
    assert settings.judge_http_timeout_seconds == 10
    assert settings.rabbitmq_heartbeat_seconds == 300
