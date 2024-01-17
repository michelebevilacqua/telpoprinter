package com.rch.telpoprinter3;

import static com.rch.telpoprinter3.PrintableDocument.FORMAT_DOUBLE_H;
import static com.rch.telpoprinter3.PrintableDocument.FORMAT_DOUBLE_HW;
import static com.rch.telpoprinter3.PrintableDocument.FORMAT_DOUBLE_W;

import java.util.ArrayList;

public class LineToken {

    int modifierCode= PrintableDocument.FORMAT_UNDEFINED;
    ArrayList<Byte> text;

    public LineToken(int modifierCode, ArrayList<Byte> text) {
        this.modifierCode = modifierCode;
        this.text = text;
    }

    public int getModifierCode() {
        return modifierCode;
    }

    public void setModifierCode(int modifierCode) {
        this.modifierCode = modifierCode;
    }

    public ArrayList<Byte> getText() {
        return text;
    }

    public void setText(ArrayList<Byte> text) {
        this.text = text;
    }

    public int getHeightUnits()
    {
        if (modifierCode==FORMAT_DOUBLE_H || modifierCode==FORMAT_DOUBLE_HW)
            return 2;
        else return 1;
    }
    public int getWidthUnits()
    {
        if (modifierCode==FORMAT_DOUBLE_W|| modifierCode==FORMAT_DOUBLE_HW)
            return 2;
        else return 1;
    }
}