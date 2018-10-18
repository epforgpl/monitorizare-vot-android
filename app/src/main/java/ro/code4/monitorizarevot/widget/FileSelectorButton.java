package ro.code4.monitorizarevot.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import ro.code4.monitorizarevot.R;

public class FileSelectorButton extends FrameLayout {
    private ImageView icon;
    private TextView text;

    private Integer mIconId;

    public FileSelectorButton(Context context) {
        super(context);
        init(context);
    }

    public FileSelectorButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.FileSelectorButton,
                0, 0);
        try {
            mIconId = a.getResourceId(R.styleable.FileSelectorButton_icon, R.id.file_selector_icon);
        } finally {
            a.recycle();
        }

        init(context);
    }

    public FileSelectorButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FileSelectorButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.widget_file_selector, this);

        icon = findViewById(R.id.file_selector_icon);
        icon.setImageDrawable(getResources().getDrawable(mIconId));
        text = findViewById(R.id.file_selector_text);

        showIcon();
    }

    public void setText(String value) {
        if (value == null || value.isEmpty()) {
            clearText();
        } else {
            text.setText(value);
            showText();
        }
    }

    public void clearText() {
        showIcon();
    }

    private void showText() {
        text.setVisibility(VISIBLE);
        icon.setVisibility(GONE);
    }

    private void showIcon() {
        text.setVisibility(GONE);
        icon.setVisibility(VISIBLE);
    }
}
