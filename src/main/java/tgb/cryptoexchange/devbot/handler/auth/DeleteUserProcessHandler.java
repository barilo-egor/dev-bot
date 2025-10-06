package tgb.cryptoexchange.devbot.handler.auth;

import org.springframework.stereotype.Service;
import tgb.cryptoexchange.devbot.constants.CallbackQueryId;
import tgb.cryptoexchange.devbot.service.AuthService;
import tgb.cryptoexchange.tgcommon.exception.TelegramCommonException;
import tgb.cryptoexchange.tgcommon.handler.CallbackQueryHandler;
import tgb.cryptoexchange.tgcommon.keyboard.PressedInlineButton;
import tgb.cryptoexchange.tgcommon.service.sender.ResponseSender;

@Service
public class DeleteUserProcessHandler implements CallbackQueryHandler {

    private final AuthService authService;

    private final DeleteUserCallbackHandler deleteUserCallbackHandler;

    private final ResponseSender responseSender;

    public DeleteUserProcessHandler(AuthService authService, DeleteUserCallbackHandler deleteUserCallbackHandler,
                                    ResponseSender responseSender) {
        this.authService = authService;
        this.deleteUserCallbackHandler = deleteUserCallbackHandler;
        this.responseSender = responseSender;
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
        deleteUserCallbackHandler.sendUsers(button.getChatId(), null);
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
