package com.emiyaoj.moderation.dto;

public final class ModerationRabbitConstants {

    private ModerationRabbitConstants() {
    }

    public static final String EXCHANGE = "emiyaoj.moderation.exchange";
    public static final String DEAD_LETTER_EXCHANGE = "emiyaoj.moderation.dlx";

    public static final String BLOG_TEXT_QUEUE = "emiyaoj.moderation.blog.text.queue";
    public static final String COMMENT_TEXT_QUEUE = "emiyaoj.moderation.comment.text.queue";
    public static final String BLOG_TEXT_DLQ = "emiyaoj.moderation.blog.text.dlq";
    public static final String COMMENT_TEXT_DLQ = "emiyaoj.moderation.comment.text.dlq";

    public static final String BLOG_TEXT_ROUTING_KEY = "moderation.blog.text";
    public static final String COMMENT_TEXT_ROUTING_KEY = "moderation.comment.text";
    public static final String BLOG_TEXT_DLQ_ROUTING_KEY = "moderation.blog.text.dead";
    public static final String COMMENT_TEXT_DLQ_ROUTING_KEY = "moderation.comment.text.dead";
}
