package org.nutritionfacts.dailydozen.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.support.v4.widget.CompoundButtonCompat;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import org.nutritionfacts.dailydozen.R;
import org.nutritionfacts.dailydozen.model.Day;
import org.nutritionfacts.dailydozen.model.Food;
import org.nutritionfacts.dailydozen.model.Servings;
import org.nutritionfacts.dailydozen.task.CalculateStreakTask;
import org.nutritionfacts.dailydozen.task.StreakTaskInput;
import org.nutritionfacts.dailydozen.view.ServingCheckBox;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class FoodCheckBoxes extends LinearLayout {
    @BindView(R.id.food_check_boxes_container)
    protected ViewGroup vgContainer;

    private List<ServingCheckBox> checkBoxes;

    private Food food;
    private Day day;

    private boolean darkModeSetting = false;

    public FoodCheckBoxes(Context context) {
        this(context, null);
    }

    public FoodCheckBoxes(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FoodCheckBoxes(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.food_check_boxes, this);
        ButterKnife.bind(this);

        // Get dark mode in shared preferences
        SharedPreferences prefs = context.getSharedPreferences("darkModePrefs", context.MODE_PRIVATE);
        darkModeSetting = prefs.getBoolean("darkMode", false);
    }

    public void setDay(Day day) {
        this.day = day;
    }

    public void setFood(Food food) {
        this.food = food;
    }

    public void setServings(final Servings servings) {
        checkBoxes = new ArrayList<>();
        createCheckBox(
                checkBoxes,
                servings != null ? servings.getServings() : 0,
                food.getRecommendedServings());

        vgContainer.removeAllViews();

        for (ServingCheckBox checkBox : checkBoxes) {
            vgContainer.addView(checkBox);
        }
    }

    private static void setCheckBoxColor(ServingCheckBox checkBox, int uncheckedColor, int checkedColor) {
        ColorStateList colorStateList = new ColorStateList(
                new int[][] {
                        new int[] { -android.R.attr.state_checked }, // unchecked
                        new int[] {  android.R.attr.state_checked }  // checked
                },
                new int[] {
                        uncheckedColor,
                        checkedColor
                }
        );

        CompoundButtonCompat.setButtonTintList(checkBox, colorStateList);
    }

    private ServingCheckBox createCheckBox(List<ServingCheckBox> checkBoxes, Integer currentServings, Integer maxServings) {
        final ServingCheckBox checkBox = new ServingCheckBox(getContext());

        // Check for dark mode
        if (darkModeSetting) {
            int grey = getResources().getColor(R.color.gray_light);
            setCheckBoxColor(checkBox, grey, grey);
        }

        checkBox.setChecked(currentServings > 0);
        checkBox.setOnCheckedChangeListener(getOnCheckedChangeListener(checkBox));
        if (maxServings > 1)
            checkBox.setNextServing(createCheckBox(checkBoxes, --currentServings, --maxServings));
        checkBoxes.add(checkBox);
        return checkBox;
    }

    private CompoundButton.OnCheckedChangeListener getOnCheckedChangeListener(final ServingCheckBox checkBox) {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkBox.onCheckChange(isChecked);
                if (isChecked) {
                    handleServingChecked();
                } else {
                    handleServingUnchecked();
                }
            }
        };
    }

    private Integer getNumberOfCheckedBoxes() {
        Integer numChecked = 0;
        for (ServingCheckBox checkbox : checkBoxes) {
            if (checkbox.isChecked()) {
                numChecked++;
            }
        }
        return numChecked;
    }

    private void handleServingChecked() {
        day = Day.createDayIfDoesNotExist(day);

        final Servings servings = Servings.createServingsIfDoesNotExist(day, food);
        final Integer numberOfCheckedBoxes = getNumberOfCheckedBoxes();

        if (servings != null && servings.getServings() != numberOfCheckedBoxes) {
            servings.setServings(numberOfCheckedBoxes);

            servings.save();
            onServingsChanged();
            Timber.d(String.format("Increased Servings for %s", servings));
        }
    }

    private void handleServingUnchecked() {
        final Servings servings = getServings();
        final Integer numberOfCheckedBoxes = getNumberOfCheckedBoxes();

        if (servings != null && servings.getServings() != numberOfCheckedBoxes) {
            servings.setServings(numberOfCheckedBoxes);

            if (servings.getServings() > 0) {
                servings.save();
                Timber.d(String.format("Decreased Servings for %s", servings));
            } else {
                Timber.d(String.format("Deleting %s", servings));
                servings.delete();
            }

            onServingsChanged();
        }
    }

    private Servings getServings() {
        return Servings.getByDateAndFood(day, food);
    }

    private void onServingsChanged() {
        new CalculateStreakTask(getContext()).execute(new StreakTaskInput(day, food));
    }
}
