/*
 * Copyright (c) 2020-2023 GeyserMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/Cumulus
 */
package me.neon.cumulus.component.impl;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import me.neon.cumulus.component.StepSliderComponent;
import me.neon.cumulus.component.util.ComponentType;
import org.jetbrains.annotations.NotNull;

public final class StepSliderComponentImpl extends ComponentImpl implements StepSliderComponent {
  private final List<String> steps;

  @SerializedName("default")
  private final int defaultStep;

  public StepSliderComponentImpl(
          @NotNull String text, @NotNull List<String> steps, int defaultStep) {
    super(ComponentType.STEP_SLIDER, text);
    if (defaultStep < 0) throw new IllegalArgumentException("defaultStep cannot be negative");

    this.steps = Collections.unmodifiableList(steps);
    // todo should we allow this?
    if (defaultStep >= steps.size()) {
      defaultStep = 0;
    }
    this.defaultStep = defaultStep;
  }

  public static @NotNull Builder builder() {
    return new Builder();
  }

  public static @NotNull Builder builder(@NotNull String text) {
    return builder().text(text);
  }

  @Override
  public @NotNull List<String> steps() {
    return steps;
  }

  @Override
  public int defaultStep() {
    return defaultStep;
  }

  public static final class Builder implements StepSliderComponent.Builder {
    private final List<String> steps = new ArrayList<>();
    private String text = "";
    private int defaultStep;

    public Builder text(@NotNull String text) {
      this.text = text;
      return this;
    }

    public Builder step(@NotNull String step, boolean isDefault) {
      steps.add(step);
      if (isDefault) {
        this.defaultStep = steps.size() - 1;
      }
      return this;
    }

    public Builder step(@NotNull String step) {
      return step(step, false);
    }

    public Builder defaultStep(int defaultStep) {
      if (defaultStep < 0) throw new IllegalArgumentException("defaultStep cannot be negative");
      if (defaultStep >= steps.size()) {
        throw new IllegalArgumentException("defaultStep is out of bound");
      }
      this.defaultStep = defaultStep;
      return this;
    }

    public @NotNull StepSliderComponentImpl build() {
      return new StepSliderComponentImpl(text, steps, defaultStep);
    }

    public @NotNull StepSliderComponentImpl translateAndBuild(
        @NotNull Function<String, String> translator) {
      steps.replaceAll(translator::apply);
      return new StepSliderComponentImpl(translator.apply(text), steps, defaultStep);
    }
  }
}
