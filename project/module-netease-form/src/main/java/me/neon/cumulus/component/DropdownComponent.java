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
package me.neon.cumulus.component;

import java.util.List;
import java.util.function.Function;
import me.neon.cumulus.component.impl.DropdownComponentImpl;
import org.jetbrains.annotations.NotNull;

/**
 * Dropdown component is a component that can only be used in CustomForm. With this component you
 * can choose one item from the given options in a dropdown.
 */
public interface DropdownComponent extends Component {
  static @NotNull DropdownComponent of(
      @NotNull String text, @NotNull List<String> options, int defaultOption) {
    return new DropdownComponentImpl(text, options, defaultOption);
  }

  // todo should these 'of' methods be removed in favor of the builders?

  static @NotNull Builder builder() {
    return new DropdownComponentImpl.Builder();
  }

  static @NotNull Builder builder(@NotNull String text) {
    return builder().text(text);
  }

  /**
   * Returns the list of options that will be shown in the dropdown.
   *
   * @since 1.1
   */
  @NotNull List<String> options();

  /**
   * Returns the index of the option that is selected by default.
   *
   * @since 1.1
   */
  int defaultOption();

  interface Builder {
    /**
     * Sets the text that will be shown in the component.
     *
     * @param text the text to show
     */
    Builder text(@NotNull String text);

    /**
     * Adds an option to the list of options.
     *
     * @param option the text to show in the dropdown entry
     * @param isDefault if this should become the default option
     */
    Builder option(@NotNull String option, boolean isDefault);

    /**
     * Adds an option to the list of options. This option won't become the default option, unless
     * {@link #defaultOption(int)} is called after this.
     *
     * @param option the text to show in the dropdown entry
     */
    Builder option(@NotNull String option);

    /**
     * Sets the default option of this dropdown.
     *
     * @param defaultOption the index of the option that should become the default option.
     * @throws IllegalArgumentException when the index of the default option is out of bounds
     */
    Builder defaultOption(int defaultOption) throws IllegalArgumentException;

    /** Returns the created dropdown from the given options. */
    @NotNull DropdownComponent build();

    /**
     * Translates everything given to this builder using the provided translation function, and
     * returns the created dropdown after that.
     *
     * @param translator the translation function
     */
    @NotNull DropdownComponent translateAndBuild(@NotNull Function<String, String> translator);
  }
}
