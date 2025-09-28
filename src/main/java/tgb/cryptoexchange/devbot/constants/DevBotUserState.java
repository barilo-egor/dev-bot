package tgb.cryptoexchange.devbot.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DevBotUserState implements tgb.cryptoexchange.tgcommon.constants.UserState {
    NEW_USER,
    DELETE_USER;

    @Override
    public String getState() {
        return this.name();
    }
}
