/*
 * Copyright (c) 2020-2024 GeyserMC
 * Licensed under the MIT license
 * @link https://github.com/GeyserMC/Cumulus
 */
package me.neon.cumulus.form;

import java.util.List;
import java.util.function.Consumer;
import me.neon.cumulus.component.ButtonComponent;
import me.neon.cumulus.form.impl.simple.SimpleFormImpl;
import me.neon.cumulus.form.util.FormBuilder;
import me.neon.cumulus.response.SimpleFormResponse;
import me.neon.cumulus.util.FormImage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a SimpleForm which can be shown to the client. A SimpleForm is a simple but handy Form
 * type. It is a list of buttons which can have images. For more information and for code examples
 * look at <a href="https://github.com/GeyserMC/Cumulus/wiki">the wiki</a>.
 *
 * @since 1.1
 */
public interface SimpleForm extends Form {
  /** Returns a new SimpleForm builder. A more friendly way of creating a Form. */
  static @NotNull Builder builder() {
    return new SimpleFormImpl.Builder();
  }

  /**
   * Create a SimpleForm with predefined information.
   *
   * @param title the title of the form
   * @param content the description of the form (under title, above the buttons)
   * @param buttons the list of buttons to place in the form
   * @return the created SimpleForm instance
   */
  static @NotNull SimpleForm of(
      @NotNull String title, @NotNull String content, @NotNull List<ButtonComponent> buttons) {
    return new SimpleFormImpl(title, content, buttons);
  }

  /** Returns the description of the Form. */
  @NotNull String content();

  /**
   * Returns all the components of the form. This includes optional components, which will be null
   * when they are not present.
   */
  @NotNull List<@Nullable ButtonComponent> buttons();

  /**
   * An easy way to create a CustomForm. For more information and code examples look at <a
   * href="https://github.com/GeyserMC/Cumulus/wiki">the wiki</a>.
   */
  interface Builder extends FormBuilder<Builder, SimpleForm, SimpleFormResponse> {
    /**
     * Set the description of the Form.
     *
     * @param content the description of the Form
     * @return the form builder
     */
    Builder content(@NotNull String content);

    /**
     * Adds a button directly to the form.
     *
     * @param button the button to add
     * @return the form builder
     */
    Builder button(@NotNull ButtonComponent button);

    /**
     * Adds a button with callback directly to the form.
     *
     * @param button the button to add
     * @param callback the handler when the button is clicked
     * @return the form builder
     */
    Builder button(@NotNull ButtonComponent button, @NotNull Consumer<SimpleFormResponse> callback);

    /**
     * Adds a button with image to the Form.
     *
     * @param text text of the button
     * @param type type of image
     * @param data the data for the image type
     * @return the form builder
     */
    Builder button(@NotNull String text, FormImage.@NotNull Type type, @NotNull String data);

    /**
     * Adds a button with image and callback to the Form.
     *
     * @param text text of the button
     * @param type type of image
     * @param data the data for the image type
     * @param callback the handler when the button is clicked
     * @return the form builder
     */
    Builder button(
        @NotNull String text,
        FormImage.@NotNull Type type,
        @NotNull String data,
        @NotNull Consumer<SimpleFormResponse> callback);

    /**
     * Adds a button with image to the Form.
     *
     * @param text the text of the button
     * @param image the image
     * @return the form builder
     */
    Builder button(@NotNull String text, @Nullable FormImage image);

    /**
     * Adds a button with image and callback to the Form.
     *
     * @param text the text of the button
     * @param image the image
     * @param callback the handler when the button is clicked
     * @return the form builder
     */
    Builder button(
        @NotNull String text,
        @Nullable FormImage image,
        @NotNull Consumer<SimpleFormResponse> callback);

    /**
     * Adds a button to the Form.
     *
     * @param text the text of the button
     * @return the form builder
     */
    Builder button(@NotNull String text);

    /**
     * Adds a button with callback to the Form.
     *
     * @param text the text of the button
     * @param callback the handler when the button is clicked
     * @return the form builder
     */
    Builder button(@NotNull String text, @NotNull Consumer<SimpleFormResponse> callback);

    /**
     * Adds a button with image to the Form, but only when shouldAdd is true.
     *
     * @param text text of the button
     * @param type type of image
     * @param data the data for the image type
     * @param shouldAdd if the button should be added
     * @return the form builder
     * @since 1.1
     */
    Builder optionalButton(
        @NotNull String text,
        FormImage.@NotNull Type type,
        @NotNull String data,
        boolean shouldAdd);

    /**
     * Adds a button with image to the Form, but only when shouldAdd is true.
     *
     * @param text the text of the button
     * @param image the image
     * @param shouldAdd if the button should be added
     * @return the form builder
     * @since 1.1
     */
    Builder optionalButton(@NotNull String text, @Nullable FormImage image, boolean shouldAdd);

    /**
     * Adds a button to the Form, but only when shouldAdd is true.
     *
     * @param text the text of the button
     * @param shouldAdd if the button should be added
     * @return the form builder
     * @since 1.1
     */
    Builder optionalButton(@NotNull String text, boolean shouldAdd);
  }
}
