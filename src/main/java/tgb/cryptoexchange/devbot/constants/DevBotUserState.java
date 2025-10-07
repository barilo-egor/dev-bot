package tgb.cryptoexchange.devbot.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.tgcommon.constants.UserState;

@Getter
@AllArgsConstructor
public enum DevBotUserState implements UserState {
    NEW_USER,
    DELETE_USER;

    @Override
    public String getState() {
        return this.name();
    }
}
