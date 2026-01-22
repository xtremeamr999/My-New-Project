package com.github.mkram17.bazaarutils.ui;

import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.UISounds;
import lombok.Setter;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class SingleOptionCheckboxDropdownComponent extends FlowLayout {

    protected static final Identifier ICONS_TEXTURE = Identifier.of("owo", "textures/gui/dropdown_icons.png");
    protected final FlowLayout entries;
    protected boolean closeWhenNotHovered = false;

    protected SingleOptionCheckboxDropdownComponent(Sizing horizontalSizing) {
        super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);

        this.entries = Containers.verticalFlow(horizontalSizing, Sizing.content());
        this.entries.padding(Insets.of(1));
        this.entries.allowOverflow(true);
        this.entries.surface(Surface.flat(0xC7000000).and(Surface.blur(3, 5)).and(Surface.outline(0xFF121212)));

        this.child(this.entries);
    }

    /**
     * Open a context menu at the given location in the given screen,
     * adjusting the position if needed to avoid overflowing screen space
     *
     * @param screen        The screen on which to operate
     * @param rootComponent The root component onto which to mount the dropdown
     * @param mountFunction The mounting function to use
     * @param mouseX        The x-coordinate at which to open the dropdown
     * @param mouseY        The y-coordinate at which to open the dropdown
     * @param builder       A function to add entries to the dropdown
     */
    public static <R extends ParentComponent> SingleOptionCheckboxDropdownComponent openContextMenu(Screen screen, R rootComponent, BiConsumer<R, SingleOptionCheckboxDropdownComponent> mountFunction, double mouseX, double mouseY, Consumer<SingleOptionCheckboxDropdownComponent> builder) {
        var dropdown = new SingleOptionCheckboxDropdownComponent(Sizing.content());
        builder.accept(dropdown);

        mountFunction.accept(rootComponent, dropdown);

        int xLocation = (int) mouseX - rootComponent.x();
        int yLocation = (int) mouseY - rootComponent.y();

        if (xLocation + dropdown.width() > screen.width) {
            xLocation -= xLocation + dropdown.width() - screen.width;
        }
        if (yLocation + dropdown.height() > screen.height) {
            yLocation -= yLocation + dropdown.height() - screen.height;
        }

        dropdown.positioning(Positioning.absolute(xLocation, yLocation));

        var dismounted = new MutableBoolean(false);
        ScreenMouseEvents.beforeMouseClick(screen).register((screen_, click) -> {
            if (dismounted.isTrue() || dropdown.isInBoundingBox(click.x(), click.y())) return;

            rootComponent.removeChild(dropdown);
            dismounted.setTrue();
        });

        return dropdown;
    }

    @Override
    public ParentComponent surface(Surface surface) {
        return this.entries.surface(surface);
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(context, mouseX, mouseY, partialTicks, delta);
        if (this.closeWhenNotHovered && !this.isInBoundingBox(mouseX, mouseY)) {
            this.queue(() -> {
                this.closeWhenNotHovered(false);
                this.parent.removeChild(this);
            });
        }
    }

    @Override
    public void layout(Size space) {
        super.layout(space);

        var entries = this.entries.children();
        for (Component entry : entries) {
            if (!(entry instanceof ResizeableComponent sizeable)) continue;

            sizeable.setWidth(this.entries.width() - this.entries.padding().get().horizontal() - entry.margins().get().horizontal());
        }
    }

    public SingleOptionCheckboxDropdownComponent divider() {
        this.entries.child(new Divider());
        return this;
    }

    public SingleOptionCheckboxDropdownComponent text(Text text) {
        this.entries.child(Components.label(text).color(Color.ofFormatting(Formatting.GRAY)).margins(Insets.of(2)));
        return this;
    }

    public SingleOptionCheckboxDropdownComponent button(Text text, Consumer<SingleOptionCheckboxDropdownComponent> onClick) {
        this.entries.child(new Button(this, text, onClick).margins(Insets.of(2)));
        return this;
    }

    public SingleOptionCheckboxDropdownComponent checkbox(Text text, boolean state, Consumer<Boolean> onClick) {
        this.entries.child(new Checkbox(this, text, state, onClick).margins(Insets.of(2)));
        return this;
    }

    public SingleOptionCheckboxDropdownComponent nested(Text text, Sizing horizontalSizing, Consumer<SingleOptionCheckboxDropdownComponent> builder) {
        var nested = new SingleOptionCheckboxDropdownComponent(horizontalSizing);
        builder.accept(nested);
        this.entries.child(new NestEntry(this, text, nested).margins(Insets.of(2)));
        return this;
    }

    @Override
    public FlowLayout removeChild(Component child) {
        if (child == this.entries) {
            this.queue(() -> {
                this.closeWhenNotHovered(false);
                this.parent.removeChild(this);
            });
        }
        return super.removeChild(child);
    }

    public SingleOptionCheckboxDropdownComponent closeWhenNotHovered(boolean closeWhenNotHovered) {
        this.closeWhenNotHovered = closeWhenNotHovered;
        return this;
    }

    public boolean closeWhenNotHovered() {
        return this.closeWhenNotHovered;
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);
        UIParsing.apply(children, "entries", Function.identity(), this::parseAndApplyEntries);
        UIParsing.apply(children, "close-when-not-hovered", UIParsing::parseBool, this::closeWhenNotHovered);
    }

    protected void parseAndApplyEntries(Element container) {
        for (var node : UIParsing.allChildrenOfType(container, Node.ELEMENT_NODE)) {
            var entry = (Element) node;

            switch (entry.getNodeName()) {
                case "divider" -> this.divider();
                case "text" -> this.text(UIParsing.parseText(entry));
                case "button" -> {
                    var children = UIParsing.childElements(entry);
                    UIParsing.expectChildren(entry, children, "text");

                    var text = UIParsing.parseText(children.get("text"));
                    this.button(text, singleOptionCheckboxDropdownComponent -> {
                    });
                }
                case "checkbox" -> {
                    var children = UIParsing.childElements(entry);
                    UIParsing.expectChildren(entry, children, "text", "checked");

                    var text = UIParsing.parseText(children.get("text"));
                    var checked = UIParsing.parseBool(children.get("checked"));

                    this.checkbox(text, checked, aBoolean -> {
                    });
                }
                case "nested" -> {
                    var text = entry.getAttribute("translate").equals("true")
                            ? Text.translatable(entry.getAttribute("name"))
                            : Text.literal(entry.getAttribute("name"));
                    this.nested(text, Sizing.content(), singleOptionCheckboxDropdownComponent -> singleOptionCheckboxDropdownComponent.parseAndApplyEntries(entry));
                }
            }
        }
    }

    protected static void drawIconFromTexture(OwoUIDrawContext context, ParentComponent dropdown, int y, int u, int v) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, ICONS_TEXTURE,
                dropdown.x() + dropdown.width() - dropdown.padding().get().right() - 10, y,
                u, v,
                9, 9,
                32, 32
        );
    }

    protected interface ResizeableComponent {
        void setWidth(int width);
    }

    protected static class Divider extends BaseComponent implements ResizeableComponent {

        public Divider() {
            this.sizing(Sizing.fixed(1));
        }

        @Override
        public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
            var margins = this.margins.get();
            context.fill(
                    this.x - margins.left(),
                    this.y - margins.top(),
                    this.x + this.width + margins.right(),
                    this.y + this.height + margins.bottom(),
                    0xFF121212
            );
        }

        @Override
        public void setWidth(int width) {
            this.width = width;
        }
    }

    protected static class NestEntry extends LabelComponent {

        private final SingleOptionCheckboxDropdownComponent child;

        protected NestEntry(SingleOptionCheckboxDropdownComponent parentDropdown, Text text, SingleOptionCheckboxDropdownComponent child) {
            super(text);
            this.child = child;

            this.mouseEnter().subscribe(() -> {
                child.margins(Insets.top(this.y - parentDropdown.y));

                parentDropdown.queue(() -> {
                    parentDropdown.removeChild(child);
                    parentDropdown.child(child);
                });
            });
        }

        @Override
        public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
            super.draw(context, mouseX, mouseY, partialTicks, delta);
            drawIconFromTexture(context, this.parent, this.y, 0, 16);

            this.child.closeWhenNotHovered(!PositionedRectangle.of(this.x, this.y, this.parent.width(), this.height).isInBoundingBox(mouseX, mouseY));
        }

        @Override
        protected int determineHorizontalContentSize(Sizing sizing) {
            return super.determineHorizontalContentSize(sizing) + 17;
        }
    }

    protected static class Button extends LabelComponent implements ResizeableComponent {

        protected final SingleOptionCheckboxDropdownComponent parentDropdown;
        protected Consumer<SingleOptionCheckboxDropdownComponent> onClick;

        protected Button(SingleOptionCheckboxDropdownComponent parentDropdown, Text text, Consumer<SingleOptionCheckboxDropdownComponent> onClick) {
            super(text);
            this.onClick = onClick;
            this.parentDropdown = parentDropdown;

            this.margins(Insets.vertical(1));
            this.cursorStyle(CursorStyle.HAND);
        }

        public void setWidth(int width) {
            this.width = width;
        }

        @Override
        public boolean onMouseDown(Click click, boolean doubled) {
            super.onMouseDown(click, doubled);

            this.onClick.accept(this.parentDropdown);
            this.playInteractionSound();

            return true;
        }

        @Override
        public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
            if (this.isInBoundingBox(mouseX, mouseY)) {
                var margins = this.margins.get();
                context.fill(
                        this.x - margins.left(),
                        this.y - margins.top(),
                        this.x + this.width + margins.right(),
                        this.y + this.height + margins.bottom(),
                        0x44FFFFFF
                );
            }

            super.draw(context, mouseX, mouseY, partialTicks, delta);
        }

        protected void playInteractionSound() {
            UISounds.playButtonSound();
        }
    }

    public void disableCheckboxes() {
        for (var entry : entries.children()) {
            if (entry instanceof Checkbox checkBox) {
                checkBox.setState(false);
            }
        }
    }

    protected static class Checkbox extends Button {

        @Setter
        protected boolean state;

        public Checkbox(SingleOptionCheckboxDropdownComponent parentDropdown, Text text, boolean state, Consumer<Boolean> onClick) {
            super(parentDropdown, text, singleOptionCheckboxDropdownComponent -> {
            });

            this.state = state;
            this.onClick = singleOptionCheckboxDropdownComponent -> {
                boolean oldState = this.state;
                onClick.accept(this.state);
                this.state = !oldState;
            };
        }

        @Override
        public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
            super.draw(context, mouseX, mouseY, partialTicks, delta);
            drawIconFromTexture(context, this.parent, this.y, this.state ? 16 : 0, 0);
        }

        @Override
        protected int determineHorizontalContentSize(Sizing sizing) {
            return super.determineHorizontalContentSize(sizing) + 17;
        }

        @Override
        protected void playInteractionSound() {
            UISounds.playInteractionSound();
        }
    }
}
