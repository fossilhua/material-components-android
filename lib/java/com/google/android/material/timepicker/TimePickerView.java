/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.timepicker;

import com.google.android.material.R;

import static java.util.Calendar.AM;
import static java.util.Calendar.HOUR;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.PM;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.button.MaterialButtonToggleGroup.OnButtonCheckedListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.timepicker.ClockHandView.OnActionUpListener;
import com.google.android.material.timepicker.ClockHandView.OnRotateListener;
import java.util.Locale;

/**
 * The main view to display a time picker.
 *
 * <p> A time picker prompts the user to choose the time of day.
 *
 */
class TimePickerView extends ConstraintLayout implements TimePickerControls {

  interface OnPeriodChangeListener {
    void onPeriodChange(@ClockPeriod int period);
  }

  interface OnSelectionChange {
    void onSelectionChanged(@ActiveSelection int selection);
  }

  private final Chip minuteView;
  private final Chip hourView;

  private final ClockHandView clockHandView;
  private final ClockFaceView clockFace;
  private final MaterialButtonToggleGroup toggle;

  private final OnClickListener selectionListener =
      new OnClickListener() {
        @Override
        public void onClick(View v) {
          if (onSelectionChangeListener != null) {
            onSelectionChangeListener.onSelectionChanged((int) v.getTag(R.id.selection_type));
          }
        }
      };

  private OnPeriodChangeListener onPeriodChangeListener;
  private OnSelectionChange onSelectionChangeListener;

  public TimePickerView(Context context) {
    this(context, null);
  }

  public TimePickerView(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public TimePickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    LayoutInflater.from(context).inflate(R.layout.material_timepicker, this);
    clockFace = findViewById(R.id.material_clock_face);
    toggle = findViewById(R.id.material_clock_period_toggle);
    toggle.addOnButtonCheckedListener(
        new OnButtonCheckedListener() {
          @Override
          public void onButtonChecked(
              MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
            int period = checkedId == R.id.material_clock_period_pm_button ? PM : AM;
            if (onPeriodChangeListener != null && isChecked) {
              onPeriodChangeListener.onPeriodChange(period);
            }
          }
        });

    minuteView = findViewById(R.id.material_minute_tv);
    hourView = findViewById(R.id.material_hour_tv);
    clockHandView = findViewById(R.id.material_clock_hand);

    setUpDisplay();
  }

  private void setUpDisplay() {
    minuteView.setTag(R.id.selection_type, MINUTE);
    hourView.setTag(R.id.selection_type, HOUR);

    minuteView.setOnClickListener(selectionListener);
    hourView.setOnClickListener(selectionListener);
  }

  @Override
  public void setValues(String[] values) {
    clockFace.setValues(values);
  }

  @Override
  public void setHandRotation(float rotation) {
    clockHandView.setHandRotation(rotation);
  }

  public void setHandRotation(float rotation, boolean animate) {
    clockHandView.setHandRotation(rotation, animate);
  }

  public void setAnimateOnTouchUp(boolean animating) {
    clockHandView.setAnimateOnTouchUp(animating);
  }

  @Override
  @SuppressLint("DefaultLocale")
  public void updateTime(@ClockPeriod int period, int hourOfDay, int minute) {
    int checkedId = period == PM
        ? R.id.material_clock_period_pm_button
        : R.id.material_clock_period_am_button;
    toggle.check(checkedId);
    Locale current = getResources().getConfiguration().locale;
    String minuteFormatted = String.format(current, "%02d", minute);
    String hourFormatted = String.format(current, "%02d", hourOfDay);
    minuteView.setText(minuteFormatted);
    hourView.setText(hourFormatted);
  }

  @Override
  public void setActiveSelection(@ActiveSelection int selection) {
    minuteView.setChecked(selection == MINUTE);
    hourView.setChecked(selection == HOUR);
  }

  public void addOnRotateListener(OnRotateListener onRotateListener) {
    clockHandView.addOnRotateListener(onRotateListener);
  }

  public void setOnActionUpListener(OnActionUpListener onActionUpListener) {
    clockHandView.setOnActionUpListener(onActionUpListener);
  }

  void setOnPeriodChangeListener(OnPeriodChangeListener onPeriodChangeListener) {
    this.onPeriodChangeListener = onPeriodChangeListener;
  }

  void setOnSelectionChangeListener(
      OnSelectionChange onSelectionChangeListener) {
    this.onSelectionChangeListener = onSelectionChangeListener;
  }

  public void showToggle() {
    toggle.setVisibility(VISIBLE);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (toggle.getVisibility() == VISIBLE) {
      // The clock display would normally be centered, clear the constraint on one side to make
      // room for the toggle
      ConstraintSet constraintSet = new ConstraintSet();
      constraintSet.clone(this);
      boolean isLtr = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR;
      int sideToClear = isLtr ? ConstraintSet.RIGHT : ConstraintSet.LEFT;
      constraintSet.clear(R.id.material_clock_display, sideToClear);
      constraintSet.applyTo(this);
    }
  }
}
