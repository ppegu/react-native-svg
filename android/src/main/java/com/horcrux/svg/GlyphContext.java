/*
 * Copyright (c) 2015-present, Horcrux.
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */


package com.horcrux.svg;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import java.util.ArrayList;

import javax.annotation.Nullable;

// https://www.w3.org/TR/SVG/text.html#TSpanElement
class GlyphContext {
    static final float DEFAULT_FONT_SIZE = 12f;

    private static final String KERNING = "kerning";
    private static final String FONT_SIZE = "fontSize";
    private static final String FONT_STYLE = "fontStyle";
    private static final String FONT_WEIGHT = "fontWeight";
    private static final String FONT_FAMILY = "fontFamily";
    private static final String LETTER_SPACING = "letterSpacing";

    // Empty font context map
    private static final WritableMap DEFAULT_MAP = Arguments.createMap();

    static {
        DEFAULT_MAP.putDouble(FONT_SIZE, DEFAULT_FONT_SIZE);
    }

    // Current stack (one per node push/pop)
    private final ArrayList<ReadableMap> mFontContext = new ArrayList<>();

    // Unique input attribute lists (only added if node sets a value)
    private final ArrayList<String[]> mXsContext = new ArrayList<>();
    private final ArrayList<String[]> mYsContext = new ArrayList<>();
    private final ArrayList<String[]> mDXsContext = new ArrayList<>();
    private final ArrayList<String[]> mDYsContext = new ArrayList<>();
    private final ArrayList<float[]> mRsContext = new ArrayList<>();

    // Unique index into attribute list (one per unique list)
    private final ArrayList<Integer> mXIndices = new ArrayList<>();
    private final ArrayList<Integer> mYIndices = new ArrayList<>();
    private final ArrayList<Integer> mDXIndices = new ArrayList<>();
    private final ArrayList<Integer> mDYIndices = new ArrayList<>();
    private final ArrayList<Integer> mRIndices = new ArrayList<>();

    // Index of unique context used (one per node push/pop)
    private final ArrayList<Integer> mXsIndices = new ArrayList<>();
    private final ArrayList<Integer> mYsIndices = new ArrayList<>();
    private final ArrayList<Integer> mDXsIndices = new ArrayList<>();
    private final ArrayList<Integer> mDYsIndices = new ArrayList<>();
    private final ArrayList<Integer> mRsIndices = new ArrayList<>();

    // Calculated on push context, percentage and em length depends on parent font size
    private float mFontSize = DEFAULT_FONT_SIZE;
    private ReadableMap topFont = DEFAULT_MAP;

    // Current accumulated values
    // https://www.w3.org/TR/SVG/types.html#DataTypeCoordinate
    // <coordinate> syntax is the same as that for <length>
    private float mX;
    private float mY;

    // https://www.w3.org/TR/SVG/types.html#Length
    private float mDX;
    private float mDY;

    // Current <list-of-coordinates> SVGLengthList
    // https://www.w3.org/TR/SVG/types.html#InterfaceSVGLengthList
    // https://www.w3.org/TR/SVG/types.html#DataTypeCoordinates

    // https://www.w3.org/TR/SVG/text.html#TSpanElementXAttribute
    private String[] mXs = new String[]{};

    // https://www.w3.org/TR/SVG/text.html#TSpanElementYAttribute
    private String[] mYs = new String[]{};

    // Current <list-of-lengths> SVGLengthList
    // https://www.w3.org/TR/SVG/types.html#DataTypeLengths

    // https://www.w3.org/TR/SVG/text.html#TSpanElementDXAttribute
    private String[] mDXs = new String[]{};

    // https://www.w3.org/TR/SVG/text.html#TSpanElementDYAttribute
    private String[] mDYs = new String[]{};

    // Current <list-of-numbers> SVGLengthList
    // https://www.w3.org/TR/SVG/types.html#DataTypeNumbers

    // https://www.w3.org/TR/SVG/text.html#TSpanElementRotateAttribute
    private float[] mRs = new float[]{0};

