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
package me.neon.cumulus.form;

import java.util.List;
import me.neon.cumulus.component.Component;
import me.neon.cumulus.component.DropdownComponent;
import me.neon.cumulus.component.StepSliderComponent;
import me.neon.cumulus.form.impl.custom.CustomFormImpl;
import me.neon.cumulus.form.util.FormBuilder;
import me.neon.cumulus.response.CustomFormResponse;
import me.neon.cumulus.util.FormImage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a CustomForm which can be shown to the client. A CustomForm is the most customisable
 * form type, you can add all component types except for buttons. For more information and for code
 * examples look at <a href="https://github.com/GeyserMC/Cumulus/wiki">the wiki</a>.
 *
 * @since 1.1
 */
public interface CustomForm extends Form {
  /** Returns a new CustomForm builder. A more friendly way of creating a Form. */
  static @NotNull Builder builder() {
    return new CustomFormImpl.Builder();
  }

  /**
   * Create a CustomForm with predefined information.
   *
   * @param title the title of the form
   * @param icon the icon of the form (optional)
   * @param content the list of components in this form
   * @return the created CustomForm instance
   */
  static @NotNull CustomForm of(
      @NotNull String title, @NotNull FormImage icon, @NotNull List<Component> content) {
    return new CustomFormImpl(title, icon, content);
  }

  /** Returns the optional icon of the form. The icon can only be seen in the servers settings. */
  @Nullable FormImage icon();

  /**
   * Returns all the components of the form. This includes optional components, which will be null
   * when they are not present.
   */
  @NotNull List<@Nullable Component> content();

  /**
   * An easy way to create a CustomForm. For more information and code examples look at <a
   * href="https://github.com/GeyserMC/Cumulus/wiki">the wiki</a>.
   */
  interface Builder extends FormBuilder<Builder, CustomForm, CustomFormResponse> {
    Builder icon(@NotNull FormImage image);

    Builder icon(FormImage.@NotNull Type type, @NotNull String data);

    Builder iconPath(@NotNull String path);

    Builder iconUrl(@NotNull String url);

    Builder component(@NotNull Component component);

    /**
     * @param component
     * @param shouldAdd
     * @return
     */
    Builder optionalComponent(@NotNull Component component, boolean shouldAdd);

    Builder dropdown(DropdownComponent.@NotNull Builder dropdownBuilder);

    Builder dropdown(
        @NotNull String text, @NotNull List<String> options, int defaultOption);

    Builder dropdown(
        @NotNull String text, int defaultOption, @NotNull String... options);

    Builder dropdown(@NotNull String text, @NotNull List<String> options);

    Builder dropdown(@NotNull String text, @NotNull String... options);

    /**
     * @param text
     * @param options
     * @param defaultOption
     * @param shouldAdd
     * @return
     */
    Builder optionalDropdown(
        @NotNull String text,
        @NotNull List<String> options,
        int defaultOption,
        boolean shouldAdd);

    /**
     * @param shouldAdd
     * @param text
     * @param defaultOption
     * @param options
     * @return
     */
    Builder optionalDropdown(
        boolean shouldAdd,
        @NotNull String text,
        int defaultOption,
        @NotNull String... options);

    /**
     * @param text
     * @param options
     * @param shouldAdd
     * @return
     */
    Builder optionalDropdown(
        @NotNull String text, @NotNull List<String> options, boolean shouldAdd);

    /**
     * @param shouldAdd
     * @param text
     * @param options
     * @return
     */
    Builder optionalDropdown(boolean shouldAdd, @NotNull String text, @NotNull String... options);

    Builder input(@NotNull String text, @NotNull String placeholder, @NotNull String defaultText);

    Builder input(@NotNull String text, @NotNull String placeholder);

    Builder input(@NotNull String text);

    /**
     * @param text
     * @param placeholder
     * @param defaultText
     * @param shouldAdd
     * @return
     */
    Builder optionalInput(
        @NotNull String text,
        @NotNull String placeholder,
        @NotNull String defaultText,
        boolean shouldAdd);

    /**
     * @param text
     * @param placeholder
     * @param shouldAdd
     * @return
     */
    Builder optionalInput(@NotNull String text, @NotNull String placeholder, boolean shouldAdd);

    /**
     * @param text
     * @param shouldAdd
     * @return
     */
    Builder optionalInput(@NotNull String text, boolean shouldAdd);

    Builder label(@NotNull String text);

    /**
     * @param text
     * @param shouldAdd
     * @return
     */
    Builder optionalLabel(@NotNull String text, boolean shouldAdd);

    Builder slider(
        @NotNull String text, float min, float max, float step, float defaultValue);

    Builder slider(@NotNull String text, float min, float max, float step);

    Builder slider(@NotNull String text, float min, float max);

    /**
     * @param text
     * @param min
     * @param max
     * @param step
     * @param defaultValue
     * @param shouldAdd
     * @return
     */
    Builder optionalSlider(
        @NotNull String text,
        float min,
        float max,
        float step,
        float defaultValue,
        boolean shouldAdd);

    /**
     * @param text
     * @param min
     * @param max
     * @param step
     * @param shouldAdd
     * @return
     */
    Builder optionalSlider(
        @NotNull String text, float min, float max, float step, boolean shouldAdd);

    /**
     * @param text
     * @param min
     * @param max
     * @param shouldAdd
     * @return
     */
    Builder optionalSlider(@NotNull String text, float min, float max, boolean shouldAdd);

    Builder stepSlider(StepSliderComponent.@NotNull Builder stepSliderBuilder);

    Builder stepSlider(
        @NotNull String text, @NotNull List<String> steps, int defaultStep);

    Builder stepSlider(
        @NotNull String text, int defaultStep, @NotNull String... steps);

    Builder stepSlider(@NotNull String text, @NotNull List<String> steps);

    Builder stepSlider(@NotNull String text, @NotNull String... steps);

    /**
     * @param text
     * @param steps
     * @param defaultStep
     * @param shouldAdd
     * @return
     */
    Builder optionalStepSlider(
        @NotNull String text,
        @NotNull List<String> steps,
        int defaultStep,
        boolean shouldAdd);

    /**
     * @param shouldAdd
     * @param text
     * @param defaultStep
     * @param steps
     * @return
     */
    Builder optionalStepSlider(
        boolean shouldAdd,
        @NotNull String text,
        int defaultStep,
        @NotNull String... steps);

    /**
     * @param text
     * @param steps
     * @param shouldAdd
     * @return
     */
    Builder optionalStepSlider(
        @NotNull String text, @NotNull List<String> steps, boolean shouldAdd);

    /**
     * @param shouldAdd
     * @param text
     * @param steps
     * @return
     */
    Builder optionalStepSlider(boolean shouldAdd, @NotNull String text, @NotNull String... steps);

    Builder toggle(@NotNull String text, boolean defaultValue);

    Builder toggle(@NotNull String text);

    /**
     * @param text
     * @param defaultValue
     * @param shouldAdd
     * @return
     */
    Builder optionalToggle(@NotNull String text, boolean defaultValue, boolean shouldAdd);

    /**
     * @param text
     * @param shouldAdd
     * @return
     */
    Builder optionalToggle(@NotNull String text, boolean shouldAdd);
  }
}
