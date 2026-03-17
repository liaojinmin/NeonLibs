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
package me.neon.cumulus.form.impl.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import me.neon.cumulus.component.Component;
import me.neon.cumulus.component.DropdownComponent;
import me.neon.cumulus.component.InputComponent;
import me.neon.cumulus.component.LabelComponent;
import me.neon.cumulus.component.SliderComponent;
import me.neon.cumulus.component.StepSliderComponent;
import me.neon.cumulus.component.ToggleComponent;
import me.neon.cumulus.form.CustomForm;
import me.neon.cumulus.form.impl.FormImpl;
import me.neon.cumulus.response.CustomFormResponse;
import me.neon.cumulus.util.FormImage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CustomFormImpl extends FormImpl<CustomFormResponse> implements CustomForm {

  private final FormImage icon;
  private final List<Component> content;

  public CustomFormImpl(
          @NotNull String title, @Nullable FormImage icon, @NotNull List<Component> content) {
    super(title);
    this.icon = icon;
    this.content = Collections.unmodifiableList(content);
  }

  @Override
  public @NotNull FormImage icon() {
    return icon;
  }

  @Override
  public @NotNull List<Component> content() {
    return content;
  }

  public static final class Builder
      extends FormImpl.Builder<CustomForm.Builder, CustomForm, CustomFormResponse>
      implements CustomForm.Builder {

    private final List<Component> components = new ArrayList<>();
    private FormImage icon;

    @Override
    public Builder icon(@NotNull FormImage image) {
      icon = image;
      return this;
    }

    @Override
    public Builder icon(FormImage.@NotNull Type type, @NotNull String data) {
      icon = FormImage.of(type, data);
      return this;
    }

    @Override
    public Builder iconPath(@NotNull String path) {
      return icon(FormImage.Type.PATH, path);
    }

    @Override
    public Builder iconUrl(@NotNull String url) {
      return icon(FormImage.Type.URL, url);
    }

    @Override
    public Builder component(@NotNull Component component) {
      components.add(component);
      return this;
    }

    @Override
    public Builder optionalComponent(@NotNull Component component, boolean shouldAdd) {
      if (shouldAdd) {
        return component(component);
      }
      return addNullComponent();
    }

    @Override
    public Builder dropdown(DropdownComponent.@NotNull Builder dropdownBuilder) {
      return component(dropdownBuilder.translateAndBuild(this::translate));
    }

    @Override
    public Builder dropdown(
        @NotNull String text, @NotNull List<String> options, int defaultOption) {
      //noinspection ConstantValue
      if (defaultOption < 0) throw new IllegalArgumentException("defaultOption cannot be negative");

      List<String> optionsList = new ArrayList<>();
      for (String option : options) {
        optionsList.add(translate(option));
      }
      return component(DropdownComponent.of(translate(text), optionsList, defaultOption));
    }

    @Override
    public Builder dropdown(@NotNull String text, int defaultOption, @NotNull String... options) {
      return dropdown(text, Arrays.asList(options), defaultOption);
    }

    @Override
    public Builder dropdown(@NotNull String text, @NotNull List<String> options) {
      return dropdown(text, options, 0);
    }

    @Override
    public Builder dropdown(@NotNull String text, @NotNull String... options) {
      return dropdown(text, Arrays.asList(options), 0);
    }

    @Override
    public Builder optionalDropdown(
        @NotNull String text,
        @NotNull List<String> options,
        int defaultOption,
        boolean shouldAdd) {
      if (shouldAdd) {
        return dropdown(text, options, defaultOption);
      }
      return addNullComponent();
    }

    @Override
    public Builder optionalDropdown(
        boolean shouldAdd,
        @NotNull String text,
        int defaultOption,
        @NotNull String... options) {
      return optionalDropdown(text, Arrays.asList(options), defaultOption, shouldAdd);
    }

    @Override
    public Builder optionalDropdown(
        @NotNull String text, @NotNull List<String> options, boolean shouldAdd) {
      if (shouldAdd) {
        return dropdown(text, options);
      }
      return addNullComponent();
    }

    @Override
    public Builder optionalDropdown(
        boolean shouldAdd, @NotNull String text, @NotNull String... options) {
      return optionalDropdown(text, Arrays.asList(options), shouldAdd);
    }

    @Override
    public Builder input(
        @NotNull String text, @NotNull String placeholder, @NotNull String defaultText) {
      return component(
          InputComponent.of(translate(text), translate(placeholder), translate(defaultText)));
    }

    @Override
    public Builder input(@NotNull String text, @NotNull String placeholder) {
      return component(InputComponent.of(translate(text), translate(placeholder)));
    }

    @Override
    public Builder input(@NotNull String text) {
      return component(InputComponent.of(translate(text)));
    }

    @Override
    public Builder optionalInput(
        @NotNull String text,
        @NotNull String placeholder,
        @NotNull String defaultText,
        boolean shouldAdd) {
      if (shouldAdd) {
        return input(text, placeholder, defaultText);
      }
      return addNullComponent();
    }

    @Override
    public Builder optionalInput(
        @NotNull String text, @NotNull String placeholder, boolean shouldAdd) {
      if (shouldAdd) {
        return input(text, placeholder);
      }
      return addNullComponent();
    }

    @Override
    public Builder optionalInput(@NotNull String text, boolean shouldAdd) {
      if (shouldAdd) {
        return input(text);
      }
      return addNullComponent();
    }

    @Override
    public Builder label(@NotNull String text) {
      return component(LabelComponent.of(translate(text)));
    }

    @Override
    public Builder optionalLabel(@NotNull String text, boolean shouldAdd) {
      if (shouldAdd) {
        return label(text);
      }
      return addNullComponent();
    }

    @Override
    public Builder slider(
        @NotNull String text, float min, float max, float step, float defaultValue) {
      return component(SliderComponent.of(text, min, max, step, defaultValue));
    }

    @Override
    public Builder slider(@NotNull String text, float min, float max, float step) {
      return component(SliderComponent.of(text, min, max, step));
    }

    @Override
    public Builder slider(@NotNull String text, float min, float max) {
      return slider(text, min, max, 1);
    }

    @Override
    public Builder optionalSlider(
        @NotNull String text,
        float min,
        float max,
        float step,
        float defaultValue,
        boolean shouldAdd) {
      if (shouldAdd) {
        return slider(text, min, max, step, defaultValue);
      }
      return addNullComponent();
    }

    @Override
    public Builder optionalSlider(
        @NotNull String text, float min, float max, float step, boolean shouldAdd) {
      if (shouldAdd) {
        return slider(text, min, max, step);
      }
      return addNullComponent();
    }

    @Override
    public Builder optionalSlider(@NotNull String text, float min, float max, boolean shouldAdd) {
      if (shouldAdd) {
        return slider(text, min, max);
      }
      return addNullComponent();
    }

    @Override
    public Builder stepSlider(StepSliderComponent.@NotNull Builder stepSliderBuilder) {
      return component(stepSliderBuilder.translateAndBuild(this::translate));
    }

    @Override
    public Builder stepSlider(
        @NotNull String text, @NotNull List<String> steps, int defaultStep) {
      //noinspection ConstantValue
      if (defaultStep < 0) throw new IllegalArgumentException("defaultStep cannot be negative");

      List<String> stepsList = new ArrayList<>();
      for (String option : steps) {
        stepsList.add(translate(option));
      }
      return component(StepSliderComponent.of(translate(text), stepsList, defaultStep));
    }

    @Override
    public Builder stepSlider(@NotNull String text, int defaultStep, String... steps) {
      return stepSlider(text, Arrays.asList(steps), defaultStep);
    }

    @Override
    public Builder stepSlider(@NotNull String text, @NotNull List<String> steps) {
      return stepSlider(text, steps, 0);
    }

    @Override
    public Builder stepSlider(@NotNull String text, String... steps) {
      return stepSlider(text, Arrays.asList(steps));
    }

    @Override
    public Builder optionalStepSlider(
        @NotNull String text,
        @NotNull List<String> steps,
        int defaultStep,
        boolean shouldAdd) {
      if (shouldAdd) {
        return stepSlider(text, steps, defaultStep);
      }
      return addNullComponent();
    }

    @Override
    public Builder optionalStepSlider(
        boolean shouldAdd,
        @NotNull String text,
        int defaultStep,
        @NotNull String... steps) {
      return optionalStepSlider(text, Arrays.asList(steps), defaultStep, shouldAdd);
    }

    @Override
    public Builder optionalStepSlider(
        @NotNull String text, @NotNull List<String> steps, boolean shouldAdd) {
      if (shouldAdd) {
        return stepSlider(text, steps);
      }
      return addNullComponent();
    }

    @Override
    public Builder optionalStepSlider(
        boolean shouldAdd, @NotNull String text, @NotNull String... steps) {
      return optionalStepSlider(text, Arrays.asList(steps), shouldAdd);
    }

    @Override
    public Builder toggle(@NotNull String text, boolean defaultValue) {
      return component(ToggleComponent.of(translate(text), defaultValue));
    }

    @Override
    public Builder toggle(@NotNull String text) {
      return component(ToggleComponent.of(translate(text)));
    }

    @Override
    public Builder optionalToggle(@NotNull String text, boolean defaultValue, boolean shouldAdd) {
      if (shouldAdd) {
        return toggle(text, defaultValue);
      }
      return addNullComponent();
    }

    @Override
    public Builder optionalToggle(@NotNull String text, boolean shouldAdd) {
      if (shouldAdd) {
        return toggle(text);
      }
      return addNullComponent();
    }

    @Override
    public @NotNull CustomForm build() {
      CustomFormImpl form = new CustomFormImpl(title, icon, components);
      setResponseHandler(form, form);
      return form;
    }

    private Builder addNullComponent() {
      components.add(null);
      return this;
    }
  }
}
