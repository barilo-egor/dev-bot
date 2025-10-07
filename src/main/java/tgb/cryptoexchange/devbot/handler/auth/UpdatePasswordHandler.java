package tgb.cryptoexchange.devbot.handler.auth;

import org.springframework.stereotype.Service;
import tgb.cryptoexchange.devbot.constants.CallbackQueryId;
import tgb.cryptoexchange.devbot.service.AuthMenuService;
import tgb.cryptoexchange.tgcommon.handler.CallbackQueryHandler;
import tgb.cryptoexchange.tgcommon.keyboard.PressedInlineButton;

@Service
public class UpdatePasswordHandler implements CallbackQueryHandler {

    private final AuthMenuService authMenuService;

    public UpdatePasswordHandler(AuthMenuService authMenuService) {
        this.authMenuService = authMenuService;
    }

    @Override
    public void handle(PressedInlineButton button) {
        authMenuService.sendUsers(button.getChatId(), button.getMessage().getMessageId(), CallbackQueryId.UPDATE_PASSWORD_CONFIRM);
    }

    @Override
    public String getId() {
        return CallbackQueryId.UPDATE_PASSWORD.name();
    }

    @Override
    public boolean hasAccess(Long chatId) {
        return true;
    }
}
