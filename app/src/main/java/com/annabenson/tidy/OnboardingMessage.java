package com.annabenson.tidy;

import java.util.List;

public class OnboardingMessage {
    public enum Type { TILLY, USER, CHIPS }

    public final Type type;
    public final String text;
    public final List<String> chips; // for Type.CHIPS
    public boolean chipsEnabled = true; // only relevant for CHIPS type

    public OnboardingMessage(Type type, String text) {
        this.type = type;
        this.text = text;
        this.chips = null;
    }

    public OnboardingMessage(List<String> chips, String hint) {
        this.type = Type.CHIPS;
        this.text = hint;
        this.chips = chips;
    }
}
