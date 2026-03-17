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
package me.neon.cumulus.util;

import com.google.gson.annotations.SerializedName;
import java.util.Locale;
import java.util.Objects;
import me.neon.cumulus.util.impl.FormImageImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a form image which is used in buttons and as image for client settings. This class
 * holds a image type and data for the image type. For more information and for code examples look
 * at <a href="https://github.com/GeyserMC/Cumulus/wiki">the wiki</a>.
 */
public interface FormImage {
  /**
   * Create a FormImage with the following information.
   *
   * @param type the form image type
   * @param data the data form the form image type
   * @return a FormImage holding the given data
   */
  static @NotNull FormImage of(@NotNull Type type, @NotNull String data) {
    return new FormImageImpl(type, data);
  }

  /**
   * Create a FormImage with the following information.
   *
   * @param type the form image type
   * @param data the data form the form image type
   * @return a FormImage holding the given data
   */
  static @NotNull FormImage of(@NotNull String type, @NotNull String data) {
    Type imageType = Type.fromName(type);
    if (imageType == null) {
      throw new IllegalArgumentException("Received an unknown type '" + type + "'");
    }
    return of(imageType, data);
  }

  /** Returns the type of FormImage. */
  @NotNull Type type();

  /** Returns the data needed for the FormImage. */
  @NotNull String data();

  /**
   * An enum which has the available FormImage Types. For more information and for code examples
   * look at <a href="https://github.com/GeyserMC/Cumulus/wiki">the wiki</a>.
   */
  enum Type {
    @SerializedName("path")
    PATH,
    @SerializedName("url")
    URL;

    private static final Type[] VALUES = values();

    private final String name = name().toLowerCase(Locale.ROOT);

    public static @Nullable Type fromName(@NotNull String name) {
      Objects.requireNonNull(name, "name");
      for (Type value : VALUES) {
        if (value.typeName().equals(name)) {
          return value;
        }
      }
      return null;
    }

    public @NotNull String typeName() {
      return name;
    }
  }
}
