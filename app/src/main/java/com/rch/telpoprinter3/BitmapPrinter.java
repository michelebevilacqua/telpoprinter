package com.rch.telpoprinter3;

import static com.rch.telpoprinter3.PrintableDocument.FORMAT_DOUBLE_H;
import static com.rch.telpoprinter3.PrintableDocument.FORMAT_DOUBLE_HW;
import static com.rch.telpoprinter3.PrintableDocument.FORMAT_DOUBLE_W;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;


import androidx.annotation.Nullable;

import java.util.ArrayList;

public class BitmapPrinter {

    public static final int LINE_DOT_WIDTH = 576;
    public static final int CHAR_WIDTH= 48;



    public static final float NORMAL_TEXT_SIZE   = LINE_DOT_WIDTH /CHAR_WIDTH *1.7f;



    private int bitmapWidth = LINE_DOT_WIDTH;
    public static int textColor = Color.BLACK;

    private Bitmap image;




    boolean negativePrint = false;
    boolean centerPrint = false;
    boolean rightPrint = false;

    private PrintableDocument printableDocument;

    private static boolean USE_TTF= false;

    private int CHAR_DOT_H=26;



    BitmapPrinter(Context context)
    {
        FontUtils.loadFonts(context);
    }
    /**
     * Crea la Bitmap
     *
     * @return true se è stata creata la bitmap
     */
    public boolean textAsBitmap(PrintableDocument printableDocument) {

        this.printableDocument=printableDocument;

        int bitmapHeight= calculateBitmapHeight();

        if(bitmapHeight == 0) return false;

        //creo bitmap e canvas
        image = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(image);
        canvas.drawColor(Color.WHITE);

        renderBitmap(canvas);

        return true;
    }

    /**
     * Ritorna l'altezza precisa della Bitmap
     */
    private int calculateBitmapHeight(){

        if (USE_TTF)
            return renderBitmap(null);
        else
            return printableDocument.getHeightUnits()*CHAR_DOT_H;
    }

    private int renderBitmap(Canvas canvas) {

        int bitmapHeight= 0;
        Paint paint;

        int layoutType;



        for (DocumentLine documentLine :printableDocument.lines)
        {


                paint = new Paint();
                paint.setTextSize(NORMAL_TEXT_SIZE);
                paint.setTextScaleX(1);

                paint.setTypeface(FontUtils.regular);
                paint.setColor(textColor);
                paint.setTextAlign(Paint.Align.LEFT);



                for (LineToken token: documentLine.tokens)
                {
                    if (token.modifierCode == PrintableDocument.FORMAT_UNDEFINED)
                        bitmapHeight = putLines(token, canvas, paint, bitmapHeight);
                    else {
                        // TODO settare il testo a seconda del formato
                        switch (token.modifierCode) {
                            case PrintableDocument.FORMAT_ALIGN_LEFT:
                                centerPrint = false;
                                rightPrint = false;
                                if(token.getText() != null)
                                    bitmapHeight = putLines(token, canvas, paint, bitmapHeight);
                                break;
                            case PrintableDocument.FORMAT_ALIGN_CENTER:
                                centerPrint = true;
                                rightPrint = false;
                                if(token.getText() != null)
                                    bitmapHeight = putLines(token, canvas, paint, bitmapHeight);

                                break;
                            case PrintableDocument.FORMAT_ALIGN_RIGHT:
                                centerPrint = false;
                                rightPrint = true;
                                if(token.getText() != null)
                                    bitmapHeight = putLines(token, canvas, paint, bitmapHeight);
                                break;
                            case FORMAT_DOUBLE_H:
                                paint.setTextSize(NORMAL_TEXT_SIZE*2);
                                paint.setTextScaleX(0.5f);
                                paint.setTypeface(FontUtils.regular);
                                if(token.getText() != null)
                                    bitmapHeight = putLines(token, canvas, paint, bitmapHeight);
                                break;
                            case PrintableDocument.FORMAT_DOUBLE_W:
                                paint.setTextScaleX(NORMAL_TEXT_SIZE);
                                paint.setTextScaleX(2.0f);
                                paint.setTypeface(FontUtils.bold);
                                if(token.getText() != null)
                                    bitmapHeight = putLines(token, canvas, paint, bitmapHeight);
                                break;
                            case PrintableDocument.FORMAT_DOUBLE_HW:
                                paint.setTextSize(NORMAL_TEXT_SIZE*2);
                                paint.setTextScaleX(1);
                                paint.setTypeface(FontUtils.bold);
                                if(token.getText() != null)
                                    bitmapHeight = putLines(token, canvas, paint, bitmapHeight);
                                break;

                            case PrintableDocument.FORMAT_NORMAL:
                                paint.setTextSize(NORMAL_TEXT_SIZE);
                                paint.setTextScaleX(1);
                                paint.setTypeface(FontUtils.bold);
                                if(token.getText() != null)
                                    bitmapHeight = putLines(token, canvas, paint, bitmapHeight);
                                break;
                            case PrintableDocument.FORMAT_NEGATIVE:
                                negativePrint = true;
                                paint.setColor(Color.WHITE);
                                if(token.getText() != null)
                                    bitmapHeight = putLines(token, canvas, paint, bitmapHeight);
                                break;
                            case PrintableDocument.FORMAT_POSITIVE:
                            case PrintableDocument.FORMAT_UNSET_NEGATIVE:
                                negativePrint = false;
                                paint.setColor(Color.BLACK);
                                if(token.getText() != null)
                                    bitmapHeight = putLines(token, canvas, paint, bitmapHeight);
                                break;
                            default:
                                if(token.getText() != null)
                                    bitmapHeight = putLines(token, canvas, paint, bitmapHeight);
                                break;
                        }
                    }
                }

        }

        return bitmapHeight;
    }