    // Current attribute list index
    private int mXsIndex;
    private int mYsIndex;
    private int mDXsIndex;
    private int mDYsIndex;
    private int mRsIndex;

    // Current value index in current attribute list
    private int mXIndex = -1;
    private int mYIndex = -1;
    private int mDXIndex = -1;
    private int mDYIndex = -1;
    private int mRIndex = -1;

    // Top index of stack
    private int mTop;

    // Constructor parameters
    private final float mScale;
    private final float mWidth;
    private final float mHeight;

    private void pushIndices() {
        mXsIndices.add(mXsIndex);
        mYsIndices.add(mYsIndex);
        mDXsIndices.add(mDXsIndex);
        mDYsIndices.add(mDYsIndex);
        mRsIndices.add(mRsIndex);
    }

    GlyphContext(float scale, float width, float height) {
        mScale = scale;
        mWidth = width;
        mHeight = height;

        mFontContext.add(DEFAULT_MAP);

        mXsContext.add(mXs);
        mYsContext.add(mYs);
        mDXsContext.add(mDXs);
        mDYsContext.add(mDYs);
        mRsContext.add(mRs);

        mXIndices.add(mXIndex);
        mYIndices.add(mYIndex);
        mDXIndices.add(mDXIndex);
        mDYIndices.add(mDYIndex);
        mRIndices.add(mRIndex);

        pushIndices();
    }

    private void reset() {
        mXsIndex = mYsIndex = mDXsIndex = mDYsIndex = mRsIndex = 0;
        mXIndex = mYIndex = mDXIndex = mDYIndex = mRIndex = -1;
        mX = mY = mDX = mDY = 0;
    }

    ReadableMap getFont() {
        return topFont;
    }

    private ReadableMap getTopOrParentFont(GroupShadowNode child) {
        if (mTop > 0) {
            return topFont;
        } else {
            GroupShadowNode parentRoot = child.getParentTextRoot();

            while (parentRoot != null) {
                ReadableMap map = parentRoot.getGlyphContext().getFont();
                if (map != DEFAULT_MAP) {
                    return map;
                }
                parentRoot = parentRoot.getParentTextRoot();
            }

            return DEFAULT_MAP;
        }
    }

    private void pushNodeAndFont(GroupShadowNode node, @Nullable ReadableMap font) {
        ReadableMap parent = getTopOrParentFont(node);
        mTop++;

        if (font == null) {
            mFontContext.add(parent);
            return;
        }

        WritableMap map = Arguments.createMap();
        mFontContext.add(map);
        topFont = map;

        if (font.hasKey(LETTER_SPACING)) {
            map.putString(LETTER_SPACING, font.getString(LETTER_SPACING));
        } else if (parent.hasKey(LETTER_SPACING)) {
            map.putString(LETTER_SPACING, parent.getString(LETTER_SPACING));
        }

        if (font.hasKey(FONT_FAMILY)) {
            map.putString(FONT_FAMILY, font.getString(FONT_FAMILY));
        } else if (parent.hasKey(FONT_FAMILY)) {
            map.putString(FONT_FAMILY, parent.getString(FONT_FAMILY));
        }

        if (font.hasKey(FONT_WEIGHT)) {
            map.putString(FONT_WEIGHT, font.getString(FONT_WEIGHT));
        } else if (parent.hasKey(FONT_WEIGHT)) {
            map.putString(FONT_WEIGHT, parent.getString(FONT_WEIGHT));
        }

        if (font.hasKey(FONT_STYLE)) {
            map.putString(FONT_STYLE, font.getString(FONT_STYLE));
        } else if (parent.hasKey(FONT_STYLE)) {
            map.putString(FONT_STYLE, parent.getString(FONT_STYLE));
        }

        if (font.hasKey(KERNING)) {
            map.putString(KERNING, font.getString(KERNING));
        } else if (parent.hasKey(KERNING)) {
            map.putString(KERNING, parent.getString(KERNING));
        }

        float parentFontSize = (float) parent.getDouble(FONT_SIZE);

        if (font.hasKey(FONT_SIZE)) {
            String string = font.getString(FONT_SIZE);
            float value = PropHelper.fromRelativeToFloat(
                string,
                parentFontSize,
                0,
                1,
                parentFontSize
            );
            map.putDouble(FONT_SIZE, value);
            mFontSize = value;
        } else {
            mFontSize = parentFontSize;
        }

        map.putDouble(FONT_SIZE, mFontSize);
    }

