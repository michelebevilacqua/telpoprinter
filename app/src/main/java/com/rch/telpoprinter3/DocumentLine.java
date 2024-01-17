package com.rch.telpoprinter3;

import java.util.ArrayList;

public class DocumentLine {

    public ArrayList<LineToken> tokens;

    public DocumentLine() {
        tokens = new ArrayList<>();
    }

    public void addToken(LineToken token)
    {
        tokens.add(token);
    }
}