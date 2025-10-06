package tgb.cryptoexchange.devbot.handler.auth;

import org.springframework.stereotype.Service;
import tgb.cryptoexchange.devbot.constants.CallbackQueryId;
import tgb.cryptoexchange.tgcommon.handler.CallbackQueryHandler;
import tgb.cryptoexchange.tgcommon.keyboard.PressedInlineButton;

@Service
public class BackToAuthMenuHandler implements CallbackQueryHandler {

    private final AuthHandler authHandler;

    public BackToAuthMenuHandler(AuthHandler authHandler) {
        this.authHandler = authHandler;
    }

    @Override
    public void handle(PressedInlineButton button) {
        authHandler.handle(button.getChatId(), button.getMessage().getMessageId());
    }

    @Override
    public String getId() {
        return CallbackQueryId.BACK_TO_AUTH_MENU.name();
    }

    @Override
    public boolean hasAccess(Long chatId) {
        return true;
    }
}
