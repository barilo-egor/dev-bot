package tgb.cryptoexchange.devbot.handler.auth;

import org.springframework.stereotype.Service;
import tgb.cryptoexchange.tgcommon.handler.EmptyHandler;

@Service
public class EmptyHandlerImpl implements EmptyHandler {

    @Override
    public void handle(Long chatId) {
        // отсутствует обработка
    }
}
