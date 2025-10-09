package tgb.cryptoexchange.devbot.handler.exception;

import lombok.extern.slf4j.Slf4j;
import tgb.cryptoexchange.tgcommon.handler.BotExceptionHandler;
import tgb.cryptoexchange.tgcommon.service.sender.ResponseSender;

@Slf4j
public abstract class RequestExceptionHandler implements BotExceptionHandler {

    private final ResponseSender responseSender;

    protected RequestExceptionHandler(ResponseSender responseSender) {
        this.responseSender = responseSender;
    }

    @Override
    public void handle(Long chatId, Exception e) {
        long currentTime = System.currentTimeMillis();
        log.error("{} Ошибка auth сервиса: {}", currentTime, e.getMessage(), e);
        responseSender.to(chatId)
                .message("Ошибка при выполнении запроса.\n<code>" + currentTime + "</code>")
                .send() ;
    }
}
