package com.octopus.android.carapps.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.AttributeSet;
import android.util.Log;

public class BTMusicNode {
	public String name;
	public int flag;
	public int count;
	
	public BTMusicNode(String n, int f, int c) {
		name = n;
		flag = f;
		count = c;
	}
}
