package com.sevenheaven.elegantunderlinespan;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.text_view);

        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        stringBuilder.append("asdfasdf");
        CharSequence text = "gfhw,zxcvpaweifnakjwwoesmasglkjq";
        stringBuilder.append(text);

        stringBuilder.append("adfadsfadfakwefadgkadjsfghalsdfgafwef");

        ElegantUnderlineSpan span = new ElegantUnderlineSpan(8, 8 + text.length() + 15, 5);
        stringBuilder.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(stringBuilder);
    }
}
