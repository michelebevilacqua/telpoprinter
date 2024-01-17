package com.rch.telpoprinter3;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FontUtils {


	public static Typeface regular;
	public static Typeface condensedForPrinting;
	public static Typeface bold;

	public static void loadFonts(Context ctx) {
		regular = Typeface.createFromAsset(ctx.getAssets(), "fonts/roboto/RobotoMono-Medium.ttf");
	//	bold = Typeface.createFromAsset(ctx.getAssets(), "fonts/roboto/RobotoMono-Bold.ttf");

		bold =  Typeface.createFromAsset(ctx.getAssets(), "fonts/roboto/RobotoMono-Medium.ttf");
	}





}