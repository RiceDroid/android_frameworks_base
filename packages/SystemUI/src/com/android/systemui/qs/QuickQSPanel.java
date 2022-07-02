/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.qs;

import android.annotation.NonNull;
import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.provider.Settings;

import com.android.internal.logging.UiEventLogger;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.plugins.qs.QSTile.SignalState;
import com.android.systemui.plugins.qs.QSTile.State;
import com.android.systemui.tuner.TunerService;

import org.omnirom.omnilib.utils.OmniUtils;

/**
 * Version of QSPanel that only shows N Quick Tiles in the QS Header.
 */
public class QuickQSPanel extends QSPanel implements TunerService.Tunable {

    private static final String TAG = "QuickQSPanel";
    // A fallback value for max tiles number when setting via Tuner (parseNumTiles)
    public static final int TUNER_MAX_TILES_FALLBACK = 6;
    public static final int DEFAULT_MIN_TILES = 4;
    public static final int DEFAULT_MIN_TILES_TWO = 3;

    // Tile Columns on normal conditions
    public int mMaxColumnsPortrait = 6;
    public int mMaxColumnsLandscape = 6;
    // Tile Columns when media player is visible
    public int mMaxColumnsMediaPlayer = 4;

    private boolean mDisabledByPolicy;
    private int mMaxTiles;