    void pushContext(GroupShadowNode node, @Nullable ReadableMap font) {
        pushNodeAndFont(node, font);
        pushIndices();
    }

    private String[] getStringArrayFromReadableArray(ReadableArray readableArray) {
        int size = readableArray.size();
        String[] strings = new String[size];
        for (int i = 0; i < size; i++) {
            strings[i] = readableArray.getString(i);
        }
        return strings;
    }

    private float[] getFloatArrayFromReadableArray(ReadableArray readableArray) {
        int size = readableArray.size();
        float[] floats = new float[size];
        for (int i = 0; i < size; i++) {
            String string = readableArray.getString(i);
            floats[i] = Float.valueOf(string);
        }
        return floats;
    }

    void pushContext(
        boolean reset,
        TextShadowNode node,
        @Nullable ReadableMap font,
        @Nullable ReadableArray x,
        @Nullable ReadableArray y,
        @Nullable ReadableArray deltaX,
        @Nullable ReadableArray deltaY,
        @Nullable ReadableArray rotate
    ) {
        if (reset) {
            this.reset();
        }

        pushNodeAndFont(node, font);

        if (x != null && x.size() != 0) {
            mXsIndex++;
            mXIndex = -1;
            mXIndices.add(mXIndex);
            mXs = getStringArrayFromReadableArray(x);
            mXsContext.add(mXs);
        }

        if (y != null && y.size() != 0) {
            mYsIndex++;
            mYIndex = -1;
            mYIndices.add(mYIndex);
            mYs = getStringArrayFromReadableArray(y);
            mYsContext.add(mYs);
        }

        if (deltaX != null && deltaX.size() != 0) {
            mDXsIndex++;
            mDXIndex = -1;
            mDXIndices.add(mDXIndex);
            mDXs = getStringArrayFromReadableArray(deltaX);
            mDXsContext.add(mDXs);
        }

        if (deltaY != null && deltaY.size() != 0) {
            mDYsIndex++;
            mDYIndex = -1;
            mDYIndices.add(mDYIndex);
            mDYs = getStringArrayFromReadableArray(deltaY);
            mDYsContext.add(mDYs);
        }

        if (rotate != null && rotate.size() != 0) {
            mRsIndex++;
            mRIndex = -1;
            mRIndices.add(mRIndex);
            mRs = getFloatArrayFromReadableArray(rotate);
            mRsContext.add(mRs);
        }

        pushIndices();
    }

    void popContext() {
        mFontContext.remove(mTop);
        mXsIndices.remove(mTop);
        mYsIndices.remove(mTop);
        mDXsIndices.remove(mTop);
        mDYsIndices.remove(mTop);
        mRsIndices.remove(mTop);

        mTop--;

        int x = mXsIndex;
        int y = mYsIndex;
        int dx = mDXsIndex;
        int dy = mDYsIndex;
        int r = mRsIndex;

        topFont = mFontContext.get(mTop);
        mXsIndex = mXsIndices.get(mTop);
        mYsIndex = mYsIndices.get(mTop);
        mDXsIndex = mDXsIndices.get(mTop);
        mDYsIndex = mDYsIndices.get(mTop);
        mRsIndex = mRsIndices.get(mTop);

        if (x != mXsIndex) {
            mXsContext.remove(x);
            mXs = mXsContext.get(mXsIndex);
            mXIndex = mXIndices.get(mXsIndex);
        }
        if (y != mYsIndex) {
            mYsContext.remove(y);
            mYs = mYsContext.get(mYsIndex);
            mYIndex = mYIndices.get(mYsIndex);
        }
        if (dx != mDXsIndex) {
            mDXsContext.remove(dx);
            mDXs = mDXsContext.get(mDXsIndex);
            mDXIndex = mDXIndices.get(mDXsIndex);
        }
        if (dy != mDYsIndex) {
            mDYsContext.remove(dy);
            mDYs = mDYsContext.get(mDYsIndex);
            mDYIndex = mDYIndices.get(mDYsIndex);
        }
        if (r != mRsIndex) {
            mRsContext.remove(r);
            mRs = mRsContext.get(mRsIndex);
            mRIndex = mRIndices.get(mRsIndex);
        }
    }

