package tgb.cryptoexchange.devbot.handler.auth;

import org.springframework.stereotype.Service;
import tgb.cryptoexchange.devbot.constants.CallbackQueryId;
import tgb.cryptoexchange.devbot.service.AuthMenuService;
import tgb.cryptoexchange.tgcommon.handler.CallbackQueryHandler;
import tgb.cryptoexchange.tgcommon.keyboard.PressedInlineButton;

@Service
public class BackToDeleteUserHandler implements CallbackQueryHandler {

    private final AuthMenuService authMenuService;

    public BackToDeleteUserHandler(AuthMenuService authMenuService) {
        this.authMenuService = authMenuService;
    }

    @Override
    public void handle(PressedInlineButton button) {
        authMenuService.sendUsers(button.getChatId(), button.getMessage().getMessageId(), CallbackQueryId.DELETE_USER_CONFIRM);
    }

    @Override
    public String getId() {
        return CallbackQueryId.BACK_TO_DELETE_USER_MENU.name();
    }

    @Override
    public boolean hasAccess(Long chatId) {
        return true;
    }
}
