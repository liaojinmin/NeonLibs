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
package me.neon.cumulus;

import java.util.function.BiConsumer;
import me.neon.cumulus.component.Component;
import me.neon.cumulus.component.impl.DropdownComponentImpl;
import me.neon.cumulus.component.impl.InputComponentImpl;
import me.neon.cumulus.component.impl.LabelComponentImpl;
import me.neon.cumulus.component.impl.SliderComponentImpl;
import me.neon.cumulus.component.impl.StepSliderComponentImpl;
import me.neon.cumulus.component.impl.ToggleComponentImpl;
import me.neon.cumulus.component.util.ComponentType;
import me.neon.cumulus.form.Form;
import me.neon.cumulus.form.impl.FormDefinitions;
import me.neon.cumulus.form.util.FormCodec;
import me.neon.cumulus.form.util.FormType;
import me.neon.cumulus.response.FormResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Forms {
  /**
   * Translate the data that is readable by the Bedrock client into a form instance.
   *
   * @param json the json data that is readable by the client
   * @param type the form data type
   * @param <T> the result will be cast to T
   * @return the form instance holding the translated data
   */
  public static <T extends Form> @NotNull T fromJson(
      String json, FormType type, BiConsumer<T, @Nullable String> responseHandler) {
    return FormDefinitions.instance()
        .<FormCodec<T, FormResponse>>codecFor(type)
        .fromJson(json, responseHandler);
  }

  /**
   * Get the class implementing the component by the component type.
   *
   * @param type the component type
   * @return the class implementing the component
   */
  public static @NotNull Class<? extends Component> getComponentTypeImpl(
      @NotNull ComponentType type) {
    // todo do we want a component definition as well?
    switch (type) {
      case DROPDOWN:
        return DropdownComponentImpl.class;
      case INPUT:
        return InputComponentImpl.class;
      case LABEL:
        return LabelComponentImpl.class;
      case SLIDER:
        return SliderComponentImpl.class;
      case STEP_SLIDER:
        return StepSliderComponentImpl.class;
      case TOGGLE:
        return ToggleComponentImpl.class;
      default:
        throw new RuntimeException("Cannot find implementation for ComponentType " + type);
    }
  }
}
