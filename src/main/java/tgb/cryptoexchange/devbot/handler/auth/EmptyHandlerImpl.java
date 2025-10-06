package tgb.cryptoexchange.devbot.handler.auth;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import tgb.cryptoexchange.tgcommon.handler.EmptyHandler;

@Service
public class EmptyHandlerImpl implements EmptyHandler {

    @Override
    public BotApiMethodMessage getEmptyMessage(Long chatId) {
        return null;
    }
}