    /**
     * Ritorna la baseline per il testo
     *
     * @param paint
     * @return float baseline
     */
    private float getTextBaseline(Paint paint){

        return -paint.ascent() + 0.5f; // ascent() is negative
    }

    /**
     * Ritorn l'altezza per il testo
     *
     * @param paint
     * @return float altezza del testo
     */
    private float getTextHeight(Paint paint){

        float baseline = getTextBaseline(paint);
        return baseline + paint.descent() + 0.5f;
    }


    /**
     * Se necessario va a creare più linee
     *
     * @param lineToken
     * @param canvas
     * @param paint
     * @return int              bottom (rispetto ad image) della riga inserita
     */
    private int putLines(LineToken lineToken, Canvas canvas, Paint paint, int yOffset){

//        if(lineToken.type == LineToken.QRCODE_TYPE)
//            return putQRCode(lineToken, canvas, paint, yOffset);

        if (USE_TTF) {
            Byte[] ss = lineToken.getText().toArray(new Byte[lineToken.getText().size()]);
            byte[] sss = new byte[ss.length];
            for (int i = 0; i < ss.length; i++)
                sss[i] = ss[i].byteValue();
            String text = new String(sss);

            String[] words = text.split(" ");

            //se non ho parole vado a creare una riga vuota
            //todo controllare se è corretto farlo qui questo controllo
            if (words.length == 0) {
                return putLine(text, canvas, paint, yOffset);
            }

            ArrayList<String> parole = new ArrayList<>();

            //spezzo ulteriormente parole troppo lunghe
            for (int i = 0; i < words.length; i++) {
                if (paint.measureText(words[i]) > bitmapWidth) {
                    String remaining = words[i];
                    while (paint.measureText(remaining) > bitmapWidth) {
                        String subword = remaining;
                        while (paint.measureText(subword) > bitmapWidth) {
                            subword = subword.substring(0, subword.length() - 1);
                        }
                        parole.add(subword);
                        remaining = remaining.substring(subword.length());
                    }
                    if (remaining.length() > 0)
                        parole.add(remaining);
                } else
                    parole.add(words[i]);
            }

            int wordcount = 0;

            while (wordcount < parole.size()) {
                String line = "";

                boolean line_completed = false;
                while (!line_completed && wordcount < parole.size()) {

                    String tmp = line + parole.get(wordcount);

                    if (paint.measureText(tmp) <= bitmapWidth) {
                        line += parole.get(wordcount++) + " ";
                    } else {
                        line_completed = true;
                    }
                }

                //elimino l'eventuale ultimo spazio
                if (line.length() > 0) line = line.substring(0, line.length() - 1);

                yOffset = putLine(line, canvas, paint, yOffset);
            }

            return yOffset;
        }
        else
            /*
              in this case no need to split lines, job already done at higher layer
             */
            return putLine1(lineToken, canvas, paint, yOffset);

    }


    void putChar(int xDots,int yOffset, int charIndex, Paint paint, Canvas canvas, LineToken lineToken) {
        for (int y=0;y<CHAR_DOT_H ;y++)
            for (int x=0;x<RchFont_12_26.W_DOT;x++)
            {
                if (RchFont_12_26.getPixel(charIndex,x,y)==1)
                {
                    int xx= lineToken.getWidthUnits()*x;
                    int yy= lineToken.getHeightUnits()*y;
                 canvas.drawPoint(xDots+xx,yOffset+yy,paint);
                    if (lineToken.getModifierCode() == FORMAT_DOUBLE_W || lineToken.getModifierCode() == FORMAT_DOUBLE_HW)
                        canvas.drawPoint(xDots + xx+1, yOffset + yy, paint);
                    if (lineToken.getModifierCode() == FORMAT_DOUBLE_H || lineToken.getModifierCode() == FORMAT_DOUBLE_HW)
                        canvas.drawPoint(xDots + xx, yOffset + yy+1, paint);
                    if (lineToken.getModifierCode() == FORMAT_DOUBLE_HW)
                        canvas.drawPoint(xDots + xx+1, yOffset + yy+1, paint);
                 }
            }
    }


