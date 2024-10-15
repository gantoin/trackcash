package com.levodev.views.budgettags;

import com.levodev.data.BudgetTag;
import com.levodev.services.BudgetTagService;
import com.levodev.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import jakarta.annotation.security.PermitAll;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@PageTitle("Budget Tags")
@Route(value = "budget-tags/:budgetTagID?/:action?(edit)", layout = MainLayout.class)
@PermitAll
public class BudgetTagsView extends Div implements BeforeEnterObserver {

    private final String BUDGETTAG_ID = "budgetTagID";
    private final String BUDGETTAG_EDIT_ROUTE_TEMPLATE = "budget-tags/%s/edit";

    private final Grid<BudgetTag> grid = new Grid<>(BudgetTag.class, false);

    private Upload icon;
    private Image iconPreview;
    private TextField name;
    private TextField budget;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private final BeanValidationBinder<BudgetTag> binder;

    private BudgetTag budgetTag;

    private final BudgetTagService budgetTagService;

    public BudgetTagsView(BudgetTagService budgetTagService) {
        this.budgetTagService = budgetTagService;
        addClassNames("budget-tags-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        LitRenderer<BudgetTag> iconRenderer = LitRenderer.<BudgetTag>of(
                "<span style='border-radius: 50%; overflow: hidden; display: flex; align-items: center; justify-content: center; width: 64px; height: 64px'><img style='max-width: 100%' src=${item.icon} /></span>")
                .withProperty("icon", item -> {
                    if (item != null && item.getIcon() != null) {
                        return "data:image;base64," + Base64.getEncoder().encodeToString(item.getIcon());
                    } else {
                        return "";
                    }
                });
        grid.addColumn(iconRenderer).setHeader("Icon").setWidth("96px").setFlexGrow(0);

        grid.addColumn("name").setAutoWidth(true);
        grid.addColumn("budget").setAutoWidth(true);
        grid.setItems(query -> budgetTagService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(BUDGETTAG_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(BudgetTagsView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(BudgetTag.class);

        // Bind fields. This is where you'd define e.g. validation rules
        binder.forField(budget).withConverter(new StringToIntegerConverter("Only numbers are allowed")).bind("budget");

        binder.bindInstanceFields(this);

        attachImageUpload(icon, iconPreview);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.budgetTag == null) {
                    this.budgetTag = new BudgetTag();
                }
                binder.writeBean(this.budgetTag);
                budgetTagService.update(this.budgetTag);
                clearForm();
                refreshGrid();
                Notification.show("Data updated");
                UI.getCurrent().navigate(BudgetTagsView.class);
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show(
                        "Error updating the data. Somebody else has updated the record while you were making changes.");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Failed to update the data. Check again that all values are valid");
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> budgetTagId = event.getRouteParameters().get(BUDGETTAG_ID).map(Long::parseLong);
        if (budgetTagId.isPresent()) {
            Optional<BudgetTag> budgetTagFromBackend = budgetTagService.get(budgetTagId.get());
            if (budgetTagFromBackend.isPresent()) {
                populateForm(budgetTagFromBackend.get());
            } else {
                Notification.show(String.format("The requested budgetTag was not found, ID = %s", budgetTagId.get()),
                        3000, Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(BudgetTagsView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        NativeLabel iconLabel = new NativeLabel("Icon");
        iconPreview = new Image();
        iconPreview.setWidth("100%");
        icon = new Upload();
        icon.getStyle().set("box-sizing", "border-box");
        icon.getElement().appendChild(iconPreview.getElement());
        name = new TextField("Name");
        budget = new TextField("Budget");
        formLayout.add(iconLabel, icon, name, budget);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void attachImageUpload(Upload upload, Image preview) {
        ByteArrayOutputStream uploadBuffer = new ByteArrayOutputStream();
        upload.setAcceptedFileTypes("image/*");
        upload.setReceiver((fileName, mimeType) -> {
            uploadBuffer.reset();
            return uploadBuffer;
        });
        upload.addSucceededListener(e -> {
            StreamResource resource = new StreamResource(e.getFileName(),
                    () -> new ByteArrayInputStream(uploadBuffer.toByteArray()));
            preview.setSrc(resource);
            preview.setVisible(true);
            if (this.budgetTag == null) {
                this.budgetTag = new BudgetTag();
            }
            this.budgetTag.setIcon(uploadBuffer.toByteArray());
        });
        preview.setVisible(false);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(BudgetTag value) {
        this.budgetTag = value;
        binder.readBean(this.budgetTag);
        this.iconPreview.setVisible(value != null);
        if (value == null || value.getIcon() == null) {
            this.icon.clearFileList();
            this.iconPreview.setSrc("");
        } else {
            this.iconPreview.setSrc("data:image;base64," + Base64.getEncoder().encodeToString(value.getIcon()));
        }

    }
}