    public QuickQSPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
    	boolean isLandscape = getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
	int portraitValue = Math.max(2, getResources().getInteger(R.integer.quick_settings_num_columns));
	    portraitValue = OmniUtils.getQuickQSColumnsPortrait(mContext, portraitValue);
        if (!isLandscape && portraitValue == 2) {
            mMaxTiles = Math.max(DEFAULT_MIN_TILES, getResources().getInteger(R.integer.quick_qs_panel_max_tiles));
	} else {
            mMaxTiles = Math.max(DEFAULT_MIN_TILES_TWO, getResources().getInteger(R.integer.quick_qs_panel_max_tiles));
       }
	mMaxColumnsPortrait = Math.max(2, getResources().getInteger(R.integer.quick_qs_panel_num_columns));
	mMaxColumnsPortrait = OmniUtils.getQuickQSColumnsPortrait(mContext, mMaxColumnsPortrait);
	mMaxColumnsLandscape = Math.max(4, getResources().getInteger(R.integer.quick_qs_panel_num_columns_landscape));
	mMaxColumnsLandscape = OmniUtils.getQuickQSColumnsLandscape(mContext, mMaxColumnsLandscape);
        mMaxColumnsMediaPlayer = getResources().getInteger(R.integer.quick_qs_panel_num_columns_media);
    }

    @Override
    public void setBrightnessView(@NonNull View view) {
        if (mBrightnessView != null) {
            removeView(mBrightnessView);
        }
        mBrightnessView = view;
        mAutoBrightnessView = view.findViewById(R.id.brightness_icon);
        setBrightnessViewMargin(true);
        if (mBrightnessView != null) {
            addView(mBrightnessView);
        }
    }

    View getBrightnessView() {
        return mBrightnessView;
    }

    private void setBrightnessViewMargin(boolean top) {
        if (mBrightnessView != null) {
            MarginLayoutParams lp = (MarginLayoutParams) mBrightnessView.getLayoutParams();
            if (top) {
                lp.topMargin = mContext.getResources()
                        .getDimensionPixelSize(R.dimen.qqs_top_brightness_margin_top);
                lp.bottomMargin = mContext.getResources()
                        .getDimensionPixelSize(R.dimen.qqs_top_brightness_margin_bottom);
            } else {
                lp.topMargin = mContext.getResources()
                        .getDimensionPixelSize(R.dimen.qqs_bottom_brightness_margin_top);
                lp.bottomMargin = 0;
            }
            mBrightnessView.setLayoutParams(lp);
        }
    }

    @Override
    void initialize() {
        super.initialize();
        if (mHorizontalContentContainer != null) {
            mHorizontalContentContainer.setClipChildren(false);
        }
        updateColumns();
    }

    @Override
    public TileLayout getOrCreateTileLayout() {
        return new QQSSideLabelTileLayout(mContext, this);
    }


    @Override
    protected boolean displayMediaMarginsOnMedia() {
        // Margins should be on the container to visually center the view
        return false;
    }

    @Override
    protected boolean mediaNeedsTopMargin() {
        return true;
    }

    @Override
    protected void updatePadding() {
        // QS Panel is setting a top padding by default, which we don't need.
    }

    @Override
    protected String getDumpableTag() {
        return TAG;
    }

    @Override
    protected boolean shouldShowDetail() {
        return !mExpanded;
    }

    @Override
    protected void drawTile(QSPanelControllerBase.TileRecord r, State state) {
        if (state instanceof SignalState) {
            SignalState copy = new SignalState();
            state.copyTo(copy);
            // No activity shown in the quick panel.
            copy.activityIn = false;
            copy.activityOut = false;
            state = copy;
        }
        super.drawTile(r, state);
    }

    public void updateColumns() {
        boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        int mColumnsMediaPlayer = mUsingHorizontalLayout ? 
            mMaxColumnsMediaPlayer : 
            mMaxColumnsLandscape;

        mTileLayout.setMaxColumns(isLandscape ? 
            mColumnsMediaPlayer : 
            mMaxColumnsPortrait);
    }
    
    public void setMaxTiles(int maxTiles) {
    	boolean isLandscape = getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
	int portraitValue = Math.max(2, getResources().getInteger(R.integer.quick_settings_num_columns));
	    portraitValue = OmniUtils.getQuickQSColumnsPortrait(mContext, portraitValue);
        if (!isLandscape && portraitValue == 2) {
            mMaxTiles = Math.max(DEFAULT_MIN_TILES, maxTiles);
	} else {
	    mMaxTiles = Math.max(DEFAULT_MIN_TILES_TWO, maxTiles);
       }
        
    }

    @Override
    public void onTuningChanged(String key, String newValue) {
        switch (key) {
            case QS_SHOW_BRIGHTNESS_SLIDER:
                boolean value =
                        TunerService.parseInteger(newValue, 1) > 1;
                if (mBrightnessView != null) {
                    mBrightnessView.setVisibility(value ? VISIBLE : GONE);
                }
                break;
            case QS_BRIGHTNESS_SLIDER_POSITION:
                mTop = TunerService.parseInteger(newValue, 0) == 0;
                updateBrightnessSliderPosition();
                break;
            case QS_SHOW_AUTO_BRIGHTNESS:
                if (mAutoBrightnessView != null) {
                    mAutoBrightnessView.setVisibility(mIsAutomaticBrightnessAvailable &&
                            TunerService.parseIntegerSwitch(newValue, true) ? View.VISIBLE : View.GONE);
                }
                break;
            default:
                break;
         }
    }

    private void updateBrightnessSliderPosition() {
        if (mBrightnessView == null) return;
        removeView(mBrightnessView);
        addView(mBrightnessView, mTop ? 0 : 1);
        setBrightnessViewMargin(mTop);
        if (mBrightnessRunnable != null) {
            updateResources();
            mBrightnessRunnable.run();
        }
    }

    public void setBrightnessRunnable(Runnable runnable) {
        mBrightnessRunnable = runnable;
    }

    public int getNumQuickTiles() {
        return mMaxTiles;
    }

    /**
     * Parses the String setting into the number of tiles. Defaults to
     * {@link #TUNER_MAX_TILES_FALLBACK}
     *
     * @param numTilesValue value of the setting to parse
     * @return parsed value of numTilesValue OR {@link #TUNER_MAX_TILES_FALLBACK} on error
     */
    public static int parseNumTiles(String numTilesValue) {
        try {
            return Integer.parseInt(numTilesValue);
        } catch (NumberFormatException e) {
            // Couldn't read an int from the new setting value. Use default.
            return TUNER_MAX_TILES_FALLBACK;
        }
    }

    void setDisabledByPolicy(boolean disabled) {
        if (disabled != mDisabledByPolicy) {
            mDisabledByPolicy = disabled;
            setVisibility(disabled ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Sets the visibility of this {@link QuickQSPanel}. This method has no effect when this panel
     * is disabled by policy through {@link #setDisabledByPolicy(boolean)}, and in this case the
     * visibility will always be {@link View#GONE}. This method is called externally by
     * {@link QSAnimator} only.
     */
    @Override
    public void setVisibility(int visibility) {
        if (mDisabledByPolicy) {
            if (getVisibility() == View.GONE) {
                return;
            }
            visibility = View.GONE;
        }
        super.setVisibility(visibility);
    }

    @Override
    protected QSEvent openPanelEvent() {
        return QSEvent.QQS_PANEL_EXPANDED;
    }

    @Override
    protected QSEvent closePanelEvent() {
        return QSEvent.QQS_PANEL_COLLAPSED;
    }

    @Override
    protected QSEvent tileVisibleEvent() {
        return QSEvent.QQS_TILE_VISIBLE;
    }

    static class QQSSideLabelTileLayout extends SideLabelTileLayout {

        private boolean mLastSelected;
        private QuickQSPanel mQSPanel;

        QQSSideLabelTileLayout(Context context, QuickQSPanel qsPanel) {
            super(context, null);
            mQSPanel = qsPanel;
            setClipChildren(false);
            setClipToPadding(false);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT);
            setLayoutParams(lp);
            boolean isLandscape = getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
            if (isLandscape) {
            setMaxColumns(getResourceColumnsLand());
            } else {
            setMaxColumns(getResourceColumnsPortrait());
            }
        }

        @Override
        public boolean updateResources() {
            mCellHeightResId = R.dimen.qs_quick_tile_size;
            boolean b = super.updateResources();
            mMaxAllowedRows = getResources().getInteger(R.integer.quick_qs_panel_max_rows);
            return b;
        }

        @Override
        protected void onConfigurationChanged(Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
            updateResources();
            boolean isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
             if (isLandscape) {
            	mQSPanel.setMaxTiles(getResourceColumnsLand());
             } else {
                mQSPanel.setMaxTiles(getResourceColumnsPortrait());
             }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            // Make sure to always use the correct number of rows. As it's determined by the
            // columns, just use as many as needed.
            updateMaxRows(10000, mRecords.size());
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        @Override
        public void setListening(boolean listening, UiEventLogger uiEventLogger) {
            boolean startedListening = !mListening && listening;
            super.setListening(listening, uiEventLogger);
            if (startedListening) {
                // getNumVisibleTiles() <= mRecords.size()
                for (int i = 0; i < getNumVisibleTiles(); i++) {
                    QSTile tile = mRecords.get(i).tile;
                    uiEventLogger.logWithInstanceId(QSEvent.QQS_TILE_VISIBLE, 0,
                            tile.getMetricsSpec(), tile.getInstanceId());
                }
            }
        }

        @Override
        public void setExpansion(float expansion, float proposedTranslation) {
            if (expansion > 0f && expansion < 1f) {
                return;
            }
            // The cases we must set select for marquee when QQS/QS collapsed, and QS full expanded.
            // Expansion == 0f is when QQS is fully showing (as opposed to 1f, which is QS). At this
            // point we want them to be selected so the tiles will marquee (but not at other points
            // of expansion.
            boolean selected = (expansion == 1f || proposedTranslation < 0f);
            if (mLastSelected == selected) {
                return;
            }
            // We set it as not important while we change this, so setting each tile as selected
            // will not cause them to announce themselves until the user has actually selected the
            // item.
            setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
            for (int i = 0; i < getChildCount(); i++) {
                getChildAt(i).setSelected(selected);
            }
            setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
            mLastSelected = selected;
        }

        @Override
    	public int getResourceColumnsPortrait() {
        	int resourceColumns = Math.max(2, getResources().getInteger(R.integer.quick_settings_num_columns));
        	return OmniUtils.getQuickQSColumnsPortrait(mContext, resourceColumns);
    	}
    
        @Override
    	public int getResourceColumnsLand() {
        	int resourceColumnsLand = Math.max(4, getResources().getInteger(R.integer.quick_settings_num_columns_landscape));
        	return OmniUtils.getQuickQSColumnsLandscape(mContext, resourceColumnsLand);
    	}

        @Override
        public void updateSettings() {
    	boolean isLandscape = getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
        if (isLandscape) {
            mQSPanel.setMaxTiles(getResourceColumnsLand());
        } else {
            mQSPanel.setMaxTiles(getResourceColumnsPortrait());
        }
        super.updateSettings();
        }
    }
}