    int putLine1(LineToken lineToken, Canvas canvas, Paint paint, int yOffset)
    {
        for (int x=0;x<lineToken.text.size();x++)
        {
            putChar(x*lineToken.getWidthUnits()*RchFont_12_26.W_DOT,yOffset,lineToken.text.get(x),paint,canvas,lineToken);
        }
        return lineToken.getHeightUnits()* CHAR_DOT_H+yOffset;
    }


    /**
     * Va a stampare txt su una solo riga
     *
     * @param text       testo da stampare
     * @param canvas
     * @param paint
     * @return int              bottom (rispetto ad image) della riga inserita
     */
    @Nullable
    private int putLine(String text, Canvas canvas, Paint paint, int yOffset) {

        text.replaceAll("(\\r|\\n)", "");
        if(text == null || text.length() == 0)
            return yOffset;

        //creo la Bitmap e ci scrivo dentro

        float textBaseline  = getTextBaseline(paint) + yOffset;
        float textHeight      = getTextHeight(paint);
        int rowHeight    = (int)Math.ceil(textHeight);

        boolean resetAlignment = false;

        if(canvas != null) {

            ColorMatrix ma = new ColorMatrix();
            ma.setSaturation(0);
            paint.setColorFilter(new ColorMatrixColorFilter(ma));

//            if (text.equals(PrintUtils.SEPARATOR_LINE1)) {
//                text = "" + PrintUtils.CARATTERE_CORNICE_1;
//                while (paint.measureText(text) < bitmapWidth) {
//                    text += PrintUtils.CARATTERE_CORNICE_1;
//                }
//
//                centerPrint = true;
//                resetAlignment = true;
//            }
//            if (text.equals(PrintUtils.SEPARATOR_LINE2)) {
//                text = "" + PrintUtils.CARATTERE_CORNICE_2;
//                while (paint.measureText(text) < bitmapWidth) {
//                    text += PrintUtils.CARATTERE_CORNICE_2;
//                }
//
//                centerPrint = true;
//                resetAlignment = true;
//            }
//            if (text.equals(PrintUtils.SEPARATOR_LINE3)) {
//                text = "" + PrintUtils.CARATTERE_CORNICE_3;
//                while (paint.measureText(text) < bitmapWidth) {
//                    text += PrintUtils.CARATTERE_CORNICE_3;
//                }
//
//                centerPrint = true;
//                resetAlignment = true;
//            }

            float xShift = 0;

            if (centerPrint) {

                xShift = getXShiftForCenterPrint(text, paint);

            } else if (rightPrint) {

                xShift = getXShiftForRightPrint(text, paint);
            }

            if (negativePrint) {

                Paint rectanglePaint = new Paint();
                rectanglePaint.setColor(Color.BLACK);

                canvas.drawRect(xShift, yOffset, paint.measureText(text) + xShift, textHeight + yOffset, rectanglePaint);

                paint.setColor(Color.WHITE);

            }else{

                paint.setColor(textColor);
            }

            canvas.drawText(text, xShift, textBaseline, paint);

            //risistemo l'allineamento che era stato forzato al centro
            if(resetAlignment == true)
                centerPrint = false;
        }

        return rowHeight + yOffset;
    }


//    /**
//     * Se necessario va a creare più linee
//     *
//     * @param lineToken       testo da stampare
//     * @param canvas
//     * @param paint
//     * @return int              bottom (rispetto ad image) della riga inserita
//     */
//    private int putQRCode(LineToken lineToken, Canvas canvas, Paint paint, int yOffset){
//
//        String content= lineToken.s;
//
//        int size= 0;
//        int qrCodeStartPrefix;
//        int qrCodeEndPrefix;
//        int qrCodeStartPostfix;
//        String qrCodeContent;
//
//        if ((qrCodeStartPrefix = content.indexOf(PrintableDocument.QRCODE_PREFIX)) >= 0 &&
//                (qrCodeEndPrefix = content.indexOf(">")) >= 0 &&
//                (qrCodeStartPostfix = content.indexOf(PrintableDocument.QRCODE_POSTFIX)) >= 0) {
//
//            try{
//
//                //trovo la dimensione
//                size= Integer.parseInt(content.substring(qrCodeStartPrefix + PrintableDocument.QRCODE_PREFIX.length(), qrCodeEndPrefix));
//
////                //tentativo di determinare la dimensione in base agli ingervalli ma purtroppo
////                //variano da stampante a stampante
////
////                //in base all'algoritmo di correzione degli errori e alla dimensione
////                // del testo da immagazzinare determino size
////                int calculateSize= 0;
////
////                int contentLength= qrCodeContent.length() + 3;
////                if(contentLength <= 20)     //<=20
////                    size *= 21;
////                else if(contentLength < 38) //<=38
////                    size *= 25;
////                else if(contentLength < 61) //<=58
////                    size *= 29;
////                else if(contentLength < 90) //<=78
////                    size *= 33;
////                else if(contentLength < 122)//<=
////                    size *= 37;
////                else if(contentLength < 154)
////                    size *= 41;
////                else if(contentLength < 178)
////                    size *= 45;
//
//                size= bitmapWidth / 16 * size;
//
//                if(canvas != null) {
//
//                    float xShift = 0;
//
//                    if (centerPrint) {
//
//                        xShift = (bitmapWidth - size) /2;
//
//                    } else if (rightPrint) {
//
//                        xShift = bitmapWidth - size;
//                    }
//
//
//                    //trovo il contenuto
//                    qrCodeContent = content.substring(qrCodeEndPrefix + 1, qrCodeStartPostfix);
//
//                    QRCodeWriter qrCodeWriter = new QRCodeWriter();
//                    BitMatrix matrix = qrCodeWriter.encode(qrCodeContent, BarcodeFormat.QR_CODE, size, size);
//                    Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
//
//                    for (int i = 0; i < size; i++) {
//                        for (int j = 0; j < size; j++) {
//                            if (!matrix.get(i, j))
//                                bitmap.setPixel(i, j, Color.WHITE);
//                            else
//                                bitmap.setPixel(i, j, Color.BLACK);
//                        }
//                    }
//                    canvas.drawBitmap(bitmap, xShift, yOffset, paint);
//                }
//
//            }catch (Exception e){
//
//                Log.e("getQRCodeSet", "Non è stato possibile recuperare la dimensione del QRCode");
//            }
//        }
//
//
//        return yOffset + size;
//    }
//
//    public static Bitmap getBitmapFromAsset(Context context, String filePath) {
//        AssetManager assetManager = context.getAssets();
//
//        InputStream istr;
//        Bitmap bitmap = null;
//        try {
//            istr = assetManager.open(filePath);RchFont_12_26
//            bitmap = BitmapFactory.decodeStream(istr);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return bitmap;
//    }
//
    public byte[] getPixels() {

        if(image == null){

            return new byte[0];
        }

        int bitmapHeight= getHeight();

        int[] pixels = new int[bitmapWidth * bitmapHeight];

        image.getPixels(pixels, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight);

        byte[] byteArray = new byte[(int)Math.ceil(bitmapWidth * bitmapHeight / 8d)];

        for(int i=0; i<pixels.length; i++) {

            byte pixel = (byte)((pixels[i] != -1) ? 1 : 0);

            byteArray[i/8] |= pixel << (7-(i%8));
        }



        return byteArray;
    }

    /**
     * Ritorna la coordinata X da cui partire nel caso di testo centrato
     *
     * @param text      testo da centrare
     * @param paint
     * @return float
     */
    private float getXShiftForCenterPrint(String text, Paint paint){

        text.replaceAll("(\\r|\\n)", "");
        if(text == null || text.length() == 0)
            return 0;

        return (bitmapWidth - paint.measureText(text))/2;
    }

    /**
     * Ritorna la coordinata X da cui partire nel caso di testo allineato a destra
     *
     * @param text      testo da allineare a destra
     * @param paint
     * @return  float
     */
    private float getXShiftForRightPrint(String text, Paint paint){

        text.replaceAll("(\\r|\\n)", "");
        if(text == null || text.length() == 0)
            return 0;

        return (bitmapWidth - paint.measureText(text));
    }

    public int getWidth() {
        return bitmapWidth;
    }

    public int getHeight() {
        return image != null ? image.getHeight() : 0;
    }

    public Bitmap getImage() {
        return image;
    }

//    public void reset(){
//
//        if(image != null) image.recycle();
//
//        negativePrint = false;
//        centerPrint = false;
//        rightPrint = false;
//        printableFilePath= null;
//    }
//
//    public Bitmap getBitmap() {
//        return image;
//    }
}
