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
import java.util.Objects;
import me.neon.cumulus.component.InputComponent;
import me.neon.cumulus.component.util.ComponentType;
import org.jetbrains.annotations.NotNull;

public final class InputComponentImpl extends ComponentImpl implements InputComponent {
  private final String placeholder;

  @SerializedName("default")
  private final String defaultText;

  public InputComponentImpl(
          @NotNull String text, @NotNull String placeholder, @NotNull String defaultText) {
    super(ComponentType.INPUT, text);
    this.placeholder = Objects.requireNonNull(placeholder, "placeholder");
    this.defaultText = Objects.requireNonNull(defaultText, "defaultText");
  }

  @Override
  public @NotNull String placeholder() {
    return placeholder;
  }

  @Override
  public @NotNull String defaultText() {
    return defaultText;
  }
}
