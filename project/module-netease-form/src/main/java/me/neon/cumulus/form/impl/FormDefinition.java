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
package me.neon.cumulus.form.impl;

import java.util.Objects;
import me.neon.cumulus.form.Form;
import me.neon.cumulus.form.util.FormCodec;
import me.neon.cumulus.form.util.FormType;
import me.neon.cumulus.response.FormResponse;
import me.neon.cumulus.response.result.FormResponseResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class FormDefinition<
    F extends Form, I extends FormImpl<R>, R extends FormResponse> {
  private final FormCodec<F, R> codec;
  private final FormType formType;
  private final Class<F> formClass;
  private final Class<I> formImplClass;

  protected FormDefinition(
      @NotNull FormCodec<F, R> codec,
      @NotNull FormType formType,
      @NotNull Class<F> formClass,
      @NotNull Class<I> formImplClass) {
    this.codec = codec;
    this.formType = formType;
    this.formClass = formClass;
    this.formImplClass = formImplClass;
  }

  public final @NotNull FormCodec<F, R> codec() {
    return codec;
  }

  public void handleFormResponse(F form, @Nullable String responseData) throws Exception {
    if (!callRawResponseConsumer(form, responseData)) {
      FormResponseResult<R> result = codec().deserializeFormResponse(form, responseData);
      callResponseHandler(form, result);
    }
  }

  @SuppressWarnings("unchecked")
  protected boolean callRawResponseConsumer(F form, @Nullable String responseData)
      throws Exception {
    return ((FormImpl<R>) form).callRawResponseConsumer(responseData);
  }

  @SuppressWarnings("unchecked")
  protected void callResponseHandler(F form, FormResponseResult<R> result) throws Exception {
    ((FormImpl<R>) form).callResultHandler(result);
  }

  public final @NotNull FormType formType() {
    return formType;
  }

  public final @NotNull Class<F> formClass() {
    return formClass;
  }

  public final @NotNull Class<I> formImplClass() {
    return formImplClass;
  }
}
