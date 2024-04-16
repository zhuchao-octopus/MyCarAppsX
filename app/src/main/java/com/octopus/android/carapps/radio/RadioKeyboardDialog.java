package com.octopus.android.carapps.radio;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.octopus.android.carapps.R;

public class RadioKeyboardDialog extends Dialog {
    public final static int MYDIALOG_STYLE_OK_CANCEAL = 0;
    public final static int MYDIALOG_STYLE_OK = 1;
    public final static int MYDIALOG_STYLE_CANCEAL = 2;
    public final static int MYDIALOG_STYLE_SCAN_CANCEAL = 3;
    public final static int MYDIALOG_STYLE_SCAN = 4;
    public final static int MYDIALOG_STYLE_PASSWD = 5;

    private View.OnClickListener mOk;
    private View.OnClickListener mCancel;
    private int mId;
    private String mTitle;
    private String mContent;

    private TextView mDigit;

    public RadioKeyboardDialog(Context context) {
        super(context, R.style.TranslucentTheme);
    }

    public RadioKeyboardDialog(Context context, View.OnClickListener ok) {
        super(context, R.style.TranslucentTheme);
        mOk = ok;
    }

    public void show() {
        show(0);
    }

    public void show(int id) {
        mId = id;

        if (mDigit != null) {
            mDigit.setText("");
        }

        super.show();
    }

    public void hide() {
        mId = 0;
        if (mDigit != null) {
            mDigit.setText("");
        }

        super.cancel();
    }

    public String getText() {
        return mDigit.getText().toString();
    }

    private View.OnClickListener mOnClickDialogCancel = new View.OnClickListener() {
        public void onClick(View v) {
            hide();
        }
    };

    private char getChar(int id) {
        char c = '0';
        if (id == R.id.num0) {
            c = '0';
        } else if (id == R.id.num1) {
            c = '1';
        } else if (id == R.id.num2) {
            c = '2';
        } else if (id == R.id.num3) {
            c = '3';
        } else if (id == R.id.num4) {
            c = '4';
        } else if (id == R.id.num5) {
            c = '5';
        } else if (id == R.id.num6) {
            c = '6';
        } else if (id == R.id.num7) {
            c = '7';
        } else if (id == R.id.num8) {
            c = '8';
        } else if (id == R.id.num9) {
            c = '9';
        } else if (id == R.id.num_point) {
            c = '.';
        }

        return c;
    }

    private View.OnClickListener mOnClickDialogDigital = new View.OnClickListener() {
        public void onClick(View v) {
            if (mDigit.length() < 6) {
                String s = "" + mDigit.getText() + getChar(v.getId());
                mDigit.setText(s);
                //				mDigit.setText("" + mDigit.getText()
                //						+ ((TextView) v).getText().charAt(0));
            }
        }
    };

    private View.OnClickListener mOnClickDialogDel = new View.OnClickListener() {
        public void onClick(View v) {
            if (mDigit.length() > 0) {
                CharSequence s = mDigit.getText();
                s = s.subSequence(0, mDigit.length() - 1);
                mDigit.setText(s);
                //				String ss = "";
                //				for (int i = 0; i < mDigit.length(); ++i) {
                //					ss += "*";
                //				}
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.my_dialog);
        setContentView(R.layout.radio_keyboard);
        mDigit = (TextView) findViewById(R.id.result);
        findViewById(R.id.num0).setOnClickListener(mOnClickDialogDigital);
        findViewById(R.id.num1).setOnClickListener(mOnClickDialogDigital);
        findViewById(R.id.num2).setOnClickListener(mOnClickDialogDigital);
        findViewById(R.id.num3).setOnClickListener(mOnClickDialogDigital);
        findViewById(R.id.num4).setOnClickListener(mOnClickDialogDigital);
        findViewById(R.id.num5).setOnClickListener(mOnClickDialogDigital);
        findViewById(R.id.num6).setOnClickListener(mOnClickDialogDigital);
        findViewById(R.id.num7).setOnClickListener(mOnClickDialogDigital);
        findViewById(R.id.num8).setOnClickListener(mOnClickDialogDigital);
        findViewById(R.id.num9).setOnClickListener(mOnClickDialogDigital);
        findViewById(R.id.num_point).setOnClickListener(mOnClickDialogDigital);

        findViewById(R.id.num_del).setOnClickListener(mOnClickDialogDel);
        findViewById(R.id.radio_keyboard_main).setOnClickListener(mOnClickDialogCancel);

        if (mOk != null) {
            findViewById(R.id.num_ok).setOnClickListener(mOk);
        }

        return;
    }

}