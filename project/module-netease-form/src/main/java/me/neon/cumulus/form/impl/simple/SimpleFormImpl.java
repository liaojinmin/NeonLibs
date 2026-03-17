/*
 * Copyright (c) 2020-2024 GeyserMC
 * Licensed under the MIT license
 * @link https://github.com/GeyserMC/Cumulus
 */
package me.neon.cumulus.form.impl.simple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import me.neon.cumulus.component.ButtonComponent;
import me.neon.cumulus.form.SimpleForm;
import me.neon.cumulus.form.impl.FormImpl;
import me.neon.cumulus.response.SimpleFormResponse;
import me.neon.cumulus.util.FormImage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SimpleFormImpl extends FormImpl<SimpleFormResponse> implements SimpleForm {
  private final String content;
  private final List<ButtonComponent> buttons;

  public SimpleFormImpl(
          @NotNull String title, @NotNull String content, @NotNull List<ButtonComponent> buttons) {
    super(title);
    this.content = content;
    this.buttons = Collections.unmodifiableList(buttons);
  }

  @Override
  public @NotNull String content() {
    return content;
  }

  @Override
  public @NotNull List<ButtonComponent> buttons() {
    return buttons;
  }

  public static final class Builder
      extends FormImpl.Builder<SimpleForm.Builder, SimpleForm, SimpleFormResponse>
      implements SimpleForm.Builder {

    private final List<ButtonComponent> buttons = new ArrayList<>();
    private final Map<Integer, Consumer<SimpleFormResponse>> callbacks = new HashMap<>();
    private String content = "";

    @Override
    public Builder content(@NotNull String content) {
      this.content = translate(content);
      return this;
    }

    @Override
    public Builder button(@NotNull ButtonComponent button) {
      buttons.add(button);
      return this;
    }

    @Override
    public Builder button(
        @NotNull ButtonComponent button, @NotNull Consumer<SimpleFormResponse> callback) {
      callbacks.put(buttons.size(), callback);
      return button(button);
    }

    @Override
    public Builder button(
        @NotNull String text, FormImage.@NotNull Type type, @NotNull String data) {
      buttons.add(ButtonComponent.of(translate(text), type, data));
      return this;
    }

    @Override
    public Builder button(
        @NotNull String text,
        FormImage.@NotNull Type type,
        @NotNull String data,
        @NotNull Consumer<SimpleFormResponse> callback) {
      callbacks.put(buttons.size(), callback);
      return button(text, type, data);
    }

    @Override
    public Builder button(@NotNull String text, @Nullable FormImage image) {
      buttons.add(ButtonComponent.of(translate(text), image));
      return this;
    }

    @Override
    public Builder button(
        @NotNull String text,
        @Nullable FormImage image,
        @NotNull Consumer<SimpleFormResponse> callback) {
      callbacks.put(buttons.size(), callback);
      return button(text, image);
    }

    @Override
    public Builder button(@NotNull String text) {
      buttons.add(ButtonComponent.of(translate(text)));
      return this;
    }

    @Override
    public Builder button(
        @NotNull String text, @NotNull Consumer<SimpleFormResponse> callback) {
      callbacks.put(buttons.size(), callback);
      return button(text);
    }

    @Override
    public Builder optionalButton(
        @NotNull String text,
        FormImage.@NotNull Type type,
        @NotNull String data,
        boolean shouldAdd) {
      if (shouldAdd) {
        return button(text, type, data);
      }
      return addNullButton();
    }

    @Override
    public Builder optionalButton(
        @NotNull String text, @Nullable FormImage image, boolean shouldAdd) {
      if (shouldAdd) {
        return button(text, image);
      }
      return addNullButton();
    }

    @Override
    public Builder optionalButton(@NotNull String text, boolean shouldAdd) {
      if (shouldAdd) {
        return button(text);
      }
      return addNullButton();
    }

    @Override
    public @NotNull SimpleForm build() {
      SimpleFormImpl form = new SimpleFormImpl(title, content, buttons);
      setResponseHandler(
          form,
          form,
          valid -> {
            Consumer<SimpleFormResponse> callback = callbacks.get(valid.clickedButtonId());
            if (callback != null) {
              callback.accept(valid);
            }
          });
      return form;
    }

    private Builder addNullButton() {
      buttons.add(null);
      return this;
    }
  }
}
