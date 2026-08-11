package com.ohgiraffer.submissionbox.infrastructure.persistence;

import com.ohgiraffer.global.entity.BaseTimeEntity;
import com.ohgiraffer.submissionbox.domain.model.SubmissionBoxItem;
import com.ohgiraffer.submissionbox.domain.model.SubmissionItemType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "submission_box_item")
public class SubmissionBoxItemJpaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_box_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "submission_box_id",
            nullable = false
    )
    private SubmissionBoxJpaEntity submissionBox;

    @Column(
            name = "item_name",
            nullable = false,
            length = 100
    )
    private String itemName;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "item_type",
            nullable = false,
            length = 20
    )
    private SubmissionItemType itemType;

    @Column(
            name = "allowed_file_types",
            length = 255
    )
    private String allowedFileTypes;

    @Column(
            name = "is_required",
            nullable = false
    )
    private boolean required;

    @Column(
            name = "sort_order",
            nullable = false
    )
    private int sortOrder;

    protected SubmissionBoxItemJpaEntity() {
    }

    private SubmissionBoxItemJpaEntity(
            Long id,
            String itemName,
            SubmissionItemType itemType,
            String allowedFileTypes,
            boolean required,
            int sortOrder
    ) {
        this.id = id;
        this.itemName = itemName;
        this.itemType = itemType;
        this.allowedFileTypes = allowedFileTypes;
        this.required = required;
        this.sortOrder = sortOrder;
    }

    public static SubmissionBoxItemJpaEntity from(
            SubmissionBoxItem item
    ) {
        return new SubmissionBoxItemJpaEntity(
                item.getId(),
                item.getItemName(),
                item.getItemType(),
                item.getAllowedFileTypes(),
                item.isRequired(),
                item.getSortOrder()
        );
    }

    void updateFrom(
            SubmissionBoxItem item
    ) {
        this.itemName = item.getItemName();
        this.itemType = item.getItemType();
        this.allowedFileTypes = item.getAllowedFileTypes();
        this.required = item.isRequired();
        this.sortOrder = item.getSortOrder();
    }

    void assignSubmissionBox(
            SubmissionBoxJpaEntity submissionBox
    ) {
        this.submissionBox = submissionBox;
    }

    public SubmissionBoxItem toDomain() {
        return SubmissionBoxItem.restore(
                id,
                submissionBox.getId(),
                itemName,
                itemType,
                allowedFileTypes,
                required,
                sortOrder,
                getCreatedAt(),
                getUpdatedAt()
        );
    }

    void changeSortOrder(
            int sortOrder
    ) {
        this.sortOrder = sortOrder;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public Long getId() {
        return id;
    }
}