package com.levodev.views.dashboard;

import com.levodev.data.domain.BudgetTag;
import com.levodev.data.domain.Transaction;
import com.levodev.services.TransactionService;
import com.levodev.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@PageTitle("Dashboard")
@Route(value = "", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class DashboardView extends Div {

    private Grid<Transaction> grid;

    private final Filters filters;
    private final TransactionService transactionService;

    public DashboardView(TransactionService transactionService) {
        this.transactionService = transactionService;
        setSizeFull();
        addClassNames("dashboard-view");

        filters = new Filters(this::refreshGrid);
        VerticalLayout layout = new VerticalLayout(createMobileFilters(), filters, createGrid());
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        add(layout);
    }

    private HorizontalLayout createMobileFilters() {
        // Mobile version
        HorizontalLayout mobileFilters = new HorizontalLayout();
        mobileFilters.setWidthFull();
        mobileFilters.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.BoxSizing.BORDER,
                LumoUtility.AlignItems.CENTER);
        mobileFilters.addClassName("mobile-filters");

        Icon mobileIcon = new Icon("lumo", "plus");
        Span filtersHeading = new Span("Filters");
        mobileFilters.add(mobileIcon, filtersHeading);
        mobileFilters.setFlexGrow(1, filtersHeading);
        mobileFilters.addClickListener(e -> {
            if (filters.getClassNames().contains("visible")) {
                filters.removeClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:plus");
            } else {
                filters.addClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:minus");
            }
        });
        return mobileFilters;
    }

    public static class Filters extends Div implements Specification<Transaction> {

        private final TextField name = new TextField("Name");
        private final NumberField amountMin = new NumberField("Amount");
        private final NumberField amountMax = new NumberField();
        private final TextField currency = new TextField("Currency");
        private final DatePicker startDate = new DatePicker("Date of transaction");
        private final DatePicker endDate = new DatePicker();
        private final MultiSelectComboBox<String> budgetTags = new MultiSelectComboBox<>("Budget tags");

        public Filters(Runnable onSearch) {
            HorizontalLayout currencyLayout = new HorizontalLayout();
            currencyLayout.getStyle().set("margin-left", "80px");
            currencyLayout.add(currency);

            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                    LumoUtility.BoxSizing.BORDER);
            name.setPlaceholder("Name of transaction");

            budgetTags.setItems("Groceries", "Utilities", "Rent", "Insurance", "Transport", "Eating out", "Other");

            // Action buttons
            Button resetBtn = new Button("Reset");
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            resetBtn.addClickListener(e -> {
                name.clear();
                amountMin.clear();
                amountMax.clear();
                currency.clear();
                startDate.clear();
                endDate.clear();
                budgetTags.clear();
                onSearch.run();
            });
            Button searchBtn = new Button("Search");
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            searchBtn.addClickListener(e -> onSearch.run());

            Div actions = new Div(resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            add(name, createAmountRangFilter(), currencyLayout, createDateRangeFilter(), budgetTags, actions);
        }

        private Component createAmountRangFilter() {
            amountMin.setPlaceholder("10.00");

            amountMax.setPlaceholder("100.00");

            // For screen readers
            amountMin.setAriaLabel("Minimum amount");
            amountMax.setAriaLabel("Maximum amount");

            FlexLayout amountComponent = new FlexLayout(amountMin, new Text(" – "), amountMax);
            amountComponent.setAlignItems(FlexComponent.Alignment.BASELINE);
            amountComponent.addClassName(LumoUtility.Gap.XSMALL);

            return amountComponent;
        }

        private Component createDateRangeFilter() {
            startDate.setPlaceholder("From");

            endDate.setPlaceholder("To");

            // For screen readers
            startDate.setAriaLabel("From date");
            endDate.setAriaLabel("To date");

            FlexLayout dateRangeComponent = new FlexLayout(startDate, new Text(" – "), endDate);
            dateRangeComponent.setAlignItems(FlexComponent.Alignment.BASELINE);
            dateRangeComponent.addClassName(LumoUtility.Gap.XSMALL);

            return dateRangeComponent;
        }

        @Override
        public Predicate toPredicate(Root<Transaction> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            List<Predicate> predicates = new ArrayList<>();

            if (!name.isEmpty()) {
                String lowerCaseFilter = name.getValue().toLowerCase();
                Predicate nameMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
                        lowerCaseFilter + "%");
                predicates.add(nameMatch);
            }
            if (!amountMin.isEmpty()) {
                String databaseColumn = "amount";
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(databaseColumn),
                        amountMin.getValue()));
            }
            if (!amountMax.isEmpty()) {
                String databaseColumn = "amount";
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get(databaseColumn),
                        amountMax.getValue()));
            }
            if (startDate.getValue() != null) {
                String databaseColumn = "date";
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(databaseColumn),
                        criteriaBuilder.literal(startDate.getValue())));
            }
            if (endDate.getValue() != null) {
                String databaseColumn = "date";
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(criteriaBuilder.literal(endDate.getValue()),
                        root.get(databaseColumn)));
            }
            if (!budgetTags.isEmpty()) {
                String databaseColumn = "budgetTag";
                List<Predicate> occupationPredicates = new ArrayList<>();
                for (String occupation : budgetTags.getValue()) {
                    occupationPredicates
                            .add(criteriaBuilder.equal(criteriaBuilder.literal(occupation), root.get(databaseColumn)));
                }
                predicates.add(criteriaBuilder.or(occupationPredicates.toArray(Predicate[]::new)));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        }

        private String ignoreCharacters(String in) {
            String result = in;
            for (int i = 0; i < ".,".length(); i++) {
                result = result.replace("" + ".,".charAt(i), "");
            }
            return result;
        }

        private Expression<String> ignoreCharacters(String characters, CriteriaBuilder criteriaBuilder,
                                                    Expression<String> inExpression) {
            Expression<String> expression = inExpression;
            for (int i = 0; i < characters.length(); i++) {
                expression = criteriaBuilder.function("replace", String.class, expression,
                        criteriaBuilder.literal(characters.charAt(i)), criteriaBuilder.literal(""));
            }
            return expression;
        }

    }

    private Component createGrid() {
        grid = new Grid<>(Transaction.class, false);
        grid.addColumn("name").setAutoWidth(true);
// Créer un NumberFormat pour formater correctement les nombres
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMinimumFractionDigits(2);

// Ajouter une colonne cachée pour le tri
        grid.addColumn(Transaction::getAmount)
                .setKey("amountSort")
                .setVisible(false);

// Modifier la colonne de montant pour utiliser la colonne de tri cachée
        grid.addColumn(new TextRenderer<>(transaction -> numberFormat.format(transaction.getAmount())))
                .setSortable(true)
                .setComparator((Transaction transaction1, Transaction transaction2)
                        -> compare(transaction1.getAmount(), transaction2.getAmount()))
                .setKey("amount")
                .setHeader("Amount")
                .setAutoWidth(true);


        grid.addColumn("currency").setWidth("50px");

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

// Ajouter une colonne cachée pour le tri
        grid.addColumn(Transaction::getDate)
                .setKey("dateSort")
                .setVisible(false);

// Modifier la colonne de date pour utiliser la colonne de tri cachée
        grid.addColumn(new TextRenderer<>(transaction ->
                        transaction.getDate().format(dateFormatter)))
                .setSortable(true)
                .setComparator((Transaction transaction1, Transaction transaction2)
                        -> compare(transaction1.getDate(), transaction2.getDate()))
                .setKey("date")
                .setAutoWidth(true);

        // Ajout d'une colonne cachée pour le tri
        grid.addColumn(Transaction::getBudgetTag)
                .setKey("budgetTagSort")
                .setVisible(false);

        // Modification de la colonne des libellés de budget pour utiliser la colonne de tri cachée
        grid.addColumn(new ComponentRenderer<>(transaction -> {
                    if (transaction.getBudgetTag() != null) {
                        Span span = new Span(transaction.getBudgetTag().getName());
                        span.getElement().setAttribute("theme", "badge " + transaction.getBudgetTag());
                        return span;
                    } else {
                        return new Span();
                    }
                }))
                .setSortable(true)
                .setComparator((Transaction transaction1, Transaction transaction2)
                        -> compare(transaction1.getBudgetTag(), transaction2.getBudgetTag()))
                .setKey("budgetTag")
                .setHeader("Budget tag")
                .setAutoWidth(true);

        grid.setItems(query -> transactionService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)),
                filters).stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        return grid;
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

    // Assurez-vous de configurer le comparateur pour être sûr que le tri se fait convenablement
    private int compare(BudgetTag budgetTag1, BudgetTag budgetTag2) {
        if (budgetTag1 == null) {
            return budgetTag2 == null ? 0 : 1;
        }
        if (budgetTag2 == null) {
            return -1;
        }
        // Comparez les noms des libellés de budget
        return budgetTag1.getName().compareTo(budgetTag2.getName());
    }

    // Assurez-vous de configurer le comparateur pour être sûr que le tri se fait convenablement
    private int compare(Float amount1, Float amount2) {
        if (amount1 == null) {
            return amount2 == null ? 0 : 1;
        }
        if (amount2 == null) {
            return -1;
        }
        return amount1.compareTo(amount2);
    }

    private int compare(LocalDateTime date1, LocalDateTime date2) {
        if (date1 == null) {
            return date2 == null ? 0 : 1;
        }
        if (date2 == null) {
            return -1;
        }
        return date1.compareTo(date2);
    }

}
