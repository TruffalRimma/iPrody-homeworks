package com.iprody.common.async;

/**
 * Интерфейс слушателя входящих сообщений.
 * <p>
 * Реализация интерфейса AsyncListener будет отвечать за прием и
 * валидацию сообщения, после чего обработка “чистого” сообщения будет делегирована
 * реализации {@link MessageHandler}. Вся транспортная логика обработки сообщений в
 * реализации AsyncListener, а вся бизнес логика в реализации MessageHandler.
 *
 * @param <T> тип сообщения, который обрабатывается
 */
public interface AsyncListener<T extends Message> {
    /**
     * Вызывается для каждого нового входящего сообщения.
     *
     * @param message сообщение для обработки
     */
    void onMessage(T message);
}
