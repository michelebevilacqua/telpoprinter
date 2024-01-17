package com.rch.telpoprinter3;

import java.util.ArrayList;
import java.util.Arrays;

public class PrintableDocument {

    public static final int FORMAT_UNDEFINED = -1;
    public static final int FORMAT_NORMAL = 0;
    public static final int FORMAT_NORMAL2 = 1;
    public static final int FORMAT_DOUBLE_HW = 2;
    public static final int FORMAT_DOUBLE_W = 3;
    public static final int FORMAT_DOUBLE_H = 4;

    public static final int FORMAT_NEGATIVE = 6;
    public static final int FORMAT_UNSET_NEGATIVE = 7;
    public static final int FORMAT_POSITIVE = 8;
    public static final int FORMAT_ALIGN_LEFT = 9;
    public static final int FORMAT_ALIGN_CENTER = 10;
    public static final int FORMAT_ALIGN_RIGHT = 11;

    public static final int FORMAT_BIG_TEXT = 12;
    public static final int FORMAT_HUGE_TEXT = 13;

    ArrayList<DocumentLine>lines;


    static final byte[] SELECT_PRINT_MODE ={0x1b,0x21};

    static final byte DOUBLE_W=0x20;
    static final byte DOUBLE_H=0x10;


    public void clearLines()
    {
        lines.clear();
    }
    PrintableDocument()
    {
        lines= new ArrayList<>();
    }

    public void addLine(Byte[] inputLine)
    {
        DocumentLine line=parseInputLine(inputLine);
        if (line!=null)
            lines.add(line);
    }

    DocumentLine parseInputLine(Byte[] inputLine)
    {
        DocumentLine ret=new DocumentLine();
        if (inputLine[0]==SELECT_PRINT_MODE[0] && inputLine[1]==SELECT_PRINT_MODE[1])
        {
         int formatModifier=   FORMAT_NORMAL;
         if (inputLine[2]==DOUBLE_H)
             formatModifier=FORMAT_DOUBLE_H;
         else  if (inputLine[2]==DOUBLE_W)
                formatModifier=FORMAT_DOUBLE_W;
         else
         if (inputLine[2]==DOUBLE_W+DOUBLE_H)
             formatModifier=FORMAT_DOUBLE_HW;
         ArrayList<Byte> b= new ArrayList<>();
         int i= 3;
         while (inputLine[i]!= '\n')
         {
             b.add(inputLine[i++]);
         }
         ret.addToken(new LineToken(formatModifier,b));
         return ret;
        }
        else return null;
    }

    public int getLinesNumber()
    {
        return lines.size();
    }

    public int getHeightUnits()
    {
        int hDoc=0;
        for (DocumentLine line:lines)
        {
            int hLine=0;
            for (LineToken token: line.tokens)
            {
                if (token.getHeightUnits()>hLine)
                    hLine= token.getHeightUnits();
            }
            hDoc+=hLine;
        }
        return hDoc;
    }

}