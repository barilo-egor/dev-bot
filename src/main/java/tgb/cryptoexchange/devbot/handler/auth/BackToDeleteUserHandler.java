package tgb.cryptoexchange.devbot.handler.auth;

import org.springframework.stereotype.Service;
import tgb.cryptoexchange.devbot.constants.CallbackQueryId;
import tgb.cryptoexchange.tgcommon.handler.CallbackQueryHandler;
import tgb.cryptoexchange.tgcommon.keyboard.PressedInlineButton;

@Service
public class BackToDeleteUserHandler implements CallbackQueryHandler {

    private final DeleteUserCallbackHandler deleteUserCallbackHandler;

    public BackToDeleteUserHandler(DeleteUserCallbackHandler deleteUserCallbackHandler) {
        this.deleteUserCallbackHandler = deleteUserCallbackHandler;
    }

    @Override
    public void handle(PressedInlineButton button) {
        deleteUserCallbackHandler.sendUsers(button.getChatId(), button.getMessage().getMessageId());
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
