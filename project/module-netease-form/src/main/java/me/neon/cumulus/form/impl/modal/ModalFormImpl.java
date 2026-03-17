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
package me.neon.cumulus.form.impl.modal;

import java.util.Objects;
import me.neon.cumulus.form.ModalForm;
import me.neon.cumulus.form.impl.FormImpl;
import me.neon.cumulus.response.ModalFormResponse;
import org.jetbrains.annotations.NotNull;

public final class ModalFormImpl extends FormImpl<ModalFormResponse> implements ModalForm {

  private final String content;
  private final String button1;
  private final String button2;

  public ModalFormImpl(
      @NotNull String title,
      @NotNull String content,
      @NotNull String button1,
      @NotNull String button2) {
    super(title);
    this.content = content;
    this.button1 = button1;
    this.button2 = button2;
  }

  @Override
  public @NotNull String content() {
    return content;
  }

  @Override
  public @NotNull String button1() {
    return button1;
  }

  @Override
  public @NotNull String button2() {
    return button2;
  }

  public static final class Builder
      extends FormImpl.Builder<ModalForm.Builder, ModalForm, ModalFormResponse>
      implements ModalForm.Builder {
    private String content = "";
    private String button1 = "";
    private String button2 = "";

    public Builder content(@NotNull String content) {
      this.content = translate(content);
      return this;
    }

    public Builder button1(@NotNull String button1) {
      this.button1 = translate(button1);
      return this;
    }

    public Builder button2(@NotNull String button2) {
      this.button2 = translate(button2);
      return this;
    }

    @Override
    public @NotNull ModalForm build() {
      ModalFormImpl form = new ModalFormImpl(title, content, button1, button2);
      setResponseHandler(form, form);
      return form;
    }
  }
}
