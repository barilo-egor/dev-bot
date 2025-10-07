package tgb.cryptoexchange.devbot.handler.auth;

import org.springframework.stereotype.Service;
import tgb.cryptoexchange.devbot.constants.CallbackQueryId;
import tgb.cryptoexchange.devbot.service.AuthMenuService;
import tgb.cryptoexchange.devbot.service.AuthService;
import tgb.cryptoexchange.tgcommon.exception.TelegramCommonException;
import tgb.cryptoexchange.tgcommon.handler.CallbackQueryHandler;
import tgb.cryptoexchange.tgcommon.keyboard.PressedInlineButton;
import tgb.cryptoexchange.tgcommon.service.sender.ResponseSender;

@Service
public class DeleteUserProcessHandler implements CallbackQueryHandler {

    private final AuthService authService;

    private final ResponseSender responseSender;

    private final AuthMenuService authMenuService;

    public DeleteUserProcessHandler(AuthService authService, ResponseSender responseSender, AuthMenuService authMenuService) {
        this.authService = authService;
        this.responseSender = responseSender;
        this.authMenuService = authMenuService;
    }

    @Override
    public void handle(PressedInlineButton button) {
        String username = button.getArgument(1)
                .orElseThrow(() -> new TelegramCommonException("Отсутствует аргумент идентификатора пользователя."));
        authService.delete(username);
        responseSender.to(button.getChatId())
                .delete(button.getMessage().getMessageId());
        responseSender.to(button.getChatId())
                .message("Пользователь <b>" + username + "</b> был удален.")
                .send();
        authMenuService.sendUsers(button.getChatId(), button.getMessage().getMessageId(), CallbackQueryId.DELETE_USER_CONFIRM);
    }

    @Override
    public String getId() {
        return CallbackQueryId.DELETE_USER_PROCESS.name();
    }

    @Override
    public boolean hasAccess(Long chatId) {
        return true;
    }
}
