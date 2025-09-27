package tgb.cryptoexchange.devbot.handler;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import tgb.cryptoexchange.devbot.constants.CallbackQueryId;
import tgb.cryptoexchange.tgcommon.handler.CallbackQueryHandler;
import tgb.cryptoexchange.tgcommon.handler.SlashCommandHandler;
import tgb.cryptoexchange.tgcommon.keyboard.InlineButton;
import tgb.cryptoexchange.tgcommon.keyboard.KeyboardBuilder;
import tgb.cryptoexchange.tgcommon.keyboard.PressedInlineButton;
import tgb.cryptoexchange.tgcommon.service.sender.ResponseSender;

import java.util.List;

@Service
public class StartHandler implements SlashCommandHandler, CallbackQueryHandler {

    private final ResponseSender responseSender;

    private final KeyboardBuilder keyboardBuilder;

    public StartHandler(ResponseSender responseSender, KeyboardBuilder keyboardBuilder) {
        this.responseSender = responseSender;
        this.keyboardBuilder = keyboardBuilder;
    }

    @Override
    public void handle(Message message) {
        responseSender.to(message.getChatId())
                .message("Меню.")
                .replyKeyboard(keyboardBuilder.buildInline(List.of(
                        new InlineButton(CallbackQueryId.AUTH.name(), "Сервис аутентификации")
                )))
                .send();
    }

    @Override
    public String getSlashCommand() {
        return "/start";
    }

    @Override
    public boolean hasAccess(Long chatId) {
        return true;
    }

    @Override
    public void handle(PressedInlineButton button) {
        responseSender.to(button.getChatId())
                .editText(button.getMessage().getMessageId(), "Меню.")
                .replyKeyboard(keyboardBuilder.buildInline(List.of(
                        new InlineButton(CallbackQueryId.AUTH.name(), "Сервис аутентификации")
                )))
                .send();
    }

    @Override
    public String getId() {
        return CallbackQueryId.BACK_TO_MAIN_MENU.name();
    }
}