    private static void incrementIndices(ArrayList<Integer> indices, int topIndex) {
        for (int index = topIndex; index >= 0; index--) {
            int xIndex = indices.get(index);
            indices.set(index, xIndex + 1);
        }
    }

    // https://www.w3.org/TR/SVG11/text.html#FontSizeProperty

    /**
     * Get font size from context.
     * <p>
     * ‘font-size’
     * Value:       < absolute-size > | < relative-size > | < length > | < percentage > | inherit
     * Initial:     medium
     * Applies to:  text content elements
     * Inherited:   yes, the computed value is inherited
     * Percentages: refer to parent element's font size
     * Media:       visual
     * Animatable:  yes
     * <p>
     * This property refers to the size of the font from baseline to
     * baseline when multiple lines of text are set solid in a multiline
     * layout environment.
     * <p>
     * For SVG, if a < length > is provided without a unit identifier
     * (e.g., an unqualified number such as 128), the SVG user agent
     * processes the < length > as a height value in the current user
     * coordinate system.
     * <p>
     * If a < length > is provided with one of the unit identifiers
     * (e.g., 12pt or 10%), then the SVG user agent converts the
     * < length > into a corresponding value in the current user
     * coordinate system by applying the rules described in Units.
     * <p>
     * Except for any additional information provided in this specification,
     * the normative definition of the property is in CSS2 ([CSS2], section 15.2.4).
     */
    float getFontSize() {
        return mFontSize;
    }

    float nextX(float glyphWidth) {
        incrementIndices(mXIndices, mXsIndex);

        int nextIndex = mXIndex + 1;
        if (nextIndex < mXs.length) {
            mDX = 0;
            mXIndex = nextIndex;
            String string = mXs[nextIndex];
            mX = PropHelper.fromRelativeToFloat(string, mWidth, 0, mScale, mFontSize);
        }

        mX += glyphWidth;

        return mX;
    }

    float nextY() {
        incrementIndices(mYIndices, mYsIndex);

        int nextIndex = mYIndex + 1;
        if (nextIndex < mYs.length) {
            mDY = 0;
            mYIndex = nextIndex;
            String string = mYs[nextIndex];
            mY = PropHelper.fromRelativeToFloat(string, mHeight, 0, mScale, mFontSize);
        }

        return mY;
    }

    float nextDeltaX() {
        incrementIndices(mDXIndices, mDXsIndex);

        int nextIndex = mDXIndex + 1;
        if (nextIndex < mDXs.length) {
            mDXIndex = nextIndex;
            String string = mDXs[nextIndex];
            float val = PropHelper.fromRelativeToFloat(string, mWidth, 0, 1, mFontSize);
            mDX += val * mScale;
        }

        return mDX;
    }

    float nextDeltaY() {
        incrementIndices(mDYIndices, mDYsIndex);

        int nextIndex = mDYIndex + 1;
        if (nextIndex < mDYs.length) {
            mDYIndex = nextIndex;
            String string = mDYs[nextIndex];
            float val = PropHelper.fromRelativeToFloat(string, mHeight, 0, 1, mFontSize);
            mDY += val * mScale;
        }

        return mDY;
    }

    float nextRotation() {
        incrementIndices(mRIndices, mRsIndex);

        mRIndex = Math.min(mRIndex + 1, mRs.length - 1);

        return mRs[mRIndex];
    }

    float getWidth() {
        return mWidth;
    }

    float getHeight() {
        return mHeight;
    }
}
