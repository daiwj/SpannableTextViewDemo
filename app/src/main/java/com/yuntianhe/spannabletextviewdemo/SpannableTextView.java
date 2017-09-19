package com.yuntianhe.spannabletextviewdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ListView中使用效率不高，效率不高，效率不高
 *
 * @author dwj  2017/7/19 17:34
 */

public class SpannableTextView extends AppCompatTextView {

    public static final String TAG = SpannableTextView.class.getSimpleName();

    private final ArrayList<Span> spanList = new ArrayList<>();

    private boolean isMoved = false;
    private boolean isClickedSpan = false;

    private int mMotionX, mMotionY;
    private ViewConfiguration configuration;

    public SpannableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        configuration = ViewConfiguration.get(context);
    }

    private class Span {
        int start;
        int end;
        Object span;
        int flag;
    }

    private class SeparatorSpan extends Span {
        String separator;
    }

    private static class InnerMovementMethod extends LinkMovementMethod {

        private static InnerMovementMethod sInstance;

        public static InnerMovementMethod getInstance() {
            if (sInstance == null) {
                sInstance = new InnerMovementMethod();
            }
            return sInstance;
        }

        @Override
        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
            int action = event.getAction();

            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();

                x += widget.getScrollX();
                y += widget.getScrollY();

                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);

                ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

                if (link.length != 0) {
                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            buffer.setSpan(new BackgroundColorSpan(Color.LTGRAY),
                                    buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]),
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            Selection.setSelection(buffer,
                                    buffer.getSpanStart(link[0]),
                                    buffer.getSpanEnd(link[0]));
                            break;
                        case MotionEvent.ACTION_UP:
                            buffer.setSpan(new BackgroundColorSpan(Color.TRANSPARENT),
                                    buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]),
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            Selection.removeSelection(buffer);

                            SpannableTextView parent = (SpannableTextView) widget;
                            ((SpannableTextView) widget).isClickedSpan = !parent.isMoved; // 防止触发TextView自身点击事件
                            if (parent.isClickedSpan) {
                                link[0].onClick(widget);
                            }
                            break;
                        case MotionEvent.ACTION_CANCEL:
                            buffer.setSpan(new BackgroundColorSpan(Color.TRANSPARENT),
                                    buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]),
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            Selection.removeSelection(buffer);
                            break;
                    }
                    return true;
                } else {
                    Selection.removeSelection(buffer);
                }
            }
            return super.onTouchEvent(widget, buffer, event);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isMoved = false;
                mMotionX = x;
                mMotionY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(x - mMotionX) > configuration.getScaledTouchSlop()
                        || Math.abs(y - mMotionY) > configuration.getScaledTouchSlop()) {
                    isMoved = true;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return isClickedSpan || super.performClick();
    }

    /**
     * 设置文本内容其中文字的字体大小
     *
     * @param content 指定内容
     * @param size    字体大小
     */
    public SpannableTextView setSingleSize(String content, int size) {
        if (!TextUtils.isEmpty(getText())) {
            int start = getText().toString().indexOf(content);
            if (start > -1) {
                Span span = new Span();
                span.span = new AbsoluteSizeSpan((int) (getResources().getDisplayMetrics().density * size + 0.5f));
                span.start = start;
                span.end = start + content.length();
                span.flag = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE;
                spanList.add(span);
            }
        }
        return this;
    }

    /**
     * 设置文本内容中指定文本的字体颜色, 只能设置一处
     *
     * @param content 指定内容
     * @param color   字体颜色
     */
    public SpannableTextView setSingleColor(String content, int color) {
        if (!TextUtils.isEmpty(getText())) {
            int start = getText().toString().indexOf(content);
            if (start > -1) {
                Span span = new Span();
                span.span = new ForegroundColorSpan(color);
                span.start = start;
                span.end = start + content.length();
                span.flag = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE;
                spanList.add(span);
            }
        }
        return this;
    }

    /**
     * 设置文本内容中指定文本的字体颜色, 只能设置一处
     *
     * @param content 指定内容
     * @param color   字体颜色
     */
    public SpannableTextView setSingleColor(String content, String color) {
        return setSingleColor(content, Color.parseColor(color));
    }

    /**
     * 设置文本内容中指定文本的字体颜色，可同时设置多处，指定文本左右需要用separator分隔
     *
     * @param separator 分隔符 例如：#111#222#333#，#号为分隔符
     * @param color     字体颜色
     */
    public SpannableTextView setMultipleColor(char separator, int color) {
        final String origin = getText().toString();
        if (!TextUtils.isEmpty(origin)) {
            final String separatorStr = String.valueOf(separator);
            final List<String> matchedTexts = getMatchedTexts(origin, separatorStr);
            int fromIndex = 0;
            for (int i = 0, size = matchedTexts.size(); i < size; i++) {
                String special = matchedTexts.get(i);
                if (!TextUtils.isEmpty(special)) {
                    int start = origin.indexOf(special, fromIndex);
                    if (start > -1) {
                        SeparatorSpan span = new SeparatorSpan();
                        span.span = new ForegroundColorSpan(color);
                        span.start = start;
                        span.end = start + special.length();
                        span.separator = separatorStr;
                        span.flag = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE;
                        spanList.add(span);
                        fromIndex = span.end;
                    }
                }
            }
        }
        return this;
    }

    /**
     * 设置文本内容中指定文本的字体颜色，可同时设置多处，指定文本左右需要用separator分隔
     *
     * @param separator 分隔符 例如：#111#222#333#，#号为分隔符
     * @param color     字体颜色
     */
    public SpannableTextView setMultipleColor(char separator, String color) {
        return setMultipleColor(separator, Color.parseColor(color));
    }

    private List<String> getMatchedTexts(String origin, String pattern) {
        List<Integer> indexList = new ArrayList<>();
        List<String> textList = new ArrayList<>();
        for (int i = -1; i <= origin.lastIndexOf(pattern); ++i) {
            i = origin.indexOf(pattern, i);
            indexList.add(i);
        }
        int size = indexList.size();
        size = size % 2 == 0 ? size : size - 1;
        for (int i = 0; i < size; i += 2) {
            int start = indexList.get(i);
            int end = indexList.get(i + 1);
            textList.add(origin.substring(start + 1, end));
        }
        return textList;
    }

    /**
     * 设置文本内容中指定文本的字体颜色，能同时设置多处
     *
     * @param color 字体颜色
     * @param texts 多个文本
     */
    public SpannableTextView setMultipleColor(int color, String... texts) {
        return setMultipleColor(Arrays.asList(texts), color);
    }

    /**
     * 设置文本内容中指定文本的字体颜色，能同时设置多处
     *
     * @param color 字体颜色
     * @param texts 多个文本
     */
    public SpannableTextView setMultipleColor(String color, String... texts) {
        return setMultipleColor(Arrays.asList(texts), Color.parseColor(color));
    }

    /**
     * 设置文本内容中指定文本的字体颜色，能同时设置多处
     *
     * @param texts 多个文本
     * @param color 字体颜色
     */
    public SpannableTextView setMultipleColor(List<String> texts, String color) {
        return setMultipleColor(texts, Color.parseColor(color));
    }

    /**
     * 设置文本内容中指定文本的字体颜色，能同时设置多处
     *
     * @param texts 多个文本
     * @param color 字体颜色
     */
    public SpannableTextView setMultipleColor(List<String> texts, int color) {
        final String origin = getText().toString();
        if (!TextUtils.isEmpty(origin)) {
            int fromIndex = 0;
            for (int i = 0, size = texts.size(); i < size; i++) {
                String special = texts.get(i);
                if (!TextUtils.isEmpty(special)) {
                    int start = origin.indexOf(special, fromIndex);
                    if (start > -1) {
                        Span span = new Span();
                        span.span = new ForegroundColorSpan(color);
                        span.start = start;
                        span.end = start + special.length();
                        span.flag = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE;
                        spanList.add(span);
                        fromIndex = span.end;
                    }
                }
            }
        }
        return this;
    }

    /**
     * 设置文本内容中指定文本的字体颜色, 只能设置一处
     *
     * @param regex 正则表达式（表达式不能包含中、韩、日字符）
     * @param color   指定颜色
     */
    public SpannableTextView matchColor(String regex, int color) {
        final String origin = getText().toString();
        if (!TextUtils.isEmpty(origin)) {
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(origin);
            while (m.find()) {
                    Log.d(TAG, m.group());
                Span span = new Span();
                span.span = new ForegroundColorSpan(color);
                span.start = m.start();
                span.end = m.end();
                span.flag = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE;
                spanList.add(span);
            }
        }
        return this;
    }

    /**
     * 设置文本内容中指定文本的字体颜色, 只能设置一处
     *
     * @param content 指定内容
     * @param bitmap   制定的图片
     */
    public SpannableTextView setSingleImage(String content, Bitmap bitmap) {
        return setSingleImage(content, new BitmapDrawable(getResources(), bitmap));
    }

    /**
     * 设置文本内容中指定文本的字体颜色, 只能设置一处
     *
     * @param content 指定内容
     * @param drawable   制定的图片
     */
    public SpannableTextView setSingleImage(String content, int drawable) {
        return setSingleImage(content, ContextCompat.getDrawable(getContext(), drawable));
    }

    /**
     * 设置文本内容中指定文本的字体颜色, 只能设置一处
     *
     * @param content 指定内容
     * @param drawable   制定的图片
     */
    public SpannableTextView setSingleImage(String content, Drawable drawable) {
        final String origin = getText().toString();
        if (!TextUtils.isEmpty(origin) && drawable != null) {
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            int start = origin.indexOf(content);
            if (start > -1) {
                Span span = new Span();
                span.span = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
                span.start = start;
                span.end = start + content.length();
                span.flag = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE;
                spanList.add(span);
            }
        }
        return this;
    }

    /**
     * 设置文本内容中指定文本的字体颜色, 只能设置一处
     *
     * @param content 正则表达式（表达式不能包含中、韩、日字符）
     * @param drawable   制定的图片
     */
    public SpannableTextView setMultipleColor(String regex, int color) {
        final String origin = getText().toString();
        if (!TextUtils.isEmpty(origin)) {
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(origin);
            while (m.find()) {
                Span span = new Span();
                span.span = new ForegroundColorSpan(color);
                span.start = m.start();
                span.end = m.end();
                span.flag = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE;
                spanList.add(span);
            }
        }
        return this;
    }

    /**
     * 设置指定文本内容为超链接
     *
     * @param content  指定内容
     * @param color    字体颜色
     * @param listener 点击事件
     */
    public SpannableTextView setTextClick(final String content, final int color, final View.OnClickListener listener) {
        if (!TextUtils.isEmpty(getText())) {
            setHighlightColor(Color.RED);
            int start = getText().toString().indexOf(content);
            if (start > -1) {
                ClickableSpan clickSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        TextView textView = (TextView) widget;
                        if (listener != null) {
                            listener.onClick(textView);
                        }
                    }

                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setColor(color);
                        ds.setUnderlineText(false);
                        ds.clearShadowLayer();
                    }
                };
                setMovementMethod(InnerMovementMethod.getInstance());

                Span span = new Span();
                span.span = clickSpan;
                span.start = start;
                span.end = start + content.length();
                span.flag = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE;
                spanList.add(span);
            }
        }
        return this;
    }

    public void apply() {
        SpannableStringBuilder spannableString = new SpannableStringBuilder(getText());
        for (Span span : spanList) {
            spannableString.setSpan(span.span, span.start, span.end, span.flag);
        }

        int count = 0;
        for (Span span : spanList) {
            if (span instanceof SeparatorSpan) {
                spannableString = spannableString.replace(span.start - 1 - count, span.start - count, "");
                spannableString = spannableString.replace(span.end - 1 - count, span.end - count, "");
                count += 2;
            }
        }

        setText(spannableString, BufferType.SPANNABLE);

        spanList.clear();
    }
}
