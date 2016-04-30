package org.polaric.md_colorfragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

@SuppressWarnings({"FieldCanBeLocal", "ConstantConditions"})
public class ColorChooserFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    private int mCircleSize;
    private ColorFragmentCallback mCallback;
    private GridView mGrid;
    private int mPreselect;
    private boolean mSetPreselectionColor = false;
    private int mSubIndex=-1;
    private int mTopIndex=-1;
    private boolean mInSub=false;
    private String name = "ColorFragment";

    @NonNull
    private int[] mColorsTop;
    @Nullable
    private int[][] mColorsSub;

    public ColorChooserFragment() {
        // Required stub
    }

    @NonNull
    public ColorChooserFragment preselect(@ColorInt int preselect) {
        mPreselect = preselect;
        mSetPreselectionColor = true;
        return this;
    }

    @NonNull
    public ColorChooserFragment setName(String name) {
        this.name=name;
        return this;
    }

    private void invalidate() {
        if (mGrid.getAdapter() == null) {
            mGrid.setAdapter(new ColorGridAdapter());
            mGrid.setSelector(ResourcesCompat.getDrawable(getResources(), R.drawable.md_transparent, null));
        } else ((BaseAdapter) mGrid.getAdapter()).notifyDataSetChanged();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.colorchooser, container, false);
        root.findViewById(R.id.done).setOnClickListener(this);
        root.findViewById(R.id.back).setOnClickListener(this);
        generateColors();
        int preselectColor;
        if (savedInstanceState == null) {
            if (mSetPreselectionColor) {
                preselectColor = mPreselect;
                if (preselectColor != 0) {
                    for (int topIndex = 0; topIndex < mColorsTop.length; topIndex++) {
                        if (mColorsTop[topIndex] == preselectColor) {
                            topIndex(topIndex);
                            if (mColorsSub != null) {
                                findSubIndexForColor(topIndex, preselectColor);
                            } else {
                                subIndex(5);
                            }
                            break;
                        }

                        if (mColorsSub != null) {
                            for (int subIndex = 0; subIndex < mColorsSub[topIndex].length; subIndex++) {
                                if (mColorsSub[topIndex][subIndex] == preselectColor) {
                                    topIndex(topIndex);
                                    subIndex(subIndex);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        mCircleSize = getResources().getDimensionPixelSize(R.dimen.colorchooser_circlesize);
        mGrid = (GridView) root.findViewById(R.id.grid);
        invalidate();
        return root;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof ColorFragmentCallback))
            throw new IllegalStateException("ColorChooserFragment needs to be created from an Activity implementing ColorFragmentCallback.");
        mCallback = (ColorFragmentCallback) activity;
    }



    private boolean isInSub() {
        return mInSub;
    }

    private void isInSub(boolean value) {
        mInSub=value;
    }

    private int topIndex() {
        return mTopIndex;
    }

    private void topIndex(int value) {
        if (topIndex() != value && value > -1)
            findSubIndexForColor(value, mColorsTop[value]);
        mTopIndex=value;
    }

    private int subIndex() {
        if (mColorsSub == null) return -1;
        return mSubIndex;
    }

    private void subIndex(int value) {
        if (mColorsSub == null) return;
        mSubIndex=value;
    }

    private void generateColors() {
        mColorsTop = ColorPalette.PRIMARY_COLORS;
        mColorsSub = ColorPalette.PRIMARY_COLORS_SUB;
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() != null) {
            final String[] tag = ((String) v.getTag()).split(":");
            final int index = Integer.parseInt(tag[0]);
            final int color = Integer.parseInt(tag[1]);

            if (isInSub()) {
                subIndex(index);
            } else {
                topIndex(index);
                if (mColorsSub != null && index < mColorsSub.length) {
                    isInSub(true);
                }
            }
            mCallback.onColorSelection(name,color);
        }
        if (v.getId()==R.id.done) {
            mCallback.onFragmentDone(name);
        } else if (v.getId()==R.id.back) {
            isInSub(false);
        }
        invalidate();
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getTag() != null) {
            final String[] tag = ((String) v.getTag()).split(":");
            final int color = Integer.parseInt(tag[1]);
            ((CircleView) v).showHint(color);
            return true;
        }
        return false;
    }

    public interface ColorFragmentCallback {
        void onColorSelection(@NonNull String name, @ColorInt int selectedColor);
        void onFragmentDone(@NonNull String name);
    }

    private void findSubIndexForColor(int topIndex, int color) {
        if (mColorsSub == null || mColorsSub.length - 1 < topIndex)
            return;
        int[] subColors = mColorsSub[topIndex];
        for (int subIndex = 0; subIndex < subColors.length; subIndex++) {
            if (subColors[subIndex] == color) {
                subIndex(subIndex);
                break;
            }
        }
    }

    private class ColorGridAdapter extends BaseAdapter {

        public ColorGridAdapter() {

        }

        @Override
        public int getCount() {
            if (isInSub()) return mColorsSub[topIndex()].length;
            else return mColorsTop.length;
        }

        @Override
        public Object getItem(int position) {
            if (isInSub()) return mColorsSub[topIndex()][position];
            else return mColorsTop[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressWarnings("ResourceAsColor")
        @SuppressLint("DefaultLocale")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new CircleView(getContext());
                convertView.setLayoutParams(new GridView.LayoutParams(mCircleSize, mCircleSize));
            }
            CircleView child = (CircleView) convertView;
            final int color = isInSub() ? mColorsSub[topIndex()][position] : mColorsTop[position];
            child.setBackgroundColor(color);
            if (isInSub())
                child.setSelected(subIndex() == position);
            else child.setSelected(topIndex() == position);
            child.setTag(String.format("%d:%d", position, color));
            child.setOnClickListener(ColorChooserFragment.this);
            child.setOnLongClickListener(ColorChooserFragment.this);
            return convertView;
        }
    }

}
