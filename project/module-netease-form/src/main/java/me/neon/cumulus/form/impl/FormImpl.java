/*
 * Copyright (c) 2024 GeyserMC
 * Licensed under the MIT license
 * @link https://github.com/GeyserMC/Cumulus
 */
package me.neon.cumulus.form.impl;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import me.neon.cumulus.form.Form;
import me.neon.cumulus.form.util.FormBuilder;
import me.neon.cumulus.response.FormResponse;
import me.neon.cumulus.response.result.FormResponseResult;
import me.neon.cumulus.response.result.InvalidFormResponseResult;
import me.neon.cumulus.response.result.ResultType;
import me.neon.cumulus.response.result.ValidFormResponseResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class FormImpl<R extends FormResponse> implements Form {
  protected Consumer<FormResponseResult<R>> responseHandler;
  protected Consumer<@Nullable String> rawResponseConsumer;

  private final String title;

  public FormImpl(@NotNull String title) {
    this.title = title;
  }

  public boolean callRawResponseConsumer(@Nullable String responseData) throws Exception {
    if (rawResponseConsumer != null) {
      rawResponseConsumer.accept(responseData);
      return true;
    }
    return false;
  }

  public void rawResponseConsumer(Consumer<@Nullable String> rawResponseConsumer) {
    this.rawResponseConsumer = rawResponseConsumer;
  }

  public void callResultHandler(@Nullable FormResponseResult<R> response) throws Exception {
    if (responseHandler != null) {
      responseHandler.accept(response);
    }
  }

  public void resultHandler(@NotNull Consumer<FormResponseResult<R>> responseHandler) {
    this.responseHandler =responseHandler;
  }

  @Override
  public @NotNull String title() {
    return title;
  }

  public abstract static class Builder<
          B extends FormBuilder<B, F, R>, F extends Form, R extends FormResponse>
      implements FormBuilder<B, F, R> {

    protected String title = "";

    protected BiFunction<String, String, String> translationHandler = null;
    protected String locale;

    protected BiConsumer<F, FormResponseResult<R>> selectedResultHandler;

    protected Consumer<F> closedResultHandlerConsumer;
    protected BiConsumer<F, InvalidFormResponseResult<R>> invalidResultHandler;
    protected BiConsumer<F, FormResponseResult<R>> closedOrInvalidResultHandler;
    protected BiConsumer<F, R> validResultHandler;

    @Override
    public B title(@NotNull String title) {
      this.title = translate(title);
      return self();
    }

    @Override
    public B translator(
        @NotNull BiFunction<String, String, String> translator, @NotNull String locale) {
      this.translationHandler = translator;
      this.locale = locale;
      return title(title);
    }

    @Override
    public B translator(@NotNull BiFunction<String, String, String> translator) {
      return translator(translator, locale);
    }

    @Override
    public B closedResultHandler(@NotNull Consumer<F> resultHandler) {
      this.closedResultHandlerConsumer = resultHandler;
      return self();
    }

    @Override
    public B closedResultHandler(@NotNull Runnable resultHandler) {

      return closedResultHandler($ -> resultHandler.run());
    }

    @Override
    public B invalidResultHandler(@NotNull Runnable resultHandler) {

      return invalidResultHandler(($, $$) -> resultHandler.run());
    }

    @Override
    public B invalidResultHandler(@NotNull Consumer<InvalidFormResponseResult<R>> resultHandler) {

      return invalidResultHandler(($, result) -> resultHandler.accept(result));
    }

    @Override
    public B invalidResultHandler(
        @NotNull BiConsumer<F, InvalidFormResponseResult<R>> resultHandler) {
      this.invalidResultHandler = resultHandler;
      return self();
    }

    @Override
    public B closedOrInvalidResultHandler(@NotNull Runnable resultHandler) {
      return closedOrInvalidResultHandler(($, $$) -> resultHandler.run());
    }

    @Override
    public B closedOrInvalidResultHandler(@NotNull Consumer<FormResponseResult<R>> resultHandler) {
      return closedOrInvalidResultHandler(($, result) -> resultHandler.accept(result));
    }

    @Override
    public B closedOrInvalidResultHandler(
        @NotNull BiConsumer<F, FormResponseResult<R>> resultHandler) {
      this.closedOrInvalidResultHandler = resultHandler;
      return self();
    }

    @Override
    public B validResultHandler(@NotNull Consumer<R> resultHandler) {
      return validResultHandler(($, result) -> resultHandler.accept(result));
    }

    @Override
    public B validResultHandler(@NotNull BiConsumer<F, R> resultHandler) {
      this.validResultHandler = resultHandler;
      return self();
    }

    @Override
    public B resultHandler(@NotNull BiConsumer<F, FormResponseResult<R>> resultHandler) {
      this.selectedResultHandler = resultHandler;
      return self();
    }

    @Override
    public B resultHandler(
        @NotNull BiConsumer<F, FormResponseResult<R>> resultHandler,
        @NotNull ResultType[] selectedTypes) {

      if (selectedTypes.length == 0) {
        return self();
      }

      EnumSet<ResultType> selected = EnumSet.noneOf(ResultType.class);
      selected.addAll(Arrays.asList(selectedTypes));

      this.selectedResultHandler =
          (form, response) -> {
            if (selected.contains(response.responseType())) {
              resultHandler.accept(form, response);
            }
          };
      return self();
    }

    @Override
    public abstract @NotNull F build();

    protected void setResponseHandler(@NotNull FormImpl<R> impl, @NotNull F form) {
      setResponseHandler(impl, form, null);
    }

    protected void setResponseHandler(
        @NotNull FormImpl<R> impl, @NotNull F form, @Nullable Consumer<R> validHandler) {
      impl.resultHandler(
          result -> {
            if (selectedResultHandler != null) {
              selectedResultHandler.accept(form, result);
            }

            if (result.isClosed()) {
              if (closedResultHandlerConsumer != null) {
                closedResultHandlerConsumer.accept(form);
              }
              if (closedOrInvalidResultHandler != null) {
                closedOrInvalidResultHandler.accept(form, result);
              }
            }

            if (result.isInvalid()) {
              if (invalidResultHandler != null) {
                invalidResultHandler.accept(form, (InvalidFormResponseResult<R>) result);
              }
              if (closedOrInvalidResultHandler != null) {
                closedOrInvalidResultHandler.accept(form, result);
              }
            }

            if (result.isValid()) {
              R response = ((ValidFormResponseResult<R>) result).response();
              if (validResultHandler != null) {
                validResultHandler.accept(form, response);
              }
              if (validHandler != null) {
                validHandler.accept(response);
              }
            }
          });
    }

    protected @NotNull String translate(@NotNull String text) {

      if (translationHandler != null && !text.isEmpty()) {
        String result = translationHandler.apply(text, locale);
        return result != null ? result : text;
      }

      return text;
    }

    @SuppressWarnings("unchecked")
    protected B self() {
      return (B) this;
    }
  }
}
